package ru.mai.chat.common.compression.deflate;


import ru.mai.chat.common.compression.deflate.comp.BitOutputStream;
import ru.mai.chat.common.compression.deflate.comp.Decision;
import ru.mai.chat.common.compression.deflate.comp.Lz77Huffman;
import ru.mai.chat.common.compression.deflate.comp.Strategy;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;


/**
 * Compresses a byte stream into a DEFLATE data stream
 * (raw format without zlib or gzip headers or footers).
 * <p>Currently only supports uncompressed blocks for simplicity, which actually
 * expands the data slightly, but still conforms to the DEFLATE format.</p>
 * <p>This class performs its own buffering, so it is unnecessary to wrap a {@link
 * BufferedOutputStream} around the {@link OutputStream} given to the constructor.</p>
 *
 * @see InflaterInputStream
 */
public final class DeflaterOutputStream extends OutputStream {

    private static final int MAX_HISTORY_CAPACITY = 32 * 1024;
    private final int historyLookbehindLimit;
    private final int dataLookaheadLimit;
    private final Strategy strategy;
    private final byte[] combinedBuffer;
    private OutputStream output;
    private BitOut bitOutput;
    private int historyStart = 0;
    private int historyLength = 0;
    private int dataLength = 0;


    public DeflaterOutputStream(OutputStream out) {
        this(out, 64 * 1024, MAX_HISTORY_CAPACITY, Lz77Huffman.RLE_DYNAMIC);
    }


    public DeflaterOutputStream(OutputStream out, int dataLookaheadLimit, int historyLookbehindLimit, Strategy strat) {
        output = Objects.requireNonNull(out);
        bitOutput = new BitOut();
        if (dataLookaheadLimit < 1 || historyLookbehindLimit < 0 || historyLookbehindLimit > MAX_HISTORY_CAPACITY
                || (long) dataLookaheadLimit + historyLookbehindLimit > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid capacities");
        combinedBuffer = new byte[historyLookbehindLimit + Math.max(dataLookaheadLimit, historyLookbehindLimit)];
        this.historyLookbehindLimit = historyLookbehindLimit;
        this.dataLookaheadLimit = dataLookaheadLimit;
        strategy = Objects.requireNonNull(strat);
    }


    OutputStream getUnderlyingStream() {
        if (output == null)
            throw new IllegalStateException("Stream already closed");
        return output;
    }


    @Override
    public void write(int b) throws IOException {
        if (bitOutput == null)
            throw new IllegalStateException("Stream already ended");
        if (dataLength >= dataLookaheadLimit)
            writeBuffer(false);
        combinedBuffer[historyStart + historyLength + dataLength] = (byte) b;
        dataLength++;
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (bitOutput == null)
            throw new IllegalStateException("Stream already ended");
        Objects.checkFromIndexSize(off, len, b.length);
        while (len > 0) {
            if (dataLength >= dataLookaheadLimit)
                writeBuffer(false);
            int n = Math.min(len, dataLookaheadLimit - dataLength);
            System.arraycopy(b, off, combinedBuffer, historyStart + historyLength + dataLength, n);
            off += n;
            len -= n;
            dataLength += n;
        }
    }


    public void finish() throws IOException {
        if (bitOutput == null)
            throw new IllegalStateException("Stream already ended");
        writeBuffer(true);
        bitOutput.finish();
        bitOutput = null;
    }


    @Override
    public void close() throws IOException {
        if (bitOutput != null)
            finish();
        output.close();
        output = null;
    }


    private void writeBuffer(boolean isFinal) throws IOException {
        if (bitOutput == null)
            throw new IllegalStateException("Stream already ended");

        Decision dec = strategy.decide(combinedBuffer, historyStart, historyLength, dataLength);
        dec.compressTo(bitOutput, isFinal);
        if (isFinal)
            return;

        int dataEnd = historyStart + historyLength + dataLength;
        historyLength = Math.min(historyLength + dataLength, historyLookbehindLimit);
        dataLength = 0;
        if (combinedBuffer.length - dataEnd >= dataLookaheadLimit)
            historyStart = dataEnd - historyLength;
        else {
            System.arraycopy(combinedBuffer, dataEnd - historyLength, combinedBuffer, 0, historyLength);
            historyStart = 0;
        }
    }


    private final class BitOut implements BitOutputStream {

        private long bitBuffer = 0;
        private int bitBufferLength = 0;


        @Override
        public void writeBits(int value, int numBits) throws IOException {
            assert 0 <= numBits && numBits <= 31 && value >>> numBits == 0;
            if (numBits > 64 - bitBufferLength) {
                for (; bitBufferLength >= 8; bitBufferLength -= 8, bitBuffer >>>= 8)
                    output.write((byte) bitBuffer);
            }
            assert numBits <= 64 - bitBufferLength;
            bitBuffer |= (long) value << bitBufferLength;
            bitBufferLength += numBits;
        }


        @Override
        public int getBitPosition() {
            return bitBufferLength % 8;
        }


        public void finish() throws IOException {
            writeBits(0, (8 - getBitPosition()) % 8);
            for (; bitBufferLength >= 8; bitBufferLength -= 8, bitBuffer >>>= 8)
                output.write((byte) bitBuffer);
            assert bitBufferLength == 0;
        }

    }

}
