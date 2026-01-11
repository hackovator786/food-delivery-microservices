package com.hackovation.userservice.service;

import com.hackovation.userservice.dto.request.AddressRequest;
import com.hackovation.userservice.dto.response.AddressResponse;
import com.hackovation.userservice.exception.ApiException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressService {
    public AddressResponse addAddress(String userId, AddressRequest addressRequest) throws ApiException;
    public List<AddressResponse> getAllAddresses(String userId) throws ApiException;
    public AddressResponse getAddressByUserIdAndAddressId(String userId, Long addressId) throws ApiException;
    public AddressResponse updateAddressById(String userId, Long addressId, AddressRequest addressRequest) throws ApiException;
    AddressResponse updateAddressByName(String userId, String name, AddressRequest addressRequest) throws ApiException;
    public void deleteAddress(String userId, Long addressId) throws Exception;
}
