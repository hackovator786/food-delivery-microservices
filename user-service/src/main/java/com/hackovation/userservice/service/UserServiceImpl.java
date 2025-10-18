package com.hackovation.userservice.service;

import com.hackovation.userservice.dto.request.UserRequest;
import com.hackovation.userservice.dto.response.UserResponse;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.exception.RegException;
import com.hackovation.userservice.model.User;
import com.hackovation.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UserServiceImpl {
    @Autowired
    private UserRepository userRepository;

    @Transactional(rollbackFor = {Exception.class})
    public UserResponse createUser(UserRequest userRequest) throws RegException, ApiException {
        if(userRepository.existsByUserId(userRequest.getUserId())) {
            throw new RegException("User already exists");
        }
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RegException("Email already exists");
        }

        User user = new User();
        user.setUserId(userRequest.getUserId());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());

        User saveduser = userRepository.save(user);
        return mapUserToUserResponse(saveduser);
    }

    public UserResponse getUser(String userId) throws ApiException {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new ApiException("User not found"));
        return mapUserToUserResponse(user);
    }

    public UserResponse updateUser(UserRequest userRequest) throws ApiException {
        User user = userRepository.findByUserId(userRequest.getUserId()).orElseThrow(() -> new ApiException("User not found"));

        if(!user.getEmail().equals(userRequest.getEmail())) {
            throw new ApiException("Incorrect email");
        }

        user.setName(userRequest.getName());
        user.setPhoneNumber(userRequest.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        return mapUserToUserResponse(updatedUser);
    }

    private UserResponse mapUserToUserResponse(User user) {
        if(user != null) {
            return UserResponse.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
        }
        return null;
    }
}
