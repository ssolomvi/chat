package ru.mai.chat.common.encryption_mode.abstr;

import ru.mai.chat.common.encryption_mode.EncryptionMode;

public interface EncryptionModeWithInitVector extends EncryptionMode {
    void invokeNextAsNew();
}
