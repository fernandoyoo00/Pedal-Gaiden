package com.pedalgaiden.presentation.view;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.pedalgaiden.presentation.viewmodel.FlowViewModel;
import com.pedalgaiden.presentation.viewmodel.RaceUiState.*;
import static com.pedalgaiden.domain.RaceRules.*;
import static com.pedalgaiden.presentation.view.ViewArt.*;
import static com.pedalgaiden.presentation.view.ViewLayout.*;

final class RaceView {
    private final FlowViewModel flow;
    private final DrawContext draw;
    private final boolean iot;
    RaceView(FlowViewModel flow,DrawContext draw,boolean iot){this.flow=flow;this.draw=draw;this.iot=iot;}

    void drawCountdown(){
        // ShapeRenderer does not enable alpha blending itself.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        draw.rect(0,0,W,H,draw.rgba(0,0,.03f,.25f));
        Gdx.gl.glDisable(GL20.GL_BLEND);
        int number=flow.countdownNumber();
        TextureRegion artwork=draw.assets.artwork("countdown_"+number);
        float scale=Math.min(440f/artwork.getRegionWidth(),200f/artwork.getRegionHeight());
        float width=artwork.getRegionWidth()*scale,height=artwork.getRegionHeight()*scale;
        draw.batch.begin();draw.batch.setColor(Color.WHITE);draw.batch.draw(artwork,(W-width)/2,(H-height)/2,width,height);draw.batch.end();
    }

    private void background(){draw.pic("bg_"+bg[flow.level()],0,0,W,H);}

    private void ridingBackground(){
        if(flow.level()==0){
            float[] bottoms={0,330,330,330,330,ROAD_BOTTOM};
            float[] heights={H,260,160,180,55,240};
            for(int i=0;i<FUJI_LAYERS.length;i++)scrollLayer(draw.assets.fuji(FUJI_LAYERS[i]),bottoms[i],heights[i],LAYER_SPEEDS[i]);
            return;
        }
        Texture scenery=draw.assets.image("bg_"+bg[flow.level()]);
        float offset=(flow.race().state().distance()*.35f)%(W*2);
        draw.batch.begin();
        draw.batch.setColor(Color.WHITE);
        for(int i=0;i<3;i++){
            float x=i*W-offset;
            draw.batch.draw(scenery,x,0,W,H,0,0,scenery.getWidth(),scenery.getHeight(),i%2==1,false);
        }
        draw.batch.end();
    }

    private void scrollLayer(TextureRegion layer,float y,float height,float speedFactor){
        float width=ViewLayout.layerWidth(layer.getRegionWidth(),layer.getRegionHeight(),height);
        float offset=(flow.race().state().distance()*speedFactor)%(width*2);
        int copies=(int)Math.ceil(W/width)+2;
        draw.batch.begin();draw.batch.setColor(Color.WHITE);
        for(int i=0;i<copies;i++){
            boolean mirrored=i%2==1;
            draw.batch.draw(layer.getTexture(),i*width-offset,y,width,height,
                    mirrored?layer.getU2():layer.getU(),layer.getV2(),
                    mirrored?layer.getU():layer.getU2(),layer.getV());
        }
        draw.batch.end();
    }

    private void road(){
        if(flow.level()!=0)draw.rect(0,ROAD_BOTTOM,W,240,draw.rgba(.12f,.14f,.20f,1));
        for(int i=1;flow.level()!=0&&i<4;i++){
            float y=ROAD_BOTTOM+i*LANE_HEIGHT;
            if(i==2){draw.rect(0,y-3,W,2,Color.GOLD);draw.rect(0,y+2,W,2,Color.GOLD);}
            else for(int dash=0;dash<17;dash++)draw.rect(dash*84-flow.race().state().distance()%84,y,42,3,Color.WHITE);
        }
    }

    void gameplay(){
        ridingBackground();
        draw.rect(0,0,W,83,draw.rgba(.015f,.045f,.09f,.72f));
        road();
        drawNinjaBots();
        TextureRegion pavilion=draw.assets.artwork("pavilion");
        float pavilionWidth=280, pavilionHeight=pavilionWidth*pavilion.getRegionHeight()/pavilion.getRegionWidth();
        for(PathBin bin:flow.race().state().pathBins()){
            draw.batch.begin();draw.batch.setColor(Color.WHITE);
            draw.batch.draw(pavilion,bin.x()+32-pavilionWidth/2,ROADSIDE_Y-5,pavilionWidth,pavilionHeight);
            draw.batch.end();
        }
        drawTraffic();
        for(Item item:flow.race().state().items()){
            float y=laneY(item.lane())-12;
            Texture collectible=draw.assets.image(waste[item.type()]);
            float scale=ViewLayout.spriteScale(collectible.getWidth(),collectible.getHeight(),48,48);
            float width=collectible.getWidth()*scale, height=collectible.getHeight()*scale;
            draw.pic(waste[item.type()],item.x()+(54-width)/2,y,width,height);
        }
        for(PathBin bin:flow.race().state().pathBins()){
            String state=bin.effectTime()>0?"open":"closed";
            float y=ROADSIDE_Y;
            draw.pic("bin_"+BIN_COLORS[bin.type()]+"_"+state,bin.x(),y,64,92);
            draw.centeredLabel(BIN_LABELS[bin.type()],bin.x()+32,y+110,.8f,Color.WHITE);
            if(bin.effectTime()>0){
                drawRideEffect("recycle_effect",bin.effectTime(),1.2f,bin.x()+32,y+60,125,100,false);
                draw.batch.begin();
                draw.batch.setColor(Color.WHITE);
                draw.batch.draw(draw.assets.image("bin_sparkles"),bin.x()-5,y+69,74,60,0,0,1,.35f);
                draw.batch.end();
            }
        }
        if(flow.race().state().scoreEffectTime()>0)draw.pic("points_"+flow.race().state().effectPoints(),flow.race().state().effectX(),flow.race().state().effectY()+(1.2f-flow.race().state().scoreEffectTime())*30,90,55);
        int frame=flow.race().state().cadence()>=1?(int)(flow.race().state().pedalCycle()*6):0;
        float bob=flow.race().state().cadence()>=1?MathUtils.sin(flow.race().state().pedalCycle()*MathUtils.PI2*2)*2:0;
        float feet=flow.race().state().riderY()-12+bob;
        if(flow.race().state().turboRiding()){
            Texture boost=draw.assets.image(ninjas[flow.profile().character()]+"_boost_"+flow.race().state().turbo().frame());
            float boostWidth=100f*boost.getWidth()/boost.getHeight();
            draw.batch.begin();draw.batch.setColor(flow.race().state().collisionImmunity()>0?Color.PINK:Color.WHITE);
            draw.batch.draw(boost,299-boostWidth,feet,boostWidth,100);
            draw.batch.setColor(Color.WHITE);draw.batch.end();
        }else if(flow.race().state().cheerTime()>0){
            Texture cheer=draw.assets.image(ninjas[flow.profile().character()]+"_cheer_"+cheerFrame(flow.race().state().cheerTime()));
            float width=96f*cheer.getWidth()/cheer.getHeight();
            draw.batch.begin();
            draw.batch.setColor(flow.race().state().collisionImmunity()>0?Color.PINK:Color.WHITE);
            draw.batch.draw(cheer,235-width/2,feet,width,96);
            draw.batch.setColor(Color.WHITE);
            draw.batch.end();
        }else{
            draw.batch.begin();
            draw.batch.setColor(flow.race().state().collisionImmunity()>0?Color.PINK:Color.WHITE);
            draw.batch.draw(draw.assets.image(ninjas[flow.profile().character()]+"_ride_"+frame),171,feet,128,96);
            draw.batch.setColor(Color.WHITE);
            draw.batch.end();
        }
        drawRideEffect("lane_effect",flow.race().state().laneEffectTime(),.35f,235,feet+20,130,55,flow.race().state().laneEffectDirection()<0);
        drawRideEffect("impact_effect",flow.race().state().impactEffectTime(),.45f,235,feet+40,120,90,false);
        drawGameplayHud();
        drawTrafficWarnings();
        int remaining=(int)Math.ceil(Math.max(0,flow.duration()*60-flow.race().state().elapsed()));
        draw.text(String.format(PT_BR,"DIST\u00c2NCIA %.0f     TEMPO %02d:%02d",flow.race().state().distance(),remaining/60,remaining%60),24,28,1.3f,Color.WHITE);
        draw.button("PAUSAR",1110,20,145,45,false);
        if(flow.race().state().feedbackTime()>0)draw.centeredLabel(flow.race().feedback(),640,300,1.4f,Color.YELLOW);
        draw.button("FAIXA -",490,18,160,46,false);
        draw.button("FAIXA +",665,18,160,46,false);
        draw.text("FAIXA "+(flow.race().state().lane()+1)+"/"+activeLanes(flow.level())+"   ITENS "+flow.race().state().carriedItems().size(),24,66,1.1f,Color.WHITE);
        if(!iot)draw.centeredLabel("ESPA\u00c7O/SETA ACIMA: PEDALAR   SHIFT: ACELERAR   W/S: FAIXAS",W/2,510,1f,Color.WHITE);
        else draw.centeredLabel(flow.race().sensorConnected()?"SENSOR CONECTADO - USE OS CONTROLES DE FAIXA NA TELA":"AGUARDANDO SENSOR",W/2,510,1f,Color.WHITE);
    }

    private void drawGameplayHud(){
        TextureRegion frame=draw.assets.artwork("status_frame");
        float width=HUD_WIDTH,height=width*frame.getRegionHeight()/frame.getRegionWidth();
        float x=(W-width)/2,y=H-height-10;
        // Fill the transparent panel windows before drawing the decorative frame.
        Color backing=draw.rgba(.025f,.02f,.06f,1);
        draw.rect(x+width*.045f,y+height*.13f,width*.33f,height*.40f,backing);
        draw.rect(x+width*.395f,y+height*.13f,width*.245f,height*.40f,backing);
        draw.rect(x+width*.665f,y+height*.13f,width*.31f,height*.40f,backing);
        draw.batch.begin();draw.batch.setColor(Color.WHITE);draw.batch.draw(frame,x,y,width,height);draw.batch.end();
        float upper=y+height*.44f,lower=y+height*.26f;
        draw.fittedLabel("FASE "+(flow.level()+1)+" - "+stages[flow.level()],x+width*.06f,upper-15,width*.30f,20,1.15f,Color.WHITE);
        draw.fittedLabel("PONTOS "+flow.race().state().score()+"   RECICLADOS "+flow.race().state().recycled(),x+width*.06f,lower-15,width*.30f,20,1.05f,Color.GOLD);
        draw.text(String.format(PT_BR,"CAD\u00caNCIA %.0f RPM",flow.race().state().cadence()),x+width*.42f,upper,1.1f,Color.WHITE);
        draw.text(String.format(PT_BR,"RITMO %.0f%%",flow.race().state().rhythm()),x+width*.42f,lower,1.1f,Color.LIME);
        draw.text(flow.race().state().turbo().remaining()>0?String.format(PT_BR,"TURBO ECO %.1fs",flow.race().state().turbo().remaining()):
                String.format(PT_BR,"ENERGIA ECO %.0f%%",flow.race().state().turbo().energy()),x+width*.69f,upper,1.1f,Color.WHITE);
        float fill=flow.race().state().turbo().remaining()>0?flow.race().state().turbo().remaining()/5:flow.race().state().turbo().energy()/100;
        for(int segment=0;segment<10;segment++){
            float amount=MathUtils.clamp(fill*10-segment,0,1);
            float segmentX=x+width*.69f+segment*26,segmentY=lower-12;
            draw.rect(segmentX-1,segmentY-1,24,16,Color.BLACK);
            draw.rect(segmentX,segmentY,22,14,Color.DARK_GRAY);
            if(amount>0){
                Color color=flow.race().state().turbo().remaining()>0?Color.GOLD:Color.LIME;
                draw.rect(segmentX,segmentY,22*amount,14,color);
                draw.rect(segmentX,segmentY+11,22*amount,2,Color.WHITE);
            }
        }
    }

    private void drawNinjaBots(){
        draw.batch.begin();
        draw.batch.setColor(.80f,.84f,.90f,1);
        for(int i=flow.race().state().ninjaBots().size()-1;i>=0;i--){
            NinjaBot bot=flow.race().state().ninjaBots().get(i);
            int frame=(int)(bot.pedalCycle()*6);
            float bob=MathUtils.sin(bot.pedalCycle()*MathUtils.PI2*2)*.75f;
            float width=bot.height()*128f/96;
            Texture sprite=draw.assets.image(ninjas[bot.character()]+"_ride_"+frame);
            draw.batch.draw(sprite,bot.x()-width/2,bot.y()+bob,width,bot.height(),
                    0,0,sprite.getWidth(),sprite.getHeight(),false,false);
        }
        draw.batch.setColor(Color.WHITE);
        draw.batch.end();
    }

    private void drawRideEffect(String key,float remaining,float effectDuration,float centerX,float centerY,
            float maxWidth,float maxHeight,boolean flip){
        if(remaining<=0)return;
        Texture texture=draw.assets.image(key);
        float scale=ViewLayout.spriteScale(texture.getWidth(),texture.getHeight(),maxWidth,maxHeight);
        float width=texture.getWidth()*scale, height=texture.getHeight()*scale;
        draw.batch.begin();
        draw.batch.setColor(1,1,1,remaining/effectDuration);
        draw.batch.draw(texture,centerX-width/2,centerY-height/2,width,height,0,0,
                texture.getWidth(),texture.getHeight(),flip,false);
        draw.batch.setColor(Color.WHITE);
        draw.batch.end();
    }

    private void drawTrafficWarnings(){
        float height=TRAFFIC_WARNING_HEIGHT;
        for(int warningLane=0;warningLane<activeLanes(flow.level());warningLane++){
            boolean visible=trafficWarning(flow.race().state().trafficTimer(),flow.level())&&warningLane==flow.race().state().nextTrafficLane();
            for(TrafficCar car:flow.race().state().cars()){
                if(car.lane()==warningLane&&car.x()>W-240){visible=true;break;}
            }
            if(visible){
                float x=W-TRAFFIC_WARNING_WIDTH-16,y=laneY(warningLane)-height/2;
                draw.rect(x-2,y-2,TRAFFIC_WARNING_WIDTH+4,height+4,Color.GOLD);
                draw.rect(x,y,TRAFFIC_WARNING_WIDTH,height,draw.rgba(.15f,.025f,.02f,1));
                draw.fittedLabel("ATEN\u00c7\u00c3O",x+5,y+22,TRAFFIC_WARNING_WIDTH-10,18,1.1f,Color.GOLD);
                draw.fittedLabel("VE\u00cdCULO \u00c0 FRENTE",x+5,y+3,TRAFFIC_WARNING_WIDTH-10,18,1,Color.WHITE);
            }
        }
    }

    private void drawTraffic(){
        for(int carLane=3;carLane>=0;carLane--){
            for(TrafficCar car:flow.race().state().cars()){
                if(car.lane()!=carLane)continue;
                Texture frame=draw.assets.image(CAR_NAMES[car.type()]+(1+(int)(car.animationTime()*8)%4));
                float height=CAR_WIDTH*frame.getHeight()/frame.getWidth();
                draw.batch.begin();draw.batch.setColor(Color.WHITE);
                draw.batch.draw(frame,car.x(),laneY(car.lane())-16,CAR_WIDTH,height,0,0,
                        frame.getWidth(),frame.getHeight(),car.velocity()<0,false);
                draw.batch.end();
            }
        }
    }

    void overlay(){draw.rect(0,0,W,H,draw.rgba(0,0,.05f,.70f));}

    void pauseMenu(){draw.panel(430,170,420,440);draw.centeredLabel("PAUSADO",640,550,3,Color.WHITE);draw.button("CONTINUAR",480,435,320,62,true);draw.button("REINICIAR",480,345,320,62,false);draw.button("MENU PRINCIPAL",480,255,320,62,false);}

    void results(){background();overlay();draw.panel(310,95,660,545);draw.fittedLabel("FASE CONCLU\u00cdDA!",340,530,600,50,3,Color.YELLOW);draw.pic(ninjas[flow.profile().character()]+"0",340,250,240,230);draw.text("PONTOS: "+flow.race().state().score(),610,455,1.7f,Color.WHITE);draw.text("RECICLADOS: "+flow.race().state().recycled(),610,410,1.7f,Color.WHITE);draw.text(String.format(PT_BR,"DIST\u00c2NCIA: %.0f",flow.race().state().distance()),610,365,1.7f,Color.WHITE);draw.text(String.format(PT_BR,"RITMO: %.0f%%",flow.race().state().rhythm()),610,320,1.7f,Color.WHITE);draw.button("REPETIR",345,135,185,55,false);draw.button("PR\u00d3XIMA FASE",550,135,220,55,true);draw.button("MENU",785,135,160,55,false);}
}
