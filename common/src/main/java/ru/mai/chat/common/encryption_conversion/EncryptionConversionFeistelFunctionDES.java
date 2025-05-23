package ru.mai.chat.common.encryption_conversion;

import ru.mai.chat.common.utils.Operations;
import ru.mai.chat.common.utils.Permutation;

public class EncryptionConversionFeistelFunctionDES implements EncryptionConversion {
    private static final int[][] sBlocks = {
            {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                    0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                    4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                    15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13},
            {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                    3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                    0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                    13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9},
            {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
                    13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                    13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
                    1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12},
            {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
                    13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                    10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
                    3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14},
            {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
                    14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                    4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
                    11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3},
            {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
                    10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                    9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
                    4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13},
            {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
                    13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                    1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
                    6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12},
            {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
                    1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                    7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
                    2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11},
    };
    private static final int[] expansionFunctionE = {
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1};

    private static final int[] permutationTableP = {
            16, 7, 20, 21, 29, 12, 28, 17,
            1, 15, 23, 26, 5, 18, 31, 10,
            2, 8, 24, 14, 32, 27, 3, 9,
            19, 13, 30, 6, 22, 11, 4, 25
    };

    // todo: make prettier. Now it's ugly as ass
    private int getIndexInSBlock(byte b) {
        // if next 6 digits are indexes of bits: 1 2 3 4 5 6 0 0 --> 1 6 -- #str, 0 0 0 0 2 3 4 5 -- #column
        // in s block there are 4 strings and 16 columns
        byte[] bArr = {b};

        byte[] str = new byte[1];
        Operations.setBit(6,
                Operations.getBit(0, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                str,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        Operations.setBit(7,
                Operations.getBit(5, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                str,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        int strInt = str[0];

        byte[] col = new byte[1];
        Operations.setBit(4,
                Operations.getBit(1, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                col,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        Operations.setBit(5,
                Operations.getBit(2, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                col,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        Operations.setBit(6,
                Operations.getBit(3, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                col,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        Operations.setBit(7,
                Operations.getBit(4, bArr, Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0),
                col,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_0);
        int colInt = col[0];

        return strInt * 16 + colInt;
    }

    /**
     * @param input    is array of bytes of size 4
     * @param roundKey is array of bytes of size 6
     * @return Returns encoded data as array of bytes of size 4
     */
    @Override
    public byte[] encrypt(byte[] input, byte[] roundKey) {
        // expandedInput is of size 32 bit = 4 byte
        byte[] expandedInput = Permutation.permute(input, expansionFunctionE,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_1);

        // expandedInputXorRoundKey is of size 48 bit = 6 byte
        byte[] expandedInputXorRoundKey = Operations.xor(expandedInput, roundKey);

        // B is array of bytes of 64 bits where in every byte only the first 6 significant digits are sufficient
        byte[] B = new byte[8];

        for (int i = 0, bitCounterXor = 0; i < 48; i++, bitCounterXor++) {
            // get 6 bits 8 times
            B[i / 6] |= (byte) ((1 << (7 - (bitCounterXor % 8))) & (expandedInputXorRoundKey[bitCounterXor / 8] & 0xff));
        }

        byte[] B_SBlock = new byte[4];

        for (int i = 0; i < 8; i++) {
            int indexInSBlock = getIndexInSBlock(B[i]);

            byte fromSBlock = (byte) sBlocks[i][indexInSBlock];

            if (i % 2 == 0) {
                fromSBlock = (byte) (fromSBlock << 4);
            }
            B_SBlock[i / 2] |= fromSBlock;
        }

        return Permutation.permute(B_SBlock, permutationTableP,
                Operations.IndexingRule.FROM_MOST_TO_LEAST_START_WITH_1);
    }
}
