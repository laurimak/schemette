package schemette;

import java.util.List;

public class Util {
    public static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static <T> List<T> rest(List<T> list) {
        return list.subList(1, list.size());
    }
}
