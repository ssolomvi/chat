package ru.mai.chat.client.service;

import com.google.protobuf.ByteString;
import com.vaadin.flow.component.page.WebStorage;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mai.chat.client.repository.ChatroomRepository;
import ru.mai.chat.common.*;
import ru.mai.chat.common.encryption_algorithm.EncryptionAlgorithm;
import ru.mai.chat.common.encryption_context.EncryptionService;
import ru.mai.chat.common.encryption_context.SymmetricEncryptionServiceImpl;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClientService {

    private static final String ID = "id";

    @GrpcClient("chat-client")
    private ChatServiceGrpc.ChatServiceBlockingStub stub;

    private final ChatroomRepository chatroomRepository;

    @Autowired
    public ClientService(ChatroomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    public void hello(String login) {
        var hello = HelloRequest.newBuilder()
                .setLogin(login)
                .build();

        var helloResponse = stub.hello(hello);

        if (isNewUser(helloResponse)) {
            // sign up
            // todo: save p, g in local storage
            signUp(login);
        } else {
            // restore
            restoreChatrooms();
            restoreMessages();
        }

    }

    private boolean isNewUser(HelloResponse response) {
        return StringUtils.isBlank(response.getP()) || response.getId() == 0L;
    }

    private void signUp(String login) {
        // generate public key
        var signUp = SignUpRequest.newBuilder()
                .setLogin(login)
                .setPublicKey(computePublicKey())
                .build();

        var signUpResponse = stub.signUp(signUp);

        WebStorage.setItem(ID, String.valueOf(signUpResponse.getId()));
    }

    private String computePublicKey() {
        // todo;
    }

    private void disconnect() {
        var disconnect = DisconnectRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .build();

        stub.disconnect(disconnect);
    }

    private void restoreChatrooms() {
        var restoreChatroom = RestoreMessagesRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .build();

        var response = stub.restoreChatrooms(restoreChatroom);
        for (var chatroomInfo : response.getChatroomsList()) {
            // todo: create chat room encryption context for chatroom id, add it to repository
            createChatroom(chatroomInfo);
        }
    }

    private void createChatroom(ChatroomInfo info) {
        var encryptionService = createEncryptionService(info);

        chatroomRepository.addChatroom(info.getChatRoomId(), encryptionService);
    }

    private EncryptionService createEncryptionService(ChatroomInfo info) {
        var algorithm = createAlgorithm(info);

        return new SymmetricEncryptionServiceImpl(
                info.getEncryptionMode(),
                info.getPaddingMode(),
                algorithm
        );
    }

    private EncryptionAlgorithm createAlgorithm(ChatroomInfo info) {
        // todo: calculate private key
        // init algo
    }

    private void restoreMessages() {
        var restoreMsg = RestoreMessagesRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .build();

        stub.restoreMessages(restoreMsg);
        // handle messages with kafka
    }

    private void createChatroom(String companionLogin, AlgorithmEnum algorithm, EncryptionModeEnum encryptionMode, PaddingModeEnum paddingMode) {
        var createChatroom = ChatRoomCreateRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .setCompanionLogin(companionLogin)
                .setAlgorithm(algorithm)
                .setEncryptionMode(encryptionMode)
                .setPaddingMode(paddingMode)
                .build();

        var chatroomInfo = stub.chatroomCreate(createChatroom);
        var service = createEncryptionService(chatroomInfo);

        chatroomRepository.addChatroom(chatroomInfo.getChatRoomId(), service);
    }

    public void sendTextMessage(Long chatroomId, byte[] text) {
        var sendMsg = SendMessageRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .setChatRoomId(chatroomId)
                .setMessage(
                        Message.newBuilder()
                                .setText(ByteString.copyFrom(text))

                ).build();

        stub.sendMessage(sendMsg);
    }

    public void sendFileMessage(Long chatroomId, String filename, int filePartNumber, int filePartCount, byte[] text) {
        var sendMsg = SendMessageRequest.newBuilder()
                .setId(getIdFromLocalStorage())
                .setChatRoomId(chatroomId)
                .setMessage(
                        Message.newBuilder()
                                .setFilename(filename)
                                .setFilePartNumber(filePartNumber)
                                .setFilePartCount(filePartCount)
                                .setText(ByteString.copyFrom(text))
                )
                .build();

        stub.sendMessage(sendMsg);
    }

    private Long getIdFromLocalStorage() {
        // todo: use json deserializer
        AtomicReference<Long> id = new AtomicReference<>();
        WebStorage.getItem(
                ID,
                value -> id.set(Long.parseLong(value))
        );

        return id.get();
    }

    private void saveObjectInLocalRepository(String key, Object o) {
        // todo: use json serializer
        WebStorage.setItem(key, String.valueOf(o));
    }

}
