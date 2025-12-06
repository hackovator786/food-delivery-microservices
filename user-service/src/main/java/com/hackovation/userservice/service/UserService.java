package com.hackovation.userservice.service;

import com.hackovation.userservice.dto.request.UserRequest;
import com.hackovation.userservice.dto.response.UserResponse;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.exception.RegException;

public interface UserService {
    public UserResponse createUser(UserRequest userRequest) throws RegException, ApiException;
    public UserResponse getUser(String userId) throws ApiException;
    public UserResponse updateUser(UserRequest userRequest) throws ApiException;
}
