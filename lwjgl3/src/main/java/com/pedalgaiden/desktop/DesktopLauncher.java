package com.pedalgaiden.desktop;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.pedalgaiden.PedalGaidenGame;
public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Pedal Gaiden | Ride. Recycle. Restore.");
        cfg.setWindowedMode(1280,720);
        cfg.useVsync(true);
        new Lwjgl3Application(new PedalGaidenGame(java.util.Arrays.asList(args).contains("--iot")),cfg);
    }
}
