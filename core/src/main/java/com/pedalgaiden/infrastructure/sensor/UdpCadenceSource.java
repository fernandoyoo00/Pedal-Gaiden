package com.pedalgaiden.infrastructure.sensor;

import com.pedalgaiden.application.port.CadenceSource;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UdpCadenceSource implements CadenceSource {
    private static final Logger LOG=Logger.getLogger(UdpCadenceSource.class.getName());
    private final DatagramSocket socket;
    private final LongSupplier clock;
    private final Thread receiver;
    private volatile Reading reading;
    private record Reading(float rpm,long receivedAt){}

    public UdpCadenceSource(int port)throws SocketException{
        this(port,System::nanoTime);
    }
    public UdpCadenceSource(int port,LongSupplier clock)throws SocketException{
        this.clock=clock;
        socket=new DatagramSocket(new InetSocketAddress("127.0.0.1",port));
        receiver=new Thread(this::receive,"pedal-sensor");
        receiver.setDaemon(true);receiver.start();
    }
    private void receive(){
        byte[] data=new byte[128];
        while(!socket.isClosed()){
            try{
                DatagramPacket packet=new DatagramPacket(data,data.length);
                socket.receive(packet);
                String text=new String(packet.getData(),packet.getOffset(),packet.getLength(),StandardCharsets.US_ASCII).trim();
                try{
                    reading=new Reading(parseRpm(text),clock.getAsLong());
                }catch(IllegalArgumentException ex){
                    LOG.log(Level.WARNING,"Pacote de cadencia invalido: {0}",ex.getMessage());
                }
            }catch(IOException ex){
                if(!socket.isClosed()){
                    LOG.log(Level.SEVERE,"Falha ao receber cadencia; sensor desconectado",ex);
                    socket.close();
                }
            }
        }
    }
    public static float parseRpm(String text){
        if(!text.startsWith("RPM:"))throw new IllegalArgumentException("Esperado RPM:<valor>");
        float rpm=Float.parseFloat(text.substring(4));
        if(!Float.isFinite(rpm)||rpm<0||rpm>160)throw new IllegalArgumentException("RPM fora de 0-160");
        return rpm;
    }
    private boolean fresh(Reading value){
        return !socket.isClosed()&&value!=null&&clock.getAsLong()-value.receivedAt()<2_000_000_000L;
    }
    @Override public float rpm(){Reading value=reading;return fresh(value)?value.rpm():0;}
    @Override public boolean connected(){return fresh(reading);}
    public int port(){return socket.getLocalPort();}
    @Override public void close(){
        socket.close();
        try{receiver.join(1000);}
        catch(InterruptedException ex){Thread.currentThread().interrupt();LOG.log(Level.WARNING,"Espera do sensor interrompida",ex);}
    }
}
