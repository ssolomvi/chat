package ru.mai.chat.common.compression.image.compressor;

import java.awt.image.BufferedImage;

public interface ImageCompressor {

    BufferedImage compress(BufferedImage img, int coefficients);

}
