package ru.mai.chat.common.compression.deflate.comp;


public interface Strategy {

    Decision decide(byte[] b, int off, int historyLen, int dataLen);

}
