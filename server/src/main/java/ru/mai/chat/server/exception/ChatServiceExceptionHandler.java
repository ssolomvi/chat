package ru.mai.chat.server.exception;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import ru.mai.chat.common.ChatServiceExceptionResponse;

import java.time.Instant;

@GrpcAdvice
public class ChatServiceExceptionHandler {

    @GrpcExceptionHandler(ChatServiceException.class)
    public StatusRuntimeException handleValidationError(ChatServiceException cause) {

        var time = Instant.now();
        var timestamp = Timestamp.newBuilder()
                .setSeconds(time.getEpochSecond())
                .setNanos(time.getNano())
                .build();

        var exceptionResponse = ChatServiceExceptionResponse.newBuilder()
                .setErrorCode(cause.getErrorCode())
                .setTimestamp(timestamp)
                .build();

        var status = Status.newBuilder();

        switch (cause.getErrorCode()) {
            case ILLEGAL_ARGUMENT_CODE_VALUE -> status.setCode(Code.INVALID_ARGUMENT.getNumber())
                    .setMessage("Illegal argument error happened");
            case NOT_FOUND_CODE_VALUE -> status.setCode(Code.NOT_FOUND.getNumber())
                    .setMessage("Not found error happened");
            case SQL_EXCEPTION -> status.setCode(Code.INTERNAL.getNumber())
                    .setMessage("Sql exception happened");
            case KAFKA_EXCEPTION -> status.setCode(Code.INTERNAL.getNumber())
                    .setMessage("Kafka exception happened");
            case UNRECOGNIZED -> status.setCode(Code.UNKNOWN.getNumber())
                    .setMessage("Unknown error happened");
        }

        status.addDetails(Any.pack(exceptionResponse))
                .build();

        return StatusProto.toStatusRuntimeException(status.build());
    }

}
