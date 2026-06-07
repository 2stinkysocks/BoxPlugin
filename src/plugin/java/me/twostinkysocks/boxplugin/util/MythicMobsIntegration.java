package me.twostinkysocks.boxplugin.util;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.bukkit.commands.CommandHelper;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.WorldScaling;
import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class MythicMobsIntegration {

    public static UUID spawnWithData(String id, int xp, Location spawnloc) {
        return spawnWithData(id, xp, spawnloc, false);
    }
    public static UUID spawnWithData(String id, int xp, Location spawnloc, boolean ignoreMaxRadius) {
        CommandSender sender = Bukkit.getConsoleSender();
        String[] args = new String[] {
            id, "1", (spawnloc.getWorld().getName() + "," + spawnloc.getX() + "," + spawnloc.getY() + "," + spawnloc.getZ())
        };
        MythicBukkit plugin = (MythicBukkit) Bukkit.getPluginManager().getPlugin("MythicMobs");

        // copy pasted from the mythicmobs command file
        Location loc = null;
        int amount = 1;
        boolean optAtTarget = false;
        boolean optAtPlayer = false;
        boolean optSilent = false;
        boolean optNatural = false;
        if (args != null && args.length > 1 && args[0].startsWith("-")) {
            if (args[0].contains("s")) {
                optSilent = true;
            }

            if (args[0].contains("t")) {
                optAtTarget = true;
            }

            if (args[0].contains("p")) {
                optAtPlayer = true;
            }

            if (args[0].contains("n")) {
                optNatural = true;
            }

            args = (String[]) Arrays.copyOfRange(args, 1, args.length);
        }

        String mob;
        if (optAtPlayer) {
            mob = args[0];
            Player player = Bukkit.getPlayer(mob);
            if (player == null) {
                if (!optSilent) {
                    CommandHelper.sendError(sender, "Player " + mob + " not found.");
                }

                return null;
            }

            if (optAtTarget) {
                loc = player.getTargetBlock(MythicBukkit.inst().getConfiguration().getTransparentBlocks(), 200).getRelative(BlockFace.UP).getLocation();
            } else {
                loc = player.getLocation();
            }

            args = (String[])Arrays.copyOfRange(args, 1, args.length);
        }

        double level = 1.0D;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (Exception var20) {
                amount = 1;
            }
        }

        if (args.length > 2) {
            try {
                String[] part = args[2].split(",");
                World w = Bukkit.getWorld(part[0]);
                float x = Float.parseFloat(part[1]);
                float y = Float.parseFloat(part[2]);
                float z = Float.parseFloat(part[3]);
                float pitch = 0.0F;
                float yaw = 0.0F;
                if (part.length > 4) {
                    yaw = Float.parseFloat(part[4]);
                }

                if (part.length > 5) {
                    pitch = Float.parseFloat(part[5]);
                }

                if (w != null) {
                    loc = new Location(w, (double)x, (double)y, (double)z, yaw, pitch);
                }
            } catch (Exception var19) {
                CommandHelper.sendError(sender, "Invalid location specified for spawning a mob: location must be in format world,x,y,z,yaw,pitch");
                return null;
            }
        } else if (!optAtPlayer && sender instanceof Player) {
            if (optAtTarget) {
                loc = ((Player)sender).getTargetBlock(((MythicBukkit)plugin).getConfiguration().getTransparentBlocks(), 200).getRelative(BlockFace.UP).getLocation();
            } else {
                loc = ((Player)sender).getLocation();
            }
        }

        if (loc == null) {
            if (!optSilent) {
                CommandHelper.sendError(sender, "Invalid location specified for spawning a mob: world does not exist.");
            }

            return null;
        } else {
            AbstractLocation aLocation = BukkitAdapter.adapt(loc);
            if (args[0].contains(":")) {
                String[] split = args[0].split(":");
                mob = split[0];

                try {
                    level = Double.parseDouble(split[1]);
                } catch (Error | Exception var21) {
                    if (!optSilent) {
                        CommandHelper.sendError(sender, "Invalid mob level supplied: must be a number.");
                    }

                    return null;
                }
            } else {
                mob = args[0];
                level = WorldScaling.getLevelBonus(BukkitAdapter.adapt(loc));
            }

            int i;
            if (((MythicBukkit)plugin).getMobManager().getMythicMob(mob).isPresent()) {
                ActiveMob spawnedMob = null;
                for(i = 0; i < amount; ++i) {
                    if (optNatural) {
                        spawnedMob = ((MythicBukkit)plugin).getMobManager().spawnMob(mob, aLocation, SpawnReason.NATURAL, level);
                    } else {
                        spawnedMob = ((MythicBukkit)plugin).getMobManager().spawnMob(mob, aLocation, SpawnReason.COMMAND, level);
                    }
                    Entity entity = spawnedMob.getEntity().getBukkitEntity();
                    entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING, id);
                    entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmob"), PersistentDataType.INTEGER, 1);
                    if(xp != 0) entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, xp);
                    if(ignoreMaxRadius) {
                        entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ignoremaxradius"), PersistentDataType.INTEGER, 1);
                    }
                    return spawnedMob.getUniqueId();
                }

                if (spawnedMob != null) {
                    if (!optSilent) {
                        CommandHelper.sendSuccess(sender, "Spawned " + amount + "x " + mob + "&a!");
                    }
                } else if (!optSilent) {
                    CommandHelper.sendError(sender, "Failed to spawn mob. See console for more details. If no error then you are in a mob spawning denied region.");
                }
            } else if (MythicEntityType.get(mob) != null) {
                for(i = 0; i < amount; ++i) {
                    Entity entity;
                    if (optNatural) {
                        entity = BukkitEntityType.getMythicEntity(mob).spawn(loc, SpawnReason.NATURAL);
                    } else {
                        entity = BukkitEntityType.getMythicEntity(mob).spawn(loc, SpawnReason.COMMAND);
                    }

                    MythicEntityType mythicType = ((MythicBukkit)plugin).getBootstrap().getMythicEntityType(entity);
                    Optional<MythicMob> maybeMob = ((MythicBukkit)plugin).getMobManager().getVanillaType(mythicType);
                    if (maybeMob.isPresent()) {
                        MythicMob mm = (MythicMob)maybeMob.get();
                        ActiveMob am = ((MythicBukkit)plugin).getMobManager().registerActiveMob(BukkitAdapter.adapt(entity), mm, (double)((int)level));
                        mm.getMythicEntity().applyOptions(entity);
                        mm.applyMobOptions(am, level);
                        mm.applyMobVolatileOptions(am);
                        mm.applySpawnModifiers(am);
                        entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING, id);
                        entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmob"), PersistentDataType.INTEGER, 1);
                        if(ignoreMaxRadius) {
                            entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ignoremaxradius"), PersistentDataType.INTEGER, 1);
                        }
                        if(xp != 0) entity.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, xp);
                    }
                    return entity.getUniqueId();
                }

                if (!optSilent) {
                    CommandHelper.sendSuccess(sender, "Spawned " + amount + "x " + mob + "&a!");
                }
            } else {
                try {
                    EntityType type = EntityType.valueOf(mob);

                    for(i = 0; i < amount; ++i) {
                        Entity e = loc.getWorld().spawnEntity(loc, type);
                        return e.getUniqueId();
                    }

                    if (!optSilent) {
                        CommandHelper.sendSuccess(sender, "Spawned " + amount + "x " + mob + "&a!");
                    }

                    return null;
                } catch (Exception var22) {
                    if (!optSilent) {
                        CommandHelper.sendError(sender, "No mob type found with the name " + mob + ".");
                    }
                }
            }

            return null;
        }
    }
}
