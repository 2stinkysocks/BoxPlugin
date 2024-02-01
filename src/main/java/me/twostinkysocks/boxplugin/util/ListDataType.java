package me.twostinkysocks.boxplugin.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListDataType implements PersistentDataType<byte[], List>  {

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<List> getComplexType() {
        return List.class;
    }

    @NotNull
    @Override
    public byte[] toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream oos = new BukkitObjectOutputStream(bos)) {
            oos.writeObject(complex);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @NotNull
    @Override
    public List fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(primitive);
            BukkitObjectInputStream ois = new BukkitObjectInputStream(bis)) {
            return (List) ois.readObject();
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }
}
