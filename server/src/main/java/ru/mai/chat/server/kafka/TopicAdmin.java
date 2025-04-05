package ru.mai.chat.server.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mai.chat.common.ChatServiceErrorCode;
import ru.mai.chat.server.exception.ChatServiceException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class TopicAdmin {

    private final Admin admin;

    @Autowired
    public TopicAdmin(Admin admin) {
        this.admin = admin;
    }

    public void createTopic(String topic) {
        Map<String, String> topicConfig = new HashMap<>();
        topicConfig.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(24 * 60 * 60 * 1000)); // 24 hours retention

        NewTopic newTopic = new NewTopic(topic, 1, (short) 1).configs(topicConfig);

        //Blocking call to make sure topic is created
        try {
            admin.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new ChatServiceException(ChatServiceErrorCode.KAFKA_EXCEPTION);
        }
    }

    public void deleteTopic(String topic) {
        try {
            admin.deleteTopics(Collections.singletonList(topic));
        } catch (Exception e) {
            throw new ChatServiceException(ChatServiceErrorCode.KAFKA_EXCEPTION);
        }
    }

    public void deleteTopics(List<String> topics) {
        admin.deleteTopics(topics);
    }

}
