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

@Service
public class UserServiceImpl {
    @Autowired
    private UserRepository userRepository;


    @Transactional(rollbackFor = {Exception.class})
    public UserResponse createUser(UserRequest userRequest) throws RegException {
        if(userRepository.existsByUserId(userRequest.getUserId())) {
            throw new RegException("User with id: " + userRequest.getUserId() + " already exists");
        }
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RegException("Email already exists");
        }

        User user = new User();
        user.setUserId(userRequest.getUserId());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setUserRole(userRequest.getUserRole());

        User saveduser = userRepository.save(user);
        return UserResponse.builder().
                userId(saveduser.getUserId())
                .name(saveduser.getName())
                .email(saveduser.getEmail())
                .phoneNumber(saveduser.getPhoneNumber())
                .userRole(saveduser.getUserRole())
                .build();
    }

    public UserResponse getUser(String userId) throws ApiException {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new ApiException("User not found"));

        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .userRole(user.getUserRole())
                .build();
    }

    public UserResponse updateUser(String userId, UserRequest userRequest) throws RegException {
//        Optional<User> user1 = userRepository.findByUserId(user.getUserId());
        return null;
    }

    private UserResponse updateUser(User user, User userNew) {
        user.setName(userNew.getName());
        user.setEmail(userNew.getEmail());
        user.setPhoneNumber(userNew.getPhoneNumber());
        user.setAddress(userNew.getAddress());
        userRepository.save(user);
        return mapUserToUserResponse(user);
    }

    private UserResponse mapUserToUserResponse(User user1) {
        return null;
    }
}
