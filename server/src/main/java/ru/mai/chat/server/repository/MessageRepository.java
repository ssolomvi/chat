package ru.mai.chat.server.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mai.chat.server.model.Message;

import java.util.Set;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

    @Query("select * from message m join user u on m.sender = u.id where u.id = :sender")
    Set<Message> findBySender(@Param("sender") Long sender);

    @Query("select * from message m join chatroom c on m.chatroom = c.id where c.id = :chatroom")
    Set<Message> findByChatroom(@Param("chatroom") Long chatroom);

}
