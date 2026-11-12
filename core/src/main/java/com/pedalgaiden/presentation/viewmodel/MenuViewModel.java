package com.pedalgaiden.presentation.viewmodel;

import com.pedalgaiden.application.port.AudioOutput;

public final class MenuViewModel {
    public enum Action { NONE, PLAY, INSTRUCTIONS }
    private static final String[] OPTIONS={"Jogar","Instru\u00e7\u00f5es","Configura\u00e7\u00f5es","Cr\u00e9ditos"};
    private final AudioOutput audio;
    private int selected;
    private String notice="";
    public MenuViewModel(AudioOutput audio){this.audio=audio;}
    public int optionCount(){return OPTIONS.length;}
    public String option(int index){return OPTIONS[index];}
    public int selected(){return selected;}
    public String notice(){return notice;}
    public void move(int direction){selected=Math.floorMod(selected+direction,OPTIONS.length);}
    public void select(int index){
        if(index<0||index>=OPTIONS.length)throw new IllegalArgumentException("Opcao invalida");
        selected=index;
    }
    public Action activate(){
        audio.click();
        if(selected==0)return Action.PLAY;
        if(selected==1)return Action.INSTRUCTIONS;
        notice=OPTIONS[selected]+" - EM BREVE";
        return Action.NONE;
    }
    public void show(){audio.menu();}
    public void pause(){audio.pause();}
}
