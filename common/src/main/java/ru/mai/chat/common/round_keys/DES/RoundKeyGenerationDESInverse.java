package ru.mai.chat.common.round_keys.DES;

import ru.mai.chat.common.utils.Operations;

public class RoundKeyGenerationDESInverse extends RoundKeyGenerationDESDirect {

    @Override
    public byte[][] generateRoundKeys(byte[] key) {
        byte[][] keys = super.generateRoundKeys(key);
        return Operations.reverseArrayOfArray(keys);
    }
}
