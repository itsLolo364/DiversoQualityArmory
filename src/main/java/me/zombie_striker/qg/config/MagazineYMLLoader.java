package me.zombie_striker.qg.config;

import me.zombie_striker.customitemmanager.MaterialStorage;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.magazines.Magazine;
import me.zombie_striker.qg.magazines.MagazineManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.ArrayList;

public class MagazineYMLLoader {

    public static void loadMagazines(QAMain main) {
        File magazineFolder = new File(main.getDataFolder(), "magazines");

        if (!magazineFolder.exists()) {
            magazineFolder.mkdirs();
            createDefaultMagazines(main);
            return;
        }

        int items = 0;
        for (File f : magazineFolder.listFiles()) {
            try {
                if (f.getName().contains("yml")) {
                    FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);

                    if (f2.getBoolean("invalid", false))
                        continue;

                    String name = f2.getString("name");
                    if (QAMain.verboseLoadingLogging)
                        main.getLogger().info("-Loading Magazine: " + name);

                    String baseGunName = f2.getString("baseGun");
                    Gun baseGun = me.zombie_striker.qg.api.QualityArmory.getGunByName(baseGunName);

                    if (baseGun == null) {
                        main.getLogger()
                                .warning("Cannot load magazine " + name + ": gun " + baseGunName + " not found!");
                        continue;
                    }

                    int maxAmmo = f2.getInt("maxAmmo", baseGun.getMaxBullets());
                    int idm = f2.getInt("IDM", 394);
                    double price = f2.getDouble("price", 100);

                    MaterialStorage ms = MaterialStorage.getMS(Material.STICK, idm, 0);
                    String displayName = f2.contains("displayname") ? f2.getString("displayname")
                            : baseGun.getDisplayName();

                    Object[] ingredients = main.convertIngredientsRaw(
                            f2.getStringList("craftingRequirements"));

                    Magazine magazine = new Magazine(name, displayName, baseGun, ms, idm, maxAmmo, price, ingredients);
                    MagazineManager.registerMagazine(magazine);

                    // Registra anche in QAMain per il comando /qa give
                    QAMain.magazineRegister.put(ms, magazine);
                    QAMain.miscRegister.put(ms, magazine);

                    items++;
                }
            } catch (Exception e) {
                main.getLogger().severe("Error loading magazine from " + f.getName());
                e.printStackTrace();
            }
        }

        if (!QAMain.verboseLoadingLogging)
            main.getLogger().info("-Loaded " + items + " Magazine types.");
    }

    private static void createDefaultMagazines(QAMain main) {
        // Crea caricatori di esempio per armi esistenti
        if (QAMain.enableCreationOfFiles) {
            for (Gun gun : QAMain.gunRegister.values()) {
                if (gun.getMaxBullets() > 0) {
                    createMagazineYML(main, gun);
                }
            }
        }
    }

    private static void createMagazineYML(QAMain main, Gun gun) {
        File magazineFolder = new File(main.getDataFolder(), "magazines");
        if (!magazineFolder.exists())
            magazineFolder.mkdirs();

        File magFile = new File(magazineFolder, "mag_" + gun.getName() + ".yml");
        if (magFile.exists())
            return;

        MagazineYML yml = new MagazineYML(magFile);
        yml.set("invalid", false);
        yml.set("name", "mag_" + gun.getName());
        yml.set("displayname", gun.getDisplayName());
        yml.setBaseGun(gun.getName());
        yml.setMaxAmmo(gun.getMaxBullets());
        yml.setIDM(getNextAvailableIDM(gun));
        yml.setPrice(100);
        yml.set("craftingRequirements", new ArrayList<String>());

        yml.verifyAllTagsExist();
        yml.save();
    }

    private static int getNextAvailableIDM(Gun gun) {
        // Determina IDM in base al tipo di munizione
        String ammoType = gun.getAmmoType().getName();

        switch (ammoType.toLowerCase()) {
            case "9mm":
            case "px4":
            case "fs92":
            case "glock":
                return 394;
            case "556":
            case "762":
            case "hkg36":
            case "m4a1s":
                return 395;
            case "barret":
            case "50cal":
                return 397;
            default:
                return 394;
        }
    }
}