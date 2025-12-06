package com.hackovation.userservice.controller;

import com.hackovation.userservice.dto.request.AddressRequest;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/add")
    public ResponseEntity<?> addAddress(@RequestHeader("loggedInUser") String userId, @Valid @RequestBody AddressRequest addressRequest) throws ApiException {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(userId,addressRequest));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAddresses(@RequestHeader("loggedInUser") String userId) throws ApiException {
        return ResponseEntity.ok().body(addressService.getAllAddresses(userId));
    }

    @GetMapping("/get/{addressId}")
    public ResponseEntity<?> getAddressById(@RequestHeader("loggedInUser") String userId, @PathVariable Long addressId) throws ApiException {
        return ResponseEntity.ok().body(addressService.getAddressByUserIdAndAddressId(userId,addressId));
    }

    @PutMapping("/update/{addressId}")
    public ResponseEntity<?> updateAddress(@RequestHeader("loggedInUser") String userId, @PathVariable Long addressId, @Valid @RequestBody AddressRequest addressRequest) throws ApiException {
        return ResponseEntity.ok().body(addressService.updateAddress(userId, addressId, addressRequest));
    }

    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(@RequestHeader("loggedInUser") String userId, @PathVariable Long addressId) throws Exception {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.status(HttpStatus.valueOf(204)).build();
    }
}
