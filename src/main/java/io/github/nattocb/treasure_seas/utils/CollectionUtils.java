package io.github.nattocb.treasure_seas.utils;

import java.io.*;
import java.util.List;

/**
 * @ClassName: CollectionUtils
 * @Description:
 * @Author JasperFang
 * @Date 2024/9/25
 * @Version 1.0
 */
public class CollectionUtils {

    public static <T> List<T> deepClone(List<T> list) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deep clone failed", e);
        }
    }

}
