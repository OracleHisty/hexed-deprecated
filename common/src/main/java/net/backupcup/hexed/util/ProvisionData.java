package net.backupcup.hexed.util;

public class ProvisionData {
    public boolean uiActive;
    public int indicatorPos;
    public int reloadSpeed;
    public int reloadsPlayed;

    public ProvisionData(boolean uiActive, int indicatorPos, int reloadSpeed, int reloadsPlayed) {
        this.uiActive = uiActive;
        this.indicatorPos = indicatorPos;
        this.reloadSpeed = reloadSpeed;
        this.reloadsPlayed = reloadsPlayed;
    }
}
