package com.pedalgaiden.domain;

import java.util.*;

public final class RaceRules {
    private RaceRules(){}
    public static final float W=1280, CAR_WIDTH=156, ROAD_BOTTOM=90, LANE_HEIGHT=60;
    public static final float CHEER_DURATION=1.2f, COLLECTIBLE_SPACING=1600, BIN_FOLLOW_DISTANCE=360;
    public static final float ROADSIDE_Y=ROAD_BOTTOM+LANE_HEIGHT*4-12;
    private static final int[] WASTE_BINS={3,1,0,2,4};
    public static final int RECYCLE_POINTS=20;
    public enum DepositResult { EMPTY, WRONG_BIN, RECYCLED }
    public static float trafficInterval(int stage){return 8-Math.min(3,stage);}
    public static boolean trafficWarning(float timer,int stage){return timer>=trafficInterval(stage)-1.5f;}
    public static float trafficVelocity(int lane,int variant){return lane<2?55+variant*15:-(90+variant*25);}
    public static boolean trafficCollision(float previousX,float x,float y,int lane){
        return Math.abs(y-laneY(lane))<24&&Math.min(previousX,x)<275
                &&Math.max(previousX,x)+CAR_WIDTH>195;
    }
    public static int activeLanes(int stage){return 4;}
    public static float laneY(int lane){return ROAD_BOTTOM+LANE_HEIGHT*(lane+.5f);}
    public static boolean canReachLane(float y,int lane){
        return Math.abs(y-laneY(lane))<=12;
    }
    public static boolean shouldSpawnCollectible(float travelledSinceSpawn){return travelledSinceSpawn>=COLLECTIBLE_SPACING;}
    public static int cheerFrame(float remainingTime){
        return Math.max(0,Math.min(7,(int)((CHEER_DURATION-remainingTime)/CHEER_DURATION*8)));
    }
    public static boolean stageFinished(float elapsed,int durationMinutes){return elapsed>=durationMinutes*60f;}
    public static boolean crossedPickup(float previousX,float x){return previousX>=175&&x<=255;}
    public static int matchingBin(int wasteType){return WASTE_BINS[wasteType];}
    public static float cadenceRhythmTarget(float rpm,float cadence,float previousCadence){
        if(rpm<=0)return 0;
        return Math.max(0,100-Math.abs(cadence-previousCadence)*3);
    }
    public static DepositResult depositNext(Deque<Integer> queue,int bin){
        if(queue.isEmpty())return DepositResult.EMPTY;
        Iterator<Integer> items=queue.iterator();
        while(items.hasNext()){
            if(matchingBin(items.next())==bin){items.remove();return DepositResult.RECYCLED;}
        }
        return DepositResult.WRONG_BIN;
    }
}
