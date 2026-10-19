package org.example.notification.kafka;

import org.example.notification.dto.UserEvent;
import org.example.notification.service.EmailNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final EmailNotificationService emailNotificationService;

    public UserEventListener(
            EmailNotificationService emailNotificationService
    ) {
        this.emailNotificationService = emailNotificationService;
    }

    @KafkaListener(topics = "user-events")
    public void handleUserEvent(UserEvent event) {
        System.out.println("Получено событие из Kafka");
        System.out.println("Операция: " + event.getOperation());
        System.out.println("Email: " + event.getEmail());

        emailNotificationService.sendUserEvent(
                event.getEmail(),
                event.getOperation()
        );
    }
}