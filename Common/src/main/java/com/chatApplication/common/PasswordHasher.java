package com.chatApplication.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String algorithm = "SHA-256";
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static PasswordHasher instance = new PasswordHasher();

    private PasswordHasher() {
    }

    public static PasswordHasher getInstance() {
        return instance;
    }

    public String generateHash(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.reset();
        byte[] hash = digest.digest(password.getBytes());

        return bytesToStringHex(hash);
    }

    private String bytesToStringHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(getInstance().generateHash("john"));
    }
}
