package com.pedalgaiden.presentation.viewmodel;

import com.pedalgaiden.domain.RaceState;
import java.util.List;

/** Immutable frame data: views cannot mutate a running session. */
public record RaceUiState(
    float trafficTimer, float collisionImmunity, int nextTrafficLane, int effectPoints,
float scoreEffectTime, float effectX, float cheerTime, float laneEffectTime, float impactEffectTime,
int laneEffectDirection, int lane, int level, int character, int score, int recycled, int duration,
float riderY, float effectY, float distance, float elapsed, float speed, float cadence, float lastCadence,
float rhythm, float spawn, float feedbackTime, float pedalCycle, boolean turboRiding,
    EcoTurbo turbo, List<Item> items, List<Integer> carriedItems, List<PathBin> pathBins,
    List<TrafficCar> cars, List<NinjaBot> ninjaBots) {
    public record EcoTurbo(float energy,float remaining,int frame){}
    public record Item(float x,int type,int lane){}
    public record PathBin(float x,float effectTime,int type){}
    public record TrafficCar(float x,float animationTime,int type,int lane,float velocity){}
    public record NinjaBot(int character,float x,float y,float height,float pedalCycle){}
    static RaceUiState from(RaceState s){
        return new RaceUiState(
            s.trafficTimer,s.collisionImmunity,s.nextTrafficLane,s.effectPoints,s.scoreEffectTime,s.effectX,s.cheerTime,s.laneEffectTime,s.impactEffectTime,s.laneEffectDirection,s.lane,s.level,s.character,s.score,s.recycled,s.duration,s.riderY,s.effectY,s.distance,s.elapsed,s.speed,s.cadence,s.lastCadence,s.rhythm,s.spawn,s.feedbackTime,s.pedalCycle,s.turboRiding,
            new EcoTurbo(s.turbo.energy,s.turbo.remaining,s.turbo.frame()),
            s.items.stream().map(v->new Item(v.x,v.type,v.lane)).toList(),
            List.copyOf(s.carriedItems),
            s.pathBins.stream().map(v->new PathBin(v.x,v.effectTime,v.type)).toList(),
            s.cars.stream().map(v->new TrafficCar(v.x,v.animationTime,v.type,v.lane,v.velocity)).toList(),
            s.ninjaBots.stream().map(v->new NinjaBot(v.character,v.x,v.y,v.height,v.pedalCycle)).toList());
    }
}
