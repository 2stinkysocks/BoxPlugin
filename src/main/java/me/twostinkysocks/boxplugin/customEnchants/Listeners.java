package me.twostinkysocks.boxplugin.customEnchants;

import io.lumine.mythic.bukkit.utils.events.extra.ArmorEquipEvent;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_21_R3.attribute.CraftAttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.swing.*;
import java.util.*;

public class Listeners implements Listener {
    private final Set<UUID> prickleProcessing = new HashSet<>();
    private final Set<UUID> brambleProcessing = new HashSet<>();
    private final Set<UUID> magmaProcessing = new HashSet<>();
    private final Set<UUID> freezeProcessing = new HashSet<>();
    private final Set<UUID> drownProcessing = new HashSet<>();
    private final Set<UUID> lightningProcessing = new HashSet<>();
    private final Set<UUID> voidProcessing = new HashSet<>();

    public void calcDamage(Player target, DamageType damageType, double ammount, Player attacker){
        int elementMult = 0;
        int resistMult = 0;
        double finalDamage;
        if(damageType == DamageType.CACTUS){//calculate weaknesses to damage type
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
        }
        if (damageType == DamageType.LAVA) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.IceBorn, target);
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.Overgrowth, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
        }
        if (damageType == DamageType.FREEZE) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.VoidBorn, target);
        }
        if (damageType == DamageType.DROWN) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.FireBorn, target);
        }
        if (damageType == DamageType.MAGIC) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
        }
        if (damageType == DamageType.OUT_OF_WORLD) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.GodBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
        }
        if(elementMult >= 2){
            finalDamage = (ammount * elementMult/2.0) * 1.1;
        } else if (elementMult > 4) {
            finalDamage = (ammount * Math.min(3.2, elementMult/2.0)); //max damage is 3.2 x base
        } else {
            finalDamage = ammount;
        }
        if(resistMult > 0){
            finalDamage = finalDamage * Math.max(0.4, (1.0) - 0.1 * elementMult);//max 60% damage reduction
        }
        if(target.isBlocking()){
            return;
        }
        target.damage(finalDamage, DamageSource.builder(damageType).withCausingEntity(attacker).withDirectEntity(attacker).build());
        Util.debug(attacker, "dealt " + (finalDamage) + " bonus damage to " + target.getName());
    }

    public void updateSpeed(Player p){
        p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.WaterBorn)){
            ItemStack boots = p.getInventory().getBoots();
            if(boots != null && boots.hasItemMeta()){
                double depthStiderLvl = 0;
                if(boots.containsEnchantment(Enchantment.DEPTH_STRIDER)){
                    depthStiderLvl = boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
                }
                if(depthStiderLvl > 0){
                    int totalWaterBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.WaterBorn);
                    double speedToAdd = ((depthStiderLvl/100.0) * ((BoxPlugin.instance.getWaterBornEnchant().getStackingSpeedFromTotalLevel(totalWaterBornLvl) / 3.0) + 1) / 5.0);
                    p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1 + speedToAdd);
                    Util.debug(p, "increasing speed by " + String.format("%.2f",speedToAdd * 1000) + "%");
                }
            }
        }
    }
    @EventHandler
    public void entityDamageLeaf(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the cactus gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Prickle.instance.hasEnchant(mainHandItem)){//prickle enchant on weapon
                if(prickleProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                prickleProcessing.add(target.getUniqueId());
                int cacterLvl = CustomEnchantsMain.Enchant.Prickle.instance.getLevel(mainHandItem);
                int cacterDmg = BoxPlugin.instance.getEnchantPrickle().getDamageFromTotalLevel(cacterLvl);
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.CACTUS, cacterDmg, p);
                } else {
                    target.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + target.getName());
                }
                prickleProcessing.remove(target.getUniqueId());
            }
        }
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            int totalOvergrowthLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Overgrowth);
            int totalBrambleLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Bramble);

            Random random = new Random();

            if(totalOvergrowthLvl > 0){//overgrowth logic
                int overGrowChanceRolled = random.nextInt(100) + 1;
                int overGrowChance = BoxPlugin.instance.getEnchantOvergrowth().getChanceFromTotalLevel(totalOvergrowthLvl);
                double hpDiff = (e.getFinalDamage());
                if(hpDiff >= p.getHealth()){//cancel if this hit kills you
                    return;
                }
                if(overGrowChanceRolled <= overGrowChance){
                    double ammountToHeal = (hpDiff * BoxPlugin.instance.getEnchantOvergrowth().getHealFromTotalLevel(totalOvergrowthLvl));
                    Util.debug(p, "took " + hpDiff + " dammage, starting heal for: " + ammountToHeal);
                    int i[] = {0};
                    if(overGrowChanceRolled <= overGrowChance){// heals for a percent ofer 5 seconds
                        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
                            if(task.isCancelled()) return; // just in case
                            if(i[0] >= 5){
                                task.cancel();
                            }
                            if((p.getHealth() + (ammountToHeal/5)) < p.getMaxHealth()){
                                p.setHealth(p.getHealth() + (ammountToHeal/5));
                                Util.debug(p, "healed " + ((ammountToHeal/5) + " hp back"));
                            } else {
                                p.setHealth(p.getMaxHealth());
                            }
                            i[0]++;
                        }, 0, 20);
                    }
                }
            }
            if(totalBrambleLvl > 0){//bramble logic

                int brambleChanceRolled = random.nextInt(100) + 1;
                int brambleChance = BoxPlugin.instance.getEnchantBramble().getChanceFromTotalLevel(totalBrambleLvl);
                if(brambleChanceRolled <= brambleChance){
                    LivingEntity attacker = (LivingEntity) e.getDamager();
                    if(brambleProcessing.contains(attacker.getUniqueId())){//prevent recussion
                        return;
                    }
                    brambleProcessing.add(attacker.getUniqueId());
                    double cacterDmg = BoxPlugin.instance.getEnchantBramble().getDamageFromTotalLevel(totalBrambleLvl);
                    attacker.setNoDamageTicks(0);
                    if(attacker instanceof Player pTarget){
                        calcDamage(pTarget, DamageType.CACTUS, cacterDmg, p);
                    } else {
                        attacker.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + attacker.getName());
                    }
                    attacker.setNoDamageTicks(0);
                    brambleProcessing.remove(attacker.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void entityDamageLava(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the lava gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Magma.instance.hasEnchant(mainHandItem)){//magma enchant on weapon
                if(magmaProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                magmaProcessing.add(target.getUniqueId());
                int magmaLvl = CustomEnchantsMain.Enchant.Magma.instance.getLevel(mainHandItem);
                int totalFireBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.FireBorn);
                double magmaDmg = BoxPlugin.instance.getMagmaEnchant().getDamageFromTotalLevel(magmaLvl);
                if(totalFireBornLvl > 0){
                    magmaDmg *= BoxPlugin.instance.getFireBornEnchant().getDamageAmpFromTotalLevel(totalFireBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.LAVA, magmaDmg, p);
                } else {
                    target.damage(magmaDmg, DamageSource.builder(DamageType.LAVA).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (magmaDmg) + " bonus damage to " + target.getName());
                }
                magmaProcessing.remove(target.getUniqueId());
            }
        }
    }

    @EventHandler
    public void entityDamageIce(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the ice gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.IceAspect.instance.hasEnchant(mainHandItem)){//ice enchant on weapon
                if(freezeProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                freezeProcessing.add(target.getUniqueId());
                int iceLvl = CustomEnchantsMain.Enchant.IceAspect.instance.getLevel(mainHandItem);
                int totalIceBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.IceBorn);
                double freezeDmg = BoxPlugin.instance.getIceAspectEnchant().getDamageFromTotalLevel(iceLvl);
                if(totalIceBornLvl > 0){//defualt max ice is 140
                    int currFreezeTicks = target.getFreezeTicks();
                    int freezeTicks = (int) (30 * BoxPlugin.instance.getIceBornEnchant().getStackingSpeedFromTotalLevel(totalIceBornLvl));
                    target.setFreezeTicks(currFreezeTicks + freezeTicks);
                    freezeDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalIceBornLvl);
                } else {
                    int currFreezeTicks = target.getFreezeTicks();
                    target.setFreezeTicks(currFreezeTicks + 30);
                }
                if(target.getFreezeTicks() >= 140){
                    freezeDmg *= 1.3;
                }
                if(target.getFreezeTicks() >= 480){
                    freezeDmg *= 1.6;
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.FREEZE, freezeDmg, p);
                } else {
                    target.damage(freezeDmg, DamageSource.builder(DamageType.FREEZE).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (freezeDmg) + " bonus damage to " + target.getName());
                }
                freezeProcessing.remove(target.getUniqueId());
            }
        }
    }

    @EventHandler
    public void entityDamageDrown(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the water gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(mainHandItem)){//asphixiate enchant on weapon
                if(drownProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                drownProcessing.add(target.getUniqueId());
                int drownLvl = CustomEnchantsMain.Enchant.Asphyxiate.instance.getLevel(mainHandItem);
                int totalWaterBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.WaterBorn);
                double drownDmg = BoxPlugin.instance.getAsphyxiateEnchant().getDamageFromTotalLevel(drownLvl);
                if(totalWaterBornLvl > 0){//defualt max air is 300
                    int currDrownTicks = target.getRemainingAir();
                    int drownTicks = (int) (80 * BoxPlugin.instance.getWaterBornEnchant().getStackingSpeedFromTotalLevel(totalWaterBornLvl));
                    target.setRemainingAir(currDrownTicks - drownTicks);
                } else {
                    int currDrownTicks = target.getRemainingAir();
                    target.setRemainingAir(currDrownTicks - 80);
                }
                if(target.getRemainingAir() <= 0){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, true, false));
                    drownDmg *= 1.5;
                }
                if(target.getRemainingAir() <= -600){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 3, true, false));
                    drownDmg *= 1.7;
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.DROWN, drownDmg, p);
                } else {
                    target.damage(drownDmg, DamageSource.builder(DamageType.DROWN).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (drownDmg) + " bonus damage to " + target.getName());
                }
                drownProcessing.remove(target.getUniqueId());
            }
        }
        if(e.getDamager() instanceof Trident trident && e.getEntity() instanceof LivingEntity target){
            if (!(trident.getShooter() instanceof Player p)){
                return;
            }
            if(BoxPlugin.instance.getAsphyxiateEnchant().hasEnchantTrident(trident)){
                if(drownProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                drownProcessing.add(target.getUniqueId());
                int drownLvl = BoxPlugin.instance.getAsphyxiateEnchant().getLevelTrident(trident);
                int totalWaterBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.WaterBorn);
                double drownDmg = BoxPlugin.instance.getAsphyxiateEnchant().getDamageFromTotalLevel(drownLvl);
                if(totalWaterBornLvl > 0){
                    int currDrownTicks = target.getRemainingAir();
                    int drownTicks = (int) (30 * BoxPlugin.instance.getWaterBornEnchant().getStackingSpeedFromTotalLevel(totalWaterBornLvl));
                    target.setRemainingAir(currDrownTicks - drownTicks);
                } else {
                    int currDrownTicks = target.getRemainingAir();
                    target.setRemainingAir(currDrownTicks - 30);
                }
                if(target.getRemainingAir() <= 0){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, true, false));
                    drownDmg *= 1.5;
                }
                if(target.getRemainingAir() <= -600){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 3, true, false));
                    drownDmg *= 1.7;
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.DROWN, drownDmg, p);
                } else {
                    target.damage(drownDmg, DamageSource.builder(DamageType.DROWN).withCausingEntity(p).withDirectEntity(trident).build());
                    Util.debug(p, "dealt " + (drownDmg) + " bonus damage to " + target.getName());
                }
                e.setDamage(e.getDamage() + 12.0);
                drownProcessing.remove(target.getUniqueId());
            }
        }
    }

    @EventHandler
    public void entityDamageLightning(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the god gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Zeus.instance.hasEnchant(mainHandItem)){//zeus enchant on weapon
                if(lightningProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                lightningProcessing.add(target.getUniqueId());
                int zeusLvl = CustomEnchantsMain.Enchant.Zeus.instance.getLevel(mainHandItem);
                int totalGodBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.GodBorn);
                double defualtChance = BoxPlugin.instance.getZeusEnchant().getChanceFromTotalLevel(zeusLvl);
                if(totalGodBornLvl > 0){
                    defualtChance += BoxPlugin.instance.getGodBornEnchant().getAddedChanceFromTotalLevel(totalGodBornLvl);
                }
                Random random = new Random();

                double strikeRoll = random.nextDouble(1.01);
                if(defualtChance >= strikeRoll){
                    target.setNoDamageTicks(0);
                    double lightningDmg = target.getMaxHealth() * BoxPlugin.instance.getZeusEnchant().getMXPHDamageFromTotalLevel(zeusLvl);
                    if(target instanceof Player pTarget){
                        calcDamage(pTarget, DamageType.MAGIC, lightningDmg, p);
                    } else {
                        target.damage(lightningDmg, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (lightningDmg) + " bonus damage to " + target.getName());
                    }
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        p.getWorld().strikeLightningEffect(target.getLocation()); // visual only
                    }, 1);
                    target.setNoDamageTicks(0);
                }
                lightningProcessing.remove(target.getUniqueId());
            }
        }
    }

    @EventHandler
    public void entityDamageVoid(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the void gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.VoidAspect.instance.hasEnchant(mainHandItem)){//ice enchant on weapon
                if(voidProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                voidProcessing.add(target.getUniqueId());
                int voidLvl = CustomEnchantsMain.Enchant.VoidAspect.instance.getLevel(mainHandItem);
                int totalVoidBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.VoidBorn);
                double voidDmg = BoxPlugin.instance.getVoidAspectEnchant().getDamageFromTotalLevel(voidLvl);
                if(totalVoidBornLvl > 0){
                    voidDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalVoidBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.OUT_OF_WORLD, voidDmg, p);
                } else {
                    target.damage(voidDmg, DamageSource.builder(DamageType.OUT_OF_WORLD).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (voidDmg) + " bonus damage to " + target.getName());
                }
                voidProcessing.remove(target.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onTridentThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player p)) return;

        ItemStack item = p.getInventory().getItemInMainHand();

        PersistentDataContainer entityPdc = trident.getPersistentDataContainer();

        if(CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(item)) {
            entityPdc.set(CustomEnchantsMain.Enchant.Asphyxiate.instance.getEnchantKey(), PersistentDataType.INTEGER, CustomEnchantsMain.Enchant.Asphyxiate.instance.getLevel(item));
        }
    }

//    @EventHandler //my version
//    public void freeRiptideClick(PlayerInteractEvent e){
//        Player p = e.getPlayer();
//        ItemStack item = p.getItemInHand();
//        CraftItemStack csItem = (CraftItemStack) item;
//        if(item.getType() == Material.TRIDENT && CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(item)){
//            float ripTideLvl = 0;
//            if(item.containsEnchantment(Enchantment.RIPTIDE)){
//                ripTideLvl = item.getEnchantmentLevel(Enchantment.RIPTIDE);
//                ripTideLvl = 3.0F * ((1.0F + ripTideLvl) / 4.0F);
//            }
//            CraftPlayer cp = (CraftPlayer) p;
//            ServerPlayer entityhuman = cp.getHandle();
//            float f1 = entityhuman.getYRot();
//            float f2 = entityhuman.getXRot();
//            float f3 = -Mth.sin(f1 * ((float)Math.PI / 180F)) * Mth.cos(f2 * ((float)Math.PI / 180F));
//            float f4 = -Mth.sin(f2 * ((float)Math.PI / 180F));
//            float f5 = Mth.cos(f1 * ((float)Math.PI / 180F)) * Mth.cos(f2 * ((float)Math.PI / 180F));
//            float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
//            f3 *= ripTideLvl / f6;
//            f4 *= ripTideLvl / f6;
//            f5 *= ripTideLvl / f6;
//            entityhuman.push((double)f3, (double)f4, (double)f5);
//            entityhuman.startAutoSpinAttack(20, 8.0F, CraftItemStack.asNMSCopy(csItem));
//            if (entityhuman.onGround()) {
//                float f7 = 1.1999999F;
//                entityhuman.move(MoverType.SELF, new Vec3((double)0.0F, (double)1.1999999F, (double)0.0F));
//            }
//
//            p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_2, 1f, 1f);
//        }
//    }

    @EventHandler
    public void equipArmorWater(ArmorEquipEvent e){
        if (e.getOldArmorPiece() == e.getNewArmorPiece()) return; // no change
        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> updateSpeed(e.getPlayer()));
    }

    @EventHandler
    public void resetSpeedIfDie(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        updateSpeed(p);
    }
}
