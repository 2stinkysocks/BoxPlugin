package me.twostinkysocks.boxplugin.customEnchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.PrickleEnchant;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class Listeners implements Listener {
    private final Set<UUID> prickleProcessing = new HashSet<>();
    private final Set<UUID> brambleProcessing = new HashSet<>();
    @EventHandler
    public void entityDamageLeaf(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            Player p = (Player) e.getDamager();
            LivingEntity target = (LivingEntity) e.getEntity();
            ItemStack mainHandItem = p.getItemInHand();
            if(mainHandItem != null && mainHandItem.getItemMeta() != null && CustomEnchantsMain.Enchant.Prickle.instance.hasEnchant(mainHandItem)){//prickle enchant on weapon
                if(prickleProcessing.contains(target.getUniqueId())){// prevent recussion
                    return;
                }
                prickleProcessing.add(target.getUniqueId());
                int cacterLvl = CustomEnchantsMain.Enchant.Prickle.instance.getLevel(mainHandItem);
                int cacterDmg = BoxPlugin.instance.getEnchantPrickle().getDamageFromTotalLevel(cacterLvl);
                Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + target.getName());
                target.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
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
                    attacker.damage(cacterDmg, DamageSource.builder(DamageType.CACTUS).withCausingEntity(p).withDirectEntity(p).build());
                    Util.debug(p, "dealt " + (cacterDmg) + " bonus damage to " + attacker.getName());
                    attacker.setNoDamageTicks(0);
                    brambleProcessing.remove(attacker.getUniqueId());
                }
            }
        }
    }
}
