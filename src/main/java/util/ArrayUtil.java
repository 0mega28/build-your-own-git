package util;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class ArrayUtil {
    private ArrayUtil() {
    }

    public static <T> T[] shift(T[] array) {
        if (array.length == 0) throw new NoSuchElementException();
        return Arrays.stream(array)
                .skip(1)
                .toArray(size -> Arrays.copyOf(array, size));
    }
}
