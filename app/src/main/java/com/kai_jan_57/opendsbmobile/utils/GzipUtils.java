package com.kai_jan_57.opendsbmobile.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
    public static byte[] encodeGzip(byte[] input) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(input.length);
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gZIPOutputStream.write(input);
        gZIPOutputStream.close();
        byte[] output = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return output;
    }

    public static byte[] decodeGzip(byte[] input) throws IOException {
        InputStream byteArrayInputStream = new ByteArrayInputStream(input);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream, 32);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr2 = new byte[32];
        while (true) {
            int read = gZIPInputStream.read(bArr2);
            if (read != -1) {
                byteArrayOutputStream.write(bArr2, 0, read);
            } else {
                gZIPInputStream.close();
                byteArrayInputStream.close();
                byte[] output = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                return output;
            }
        }
    }
}
