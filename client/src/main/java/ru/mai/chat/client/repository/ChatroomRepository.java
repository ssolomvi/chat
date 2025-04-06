package ru.mai.chat.client.repository;

import org.springframework.stereotype.Repository;
import ru.mai.chat.common.encryption_context.EncryptionService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ChatroomRepository {

    private final Map<Long, EncryptionService> chatrooms = new ConcurrentHashMap<>();

    public void addChatroom(Long roomId, EncryptionService service) {
        chatrooms.put(roomId, service);
    }

    public EncryptionService getEncryptionService(Long roomId) {
        return chatrooms.get(roomId);
    }

}
