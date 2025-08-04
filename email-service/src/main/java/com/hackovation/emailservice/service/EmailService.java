package com.hackovation.emailservice.service;

import com.hackovation.emailservice.dto.EmailVerificationData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    @Retryable(
            retryFor = { MailException.class, MessagingException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public void sendOtpEmail(EmailVerificationData emailVerificationData) throws MessagingException {
        try {
//            simulateRandomFailure();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailVerificationData.getTo());
            helper.setSubject("User Verification OTP");
            helper.setText("Your OTP is " + emailVerificationData.getOtp() + ". Use this otp to verify.", true);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw ex;
        }
    }

    @Recover
    public void handleMessagingException(MessagingException e, EmailVerificationData emailData) {
        System.out.println("Max attempts reached. Failed to send email to " + emailData.getTo() + " after 3 attempts.");
        System.out.println("Error message: " + e.getMessage());
    }

    // Just for testing the failure scenario
    public void simulateRandomFailure() throws MessagingException {
        // generate a random number between 1 and 6
        int random = (int) (Math.random() * 6 + 1) % 5;
        System.out.println("Random number: " + random);
        if (random < 5) { // 4 out of 6 chances to fail
            throw new MessagingException("Failed to send email");
        }
    }
}
