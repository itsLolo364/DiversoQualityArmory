package me.zombie_striker.qg.guns.reloaders;

import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.utils.WeaponSounds;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class MagazineReloader implements ReloadingHandler {

    private static final HashMap<UUID, Boolean> reloadingPlayers = new HashMap<UUID, Boolean>();

    public MagazineReloader() {
        ReloadingManager.add(this);
    }

    public String getName() {
        return ReloadingManager.MAGAZINE_RELOAD;
    }

    public String getDefaultReloadingSound() {
        return WeaponSounds.RELOAD_MAG_OUT.getSoundName();
    }

    public boolean isReloading(Player player) {
        Boolean result = reloadingPlayers.get(player.getUniqueId());
        return result != null && result;
    }

    public double reload(Player player, Gun g, int amountReloading) {
        // Blocca la ricarica automatica - deve usare i caricatori manualmente
        return -1; // Ritorna -1 per bloccare la ricarica
    }

    public static void setReloading(Player player, boolean reloading) {
        if (reloading) {
            reloadingPlayers.put(player.getUniqueId(), Boolean.valueOf(true));
        } else {
            reloadingPlayers.remove(player.getUniqueId());
        }
    }
}