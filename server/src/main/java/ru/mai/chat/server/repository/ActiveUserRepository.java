package ru.mai.chat.server.repository;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.mai.chat.server.kafka.TopicAdmin;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ActiveUserRepository {

    private final Map<Long, String> activeUsersAndTopic = new ConcurrentHashMap<>();
    private final TopicAdmin topicAdmin;

    @Autowired
    public ActiveUserRepository(TopicAdmin topicAdmin) {
        this.topicAdmin = topicAdmin;
    }

    public boolean isActive(final Long id) {
        return activeUsersAndTopic.containsKey(id);
    }

    @Nullable
    public String getTopic(final Long id) {
        return activeUsersAndTopic.get(id);
    }

    public void addActiveUser(final Long id) {
        var topic = String.format("user_topic_%s", id);
        activeUsersAndTopic.put(id, topic);

        topicAdmin.createTopic(topic);
    }

    public void removeActiveUser(final Long id) {
        var topic = activeUsersAndTopic.remove(id);

        topicAdmin.deleteTopic(topic);
    }

    @PreDestroy
    public void deleteExistingTopics() {
        topicAdmin.deleteTopics(activeUsersAndTopic.values()
                .stream()
                .toList());
    }

}
