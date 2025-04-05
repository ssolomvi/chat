package ru.mai.chat.server.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mai.chat.server.model.Chatroom;

import java.util.Set;

@Repository
public interface ChatroomRepository extends CrudRepository<Chatroom, Long> {

    @Query("select * from chatroom c join chatroom_user cu on c.id = cu.chatroom where cu.user = :id")
    Set<Chatroom> findByChatroomUserId(@Param("id") Long id);

}
