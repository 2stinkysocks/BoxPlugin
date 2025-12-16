package me.twostinkysocks.boxplugin.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.twostinkysocks.boxplugin.BoxPlugin;

public class PacketListeners {
    public PacketListeners() {
        System.out.println("Enabling Sound Listener...");
        BoxPlugin.instance.getProtocolManager().addPacketListener(new PacketAdapter(
                BoxPlugin.instance,
                PacketType.Play.Server.NAMED_SOUND_EFFECT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                String worldName = event.getPlayer().getWorld().getName();
                int effectId = event.getPacket().getIntegers().read(0);

                if (effectId == 1018) {
                    if ("Xanatos_the_end".equals(worldName)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}
