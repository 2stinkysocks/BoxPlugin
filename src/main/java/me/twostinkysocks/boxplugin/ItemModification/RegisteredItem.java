package me.twostinkysocks.boxplugin.ItemModification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.LinkedHashMultimap;
import com.google.gson.Gson;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customEnchants.CustomEnchantsMain;
import me.twostinkysocks.boxplugin.manager.GearScoreManager;
import me.twostinkysocks.boxplugin.manager.MarketManager;
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
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.bridge.RegistryType;

public class RegisteredItem {
    private Connection connection;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private static final Gson gson = new Gson();
    public List<AttributeData> attributeList = new ArrayList<>();
    public Map<Enchantment, Integer> enchantHashMap = new HashMap<Enchantment, Integer>();
    public String name = null;
    public String registeredName = null;
    public String[] lore = null;
    public Material itemModel = null;
    public int gearScore = 0;
    public boolean isUnbreakable = false;
    public boolean hasHiddenEnchants = false;
    public boolean hasHiddenAttributes = false;
    public boolean hasCustomEnchants = false;
    public boolean isCustomItem = false;
    public boolean isTrimmed = false;
    public boolean isColrable = false;
    public String customItemName = null;
    public ArrayList<CustomEnchantsMain.Enchant> customEnchList = new ArrayList<>();
    public Color itemColor = null;
    public ArmorTrim armorTrim = null;
    public TrimMaterial trimMaterial = null;
    public TrimPattern trimPattern = null;

    //primitive for database:

    static class AttributeData{
        String attributeModifierKey = "";
        String attributeType = "";
        Double attributeValue;
        String attributeOperation = "";
        String attributeSlot = "";
    }
    static class RegisteredItemData {
        // attributes as JSON-string, enchants as simple map
        ArrayList<AttributeData> attributesList = new ArrayList<>();
        Map<String, Integer> serializedEnchantsMap = new HashMap<>();

        Map<String, Integer> serializedCustomEnchantsMap = new HashMap<>();
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
        boolean hasCustomEnchants = false;
        boolean isCustomItem = false;
        String customItemName = null;
    }

    public boolean isArmor(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        return meta instanceof ArmorMeta;
    }
    public boolean isColorable(ArmorMeta armorMeta){
        return armorMeta instanceof ColorableArmorMeta;
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

    public void UpdateDataBase(ItemStack item) throws SQLException {
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
        if(newItem.serializedCustomEnchantsMap != null){
            newItem.serializedCustomEnchantsMap.clear();
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

        newItem.attributesList = (ArrayList<AttributeData>) attributeList;

        if(armorTrim != null){
            newItem.armorTrimPattern = trimPattern.getKey().toString();
            newItem.armorTrimMat = trimMaterial.getKey().toString();
            newItem.isTrimmed = true;
        }

        if(itemColor != null){
            newItem.itemColor = itemColor.serialize();
            newItem.isColrable = true;
        }
        if(hasCustomEnchants){
            for(CustomEnchantsMain.Enchant entry : customEnchList.stream().toList()){
                int customEnchLvl = entry.instance.getLevel(item);

                String customEnchName = entry.instance.getEnchantName();

                newItem.serializedCustomEnchantsMap.put(customEnchName, customEnchLvl);
            }
            newItem.hasCustomEnchants = true;
        }
        if(this.isCustomItem){
            newItem.isCustomItem = true;
            newItem.customItemName = this.customItemName;
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
//        setConnection(); // ensure connection is set
      AtomicInteger affected = new AtomicInteger();
//        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM registered_items WHERE registered_name = ?")) {
//            ps.setString(1, registeredName);
//            affected.set(ps.executeUpdate());
//        }
//        CloseConnection();
        dbExecutor.execute(() -> {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + BoxPlugin.instance.getDataFolder() + "/registeredItems.db");
                 PreparedStatement ps = c.prepareStatement("DELETE FROM registered_items WHERE registered_name = ?")) {

                ps.setString(1, registeredName);
                affected.set(ps.executeUpdate());

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return affected.get() > 0; // true if something was deleted
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

    public void RegisterItemLegacy(@NotNull ItemStack item, String nameToSavAs) throws SQLException {//item cannot be null, check if a item is held in hand before calling

        ItemMeta itemMeta = item.getItemMeta();

        enchantHashMap.clear();
        if(itemMeta.hasEnchants()){
            enchantHashMap.putAll(itemMeta.getEnchants());
        }

        if(attributeList != null){
            attributeList.clear();
        }
        if(itemMeta.hasAttributeModifiers()){
            for (Map.Entry<Attribute, AttributeModifier> entry : itemMeta.getAttributeModifiers().entries()) {
                AttributeModifier modifier = entry.getValue();
                AttributeData data = new AttributeData();
                data.attributeType = entry.getKey().name();
                data.attributeModifierKey = modifier.getKey().getKey();
                data.attributeValue = modifier.getAmount();
                data.attributeOperation = modifier.getOperation().name();
                data.attributeSlot = modifier.getSlotGroup() == null ? "" : modifier.getSlotGroup().toString();
                attributeList.add(data);
            }
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
        } else {
            isUnbreakable = false;
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
        if(BoxPlugin.instance.getCustomEnchantsMain().hasCustomEnchants(item)){
            hasCustomEnchants = true;
            customEnchList = BoxPlugin.instance.getCustomEnchantsMain().getCustomEnchantsOnItem(item);
        }
        if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING)) {
            this.isCustomItem = true;
            this.customItemName = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING);
        }

        UpdateDataBase(item);
    }

    public ItemStack SetToRegisteredItemLegcay(@NotNull ItemStack item) throws SQLException {//will only be called on items that have the appropriate tags

        if(enchantHashMap != null){
            enchantHashMap.clear();
        }
        if(attributeList != null){
            attributeList.clear();
        }
        if(lore != null){
            lore = null;
        }
        if(armorTrim != null){
            armorTrim = null;
        }
        if(trimMaterial != null){
            trimMaterial = null;
        }
        if(trimPattern != null){
            trimPattern = null;
        }

        NamespacedKey keyReforge = new NamespacedKey(BoxPlugin.instance, "isReforged");
        NamespacedKey keyReskin = new NamespacedKey(BoxPlugin.instance, "isReskinned");

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

        if(itemData.getOrDefault(keyReforge, PersistentDataType.BOOLEAN, false)){//if reforged then ignore
            return item;
        }
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
            return item;//dont update ghost items
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
                    Bukkit.getLogger().info("item was not found in database: " + registeredType);
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

        LinkedHashMultimap<Attribute, AttributeModifier> attributeHashMap = LinkedHashMultimap.create();

        for(AttributeData attributeData : registeredItem.attributesList){
            Attribute attribute = Attribute.valueOf(attributeData.attributeType);

            NamespacedKey attributeKey = new NamespacedKey("minecraft", attributeData.attributeModifierKey);
            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(attributeData.attributeOperation);
            EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(attributeData.attributeSlot);

            AttributeModifier attributeModifier = new AttributeModifier(attributeKey, attributeData.attributeValue, operation, slot);

            attributeHashMap.put(attribute, attributeModifier);
        }


        itemMeta.setAttributeModifiers(attributeHashMap);
        if(!itemData.getOrDefault(keyReskin, PersistentDataType.BOOLEAN, false)){//ignore item model, lore, and name if its reskinned
            item.setType(itemModel);
            itemMeta.setDisplayName(Util.colorize(name));
            if(!lore.isEmpty()){
                itemMeta.setLore(lore);
            } else {
                itemMeta.setLore(null);
            }
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
            } else{ //remove old unused trims
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;
                if(armorMeta.hasTrim()){
                    armorMeta.setTrim(null);
                }
            }

            if(registeredItem.isColrable && itemMeta instanceof ColorableArmorMeta){
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;

                ColorableArmorMeta coloredArmorMeta = (ColorableArmorMeta) armorMeta;
                itemColor = Color.deserialize(registeredItem.itemColor);
                coloredArmorMeta.setColor(itemColor);

                itemMeta = armorMeta;
            }
        }

        if(registeredItem.isCustomItem){
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING, registeredItem.customItemName);
        }

        itemMeta.setUnbreakable(registeredItem.isUnbreakable);

        itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantments(enchantHashMap);

        ItemMeta itemMetaFlags = item.getItemMeta();//must redo this to add item flags after other things

        itemMetaFlags.setUnbreakable(registeredItem.isUnbreakable);

        itemMetaFlags.removeItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        if(registeredItem.hideEnchants){
            itemMetaFlags.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if(registeredItem.hideAttributes){
            itemMetaFlags.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(itemMetaFlags);

        item = GearScoreManager.setGearScore(item, gearScore);
        if(registeredItem.hasCustomEnchants){
            for(Map.Entry<String, Integer> entry : registeredItem.serializedCustomEnchantsMap.entrySet()){
                item = BoxPlugin.instance.getCustomEnchantsMain().setCustomEnchant(item, entry.getKey(), entry.getValue());
            }
        }
        CloseConnection();
        return item;
    }
    //new method for deep copies
    public void RegisterItem(@NotNull ItemStack item, String nameToSavAs) throws SQLException, IOException {//item cannot be null, check if a item is held in hand before calling
        //setConnection();
        String base64ItemData;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();//clanker logic to deep copy item data
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {

            oos.writeObject(item);

            base64ItemData = Base64.getEncoder().encodeToString(baos.toByteArray());
        }

        dbExecutor.execute(() -> {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + BoxPlugin.instance.getDataFolder() + "/registeredItems.db");
                 PreparedStatement ps = c.prepareStatement("INSERT INTO registered_items (registered_name, data_json) VALUES (?, ?)")) {

                ps.setString(1, nameToSavAs);
                ps.setString(2, base64ItemData);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
//        try (PreparedStatement ps = connection.prepareStatement(
//                "INSERT OR REPLACE INTO registered_items (registered_name, data_json) VALUES (?, ?)"
//        )) {
//            ps.setString(1, nameToSavAs);
//            ps.setString(2, base64ItemData);
//            ps.executeUpdate();
//        }
        //CloseConnection();
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
    //new method for deep copies
    public ItemStack SetToRegisteredItem(@NotNull ItemStack item) throws SQLException, IOException, ClassNotFoundException {//will only be called on items that have the appropriate tags

        NamespacedKey keyReforge = new NamespacedKey(BoxPlugin.instance, "isReforged");
        NamespacedKey keyReskin = new NamespacedKey(BoxPlugin.instance, "isReskinned");
        boolean isReskinned = false;

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

        if(itemData.getOrDefault(keyReforge, PersistentDataType.BOOLEAN, false)){//if reforged then ignore
            return item;
        }
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
            return item;//dont update ghost items
        }

        boolean reforgedStatus = itemData.getOrDefault(keyReforge, PersistentDataType.BOOLEAN, false);
        boolean reskinnedStatus = itemData.getOrDefault(keyReskin, PersistentDataType.BOOLEAN, false);
        boolean hadLives = false;
        int numLives = 0;
        if(BoxPlugin.instance.getItemLivesManager().hasLives(item)){
            hadLives = true;
            numLives = BoxPlugin.instance.getItemLivesManager().getLives(item);
        }

        if(itemMeta.getPersistentDataContainer().getOrDefault(keyReskin, PersistentDataType.BOOLEAN, true)){//if reskinned then save its reskin data to re apply after
            this.name = itemMeta.getDisplayName();
            this.itemModel = item.getType();
            isReskinned = true;

            if(itemMeta.hasLore()){
                this.lore = itemMeta.getLore().toArray(new String[0]);
            }
            if(isArmor(item)){
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;
                if(isColorable(armorMeta)){
                    ColorableArmorMeta coloredArmorMeta = (ColorableArmorMeta) armorMeta;
                    this.itemColor = coloredArmorMeta.getColor();
                }
                if(armorMeta.hasTrim()){
                    this.isTrimmed = true;
                    this.armorTrim = armorMeta.getTrim();
                }
            }
        }
        NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        String registeredType = itemData.get(key, PersistentDataType.STRING);
        setConnection();
        Bukkit.getLogger().info("SetToRegisteredItem: registeredType=" + registeredType);

        String base64_itemData;

        try (PreparedStatement ps = connection.prepareStatement("SELECT data_json FROM registered_items WHERE registered_name = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
            ps.setString(1, registeredType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    base64_itemData = rs.getString("data_json");
                } else {
                    CloseConnection();
                    Bukkit.getLogger().info("item was not found in database: " + registeredType);
                    return item; // item not found
                }
            }
        }
        byte[] data = Base64.getDecoder().decode(base64_itemData);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);

        item = (ItemStack) ois.readObject();

        ois.close();

        CloseConnection();
        if(isReskinned){
            item.setType(this.itemModel);
            itemMeta = item.getItemMeta();//update with new item meta
            itemMeta.setDisplayName(this.name);

            if(itemMeta.hasLore()){
                itemMeta.setLore(List.of(this.lore));
            }
            if(isArmor(item)){
                ArmorMeta armorMeta = (ArmorMeta) itemMeta;
                if(this.isTrimmed){
                    armorMeta.setTrim(this.armorTrim);

                    itemMeta = armorMeta;
                } else{ //remove old unused trims
                    if(armorMeta.hasTrim()){
                        armorMeta.setTrim(null);
                    }
                }

                if(isColorable(armorMeta)){
                    ColorableArmorMeta coloredArmorMeta = (ColorableArmorMeta) armorMeta;
                    coloredArmorMeta.setColor(this.itemColor);

                    itemMeta = armorMeta;
                }
            }
            item.setItemMeta(itemMeta);
        }
        this.lore = null;
        if(armorTrim != null){
            armorTrim = null;
        }
        if(this.itemColor != null){
            this.itemColor = null;
        }

        itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(keyReforge, PersistentDataType.BOOLEAN, reforgedStatus);
        itemMeta.getPersistentDataContainer().set(keyReskin, PersistentDataType.BOOLEAN, reskinnedStatus);
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, registeredType);
        item.setItemMeta(itemMeta);

        if(hadLives){
            BoxPlugin.instance.getItemLivesManager().setLives(item, numLives);
        }

        return item;
    }

    public boolean UpdateLegacyItems(@NotNull Player p) throws SQLException, IOException, ClassNotFoundException {//fix legacy after database fix
        boolean fixedItems = false;
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

    public void UpdateCurrentItems(@NotNull Player p) throws SQLException, IOException, ClassNotFoundException {//fix legacy after database fix
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

    public ItemStack getItemFromName(String registeredName) throws SQLException, IOException, ClassNotFoundException {//fix legacy after database fix
        ItemStack item = new ItemStack(Material.DIRT);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Temp dirt lol (you should never see this)");

        if(ExistsInDatabase(registeredName)){
            itemMeta.setDisplayName(registeredName);
            item.setItemMeta(itemMeta);
            item = SetItemToBelongToRegisteredItem(item, registeredName);
            item = SetToRegisteredItem(item);
            item = SetToRegisteredItem(item); //needs to be called again for good measure
        } else {
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public void fuckItWeBall() throws SQLException, IOException {
        //the nuclear option, make a database back up first just in case
        ItemStack item = new ItemStack(Material.DIRT);
        List<String> dataBaseItemNames = GetAllRegisteredNames();

        for (String itemName : dataBaseItemNames){
            if(ExistsInDatabase(itemName)){
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(itemName);
                item.setItemMeta(itemMeta);
                item = SetItemToBelongToRegisteredItem(item, itemName);
                item = SetToRegisteredItemLegcay(item);
                item = SetToRegisteredItemLegcay(item); //needs to be called again for good measure
                item = RemoveItemFromBelongingToParent(item);
                RemoveFromDatabase(itemName);
                RegisterItem(item, itemName);
            }
        }
    }
}
