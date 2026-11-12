package com.pedalgaiden.infrastructure.audio;

import com.pedalgaiden.application.port.AudioOutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public final class GameAudio implements Disposable, AudioOutput {
    public static final float RACE_VOLUME=.8f;
    private final Music menu, race;
    private final Sound click, pickup, recycled;

    public GameAudio(){
        this(music("mainsoundtrack",.5f),music("menuSound",RACE_VOLUME),
                Gdx.audio.newSound(Gdx.files.internal("audio/clickMenu.mp3")),
                Gdx.audio.newSound(Gdx.files.internal("audio/collectCollectible.mp3")),
                Gdx.audio.newSound(Gdx.files.internal("audio/binCollected.mp3")));
    }
    public GameAudio(Music menu,Music race,Sound click,Sound pickup,Sound recycled){
        this.menu=menu;this.race=race;
        this.click=click;this.pickup=pickup;this.recycled=recycled;
    }

    private static Music music(String name,float volume){
        Music music=Gdx.audio.newMusic(Gdx.files.internal("audio/"+name+".mp3"));
        music.setLooping(true);music.setVolume(volume);return music;
    }
    public void menu(){
        race.stop();
        if(!menu.isPlaying())menu.play();
    }
    public void countdown(){
        menu.stop();
        if(!race.isPlaying())race.play();
    }
    public void race(boolean paused){
        menu.stop();
        if(paused){race.pause();pickup.stop();recycled.stop();return;}
        if(!race.isPlaying())race.play();
    }
    public void resetRace(){race.stop();pickup.stop();recycled.stop();}
    public void click(){click.play(.6f);}
    public void pickup(){pickup.play(.7f);}
    public void recycled(){recycled.play(.8f);}
    public void pause(){menu.pause();race.pause();pickup.stop();recycled.stop();}
    @Override public void dispose(){
        menu.dispose();race.dispose();
        click.dispose();pickup.dispose();recycled.dispose();
    }
}
