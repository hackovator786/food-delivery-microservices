package com.hackovation.cartservice.service;

import com.hackovation.cartservice.dto.CartResponseInternal;
import com.hackovation.cartservice.exception.ApiException;

public interface CartServiceInternal {
    CartResponseInternal getCartItems(String userId) throws ApiException;
}
