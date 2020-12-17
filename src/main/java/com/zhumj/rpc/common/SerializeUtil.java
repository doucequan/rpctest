package com.zhumj.rpc.common;

import java.io.*;

public class SerializeUtil {
    static final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public synchronized static byte[] serializeObject(Object obj) {
        byteArrayOutputStream.reset();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);) {

            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    public synchronized static <T> T deserialize(byte[] headerBytes, Class<T> tClass) {
        try(ByteArrayInputStream in = new ByteArrayInputStream(headerBytes);
            ObjectInputStream oin = new ObjectInputStream(in);) {
            return (T) oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

}
