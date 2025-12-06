package com.hackovation.orderservice.controller;

import com.hackovation.orderservice.dto.OrderResponse;
import com.hackovation.orderservice.model.Order;
import com.hackovation.orderservice.model.OrderStatus;
import com.hackovation.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> placeOrder(@RequestHeader("loggedInUser") String userId) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userId));
    }

    @PutMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrderStatus(@RequestParam String orderId,  @RequestBody OrderStatus orderStatus) {
        orderService.updateOrderStatus(orderId, orderStatus);
    }
}
