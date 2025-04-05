package ru.mai.chat.server.service;

import com.google.common.base.Preconditions;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.CollectionUtils;
import ru.mai.chat.common.*;
import ru.mai.chat.server.exception.ChatServiceException;
import ru.mai.chat.server.kafka.MessagePublisher;
import ru.mai.chat.server.model.Chatroom;
import ru.mai.chat.server.model.ChatroomUser;
import ru.mai.chat.server.model.User;
import ru.mai.chat.server.repository.ActiveUserRepository;
import ru.mai.chat.server.repository.ChatroomRepository;
import ru.mai.chat.server.repository.MessageRepository;
import ru.mai.chat.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@GrpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {
    private static final String DIFFIE_HELLMAN_G = "17";
    private static final String DIFFIE_HELLMAN_P = "17";
    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;
    private final MessageRepository messageRepository;

    private final EmptyMessage emptyMessage = EmptyMessage.getDefaultInstance();

    private final ActiveUserRepository activeUserRepository;
    private final MessagePublisher messagePublisher;

    @Autowired
    public ChatService(UserRepository userRepository,
                       ChatroomRepository chatroomRepository,
                       MessageRepository messageRepository,
                       ActiveUserRepository activeUserRepository,
                       MessagePublisher messagePublisher) {
        this.userRepository = userRepository;
        this.chatroomRepository = chatroomRepository;
        this.messageRepository = messageRepository;
        this.activeUserRepository = activeUserRepository;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        if (StringUtils.isBlank(request.getLogin())) {
            throw new ChatServiceException(ChatServiceErrorCode.ILLEGAL_ARGUMENT_CODE_VALUE);
        }

        var user = userRepository.findByLogin(request.getLogin());

        var responseBuilder = HelloResponse.newBuilder();
        if (user == null) {
            responseBuilder.setP(DIFFIE_HELLMAN_P)
                    .setG(DIFFIE_HELLMAN_G);
        } else {
            if (user.getId() == null) {
                throw new ChatServiceException(ChatServiceErrorCode.ILLEGAL_ARGUMENT_CODE_VALUE);
            }

            responseBuilder.setId(user.getId());

            activeUserRepository.addActiveUser(user.getId());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void signUp(SignUpRequest request, StreamObserver<EmptyMessage> responseObserver) {
        if (userRepository.findByLogin(request.getLogin()) != null) {
            throw new ChatServiceException(ChatServiceErrorCode.ILLEGAL_ARGUMENT_CODE_VALUE);
        }

        var user = new User();
        user.setLogin(request.getLogin());
        user.setDiffieHellmanNumber(request.getPublicKey());

        try {
            var persistedUser = userRepository.save(user);
            activeUserRepository.addActiveUser(persistedUser.getId());
        } catch (Exception e) {
            throw new ChatServiceException(ChatServiceErrorCode.UNRECOGNIZED);
        }

        responseObserver.onNext(emptyMessage);
        responseObserver.onCompleted();
    }

    @Override
    public void disconnect(DisconnectRequest request, StreamObserver<EmptyMessage> responseObserver) {
        activeUserRepository.removeActiveUser(request.getId());

        responseObserver.onNext(emptyMessage);
        responseObserver.onCompleted();
    }

    @Override
    public void chatroomCreate(ChatRoomCreateRequest request, StreamObserver<ChatroomInfo> responseObserver) {
        var userOpt = userRepository.findById(request.getId());
        if (userOpt.isEmpty()) {
            throw new ChatServiceException(ChatServiceErrorCode.NOT_FOUND_CODE_VALUE);
        }

        if (StringUtils.equals(request.getCompanionLogin(), userOpt.get().getLogin())) {
            throw new ChatServiceException(ChatServiceErrorCode.ILLEGAL_ARGUMENT_CODE_VALUE);
        }

        var companion = userRepository.findByLogin(request.getCompanionLogin());

        if (companion == null || companion.getId() == null) {
            throw new ChatServiceException(ChatServiceErrorCode.NOT_FOUND_CODE_VALUE);
        }

        var chatroom = new Chatroom();
        chatroom.setChatRoomUsers(Set.of(
                new ChatroomUser(request.getId()),
                new ChatroomUser(companion.getId())
        ));
        chatroom.setAlgorithmEnum(request.getAlgorithm());
        chatroom.setEncryptionMode(request.getEncryptionMode());
        chatroom.setPaddingMode(request.getPaddingMode());

        try {
            chatroom = chatroomRepository.save(chatroom);

            Preconditions.checkNotNull(chatroom.getId());
        } catch (Exception e) {
            throw new ChatServiceException(ChatServiceErrorCode.SQL_EXCEPTION);
        }

        var info = ChatroomInfo.newBuilder()
                .setChatRoomId(chatroom.getId())
                .setAlgorithm(request.getAlgorithm())
                .setEncryptionMode(request.getEncryptionMode())
                .setPaddingMode(request.getPaddingMode())
                .setCompanionPk(companion.getDiffieHellmanNumber())
                .build();


        responseObserver.onNext(info);
        responseObserver.onCompleted();
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<EmptyMessage> responseObserver) {
        var sender = findById(userRepository, request.getId());

        var chatroom = findById(chatroomRepository, request.getChatRoomId());

        var companionId = findCompanion(chatroom, sender.getId());

        var msg = new ru.mai.chat.server.model.Message();
        msg.setChatroom(chatroom.getId());
        msg.setCreatedDate(LocalDateTime.now());
        msg.setSender(sender.getId());
        msg.setFilename(request.getMessage().getFilename());
        msg.setFilePartNumber(request.getMessage().getFilePartNumber());
        msg.setFilePartCount(request.getMessage().getFilePartCount());
        msg.setText(request.getMessage().getText().toByteArray());

        messageRepository.save(msg);

        if (activeUserRepository.isActive(companionId)) {
            messagePublisher.publish(activeUserRepository.getTopic(companionId), msg);
        }

        responseObserver.onNext(emptyMessage);
        responseObserver.onCompleted();
    }

    @Override
    public void restoreChatrooms(RestoreMessagesRequest request, StreamObserver<RestoreChatroomsResponse> responseObserver) {
        var user = findById(userRepository, request.getId());

        var chatrooms = chatroomRepository.findByChatroomUserId(user.getId());
        if (CollectionUtils.isEmpty(chatrooms)) {
            responseObserver.onNext(RestoreChatroomsResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }

        var builder = RestoreChatroomsResponse.newBuilder();
        for (var chatroom : chatrooms) {
            Preconditions.checkNotNull(chatroom.getId());

            var companionId = findCompanion(chatroom, user.getId());
            var companion = findById(userRepository, companionId);

            var info = ChatroomInfo.newBuilder()
                    .setChatRoomId(chatroom.getId())
                    .setAlgorithm(chatroom.getAlgorithmEnum())
                    .setEncryptionMode(chatroom.getEncryptionMode())
                    .setPaddingMode(chatroom.getPaddingMode())
                    .setCompanionPk(companion.getDiffieHellmanNumber())
                    .build();

            builder.addChatrooms(info);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void restoreMessages(RestoreMessagesRequest request, StreamObserver<EmptyMessage> responseObserver) {
        var user = findById(userRepository, request.getId());
        var chatrooms = chatroomRepository.findByChatroomUserId(user.getId());

        var topic = activeUserRepository.getTopic(user.getId());
        for (var chatroom : chatrooms) {
            var messages = messageRepository.findByChatroom(chatroom.getId());

            for (var message : messages) {
                messagePublisher.publish(topic, message);
            }
        }

        responseObserver.onNext(emptyMessage);
        responseObserver.onCompleted();
    }

    private <T> T findById(CrudRepository<T, Long> repository, Long id) {
        var op = repository.findById(id);
        if (op.isEmpty()) {
            throw new ChatServiceException(ChatServiceErrorCode.NOT_FOUND_CODE_VALUE);
        }

        return op.get();
    }

    private Long findCompanion(Chatroom chatroom, Long id) {
        var companionIdOpt = chatroom.getChatRoomUsers()
                .stream()
                .filter(ch -> !Objects.equals(ch.getUser(), id))
                .findAny();

        if (companionIdOpt.isEmpty()) {
            throw new ChatServiceException(ChatServiceErrorCode.NOT_FOUND_CODE_VALUE);
        }

        return companionIdOpt.get().getUser();
    }

}
