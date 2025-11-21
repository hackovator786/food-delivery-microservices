package com.hackovation.cartservice.config;

import com.hackovation.cartservice.model.Cart;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class MongoAuditingListener extends AbstractMongoEventListener<Cart> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Cart> event) {
        Cart cart = event.getSource();

        if (cart.getCreatedAt() == null) {
            cart.beforeConvert();
        }
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Cart> event) {
        Cart cart = event.getSource();
        cart.beforeSave();
    }
}