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

                String sound = event.getPacket().getSoundEffects().readSafely(0).toString();

                if (!sound.contains("entity.ender_dragon.death")) return;
                System.out.println("Found a dragon sound!");

                if (event.getPlayer().getWorld().getEnvironment() != World.Environment.THE_END) {
                    event.setCancelled(true);
                }
            }
        });
    }
}
