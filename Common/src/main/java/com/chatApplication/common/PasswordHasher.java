package com.chatApplication.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static PasswordHasher instance;

    private PasswordHasher() {
    }

    public static PasswordHasher getInstance() {
        if (instance == null) {
            instance = new PasswordHasher();
        }
        return instance;
    }

    public String generateHash(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.reset();
        byte[] hash = digest.digest(password.getBytes());

        return bytesToStringHex(hash);
    }

    private String bytesToStringHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(getInstance().generateHash("john"));
    }
}
