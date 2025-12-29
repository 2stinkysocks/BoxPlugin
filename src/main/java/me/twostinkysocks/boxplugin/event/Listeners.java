package me.twostinkysocks.boxplugin.event;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.ItemModification.RegisteredItem;
import me.twostinkysocks.boxplugin.customitems.items.impl.CageStaff;
import me.twostinkysocks.boxplugin.manager.GearScoreManager;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.manager.PerksManager.Perk;
import me.twostinkysocks.boxplugin.perks.MegaPerkHeartSteal;
import me.twostinkysocks.boxplugin.perks.PerkXPBoost;
import me.twostinkysocks.boxplugin.util.RenderUtil;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryPlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class Listeners implements Listener {

    private Set<UUID> running = new HashSet<>(); //used for megaperk strength
    record Pair(UUID player, UUID target) {}
    private Map<Pair, Integer> entityHits = new HashMap<>();
    private Map<Pair, Boolean> completedStacks = new HashMap<>();

    public RegisteredItem getRegisteredItem(){
        RegisteredItem RegisteredItem = new RegisteredItem();
        return RegisteredItem;
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent e) {
        entityHits.keySet().removeIf(key -> key.target.equals(e.getEntity().getUniqueId()));

        if(!(e.getEntity() instanceof Player) && e.getEntity().getKiller() != null) {
            if(BoxPlugin.instance.entityExperience.containsKey(e.getEntityType()) && !e.getEntity().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER)) {
                int before = BoxPlugin.instance.getXpManager().getXP(e.getEntity().getKiller());
                BoxPlugin.instance.getXpManager().addXP(e.getEntity().getKiller(), BoxPlugin.instance.entityExperience.get(e.getEntityType()));
                int after = BoxPlugin.instance.getXpManager().getXP(e.getEntity().getKiller());
                Bukkit.getPluginManager().callEvent(new PlayerBoxXpUpdateEvent(e.getEntity().getKiller(), before, after));
            }
        }
        if(!(e.getEntity() instanceof Player) && e.getEntity().getKiller() != null &&e.getEntity().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER)) {
            int before = BoxPlugin.instance.getXpManager().getXP(e.getEntity().getKiller());
            BoxPlugin.instance.getXpManager().addXP(e.getEntity().getKiller(), e.getEntity().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER));
            int after = BoxPlugin.instance.getXpManager().getXP(e.getEntity().getKiller());
            Bukkit.getPluginManager().callEvent(new PlayerBoxXpUpdateEvent(e.getEntity().getKiller(), before, after));
        }
        if(e.getEntityType() == EntityType.GUARDIAN) {
            e.getDrops().add(new ItemStack(Material.PRISMARINE_CRYSTALS));
        }
        if(!(e.getEntity() instanceof Player)) { // handled in playerdeathevent
            if(e.getEntity().getKiller() != null) {
                Player p = e.getEntity().getKiller();
                if(BoxPlugin.instance.getPerksManager().getSelectedPerks(p).contains(Perk.MAGNET)) {
                    HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(e.getDrops().toArray(new ItemStack[0]));
                    e.getDrops().clear();
                    for(ItemStack item : leftover.values()) {
                        Item drop = (Item) p.getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.ITEM);
                        drop.setItemStack(item);
                    }
                    p.giveExp(e.getDroppedExp());
                    e.setDroppedExp(0);
                }
            }
        }
    }

    @EventHandler
    public void explodeForCage(BlockExplodeEvent e) {
        for(HashSet<Location> locations : CageStaff.cageBlocks.values()) {
            for(Location loc : locations) {
                if(e.blockList().contains(loc)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if(e.getMessage().startsWith("/spawn")) {
            if(BoxPlugin.instance.getPvpManager().getStreak(e.getPlayer()) >= 20) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You can't /spawn with a high streak!");
            }
        }
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e){//update gear score on inventory update
        Player p = (Player) e.getPlayer();
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            GearScoreManager.UpdatePlayerGearscore(p);
            BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(p);
        }, 10);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e){//update gear score on item pickup
        if (!(e.getEntity() instanceof Player player)){
            return;
        }

        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            GearScoreManager.UpdatePlayerGearscore(player);
            BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(player);
        }, 10);
    }

    @EventHandler
    public void entityInteract(PlayerInteractEntityEvent e) {
        Entity interacted = e.getRightClicked();
        Player p = e.getPlayer();

//        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//        RegionQuery query = container.createQuery();
//        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(interacted.getLocation().getWorld())).getApplicableRegions(BlockVector3.at(interacted.getLocation().getX(),interacted.getLocation().getY(),interacted.getLocation().getZ()));
//        if(!set.testState(WorldGuardPlugin.inst().wrapPlayer(p), BoxPlugin.instance.getEntityInteractFlag())) {
//            e.setCancelled(true);
//            p.sendMessage(ChatColor.RED + "You don't meet the level requirement to access this!");
//        }

        if(interacted.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_npc"), PersistentDataType.INTEGER) && interacted.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_npc"), PersistentDataType.INTEGER) == 1) {
            e.setCancelled(true);
            BoxPlugin.instance.getPerksManager().openMainGui(p);
        }
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            GearScoreManager.UpdatePlayerGearscore(p);
            BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(p);
        }, 10);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "noplace"), PersistentDataType.INTEGER) && e.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "noplace"), PersistentDataType.INTEGER) == 1) {
            e.setCancelled(true);
            return;
        }
        if(e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "nointeract"), PersistentDataType.INTEGER) && e.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "nointeract"), PersistentDataType.INTEGER) == 1) {
            e.setCancelled(true);
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "eggcommand"), PersistentDataType.STRING)) {
            Location toSpawn = e.getClickedBlock().getLocation().add(0,1,0);
            String command = e.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "eggcommand"), PersistentDataType.STRING);
            command = command.replaceAll("%x%", String.valueOf(toSpawn.getBlockX())).replaceAll("%y%", String.valueOf(toSpawn.getBlockY())).replaceAll("%z%", String.valueOf(toSpawn.getBlockZ())).replaceAll("%world%", toSpawn.getWorld().getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            e.setCancelled(true);
            ItemStack toRemove = e.getItem().clone();
            toRemove.setAmount(1);
            p.getInventory().removeItem(toRemove);
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent e) {
        NamespacedKey readyForHpKey = new NamespacedKey(BoxPlugin.instance, "has_been_tagged");
        if(e.getDamager() instanceof Player && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            if(BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_LIFESTEAL)) {
                if(p.getLocation().distance(e.getEntity().getLocation()) < 6) {
                    Util.debug(p, "Healed for " + (e.getFinalDamage() * 0.15) + " from lifesteal");
                    p.setHealth(Math.min(p.getHealth() + (e.getFinalDamage() * 0.15), p.getAttribute(Attribute.MAX_HEALTH).getValue()));

                }
            }

            if(BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_HEARTSTEEL)) {
                if(!(e.getEntity() instanceof LivingEntity)){
                    return;
                }
                LivingEntity target = (LivingEntity) e.getEntity();
                if(target instanceof Player || target.getMaxHealth() >= 100){
                    Pair key = new Pair(p.getUniqueId(), target.getUniqueId());
                    if(entityHits.containsKey(key)){
                        int oldHits = entityHits.get(key);
                        entityHits.put(key, oldHits + 1);
                    } else {
                        entityHits.put(key, 1);
                    }
                    if(completedStacks.getOrDefault(key, false)){ //cancel if already claimed
                        Util.debug(p, target.getName() + " Has had HP claimed already");
                        return;
                    }
                    if(target.getPersistentDataContainer().has(readyForHpKey)){ //cancel if already tagged
                        if(target.getPersistentDataContainer().get(readyForHpKey, PersistentDataType.BOOLEAN)){
                            return;
                        }
                    }
                    target.getPersistentDataContainer().set(readyForHpKey, PersistentDataType.BOOLEAN, true);

                    Particle.DustOptions finalDust = new Particle.DustOptions(Color.fromRGB(200, 16, 66), 1.0F);
                    Particle.DustOptions halfwayDust = new Particle.DustOptions(Color.fromRGB(147, 103, 214), 0.75F);
                    Particle.DustOptions startDust = new Particle.DustOptions(Color.fromRGB(148, 197, 227), 0.5F);

                    int[] i = {0};//num seconds until ready
                    boolean[] claimedHealth = {false};//if health has been claimed
                    int[] hitsAtReady = {0};
                    double entityHieght = target.getHeight();

                    Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
                        if(task.isCancelled()) return; // just in case

                        Location origin = target.getLocation().clone();
                        origin.add(0, (entityHieght + 1), 0);

                        if(i[0] == 6){
                            Util.debug(p, "Started heart steal stack timer on " + target.getName());
                            p.playSound(p.getLocation(), Sound.UI_HUD_BUBBLE_POP, 4f, 0.8f);
                        } else if (i[0] == 16) {
                            p.playSound(p.getLocation(), Sound.UI_HUD_BUBBLE_POP, 4f, 1f);
                        } else if (i[0] == 26) {
                            p.playSound(p.getLocation(), Sound.UI_HUD_BUBBLE_POP, 4f, 1.4f);
                            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 2f);
                            Util.debug(p, "Stack is ready to be claimed from " + target.getName());
                            hitsAtReady[0] = entityHits.getOrDefault(key, 0);
                        }
                        if(i[0] <= 16 && i[0] >= 6){
                            RenderUtil.renderDustOrb(origin, 5, 0.05, startDust);
                        } else if(i[0] <= 26 && i[0] > 16){
                            RenderUtil.renderDustOrb(origin, 8, 0.075, halfwayDust);
                        } else if(i[0] > 26){
                            RenderUtil.renderDustOrb(origin, 10, 0.1, finalDust);
                        }
                        if(entityHits.getOrDefault(key, 0) > hitsAtReady[0] && i[0] >= 26){
                            completedStacks.put(key, true);
                            claimedHealth[0] = true;
                            p.sendMessage(ChatColor.GREEN + "Claimed 1 stack from " + target.getName());
                            int currentStacks = BoxPlugin.instance.getMegaPerkHeartSteal().getStacks(p);
                            BoxPlugin.instance.getMegaPerkHeartSteal().setStacks(p, currentStacks + 1);
                            int bonusHP = BoxPlugin.instance.getMegaPerkHeartSteal().stacksToHealth(p);
                            BoxPlugin.instance.getMegaPerkHeartSteal().updateHealth(p);
                            target.damage(currentStacks*1.5);
                            Util.debug(p, "you now have: " + BoxPlugin.instance.getMegaPerkHeartSteal().getStacks(p) + " stacks, granting you: " + bonusHP + " health");
                            Util.debug(p, "dealt " + (currentStacks*1.5) + " damage to " + target.getName());

                            //sfx
                            p.playSound(p.getLocation(), Sound.UI_HUD_BUBBLE_POP, 1f, 0.8f);
                            p.playSound(p.getLocation(), Sound.BLOCK_BONE_BLOCK_BREAK, 0.9f, 1.8f);
                            p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 0.8f);
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.1f, 1.6f);
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.1f, 0.6f);
                            p.playSound(p.getLocation(), Sound.BLOCK_MEDIUM_AMETHYST_BUD_PLACE, 0.55f, 0.5f);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1f, 0.5f);
                            p.playSound(p.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 0.35f, 2f);
                            p.playSound(p.getLocation(), Sound.BLOCK_CREAKING_HEART_BREAK, 0.5f, 1.5f);

                            RenderUtil.renderParticleOrb(origin, 8, 0.08, Particle.SCULK_SOUL, 0.02);
                            target.getPersistentDataContainer().set(readyForHpKey, PersistentDataType.BOOLEAN, false);
                            task.cancel();
                        }
                        if(target.isDead() || claimedHealth[0] || i[0] == 200){//cancel if dead, claimed, or 20 seconds since activated
                            target.getPersistentDataContainer().set(readyForHpKey, PersistentDataType.BOOLEAN, false);
                            task.cancel();
                        }

                        i[0]++;
                    }, 0, 2);
                }
            }
        }
        if(e.getDamager() instanceof WitherSkull && !(e.getEntity() instanceof HumanEntity) && !(e.getEntity() instanceof Mob)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void stopDrown(EntityDamageEvent e) {
        if(e.getEntityType() == EntityType.WARDEN && e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void hangingDamage(HangingBreakByEntityEvent e) {
        if((e.getRemover() instanceof WitherSkull || e.getRemover() instanceof Boat)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void hangingDamage(HangingBreakEvent e) {
        if(e.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallBlock(EntityChangeBlockEvent e) {
        if(e.getEntity() instanceof FallingBlock) {
            FallingBlock fb = (FallingBlock) e.getEntity();
            if(fb.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "LAVA"), PersistentDataType.STRING)) {
                Player p = Bukkit.getPlayer(UUID.fromString(fb.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "LAVA"), PersistentDataType.STRING)));
                BlockPlaceEvent bpe = new BlockPlaceEvent(
                        e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()),
                        e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()).getState(),
                        e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()),
                        new ItemStack(Material.MAGMA_BLOCK),
                        p,
                        true,
                        EquipmentSlot.HAND
                );
                Bukkit.getPluginManager().callEvent(bpe);
                if(!bpe.isCancelled()) {
                    e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()).setType(Material.LAVA);
                }
                e.setCancelled(true);
                fb.remove();

            }
        }
    }

    @EventHandler
    public void removeLotteryOnJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(p.getPersistentDataContainer().has(BoxPlugin.instance.getLotteryManager().inLotteryKey, PersistentDataType.INTEGER) && !BoxPlugin.instance.getLotteryManager().hasTickets(p)) {
            p.getPersistentDataContainer().remove(BoxPlugin.instance.getLotteryManager().inLotteryKey);
        }
    }

    @EventHandler
    public void onBlockBreakForCage(BlockBreakEvent e) {
        for(HashSet<Location> locations : CageStaff.cageBlocks.values()) {
            if(locations.contains(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplodeForCage(BlockExplodeEvent e) {
        for(HashSet<Location> locations : CageStaff.cageBlocks.values()) {
            for(Block block : e.blockList()) {
                if(locations.contains(block.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void itemSpawn(EntityDropItemEvent e) {
        if(e.getEntity().getType() == EntityType.FALLING_BLOCK && e.getEntity().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "LAVA"), PersistentDataType.STRING)) {
            Player p = Bukkit.getPlayer(UUID.fromString(e.getEntity().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "LAVA"), PersistentDataType.STRING)));
            BlockPlaceEvent bpe = new BlockPlaceEvent(
                    e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()),
                    e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()).getState(),
                    e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()),
                    new ItemStack(Material.MAGMA_BLOCK),
                    p,
                    true,
                    EquipmentSlot.HAND
            );
            Bukkit.getPluginManager().callEvent(bpe);
            if(!bpe.isCancelled()) {
                e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation()).setType(Material.LAVA);
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws SQLException {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "PREVIOUS_HEALTH"), PersistentDataType.DOUBLE)) {
                try{
                    p.setHealth(p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "PREVIOUS_HEALTH"), PersistentDataType.DOUBLE));
                } catch(IllegalArgumentException ex) {}
            }
        }, 5L);
        if(!p.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "cgive TUTORIAL " + p.getName());
            }, 20L);
        }
        for(Perk perk : BoxPlugin.instance.getPerksManager().getSelectedPerks(p)) {
            if(perk != null) {
                perk.instance.onEquip(p);
            }
        }
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.DOUBLE)) {
            p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "xp"));
        }
        if(!p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER)) {
            p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, 0);
        }

        //set gearscore and items
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            try {
                if(getRegisteredItem().UpdateLegacyItems(p)){
                    p.sendMessage(ChatColor.GOLD + "Old items were found in your inventory and updated automatically!");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                getRegisteredItem().UpdateCurrentItems(p);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            GearScoreManager.UpdatePlayerGearscore(p);
        }, 20);

        // legacy xp

        if(BoxPlugin.instance.getXpManager().getXP(p) == 0) { // add flag by default if new player
            p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "legacylevelscompensated"), PersistentDataType.INTEGER, 1);
        }
        if(BoxPlugin.instance.getXpManager().getXP(p) > 0 && !p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "legacylevelscompensated"), PersistentDataType.INTEGER)) {
            p.sendTitle(ChatColor.RED + "Make sure to read chat!", null, 10, 100, 10);
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Unrewarded Levels!");
                p.sendMessage(ChatColor.RED + "Leveling up now rewards coins over time, which you haven't claimed!");
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You cannot gain xp until you claim your levelup rewards with " + ChatColor.GREEN + ChatColor.BOLD + "/claimlegacyrewards");
                p.sendMessage(ChatColor.RED + "Make sure to clear your inventory before claiming rewards!");
            },20);
        }

        //


//        BoxPlugin.instance.getLeaderboardManager().updateLeaderboard(p);
        BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(p);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        BoxPlugin.instance.getOfflineXPFile().set(e.getPlayer().getUniqueId().toString(), BoxPlugin.instance.getXpManager().getXP(e.getPlayer()));
        try {
            BoxPlugin.instance.getOfflineXPFile().save(new File(BoxPlugin.instance.getDataFolder(), "offlinexp.yml"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        BoxPlugin.instance.getScoreboardManager().getQueuedUpdates().remove(e.getPlayer());
        e.getPlayer().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "PREVIOUS_HEALTH"), PersistentDataType.DOUBLE, e.getPlayer().getHealth());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(!BoxPlugin.instance.placedBlocks.contains(e.getBlock().getLocation())) {
            BoxPlugin.instance.placedBlocks.add(e.getBlock().getLocation());
        }
    }
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        BoxPlugin.instance.placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        ItemStack item = event.getCursor();
        ItemStack itemselected = event.getCurrentItem();
        ItemStack hotkeyItem = event.getClick() == ClickType.NUMBER_KEY ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem();
        ItemStack offHandItem = event.getClick() == ClickType.SWAP_OFFHAND ? event.getWhoClicked().getInventory().getItem(45) : event.getCurrentItem();
//        Bukkit.broadcastMessage("CLICK EVENT:");
//        Bukkit.broadcastMessage("Slot " + event.getRawSlot());
//        Bukkit.broadcastMessage("Bottom Size " + player.getInventory().getSize());
//        Bukkit.broadcastMessage("Top Size " + player.getOpenInventory().getTopInventory().getSize());
//        Bukkit.broadcastMessage("Cursor item " + (item == null ? null : item.getType()));
//        Bukkit.broadcastMessage("Current item " + (itemselected == null ? null : itemselected.getType()));
//        Bukkit.broadcastMessage("Clicked inv " + event.getClickedInventory());
//        Bukkit.broadcastMessage("Top inv " + player.getOpenInventory().getTopInventory());
//        Bukkit.broadcastMessage("Action " + event.getAction() + "\n");


        // TODO: fix hotkey
        boolean shouldCancel = false;


        // shift click into another inventory
        if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && !(player.getOpenInventory().getTopInventory() instanceof CraftInventoryPlayer)) {
            shouldCancel = true;
        }

        // hotkey from hotbar into inventory
        else if((event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) && !(player.getOpenInventory().getTopInventory() instanceof CraftInventoryCrafting)) {
            shouldCancel = true;
        }

        // click item into inventory
        else if((event.getAction() == InventoryAction.SWAP_WITH_CURSOR || event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME) && !(event.getClickedInventory() instanceof CraftInventoryPlayer)) {
            shouldCancel = true;
        }

        if(event.getClick() == ClickType.SWAP_OFFHAND && !(player.getOpenInventory().getTopInventory() instanceof CraftInventoryCrafting)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't swap hands while in an inventory!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
            return;
        }


        if(shouldCancel) {
            if(Util.isSoulbound(item) || Util.isSoulbound(itemselected) || Util.isSoulbound(hotkeyItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't remove soulbound items from your inventory!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
            }
        }
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            GearScoreManager.UpdatePlayerGearscore(player);
            BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(player);
        }, 10);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        ItemStack dragged = event.getOldCursor(); // This is the item that is being dragged

        if (Util.isSoulbound(dragged)) {
            int inventorySize = event.getInventory().getSize(); // The size of the inventory, for reference

            // Now we go through all of the slots and check if the slot is inside our inventory (using the inventory size as reference)
            for (int i : event.getRawSlots()) {
                if (i < inventorySize) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {//let it register item in inventory first
            GearScoreManager.UpdatePlayerGearscore(player);
            BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(player);
        }, 10);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(!e.isCancelled()) {
            if(BoxPlugin.instance.blockExperience.containsKey(e.getBlock().getType()) && !BoxPlugin.instance.placedBlocks.contains(e.getBlock().getLocation())) {
                int newxp = BoxPlugin.instance.blockExperience.get(e.getBlock().getType());
                int existingxp = p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER);
                p.getPersistentDataContainer()
                        .set(
                                new NamespacedKey(BoxPlugin.instance, "xp"),
                                PersistentDataType.INTEGER,
                                existingxp + newxp
                        );
                Bukkit.getPluginManager().callEvent(new PlayerBoxXpUpdateEvent(p, existingxp, existingxp + newxp));
            }
            BoxPlugin.instance.placedBlocks.remove(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void blockDropItem(BlockDropItemEvent e) {
        Player p = e.getPlayer();
        if(BoxPlugin.instance.getPerksManager().getSelectedPerks(p).contains(Perk.MAGNET)) {
            HashMap<Integer, ItemStack> leftover = p.getInventory().addItem((e.getItems().stream().map(i -> i.getItemStack()).collect(Collectors.toList())).toArray(new ItemStack[0]));
            e.getItems().clear();
            for(ItemStack item : leftover.values()) {
                Item drop = (Item) p.getWorld().spawnEntity(e.getBlock().getLocation(), EntityType.ITEM);
                drop.setItemStack(item);
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        if(Util.isSoulbound(item)) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You can't drop soulbound items!");
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
        }
        GearScoreManager.UpdatePlayerGearscore(p);
        BoxPlugin.instance.getScoreboardManager().updatePlayerScoreboard(p);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                for(Perk perk : BoxPlugin.instance.getPerksManager().getSelectedPerks(p)) {
                    if(perk != null) perk.instance.onRespawn(e);
                }
                for(PerksManager.MegaPerk perk : BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p)) {
                    if(perk != null) perk.instance.onRespawn(e);
                }
            }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // more than 10 below - 0xp
        // up to 10 below - 10xp
        // your level or higher - 100xp
        Player cause = e.getEntity().getKiller();
        Player target = e.getEntity();

        if(target != null && BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(target).contains(PerksManager.MegaPerk.MEGA_HEARTSTEEL)){//if you die, reset claimable for all entities
            if(completedStacks != null){
                completedStacks.keySet().removeIf(key -> key.player.equals(target.getUniqueId()));
            }
        }
        if(cause != null && BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(cause).contains(PerksManager.MegaPerk.MEGA_HEARTSTEEL)){//reset claimable to killer when target dies
            if(completedStacks != null){
                completedStacks.keySet().removeIf(key -> key.player.equals(cause.getUniqueId()) && key.target.equals(target.getUniqueId()));
            }
        }

        BoxPlugin.instance.getGhostTokenManager().onPreDeath(e.getDrops(), e.getEntity());

        for(Perk perk : BoxPlugin.instance.getPerksManager().getSelectedPerks(target)) {
            if(perk != null) perk.instance.onDeath(e);
        }
        for(PerksManager.MegaPerk perk : BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(target)) {
            if(perk != null) perk.instance.onDeath(e);
        }

        if(cause == null) {
            BoxPlugin.instance.getPvpManager().resetStreak(target);
            BoxPlugin.instance.getScoreboardManager().queueUpdate(target);
            Util.dropPercent(e, 0.10);
            target.sendMessage(ChatColor.RED + "You lost 10% of your items from dying to a non-player!");
            BoxPlugin.instance.getGhostTokenManager().onPostDeath(e.getDrops(), e.getEntity());
            return;
        }

        // prevent spam killing
        HashMap<UUID, Integer> kills = BoxPlugin.instance.getKillsInHour().get(cause.getUniqueId());
        // if the player has already killed at least someone and has killed the specific player
        if(kills != null && kills.containsKey(target.getUniqueId())) {
            // then add one to that person
            kills.put(target.getUniqueId(), kills.get(target.getUniqueId())+1);
        } else {
            // if the player has not killed anyone
            if(kills == null) {
                // create new data
                HashMap<UUID, Integer> inner = new HashMap<>();
                inner.put(target.getUniqueId(), 1);
                BoxPlugin.instance.getKillsInHour().put(cause.getUniqueId(), inner);
                // if the player has killed people, but has not killed this person
            } else if(kills != null && !kills.containsKey(target.getUniqueId())) {
                // set the kilsl for that person to 1
                kills.put(target.getUniqueId(), 1);
            }
        }
        kills = BoxPlugin.instance.getKillsInHour().get(cause.getUniqueId());
        BoxPlugin.instance.getKillsInHour().put(cause.getUniqueId(), kills);

        if(BoxPlugin.instance.getKillsInHour().get(cause.getUniqueId()).get(target.getUniqueId()) > 10) {
            e.setKeepInventory(true);
            e.getDrops().clear();
            cause.sendMessage(ChatColor.RED + "You cannot kill the same player more than 10 times per 2 hours!");
            target.sendMessage(ChatColor.RED + "You were killed by the same player more than 10 times in 2 hours, so you lost no items.");
            return;
        }

        int causelevel = BoxPlugin.instance.getXpManager().getLevel(cause);
        int causexp = BoxPlugin.instance.getXpManager().getXP(cause);
        int targetlevel = BoxPlugin.instance.getXpManager().getLevel(target);

        double dropChanceConst1 = 0.8; //raise
        double dropChanceConst2 = 1.6;

        double xpdiff = ((double) BoxPlugin.instance.getXpManager().getXP(cause)) / BoxPlugin.instance.getXpManager().getXP(target);
        double gearScoreDiff = ((double) GearScoreManager.GetPlayerGearscore(cause) / GearScoreManager.GetPlayerGearscore(target));

        double dropChanceFromXp = 205.0 - (100.0*xpdiff);
        double dropChanceFromGearScore = 205.0 - (100.0*gearScoreDiff);

        if(dropChanceFromGearScore < 0){
            dropChanceFromGearScore = 0;
        }

        double dropChance = (dropChanceConst1)*(Math.pow(dropChanceFromGearScore, dropChanceConst2)) + ((1 - dropChanceConst1)*(dropChanceFromXp)); //big fucky formula that works in favoring gearscore
        double percentChance = Math.max(Math.min(1.0, dropChance / 100.0), 0.05);

        Util.dropPercent(e, percentChance);
        target.sendMessage(ChatColor.RED + "You lost " + (int)(100*percentChance) + "% of your items due to the level and gear score difference between you and the other player!");
        // skulls
        if(xpdiff <= 2) {
            e.getDrops().add(new ItemStack(Material.SKELETON_SKULL, BoxPlugin.instance.getPvpManager().getBounty(target)));
            cause.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lSkulls Claimed! &7You claimed " + BoxPlugin.instance.getPvpManager().getBounty(target) + " skulls from " + target.getName()));
        } else {
            cause.sendMessage(ChatColor.RED + "You earned no skulls due to having more than double your opponent's xp.");
        }
        int xptoadd = Math.min((int)Math.min(50000,causexp*0.1), Math.max(0, BoxPlugin.instance.getXpManager().getLevel(target) * 100));
        BoxPlugin.instance.getXpManager().addXP(cause, xptoadd);
        Bukkit.getPluginManager().callEvent(new PlayerBoxXpUpdateEvent(cause, causexp, causexp + xptoadd));

        BoxPlugin.instance.getGhostTokenManager().onPostDeath(e.getDrops(), e.getEntity());

        if(e.getEntity().getKiller() != null) {
            Player p = e.getEntity().getKiller();
            if(BoxPlugin.instance.getPerksManager().getSelectedPerks(p).contains(Perk.MAGNET)) {
                HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(e.getDrops().toArray(new ItemStack[0]));
                e.getDrops().clear();
                for(ItemStack item : leftover.values()) {
                    Item drop = (Item) p.getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.ITEM);
                    drop.setItemStack(item);
                }
                p.giveExp(e.getDroppedExp());
                e.setDroppedExp(0);
            }
        }
        BoxPlugin.instance.getPvpManager().registerKill(cause, target); // resets streak here
        BoxPlugin.instance.getScoreboardManager().queueUpdate(cause);
        BoxPlugin.instance.getScoreboardManager().queueUpdate(target);


    }


    @EventHandler
    public void onItemBreak(EntityDamageEvent e) {
        if(e.getEntity() instanceof Item) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUpdateXp(PlayerBoxXpUpdateEvent e) {
        Player p = e.getPlayer();
        double multiplier = ((PerkXPBoost)Perk.XPBOOST.instance).calculateXPMultiplier(p).doubleValue();
        int beforelevel = BoxPlugin.instance.getXpManager().convertXPToLevel(e.getBeforeXP());
        int difference = e.getAfterXP() - e.getBeforeXP();
        if(difference > 0 && !e.isMultiplierBypassed() && BoxPlugin.instance.getPerksManager().getSelectedPerks(p).contains(Perk.XPBOOST)) { // xp gain
            int toAdd = (int) (difference * multiplier) - difference;
            BoxPlugin.instance.getXpManager().addXP(p, toAdd);
        }
        int afterlevel = BoxPlugin.instance.getXpManager().convertXPToLevel(BoxPlugin.instance.getXpManager().getXP(p));

        // legacy xp

        if(!p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "legacylevelscompensated"), PersistentDataType.INTEGER)) {
            p.sendTitle(ChatColor.RED + "Make sure to read chat!", null, 10, 100, 10);
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Unrewarded Levels!");
            p.sendMessage(ChatColor.RED + "Leveling up now rewards coins over time, which you haven't claimed!");
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You cannot gain xp until you claim your levelup rewards with " + ChatColor.GREEN + ChatColor.BOLD + "/claimlegacyrewards");
            p.sendMessage(ChatColor.RED + "Make sure to clear your inventory before claiming rewards!");
            BoxPlugin.instance.getXpManager().setXP(p, e.getBeforeXP());
            return;
        }

        if((afterlevel/5) > (beforelevel/5) || (afterlevel/6) > (beforelevel/5)) {
            int toGive = BoxPlugin.instance.getXpManager().getLevelUpRewardLevelToLevel(BoxPlugin.instance.getXpManager().convertXPToLevel(e.getBeforeXP()), BoxPlugin.instance.getXpManager().getLevel(p));
            HashMap<Integer, ItemStack> toDrop = p.getInventory().addItem(Util.itemArray(toGive, Util::gigaCoin));
            toDrop.forEach((index, item) -> {
                Item entity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                entity.setItemStack(item);
            });
            p.sendMessage(ChatColor.GOLD + "Earned " + ChatColor.BOLD + toGive + " Giga Coins " + ChatColor.GOLD + "from leveling up!");
        }

        //
        BoxPlugin.instance.getXpManager().handleGroupUpdate(p, beforelevel, afterlevel);
        if(beforelevel < afterlevel) {
            p.resetTitle();
            p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "LEVEL UP!", ChatColor.GRAY + "" + beforelevel + " → " + afterlevel, 10, 40, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f);
        }
        // remove second perk
        if(afterlevel < 50 && beforelevel >= 50) {
            List<Perk> selected = BoxPlugin.instance.getPerksManager().getSelectedPerks(p);
            if(selected.size() >= 1) {
                BoxPlugin.instance.getPerksManager().setSelectedPerks(p, List.of(selected.get(0)));
            }
        }

        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 2f);
        if(e.getAfterXP() < 0) {
            p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, Math.abs(e.getAfterXP()));
        }
        BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
    }

    @EventHandler
    public void onSplit(SlimeSplitEvent e) {
        if(e.getEntity() instanceof MagmaCube) {
            if(e.getEntity().getSize() == 2) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCombatStart(PlayerTagEvent e) {
        Player p = e.getPlayer();
        if(!BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_STRENGTH)){
            return;
        }
        if (running.contains(p.getUniqueId())) {
            return;
        }
        running.add(p.getUniqueId());
        final long[] lastInCombat = { System.currentTimeMillis() };
        final long start = System.currentTimeMillis();
        ICombatLogX combatLogX = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");

        ICombatManager combatManager = combatLogX.getCombatManager();

        final BukkitTask[] task = new BukkitTask[1];

        task[0] = Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {

            if(combatManager.isInCombat(p)) {
                lastInCombat[0] = System.currentTimeMillis();
            }

            long elapsedTime = (System.currentTimeMillis() - start) / 1000;
            long outOfCombatTime = (System.currentTimeMillis() - lastInCombat[0]) / 1000;

            if(outOfCombatTime == 0){
                p.sendMessage(ChatColor.RED + "Your strength will fade within 30 seconds of no combat!");
            }

            if(outOfCombatTime >= 30){
                p.sendMessage(ChatColor.RED + "Your strength has faded back to normal");
                p.removePotionEffect(PotionEffectType.STRENGTH);
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, true, false));
                running.remove(p.getUniqueId());
                task[0].cancel();
                return;
            }

            if (elapsedTime == 30) {
                p.sendMessage(ChatColor.GOLD + "Your strength has increased...");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 4, true, false));
            }
            if (elapsedTime == 60) {
                p.sendMessage(ChatColor.GOLD + "Your strength has increased...");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 5, true, false));
            }
            if (elapsedTime == 90) {
                p.sendMessage(ChatColor.GOLD + "Your strength has increased...");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 6, true, false));
            }
            if (elapsedTime == 120) {
                p.sendMessage(ChatColor.GOLD + "Your strength has increased...");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 7, true, false));
            }
            if (elapsedTime == 150) {
                p.sendMessage(ChatColor.GOLD + "Your strength has increased...");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 8, true, false));
            }
            if (elapsedTime == 180) {
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your strength has reached max power!" + ChatColor.RED + " +30 attack damage!");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 9, true, false));
            }
        }, 20L, 20L);

    }


}
