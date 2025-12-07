package me.twostinkysocks.boxplugin.ItemModification;

import java.sql.*;
import java.util.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.manager.GearScoreManager;
import me.twostinkysocks.boxplugin.manager.ItemLivesManager;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.bridge.RegistryType;

public class RegisteredItem {
    private Connection connection;
    private static final Gson gson = new Gson();
    public Multimap<Attribute, AttributeModifier> attributeMap = ArrayListMultimap.create();
    public Map<Enchantment, Integer> enchantHashMap = new HashMap<Enchantment, Integer>();
    public String name = null;
    public String registeredName = null;
    public String[] lore = null;
    public Material itemModel = null;
    public int gearScore = 0;
    public boolean isUnbreakable = false;
    public boolean hasHiddenEnchants = false;
    public boolean hasHiddenAttributes = false;
    public Color itemColor = null;
    public ArmorTrim armorTrim = null;
    public TrimMaterial trimMaterial = null;
    public TrimPattern trimPattern = null;

    //primitive for database:

    private static class AttributeData{
        String attributeModifierKey = "";
        String attributeType = "";
        Double attributeValue;
        String attributeOperation = "";
        String attributeSlot = "";
    }
    private static class RegisteredItemData {
        // attributes as JSON-string, enchants as simple map
        ArrayList<AttributeData> attributesList = new ArrayList<>();
        Map<String, Integer> serializedEnchantsMap = new HashMap<>();
        String itemName = null;
        String[] lore = null;
        String itemType = null;   // Material.name()
        String armorTrimPattern = null;
        String armorTrimMat = null;
        Map<String, Object> itemColor;
        int gearScore = 0;
        boolean isUnbreakable = false;
        boolean isTrimmed = false;
        boolean isColrable = false;
        boolean hideEnchants = false;
        boolean hideAttributes = false;
    }

    public boolean isArmor(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        return meta instanceof org.bukkit.inventory.meta.ArmorMeta;
    }
    public boolean isColorable(ArmorMeta armorMeta){
        return armorMeta instanceof org.bukkit.inventory.meta.ColorableArmorMeta;
    }

    public void setConnection() throws SQLException{
        String url = "jdbc:sqlite:" + BoxPlugin.instance.getDataFolder() + "/registeredItems.db";
        connection = DriverManager.getConnection(url);

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS registered_items (
                    registered_name TEXT UNIQUE NOT NULL,
                    data_json TEXT NOT NULL
                )
                """);
        }
    }

    public void CloseConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    public void UpdateDataBase() throws SQLException {
        setConnection();
        RegisteredItemData newItem = new RegisteredItemData();
        //set all data types to prepare for json
        if(newItem.itemColor != null){
            newItem.itemColor.clear();
        }
        if(newItem.attributesList != null){
            newItem.attributesList.clear();
        }
        if(newItem.serializedEnchantsMap != null){
            newItem.serializedEnchantsMap.clear();
        }

        newItem.itemType = itemModel.name();
        newItem.gearScore = gearScore;
        newItem.lore = lore;
        newItem.itemName = name;
        newItem.isUnbreakable = isUnbreakable;

        for(Map.Entry<Enchantment, Integer> entry : enchantHashMap.entrySet()){
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            String enchantKey = enchant.getKey().toString();

            newItem.serializedEnchantsMap.put(enchantKey, level);
        }
        //set our attribute map
        for(Map.Entry<Attribute, AttributeModifier> entry : attributeMap.entries()){
            Attribute attribute = entry.getKey();
            AttributeModifier attributeModifier = entry.getValue();
            AttributeModifier.Operation operation = attributeModifier.getOperation();
            EquipmentSlotGroup slot = attributeModifier.getSlotGroup();

            AttributeData currentData = new AttributeData();

            currentData.attributeType = attribute.name();
            currentData.attributeModifierKey = attributeModifier.getKey().getKey();
            currentData.attributeValue = attributeModifier.getAmount();
            currentData.attributeOperation = operation.name();
            currentData.attributeSlot = (slot == null ? "" : slot.toString());

            newItem.attributesList.add(currentData);
        }

        if(armorTrim != null){
            newItem.armorTrimPattern = trimPattern.getKey().toString();
            newItem.armorTrimMat = trimMaterial.getKey().toString();
            newItem.isTrimmed = true;
        }

        if(itemColor != null){
            newItem.itemColor = itemColor.serialize();
            newItem.isColrable = true;
        }

        newItem.hideAttributes = hasHiddenAttributes;
        newItem.hideEnchants = hasHiddenEnchants;

        String itemJson = gson.toJson(newItem);

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO registered_items (registered_name, data_json) VALUES (?, ?)"
        )) {
            ps.setString(1, registeredName);
            ps.setString(2, itemJson);
            ps.executeUpdate();
        }
        CloseConnection();
    }

    public boolean RemoveFromDatabase(String registeredName) throws SQLException {//clanker made this one, removes item from database based on name
        setConnection(); // ensure connection is set
        int affected = 0;
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM registered_items WHERE registered_name = ?")) {
            ps.setString(1, registeredName);
            affected = ps.executeUpdate();
        }
        CloseConnection();
        return affected > 0; // true if something was deleted
    }

    public ArrayList<String> GetAllRegisteredNames() throws SQLException {
        setConnection();
        ArrayList<String> registeredNameList = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT registered_name FROM registered_items")) {

            while (rs.next()) {
                registeredNameList.add(rs.getString("registered_name"));
            }
        } finally {
            CloseConnection();
        }

        return registeredNameList;
    }

    public void RegisterItem(@NotNull ItemStack item, String nameToSavAs) throws SQLException {//item cannot be null, check if a item is held in hand before calling
        ItemMeta itemMeta = item.getItemMeta();

        enchantHashMap.clear();
        if(itemMeta.hasEnchants()){
            enchantHashMap.putAll(itemMeta.getEnchants());
        }
        attributeMap.clear();
        if(itemMeta.hasAttributeModifiers()){
            attributeMap.putAll(itemMeta.getAttributeModifiers());
        }

        registeredName = nameToSavAs;
        name = itemMeta.getDisplayName();
        itemModel = item.getType();

        if(itemMeta.hasLore()){
            lore = itemMeta.getLore().toArray(new String[0]);
        }
        if(GearScoreManager.HasGearScore(item)){
            gearScore = GearScoreManager.GetGearScore(item);
        }
        if(itemMeta.isUnbreakable()){
            isUnbreakable = true;
        }
        if(isArmor(item)){
            ArmorMeta armorMeta = (ArmorMeta) itemMeta;
            if(isColorable(armorMeta)){
                ColorableArmorMeta coloredArmorMeta = (ColorableArmorMeta) armorMeta;
                itemColor = coloredArmorMeta.getColor();
            }
            if(armorMeta.hasTrim()){
                armorTrim = armorMeta.getTrim();
                trimMaterial = armorTrim.getMaterial();
                trimPattern = armorTrim.getPattern();
            }
        }

        if(itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)){
            hasHiddenEnchants = true;
        }
        if(itemMeta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)){
            hasHiddenAttributes = true;
        }

        UpdateDataBase();
    }

    public boolean ExistsInDatabase(String registeredName) throws SQLException {
        setConnection(); // ensure connection is set
        String query = "SELECT 1 FROM registered_items WHERE registered_name = ? LIMIT 1";
        boolean answer;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, registeredName);
            try (ResultSet rs = ps.executeQuery()) {
                answer = rs.next();
            }
        }
        CloseConnection();
        return answer;
    }

    public boolean IsRegistered(@NotNull ItemMeta item){

        NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        PersistentDataContainer itemData = item.getPersistentDataContainer();

        if(itemData.has(key)){
            return true;
        }
        return false;//is not currently registered to a item preset
    }

    public ItemMeta RemoveRegisteredToItemValue(@NotNull ItemMeta item){
        NamespacedKey keyRegister = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        NamespacedKey keyReforge = new NamespacedKey(BoxPlugin.instance, "isReforged");
        NamespacedKey keyReskin = new NamespacedKey(BoxPlugin.instance, "isReskinned");

        item.getPersistentDataContainer().remove(keyReforge);
        item.getPersistentDataContainer().remove(keyRegister);
        item.getPersistentDataContainer().remove(keyReskin);

        return item;
    }

    public ItemStack SetItemToBelongToRegisteredItem(@NotNull ItemStack item, String registryName) throws SQLException {
        ItemMeta itemMeta = item.getItemMeta();

        if(ExistsInDatabase(registryName)){//if there is a database for this item name
            if(IsRegistered(itemMeta)){//remove old register key if it exists
                itemMeta = RemoveRegisteredToItemValue(itemMeta);
            }
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "isReforged"), PersistentDataType.BOOLEAN, false);
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "isReskinned"), PersistentDataType.BOOLEAN, false);
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "belongsToParentItem"), PersistentDataType.STRING, registryName);
        }
        else {
            System.out.println("Failed to register:" + registryName + "is not found in the data base");
            return item;
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack RemoveItemFromBelongingToParent(@NotNull ItemStack item) throws SQLException {
        ItemMeta itemMeta = item.getItemMeta();

        if(IsRegistered(itemMeta)){//remove old register key if it exists
            itemMeta = RemoveRegisteredToItemValue(itemMeta);
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemMeta SetReforgedStatus(@NotNull ItemMeta itemMeta, boolean status){

        itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "isReforged"), PersistentDataType.BOOLEAN, status);
        return itemMeta;
    }

    public ItemStack SetReskinnedStatus(@NotNull ItemStack item, boolean status){
        ItemMeta itemMeta = item.getItemMeta();
        if(!IsRegistered(itemMeta)){
            return item;
        }
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "isReskinned"), PersistentDataType.BOOLEAN, status);
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack SetToRegisteredItem(@NotNull ItemStack item) throws SQLException {//will only be called on items that have the appropriate tags

        if(enchantHashMap != null){
            enchantHashMap.clear();
        }
        if(attributeMap != null){
            attributeMap.clear();
        }
        if(lore != null){
            lore = null;
        }

        NamespacedKey keyReforge = new NamespacedKey(BoxPlugin.instance, "isReforged");
        NamespacedKey keyReskin = new NamespacedKey(BoxPlugin.instance, "isReskinned");

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

        if(itemData.getOrDefault(keyReforge, PersistentDataType.BOOLEAN, false)){//if reforged then ignore
            return item;
        }
        NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        String registeredType = itemData.get(key, PersistentDataType.STRING);
        setConnection();
        Bukkit.getLogger().info("SetToRegisteredItem: registeredType=" + registeredType);

        RegisteredItemData registeredItem;

        try (PreparedStatement ps = connection.prepareStatement("SELECT data_json FROM registered_items WHERE registered_name = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
            ps.setString(1, registeredType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String jsonData = rs.getString("data_json");
                    registeredItem = gson.fromJson(jsonData, RegisteredItemData.class);
                } else {
                    CloseConnection();
                    return item; // item not found
                }
            }
        }
        gearScore = registeredItem.gearScore;
        List<String> lore = registeredItem.lore != null ? new ArrayList<>(Arrays.asList(registeredItem.lore)) : new ArrayList<>();

        if(BoxPlugin.instance.getItemLivesManager().hasLives(item)) {
            int numLives = BoxPlugin.instance.getItemLivesManager().getLives(item);
            lore.addAll(List.of("", ChatColor.RED + "" + (numLives) + " Lives"));
        }
        name = registeredItem.itemName;
        itemModel = Material.getMaterial(registeredItem.itemType);

        enchantHashMap.clear();
        for(Map.Entry<String, Integer> entry : registeredItem.serializedEnchantsMap.entrySet()){
            NamespacedKey enchantKey = NamespacedKey.fromString(entry.getKey());
            Enchantment enchant = Enchantment.getByKey(enchantKey);

            if(enchant != null){
                enchantHashMap.put(enchant, entry.getValue());
            }
        }
        for(AttributeData attributeData : registeredItem.attributesList){
            Attribute attribute = Attribute.valueOf(attributeData.attributeType);

            NamespacedKey attributeKey = new NamespacedKey("minecraft", attributeData.attributeModifierKey);
            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(attributeData.attributeOperation);
            EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(attributeData.attributeSlot);

            AttributeModifier attributeModifier = new AttributeModifier(attributeKey, attributeData.attributeValue, operation, slot);

            attributeMap.put(attribute, attributeModifier);
        }

        if(!lore.isEmpty()){
            itemMeta.setLore(lore);
        } else {
            itemMeta.setLore(null);
        }

        itemMeta.setAttributeModifiers(attributeMap);
        itemMeta.setDisplayName(Util.colorize(name));
        if(!itemData.getOrDefault(keyReskin, PersistentDataType.BOOLEAN, false)){//ignore item model if its reskinned
            item.setType(itemModel);
        }

        for(Enchantment enchant : itemMeta.getEnchants().keySet()){//removes current items enchants
            itemMeta.removeEnchant(enchant);
        }

        if(itemMeta instanceof ArmorMeta){
            if(registeredItem.isTrimmed){
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;

                NamespacedKey patternKey = NamespacedKey.fromString(registeredItem.armorTrimPattern);
                NamespacedKey matKey = NamespacedKey.fromString(registeredItem.armorTrimMat);

                trimPattern = RegistryType.TRIM_PATTERN.getRegistry().get(patternKey);
                trimMaterial = RegistryType.TRIM_MATERIAL.getRegistry().get(matKey);
                armorTrim = new ArmorTrim(trimMaterial, trimPattern);
                armorMeta.setTrim(armorTrim);

                itemMeta = armorMeta;
            }

            if(registeredItem.isColrable && itemMeta instanceof ColorableArmorMeta){
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;

                ColorableArmorMeta coloredArmorMeta = (ColorableArmorMeta) armorMeta;
                itemColor = Color.deserialize(registeredItem.itemColor);
                coloredArmorMeta.setColor(itemColor);

                itemMeta = armorMeta;
            }
        }

        itemMeta.setUnbreakable(registeredItem.isUnbreakable);

        if(registeredItem.hideEnchants){
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if(registeredItem.hideAttributes){
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantments(enchantHashMap);
        item = GearScoreManager.setGearScore(item, gearScore);
        CloseConnection();
        return item;
    }

    public boolean UpdateLegacyItems(@NotNull Player p) throws SQLException {
        ItemStack item;
        boolean fixedItems = false;

        item = p.getInventory().getHelmet();//next for are amor slots
        if(item != null && item.hasItemMeta()){
            name = item.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

            if (!IsRegistered(item.getItemMeta()) && ExistsInDatabase(name)){
                item = SetItemToBelongToRegisteredItem(item, name);
                item = SetToRegisteredItem(item);
                p.getInventory().setHelmet(item);
                fixedItems = true;
            }
        }

        item = p.getInventory().getChestplate();
        if(item != null && item.hasItemMeta()){
            name = item.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

            if (!IsRegistered(item.getItemMeta()) && ExistsInDatabase(name)){
                item = SetItemToBelongToRegisteredItem(item, name);
                item = SetToRegisteredItem(item);
                p.getInventory().setChestplate(item);
                fixedItems = true;
            }
        }

        item = p.getInventory().getLeggings();
        if(item != null && item.hasItemMeta()){
            name = item.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

            if (!IsRegistered(item.getItemMeta()) && ExistsInDatabase(name)){
                item = SetItemToBelongToRegisteredItem(item, name);
                item = SetToRegisteredItem(item);
                p.getInventory().setLeggings(item);
                fixedItems = true;
            }
        }

        item = p.getInventory().getBoots();
        if(item != null && item.hasItemMeta()){
            name = item.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

            if (!IsRegistered(item.getItemMeta()) && ExistsInDatabase(name)){
                item = SetItemToBelongToRegisteredItem(item, name);
                item = SetToRegisteredItem(item);
                p.getInventory().setBoots(item);
                fixedItems = true;
            }
        }

        item = p.getInventory().getItemInOffHand();//offhand item
        if(item != null && item.hasItemMeta()){
            name = item.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

            if (!IsRegistered(item.getItemMeta()) && ExistsInDatabase(name)){
                item = SetItemToBelongToRegisteredItem(item, name);
                item = SetToRegisteredItem(item);
                p.getInventory().setItemInOffHand(item);
                fixedItems = true;
            }
        }

        int index = 0;
        ItemStack[] contents = p.getInventory().getContents();
        for(ItemStack inventoryItem : p.getInventory().getContents()) {//everything else
            if(inventoryItem != null && inventoryItem.hasItemMeta()){
                name = inventoryItem.getItemMeta().getDisplayName().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");

                if (!IsRegistered(inventoryItem.getItemMeta()) && ExistsInDatabase(name)){
                    inventoryItem = SetItemToBelongToRegisteredItem(inventoryItem, name);
                    inventoryItem = SetToRegisteredItem(inventoryItem);
                    fixedItems = true;
                }
            }

            contents[index] = inventoryItem;
            index ++;
        }
        p.getInventory().setContents(contents);
        return fixedItems;
    }

    public void UpdateCurrentItems(@NotNull Player p) throws SQLException {
        ItemStack item;

        item = p.getInventory().getHelmet();//next for are amor slots
        if(item != null && item.hasItemMeta()){

            if (IsRegistered(item.getItemMeta())){
                item = SetToRegisteredItem(item);
                p.getInventory().setHelmet(item);
            }
        }

        item = p.getInventory().getChestplate();
        if(item != null && item.hasItemMeta()){

            if (IsRegistered(item.getItemMeta())){
                item = SetToRegisteredItem(item);
                p.getInventory().setChestplate(item);
            }
        }

        item = p.getInventory().getLeggings();
        if(item != null && item.hasItemMeta()){

            if (IsRegistered(item.getItemMeta())){
                item = SetToRegisteredItem(item);
                p.getInventory().setLeggings(item);
            }
        }

        item = p.getInventory().getBoots();
        if(item != null && item.hasItemMeta()){

            if (IsRegistered(item.getItemMeta())){
                item = SetToRegisteredItem(item);
                p.getInventory().setBoots(item);
            }
        }

        item = p.getInventory().getItemInOffHand();//offhand item
        if(item != null && item.hasItemMeta()){

            if (IsRegistered(item.getItemMeta())){
                item = SetToRegisteredItem(item);
                p.getInventory().setItemInOffHand(item);
            }
        }

        int index = 0;
        ItemStack[] contents = p.getInventory().getContents();
        for(ItemStack inventoryItem : p.getInventory().getContents()) {//everything else
            if(inventoryItem != null && inventoryItem.hasItemMeta()){

                if (IsRegistered(inventoryItem.getItemMeta())){
                    inventoryItem = SetToRegisteredItem(inventoryItem);
                }
            }

            contents[index] = inventoryItem;
            index ++;
        }
        p.getInventory().setContents(contents);
    }
}
