package me.twostinkysocks.boxplugin.util;

import net.minecraft.nbt.NBTCompressedStreamTools;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * https://stackoverflow.com/questions/8517323/how-to-convert-map-to-bytes-and-save-to-internal-storage
 */
public class ListDataType implements PersistentDataType<List, List>  {

    private final Class<List> primitiveType = List.class;

    @NotNull
    @Override
    public Class<List> getPrimitiveType() {
        return primitiveType;
    }

    @NotNull
    @Override
    public Class<List> getComplexType() {
        return primitiveType;
    }

    @NotNull
    @Override
    public List toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
        return complex;
    }

    @NotNull
    @Override
    public List fromPrimitive(@NotNull List primitive, @NotNull PersistentDataAdapterContext context) {
        return primitive;
    }
}
