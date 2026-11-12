package com.pedalgaiden;

import com.pedalgaiden.application.port.CadenceSource;
import com.pedalgaiden.infrastructure.audio.GameAudio;
import com.pedalgaiden.infrastructure.sensor.UdpCadenceSource;
import java.net.SocketException;

/** Composition root resources shared across screen transitions. */
public final class GameServices implements AutoCloseable {
    private final GameAudio audio;
    private final CadenceSource cadence;
    private final boolean iot;
    public GameServices(boolean iot){
        this.iot=iot;
        try{
            cadence=iot?new UdpCadenceSource(5055):new CadenceSource(){
                @Override public float rpm(){return 0;}
                @Override public boolean connected(){return false;}
                @Override public void close(){}
            };
        }catch(SocketException ex){
            throw new IllegalStateException("Nao foi possivel abrir a porta UDP 5055 do sensor",ex);
        }
        audio=new GameAudio();
    }
    public boolean iot(){return iot;}
    public GameAudio audio(){return audio;}
    public CadenceSource cadence(){return cadence;}
    @Override public void close(){cadence.close();audio.dispose();}
}
