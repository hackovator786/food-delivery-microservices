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
    public MenuItemResponse addMenuItem(String userId, String restaurantId, MenuItemRequest menuItemRequest, MultipartFile file) throws  Exception{
        try {
            // Validate MenuItemRequest

            ResponseEntity<?> response = restaurantInterface.getRestaurantName(restaurantId, userId);

            System.out.println("Response from user service: " + response);
            System.out.println("Response body from user service: " + response.getBody());
            System.out.println("Response status code from user service: " + response.getStatusCode());
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
                        Tag t = new Tag();
                        t.setName(name);
                        return t;
                    }))
                    .collect(Collectors.toSet());

            menuItem.getTags().addAll(persistentTags);
            persistentTags.forEach(t -> t.getMenuItems().add(menuItem));

            // Save MenuItem
            MenuItem savedMenuItem = menuItemRepository.save(menuItem);

            // Publish MenuItemCreated event to Kafka
            menuItemEventPublisher.publishMenuItemCreated(
                    MenuItemEvent.builder()
                            .menuItemId(savedMenuItem.getMenuItemId())
                            .restaurantId(savedMenuItem.getRestaurantId())
                            .restaurantName(restaurantName)
                            .menuItemName(savedMenuItem.getMenuItemName())
                            .description(savedMenuItem.getDescription())
                            .category(savedMenuItem.getCategory().getName())
                            .price(savedMenuItem.getPrice())
                            .isAvailable(savedMenuItem.getIsAvailable())
                            .imageUrl(savedMenuItem.getImageUrl())
                            .tags(savedMenuItem.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                            .eventType("CREATE")
                            .build()
                    );

            return MenuItemResponse.builder()
                    .restaurantId(savedMenuItem.getRestaurantId())
                    .name(savedMenuItem.getMenuItemName())
                    .description(savedMenuItem.getDescription())
                    .price(savedMenuItem.getPrice())
                    .category(savedMenuItem.getCategory().getName())
                    .imageUrl(savedMenuItem.getImageUrl())
                    .isAvailable(savedMenuItem.getIsAvailable())
                    .tags(savedMenuItem.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                    .build();
        }catch (FeignException feignException) {
            System.out.println("Error from restaurant service: " + feignException.getMessage());
            throw new ApiException("Error from restaurant service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from restaurant service: " + error.getMessage());
            throw new ApiException("Error from restaurant service: " + error.getMessage());
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

    public List<MenuItemResponse> getAllFoodItems(String restaurantId) {
        return null;
    }

    public String updateFoodItem(MenuItemResponse foodItemDto, String username) {

        return "Food Item not present";
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
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                throw new Exception("Failed to upload file to S3");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to upload file to S3");
        }
    }

    private void update(MenuItem item, MenuItemResponse foodItemDto) {

    }


}
