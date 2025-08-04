package com.hackovation.emailservice.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.emailservice.dto.EmailVerificationData;
import com.hackovation.emailservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "user-verification", groupId = "${spring.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void emailVerificationListener(String message) throws Exception {
        try {
            EmailVerificationData emailData = new ObjectMapper().readValue(message, EmailVerificationData.class);
            System.out.println("Received message from user-verification: " + emailData);
            emailService.sendOtpEmail(emailData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "login-attempt", groupId = "${spring.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void passwordResetListener(String message) throws Exception {
        try {
            EmailVerificationData emailData = new ObjectMapper().readValue(message, EmailVerificationData.class);
            System.out.println("Received message from password-reset: " + emailData);
            // TODO: send email
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
