package com.pedalgaiden.presentation.viewmodel;

import com.pedalgaiden.application.port.AudioOutput;
import com.pedalgaiden.application.port.CadenceSource;
import com.pedalgaiden.application.usecase.RideSession;
import java.util.Random;
import static com.pedalgaiden.domain.RaceRules.stageFinished;

public final class RaceViewModel {
    private final RideSession session;
    private final CadenceSource cadence;
    private final AudioOutput audio;
    private final boolean iot;
    private RaceUiState state;

    public RaceViewModel(AudioOutput audio,CadenceSource cadence,boolean iot){
        this(new RideSession(new Random(42)),audio,cadence,iot);
    }
    public RaceViewModel(RideSession session,AudioOutput audio,CadenceSource cadence,boolean iot){
        this.session=session;this.audio=audio;this.cadence=cadence;this.iot=iot;state=RaceUiState.from(session.state());
    }
    public RaceUiState state(){return state;}
    public void start(int level,int character,int duration){session.start(level,character,duration);state=RaceUiState.from(session.state());}
    public void update(float dt,float demoRpm,int direction){
        if(direction!=0)session.changeLane(direction);
        session.update(dt,iot?cadence.rpm():demoRpm);
        state=RaceUiState.from(session.state());
        for(RideSession.Event event:session.takeEvents()){
            switch(event){
                case PICKUP -> audio.pickup();
                case RECYCLED -> audio.recycled();
            }
        }
    }
    public void changeLane(int direction){session.changeLane(direction);state=RaceUiState.from(session.state());}
    public boolean finished(){return stageFinished(state.elapsed(),state.duration());}
    public boolean sensorConnected(){return cadence.connected();}
    public String feedback(){
        return switch(session.state().feedback){
            case NONE -> "";
            case PICKUP -> "COLETADO - LIXEIRA \u00c0 FRENTE";
            case COLLISION -> "COLIS\u00c3O - MUDE DE FAIXA";
            case EMPTY -> "SEM ITEM PARA RECICLAR";
            case WRONG_BIN -> "ITEM GUARDADO PARA A LIXEIRA CERTA";
            case RECYCLED -> "+20 RECICLADO";
            case TURBO -> "TURBO ECOL\u00d3GICO! +100";
        };
    }
}
