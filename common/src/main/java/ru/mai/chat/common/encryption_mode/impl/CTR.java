package ru.mai.chat.common.encryption_mode.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mai.chat.common.encryption_algorithm.EncryptionAlgorithm;
import ru.mai.chat.common.encryption_mode.abstr.EncryptionModeAbstract;
import ru.mai.chat.common.encryption_mode.abstr.EncryptionModeCounter;
import ru.mai.chat.common.exceptions.IllegalArgumentExceptionWithLog;
import ru.mai.chat.common.utils.Operations;

import java.math.BigInteger;

public class CTR extends EncryptionModeAbstract implements EncryptionModeCounter {
    private static final Logger log = LoggerFactory.getLogger(CTR.class);
    private final int nonceSize;
    private final BigInteger remainder;
    private byte[] nonce;

    public CTR(EncryptionAlgorithm algorithm, byte[] initVector) {
        super(algorithm, initVector);

        nonceSize = algorithm.getAlgorithmBlockForEncryption() / 2;

        invokeNextAsNew();

        this.remainder = BigInteger.ONE.shiftLeft(8 * nonceSize);
    }

    @Override
    public byte[] encrypt(byte[] input) {
        String errorString = "Method byte[] encrypt(byte[] input) cannot be invoked in class " + getClass().getName();
        log.error(errorString);
        throw new UnsupportedOperationException(errorString);
    }

    @Override
    public byte[] decrypt(byte[] input) {
        String errorString = "Method byte[] decrypt(byte[] input) cannot be invoked in class " + getClass().getName();
        log.error(errorString);
        throw new UnsupportedOperationException(errorString);
    }

    private byte[] getCounter(int blockNumber) {
        byte[] currCounter = BigInteger.valueOf(blockNumber).remainder(this.remainder).toByteArray();

        int lengthCounter = getAlgorithmBlockForEncryption() / 2;

        if (currCounter.length != lengthCounter) {
            // append curr counter so it has appropriate size
            byte[] toReturn = new byte[lengthCounter];
            System.arraycopy(currCounter, 0,
                    toReturn, lengthCounter - currCounter.length,
                    currCounter.length);
            currCounter = toReturn;
        }

        return currCounter;
    }

    /**
     * Encrypts array of bytes according to formula:
     * <p>
     * C_i = P_i xor E(CTR_i)
     */
    @Override
    public byte[] encrypt(byte[] input, int blockNumber) {
        if (input.length % getAlgorithmBlockForEncryption() != 0) {
            throw new IllegalArgumentExceptionWithLog("Input block size is not appropriate for this algorithm", log);
        }
        if (input.length == 0) {
            return new byte[0];
        }

        int countOfBlocks = input.length / getAlgorithmBlockForEncryption();
        byte[] encryptResult = new byte[countOfBlocks * getAlgorithmBlockForDecryption()];

        byte[] toEncrypt = new byte[getAlgorithmBlockForEncryption()];

        for (int i = 0, idxInput = 0, idxOutput = 0;
             i < countOfBlocks;
             i++, idxInput += getAlgorithmBlockForEncryption(), idxOutput += getAlgorithmBlockForDecryption()) {

            System.arraycopy(input, idxInput,
                    toEncrypt, 0,
                    getAlgorithmBlockForEncryption());

            System.arraycopy(Operations.xor(toEncrypt, algorithm.encrypt(Operations.mergeByteArrays(nonce, getCounter(blockNumber)))), 0,
                    encryptResult, idxOutput,
                    getAlgorithmBlockForDecryption());

            ++blockNumber;
        }

        return encryptResult;
    }

    /**
     * Decrypts array of bytes according to formula:
     * <p>
     * P_i = C_i xor E(CTR_i)
     */
    @Override
    public byte[] decrypt(byte[] input, int blockNumber) {
        if (input.length % getAlgorithmBlockForDecryption() != 0) {
            throw new IllegalArgumentExceptionWithLog("Input block size is not appropriate for this algorithm", log);
        }
        if (input.length == 0) {
            return new byte[0];
        }

        int countOfBlocks = input.length / getAlgorithmBlockForDecryption();
        byte[] decryptResult = new byte[countOfBlocks * getAlgorithmBlockForEncryption()];

        byte[] toDecrypt = new byte[getAlgorithmBlockForDecryption()];

        for (int i = 0, idxInput = 0, idxOutput = 0;
             i < countOfBlocks;
             i++, idxInput += getAlgorithmBlockForDecryption(), idxOutput += getAlgorithmBlockForEncryption()) {

            System.arraycopy(input, idxInput,
                    toDecrypt, 0,
                    getAlgorithmBlockForDecryption());

            System.arraycopy(Operations.xor(toDecrypt, algorithm.encrypt(Operations.mergeByteArrays(nonce, getCounter(blockNumber)))), 0,
                    decryptResult, idxOutput,
                    getAlgorithmBlockForEncryption());

            ++blockNumber;
        }

        return decryptResult;
    }

    @Override
    public void invokeNextAsNew() {
        this.nonce = new byte[nonceSize];
        System.arraycopy(initVector, 0, this.nonce, 0, nonceSize);
    }
}
