package ru.mai.chat.common.primality_test;

import java.math.BigInteger;

public interface ProbabilisticPrimalityTest {
    boolean probabilisticPrimalityTest(BigInteger number, double minProbability);
}
