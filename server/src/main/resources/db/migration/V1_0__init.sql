DO
$$
    BEGIN

        -- table chatroom
        CREATE TYPE algorithm_enum AS ENUM (
            'RIJNDAEL',
            'MARS',
            'RC6',
            'LOKI97',
            'DES',
            'DEAL'
        );

        CREATE TYPE encryption_mode AS ENUM (
            'ECB',
            'CBC',
            'PCBC',
            'OFB',
            'CFB',
            'COUNTER',
            'RANDOM_DELTA'
        );

        CREATE TYPE padding_mode AS ENUM (
            'ZEROES',
            'ANSI_X_923',
            'PKCS7',
            'ISO10126'
        );

        CREATE TABLE chat.chatroom (
	    id bigserial NOT NULL,
	    algorithm_enum algorithm_enum NOT NULL,
	    encryption_mode encryption_mode NOT NULL,
	    padding_mode padding_mode NOT NULL,
	    CONSTRAINT chatroom_pk PRIMARY KEY (id)
        );

        COMMENT ON TABLE chat.chatroom IS 'Чат-комната';

        -- Column comments

        COMMENT ON COLUMN chat.chatroom.id IS 'Идентификатор чат-комнаты';
        COMMENT ON COLUMN chat.chatroom.encryption_mode IS 'Режим шифрования';
        COMMENT ON COLUMN chat.chatroom.padding_mode IS 'Режим дополнения';
        COMMENT ON COLUMN chat.chatroom.algorithm_enum IS 'Алгоритм';

        -- table user
        CREATE TABLE chat."user" (
        	id bigserial NOT NULL,
        	login text NOT NULL,
        	diffie_hellman_number text NOT NULL,
        	CONSTRAINT user_pk PRIMARY KEY (id)
        );
        COMMENT ON TABLE chat."user" IS 'Пользователь';

        -- Column comments

        COMMENT ON COLUMN chat."user".id IS 'Идентификатор пользователя';
        COMMENT ON COLUMN chat."user".login IS 'Логин';
        COMMENT ON COLUMN chat."user".diffie_hellman_number IS 'Публичный ключ';

        -- table chatroom_user
        CREATE TABLE chat.chatroom_user (
        	"user" bigint NOT NULL,
        	chatroom bigint NOT NULL,
        	CONSTRAINT chatroom_user_pk PRIMARY KEY ("user",chatroom)
        );
        COMMENT ON TABLE chat.chatroom_user IS 'Пользователь чата';

        -- Column comments

        COMMENT ON COLUMN chat.chatroom_user."user" IS 'Пользователь';
        COMMENT ON COLUMN chat.chatroom_user.chatroom IS 'Чат-комната';

        -- table message
        CREATE TABLE chat.message (
        	id bigserial NOT NULL,
        	chatroom bigint NOT NULL,
        	sender bigint NOT NULL,
        	"text" bytea,
        	filename text,
        	file_part_number INT,
        	file_part_count INT,
        	CONSTRAINT message_pk PRIMARY KEY (id),
        	CONSTRAINT message_chatroom_fk FOREIGN KEY (chatroom) REFERENCES chat.chatroom(id),
        	CONSTRAINT message_user_fk FOREIGN KEY (sender) REFERENCES chat."user"(id)
        );
        COMMENT ON TABLE chat.message IS 'Сообщение';


    END
$$