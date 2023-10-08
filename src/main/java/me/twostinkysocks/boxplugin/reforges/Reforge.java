package me.twostinkysocks.boxplugin.reforges;

import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.reforges.AbstractReforge;
import me.twostinkysocks.boxplugin.reforges.impl.AttackReforge;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Reforge {
    private static final Class<? extends AttackReforge> ATTACK = AttackReforge.class;


    private static final Map<String, Class<? extends AbstractReforge>> BY_NAME = Maps.newHashMap();

    public static Class<? extends AbstractReforge> getByName(String name) {
        return BY_NAME.get(name);
    }

    public static List<String> getKeys() {
        return new ArrayList<String>(BY_NAME.keySet());
    }

    public static AbstractReforge ofLevel(Class<? extends AbstractReforge> reforge, double level) {
        try {
            return reforge.getDeclaredConstructor(Double.class).newInstance(level);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        try {
            // i am going mentally insane
            for (Field reforge : Arrays.stream(Reforge.class.getDeclaredFields()).filter(f -> f.getType().equals(Class.class)).collect(Collectors.toList())) {
                BY_NAME.put(reforge.getName(), (Class<? extends AbstractReforge>) reforge.get(null));
            }
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}