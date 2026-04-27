package me.twostinkysocks.boxplugin.customEnchants;

import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.utils.events.extra.ArmorEquipEvent;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.util.RenderUtil;
import me.twostinkysocks.boxplugin.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R3.damage.CraftDamageSource;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Listeners implements Listener {
    private final FixedMetadataValue FMDV = new FixedMetadataValue(BoxPlugin.instance, true);
    record Pair(UUID player, UUID target) {}
    private HashMap<Pair, ArrayList<Long>> blackLightHitMap = new HashMap<>();
    private HashMap<Pair, Integer> sublimateHitMap = new HashMap<>();
    private HashMap<UUID, Long> cooldownDivine = new HashMap<>();
    private HashMap<Pair, DamageMap> elementHashmap = new HashMap<>();

    @EventHandler
    public void clearDeads(EntityDeathEvent e){
        blackLightHitMap.keySet().removeIf(key -> key.target().equals(e.getEntity().getUniqueId()));//remove dead target from all player hits
        sublimateHitMap.keySet().removeIf(key -> key.target().equals(e.getEntity().getUniqueId()));//remove dead target from all player hits
        elementHashmap.keySet().removeIf(key -> key.target().equals(e.getEntity().getUniqueId()));
    }

    private static class DamageMap{
        //this is a queue based system of storing damage types and their damage, last in first out, always input
        //the damage type and the ammount at the same time and read at the same time, remove both after
        ArrayList<DamageType> damageType = new ArrayList<>();
        ArrayList<Double> amount = new ArrayList<>();
        public DamageType getDamageType(int indx){
            return this.damageType.get(indx);
        }
        public double getAmmount(int indx){
            return this.amount.get(indx);
        }

        public void addDamageType(DamageType damageType) {
            this.damageType.add(damageType);
        }
        public void addAmmount(double amount){
            this.amount.add(amount);
        }
        public void removeDamagetype(int indx){
            this.damageType.remove(indx);
        }
        public void removeAmount(int indx){
            this.amount.remove(indx);
        }
        public int getListSize(){
            if(damageType.size() != amount.size()){
                return 0;
            }
            return damageType.size();
        }
    }

//    private static class DamageTracker {
//        private static final Map<UUID, Double> lastDamage = new HashMap<>();
//
//        public static void storeDamage(Player p, double amount) {
//            lastDamage.put(p.getUniqueId(), amount);
//        }
//
//        public static double getLastDamage(Player p) {
//            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> lastDamage.remove(p.getUniqueId()));
//            return lastDamage.getOrDefault(p.getUniqueId(), 0.0);
//        }
//    }

    public float findCombinedDamageRecursive(Pair playerPair, int numElementals){
        if(numElementals == 0){
            return 0;
        }
        if(elementHashmap.get(playerPair).getListSize() >= 1){
            Player target = Bukkit.getPlayer(playerPair.target);
            Player p = Bukkit.getPlayer(playerPair.player);

            DamageType damageType = elementHashmap.get(playerPair).getDamageType(numElementals-1);
            double finalDamage = elementHashmap.get(playerPair).getAmmount(numElementals-1);

            CraftPlayer cp = (CraftPlayer) target;
            ServerPlayer nms = cp.getHandle();

            CraftDamageSource source = (CraftDamageSource) DamageSource.builder(damageType).withCausingEntity(p).withDirectEntity(p).build();

            float damage;
            // armor + toughness
            damage = CombatRules.getDamageAfterAbsorb(nms,
                    (float) finalDamage, source.getHandle(), nms.getArmorValue(), (float) nms.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

            // enchantments (Protection etc.)
            float prot = EnchantmentHelper.getDamageProtection(
                    (ServerLevel) nms.level(),
                    nms,
                    source.getHandle()
            );
            if (prot > 0) {
                damage *= 1.0F - Math.min(20, prot) / 25F;
            }

            // resistance potion
            if (nms.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                int lvl = nms.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier();
                damage *= (1.0F - (lvl + 1) * 0.2F);
            }
            return damage + findCombinedDamageRecursive(playerPair, numElementals-1);
        } else {
            return 0;
        }
    }

    public void calcDamage(Player target, DamageType damageType, double ammount, Player attacker, EntityDamageByEntityEvent e){
        float attackStrength = attacker.getAttackCooldown();
        int elementMult = 0;
        int resistMult = 0;

        if (target.hasMetadata("NPC")) {
            return; // NPC (Citizens and most NPC plugins)
        }

        Pair playerPair = new Pair(attacker.getUniqueId(), target.getUniqueId());

        double finalDamage;
        if(damageType == DamageType.CACTUS){//calculate weaknesses to damage type
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.NatureResist, target);
        }
        if (damageType == DamageType.LAVA) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.IceBorn, target);
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.Overgrowth, target);
        }
        if (damageType == DamageType.FREEZE) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.VoidBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.IceResist, target);
        }
        if (damageType == DamageType.DROWN) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.FireBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterResist, target);
        }
        if (damageType == DamageType.MAGIC) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.WaterBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.LightningResist, target);
        }
        if (damageType == DamageType.OUT_OF_WORLD) {
            elementMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.GodBorn, target);
            resistMult += BoxPlugin.instance.getCustomEnchantsMain().getNumElement(CustomEnchantsMain.Enchant.VoidResist, target);
        }
        if(elementMult > 0){
            finalDamage = (ammount * (((double) elementMult / 10) + 1));
        } else {
            finalDamage = ammount;
        }
        if(resistMult > 0){
            finalDamage = finalDamage * Math.max(0.4, (1.0) - 0.1 * (double) resistMult);//max 60% damage reduction
        }
        if(target.isBlocking() || BoxPlugin.instance.getCurseManager().hasCurse(attacker)){
            return;
        }
        finalDamage = finalDamage * attackStrength;
        Util.debug(attacker, "dealt " + (finalDamage) + " bonus damage to " + target.getName());

        DamageMap damageMap;
        if(elementHashmap.containsKey(playerPair)){
            damageMap = elementHashmap.get(playerPair);
            damageMap.addDamageType(damageType);
            damageMap.addAmmount(finalDamage);
            elementHashmap.put(playerPair, damageMap);
        } else {
            damageMap = new DamageMap();
            damageMap.addDamageType(damageType);
            damageMap.addAmmount(finalDamage);
            elementHashmap.put(playerPair, damageMap);
        }

        float damage = findCombinedDamageRecursive(playerPair, elementHashmap.get(playerPair).getListSize());

        double oldHp = target.getHealth();
        double absorbtion = target.getAbsorptionAmount();

        if(damage + e.getFinalDamage() >= oldHp + absorbtion){
            e.setDamage(100);
            Util.debug(attacker, "Preventing double death, executing " + target.getName());
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> elementHashmap.remove(playerPair));
            return;
        }
        target.damage(finalDamage, DamageSource.builder(damageType).withCausingEntity(attacker).withDirectEntity(attacker).build());
        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> elementHashmap.remove(playerPair));
    }

    public void calcAttacklessDamage(Player target, DamageType damageType, double ammount, Player attacker, EntityDamageByEntityEvent e){//same as above but no attack strength
        int elementMult = 0;
        int resistMult = 0;

        if (target.hasMetadata("NPC")) {
            return; // NPC (Citizens and most NPC plugins)
        }

        Pair playerPair = new Pair(attacker.getUniqueId(), target.getUniqueId());

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
        if(elementMult > 0){
            finalDamage = (ammount * ((double) elementMult /10) + 1);
        } else {
            finalDamage = ammount;
        }
        if(resistMult > 0){
            finalDamage = finalDamage * Math.max(0.4, (1.0) - 0.1 * (double) resistMult);//max 60% damage reduction
        }
        if(target.isBlocking()){
            return;
        }
        Util.debug(attacker, "dealt " + (finalDamage) + " bonus damage to " + target.getName());

        DamageMap damageMap;
        if(elementHashmap.containsKey(playerPair)){
            damageMap = elementHashmap.get(playerPair);
            damageMap.addDamageType(damageType);
            damageMap.addAmmount(finalDamage);
            elementHashmap.put(playerPair, damageMap);
        } else {
            damageMap = new DamageMap();
            damageMap.addDamageType(damageType);
            damageMap.addAmmount(finalDamage);
            elementHashmap.put(playerPair, damageMap);
        }

        float damage = findCombinedDamageRecursive(playerPair, elementHashmap.get(playerPair).getListSize());

        double oldHp = target.getHealth();
        double absorbtion = target.getAbsorptionAmount();

        if(damage + e.getFinalDamage() >= oldHp + absorbtion){
            e.setDamage(100);
            Util.debug(attacker, "Preventing double death, executing " + target.getName());
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> elementHashmap.remove(playerPair));
            return;
        }
        target.damage(finalDamage, DamageSource.builder(damageType).withCausingEntity(attacker).withDirectEntity(attacker).build());
        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> elementHashmap.remove(playerPair));
    }

    public void updateSpeed(Player p, Boolean hasSlippey){
        if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
            return;
        }

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
                    if(hasSlippey){
                        speedToAdd *= 1.4;
                    }
                    p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1 + speedToAdd);
                    Util.debug(p, "setting bonus speed +" + String.format("%.2f",speedToAdd * 1000) + "%");
                }
            }
        }
    }

    public void checkSetBonuses(Player p){
        if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
            return;
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.FeatherWeight)){
            Util.debug(p, "Feather Weight set bonus activated!");
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Sublimation)){
            Util.debug(p, "Sublimation set bonus activated!");
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Arctic)){
            Util.debug(p, "Arctic set bonus activated!");
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.EventHorizon)){
            Util.debug(p, "Event Horizon set bonus activated!");
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.StormBorn)){
            Util.debug(p, "Storm Born set bonus activated!");
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Divine)){
            Util.debug(p, "Divine set bonus activated!");
        }
    }
    @EventHandler
    public void entityDamageLeaf(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();

            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            //P is the main player with the cactus gear
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Prickle.instance.hasEnchant(mainHandItem)){//prickle enchant on weapon
                if(target.hasMetadata("Prickle_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Prickle_Hit", FMDV);
                int cacterLvl = CustomEnchantsMain.Enchant.Prickle.instance.getLevel(mainHandItem);
                double cacterDmg = BoxPlugin.instance.getEnchantPrickle().getDamageFromTotalLevel(cacterLvl);
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.CACTUS, cacterDmg, p, e);
                } else {
                    float attackStrength = p.getAttackCooldown();
                    cacterDmg *= attackStrength;
                    target.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + target.getName());
                }
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () ->
                        target.removeMetadata("Prickle_Hit", BoxPlugin.instance));
            }
        }
      if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
//            int totalOvergrowthLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Overgrowth);
            int totalBrambleLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Bramble);
//
            Random random = new Random();
//
//            if(totalOvergrowthLvl > 0){//overgrowth logic
//                int overGrowChanceRolled = random.nextInt(100) + 1;
//                double overGrowChance = BoxPlugin.instance.getEnchantOvergrowth().getChanceFromTotalLevel(totalOvergrowthLvl);
//
//                double hpDiff = e.getFinalDamage();
//
//                Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> { // no async bugs
//                    double totalDamage = DamageTracker.getLastDamage(p) + hpDiff;
//                    if(totalDamage >= p.getHealth() || totalDamage == 0){//cancel if this hit kills you
//                        return;
//                    }
//                    if(overGrowChanceRolled <= overGrowChance){
//                        double ammountToHeal = (totalDamage * BoxPlugin.instance.getEnchantOvergrowth().getHealFromTotalLevel(totalOvergrowthLvl));
//                        Util.debug(p, "took " + totalDamage + " dammage, starting heal for: " + ammountToHeal);
//                        int i[] = {0};
//                        if(overGrowChanceRolled <= overGrowChance){// heals for a percent ofer 5 seconds
//                            Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
//                                if(task.isCancelled()) return; // just in case
//                                if (!p.isOnline() || p.isDead()) {
//                                    task.cancel();
//                                    return;
//                                }
//                                if(i[0] >= 5){
//                                    task.cancel();
//                                }
//                                if((p.getHealth() + (ammountToHeal/5)) < p.getAttribute(Attribute.MAX_HEALTH).getValue()){
//                                    p.setHealth(p.getHealth() + (ammountToHeal/5));
//                                } else {
//                                    p.setHealth(p.getMaxHealth());
//                                }
//                                i[0]++;
//                            }, 1, 20);
//                        }
//                    }
//                }, 2L);
//            }
            if(totalBrambleLvl > 0){//bramble logic

                int brambleChanceRolled = random.nextInt(100) + 1;
                double brambleChance = BoxPlugin.instance.getEnchantBramble().getChanceFromTotalLevel(totalBrambleLvl);
                if(brambleChanceRolled <= brambleChance){
                    LivingEntity attacker = (LivingEntity) e.getDamager();
                    if(attacker.hasMetadata("Bramble_Hit")){// prevent recussion
                        return;
                    }
                    attacker.setMetadata("Bramble_Hit", FMDV);
                    double cacterDmg = BoxPlugin.instance.getEnchantBramble().getDamageFromTotalLevel(totalBrambleLvl);
                    attacker.setNoDamageTicks(0);
                    if(attacker instanceof Player pTarget){
                        calcAttacklessDamage(pTarget, DamageType.CACTUS, cacterDmg, p, e);
                    } else {
                        attacker.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + attacker.getName());
                    }
                    attacker.setNoDamageTicks(0);
                    Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> attacker.removeMetadata("Bramble_Hit", BoxPlugin.instance));
                }
            }
        }
    }

    @EventHandler
    public void overGrowth(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            int totalOvergrowthLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Overgrowth);

            Random random = new Random();

            if(totalOvergrowthLvl > 0){//overgrowth logic
                int overGrowChanceRolled = random.nextInt(100) + 1;
                double overGrowChance = BoxPlugin.instance.getEnchantOvergrowth().getChanceFromTotalLevel(totalOvergrowthLvl);
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
                            if (!p.isOnline() || p.isDead()) {
                                task.cancel();
                                return;
                            }
                            if(i[0] >= 5){
                                task.cancel();
                            }
                            if((p.getHealth() + (ammountToHeal/5)) < p.getMaxHealth()){
                                p.setHealth(p.getHealth() + (ammountToHeal/5));
                            } else {
                                p.setHealth(p.getMaxHealth());
                            }
                            i[0]++;
                        }, 1, 20);
                    }
                }
            }
        }
    }

    @EventHandler
    public void entityDamageLava(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the lava gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }

            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Magma.instance.hasEnchant(mainHandItem)){//magma enchant on weapon
                if(target.hasMetadata("Magma_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Magma_Hit", FMDV);
                int magmaLvl = CustomEnchantsMain.Enchant.Magma.instance.getLevel(mainHandItem);
                int totalFireBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.FireBorn);
                double magmaDmg = CustomEnchantsMain.Enchant.Magma.instance.getDamageFromTotalLevel(magmaLvl);
                if(totalFireBornLvl > 0){
                    magmaDmg *= BoxPlugin.instance.getFireBornEnchant().getDamageAmpFromTotalLevel(totalFireBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.LAVA, magmaDmg, p, e);
                } else {
                    float attackStrength = p.getAttackCooldown();
                    magmaDmg *= attackStrength;
                    target.damage(magmaDmg, DamageSource.builder(DamageType.LAVA).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (magmaDmg) + " bonus damage to " + target.getName());
                }
                Location origin = target.getLocation().clone();
                origin.add(0, (target.getHeight()), 0);//at head
                RenderUtil.renderParticleOrb(origin, (int) magmaDmg, 0.2, Particle.FLAME, 0.05);
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Magma_Hit", BoxPlugin.instance));
            }
        }
    }

    @EventHandler
    public void entityDamageIce(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }

            ItemStack mainHandItem = p.getItemInHand();
            float attackStrength = p.getAttackCooldown();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.IceAspect.instance.hasEnchant(mainHandItem)){//ice enchant on weapon
                if(target.hasMetadata("Ice_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Ice_Hit", FMDV);
                int iceLvl = CustomEnchantsMain.Enchant.IceAspect.instance.getLevel(mainHandItem);
                int totalIceBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.IceBorn);
                double freezeDmg = BoxPlugin.instance.getIceAspectEnchant().getDamageFromTotalLevel(iceLvl);
                if(totalIceBornLvl > 0){//defualt max ice is 140
                    int currFreezeTicks = target.getFreezeTicks();
                    int freezeTicks = (int) (30 * BoxPlugin.instance.getIceBornEnchant().getStackingSpeedFromTotalLevel(totalIceBornLvl) * attackStrength);
                    target.setFreezeTicks(currFreezeTicks + freezeTicks);
                    freezeDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalIceBornLvl);
                } else {
                    int currFreezeTicks = target.getFreezeTicks();
                    target.setFreezeTicks((int) (currFreezeTicks + 30 * attackStrength));
                }
                if(target.getFreezeTicks() >= 140 && target.getFreezeTicks() < 480){
                    freezeDmg *= 1.3;
                }
                if(target.getFreezeTicks() >= 480){
                    freezeDmg *= 1.6;
                    Location origin = target.getLocation().clone();
                    origin.add(0, (target.getHeight() - 1), 0);//at chest
                    BlockData iceData = Material.PACKED_ICE.createBlockData();

                    target.getWorld().spawnParticle(
                            Particle.BLOCK,
                            origin,
                            30,        // count
                            0.4, 0.5, 0.4, // offset (spread)
                            0.01,      // speed
                            iceData); //block type
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.FREEZE, freezeDmg, p, e);
                } else {
                    freezeDmg *= attackStrength;
                    target.damage(freezeDmg, DamageSource.builder(DamageType.FREEZE).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (freezeDmg) + " bonus damage to " + target.getName());
                }
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Ice_Hit", BoxPlugin.instance));
            }
        }
    }

    @EventHandler
    public void entityDamageDrown(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            ItemStack mainHandItem = p.getItemInHand();
            float attackStrength = p.getAttackCooldown();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(mainHandItem)){//asphixiate enchant on weapon
                if(target.hasMetadata("Drown_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Drown_Hit", FMDV);
                int drownLvl = CustomEnchantsMain.Enchant.Asphyxiate.instance.getLevel(mainHandItem);
                int totalWaterBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.WaterBorn);
                double drownDmg = BoxPlugin.instance.getAsphyxiateEnchant().getDamageFromTotalLevel(drownLvl);
                if(totalWaterBornLvl > 0){//defualt max air is 300
                    int currDrownTicks = target.getRemainingAir();
                    int drownTicks = (int) (80 * BoxPlugin.instance.getWaterBornEnchant().getStackingSpeedFromTotalLevel(totalWaterBornLvl) * attackStrength);
                    target.setRemainingAir(currDrownTicks - drownTicks);
                } else {
                    int currDrownTicks = target.getRemainingAir();
                    target.setRemainingAir((int) (currDrownTicks - 80 * attackStrength));
                }
                if(target.getRemainingAir() <= 0 && target.getRemainingAir() > -600){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, true, false));
                    drownDmg *= 1.5;
                    Location origin = target.getLocation().clone();
                    origin.add(0, (target.getHeight()), 0);//at head
                    RenderUtil.renderParticleOrb(origin, 10, 0.4, Particle.BUBBLE, 0.05);
                    RenderUtil.renderParticleOrb(origin, 30, 0.4, Particle.DOLPHIN, 0.2);
                }
                if(target.getRemainingAir() <= -600){
                    if(!target.hasPotionEffect(PotionEffectType.WEAKNESS)){
                        target.sendMessage(ChatColor.DARK_BLUE + "You have been strucken deep!" + ChatColor.RED + " -16 attack damage!");
                        p.playSound(p.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1f, 0.65f);
                    }
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 3, true, false));
                    drownDmg *= 1.7;
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.DROWN, drownDmg, p, e);
                } else {
                    drownDmg *= attackStrength;
                    target.damage(drownDmg, DamageSource.builder(DamageType.DROWN).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (drownDmg) + " bonus damage to " + target.getName());
                }
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Drown_Hit", BoxPlugin.instance));
            }
        }
    }

    @EventHandler
    public void entityDamageLightning(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the god gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distance(e.getEntity().getLocation()) > 7){
                return;
            }

            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getInventory().getItemInMainHand();
            if(mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Zeus.instance.hasEnchant(mainHandItem)){//zeus enchant on weapon
                if(target.hasMetadata("Electric_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Electric_Hit", FMDV);
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
                    double lightningDmg = target.getMaxHealth() * CustomEnchantsMain.Enchant.Zeus.instance.getDamageFromTotalLevel(zeusLvl);
                    lightningDmg = Math.min(lightningDmg, 150);
                    if(target instanceof Player pTarget){
                        calcDamage(pTarget, DamageType.MAGIC, lightningDmg, p, e);
                    } else {
                        float attackStrength = p.getAttackCooldown();
                        lightningDmg *= attackStrength;
                        target.damage(lightningDmg, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (lightningDmg) + " bonus damage to " + target.getName());
                    }
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        p.getWorld().strikeLightningEffect(target.getLocation()); // visual only
                    }, 1);
                    target.setNoDamageTicks(0);

                    //StormLogic
                    if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.StormBorn) &&
                    CustomEnchantsMain.Enchant.Shocking.instance.hasEnchant(mainHandItem)){
                        Location origin = target.getLocation();
                        origin.add(0,1,0);
                        Collection<Entity> nearby = p.getWorld().getNearbyEntities(origin, 10, 10, 10, ent -> !p.getUniqueId().equals(ent.getUniqueId()) && !target.getUniqueId().equals(ent.getUniqueId()) && ent instanceof LivingEntity && !ent.isDead() && ent.getLocation().distanceSquared(origin) <= 10*10);
                        Util.debug(p, "Found " + nearby.size() + " living entities within 10 blocks");
                        int i = 0; // max 10 entities
                        for(Entity ent : nearby) {
                            if(i >= 10) break;

                            // ray trace
                            Vector travelDir = ent.getLocation().toVector().add(new Vector(0, 1, 0)).subtract(origin.toVector());//dont get feet
                            //check if blocks are in the way
                            double distanceToTarget = travelDir.length();
                            RayTraceResult blocksInWay = p.getWorld().rayTraceBlocks(origin, travelDir.clone().normalize(), distanceToTarget);
                            if(blocksInWay == null){
                                // draw line
                                Util.debug(p, "Raytraced entity was FOUND");
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.END_ROD, 0.02);
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.GLOW, 0.02);
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.ELECTRIC_SPARK, 2);
                                p.getWorld().playSound(ent.getLocation(), Sound.ENTITY_BEE_STING, 1F, 0.5F);
                                p.getWorld().playSound(ent.getLocation(), Sound.ITEM_WOLF_ARMOR_BREAK, 0.8F, 2F);

                                double shockDamage = lightningDmg / (Math.min(10, nearby.size()) + 0.5);
                                if(ent instanceof Player pTarget){
                                    calcDamage(pTarget, DamageType.MAGIC, shockDamage, p, e);
                                } else {
                                    LivingEntity newTarget = (LivingEntity) ent;
                                    newTarget.damage(shockDamage, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                                    Util.debug(p, "dealt " + (shockDamage) + " bonus damage to " + newTarget.getName());
                                }
                            }
                            i++;
                        }
                    }
                }
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Electric_Hit", BoxPlugin.instance));
            }
        }
    }

    @EventHandler
    public void entityDamageVoid(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.VoidAspect.instance.hasEnchant(mainHandItem)){//ice enchant on weapon
                if(target.hasMetadata("Void_Hit")){// prevent recussion
                    return;
                }
                target.setMetadata("Void_Hit", FMDV);
                int voidLvl = CustomEnchantsMain.Enchant.VoidAspect.instance.getLevel(mainHandItem);
                int totalVoidBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.VoidBorn);
                double voidDmg = BoxPlugin.instance.getVoidAspectEnchant().getDamageFromTotalLevel(voidLvl);
                if(totalVoidBornLvl > 0){
                    voidDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalVoidBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.OUT_OF_WORLD, voidDmg, p, e);
                } else {
                    float attackStrength = p.getAttackCooldown();
                    voidDmg *= attackStrength;
                    voidDmg = voidDmg * 1.1;//10% more to mobs
                    target.damage(voidDmg, DamageSource.builder(DamageType.OUT_OF_WORLD).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (voidDmg) + " bonus damage to " + target.getName());
                }
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Void_Hit", BoxPlugin.instance));
            }
        }
    }

    @EventHandler
    public void LifeSteal(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            //P is the main player with the lifesteal weapon
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }
            if(p.getLocation().distance(e.getEntity().getLocation()) > 7){
                return;
            }


            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.LifeSteal.instance.hasEnchant(mainHandItem)){//lifesteal enchant on weapon

                int lifeStealLvl = CustomEnchantsMain.Enchant.LifeSteal.instance.getLevel(mainHandItem);
                double lifeStealAmount = CustomEnchantsMain.Enchant.LifeSteal.instance.getEffectivnessFromTotalLvl(lifeStealLvl);
                Util.debug(p, "Healed for " + (e.getFinalDamage() * lifeStealAmount) + " from lifesteal");
                p.setHealth(Math.min(p.getHealth() + (e.getFinalDamage() * lifeStealAmount), p.getAttribute(Attribute.MAX_HEALTH).getValue()));
            }
        }
    }

    @EventHandler
    public void entityDamageTitan(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the titan gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }
            LivingEntity target = (LivingEntity) e.getEntity();

            if(target.hasMetadata("Titan_Hit")){// prevent recussion
                return;
            }
            target.setMetadata("Titan_Hit", FMDV);

            if(p.getLocation().distance(e.getEntity().getLocation()) > 7){
                return;
            }

            int totalTitanLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.Titan);
            if(totalTitanLvl > 0){//has titan ehcnant armor
                double titanDmgMult = CustomEnchantsMain.Enchant.Titan.instance.getDamageAmpFromTotalLevel(totalTitanLvl);
                double titanDmg = titanDmgMult * p.getAttribute(Attribute.MAX_HEALTH).getValue();

                float attackStrength = p.getAttackCooldown();
                titanDmg *= attackStrength;
                e.setDamage(e.getDamage() + titanDmg);
                Util.debug(p, "dealt " + (titanDmg) + " bonus damage to " + target.getName());
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Magma_Hit", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void tridentHit(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Trident trident && e.getEntity() instanceof LivingEntity target){
            if (!(trident.getShooter() instanceof Player p)){
                return;
            }
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }
            if(target.hasMetadata("Trident_Hit")){// prevent recussion
                return;
            }
            target.setMetadata("Trident_Hit", FMDV);
            ItemStack tridentItem = trident.getItem();
            ItemMeta tridentMeta = tridentItem.getItemMeta();
            double tridantBonusDmg = 1;
            Multimap<Attribute, AttributeModifier> itemAttributes= tridentMeta.getAttributeModifiers();//gets the tridents attributes to set new attack dmg
            if(itemAttributes != null && itemAttributes.containsKey(Attribute.ATTACK_DAMAGE)){
                Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.ATTACK_DAMAGE);
                for(AttributeModifier modifier : attModifier){
                    tridantBonusDmg = modifier.getAmount();
                }
            }
            if(!(e.getEntity() instanceof WaterMob)){
                target.damage((tridantBonusDmg) - 8);
            }
            if(CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(tridentItem)){
                int drownLvl = CustomEnchantsMain.Enchant.Asphyxiate.instance.getLevel(tridentItem);
                int totalWaterBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.WaterBorn);
                double drownDmg = CustomEnchantsMain.Enchant.Asphyxiate.instance.getDamageFromTotalLevel(drownLvl);
                if(totalWaterBornLvl > 0){
                    int currDrownTicks = target.getRemainingAir();
                    int drownTicks = (int) (80 * CustomEnchantsMain.Enchant.Asphyxiate.instance.getStackingSpeedFromTotalLevel(totalWaterBornLvl));
                    target.setRemainingAir(currDrownTicks - drownTicks);
                } else {
                    int currDrownTicks = target.getRemainingAir();
                    target.setRemainingAir(currDrownTicks - 80);
                }
                if(target.getRemainingAir() <= 0){//more drown damage for hitting skill shots
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, true, false));
                    drownDmg *= 1.7;
                }
                if(target.getRemainingAir() <= -600){
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 3, true, false));
                    drownDmg *= 2;
                }
                if(target instanceof Player pTarget){
                    calcAttacklessDamage(pTarget, DamageType.DROWN, drownDmg, p, e);
                } else {
                    target.damage(drownDmg, DamageSource.builder(DamageType.DROWN).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (drownDmg) + " bonus damage to " + target.getName());
                }
                Location origin = target.getLocation().clone();
                origin.add(0, (target.getHeight()), 0);//at head
                RenderUtil.renderParticleOrb(origin, 10, 0.4, Particle.BUBBLE, 0.05);
                RenderUtil.renderParticleOrb(origin, 30, 0.4, Particle.DOLPHIN, 0.2);
            }
            if(CustomEnchantsMain.Enchant.Magma.instance.hasEnchant(tridentItem)) {
                int magmaLvl = CustomEnchantsMain.Enchant.Magma.instance.getLevel(tridentItem);
                int totalFireBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.FireBorn);
                double magmaDmg = CustomEnchantsMain.Enchant.Magma.instance.getDamageFromTotalLevel(magmaLvl);
                if(totalFireBornLvl > 0){
                    magmaDmg *= BoxPlugin.instance.getFireBornEnchant().getDamageAmpFromTotalLevel(totalFireBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.LAVA, magmaDmg, p, e);
                } else {
                    target.damage(magmaDmg, DamageSource.builder(DamageType.LAVA).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (magmaDmg) + " bonus damage to " + target.getName());
                }
                Location origin = target.getLocation().clone();
                origin.add(0, (target.getHeight()), 0);//at head
                RenderUtil.renderParticleOrb(origin, (int) magmaDmg, 0.2, Particle.FLAME, 0.05);
            }
            if(CustomEnchantsMain.Enchant.IceAspect.instance.hasEnchant(tridentItem)) {
                int iceLvl = CustomEnchantsMain.Enchant.IceAspect.instance.getLevel(tridentItem);
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
                if(target.getFreezeTicks() >= 140 && target.getFreezeTicks() < 480){
                    freezeDmg *= 1.3;
                }
                if(target.getFreezeTicks() >= 480){
                    freezeDmg *= 1.6;
                    Location origin = target.getLocation().clone();
                    origin.add(0, (target.getHeight() - 1), 0);//at chest
                    BlockData iceData = Material.PACKED_ICE.createBlockData();

                    target.getWorld().spawnParticle(
                            Particle.BLOCK,
                            origin,
                            15,        // count
                            0.3, 0.3, 0.3, // offset (spread)
                            0.01,      // speed
                            iceData); //block type
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.FREEZE, freezeDmg, p, e);
                } else {
                    target.damage(freezeDmg, DamageSource.builder(DamageType.FREEZE).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (freezeDmg) + " bonus damage to " + target.getName());
                }
            }
            if(CustomEnchantsMain.Enchant.Prickle.instance.hasEnchant(tridentItem)) {
                int cacterLvl = CustomEnchantsMain.Enchant.Prickle.instance.getLevel(tridentItem);
                double cacterDmg = BoxPlugin.instance.getEnchantPrickle().getDamageFromTotalLevel(cacterLvl);
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.CACTUS, cacterDmg, p, e);
                } else {
                    target.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + target.getName());
                }
            }
            if(CustomEnchantsMain.Enchant.Zeus.instance.hasEnchant(tridentItem)) {
                int zeusLvl = CustomEnchantsMain.Enchant.Zeus.instance.getLevel(tridentItem);
                int totalGodBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.GodBorn);
                double defualtChance = BoxPlugin.instance.getZeusEnchant().getChanceFromTotalLevel(zeusLvl);
                if(totalGodBornLvl > 0){
                    defualtChance += BoxPlugin.instance.getGodBornEnchant().getAddedChanceFromTotalLevel(totalGodBornLvl);
                }
                Random random = new Random();

                double strikeRoll = random.nextDouble(1.01);
                if(defualtChance >= strikeRoll){
                    target.setNoDamageTicks(0);
                    double lightningDmg = target.getMaxHealth() * CustomEnchantsMain.Enchant.Zeus.instance.getDamageFromTotalLevel(zeusLvl);
                    lightningDmg = Math.min(lightningDmg, 150);
                    if(target instanceof Player pTarget){
                        calcDamage(pTarget, DamageType.MAGIC, lightningDmg, p, e);
                    } else {
                        target.damage(lightningDmg, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (lightningDmg) + " bonus damage to " + target.getName());
                    }
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        p.getWorld().strikeLightningEffect(target.getLocation()); // visual only
                    }, 1);
                    target.setNoDamageTicks(0);

                    //StormLogic
                    if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.StormBorn) &&
                            CustomEnchantsMain.Enchant.Shocking.instance.hasEnchant(tridentItem)){
                        Location origin = target.getLocation();
                        origin.add(0,1,0);
                        Collection<Entity> nearby = p.getWorld().getNearbyEntities(origin, 10, 10, 10, ent -> !p.getUniqueId().equals(ent.getUniqueId()) && !target.getUniqueId().equals(ent.getUniqueId()) && ent instanceof LivingEntity && !ent.isDead() && ent.getLocation().distanceSquared(origin) <= 10*10);
                        Util.debug(p, "Found " + nearby.size() + " living entities within 10 blocks");
                        int i = 0; // max 10 entities
                        for(Entity ent : nearby) {
                            if(i >= 10) break;

                            // ray trace
                            Vector travelDir = ent.getLocation().toVector().add(new Vector(0, 1, 0)).subtract(origin.toVector());//dont get feet
                            //check if blocks are in the way
                            double distanceToTarget = travelDir.length();
                            RayTraceResult blocksInWay = p.getWorld().rayTraceBlocks(origin, travelDir.clone().normalize(), distanceToTarget);
                            if(blocksInWay == null){
                                // draw line
                                Util.debug(p, "Raytraced entity was FOUND");
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.END_ROD, 0.02);
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.GLOW, 0.02);
                                RenderUtil.renderParticleLine(origin.clone(), travelDir.clone(), Particle.ELECTRIC_SPARK, 2);
                                p.getWorld().playSound(ent.getLocation(), Sound.ENTITY_BEE_STING, 1F, 0.5F);
                                p.getWorld().playSound(ent.getLocation(), Sound.ITEM_WOLF_ARMOR_BREAK, 0.8F, 2F);

                                double shockDamage = lightningDmg / (Math.min(10, nearby.size()) + 0.5);
                                if(ent instanceof Player pTarget){
                                    calcDamage(pTarget, DamageType.MAGIC, shockDamage, p, e);
                                } else {
                                    LivingEntity newTarget = (LivingEntity) ent;
                                    newTarget.damage(shockDamage, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                                    Util.debug(p, "dealt " + (shockDamage) + " bonus damage to " + newTarget.getName());
                                }
                            }
                            i++;
                        }
                    }
                }
            }
            if(CustomEnchantsMain.Enchant.VoidAspect.instance.hasEnchant(tridentItem)) {
                int voidLvl = CustomEnchantsMain.Enchant.VoidAspect.instance.getLevel(tridentItem);
                int totalVoidBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.VoidBorn);
                double voidDmg = BoxPlugin.instance.getVoidAspectEnchant().getDamageFromTotalLevel(voidLvl);
                if(totalVoidBornLvl > 0){
                    voidDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalVoidBornLvl);
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.OUT_OF_WORLD, voidDmg, p, e);
                } else {
                    target.damage(voidDmg, DamageSource.builder(DamageType.OUT_OF_WORLD).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (voidDmg) + " bonus damage to " + target.getName());
                }
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Trident_Hit", BoxPlugin.instance));
        }
    }

//    @EventHandler to remove
//    public void onTridentThrow(ProjectileLaunchEvent e) {
//        if (!(e.getEntity() instanceof Trident trident)) return;
//        if (!(trident.getShooter() instanceof Player p)) return;
//        if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
//            return;
//        }
//
//        ItemStack item = p.getInventory().getItemInMainHand();
//
//        PersistentDataContainer entityPdc = trident.getPersistentDataContainer();
//
//        if(CustomEnchantsMain.Enchant.Asphyxiate.instance.hasEnchant(item)) {
//            entityPdc.set(CustomEnchantsMain.Enchant.Asphyxiate.instance.getEnchantKey(), PersistentDataType.INTEGER, CustomEnchantsMain.Enchant.Asphyxiate.instance.getLevel(item));
//        }
//    }

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
        Player p = e.getPlayer();
        if(p.hasMetadata("switchingArmor")){// prevent recussion
            return;
        }
        p.setMetadata("switchingArmor", FMDV);

        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> updateSpeed(p, false));
        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> checkSetBonuses(p));

        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("switchingArmor", BoxPlugin.instance));
    }

    @EventHandler
    public void resetSpeedIfDie(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        updateSpeed(p, false);
    }

    /*
        The following is logic for enchants used in the combo sets:
     */

    @EventHandler
    public void entityDamageIceThorns(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }
            int iceThornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.IceThorns);

            Random random = new Random();

            if(iceThornLvl > 0){//ice thorns logic

                int iceThornChanceRolled = random.nextInt(100) + 1;
                double iceChance = CustomEnchantsMain.Enchant.IceThorns.instance.getChanceFromTotalLevel(iceThornLvl);
                if(iceThornChanceRolled <= iceChance){
                    LivingEntity attacker = (LivingEntity) e.getDamager();
                    if(attacker.hasMetadata("IceThorn_hit")){// prevent recussion
                        return;
                    }
                    attacker.setMetadata("IceThorn_hit", FMDV);
                    double iceDmg = CustomEnchantsMain.Enchant.IceThorns.instance.getDamageFromTotalLevel(iceThornLvl);
                    attacker.setNoDamageTicks(0);
                    int totalIceBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.IceBorn);
                    if(totalIceBornLvl > 0){//defualt max ice is 140
                        int currFreezeTicks = attacker.getFreezeTicks();
                        int freezeTicks = (int) (30 * BoxPlugin.instance.getIceBornEnchant().getStackingSpeedFromTotalLevel(totalIceBornLvl));
                        attacker.setFreezeTicks(currFreezeTicks + freezeTicks);
                        iceDmg *= BoxPlugin.instance.getIceBornEnchant().getDamageAmpFromTotalLevel(totalIceBornLvl);
                    } else {
                        int currFreezeTicks = attacker.getFreezeTicks();
                        attacker.setFreezeTicks(currFreezeTicks + 30);
                    }
                    if(attacker.getFreezeTicks() >= 140 && attacker.getFreezeTicks() < 480){
                        iceDmg *= 1.3;
                    }
                    if(attacker.getFreezeTicks() >= 480){
                        iceDmg *= 1.6;
                        Location origin = attacker.getLocation().clone();
                        origin.add(0, (attacker.getHeight() - 1), 0);//at chest
                        BlockData iceData = Material.PACKED_ICE.createBlockData();

                        attacker.getWorld().spawnParticle(
                                Particle.BLOCK,
                                origin,
                                15,        // count
                                0.4, 0.5, 0.4, // offset (spread)
                                0.01,      // speed
                                iceData); //block type
                    }
                    if(attacker instanceof Player pTarget){
                        calcAttacklessDamage(pTarget, DamageType.FREEZE, iceDmg, p, e);
                    } else {
                        attacker.damage(iceDmg, DamageSource.builder(DamageType.FREEZE).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (iceDmg) + " bonus damage to " + attacker.getName());
                    }
                    attacker.setNoDamageTicks(0);
                    Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> attacker.removeMetadata("IceThorn_hit", BoxPlugin.instance));
                }
            }
        }
    }

    @EventHandler
    public void entityDamageLavaThorns(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            int lavaThornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.LavaThorns);

            Random random = new Random();

            if(lavaThornLvl > 0){//lava thorns logic

                int lavaThornChanceRolled = random.nextInt(100) + 1;
                double lavaChance = CustomEnchantsMain.Enchant.LavaThorns.instance.getChanceFromTotalLevel(lavaThornLvl);
                if(lavaThornChanceRolled <= lavaChance){
                    LivingEntity attacker = (LivingEntity) e.getDamager();
                    if(attacker.hasMetadata("LavaThorn_hit")){// prevent recussion
                        return;
                    }
                    attacker.setMetadata("LavaThorn_hit", FMDV);
                    double lavaDmg = CustomEnchantsMain.Enchant.LavaThorns.instance.getDamageFromTotalLevel(lavaThornLvl);
                    attacker.setNoDamageTicks(0);
                    int totalFireBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.FireBorn);
                    if(totalFireBornLvl > 0){
                        lavaDmg *= BoxPlugin.instance.getFireBornEnchant().getDamageAmpFromTotalLevel(totalFireBornLvl);
                    }
                    if(attacker instanceof Player pTarget){
                        calcAttacklessDamage(pTarget, DamageType.LAVA, lavaDmg, p, e);
                    } else {
                        attacker.damage(lavaDmg, DamageSource.builder(DamageType.LAVA).withCausingEntity(p).withDirectEntity(p).build());
                        Util.debug(p, "dealt " + (lavaDmg) + " bonus damage to " + attacker.getName());
                    }
                    Location origin = attacker.getLocation().clone();
                    origin.add(0, (attacker.getHeight()), 0);//at head
                    RenderUtil.renderParticleOrb(origin, (int) (lavaDmg/2), 0.2, Particle.FLAME, 0.05);
                    attacker.setNoDamageTicks(0);
                    Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> attacker.removeMetadata("LavaThorn_hit", BoxPlugin.instance));
                }
            }
        }
    }

    @EventHandler
    public void entityDamageGodThorns(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            int godThornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.GodThorns);

            Random random = new Random();

            if(godThornLvl > 0){//god thorns logic

                int godThornChanceRolled = random.nextInt(100) + 1;
                double godThornChance = CustomEnchantsMain.Enchant.GodThorns.instance.getChanceFromTotalLevel(godThornLvl);
                //final damage must be more than 10% max hp && (p.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.1) <= e.getFinalDamage()
                if(godThornChanceRolled <= godThornChance){
                    LivingEntity attacker = (LivingEntity) e.getDamager();
                    if(attacker.hasMetadata("GodThorn_hit")){// prevent recussion
                        return;
                    }
                    attacker.setMetadata("GodThorn_hit", FMDV);
                    AtomicReference<Double> hpDiff = new AtomicReference<>(0.0);
                    double curHp = p.getHealth();
                    Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> hpDiff.set(curHp - p.getHealth()));
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> { // no async bugs
                        double lightningDamage = CustomEnchantsMain.Enchant.GodThorns.instance.getDamageFromTotalLevel(godThornLvl) * hpDiff.get();
                        attacker.setNoDamageTicks(0);

                        if(attacker instanceof Player pTarget){
                            calcAttacklessDamage(pTarget, DamageType.MAGIC, lightningDamage, p, e);
                        } else {
                            attacker.damage(lightningDamage, DamageSource.builder(DamageType.MAGIC).withCausingEntity(p).withDirectEntity(p).build());
                            Util.debug(p, "dealt " + (lightningDamage) + " bonus damage to " + attacker.getName());
                        }
                        Location origin = attacker.getLocation().clone();
                        origin.add(0, (attacker.getHeight() * 2), 0);//at head
                        Location lineEnd = attacker.getLocation().clone();
                        lineEnd.subtract(0, (attacker.getHeight() * 2), 0);//at feet
                        Vector dir = lineEnd.toVector().subtract(origin.toVector());
                        RenderUtil.renderParticleLine(origin, dir, Particle.ELECTRIC_SPARK, 0.2);
                        p.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_BEE_STING, 1F, 0.5F);
                        attacker.setNoDamageTicks(0);
                        Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> attacker.removeMetadata("GodThorn_hit", BoxPlugin.instance));
                    }, 2L);
                }
            }
        }
    }

    @EventHandler
    public void entityDamageBrittle(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            boolean crit =
                    p.getFallDistance() > 0 &&
                            !p.isOnGround() &&
                            !p.isSprinting() &&
                            !p.isInsideVehicle() &&
                            !p.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                            p.getAttackCooldown() > 0.9;
            if(!crit){
                return;
            }
            if(p.hasMetadata("Sublimate_hit")){// prevent recussion
                return;
            }
            p.setMetadata("Sublimate_hit", FMDV);

            ItemStack mainHandItem = p.getInventory().getItemInMainHand();

            if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Sublimation) && mainHandItem.getItemMeta() != null &&
                    CustomEnchantsMain.Enchant.Brittle.instance.hasEnchant(mainHandItem)){//sublimation logic

                Pair key = new Pair(p.getUniqueId(), target.getUniqueId());
                if(sublimateHitMap.containsKey(key)){
                    int newHits = sublimateHitMap.get(key) + 1;
                    sublimateHitMap.put(key, newHits);
                } else {
                    sublimateHitMap.put(key, 1);
                }

                if(target.hasMetadata("Sublimate_ready")){
                    return;
                }

                Particle.DustOptions startDust = new Particle.DustOptions(Color.fromRGB(148, 197, 227), 0.5F);
                int[] hitsAtReady = {0};
                int[] i = {0};//num seconds until ready
                //every 2 crits
                AtomicBoolean validProc = new AtomicBoolean(false);
                if(sublimateHitMap.get(key) % 2 == 0) {
                    hitsAtReady[0] = sublimateHitMap.getOrDefault(key, 0);
                    double entityHieght = target.getHeight();
                    target.setMetadata("Sublimate_ready", FMDV);
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2f, 2f);
                    Util.debug(p, "Stack is ready to be claimed from " + target.getName());

                    Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
                        if (task.isCancelled()) return; // just in case

                        Location origin = target.getLocation().clone();
                        origin.add(0, (entityHieght + 1), 0);

                        RenderUtil.renderDustOrb(origin, 60, 0.3, startDust);

                        if (sublimateHitMap.getOrDefault(key, 0) > hitsAtReady[0]) {
                            Util.debug(p,ChatColor.GREEN + "Proced the brittle " + target.getName());
                            validProc.set(true);
                            double sublimateDmg = 0;

                            int iceLvl = CustomEnchantsMain.Enchant.IceAspect.instance.getLevel(mainHandItem);
                            double freezeDmg = CustomEnchantsMain.Enchant.IceAspect.instance.getDamageFromTotalLevel(iceLvl);

                            if(target.getFreezeTicks() >= 140 && target.getFreezeTicks() < 480){
                                freezeDmg *= 1.3;
                            }
                            if(target.getFreezeTicks() >= 480){
                                freezeDmg *= 1.6;
                            }
                            sublimateDmg += freezeDmg;

                            int magmaLvl = CustomEnchantsMain.Enchant.Magma.instance.getLevel(mainHandItem);
                            int totalFireBornLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.FireBorn);
                            double magmaDmg = CustomEnchantsMain.Enchant.Magma.instance.getDamageFromTotalLevel(magmaLvl);
                            if(totalFireBornLvl > 0){
                                magmaDmg *= BoxPlugin.instance.getFireBornEnchant().getDamageAmpFromTotalLevel(totalFireBornLvl);
                            }
                            sublimateDmg += magmaDmg;
                            sublimateDmg *= CustomEnchantsMain.Enchant.Brittle.instance.getLevel(mainHandItem) * 2.5;

                            target.damage(sublimateDmg, DamageSource.builder(DamageType.FALLING_ANVIL).withCausingEntity(p).withDirectEntity(p).build());
                            Util.debug(p, "dealt " + (sublimateDmg) + " bonus damage to " + target.getName());

                            sublimateHitMap.remove(key);

                            //sfx
                            p.playSound(p.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1f, 1.55f);
                            p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);

                            RenderUtil.renderParticleOrb(origin, 60, 0.5, Particle.SNOWFLAKE, 0.3);
                            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Sublimate_ready", BoxPlugin.instance));
                            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("Sublimate_hit", BoxPlugin.instance));
                            task.cancel();
                        }
                        if (target.isDead() || validProc.get() || i[0] == 200) {//cancel if dead, claimed, or 20 seconds since activated
                            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> target.removeMetadata("Sublimate_ready", BoxPlugin.instance));
                            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("Sublimate_hit", BoxPlugin.instance));
                            task.cancel();
                        }
                        i[0]++;
                    }, 0, 2);
                }
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("Sublimate_hit", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void entityDamageCloudBurst(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the feather weight gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }
            if(p.hasMetadata("launching")){// prevent recussion
                return;
            }
            p.setMetadata("launching", FMDV);

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("launching", BoxPlugin.instance));
                return;
            }
            boolean crit =
                    p.getFallDistance() > 0 &&
                            !p.isOnGround() &&
                            !p.isSprinting() &&
                            !p.isInsideVehicle() &&
                            !p.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                            p.getAttackCooldown() > 0.9;
            if(!crit){
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("launching", BoxPlugin.instance));
                return;
            }
            if(target.getHeight() + target.getLocation().getY() + 1.0 < p.getLocation().getY()){//dont do it if already bouncing
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("launching", BoxPlugin.instance));
                return;
            }
            ItemStack mainHandItem = p.getInventory().getItemInMainHand();

            if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.FeatherWeight) && mainHandItem.getItemMeta() != null &&
                    CustomEnchantsMain.Enchant.CloudBurst.instance.hasEnchant(mainHandItem)){//cloud burst logic

                Location origin = p.getLocation().clone();
                origin.subtract(0, (1), 0);
                RenderUtil.renderParticleOrb(origin, 70, 1, Particle.CAMPFIRE_COSY_SMOKE, 0.2);
                Vector v = p.getVelocity();
                double base = Math.max(0, v.getY()); // ignore downward motion
                Util.debug(p, "Launching up");
                p.setVelocity(p.getVelocity().add(new Vector(0, ((double) CustomEnchantsMain.Enchant.CloudBurst.instance.getLevel(mainHandItem) * 1.2 + base), 0)));
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("launching", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void slippyMoment(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
            return;
        }
        if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Arctic)){
            if(p.hasMetadata("switchingHand")){// prevent recussion
                return;
            }
            p.setMetadata("switchingHand", FMDV);
            ItemStack mainHandItem = p.getInventory().getItem(e.getNewSlot());
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Slippery.instance.hasEnchant(mainHandItem)){
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> updateSpeed(p, true));
            } else {
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> updateSpeed(p, false));
            }

            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("switchingHand", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void entityDamageDarkFlame(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            if(p.hasMetadata("darkFlameHit")){// prevent recussion
                return;
            }
            p.setMetadata("darkFlameHit", FMDV);
            boolean crit =
                    p.getFallDistance() > 0 &&
                            !p.isOnGround() &&
                            !p.isSprinting() &&
                            !p.isInsideVehicle() &&
                            !p.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                            p.getAttackCooldown() > 0.9;
            if(!crit){
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("darkFlameHit", BoxPlugin.instance));
                return;
            }
            ItemStack mainHandItem = p.getInventory().getItemInMainHand();

            if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.EventHorizon) && mainHandItem.getItemMeta() != null &&
                    CustomEnchantsMain.Enchant.DarkFlame.instance.hasEnchant(mainHandItem)){//dark flame logic

                Random random = new Random();
                int stunChanceRoll = random.nextInt(100) + 1;
                int stunChance = CustomEnchantsMain.Enchant.DarkFlame.instance.getLevel(mainHandItem);
                if(stunChanceRoll <= CustomEnchantsMain.Enchant.DarkFlame.instance.getChanceFromTotalLevel(stunChance)){
                    Location origin = target.getLocation().clone();

                    double w = target.getWidth();
                    double h = target.getHeight();

                    // move to MIN corner (bottom southwest corner of hitbox)
                    origin.add(-w / 2, 0, -w / 2);

                    RenderUtil.renderParticleBox(origin, w, h, w, Particle.SMOKE, 0.03);
                    RenderUtil.renderParticleBox(origin, w, h, w, Particle.SCULK_SOUL, 0.06);
                    p.playSound(target.getLocation(), Sound.BLOCK_GILDED_BLACKSTONE_BREAK, 4f, 0.5f);

                    int ticksToStun = 20 * (CustomEnchantsMain.Enchant.Magma.instance.getLevel(mainHandItem) +
                            CustomEnchantsMain.Enchant.VoidAspect.instance.getLevel(mainHandItem)) / 8;
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticksToStun, 10, true, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticksToStun, 10, true, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticksToStun, 1, true, false));
                }
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("darkFlameHit", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void entityDamageDivine(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            //P is the main player with the ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distanceSquared(target.getLocation()) > 49){
                return;
            }
            boolean goodHit = !p.hasPotionEffect(PotionEffectType.BLINDNESS) && p.getAttackCooldown() > 0.9;
            if(!goodHit){
                return;
            }
            if(p.hasMetadata("BlackLight_hit")){// prevent recussion
                return;
            }
            p.setMetadata("BlackLight_hit", FMDV);

            ItemStack mainHandItem = p.getInventory().getItemInMainHand();

            if(BoxPlugin.instance.getCustomEnchantsMain().hasFullSetBonus(p, CustomEnchantsMain.Enchant.Divine) && mainHandItem.getItemMeta() != null &&
                    CustomEnchantsMain.Enchant.BlackLight.instance.hasEnchant(mainHandItem) && (!cooldownDivine.containsKey(p.getUniqueId())
                    || cooldownDivine.get(p.getUniqueId()) < System.currentTimeMillis())) {//divine logic

                Pair key = new Pair(p.getUniqueId(), target.getUniqueId());
                ArrayList<Long> hitTimes = new ArrayList<>();
                if (blackLightHitMap.containsKey(key)) {
                    hitTimes = blackLightHitMap.get(key);
                    hitTimes.add(System.currentTimeMillis());
                    blackLightHitMap.put(key, hitTimes);
                } else {
                    hitTimes.add(System.currentTimeMillis());
                    blackLightHitMap.put(key, hitTimes);
                }
                Util.debug(p, "Registered crit pair in index: " + hitTimes.size());

                if (hitTimes.size() >= 3) {//need atleast 3 hits
                    double hit1 = (double) hitTimes.get(hitTimes.size() - 1) / 1000;
                    double hit3 = (double) hitTimes.get(hitTimes.size() - 3) / 1000;

                    if (hit1 - hit3 <= 4) {//all 3 crits happened in the last 4 seconds

                        double targetMXHP = target.getAttribute(Attribute.MAX_HEALTH).getValue();
                        double playerMXHP = p.getAttribute(Attribute.MAX_HEALTH).getValue();

                        if((target.getHealth() >= targetMXHP/3) && (p.getHealth() >= playerMXHP/3)){
                            double blackLightDmg = target.getHealth() * 0.15;//15% current HP
                            target.damage(blackLightDmg, DamageSource.builder(DamageType.OUT_OF_WORLD).withCausingEntity(p).withDirectEntity(p).build());
                            Util.debug(p, "Divine Darkness activated");
                            Util.debug(p, "dealt " + (blackLightDmg) + " bonus damage to " + target.getName());

                            //sfx
                            p.playSound(p.getLocation(), Sound.BLOCK_SNIFFER_EGG_CRACK, 1f, 0.5f);
                            p.playSound(p.getLocation(), Sound.ITEM_WOLF_ARMOR_CRACK, 1f, 0.5f);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0.5f);

                            Location origin = target.getLocation().clone();
                            origin.add(0, (target.getHeight() * 4), 0);//at head
                            Location lineEnd = target.getLocation().clone();
                            lineEnd.subtract(0, (target.getHeight() * 2), 0);//at feet
                            Vector dir = lineEnd.toVector().subtract(origin.toVector());
                            RenderUtil.renderParticleLine(origin, dir, Particle.WITCH, 0.3);
                        } else {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 2, true, false));
                            Util.debug(p, "Divine Light activated");

                            //sfx
                            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);

                            Location origin = p.getLocation().clone();
                            origin.add(0, (p.getHeight() * 4), 0);//at head
                            Location lineEnd = p.getLocation().clone();
                            lineEnd.subtract(0, (p.getHeight() * 2), 0);//at feet
                            Vector dir = lineEnd.toVector().subtract(origin.toVector());
                            RenderUtil.renderParticleLine(origin, dir, Particle.WAX_ON, 0.3);
                        }
                        cooldownDivine.put(p.getUniqueId(), System.currentTimeMillis() +
                                (long)(5000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 5 seconds

                        blackLightHitMap.remove(key);
                    }
                }
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("BlackLight_hit", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void entityDamageBlackIce(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            //P is the main player with the black ice gear
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                return;
            }

            if(p.getLocation().distance(e.getEntity().getLocation()) > 7){
                return;
            }

            if(p.hasMetadata("BlackIce_hit")){// prevent recussion
                return;
            }
            p.setMetadata("BlackIce_hit", FMDV);

            LivingEntity target = (LivingEntity) e.getEntity();
            int totalBlackIceLvl = BoxPlugin.instance.getCustomEnchantsMain().getCombinedEnchLevel(p, CustomEnchantsMain.Enchant.BlackIce);
            ItemStack mainHandItem = p.getInventory().getItemInMainHand();
            if(mainHandItem.getItemMeta() == null){
                Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("BlackIce_hit", BoxPlugin.instance));
                return;
            }
            if(totalBlackIceLvl > 0 && CustomEnchantsMain.Enchant.IceAspect.instance.hasEnchant(mainHandItem)){//has black ice armor
                double blackIceDmgMult = CustomEnchantsMain.Enchant.BlackIce.instance.getDamageAmpFromTotalLevel(totalBlackIceLvl);
                double blackIcedmg = blackIceDmgMult * (p.getAttribute(Attribute.MAX_HEALTH).getValue() - p.getHealth());

                float attackStrength = p.getAttackCooldown();
                blackIcedmg *= attackStrength;

                if(target.getFreezeTicks() >= 140 && target.getFreezeTicks() < 480){
                    blackIcedmg *= 1.3;
                }
                if(target.getFreezeTicks() >= 480){
                    blackIcedmg *= 1.6;
                    Location origin = target.getLocation().clone();
                    origin.add(0, (target.getHeight() - 1), 0);//at chest
                    BlockData iceData = Material.OBSIDIAN.createBlockData();

                    target.getWorld().spawnParticle(
                            Particle.BLOCK,
                            origin,
                            30,        // count
                            0.4, 0.5, 0.4, // offset (spread)
                            0.01,      // speed
                            iceData); //block type
                }
                if(target instanceof Player pTarget){
                    calcDamage(pTarget, DamageType.FREEZE, blackIcedmg, p, e);
                } else {
                    target.damage(blackIcedmg, DamageSource.builder(DamageType.FREEZE).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (blackIcedmg) + " bonus damage to " + target.getName());
                }
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("BlackIce_hit", BoxPlugin.instance));
        }
    }

    @EventHandler
    public void layEggs(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();

        ItemStack leggings = p.getInventory().getLeggings();
        if(leggings != null && leggings.hasItemMeta() && CustomEnchantsMain.Enchant.SaturnEgg.instance.hasEnchant(leggings) && p.isSneaking()){
            if(p.hasMetadata("CurrentlyShitting")){// prevent recussion
                return;
            }
            p.setMetadata("CurrentlyShitting", FMDV);
            Random random = new Random();
            int rolledChance = random.nextInt(100) + 1;
            int eggLvl = CustomEnchantsMain.Enchant.SaturnEgg.instance.getLevel(leggings);
            double eggChance = CustomEnchantsMain.Enchant.SaturnEgg.instance.getChanceFromTotalLevel(eggLvl);

            if(eggChance >= rolledChance){
                ItemStack egg = new ItemStack(Material.EGG);
                Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                itemEntity.setItemStack(egg);
            }
            Bukkit.getScheduler().runTask(BoxPlugin.instance, () -> p.removeMetadata("CurrentlyShitting", BoxPlugin.instance));
        }
    }
}
