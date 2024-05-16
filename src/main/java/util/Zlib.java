package util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
}
