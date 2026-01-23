package com.arthurfrade.nullarchive.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MultipartUtil {

    public static final class Part {
        public final Map<String, String> headers;
        public final String name;
        public final String filename;
        public final String contentType;
        public final byte[] data;

        public Part(Map<String, String> headers, String name, String filename, String contentType, byte[] data) {
            this.headers = headers;
            this.name = name;
            this.filename = filename;
            this.contentType = contentType;
            this.data = data;
        }

        public String asStringUtf8() {
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    public static Map<String, Part> parse(HttpExchange exchange) throws IOException {
        String ct = exchange.getRequestHeaders().getFirst("Content-Type");
        String boundary = extractBoundary(ct);
        if (boundary == null) throw new IOException("No boundary");

        byte[] body = exchange.getRequestBody().readAllBytes();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);

        List<int[]> chunks = splitByBoundary(body, boundaryBytes);
        Map<String, Part> parts = new HashMap<>();

        for (int[] range : chunks) {
            int start = range[0];
            int end = range[1];

            if (end <= start) continue;

            Part p = parsePart(Arrays.copyOfRange(body, start, end));
            if (p != null && p.name != null) parts.put(p.name, p);
        }

        return parts;
    }

    private static String extractBoundary(String contentType) {
        if (contentType == null) return null;
        String[] items = contentType.split(";");
        for (String it : items) {
            String s = it.trim();
            if (s.startsWith("boundary=")) {
                String b = s.substring("boundary=".length());
                if (b.startsWith("\"") && b.endsWith("\"") && b.length() >= 2) {
                    b = b.substring(1, b.length() - 1);
                }
                return b;
            }
        }
        return null;
    }

    private static Part parsePart(byte[] bytes) {
        int sep = indexOf(bytes, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1), 0);
        if (sep < 0) return null;

        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, sep);
        byte[] dataBytes = Arrays.copyOfRange(bytes, sep + 4, bytes.length);

        // remove CRLF final
        if (dataBytes.length >= 2 && dataBytes[dataBytes.length - 2] == '\r' && dataBytes[dataBytes.length - 1] == '\n') {
            dataBytes = Arrays.copyOfRange(dataBytes, 0, dataBytes.length - 2);
        }

        String headerText = new String(headerBytes, StandardCharsets.ISO_8859_1);
        Map<String, String> headers = new HashMap<>();

        for (String line : headerText.split("\r\n")) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String k = line.substring(0, idx).trim().toLowerCase();
                String v = line.substring(idx + 1).trim();
                headers.put(k, v);
            }
        }

        String cd = headers.get("content-disposition");
        String name = extractDispValue(cd, "name");
        String filename = extractDispValue(cd, "filename");
        String ctype = headers.getOrDefault("content-type", "application/octet-stream");

        return new Part(headers, name, filename, ctype, dataBytes);
    }

    private static String extractDispValue(String contentDisposition, String key) {
        if (contentDisposition == null) return null;
        String[] parts = contentDisposition.split(";");
        for (String p : parts) {
            String s = p.trim();
            if (s.startsWith(key + "=")) {
                String v = s.substring((key + "=").length()).trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);
                }
                return v;
            }
        }
        return null;
    }

    private static List<int[]> splitByBoundary(byte[] body, byte[] boundaryBytes) {
        List<int[]> ranges = new ArrayList<>();

        int i = 0;
        while (true) {
            int bStart = indexOf(body, boundaryBytes, i);
            if (bStart < 0) break;

            int afterBoundary = bStart + boundaryBytes.length;
            // termina?
            if (afterBoundary + 1 < body.length && body[afterBoundary] == '-' && body[afterBoundary + 1] == '-') break;

            // pula CRLF
            int partStart = afterBoundary;
            if (partStart + 1 < body.length && body[partStart] == '\r' && body[partStart + 1] == '\n') partStart += 2;

            int nextBoundary = indexOf(body, boundaryBytes, partStart);
            if (nextBoundary < 0) break;

            int partEnd = nextBoundary;
            ranges.add(new int[]{partStart, partEnd});

            i = nextBoundary;
        }

        return ranges;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from) {
        outer:
        for (int i = from; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
