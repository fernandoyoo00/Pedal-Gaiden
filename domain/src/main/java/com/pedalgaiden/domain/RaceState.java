package com.pedalgaiden.domain;

import java.util.*;
import static com.pedalgaiden.domain.RaceRules.*;

public final class RaceState {
    public final EcoTurbo turbo=new EcoTurbo();
    public final List<Item> items=new ArrayList<>();
    public final Deque<Integer> carriedItems=new ArrayDeque<>();
    public final List<PathBin> pathBins=new ArrayList<>();
    public final List<TrafficCar> cars=new ArrayList<>();
    public final List<NinjaBot> ninjaBots=new ArrayList<>();
    public float trafficTimer, collisionImmunity;
    public int nextTrafficLane, effectPoints;
    public float scoreEffectTime, effectX, cheerTime, laneEffectTime, impactEffectTime;
    public int laneEffectDirection=1, lane, level, character, score, recycled, duration=5;
    public float riderY=120, effectY, distance, elapsed, speed, cadence, lastCadence, rhythm=100;
    public float spawn, feedbackTime, pedalCycle;
    public boolean turboRiding;
    public Feedback feedback=Feedback.NONE;
    public enum Feedback { NONE, PICKUP, COLLISION, EMPTY, WRONG_BIN, RECYCLED, TURBO }

    public static final class Item {
        public float x;
        public final int type, lane;
        public Item(float x,int type,int lane){this.x=x;this.type=type;this.lane=lane;}
    }
    public static final class EcoTurbo {
        public float energy, remaining, animationTime;
        public boolean recycle(){
            energy=Math.min(100,energy+12);
            if(energy<100)return false;
            energy=0;remaining=5;animationTime=0;return true;
        }
        public void update(float dt){
            if(remaining>0)animationTime+=Math.min(dt,remaining);
            remaining=Math.max(0,remaining-dt);
        }
        public int frame(){return (int)(animationTime*10)%4;}
        public float multiplier(boolean pedalling){return remaining>0&&pedalling?1.5f:1;}
        public void reset(){energy=remaining=animationTime=0;}
    }
    public static final class PathBin {
        public float x, effectTime;
        public final int type;
        public boolean passed;
        public PathBin(float x,int type){this.x=x;this.type=type;}
        public boolean update(float dt,float movement){
            float previousX=x;
            x-=movement;
            effectTime=Math.max(0,effectTime-dt);
            if(!passed&&crossedPickup(previousX,x)){passed=true;return true;}
            return false;
        }
    }
    public static final class TrafficCar {
        public float x, animationTime;
        public final int type, lane;
        public final float velocity;
        public boolean hit;
        public TrafficCar(float x,int type,int lane,float velocity){
            this.x=x;this.type=type;this.lane=lane;this.velocity=velocity;
        }
    }
    public static final class NinjaBot {
        public final int character;
        public final float anchorX, y, height;
        public float x, pedalCycle, rideTime, reentryDelay;
        public NinjaBot(int character,float anchorX,float y,float height){
            this.character=character;this.anchorX=anchorX;this.x=anchorX;this.y=y;this.height=height;
        }
        public void update(float dt,float movement){
            if(reentryDelay>0){reentryDelay=Math.max(0,reentryDelay-dt);return;}
            float cycles=dt*(.8f+character*.08f);
            pedalCycle=(pedalCycle+cycles)%1;
            rideTime+=cycles;
            x+=(25+character*8)*dt+movement*.4f;
            if(x>W+80){x=-160-character*120;reentryDelay=8+character*3;}
        }
    }
    public static List<NinjaBot> createNinjaBots(int playerCharacter){
        List<NinjaBot> bots=new ArrayList<>();
        for(int i=0;i<3;i++)bots.add(new NinjaBot((playerCharacter+i+1)%4,420+i*270,
                ROADSIDE_Y+i*4,48-i*4));
        return bots;
    }
}
