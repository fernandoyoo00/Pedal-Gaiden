package com.pedalgaiden.application.usecase;

import java.util.*;
import com.pedalgaiden.domain.RaceState;
import com.pedalgaiden.domain.RaceState.*;
import static com.pedalgaiden.domain.RaceRules.*;
import static com.pedalgaiden.domain.RaceState.createNinjaBots;

public final class RideSession {
    private final RaceState state=new RaceState();
    private final Random random;
    private final List<Event> events=new ArrayList<>();
    public enum Event { PICKUP, RECYCLED }
    public RideSession(Random random){this.random=Objects.requireNonNull(random);}
    public RaceState state(){return state;}
    public List<Event> takeEvents(){List<Event> result=List.copyOf(events);events.clear();return result;}
    private void reset(){state.distance=state.elapsed=state.speed=state.cadence=state.spawn=state.pedalCycle=state.scoreEffectTime=state.effectX=state.cheerTime=0;state.turbo.reset();state.turboRiding=false;state.rhythm=100;state.score=state.recycled=0;state.items.clear();state.pathBins.clear();state.carriedItems.clear();state.effectPoints=0;state.feedback=Feedback.NONE;state.feedbackTime=0;}
    public void start(int level,int character,int duration){
        if(level<0||level>=4||character<0||character>=4||duration<=0)
            throw new IllegalArgumentException("Configuracao de corrida invalida");
        state.level=level;state.character=character;state.duration=duration;
        reset();state.lastCadence=0;
        state.cars.clear();state.nextTrafficLane=random.nextInt(activeLanes(level));
        state.ninjaBots.clear();state.ninjaBots.addAll(createNinjaBots(character));
        state.trafficTimer=state.collisionImmunity=state.laneEffectTime=state.impactEffectTime=0;
        state.lane=0;state.riderY=laneY(0);state.effectY=0;events.clear();
    }
    public void update(float dt,float rpm){
        if(!Float.isFinite(dt)||dt<0||!Float.isFinite(rpm)||rpm<0||rpm>160)
            throw new IllegalArgumentException("Tempo ou cadencia invalida");
        state.laneEffectTime=Math.max(0,state.laneEffectTime-dt);
        state.impactEffectTime=Math.max(0,state.impactEffectTime-dt);
        state.riderY+=(laneY(state.lane)-state.riderY)*Math.min(1,dt*12);
        state.collisionImmunity=Math.max(0,state.collisionImmunity-dt);
        state.turbo.update(dt);
        state.turboRiding=state.turbo.multiplier(rpm>0)>1;
        state.cadence+=(rpm-state.cadence)*Math.min(1,dt*3);
        if(rpm==0&&state.cadence<.1f)state.cadence=0;
        float targetSpeed=state.cadence*.11f*state.turbo.multiplier(rpm>0)*(state.collisionImmunity>1.5f?.4f:1);state.speed+=(targetSpeed-state.speed)*Math.min(1,dt*2.5f);
        if(targetSpeed==0&&state.speed<.02f)state.speed=0;
        if(state.cadence>=1)state.pedalCycle=(state.pedalCycle+state.cadence/60f*dt)%1;
        if(rpm<=0)state.rhythm=0;
        else state.rhythm+=(cadenceRhythmTarget(rpm,state.cadence,state.lastCadence)-state.rhythm)*Math.min(1,dt*1.5f);
        state.lastCadence=state.cadence;
        float movement=state.speed*dt*36;state.distance+=movement;state.elapsed+=dt;state.spawn+=movement;state.feedbackTime=Math.max(0,state.feedbackTime-dt);
        state.scoreEffectTime=Math.max(0,state.scoreEffectTime-dt);
        state.cheerTime=Math.max(0,state.cheerTime-dt);
        state.effectX-=movement;
        if(shouldSpawnCollectible(state.spawn)){
            state.spawn-=COLLECTIBLE_SPACING;
            int type=random.nextInt(5);
            int itemLane=random.nextInt(activeLanes(state.level));
            state.items.add(new Item(W+50,type,itemLane));
            state.pathBins.add(new PathBin(W+50+BIN_FOLLOW_DISTANCE,matchingBin(type)));
        }
        Iterator<Item> it=state.items.iterator();while(it.hasNext()){
            Item item=it.next();float previousX=item.x;item.x-=movement;
            if(crossedPickup(previousX,item.x)&&canReachLane(state.riderY,item.lane)){it.remove();state.carriedItems.addLast(item.type);events.add(Event.PICKUP);state.feedback=Feedback.PICKUP;state.feedbackTime=1.2f;}
            else if(item.x< -100)it.remove();
        }
        Iterator<PathBin> bins=state.pathBins.iterator();
        while(bins.hasNext()){
            PathBin bin=bins.next();
            if(bin.update(dt,movement)){
                if(recycleInto(bin.type,bin.x)){
                    bin.effectTime=1.2f;state.effectY=ROADSIDE_Y+95;
                }
            }
            if(bin.x< -100)bins.remove();
        }
        updateTraffic(dt,movement);
        for(NinjaBot bot:state.ninjaBots)bot.update(dt,movement);
    }
    private void updateTraffic(float dt,float movement){
        state.trafficTimer+=dt;
        if(state.trafficTimer>=trafficInterval(state.level)){
            state.trafficTimer=0;
            int carLane=state.nextTrafficLane;
            float velocity=trafficVelocity(carLane,random.nextInt(4));
            state.cars.add(new TrafficCar(W+80,random.nextInt(4),carLane,velocity));
            state.nextTrafficLane=random.nextInt(activeLanes(state.level));
        }
        Iterator<TrafficCar> traffic=state.cars.iterator();
        while(traffic.hasNext()){
            TrafficCar car=traffic.next();
            float previousX=car.x;
            car.x+=car.velocity*dt-movement;
            car.animationTime+=dt;
            if(!car.hit&&state.collisionImmunity==0&&trafficCollision(previousX,car.x,state.riderY,car.lane)){
                car.hit=true;
                state.collisionImmunity=3;
                state.impactEffectTime=.45f;
                state.speed*=.4f;
                state.cheerTime=0;
                state.feedback=Feedback.COLLISION;
                state.feedbackTime=2;
            }
            if(car.x< -CAR_WIDTH-100||car.x>W+300)traffic.remove();
        }
    }
    public void changeLane(int direction){
        if(direction!=1&&direction!=-1)throw new IllegalArgumentException("Direcao de faixa invalida");
        int nextLane=Math.max(0,Math.min(activeLanes(state.level)-1,state.lane+direction));
        if(nextLane!=state.lane){state.lane=nextLane;state.laneEffectTime=.35f;state.laneEffectDirection=direction;}
    }
    public boolean recycleInto(int bin,float x){
        DepositResult result=depositNext(state.carriedItems,bin);
        if(result==DepositResult.EMPTY){state.feedback=Feedback.EMPTY;state.feedbackTime=1.5f;return false;}
        if(result==DepositResult.WRONG_BIN){state.feedback=Feedback.WRONG_BIN;state.feedbackTime=1.5f;return false;}
        state.recycled++;
        events.add(Event.RECYCLED);
        state.cheerTime=CHEER_DURATION;
        state.score+=RECYCLE_POINTS;
        boolean turboActivated=state.turbo.recycle();
        state.effectX=x;
        state.effectPoints=RECYCLE_POINTS;
        state.scoreEffectTime=1.2f;
        state.feedback=Feedback.RECYCLED;
        if(turboActivated){state.score+=100;state.effectPoints=100;state.feedback=Feedback.TURBO;}
        state.feedbackTime=1.5f;
        return true;
    }
}
