package ru.mai.chat.server.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mai.chat.server.model.Message;

@Component
public class MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

    private final KafkaTemplate<String, Message> publisher;

    @Autowired
    public MessagePublisher(KafkaTemplate<String, Message> publisher) {
        this.publisher = publisher;
    }

    public void publish(String topic, Message message) {
        log.info("Publishing message {}", message);
        publisher.send(topic, message);
    }

}
