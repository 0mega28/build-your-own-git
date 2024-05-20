import exceptions.NotEnoughArgumentException;
import util.ArrayUtil;
import util.Hash;
import util.Zlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        final String command = args[0];

        try {
            switch (command) {
                case "init" -> {
                    try {
                        Files.createDirectory(Path.of(".git"));
                        Files.createDirectory(Path.of(".git", "objects"));
                        Files.createDirectory(Path.of(".git", "refs"));
                        Files.writeString(Path.of(".git/HEAD"), "ref: refs/heads/main\n");
                        System.out.println("Initialized git directory");
                    } catch (IOException e) {
                        System.err.println("Error initializing git directory: " + e.getMessage());
                    }
                }
                case "cat-file" -> {
                    args = ArrayUtil.shift(args);
                    handleCatFile(args);
                }
                case "hash-object" -> {
                    args = ArrayUtil.shift(args);
                    handleHashObject(args);
                }
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (NotEnoughArgumentException ex) {
            System.err.println("Not enough arguments: " + ex.getMessage());
        }
    }

    private static void handleHashObject(String[] args) {
        final String flag = args[0];

        switch (flag) {
            case "-w" -> {
                args = ArrayUtil.shift(args);
                String fileName = args[0];
                Path filePath = Path.of(fileName);
                if (!Files.isRegularFile(filePath)) {
                    System.err.println("File does not exist: " + fileName);
                    System.exit(1);
                }
                String objectContent = null;
                try {
                    String fileContent = Files.readString(filePath);
                    objectContent = String.format("blob %d\0%s",
                            fileContent.length(), fileContent);
                } catch (IOException e) {
                    System.err.println("Error reading file: " + fileName);
                    System.exit(1);
                }

                String sha1Hash = Hash.sha1(objectContent);
                System.out.println(sha1Hash);

                String directoryName = sha1Hash.substring(0, 2);
                String outputFileName = sha1Hash.substring(2);

                try {
                    Files.createDirectory(Path.of(".git", "objects", directoryName));
                } catch (IOException e) {
                    System.err.println("Error creating directory: " + directoryName);
                    System.exit(1);
                }
                ByteArrayOutputStream zlibCompressedOutStream = Zlib.compress(objectContent);

                try (OutputStream fileOutputStream = Files.newOutputStream(
                        Path.of(".git", "objects", directoryName, outputFileName))) {
                    zlibCompressedOutStream.writeTo(fileOutputStream);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + outputFileName);
                    System.exit(1);
                }
            }
            default -> System.err.println("Unknown flag for hash-object: " + flag);
        }
    }

    private static void handleCatFile(String[] args) {
        final String flag = args[0];

        switch (flag) {
            case "-p" -> {
                args = ArrayUtil.shift(args);
                final String blobSha = args[0];
                final int MIN_BLOB_SHA_LENGTH = 2;
                if (blobSha.length() <= MIN_BLOB_SHA_LENGTH) {
                    System.err.printf("Invalid blob sha: %s expected length more than %d length%n"
                            , blobSha, MIN_BLOB_SHA_LENGTH);
                    System.exit(1);
                }

                final String directory = blobSha.substring(0, 2);
                final String fileName = blobSha.substring(2);

                boolean directoryExists = Files.isDirectory(Path.of(".git", "objects", directory));

                if (!directoryExists) {
                    System.err.println("Directory does not exist: " + directory);
                    System.exit(1);
                    ;
                }

                try {
                    InputStream compressedData = Files.newInputStream(
                            Path.of(".git", "objects", directory, fileName));
                    String data = Zlib.decompress(compressedData);

                    Pattern blobPattern = Pattern.compile("^blob\\s(\\d+)\0(.*)$");
                    Matcher matcher = blobPattern.matcher(data);
                    if (!matcher.find()) {
                        System.err.println("Invalid blob sha: " + data);
                        System.exit(1);
                    }
                    Integer bytes = Integer.parseInt(matcher.group(1));
                    String content = matcher.group(2);

//                    if (content.length() != bytes) {
//                        System.err.printf("Invalid blob sha: %s expected %d bytes but got %d bytes\n"
//                                , data, bytes, content.length());
//                        System.exit(1);
//                    }

                    System.out.print(content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                System.err.println("Unknown flag: " + flag);
            }
        }
    }
}
