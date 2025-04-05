package ru.mai.chat.server.model;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "chatroom_user")
public class ChatroomUser {

    private Long user;

    public ChatroomUser() {

    }

    public ChatroomUser(Long user) {
        this.user = user;
    }

    public Long getUser() {
        return user;
    }

}
