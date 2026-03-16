package me.zombie_striker.qg.magazines;

import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.customitemmanager.MaterialStorage;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Magazine extends CustomBaseObject {

    private Gun baseGun;
    private int maxAmmo;
    private int currentAmmo;
    private int customModelData;
    private Ammo compatibleAmmo;

    private NamespacedKey magIdKey;
    private NamespacedKey magAmmoKey;
    private NamespacedKey magMaxAmmoKey;

    public Magazine(String name, String displayName, Gun baseGun, MaterialStorage ms,
            int customModelData, int maxAmmo, double price, Object[] ingredients) {
        super(name, ms, displayName, null, false);
        this.baseGun = baseGun;
        this.maxAmmo = maxAmmo;
        this.currentAmmo = maxAmmo;
        this.customModelData = customModelData;
        this.compatibleAmmo = baseGun.getAmmoType();

        setDisplayname(displayName);

        initKeys();
    }

    private void initKeys() {
        magIdKey = new NamespacedKey(QAMain.getInstance(), "mag_id");
        magAmmoKey = new NamespacedKey(QAMain.getInstance(), "mag_ammo");
        magMaxAmmoKey = new NamespacedKey(QAMain.getInstance(), "mag_max_ammo");
    }

    public ItemStack createMagazineItem(int currentAmmo) {
        ItemStack mag = new ItemStack(Material.STICK);
        ItemMeta meta = mag.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(customModelData);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(magIdKey, PersistentDataType.STRING, baseGun.getName());
            container.set(magMaxAmmoKey, PersistentDataType.INTEGER, Integer.valueOf(maxAmmo));
            container.set(magAmmoKey, PersistentDataType.INTEGER, Integer.valueOf(currentAmmo));

            // Imposta nome e lore
            String gunDisplayName = baseGun.getDisplayName();
            if (gunDisplayName == null || gunDisplayName.isEmpty()) {
                gunDisplayName = baseGun.getName();
            }

            String displayName = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&7Caricatore " + gunDisplayName + " &7(" + currentAmmo + "/" + maxAmmo + ")");
            meta.setDisplayName(displayName);
            meta.setLore(java.util.Collections.emptyList());

            mag.setItemMeta(meta);
        }

        return mag;
    }

    private void updateLore(ItemMeta meta, int currentAmmo) {
        String displayName = ChatColor.translateAlternateColorCodes('&',
                "&7Caricatore " + baseGun.getDisplayName() + " &7(" + currentAmmo + "/" + maxAmmo + ")");
        meta.setDisplayName(displayName);
        meta.setLore(Collections.emptyList());
    }

    public static boolean isMagazine(ItemStack item) {
        if (item == null || item.getType() != Material.STICK)
            return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        NamespacedKey key = new NamespacedKey(QAMain.getInstance(), "mag_id");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    public static String getMagGunName(ItemStack item) {
        if (!isMagazine(item))
            return "";
        NamespacedKey key = new NamespacedKey(QAMain.getInstance(), "mag_id");
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.STRING, "");
    }

    public static int getCurrentAmmo(ItemStack item) {
        if (!isMagazine(item))
            return 0;
        NamespacedKey key = new NamespacedKey(QAMain.getInstance(), "mag_ammo");
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static int getMaxAmmo(ItemStack item) {
        if (!isMagazine(item))
            return 0;
        NamespacedKey key = new NamespacedKey(QAMain.getInstance(), "mag_max_ammo");
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static void setCurrentAmmo(ItemStack item, int ammo) {
        if (!isMagazine(item))
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        NamespacedKey ammoKey = new NamespacedKey(QAMain.getInstance(), "mag_ammo");
        NamespacedKey maxKey = new NamespacedKey(QAMain.getInstance(), "mag_max_ammo");
        NamespacedKey magIdKey = new NamespacedKey(QAMain.getInstance(), "mag_id");

        int maxAmmo = meta.getPersistentDataContainer().has(maxKey, PersistentDataType.INTEGER)
                ? meta.getPersistentDataContainer().get(maxKey, PersistentDataType.INTEGER).intValue()
                : 0;

        String gunName = meta.getPersistentDataContainer().has(magIdKey, PersistentDataType.STRING)
                ? meta.getPersistentDataContainer().get(magIdKey, PersistentDataType.STRING)
                : "Unknown";

        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, Integer.valueOf(ammo));

        String displayName = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&7Caricatore " + gunName + " &7(" + ammo + "/" + maxAmmo + ")");
        meta.setDisplayName(displayName);
        meta.setLore(java.util.Collections.emptyList());

        item.setItemMeta(meta);
    }

    public Gun getBaseGun() {
        return baseGun;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Ammo getCompatibleAmmo() {
        return compatibleAmmo;
    }
}