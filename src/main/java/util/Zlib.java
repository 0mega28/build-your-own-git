package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

public class Zlib {
    private Zlib() {
    }

    public static String decompress(InputStream compressed) {
        InflaterInputStream in = new InflaterInputStream(compressed);

        try {
            return new String(in.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteArrayOutputStream compress(String uncompressedString) {
        Deflater deflater = new Deflater();
        deflater.setInput(uncompressedString.getBytes());
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int compressedLength = deflater.deflate(buffer);
            outputStream.write(buffer, 0, compressedLength);
        }

        return outputStream;
    }
}
