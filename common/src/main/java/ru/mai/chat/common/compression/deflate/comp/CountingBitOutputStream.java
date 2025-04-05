package ru.mai.chat.common.compression.deflate.comp;

final class CountingBitOutputStream implements BitOutputStream {

    private long length = 0;


    @Override
    public void writeBits(int value, int numBits) {
        length += numBits;
    }


    @Override
    public int getBitPosition() {
        return (int) length % 8;
    }


    public long getBitLength() {
        return length;
    }

}
