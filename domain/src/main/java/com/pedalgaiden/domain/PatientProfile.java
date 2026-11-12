package com.pedalgaiden.domain;

public final class PatientProfile {
    private String name="Jogador 1";
    private int age=28, weight=70, character, experience, rehabilitationGoal, activity;
    private final boolean[] goals={true,false,false,false};

    public String name(){return name;}
    public int age(){return age;}
    public int weight(){return weight;}
    public int character(){return character;}
    public int experience(){return experience;}
    public int rehabilitationGoal(){return rehabilitationGoal;}
    public int activity(){return activity;}
    public boolean goal(int index){return goals[index];}
    public void rename(String value){
        if(value==null||value.isBlank())throw new IllegalArgumentException("Nome vazio");
        name=value.trim();
    }
    public void adjustAge(int direction){age=Math.max(12,Math.min(100,age+direction));}
    public void adjustWeight(int direction){weight=Math.max(25,Math.min(250,weight+direction));}
    public void selectCharacter(int value){checkIndex(value,4);character=value;}
    public void selectExperience(int value){checkIndex(value,3);experience=value;}
    public void nextRehabilitationGoal(){rehabilitationGoal=(rehabilitationGoal+1)%4;}
    public void nextActivity(){activity=(activity+1)%3;}
    public void toggleGoal(int index){checkIndex(index,4);goals[index]=!goals[index];}
    private static void checkIndex(int value,int count){
        if(value<0||value>=count)throw new IllegalArgumentException("Indice fora do intervalo: "+value);
    }
}
