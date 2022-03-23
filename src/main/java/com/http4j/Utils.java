package com.http4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

public class Utils {
    private static final String DGT = "0123456789";
    private static final SecureRandom rnd = new SecureRandom();

    public static String createMultipartId(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(DGT.charAt(rnd.nextInt(DGT.length())));
        return sb.toString();
    }

    public static byte[] unwrapBody(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            baos.write(data, 0, nRead);
        }
        baos.flush();
        is.close();
        baos.close();
        return baos.toByteArray();
    }

}
