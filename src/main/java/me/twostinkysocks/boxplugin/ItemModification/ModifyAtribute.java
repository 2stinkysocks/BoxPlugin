package me.twostinkysocks.boxplugin.ItemModification;

import com.google.common.collect.Multimap;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.manager.GearScoreManager;
import net.minecraft.world.item.TridentItem;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ModifyAtribute {

    public static final NamespacedKey MAX_HEALTH_KEY = new NamespacedKey(BoxPlugin.instance, "max_health");
    public static final NamespacedKey FLAT_ATTACKSPEED_KEY = new NamespacedKey(BoxPlugin.instance, "flat_attackspeed");
    public static final NamespacedKey PERCENT_ATTACKSPEED_KEY = new NamespacedKey(BoxPlugin.instance, "percent_attackspeed");
    public static final NamespacedKey FLAT_DAMAGE_KEY = new NamespacedKey(BoxPlugin.instance, "flat_damage");
    public static final NamespacedKey PERCENT_DAMAGE_KEY = new NamespacedKey(BoxPlugin.instance, "percent_damage");
    public static final NamespacedKey ATTACK_REACH_KEY = new NamespacedKey(BoxPlugin.instance, "attack_reach");
    public static final NamespacedKey BLOCK_REACH_KEY = new NamespacedKey(BoxPlugin.instance, "block_reach");
    public static final NamespacedKey PERCENT_MOVE_SPEED_KEY = new NamespacedKey(BoxPlugin.instance, "move_speed");
    public static final NamespacedKey ARMOR_KEY = new NamespacedKey(BoxPlugin.instance, "max_armor");
    public static final NamespacedKey ARMOR_TUFFNESS_KEY = new NamespacedKey(BoxPlugin.instance, "max_armortoughness");
    public static final NamespacedKey KB_RES_KEY = new NamespacedKey(BoxPlugin.instance, "knockback_res");
    public static final NamespacedKey PERCENT_SCALE_KEY = new NamespacedKey(BoxPlugin.instance, "percent_scale");
    public static final NamespacedKey WATER_MINE_KEY = new NamespacedKey(BoxPlugin.instance, "water_mine");
    public static final NamespacedKey PERCENT_GRAVITY_KEY = new NamespacedKey(BoxPlugin.instance, "percent_gravity");
    public static final NamespacedKey SAFE_FALL_KEY = new NamespacedKey(BoxPlugin.instance, "safe_fall");

    public static ItemMeta RemoveAttribute(@NotNull ItemMeta item, Attribute attributeToRemove){//removes a attribute to update its value
        item.removeAttributeModifier(attributeToRemove);
        return item;
    }

    @Contract(pure = true)
    public static @NotNull AttributeModifier getNewItemModifier(NamespacedKey itemKey, double ammount, AttributeModifier.Operation operation, EquipmentSlotGroup slot){
        AttributeModifier newModifier = new AttributeModifier(itemKey, ammount, operation, slot);
        return newModifier;
    }

    public static @NotNull ItemStack ModifyGearScore(ItemStack item, int score){
        score = GearScoreManager.GetGearScore(item) + score;
        item = GearScoreManager.setGearScore(item, score);

        return item;
    }

    public static @NotNull ItemStack ModifyFlatAttackSpeed(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentAttackSpeed = -2.4;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ATTACK_SPEED)){
            Collection<AttributeModifier> attackSpeedModifier = itemAttributes.get(Attribute.ATTACK_SPEED);
            for(AttributeModifier modifier : attackSpeedModifier){
                currentAttackSpeed = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ATTACK_SPEED);//if it currently has attack speed
        }

        AttributeModifier speedModifier = getNewItemModifier(FLAT_ATTACKSPEED_KEY, (currentAttackSpeed + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentAttackSpeed(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentAttackSpeed = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ATTACK_SPEED)){
            Collection<AttributeModifier> attackSpeedModifier = itemAttributes.get(Attribute.ATTACK_SPEED);
            for(AttributeModifier modifier : attackSpeedModifier){
                currentAttackSpeed = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ATTACK_SPEED);//if it currently has attack speed
        }

        AttributeModifier speedModifier = getNewItemModifier(PERCENT_ATTACKSPEED_KEY, (currentAttackSpeed + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatAttackDmg(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentAttackDmg = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ATTACK_DAMAGE)){
            Collection<AttributeModifier> attackDamageModifier = itemAttributes.get(Attribute.ATTACK_DAMAGE);
            for(AttributeModifier modifier : attackDamageModifier){
                currentAttackDmg = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ATTACK_DAMAGE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(FLAT_DAMAGE_KEY, (currentAttackDmg + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentAttackDmg(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentAttackDmg = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ATTACK_DAMAGE)){
            Collection<AttributeModifier> attackDamageModifier = itemAttributes.get(Attribute.ATTACK_DAMAGE);
            for(AttributeModifier modifier : attackDamageModifier){
                currentAttackDmg = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ATTACK_DAMAGE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(PERCENT_DAMAGE_KEY, (currentAttackDmg + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatAttackReach(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentAttackreach = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ENTITY_INTERACTION_RANGE)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.ENTITY_INTERACTION_RANGE);
            for(AttributeModifier modifier : attModifier){
                currentAttackreach = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ENTITY_INTERACTION_RANGE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(ATTACK_REACH_KEY, (currentAttackreach + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatBlockReach(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentBlockReach = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.BLOCK_INTERACTION_RANGE)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.BLOCK_INTERACTION_RANGE);
            for(AttributeModifier modifier : attModifier){
                currentBlockReach = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.BLOCK_INTERACTION_RANGE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(BLOCK_REACH_KEY, (currentBlockReach + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentMoveSpeed(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double currentMoveSpeed = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.MOVEMENT_SPEED)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.MOVEMENT_SPEED);
            for(AttributeModifier modifier : attModifier){
                currentMoveSpeed = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.MOVEMENT_SPEED);//if it currently has speed
        }

        AttributeModifier newAttModifier = getNewItemModifier(PERCENT_MOVE_SPEED_KEY, (currentMoveSpeed + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.MOVEMENT_SPEED, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatArmorToughness(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double armorTuffness = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ARMOR_TOUGHNESS)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.ARMOR_TOUGHNESS);
            for(AttributeModifier modifier : attModifier){
                armorTuffness = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ARMOR_TOUGHNESS);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(ARMOR_TUFFNESS_KEY, (armorTuffness + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatArmor(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double armor = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.ARMOR)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.ARMOR);
            for(AttributeModifier modifier : attModifier){
                armor = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.ARMOR);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(ARMOR_KEY, (armor + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.ARMOR, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatHealth(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double maxHP = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.MAX_HEALTH)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.MAX_HEALTH);
            for(AttributeModifier modifier : attModifier){
                maxHP = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.MAX_HEALTH);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(MAX_HEALTH_KEY, (maxHP + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.MAX_HEALTH, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatKBResistance(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double kbRes = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.KNOCKBACK_RESISTANCE)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.KNOCKBACK_RESISTANCE);
            for(AttributeModifier modifier : attModifier){
                kbRes = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.KNOCKBACK_RESISTANCE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(KB_RES_KEY, (kbRes + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentWaterMineSpeed(@NotNull ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double underWataMineSpeed = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.SUBMERGED_MINING_SPEED)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.SUBMERGED_MINING_SPEED);
            for(AttributeModifier modifier : attModifier){
                underWataMineSpeed = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.SUBMERGED_MINING_SPEED);//if it currently has this attributed
        }

        AttributeModifier newAttModifier = getNewItemModifier(WATER_MINE_KEY, (underWataMineSpeed + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.SUBMERGED_MINING_SPEED, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentScale(ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double scaleDiff = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.SCALE)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.SCALE);
            for(AttributeModifier modifier : attModifier){
                scaleDiff = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.SCALE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(PERCENT_SCALE_KEY, (scaleDiff + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.SCALE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyFlatSafeFallDistance(ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing attack speed on a item
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double fallDistance = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.SAFE_FALL_DISTANCE)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.SAFE_FALL_DISTANCE);
            for(AttributeModifier modifier : attModifier){
                fallDistance = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.SAFE_FALL_DISTANCE);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(SAFE_FALL_KEY, (fallDistance + ammount), AttributeModifier.Operation.ADD_NUMBER, slot);

        itemMeta.addAttributeModifier(Attribute.SAFE_FALL_DISTANCE, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack ModifyPercentGravity(ItemStack item, double ammount, EquipmentSlotGroup slot){//chanegs existing gravity
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        double fallDistance = 0;
        Multimap<Attribute, AttributeModifier> itemAttributes= itemMeta.getAttributeModifiers();//gets the items current attributes
        if(itemAttributes != null && itemAttributes.containsKey(Attribute.GRAVITY)){
            Collection<AttributeModifier> attModifier = itemAttributes.get(Attribute.GRAVITY);
            for(AttributeModifier modifier : attModifier){
                fallDistance = modifier.getAmount();
            }
            itemMeta = RemoveAttribute(itemMeta, Attribute.GRAVITY);//if it currently has this attribute
        }

        AttributeModifier newAttModifier = getNewItemModifier(PERCENT_GRAVITY_KEY, (fallDistance + ammount), AttributeModifier.Operation.ADD_SCALAR, slot);

        itemMeta.addAttributeModifier(Attribute.GRAVITY, newAttModifier);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack modifyEnchant(ItemStack item, Enchantment enchant, int ammount){
        if(item.containsEnchantment(enchant)){
            ammount += item.getEnchantmentLevel(enchant);
        }
        item.addUnsafeEnchantment(enchant, ammount);
        return item;
    }

}
