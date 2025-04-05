package ru.mai.chat.server.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "message")
public class Message {
    @Id
    private Long id;

    private Long chatroom;
    private Long sender;
    private String filename;
    private byte[] text;
    private Integer filePartNumber;
    private Integer filePartCount;

    @CreatedDate
    @ReadOnlyProperty
    private LocalDateTime createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatroom() {
        return chatroom;
    }

    public void setChatroom(Long chatroom) {
        this.chatroom = chatroom;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getText() {
        return text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

    public Integer getFilePartNumber() {
        return filePartNumber;
    }

    public void setFilePartNumber(Integer filePartNumber) {
        this.filePartNumber = filePartNumber;
    }

    public Integer getFilePartCount() {
        return filePartCount;
    }

    public void setFilePartCount(Integer filePartCount) {
        this.filePartCount = filePartCount;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

}
