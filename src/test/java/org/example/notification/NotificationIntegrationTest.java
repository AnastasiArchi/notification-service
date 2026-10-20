package org.example.notification;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = "user-events",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class NotificationIntegrationTest {

    @RegisterExtension
    static final GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void cleanMailbox() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void shouldSendCreatedEmailThroughApi() throws Exception {
        String email = "create@example.com";

        String request = """
                {
                    "email": "create@example.com",
                    "operation": "CREATE"
                }
                """;

        mockMvc.perform(
                post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());

        MimeMessage[] messages = waitForMessages(1);

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString())
                .isEqualTo(email);
        assertThat(messages[0].getContent().toString())
                .contains("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
    }

    @Test
    void shouldSendDeletedEmailThroughApi() throws Exception {
        String email = "delete@example.com";

        String request = """
                {
                    "email": "delete@example.com",
                    "operation": "DELETE"
                }
                """;

        mockMvc.perform(
                post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());

        MimeMessage[] messages = waitForMessages(1);

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString())
                .isEqualTo(email);
        assertThat(messages[0].getContent().toString())
                .contains("Здравствуйте! Ваш аккаунт был удалён.");
    }

    @Test
    void shouldSendCreatedEmailWhenKafkaEventReceived()
            throws Exception {

        String email = "kafka-create@example.com";

        String event = """
                {
                    "operation": "CREATE",
                    "email": "kafka-create@example.com"
                }
                """;

        kafkaTemplate.send("user-events", email, event)
                .get(10, TimeUnit.SECONDS);

        MimeMessage[] messages = waitForMessages(1);

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString())
                .isEqualTo(email);
        assertThat(messages[0].getContent().toString())
                .contains(
                        "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан."
                );
    }

    @Test
    void shouldSendDeletedEmailWhenKafkaEventReceived()
            throws Exception {

        String email = "kafka-delete@example.com";

        String event = """
                {
                    "operation": "DELETE",
                    "email": "kafka-delete@example.com"
                }
                """;

        kafkaTemplate.send("user-events", email, event)
                .get(10, TimeUnit.SECONDS);

        MimeMessage[] messages = waitForMessages(1);

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString())
                .isEqualTo(email);
        assertThat(messages[0].getContent().toString())
                .contains("Здравствуйте! Ваш аккаунт был удалён.");
    }

    private MimeMessage[] waitForMessages(int expected)
            throws Exception {

        long deadline = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(10);

        while (System.nanoTime() < deadline) {
            MimeMessage[] messages = greenMail.getReceivedMessages();

            if (messages.length >= expected) {
                return messages;
            }

            Thread.sleep(100);
        }

        return greenMail.getReceivedMessages();
    }
}