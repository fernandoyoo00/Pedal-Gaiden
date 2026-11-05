package com.pedalgaiden;
import com.badlogic.gdx.Game;
public final class PedalGaidenGame extends Game {
    private final boolean iot;
    public PedalGaidenGame(boolean iot) { this.iot = iot; }
    @Override public void create() { setScreen(new MainMenuScreen(this)); }
}
