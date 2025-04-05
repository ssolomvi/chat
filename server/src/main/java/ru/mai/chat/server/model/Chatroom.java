package ru.mai.chat.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import ru.mai.chat.common.AlgorithmEnum;
import ru.mai.chat.common.EncryptionModeEnum;
import ru.mai.chat.common.PaddingModeEnum;

import java.util.*;
import java.util.stream.Collectors;

@Table(name = "chatroom")
public class Chatroom extends AbstractAggregateRoot<Chatroom> implements Persistable<Long> {

    @Id
    private Long id;
    private EncryptionModeEnum encryptionMode = EncryptionModeEnum.RANDOM_DELTA;
    private PaddingModeEnum paddingMode = PaddingModeEnum.PKCS7;
    private AlgorithmEnum algorithmEnum = AlgorithmEnum.RIJNDAEL;
    private Set<ChatroomUser> chatroomUsers = new HashSet<>();

    public Chatroom() {

    }

    @SuppressWarnings("deprecation")
    @PersistenceConstructor
    public Chatroom(Long id, EncryptionModeEnum encryptionMode, PaddingModeEnum paddingMode, AlgorithmEnum algorithmEnum, Set<ChatroomUser> chatroomUsers) {
        this.id = id;
        this.encryptionMode = encryptionMode;
        this.paddingMode = paddingMode;
        this.algorithmEnum = algorithmEnum;
        this.chatroomUsers = Optional.ofNullable(chatroomUsers).orElse(new HashSet<>());
    }

    public Chatroom withUsers(Collection<User> users) {
        chatroomUsers.addAll(
                users.stream()
                        .map(User::getId)
                        .map(ChatroomUser::new)
                        .toList()
        );

        return this;
    }

    public Chatroom withUsers(User... users) {
        chatroomUsers.addAll(Arrays.stream(users)
                .map(User::getId)
                .map(ChatroomUser::new)
                .toList()
        );

        return this;
    }

    public Set<Long> getChatRoomUsersIds() {
        return chatroomUsers.stream()
                .map(ChatroomUser::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    @Transient
    public boolean isNew() {
        return id == null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EncryptionModeEnum getEncryptionMode() {
        return encryptionMode;
    }

    public void setEncryptionMode(EncryptionModeEnum encryptionMode) {
        this.encryptionMode = encryptionMode;
    }

    public PaddingModeEnum getPaddingMode() {
        return paddingMode;
    }

    public void setPaddingMode(PaddingModeEnum paddingMode) {
        this.paddingMode = paddingMode;
    }

    public AlgorithmEnum getAlgorithmEnum() {
        return algorithmEnum;
    }

    public void setAlgorithmEnum(AlgorithmEnum algorithmEnum) {
        this.algorithmEnum = algorithmEnum;
    }

    public Set<ChatroomUser> getChatRoomUsers() {
        return chatroomUsers;
    }

    public void setChatRoomUsers(Set<ChatroomUser> chatroomUsers) {
        this.chatroomUsers = chatroomUsers;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof Chatroom other) {
//            return Objects.equals(other.id, this.id);
//        }
//
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        return id.hashCode();
//    }

    @Override
    public String toString() {
        return super.toString();
    }

}
