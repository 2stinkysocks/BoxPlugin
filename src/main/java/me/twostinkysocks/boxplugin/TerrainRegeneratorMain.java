package me.twostinkysocks.boxplugin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.twostinkysocks.boxplugin.util.MythicMobsIntegration;
import me.twostinkysocks.boxplugin.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftWarden;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class TerrainRegeneratorMain implements Listener, CommandExecutor, TabCompleter {

    private ArrayList<BukkitTask> tasks;

    private ArrayList<Entity> toDeAgro;

    private HashMap<String, ArrayList<UUID>> spawnedEntities;

    private YamlConfiguration config;

    public void onEnable() {
        tasks = new ArrayList<>();
        toDeAgro = new ArrayList<>();
        spawnedEntities = new HashMap<String, ArrayList<UUID>>();
        BoxPlugin.instance.getCommand("terrainregenerator").setExecutor(this);
        BoxPlugin.instance.getCommand("mmxp").setExecutor(this);
        BoxPlugin.instance.getCommand("mmxp").setTabCompleter(this);
        BoxPlugin.instance.getCommand("terrainregenerator").setTabCompleter(this);
        BoxPlugin.instance.getCommand("regenschematic").setExecutor(this);
        BoxPlugin.instance.getCommand("regenschematic").setTabCompleter(this);
        BoxPlugin.instance.getServer().getPluginManager().registerEvents(this, BoxPlugin.instance);
        this.load();
    }


    public int load() {
        for(BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        if(!BoxPlugin.instance.getDataFolder().exists()) {
            BoxPlugin.instance.getDataFolder().mkdir();
        }
        File schematicsFolder = new File(BoxPlugin.instance.getDataFolder(), "schematics");
        if(!schematicsFolder.exists()) {
            schematicsFolder.mkdir();
        }
        File config = new File(BoxPlugin.instance.getDataFolder(), "terrainregenerator.yml");
        if(!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = new YamlConfiguration();
        try {
            this.config.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity e : world.getEntities()) {
                    if(e.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "respawningmob"), PersistentDataType.INTEGER) && e.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "respawningmob"), PersistentDataType.INTEGER) == 1) {
                        e.remove();
                    }
                }
            }
        }, 20*60L);
        // schematics
        for(Object schem : this.config.getConfigurationSection("schematics").getKeys(false).toArray()) {
            int timerseconds = this.config.getInt("schematics." + schem + ".timer-seconds");
            File schemFile = new File(BoxPlugin.instance.getDataFolder(), "schematics/" + schem + ".schem");
            tasks.add(Bukkit.getScheduler()
                    .runTaskTimer(
                            BoxPlugin.instance,
                            () -> this.regenerateNew(schemFile),
                            timerseconds * 20,
                            timerseconds * 20
                    ));
            tasks.add(Bukkit.getScheduler()
                    .runTaskTimer(
                            BoxPlugin.instance,
                            () -> this.schematicPreSpawn((String) schem),
                            (timerseconds * 20) - (20*10),
                            timerseconds * 20
                    )
            );
            BoxPlugin.instance.getLogger().info("Started timer for schematic " + schem);
        }

        if(this.config.getConfigurationSection("randomizedschematics.groups") != null ) {
            for(Object group : this.config.getConfigurationSection("randomizedschematics.groups").getKeys(false).toArray()) {
                int timerseconds = this.config.getInt("randomizedschematics.groups." + group + ".timer-seconds");
                tasks.add(Bukkit.getScheduler()
                        .runTaskTimer(
                                BoxPlugin.instance,
                                () -> this.regenerateRandomly((String) group),
                                timerseconds * 20,
                                timerseconds * 20
                        ));
                BoxPlugin.instance.getLogger().info("Started timer for schematic group " + group);
            }
        }

        // entities
        for(Object entity : this.config.getConfigurationSection("entities").getKeys(false).toArray()) {
            int timerseconds = this.config.getInt("entities." + entity + ".timer-seconds");
            try {
                CompoundTag nbt = null;
                if(!this.config.isSet("entities." + entity + ".isMythic") || !this.config.getBoolean("entities." + entity + ".isMythic")) {
                    nbt = TagParser.parseTag(this.config.getString("entities." + entity + ".nbt"));
                }
                EntityType entityType = EntityType.fromName(this.config.getString("entities." + entity + ".type"));
                int x = this.config.getInt("entities." + entity + ".x");
                int y = this.config.getInt("entities." + entity + ".y");
                int z = this.config.getInt("entities." + entity + ".z");
//                System.out.println("Entity " + entity + " located at " + x + " " + y + " " + z);
                CompoundTag finalNbt = nbt;
                tasks.add(Bukkit.getScheduler()
                        .runTaskTimer(
                                BoxPlugin.instance,
                                () -> this.spawnEntity((String) entity, entityType, finalNbt, x, y, z),
                                timerseconds * 20,
                                timerseconds * 20
                        ));
                BoxPlugin.instance.getLogger().info("Started timer for entity " + entity + " with type " + entityType);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        tasks.add(Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world: Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if(entity instanceof Slime) {
                        Slime slime = (Slime) entity;
                        if(slime.getSize() == 1) {
                            slime.remove();
                        }
                    } else if(entity instanceof Drowned) {
                        entity.remove();
                    }
                }
            }
        }, 30 * 60 * 20, 30 * 60 * 20));
        tasks.add(Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(ArrayList<UUID> list : spawnedEntities.values()) {
                for(UUID uuid : list) {
                    if(uuid == null) continue;
                    Entity entity = Bukkit.getEntity(uuid);
                    if(entity instanceof Warden) {
                        Warden warden = (Warden) entity;
                        if(warden.getTarget() != null && warden.getTarget() instanceof Silverfish) {
                            warden.setAnger(warden.getTarget(), 0);
                        }
                    }
                }
            }
        }, 1, 1));
        tasks.add(Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world: Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if(entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING)) {
                        String name = entity.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING);
                        if(this.config.isSet("entities." + name + ".maxradius") && !entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ignoremaxradius"), PersistentDataType.INTEGER)) {
                            int x = this.config.getInt("entities." + name + ".x");
                            int y = this.config.getInt("entities." + name + ".y");
                            int z = this.config.getInt("entities." + name + ".z");
                            int radius = this.config.getInt("entities." + name + ".maxradius");
                            if(entity.getLocation().distance(new Location(Bukkit.getWorld(this.config.getString("entities." + name + ".world")), x, y, z)) > radius) {
                                entity.teleport(new Location(Bukkit.getWorld(this.config.getString("entities." + name + ".world")), x, y, z));
                                toDeAgro.add(entity);
                            }
                        }
                    }
                    if(entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING)) {
                        String name = entity.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING);
                        if(this.config.isSet("entities." + name + ".isBoss") && this.config.getBoolean("entities." + name + ".isBoss")) {
                            if(entity instanceof CraftCreature) {
                                List<Entity> nearby = entity.getNearbyEntities(35, 35, 35);
                                List<Player> nearbyPlayers = nearby.stream().filter(e -> {
                                    return e instanceof Player && (((Player)e).getGameMode() == GameMode.ADVENTURE || ((Player)e).getGameMode() == GameMode.SURVIVAL);
                                }).map(e -> (Player)e).collect(Collectors.toList());
                                if(nearbyPlayers.size() > 0) {
                                    Player nearest = nearbyPlayers.get(0);
                                    for(Player nearbyEntity : nearbyPlayers) {
                                        if(nearbyEntity.getLocation().distance(entity.getLocation()) < nearest.getLocation().distance(entity.getLocation())) {
                                            nearest = nearbyEntity;
                                        }
                                    }
                                    CraftCreature creature = (CraftCreature) entity;
                                    if(creature.getHandle() instanceof net.minecraft.world.entity.monster.warden.Warden) {
                                        net.minecraft.world.entity.monster.warden.Warden warden = (net.minecraft.world.entity.monster.warden.Warden) creature.getHandle();
                                        warden.setAttackTarget(((CraftPlayer) nearest).getHandle()); // setAttackTarget
                                    } else {
                                        creature.setTarget(nearest);
                                    }
                                }
                            }
                        }
//                        if(this.config.isSet("entities." + name + ".teleporting") && this.config.getBoolean("entities." + name + ".teleporting")) {
//                            if(entity instanceof CraftCreature) {
//                                List<Entity> nearby = entity.getNearbyEntities(5, 30, 5);
//                                List<Player> nearbyPlayers = nearby.stream().filter(e -> {
//                                    return e instanceof Player && (((Player)e).getGameMode() == GameMode.ADVENTURE || ((Player)e).getGameMode() == GameMode.SURVIVAL);
//                                }).map(e -> (Player)e).collect(Collectors.toList());
//                                if(nearbyPlayers.size() > 0) {
//                                    Player nearest = nearbyPlayers.get(0);
//                                    for(Player nearbyEntity : nearbyPlayers) {
//                                        if(nearbyEntity.getLocation().distance(entity.getLocation()) < nearest.getLocation().distance(entity.getLocation())) {
//                                            nearest = nearbyEntity;
//                                        }
//                                    }
//                                    CraftCreature creature = (CraftCreature) entity;
//                                    creature.teleport(nearest.getLocation());
//                                    nearest.getWorld().playSound(nearest.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
//                                }
//                            }
//                        }
                    }
                }
            }
        }, 280L, 280L));

        return this.config.getConfigurationSection("schematics").getKeys(false).toArray().length + this.config.getConfigurationSection("entities").getKeys(false).toArray().length;
    }

    private void spawnEntity(String name, EntityType entityType, CompoundTag nbt, int x, int y, int z) {
        if(spawnedEntities.containsKey(name) && this.config.getBoolean("entities." + name + ".kill-existing")) {
            for(UUID uuid : new ArrayList<>(spawnedEntities.get(name))) {
                if(Bukkit.getEntity(uuid) != null) {
                    if(this.config.isSet("entities." + name + ".isMythic") && this.config.getBoolean("entities." + name + ".isMythic")) {
                        // reduce memory usage hopefully
                        ActiveMob am = ((MythicBukkit)Bukkit.getPluginManager().getPlugin("MythicMobs")).getMobManager().getMythicMobInstance(Bukkit.getEntity(uuid));
                        ((MythicBukkit)Bukkit.getPluginManager().getPlugin("MythicMobs")).getMobManager().unregisterActiveMob(am);
                        am.setDespawned();
                    }
                    Bukkit.getEntity(uuid).remove();
                    spawnedEntities.get(name).remove(uuid);
                }
            }
        }
        Location loc = new Location(Bukkit.getWorld(this.config.getString("entities." + name + ".world")), x, y, z);
        for(int i = 0; i < this.config.getInt("entities." + name + ".count"); i++) {
            if(this.config.isSet("entities." + name + ".isMythic") && this.config.getBoolean("entities." + name + ".isMythic")) {
                int xp = this.config.isSet("entities." + name + ".xp") ? this.config.getInt("entities." + name + ".xp") : 0;
                UUID uuid = MythicMobsIntegration.spawnWithData(name, xp, loc);
                if(!spawnedEntities.containsKey(name) && this.config.getBoolean("entities." + name + ".kill-existing")) {
                    spawnedEntities.put(name, new ArrayList<>());
                }
                spawnedEntities.get(name).add(uuid);
            } else {
                CraftEntity e = (CraftEntity) Bukkit.getWorld(this.config.getString("entities." + name + ".world")).spawnEntity(loc, entityType);
                if(!spawnedEntities.containsKey(name) && this.config.getBoolean("entities." + name + ".kill-existing")) {
                    spawnedEntities.put(name, new ArrayList<>());
                }
                spawnedEntities.get(name).add(e.getUniqueId());
                if(this.config.getInt("entities." + name + ".xp") != 0) {
                    e.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, this.config.getInt("entities." + name + ".xp"));
                }
                e.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmob"), PersistentDataType.INTEGER, 1);
                e.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING, name);
                if(nbt != null) e.getHandle().load(nbt); // Entity#load()
                e.teleport(loc); // loading nbt resets loc to 0
            }
        }
        if(this.config.getString("entities." + name + ".respawn-broadcast") != null) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("entities." + name + ".respawn-broadcast")));
        }
    }

    private void schematicPreSpawn(String name) {
        if(this.config.getString("schematics." + name + ".respawn-pre-broadcast") != null) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("schematics." + name + ".respawn-pre-broadcast")));
        }
    }

    private void regenerateNew(File schematic) {
        if(schematic.exists()) {
            String filename = schematic.getName().replace(".schem", "");
            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematic);
            try(ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
                clipboard = reader.read();
                EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld(this.config.getString("schematics." + filename + ".world"))));
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(
                                this.config.getInt("schematics." + filename + ".x"),
                                this.config.getInt("schematics." + filename + ".y"),
                                this.config.getInt("schematics." + filename + ".z")
                        ))
                        .copyEntities(this.config.getBoolean("schematics." + filename + ".includeentities"))
                        .ignoreAirBlocks(this.config.getBoolean("schematics." + filename + ".ignoreairblocks"))
                        .build();
                Operations.complete(operation);
                editSession.flushSession();
                if(this.config.getString("schematics." + filename + ".respawn-broadcast") != null) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("schematics." + filename + ".respawn-broadcast")));
                }
                if(this.config.getString("schematics." + filename + ".command") != null) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.config.getString("schematics." + filename + ".command"));
                }
            } catch (IOException | WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            BoxPlugin.instance.getLogger().severe("Schematic: " + schematic + " was not found");
        }
    }

    private void regenerateRandomly(String group) {
        List<File> schematicFiles = new ArrayList<>(
                this.config.getStringList("randomizedschematics.groups." + group + ".schematics")
        ).stream().map(name -> new File(BoxPlugin.instance.getDataFolder(), "schematics/" + name + ".schem")).collect(Collectors.toList());
        List<Location> locations = new ArrayList<>(
                this.config.getStringList("randomizedschematics.groups." + group + ".locations")
        ).stream().map(coordsstring -> {
            List<Integer> vals = Arrays.stream(coordsstring.split(",")).map(val -> Integer.parseInt(val.trim())).collect(Collectors.toList());
            return new Location(Bukkit.getWorld(this.config.getString("randomizedschematics.groups." + group + ".world")), vals.get(0), vals.get(1), vals.get(2));
        }).collect(Collectors.toList());
        Collections.shuffle(locations, ThreadLocalRandom.current());
        for(int i = 0; i < locations.size(); i++) {
            File schematic = schematicFiles.get(i);
            Location loc = locations.get(i);
            if(schematic.exists()) {
                String filename = schematic.getName().replace(".schem", "");
                Clipboard clipboard;
                ClipboardFormat format = ClipboardFormats.findByFile(schematic);
                try(ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
                    clipboard = reader.read();
                    EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld(this.config.getString("randomizedschematics.groups." + group + ".world"))));
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(
                                    loc.getX(),
                                    loc.getY(),
                                    loc.getZ()
                            ))
                            .copyEntities(this.config.getBoolean("randomizedschematics.types." + filename + ".includeentities"))
                            .ignoreAirBlocks(this.config.getBoolean("randomizedschematics.types." + filename + ".ignoreairblocks"))
                            .build();
                    Operations.complete(operation);
                    editSession.flushSession();
                    if(this.config.getString("randomizedschematics.types." + filename + ".command") != null) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.config.getString("randomizedschematics.types." + filename + ".command"));
                    }
                } catch (IOException | WorldEditException e) {
                    e.printStackTrace();
                }
            } else {
                BoxPlugin.instance.getLogger().severe("Schematic: " + schematic + " was not found");
            }
        }
        if(this.config.getString("randomizedschematics.groups." + group + ".respawn-broadcast") != null) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("randomizedschematics.groups." + group + ".respawn-broadcast")));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if(spawnedEntities.containsValue(e.getEntity().getUniqueId())) {
            spawnedEntities.values().remove(e.getEntity().getUniqueId());
        }
    }

//    @EventHandler
//    public void onDamage(EntityDamageByEntityEvent e) {
//        Entity entity = e.getEntity();
//        if(entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING)) {
//            String name = entity.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING);
//            if(e.getEntity() instanceof CraftCreature && this.config.isSet("entities." + name + ".teleporting")) {
//                int rand = (int)(Math.random() * (10) + 1);
//                if(rand == 1) {
//                    Player p = null;
//                    if((e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player)) {
//                        p = (Player) ((Projectile)e.getDamager()).getShooter();
//                    } else if(e.getDamager() instanceof Player) {
//                        p = (Player) e.getDamager();
//                    }
//                    if(p != null) {
//                        CraftCreature creature = (CraftCreature) e.getEntity();
//                        creature.teleport(p.getLocation());
//                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
//                    }
//                }
//            }
//        }
//    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(label.equals("mmxp")) {
            // mmxp <name> <World>,<x>,<y>,<z>
            if(args.length < 2) {
                sender.sendMessage(ChatColor.RED + "/mmxp <name> <World>,<x>,<y>,<z>");
                return true;
            }
            if(!sender.hasPermission("terrainregenerator.mmxp") && !(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }
            String name = args[0];
            String[] loc = args[1].split(",");
            if(loc.length < 4) {
                sender.sendMessage(ChatColor.RED + "/mmxp <name> <World>,<x>,<y>,<z>");
                return true;
            }
            String world = loc[0];
            if(Bukkit.getWorld(world) == null) {
                sender.sendMessage(ChatColor.RED + "/mmxp <name> <World>,<x>,<y>,<z>");
                return true;
            }
            if(!Util.isInteger(loc[1]) || !Util.isInteger(loc[2]) || !Util.isInteger(loc[3])) {
                sender.sendMessage(ChatColor.RED + "/mmxp <name> <World>,<x>,<y>,<z>");
                return true;
            }
            int x = Integer.parseInt(loc[1]);
            int y = Integer.parseInt(loc[2]);
            int z = Integer.parseInt(loc[3]);
            MythicMobsIntegration.spawnWithData(name, this.config.getInt("entities." + name + ".xp"), new Location(Bukkit.getWorld(world), x, y, z), true);
        }
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(label.equals("terrainregenerator")) {
                if(args.length == 0 || !args[0].equals("reload")) {
                    p.sendMessage(ChatColor.GRAY + "Use /terrainregenerator reload to reload config files");
                } else {
                    int num = load();
                    p.sendMessage("Reloaded " + num + " schematics!");
                }
                return true;
            } else if(label.equals("regenschematic")) {
                if(!p.hasPermission("terrainregenerator.regenschematic")) {
                    p.sendMessage(ChatColor.RED + "You don't have permission!");
                    return true;
                }
                if(args.length == 0 || !this.config.contains("schematics." + args[0])) {
                    p.sendMessage(ChatColor.RED + "Invalid schematic name");
                    return true;
                }
                File schemFile = new File(BoxPlugin.instance.getDataFolder(), "schematics/" + args[0] + ".schem");
                p.sendMessage(ChatColor.AQUA + "Manually regenerating schematic...");
                this.regenerateNew(schemFile);
            }
        }
        return true;
    }

    @EventHandler
    public void onUnloadChunk(ChunkUnloadEvent e) {
        for(Entity entity : e.getChunk().getEntities()) {
            if(entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "respawningmobid"), PersistentDataType.STRING)) {
                e.getChunk().addPluginChunkTicket(BoxPlugin.instance);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if(e.getEntity() instanceof Warden && e.getTarget() instanceof Silverfish) {
            net.minecraft.world.entity.monster.warden.Warden nmsWarden = ((CraftWarden) e.getEntity()).getHandle();
            nmsWarden.setAttackTarget(null);
            nmsWarden.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            e.setTarget(null);
            e.setCancelled(true);
        }
        if(e.getEntity() instanceof Silverfish && e.getTarget() instanceof Warden) {
            net.minecraft.world.entity.monster.warden.Warden nmsWarden = ((CraftWarden) e.getTarget()).getHandle();
            nmsWarden.setAttackTarget(null);
            nmsWarden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            e.setTarget(null);
            e.setCancelled(true);
        }
        for(Entity entity : new ArrayList<>(toDeAgro)) {
            if(entity.getUniqueId().equals(e.getEntity().getUniqueId())) {
                e.setTarget(null);
                toDeAgro.remove(entity);
            }
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if(alias.equals("terrainregenerator")) {
            return List.of("reload");
        } else if(alias.equals("regenschematic")) {
            if(args.length == 1) {
                StringUtil.copyPartialMatches(args[0], this.config.getConfigurationSection("schematics").getKeys(false), completions);
                return completions;
            }
        }
        return List.of();
    }
}
