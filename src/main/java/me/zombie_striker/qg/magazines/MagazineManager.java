package me.zombie_striker.qg.magazines;

import me.zombie_striker.customitemmanager.MaterialStorage;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class MagazineManager {

    private static final Map<String, Magazine> magazines = new HashMap<>();

    public static void registerMagazine(Magazine magazine) {
        magazines.put(magazine.getBaseGun().getName(), magazine);
    }

    public static Magazine getMagazine(String gunName) {
        return magazines.get(gunName);
    }

    public static Magazine getMagazine(Gun gun) {
        return magazines.get(gun.getName());
    }

    public static boolean isCompatible(ItemStack weapon, ItemStack magazine) {
        if (!Magazine.isMagazine(magazine))
            return false;

        Gun gun = me.zombie_striker.qg.api.QualityArmory.getGun(weapon);
        if (gun == null)
            return false;

        String magGunName = Magazine.getMagGunName(magazine);
        return gun.getName().equals(magGunName);
    }

    public static ItemStack createMagazineForGun(Gun gun, int currentAmmo) {
        Magazine mag = getMagazine(gun);
        if (mag == null)
            return null;

        return mag.createMagazineItem(currentAmmo);
    }

    public static int getAmmoCMD(String gunName) {
        Magazine mag = getMagazine(gunName);
        if (mag == null)
            return 0;

        Ammo ammo = mag.getCompatibleAmmo();
        if (ammo == null)
            return 0;

        return ammo.getItemData().getData();
    }

    public static boolean isAmmo(ItemStack item) {
        if (item == null || item.getType() != Material.STICK)
            return false;
        if (!item.hasItemMeta())
            return false;

        int cmd = item.getItemMeta().getCustomModelData();

        for (Ammo ammo : QAMain.ammoRegister.values()) {
            if (ammo.getItemData().getData() == cmd) {
                return true;
            }
        }

        return false;
    }

    public static Ammo getAmmoType(ItemStack item) {
        if (!isAmmo(item))
            return null;

        int cmd = item.getItemMeta().getCustomModelData();

        for (Ammo ammo : QAMain.ammoRegister.values()) {
            if (ammo.getItemData().getData() == cmd) {
                return ammo;
            }
        }

        return null;
    }

    public static void clear() {
        magazines.clear();
    }
}