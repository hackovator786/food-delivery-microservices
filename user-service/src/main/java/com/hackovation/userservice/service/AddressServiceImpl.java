package com.hackovation.userservice.service;

import com.hackovation.userservice.dto.request.AddressRequest;
import com.hackovation.userservice.dto.response.AddressResponse;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.model.Address;
import com.hackovation.userservice.model.User;
import com.hackovation.userservice.repository.AddressRepository;
import com.hackovation.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public AddressResponse addAddress(String userId, AddressRequest addressRequest) throws ApiException {
        try {
            Optional<User> optionalUser = userRepository.findByUserId(userId);
            if (optionalUser.isEmpty()) {
                throw new ApiException("User not found");
            }
            User user = optionalUser.get();
            user.addAddress(mapAddressRequestToAddress(addressRequest));
            User savedUser = userRepository.save(user);
            Address savedAddress = savedUser.getAddressByName(addressRequest.getName());
            return mapAddressToAddressResponse(savedAddress);
        } catch (ApiException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<AddressResponse> getAllAddresses(String userId) throws ApiException{
        try {
            List<Address> addresses = addressRepository.findByUserId(getIdByUserId(userId));
            return addresses.stream().map(this::mapAddressToAddressResponse).toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public AddressResponse getAddressByUserIdAndAddressId(String userId, Long addressId) throws ApiException {
        try {
            Address address = getAddress(userId, addressId);
            return mapAddressToAddressResponse(address);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public AddressResponse updateAddressById(String userId, Long addressId, AddressRequest addressRequest) throws ApiException {
        try {
            Address fetchedAddress = getAddress(userId, addressId);
            fetchedAddress.setName(addressRequest.getName());
            fetchedAddress.setDoorNumber(addressRequest.getDoorNumber());
            fetchedAddress.setStreet(addressRequest.getStreet());
            fetchedAddress.setArea(addressRequest.getArea());
            fetchedAddress.setCity(addressRequest.getCity());
            fetchedAddress.setState(addressRequest.getState());
            fetchedAddress.setCountry(addressRequest.getCountry());
            fetchedAddress.setZipcode(addressRequest.getZipcode());
            Address updatedAddress = addressRepository.save(fetchedAddress);
            return mapAddressToAddressResponse(updatedAddress);
        } catch (ApiException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public AddressResponse updateAddressByName(String userId, String name, AddressRequest addressRequest) throws ApiException {
        try {
            Address fetchedAddress = getAddressByName(userId, name);
            fetchedAddress.setName(addressRequest.getName());
            fetchedAddress.setDoorNumber(addressRequest.getDoorNumber());
            fetchedAddress.setStreet(addressRequest.getStreet());
            fetchedAddress.setArea(addressRequest.getArea());
            fetchedAddress.setCity(addressRequest.getCity());
            fetchedAddress.setState(addressRequest.getState());
//            fetchedAddress.setCountry(addressRequest.getCountry());
            fetchedAddress.setZipcode(addressRequest.getZipcode());
            Address updatedAddress = addressRepository.save(fetchedAddress);
            return mapAddressToAddressResponse(updatedAddress);
        } catch (ApiException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteAddress(String userId, Long addressId) throws Exception{
        try {
            int isDeleted = addressRepository.deleteByUserIdAndId(getIdByUserId(userId), addressId);
            if(isDeleted == 0){
                throw new ApiException("Error deleting address");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private Address getAddress(String userId, Long addressId) throws ApiException {
        return addressRepository.findByUserIdAndId(getIdByUserId(userId), addressId).orElseThrow(() -> new ApiException("Address not found"));
    }

    private Address getAddressByName(String userId, String name) throws ApiException{
        return addressRepository.findByUserIdAndName(getIdByUserId(userId), name).orElseThrow(() -> new ApiException("Address not found"));
    }

    private Long getIdByUserId(String userId) throws ApiException {
        Long id = userRepository.getIdByUserId(userId);
        if(id == null) {
            throw new ApiException("User not found");
        }
        return id;
    }

    private Address mapAddressRequestToAddress(AddressRequest addressRequest) {
        return Address.builder()
                .name(addressRequest.getName())
                .doorNumber(addressRequest.getDoorNumber())
                .street(addressRequest.getStreet())
                .area(addressRequest.getArea())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .country("India")
                .zipcode(addressRequest.getZipcode())
                .build();
    }

    private AddressResponse mapAddressToAddressResponse(Address address){
        return AddressResponse.builder()
                .id(address.getId())
                .name(address.getName())
                .doorNumber(address.getDoorNumber())
                .street(address.getStreet())
                .area(address.getArea())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipcode(address.getZipcode())
                .build();
    }
}
