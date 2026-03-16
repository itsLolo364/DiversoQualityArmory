package me.zombie_striker.qg.config;

import java.io.File;

public class MagazineYML extends ArmoryYML {

    public MagazineYML(File file) {
        super(file);
    }

    @Override
    public void verifyAllTagsExist() {
        super.verifyAllTagsExist();
        verify("baseGun", "unknown");
        verify("maxAmmo", 30);
        verify("IDM", 394); // ID Model del caricatore
    }

    public MagazineYML setBaseGun(String gunName) {
        set("baseGun", gunName);
        return this;
    }

    public MagazineYML setMaxAmmo(int maxAmmo) {
        set("maxAmmo", maxAmmo);
        return this;
    }

    public MagazineYML setIDM(int idm) {
        set("IDM", idm);
        return this;
    }
}