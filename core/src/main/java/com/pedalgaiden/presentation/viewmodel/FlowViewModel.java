package com.pedalgaiden.presentation.viewmodel;

import com.pedalgaiden.application.port.AudioOutput;
import com.pedalgaiden.application.port.CadenceSource;

public final class FlowViewModel {
    public enum Page { PROFILE, GOALS, EXPERIENCE, CONFIRM, LEVELS, COUNTDOWN, PLAY, PAUSE, RESULTS, HELP }
    private final AudioOutput audio;
    private final ProfileViewModel profile=new ProfileViewModel();
    private final RaceViewModel race;
    private Page page=Page.PROFILE, previous=Page.PROFILE;
    private int level;
    private final int duration=5;
    private float countdownTime;

    public FlowViewModel(AudioOutput audio,CadenceSource cadence,boolean iot){
        this.audio=audio;race=new RaceViewModel(audio,cadence,iot);
    }
    public Page page(){return page;}
    public Page previous(){return previous;}
    public ProfileViewModel profile(){return profile;}
    public RaceViewModel race(){return race;}
    public int level(){return level;}
    public int duration(){return duration;}
    public float countdownTime(){return countdownTime;}
    public static int countdownNumber(float time){return Math.max(0,3-(int)time);}
    public int countdownNumber(){return countdownNumber(countdownTime);}
    public void goTo(Page destination){
        if(destination==Page.PLAY||destination==Page.COUNTDOWN||destination==Page.PAUSE||destination==Page.RESULTS)
            throw new IllegalArgumentException("Use os comandos de corrida para mudar de estado");
        page=destination;
    }
    public void selectLevel(int value){
        if(value<0||value>=4)throw new IllegalArgumentException("Fase invalida");
        level=value;
    }
    public void start(){
        race.start(level,profile.character(),duration);
        countdownTime=0;page=Page.COUNTDOWN;audio.resetRace();audio.countdown();
    }
    public void update(float delta,float demoRpm,int direction){
        if(!Float.isFinite(delta)||delta<0)throw new IllegalArgumentException("Tempo invalido");
        float dt=Math.min(.05f,delta);
        if(page==Page.COUNTDOWN){
            countdownTime+=dt;
            if(countdownTime>=3.7f)page=Page.PLAY;
        }else if(page==Page.PLAY){
            race.update(dt,demoRpm,direction);
            if(race.finished())page=Page.RESULTS;
        }
        syncAudio();
    }
    public void changeLane(int direction){if(page==Page.PLAY)race.changeLane(direction);}
    public void pauseRace(){
        if(page==Page.PLAY||page==Page.COUNTDOWN){previous=page;page=Page.PAUSE;audio.pause();}
    }
    public void resumeRace(){if(page==Page.PAUSE){page=previous;syncAudio();}}
    public void clicked(){audio.click();}
    public void pauseAudio(){audio.pause();}
    public void resumeAudio(){syncAudio();}
    public void hide(){audio.resetRace();}
    private void syncAudio(){
        switch(page){
            case COUNTDOWN -> audio.countdown();
            case PLAY -> audio.race(false);
            case PAUSE -> audio.pause();
            default -> audio.menu();
        }
    }
}
