package me.twostinkysocks.boxplugin.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.twostinkysocks.boxplugin.BoxPlugin;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.World;

public class PacketListeners {
    public PacketListeners() {
        System.out.println("Enabling Sound Listener...");
        BoxPlugin.instance.getProtocolManager().addPacketListener(new PacketAdapter(
                BoxPlugin.instance,
                PacketType.Play.Server.NAMED_SOUND_EFFECT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.NAMED_SOUND_EFFECT) return;

                if (event.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) return;

                PacketContainer packet = event.getPacket();

                for (Object obj : packet.getModifier().getValues()) {
                    if (obj == null) continue;

                    String str = obj.toString();
                    if (str.contains("ender_dragon") && str.contains("death")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        });
    }
}
