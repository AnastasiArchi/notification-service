package org.example.notification.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendUserEvent(String email, String operation) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Уведомление: " + operation);
        message.setText(
                "В системе произошло событие: " + operation
                        + "\nEmail пользователя: " + email
        );

        mailSender.send(message);
    }
}