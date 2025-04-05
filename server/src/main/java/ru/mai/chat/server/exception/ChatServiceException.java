package ru.mai.chat.server.exception;

import ru.mai.chat.common.ChatServiceErrorCode;

public class ChatServiceException extends RuntimeException {

    private final ChatServiceErrorCode errorCode;

    public ChatServiceException(ChatServiceErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public ChatServiceErrorCode getErrorCode() {
        return errorCode;
    }

}
