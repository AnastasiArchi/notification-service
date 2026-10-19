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
        String subject;
        String text;

        switch (operation) {
            case "CREATE" -> {
                subject = "Аккаунт успешно создан";
                text = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
            }
            case "DELETE" -> {
                subject = "Аккаунт удалён";
                text = "Здравствуйте! Ваш аккаунт был удалён.";
            }
            case "UPDATE" -> {
                // По текущему ТЗ для обновления email отправлять не нужно.
                return;
            }
            default -> throw new IllegalArgumentException(
                    "Неизвестная операция: " + operation
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}