package com.pedalgaiden.presentation.viewmodel;

import com.pedalgaiden.domain.PatientProfile;

public final class ProfileViewModel {
    private final PatientProfile profile=new PatientProfile();
    private static final String[] REHABILITATION_GOALS={"Mobilidade do joelho","Resist\u00eancia cardiovascular",
            "For\u00e7a dos membros inferiores","Equil\u00edbrio e coordena\u00e7\u00e3o"};
    private static final String[] ACTIVITY={"Baixa","Moderada","Alta"};
    public String name(){return profile.name();}
    public int age(){return profile.age();}
    public int weight(){return profile.weight();}
    public int character(){return profile.character();}
    public int experience(){return profile.experience();}
    public boolean goal(int index){return profile.goal(index);}
    public void adjustAge(int direction){profile.adjustAge(direction);}
    public void adjustWeight(int direction){profile.adjustWeight(direction);}
    public void selectCharacter(int index){profile.selectCharacter(index);}
    public void selectExperience(int index){profile.selectExperience(index);}
    public void nextRehabilitationGoal(){profile.nextRehabilitationGoal();}
    public void nextActivity(){profile.nextActivity();}
    public void toggleGoal(int index){profile.toggleGoal(index);}
    public void rename(String value){profile.rename(value);}
    public String rehabilitationGoal(){return REHABILITATION_GOALS[profile.rehabilitationGoal()];}
    public String activity(){return ACTIVITY[profile.activity()];}
}
