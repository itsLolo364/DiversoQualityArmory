package me.zombie_striker.qg.magazines;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MagazineListener implements Listener {

    private final Map<UUID, Boolean> isReloading = new HashMap<>();
    private final NamespacedKey currentAmmoKey;
    private final NamespacedKey maxAmmoKey;

    public MagazineListener() {
        currentAmmoKey = new NamespacedKey(QAMain.getInstance(), "current_ammo");
        maxAmmoKey = new NamespacedKey(QAMain.getInstance(), "max_ammo");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();

        Gun gun = me.zombie_striker.qg.api.QualityArmory.getGun(mainHand);

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // Inserimento caricatore nell'arma
            if (gun != null && Magazine.isMagazine(offHand)) {
                if (MagazineManager.isCompatible(mainHand, offHand)) {
                    insertMagazine(p, mainHand, offHand);
                    e.setCancelled(true);
                    return;
                }
            }

            // Espulsione caricatore dall'arma
            if (gun != null && !Magazine.isMagazine(offHand)) {
                ejectMagazine(p, mainHand, gun);
                e.setCancelled(true);
                return;
            }

            // Ricarica caricatore
            if (Magazine.isMagazine(mainHand)) {
                reloadMagazine(p, mainHand);
                e.setCancelled(true);
                return;
            }
        }
    }

    private void insertMagazine(Player p, ItemStack weapon, ItemStack mag) {
        Gun gun = me.zombie_striker.qg.api.QualityArmory.getGun(weapon);
        if (gun == null)
            return;

        ItemMeta weaponMeta = weapon.getItemMeta();
        if (weaponMeta == null)
            return;

        PersistentDataContainer weaponContainer = weaponMeta.getPersistentDataContainer();
        int currentAmmoInWeapon = weaponContainer.getOrDefault(currentAmmoKey, PersistentDataType.INTEGER, 0);
        int maxAmmoInWeapon = weaponContainer.getOrDefault(maxAmmoKey, PersistentDataType.INTEGER, 0);

        // Espelli caricatore vecchio se presente
        if (currentAmmoInWeapon > 0 || maxAmmoInWeapon > 0) {
            ItemStack oldMag = MagazineManager.createMagazineForGun(gun, currentAmmoInWeapon);
            if (oldMag != null) {
                playSound(p, "weapons.reloadmagout");
                p.getInventory().addItem(oldMag);
            }
        }

        // Inserisci nuovo caricatore
        int newMagAmmo = Magazine.getCurrentAmmo(mag);
        int newMagMaxAmmo = Magazine.getMaxAmmo(mag);

        weaponContainer.set(currentAmmoKey, PersistentDataType.INTEGER, newMagAmmo);
        weaponContainer.set(maxAmmoKey, PersistentDataType.INTEGER, newMagMaxAmmo);

        Gun.updateAmmo(gun, weapon, newMagAmmo);
        weapon.setItemMeta(weaponMeta);

        p.getInventory().setItemInOffHand(null);
        p.updateInventory();

        playSound(p, "weapons.reloadmagin");
    }

    private void ejectMagazine(Player p, ItemStack weapon, Gun gun) {
        ItemMeta weaponMeta = weapon.getItemMeta();
        if (weaponMeta == null)
            return;

        PersistentDataContainer weaponContainer = weaponMeta.getPersistentDataContainer();
        int currentAmmo = weaponContainer.getOrDefault(currentAmmoKey, PersistentDataType.INTEGER, 0);
        int maxAmmo = weaponContainer.getOrDefault(maxAmmoKey, PersistentDataType.INTEGER, 0);

        if (maxAmmo == 0)
            return; // Non c'è caricatore

        ItemStack mag = MagazineManager.createMagazineForGun(gun, currentAmmo);
        if (mag == null)
            return;

        playSound(p, "weapons.reloadmagout");
        p.getInventory().addItem(mag);

        weaponContainer.set(currentAmmoKey, PersistentDataType.INTEGER, 0);
        weaponContainer.set(maxAmmoKey, PersistentDataType.INTEGER, 0);

        Gun.updateAmmo(gun, weapon, 0);
        weapon.setItemMeta(weaponMeta);

        p.getInventory().setItemInMainHand(weapon);
        p.updateInventory();
    }

    private void reloadMagazine(Player p, ItemStack mag) {
        if (isReloading.getOrDefault(p.getUniqueId(), false))
            return;

        int currentAmmo = Magazine.getCurrentAmmo(mag);
        int maxAmmo = Magazine.getMaxAmmo(mag);
        String gunName = Magazine.getMagGunName(mag);

        int requiredAmmoCMD = MagazineManager.getAmmoCMD(gunName);

        ItemStack ammoItem = findAmmo(p, requiredAmmoCMD);

        if (currentAmmo < maxAmmo && ammoItem != null) {
            // Ricarica
            isReloading.put(p.getUniqueId(), true);
            new ReloadTask(p, mag, ammoItem, true, maxAmmo).runTaskTimer(QAMain.getInstance(), 0L, 5L);
        } else if (currentAmmo > 0 && ammoItem == null) {
            // Scarica
            isReloading.put(p.getUniqueId(), true);
            new ReloadTask(p, mag, null, false, maxAmmo).runTaskTimer(QAMain.getInstance(), 0L, 5L);
        }
    }

    private ItemStack findAmmo(Player p, int cmd) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (MagazineManager.isAmmo(item)) {
                if (item.getItemMeta().getCustomModelData() == cmd) {
                    return item;
                }
            }
        }
        return null;
    }

    private void playSound(Player p, String sound) {
        try {
            p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback a suoni vanilla se custom sounds non disponibili
        }
    }

    private class ReloadTask extends BukkitRunnable {
        private final Player p;
        private final ItemStack mag;
        private final ItemStack ammoItem;
        private final boolean loading;
        private final int maxAmmo;

        public ReloadTask(Player p, ItemStack mag, ItemStack ammoItem, boolean loading, int maxAmmo) {
            this.p = p;
            this.mag = mag;
            this.ammoItem = ammoItem;
            this.loading = loading;
            this.maxAmmo = maxAmmo;
        }

        @Override
        public void run() {
            if (!p.isOnline() || !mag.equals(p.getInventory().getItemInMainHand())) {
                cancelTask();
                return;
            }

            int currentAmmo = Magazine.getCurrentAmmo(mag);

            if (loading) {
                if (currentAmmo >= maxAmmo || ammoItem == null || ammoItem.getAmount() <= 0) {
                    cancelTask();
                    return;
                }

                currentAmmo++;
                ammoItem.setAmount(ammoItem.getAmount() - 1);
                playSound(p, "weapons.reloadbullet");

            } else {
                if (currentAmmo <= 0) {
                    cancelTask();
                    return;
                }

                currentAmmo--;

                String gunName = Magazine.getMagGunName(mag);
                int ammoCMD = MagazineManager.getAmmoCMD(gunName);
                ItemStack droppedAmmo = createAmmoItem(1, ammoCMD);

                p.getInventory().addItem(droppedAmmo);
                playSound(p, "weapons.reloadmagout");
            }

            Magazine.setCurrentAmmo(mag, currentAmmo);
            p.updateInventory();

            if (currentAmmo == (loading ? maxAmmo : 0)) {
                cancelTask();
            }
        }

        private void cancelTask() {
            isReloading.remove(p.getUniqueId());
            cancel();
        }

        private ItemStack createAmmoItem(int amount, int cmd) {
            for (Ammo ammo : QAMain.ammoRegister.values()) {
                if (ammo.getItemData().getData() == cmd) {
                    ItemStack item = new ItemStack(ammo.getItemData().getMat(), amount);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(cmd);
                        meta.setDisplayName(ammo.getDisplayName());
                        item.setItemMeta(meta);
                    }
                    return item;
                }
            }
            return new ItemStack(org.bukkit.Material.STICK, amount);
        }
    }
}