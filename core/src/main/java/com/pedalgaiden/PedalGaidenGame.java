package com.pedalgaiden;
import com.badlogic.gdx.Game;
import com.pedalgaiden.presentation.view.MainMenuScreen;
public final class PedalGaidenGame extends Game {
    private final boolean iot;
    private GameServices services;
    public PedalGaidenGame(boolean iot) { this.iot = iot; }
    @Override public void create() { services=new GameServices(iot);setScreen(new MainMenuScreen(this,services)); }
    @Override public void dispose() { if(getScreen()!=null)getScreen().dispose();if(services!=null)services.close(); }
}
