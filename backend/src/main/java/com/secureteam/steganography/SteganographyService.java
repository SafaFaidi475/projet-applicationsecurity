package com.secureteam.steganography;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

@ApplicationScoped
public class SteganographyService {

    private static final Logger LOGGER = Logger.getLogger(SteganographyService.class.getName());

    public byte[] embed(byte[] coverImageBytes, byte[] messageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(coverImageBytes));
            if (image == null) {
                throw new IOException("Failed to read image.");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // We need 32 bits for the length + message bits
            if (messageBytes.length * 8 + 32 > width * height * 3) {
                throw new IllegalArgumentException("Message too large for this image.");
            }

            // Embed length (32 bits)
            int messageLength = messageBytes.length;
            embedValue(image, 0, 32, messageLength);

            // Embed message bits starting after the length (pixel offset 32/3 ~ 11)
            for (int i = 0; i < messageBytes.length; i++) {
                embedValue(image, 32 + (i * 8), 8, messageBytes[i] & 0xFF);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Steganography embedding failed", e);
            return coverImageBytes;
        }
    }

    public byte[] extract(byte[] stegoImageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(stegoImageBytes));
            if (image == null) {
                throw new IOException("Failed to read image.");
            }

            // Extract length (32 bits)
            int messageLength = extractValue(image, 0, 32);
            if (messageLength < 0 || messageLength > 1000000) { // Sanity check 1MB
                return "INVALID_OR_NO_DATA".getBytes();
            }

            byte[] message = new byte[messageLength];
            for (int i = 0; i < messageLength; i++) {
                message[i] = (byte) extractValue(image, 32 + (i * 8), 8);
            }

            return message;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Steganography extraction failed", e);
            return null;
        }
    }

    private void embedValue(BufferedImage image, int bitOffset, int bitCount, int value) {
        int width = image.getWidth();
        for (int i = 0; i < bitCount; i++) {
            int currentBitOffset = bitOffset + i;
            int pixelX = (currentBitOffset / 3) % width;
            int pixelY = (currentBitOffset / 3) / width;
            int channel = currentBitOffset % 3; // 0=R, 1=G, 2=B

            int rgb = image.getRGB(pixelX, pixelY);
            int bit = (value >> (bitCount - 1 - i)) & 1;

            if (channel == 0) { // Red
                rgb = (rgb & 0xFFFEFFFF) | (bit << 16);
            } else if (channel == 1) { // Green
                rgb = (rgb & 0xFFFFFEFF) | (bit << 8);
            } else { // Blue
                rgb = (rgb & 0xFFFFFFFE) | bit;
            }
            image.setRGB(pixelX, pixelY, rgb);
        }
    }

    private int extractValue(BufferedImage image, int bitOffset, int bitCount) {
        int value = 0;
        int width = image.getWidth();
        for (int i = 0; i < bitCount; i++) {
            int currentBitOffset = bitOffset + i;
            int pixelX = (currentBitOffset / 3) % width;
            int pixelY = (currentBitOffset / 3) / width;
            int channel = currentBitOffset % 3;

            int rgb = image.getRGB(pixelX, pixelY);
            int bit;
            if (channel == 0) {
                bit = (rgb >> 16) & 1;
            } else if (channel == 1) {
                bit = (rgb >> 8) & 1;
            } else {
                bit = rgb & 1;
            }
            value = (value << 1) | bit;
        }
        return value;
    }
}
