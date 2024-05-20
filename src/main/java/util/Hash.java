package util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private Hash () {}

    public static String sha1(String content) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-1");
            msgDigest.update(content.getBytes(StandardCharsets.UTF_8), 0, content.length());
            return StringUtil.byteArrayToHexString(msgDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

}
