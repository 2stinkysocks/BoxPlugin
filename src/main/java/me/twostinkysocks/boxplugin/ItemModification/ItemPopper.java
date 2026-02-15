package me.twostinkysocks.boxplugin.ItemModification;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ItemPopper {
    private Connection connection;

    public String name = null;
    public String predecessorName = null;
    public String successorName = null;

    public void setConnection() throws SQLException {
        String url = "jdbc:sqlite:" + BoxPlugin.instance.getDataFolder() + "/registeredItems.db";
        connection = DriverManager.getConnection(url);

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_successor (
                    item_pointer TEXT UNIQUE NOT NULL,
                    data_base64 TEXT NOT NULL
                )
                """);
        }
    }

    public void setConnectionPointer() throws SQLException {
        String url = "jdbc:sqlite:" + BoxPlugin.instance.getDataFolder() + "/registeredItems.db";
        connection = DriverManager.getConnection(url);

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_successor_pointer (
                    successor_name TEXT UNIQUE NOT NULL,
                    predecessor_name TEXT NOT NULL
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

    public void changeSuccessorName(String oldName, String newName) throws SQLException {
        setConnection();
        String base64ItemData = null;
        try (PreparedStatement ps = connection.prepareStatement("SELECT data_base64 FROM item_successor WHERE item_pointer = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
            ps.setString(1, oldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    base64ItemData = rs.getString("data_base64");
                } else {
                    CloseConnection();
                    Bukkit.getLogger().info("no item data found: " + oldName);
                    return;
                }
            }
        }
        if(base64ItemData == null){
            return;
        }


        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO item_successor (item_pointer, data_base64) VALUES (?, ?)"
        )) {
            ps.setString(1, newName);
            ps.setString(2, base64ItemData);
            ps.executeUpdate();
        }
        CloseConnection();
        RemoveSuccessorDataFromDatabase(oldName);
    }

    public void ExperimentalUpdateDatabase(List<ItemStack> items, String predecessorName, String successorName) throws SQLException, IOException {
        setConnection();
        String base64ItemData;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();//clanker logic to deep copy item data
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {

            oos.writeInt(items.size());
            for (ItemStack item : items) {
                oos.writeObject(item);
            }

            base64ItemData = Base64.getEncoder().encodeToString(baos.toByteArray());
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO item_successor (item_pointer, data_base64) VALUES (?, ?)"
        )) {
            ps.setString(1, successorName);
            ps.setString(2, base64ItemData);
            ps.executeUpdate();
        }
        CloseConnection();
        SetSuccessorPointer(successorName, predecessorName);
    }

    public void SetSuccessorPointer(String successorName, String predecessorName) throws SQLException {//make a tree of pointers of items to point to other items
        //use the predecessor name to get items stored for this item
        setConnectionPointer();

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO item_successor_pointer (successor_name, predecessor_name) VALUES (?, ?)"
        )) {
            ps.setString(1, successorName);
            ps.setString(2, predecessorName);
            ps.executeUpdate();
        }
        CloseConnection();
    }

    public boolean RemoveFromDatabase(String successorName) throws SQLException {//clanker made this one, removes item from database based on name
        setConnection(); // ensure connection is set
        int affected = 0;
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM item_successor WHERE item_pointer = ?")) {
            ps.setString(1, successorName);
            affected = ps.executeUpdate();
        }
        CloseConnection();
        setConnectionPointer(); // ensure connection is set
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM item_successor_pointer WHERE successor_name = ?")) {
            ps.setString(1, successorName);
            affected = ps.executeUpdate();
        }
        CloseConnection();
        return affected > 0; // true if something was deleted
    }
    public void RemoveSuccessorDataFromDatabase(String successorName) throws SQLException {//clanker made this one, removes item from database based on name
        setConnection(); // ensure connection is set
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM item_successor WHERE item_pointer = ?")) {
            ps.setString(1, successorName);
            ps.executeUpdate();
        }
        CloseConnection();
    }
    public boolean isPopable(ItemStack item) throws SQLException {
        ItemMeta itemMeta = item.getItemMeta();
        if(!BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta)){
            return false;
        }
        NamespacedKey itemKey = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        String successorName = itemMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

        if(!SuccessorExistsInDatabase(successorName)){
            return false;
        }
        return true;
    }

    public boolean PredecessorExistsInDatabase(String predecessorName) throws SQLException {
        setConnection(); // ensure connection is set
        String query = "SELECT 1 FROM item_successor WHERE item_pointer = ? LIMIT 1";
        boolean answer;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, predecessorName);
            try (ResultSet rs = ps.executeQuery()) {
                answer = rs.next();
            }
        }
        CloseConnection();
        return answer;
    }

    public boolean SuccessorExistsInDatabase(String successorName) throws SQLException {
        setConnectionPointer(); // ensure connection is set
        String query = "SELECT 1 FROM item_successor_pointer WHERE successor_name = ? LIMIT 1";
        boolean answer;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, successorName);
            try (ResultSet rs = ps.executeQuery()) {
                answer = rs.next();
            }
        }
        CloseConnection();
        return answer;
    }

//    public List<ItemStack> getItemsToDrop(ItemStack item) throws SQLException, IOException, ClassNotFoundException {//will only be called on items that have the appropriate tags
//        ItemMeta itemMeta = item.getItemMeta();
//        List<ItemStack> itemsToDrop = new ArrayList<>();
//        if(!BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta)){
//            return itemsToDrop;
//        }
//        NamespacedKey itemKey = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
//        String successorName = itemMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
//
//        if(!SuccessorExistsInDatabase(successorName)){
//            return itemsToDrop;
//        }
//
//        ItemStack rubyItem = Util.getSkull("http://textures.minecraft.net/texture/2530191500c2453624dd937ec125d44f0942cc2b664073e2a366b3fa67a0c897");
//        ItemMeta rubyItemMeta = rubyItem.getItemMeta();
//        rubyItemMeta.setDisplayName("&x&F&B&0&0&0&0&lR&x&F&C&1&B&1&B&lu&x&F&C&3&7&3&7&lb&x&F&D&5&2&5&2&ly");
//        rubyItemMeta.setLore(List.of(
//                "&cRare Gemstone that can enhance items."
//        ));
//        rubyItem.setItemMeta(rubyItemMeta);
//        int numRubies = 0;
//        if(BoxPlugin.instance.getReforgeManager().hasReforges(item)){
//            int numReforges = BoxPlugin.instance.getReforgeManager().getNumReforges(item) - BoxPlugin.instance.getReforgeManager().getNumFreeReforges(item); //if you had free reforges, the bonus no longer counts to your refund
//            numRubies = (int) (BoxPlugin.instance.getReforgeManager().getREFORGECOST() * numReforges * 0.9);
//        }
//
//        setConnectionPointer();
//
//        try (PreparedStatement ps = connection.prepareStatement("SELECT predecessor_name FROM item_successor_pointer WHERE successor_name = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
//            ps.setString(1, successorName);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    this.predecessorName = rs.getString("predecessor_name");
//                } else {
//                    CloseConnection();
//                    Bukkit.getLogger().info("no predecessor found: " + successorName);
//                    return itemsToDrop; // item not found
//                }
//            }
//        }
//        CloseConnection();
//        if(!PredecessorExistsInDatabase(this.predecessorName)){
//            return itemsToDrop;
//        }
//        setConnection();
//        String base64_itemData;
//
//        try (PreparedStatement ps = connection.prepareStatement("SELECT data_base64 FROM item_successor WHERE item_pointer = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
//            ps.setString(1, predecessorName);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    base64_itemData = rs.getString("data_base64");
//                } else {
//                    CloseConnection();
//                    Bukkit.getLogger().info("no item data found: " + predecessorName);
//                    return itemsToDrop; // item not found
//                }
//            }
//        }
//
//        byte[] data = Base64.getDecoder().decode(base64_itemData);
//
//        ByteArrayInputStream bais = new ByteArrayInputStream(data);
//        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);
//
//        int count = ois.readInt();
//        for (int i = 0; i < count; i++) {
//            itemsToDrop.add((ItemStack) ois.readObject());
//        }
//
//        ois.close();
//
//        CloseConnection();
//        if(numRubies > 0){
//            rubyItem.setAmount(numRubies);
//            itemsToDrop.add(rubyItem);
//        }
//
//        return itemsToDrop;
//    }

    public List<ItemStack> getItemsToDrop(ItemStack item) throws SQLException, IOException, ClassNotFoundException {//will only be called on items that have the appropriate tags
        ItemMeta itemMeta = item.getItemMeta();
        List<ItemStack> itemsToDrop = new ArrayList<>();
        if(!BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta)){
            return itemsToDrop;
        }
        NamespacedKey itemKey = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        String successorName = itemMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

        if(!SuccessorExistsInDatabase(successorName)){
            return itemsToDrop;
        }

        ItemStack rubyItem = Util.getSkull("http://textures.minecraft.net/texture/2530191500c2453624dd937ec125d44f0942cc2b664073e2a366b3fa67a0c897");
        ItemMeta rubyItemMeta = rubyItem.getItemMeta();
        rubyItemMeta.setDisplayName("§x§F§B§0§0§0§0§lR§x§F§C§1§B§1§B§lu§x§F§C§3§7§3§7§lb§x§F§D§5§2§5§2§ly");
        rubyItemMeta.setLore(List.of(
                "§cRare Gemstone that can enhance items."
        ));
        rubyItem.setItemMeta(rubyItemMeta);
        int numRubies = 0;
        if(BoxPlugin.instance.getReforgeManager().hasReforges(item)){
            int numReforges = BoxPlugin.instance.getReforgeManager().getNumReforges(item) - BoxPlugin.instance.getReforgeManager().getNumFreeReforges(item); //if you had free reforges, the bonus no longer counts to your refund
            numRubies = (int) (BoxPlugin.instance.getReforgeManager().getREFORGECOST() * numReforges * 0.9);
        }
        if(!SuccessorExistsInDatabase(successorName)){
            return itemsToDrop;
        }
        setConnection();
        String base64_itemData;

        try (PreparedStatement ps = connection.prepareStatement("SELECT data_base64 FROM item_successor WHERE item_pointer = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
            ps.setString(1, successorName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    base64_itemData = rs.getString("data_base64");
                } else {
                    CloseConnection();
                    Bukkit.getLogger().info("no item data found: " + successorName);
                    return itemsToDrop; // item not found
                }
            }
        }

        byte[] data = Base64.getDecoder().decode(base64_itemData);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);

        int count = ois.readInt();
        for (int i = 0; i < count; i++) {
            itemsToDrop.add((ItemStack) ois.readObject());
        }

        ois.close();

        CloseConnection();
        if(numRubies > 0){
            rubyItem.setAmount(numRubies);
            itemsToDrop.add(rubyItem);
        }

        return itemsToDrop;
    }

    public ItemStack getDowngradedItem(ItemStack item) throws SQLException, IOException, ClassNotFoundException {
        ItemMeta itemMeta = item.getItemMeta();
        if(!BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta)){
            return item;
        }
        NamespacedKey itemKey = new NamespacedKey(BoxPlugin.instance, "belongsToParentItem");
        String successorName = itemMeta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);

        setConnectionPointer();

        try (PreparedStatement ps = connection.prepareStatement("SELECT predecessor_name FROM item_successor_pointer WHERE successor_name = ? LIMIT 1")) {//this should make a RegisteredItemData object from database
            ps.setString(1, successorName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.predecessorName = rs.getString("predecessor_name");
                } else {
                    CloseConnection();
                    Bukkit.getLogger().info("no predecessor found: " + successorName);
                    return item; // item not found
                }
            }
        }
        CloseConnection();
        return BoxPlugin.instance.getRegisteredItem().getItemFromName(predecessorName);
    }

    public void openPopperGUI(Player p, String successorName, String predecessorName) {
        ChestGui gui = new ChestGui(3, "Predecessor Requirements");
        StaticPane pane = new StaticPane(9,3);

        // 3 4 5 12 13 14 21 22 23
        gui.setOnClose(e -> {
            ArrayList<ItemStack> toAdd = new ArrayList<>();
            if(e.getView().getTopInventory().getItem(3) != null) toAdd.add(e.getView().getTopInventory().getItem(3));
            if(e.getView().getTopInventory().getItem(4) != null) toAdd.add(e.getView().getTopInventory().getItem(4));
            if(e.getView().getTopInventory().getItem(5) != null) toAdd.add(e.getView().getTopInventory().getItem(5));
            if(e.getView().getTopInventory().getItem(12) != null) toAdd.add(e.getView().getTopInventory().getItem(12));
            if(e.getView().getTopInventory().getItem(13) != null) toAdd.add(e.getView().getTopInventory().getItem(13));
            if(e.getView().getTopInventory().getItem(14) != null) toAdd.add(e.getView().getTopInventory().getItem(14));
            if(e.getView().getTopInventory().getItem(21) != null) toAdd.add(e.getView().getTopInventory().getItem(21));
            if(e.getView().getTopInventory().getItem(22) != null) toAdd.add(e.getView().getTopInventory().getItem(22));
            if(e.getView().getTopInventory().getItem(23) != null) toAdd.add(e.getView().getTopInventory().getItem(23));
            HashMap<Integer, ItemStack> toDrop = e.getPlayer().getInventory().addItem(toAdd.toArray(new ItemStack[toAdd.size()]));
            for(ItemStack stack : toDrop.values()) {
                Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                itemEntity.setItemStack(stack);
            }
        });

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        GuiItem confirmGui = new GuiItem(confirm.clone(), e -> {
            e.setCancelled(true);
            List<ItemStack> itemsForDatabase = confirmRegister(e, p);

            try {
                ExperimentalUpdateDatabase(itemsForDatabase, predecessorName, successorName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                SetSuccessorPointer(successorName, predecessorName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            p.sendMessage(ChatColor.GREEN + "Stored item drops for " +successorName + " into the data base");
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            cancelRegister(e, p);
            p.sendMessage(ChatColor.GREEN + "Cancelled register");
        });

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                pane.addItem(confirmGui.copy(), i, j);
                pane.addItem(cancelGui.copy(), i+6,j);
            }
        }
        gui.addPane(pane);
        gui.copy().show(p);
    }

    public List<ItemStack> confirmRegister(InventoryClickEvent e, Player p) {
        ArrayList<ItemStack> toAdd = new ArrayList<>();
        if(e.getView().getTopInventory().getItem(3) != null) toAdd.add(e.getView().getTopInventory().getItem(3));
        if(e.getView().getTopInventory().getItem(4) != null) toAdd.add(e.getView().getTopInventory().getItem(4));
        if(e.getView().getTopInventory().getItem(5) != null) toAdd.add(e.getView().getTopInventory().getItem(5));
        if(e.getView().getTopInventory().getItem(12) != null) toAdd.add(e.getView().getTopInventory().getItem(12));
        if(e.getView().getTopInventory().getItem(13) != null) toAdd.add(e.getView().getTopInventory().getItem(13));
        if(e.getView().getTopInventory().getItem(14) != null) toAdd.add(e.getView().getTopInventory().getItem(14));
        if(e.getView().getTopInventory().getItem(21) != null) toAdd.add(e.getView().getTopInventory().getItem(21));
        if(e.getView().getTopInventory().getItem(22) != null) toAdd.add(e.getView().getTopInventory().getItem(22));
        if(e.getView().getTopInventory().getItem(23) != null) toAdd.add(e.getView().getTopInventory().getItem(23));

        e.getView().getTopInventory().setItem(3, null);
        e.getView().getTopInventory().setItem(4, null);
        e.getView().getTopInventory().setItem(5, null);
        e.getView().getTopInventory().setItem(12, null);
        e.getView().getTopInventory().setItem(13, null);
        e.getView().getTopInventory().setItem(14, null);
        e.getView().getTopInventory().setItem(21, null);
        e.getView().getTopInventory().setItem(22, null);
        e.getView().getTopInventory().setItem(23, null);

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);

        e.getView().close();
        return toAdd;
    }

    public void cancelRegister(InventoryClickEvent e, Player p) {
        e.getView().close();
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
    }
}
