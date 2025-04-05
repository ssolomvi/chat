package ru.mai.chat.common.encryption_algorithm.attacks;

import ru.mai.chat.common.utils.Pair;

import java.math.BigInteger;

public interface RSAAttacks {
    // d, phi
    Pair<BigInteger, BigInteger> attack(Pair<BigInteger, BigInteger> publicKey);
}
