package com.hackovation.menu_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.menu_service.dto.*;
import com.hackovation.menu_service.exception.ApiException;
import com.hackovation.menu_service.feign.FeignExceptionWrapper;
import com.hackovation.menu_service.feign.RestaurantInterface;
import com.hackovation.menu_service.model.MenuCategory;
import com.hackovation.menu_service.model.MenuItem;
import com.hackovation.menu_service.model.Tag;
import com.hackovation.menu_service.repository.MenuCategoryRepository;
import com.hackovation.menu_service.repository.MenuItemRepository;
import com.hackovation.menu_service.repository.TagRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RestaurantInterface restaurantInterface;

    @Autowired
    private S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuItemEventPublisher menuItemEventPublisher;


    @Transactional(rollbackFor = {Exception.class})
    public MenuItemResponse addMenuItem(String userId, String restaurantId, MenuItemRequest menuItemRequest, MultipartFile file) throws Exception {
        try {
            // Validate MenuItemRequest

            // Get Restaurant name
            String restaurantName = getRestaurantName(restaurantId, userId);
            if (restaurantName == null) {
                throw new ApiException("Restaurant does not exist");
            }

            // Check valid category
            MenuCategory menuCategory = menuCategoryRepository.findByRestaurantIdAndName(restaurantId, menuItemRequest.getCategory());
            if (menuCategory == null) {
                throw new ApiException("Category does not exist");
            }

            // Upload image to S3
            String imageUrl = uploadImageToS3(file);

            // Create MenuItem object
            MenuItem menuItem = new MenuItem();
            menuItem.setMenuItemId(UUID.randomUUID().toString());
            menuItem.setRestaurantId(restaurantId);
            menuItem.setMenuItemName(menuItemRequest.getName());
            menuItem.setDescription(menuItemRequest.getDescription());
            menuItem.setCategory(menuCategory);
            menuItem.setPrice(menuItemRequest.getPrice());
            menuItem.setImageUrl(imageUrl);

            Set<Tag> persistentTags = menuItemRequest.getTags().stream()
                    .map(name -> tagRepository.findByName(name).orElseGet(() -> {
                        Tag tag = new Tag();
                        tag.setName(name);
                        return tagRepository.save(tag);
                    }))
                    .collect(Collectors.toSet());

            menuItem.setTags(persistentTags);
            persistentTags.forEach(tag -> tag.getMenuItems().add(menuItem));

            // Save MenuItem
            MenuItem savedMenuItem = menuItemRepository.save(menuItem);

            // Publish MenuItemCreated event to Kafka
            publishMenuItemEvent(savedMenuItem, restaurantName, "CREATE");

            return convertToMenuItemResponse(savedMenuItem);
        } catch (FeignException feignException) {
            System.out.println("Error from restaurant service: " + feignException.getMessage());
            throw new ApiException("Error from restaurant service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from restaurant service: " + error.getMessage());
            throw new ApiException("Error from restaurant service: " + error.getMessage());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error from menu service: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public MenuCategoryResponse addCategory(String restaurantId, MenuCategoryRequest menuCategoryRequest) throws ApiException {
        // Validate MenuCategoryRequest
        if (menuCategoryRepository.existsByRestaurantIdAndName(restaurantId, menuCategoryRequest.getName())) {
            throw new ApiException("Category already exists");
        }

        MenuCategory menuCategory = new MenuCategory();
        menuCategory.setRestaurantId(restaurantId);
        menuCategory.setName(menuCategoryRequest.getName());
        menuCategory.setSortOrder(menuCategoryRequest.getSortOrder());
        menuCategoryRepository.save(menuCategory);
        return MenuCategoryResponse.builder()
                .name(menuCategory.getName())
                .sortOrder(menuCategory.getSortOrder())
                .build();
    }

    public List<MenuItemResponse> getAllMenuItems(String restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream().map(this::convertToMenuItemResponse).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = {Exception.class})
    public MenuItemResponse updateMenuItem(String menuItemId, MenuItemRequest menuItemRequest, MultipartFile file, String restaurantId, String userId) throws ApiException {
        try {
            // Get the restaurant name, validating the user's access to the restaurant
            String restaurantName = getRestaurantName(restaurantId, userId);
            if (restaurantName == null) {
                throw new ApiException("Restaurant does not exist");
            }

            // Fetch the existing MenuItem
            MenuItem menuItem = menuItemRepository.findByMenuItemId(menuItemId)
                    .orElseThrow(() -> new ApiException("MenuItem not found"));

            // Check if the MenuItem belongs to the given restaurant
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                throw new ApiException("MenuItem does not belong to the given restaurant");
            }

            // Check if the category exists
            MenuCategory menuCategory = menuCategoryRepository.findByRestaurantIdAndName(restaurantId, menuItemRequest.getCategory());
            if (menuCategory == null) {
                throw new ApiException("Category does not exist");
            }

            // Upload the image to S3 if a new file is provided
            String imageUrl = menuItem.getImageUrl(); // Existing image URL
            if (file != null && !file.isEmpty()) {
                imageUrl = uploadImageToS3(file);
            }

            // Update the MenuItem properties
            menuItem.setMenuItemName(menuItemRequest.getName());
            menuItem.setDescription(menuItemRequest.getDescription());
            menuItem.setCategory(menuCategory);
            menuItem.setPrice(menuItemRequest.getPrice());
            menuItem.setImageUrl(imageUrl);

            // Update tags
            Set<Tag> persistentTags = menuItemRequest.getTags().stream()
                    .map(name -> tagRepository.findByName(name).orElseGet(() -> {
                        Tag tag = new Tag();
                        tag.setName(name);
                        return tagRepository.save(tag);
                    }))
                    .collect(Collectors.toSet());

            menuItem.getTags().clear();
            menuItem.getTags().addAll(persistentTags);
            persistentTags.forEach(tag -> tag.getMenuItems().add(menuItem));

            // Save the updated MenuItem
            MenuItem updatedMenuItem = menuItemRepository.save(menuItem);

            // Publish the MenuItemUpdated event to Kafka
            publishMenuItemEvent(updatedMenuItem, restaurantName, "UPDATE");

            // Convert and return the response
            return convertToMenuItemResponse(updatedMenuItem);

        } catch (FeignException feignException) {
            throw new ApiException("Error from restaurant service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            throw new ApiException("Error from restaurant service: " + error.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error updating menu item: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public String deleteMenuItem(String menuItemId, String restaurantId, String userId) throws Exception {
        // Validate restaurantId and userId
        String restaurantName = getRestaurantName(restaurantId, userId);

        if(restaurantName == null){
            throw new ApiException("Invalid Restaurant Name");
        }

        MenuItem menuItem = menuItemRepository.findByMenuItemId(menuItemId).orElseThrow(() -> new ApiException("MenuItem not found"));

        // Disassociate from Category
        if (menuItem.getCategory() != null) {
            menuItem.getCategory().getMenuItems().remove(menuItem);
        }

        // Disassociate from Tags
        for (Tag tag : menuItem.getTags()) {
            tag.getMenuItems().remove(menuItem);
        }
        menuItem.getTags().clear();

        // Finally, delete
        menuItemRepository.delete(menuItem);

        publishMenuItemEvent(menuItem, restaurantName, "DELETE");
        return "MenuItem deleted successfully";
    }

    private String getRestaurantName(String restaurantId, String userId) {

        ResponseEntity<?> response = restaurantInterface.getRestaurantName(restaurantId, userId);
        String restaurantName = null;
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
            Map<String, Object> responseBody = (Map<String, Object>) map;
            restaurantName = (String) responseBody.get("name");
            System.out.println("Restaurant Name: " + restaurantName);
        } else if (response.getBody() instanceof Map<?, ?> map) {
            ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
            System.out.println("Error: " + error.getMessage());
        } else {
            System.out.println("Unexpected response format");
        }
        return restaurantName;
    }

    private String uploadImageToS3(MultipartFile file) throws Exception {
        String fileNameExt = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID() + "." + fileNameExt;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            if (response.sdkHttpResponse().isSuccessful()) {
//                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
                return key;
            } else {
                throw new Exception("Failed to upload file to S3");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to upload file to S3");
        }
    }

    private void publishMenuItemEvent(MenuItem menuItem, String restaurantName, String eventType) throws Exception {
        if(eventType == null){
            throw new Exception("Invalid event type");
        }
        if(eventType.equals("CREATE") && menuItem.getMenuItemId() != null){
            System.out.println("Publishing CREATE event for MenuItem: " + menuItem.getMenuItemId());
            menuItemEventPublisher.publishMenuItemCreated(
                    convertToMenuItemEvent(menuItem, restaurantName, eventType)
            );
        } else if(eventType.equals("UPDATE") && menuItem.getMenuItemId() != null){
            System.out.println("Publishing UPDATE event for MenuItem: " + menuItem.getMenuItemId());
            menuItemEventPublisher.publishMenuItemUpdated(
                    convertToMenuItemEvent(menuItem, restaurantName, eventType)
            );
        } else if(eventType.equals("DELETE") && menuItem.getMenuItemId() != null){
            System.out.println("Publishing DELETE event for MenuItem: " + menuItem.getMenuItemId());
            menuItemEventPublisher.publishMenuItemDeleted(menuItem.getMenuItemId());
        } else {
            throw new Exception("Invalid event publish request");
        }
    }

    private MenuItemEvent convertToMenuItemEvent(MenuItem menuItem, String restaurantName, String eventType) throws Exception{
        return MenuItemEvent.builder()
                .menuItemId(menuItem.getMenuItemId())
                .restaurantId(menuItem.getRestaurantId())
                .restaurantName(restaurantName)
                .menuItemName(menuItem.getMenuItemName())
                .description(menuItem.getDescription())
                .category(menuItem.getCategory().getName())
                .price(menuItem.getPrice())
                .isAvailable(menuItem.getIsAvailable())
                .imageUrl(menuItem.getImageUrl())
                .tags(menuItem.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .eventType(eventType)
                .build();
    }

    private MenuItemResponse convertToMenuItemResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .menuItemId(menuItem.getMenuItemId())
                .restaurantId(menuItem.getRestaurantId())
                .name(menuItem.getMenuItemName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory().getName())
                .imageUrl(menuItem.getImageUrl())
                .isAvailable(menuItem.getIsAvailable())
                .tags(menuItem.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .build();
    }
}