package ru.mai.chat.client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.mai.chat.common.Message;

@Service
public class ConsumerService {

    // todo: set topic
    @KafkaListener(topics = "", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Message msg) {
        // todo:
        //  process text / file messages
    }

}
