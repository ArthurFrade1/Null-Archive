package com.arthurfrade.nullarchive.util;

import java.security.SecureRandom;

import com.sun.net.httpserver.HttpExchange;

public final class TokenUtil {
    private static final SecureRandom RNG = new SecureRandom();

    public static String generateSessionTokenHex64() {
        byte[] bytes = new byte[32]; // 32 bytes = 256 bits
        RNG.nextBytes(bytes);
        return toHex(bytes); // 64 chars
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        char[] digits = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = digits[v >>> 4];
            hex[i * 2 + 1] = digits[v & 0x0F];
        }
        return new String(hex);
    }
    public static String getCookieValue(HttpExchange exchange, String name) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;

        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return kv[1];
            }
        }
        return null;
    }
}
