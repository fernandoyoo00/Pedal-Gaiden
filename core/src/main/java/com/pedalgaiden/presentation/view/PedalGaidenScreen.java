package com.pedalgaiden.presentation.view;

import com.pedalgaiden.GameServices;
import com.pedalgaiden.presentation.viewmodel.FlowViewModel;
import com.pedalgaiden.presentation.viewmodel.FlowViewModel.Page;
import static com.pedalgaiden.domain.RaceRules.*;
import static com.pedalgaiden.presentation.view.ViewArt.*;
import static com.pedalgaiden.presentation.view.ViewLayout.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.*;

/** libGDX view: rendering and input hit testing; state and commands belong to ViewModels. */
public final class PedalGaidenScreen implements Screen {
    private final boolean iot;
    private final Game hostGame;
    private final GameServices services;
    private final FlowViewModel flow;
    private final DrawContext draw=new DrawContext();
    private final ProfileView profileView;
    private final RaceView raceView;
    private final OrthographicCamera cam=new OrthographicCamera();
    private final FitViewport viewport=new FitViewport(W,H,cam);
    public PedalGaidenScreen(Game hostGame,GameServices services,boolean startAtProfile){
        this.hostGame=hostGame;this.services=services;this.iot=services.iot();
        flow=new FlowViewModel(services.audio(),services.cadence(),iot);
        profileView=new ProfileView(flow,draw);raceView=new RaceView(flow,draw,iot);
        if(!startAtProfile)flow.goTo(Page.HELP);
    }

    private void returnToMainMenu(){hostGame.setScreen(new MainMenuScreen(hostGame,services));dispose();}
    private void leaveFlow(){if(hostGame!=null)returnToMainMenu();else flow.goTo(Page.PROFILE);}

    @Override public void show(){
        flow.resumeAudio();
        cam.position.set(W/2,H/2,0);viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        for(String name:bg)draw.assets.load("bg_"+name,"backgrounds/"+name+".png");
        for(String layer:FUJI_LAYERS)draw.assets.loadFujiLayer(layer);
        draw.assets.load("level_menu_bg","backgrounds/menuBg.png");
        draw.assets.loadArtwork("survey_panel","menu/RetroPixelGameUIPanel.png");
        draw.assets.loadArtwork("countdown_3","gameplay/countdown/countdown_3_red.png");
        draw.assets.loadArtwork("countdown_2","gameplay/countdown/countdown_2_amber.png");
        draw.assets.loadArtwork("countdown_1","gameplay/countdown/countdown_1_green.png");
        draw.assets.loadArtwork("countdown_0","gameplay/countdown/countdown_start_green.png");
        for(String n:ninjas)for(int i=0;i<8;i++)draw.assets.load(n+i,"characters/"+n+"_"+i+".png");
        for(String n:ninjas)for(int i=0;i<6;i++)draw.assets.load(n+"_ride_"+i,"characters/ninja_sprites/"+n+"_ride_"+i+".png");
        for(String car:CAR_NAMES)for(int i=1;i<=4;i++)draw.assets.load(car+i,"obstacles/cars/"+car+"/"+car+"_"+i+".png");
        draw.assets.loadArtwork("status_frame","gameplay/Pixel-Art Fuji Blossom Status Bar.png");
        draw.assets.loadArtwork("pavilion","gameplay/JapaneseRecyclingPavilion.png");
        for(String n:ninjas)for(int frame=0;frame<4;frame++)
            draw.assets.load(n+"_boost_"+frame,"characters/boostAnimation/"+n+"/"+n+"_boost_0"+frame+".png");
        draw.assets.load("recycle_effect","gameplay/powerEffects/03_recycling_pickup_green.png");
        for(String n:ninjas)for(int i=0;i<8;i++)draw.assets.load(n+"_cheer_"+i,"gameplay/cheering/"+n+"_cheer_"+i+".png");
        for(String name:mainCharacters)draw.assets.load(name,"characters/"+name+".png");
        for(String n:waste)draw.assets.load(n,"collectibles/"+n+".png");
        for(String color:BIN_COLORS){
            draw.assets.load("bin_"+color+"_closed","gameplay/bins/"+color+"_closed.png");
            draw.assets.load("bin_"+color+"_open","gameplay/bins/"+color+"_open.png");
        }
        draw.assets.load("bin_sparkles","gameplay/bins/blue_collect_sparkles.png");
        draw.assets.load("points_20","gameplay/score/points_20.png");
        draw.assets.load("points_100","gameplay/score/points_100.png");
        draw.assets.load("lane_effect","gameplay/powerEffects/01_lane_change_blue.png");
        draw.assets.load("impact_effect","gameplay/powerEffects/04_car_collision.png");
        for(String name:goalIconNames)draw.assets.load("goal_"+name,"menu/pedalgaiden_goal_icons/ui_icon_goal_"+name+".png");
        for(String name:helpIconNames)draw.assets.load("help_"+name,"menu/pedalgaiden_howtoplay_icons/ui_icon_howto_"+name+".png");
        draw.assets.load("goal_checkbox_on","menu/pedalgaiden_goal_icons/ui_checkbox_on.png");
        draw.assets.load("goal_checkbox_off","menu/pedalgaiden_goal_icons/ui_checkbox_off.png");
    }

    @Override public void render(float delta){
        float rpm=Gdx.input.isKeyPressed(Input.Keys.SPACE)||Gdx.input.isKeyPressed(Input.Keys.UP)
                ?(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)?90:65):0;
        int direction=0;
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)||Gdx.input.isKeyJustPressed(Input.Keys.S))direction=-1;
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)||Gdx.input.isKeyJustPressed(Input.Keys.W))direction=1;
        flow.update(delta,rpm,direction);
        Gdx.gl.glClearColor(.025f,.055f,.12f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();draw.batch.setProjectionMatrix(cam.combined);draw.shapes.setProjectionMatrix(cam.combined);
        switch(flow.page()){
            case PROFILE -> profileView.profile(); case GOALS -> profileView.goals();
            case EXPERIENCE -> profileView.experience(); case CONFIRM -> profileView.confirm(); case LEVELS -> profileView.levels();
            case COUNTDOWN -> {raceView.gameplay();raceView.drawCountdown();}
            case PLAY -> raceView.gameplay(); case PAUSE -> {raceView.gameplay();raceView.overlay();raceView.pauseMenu();}
            case RESULTS -> raceView.results(); case HELP -> help();
        }
        if(Gdx.input.justTouched()){flow.clicked();Vector3 pt=cam.unproject(new Vector3(Gdx.input.getX(),Gdx.input.getY(),0),viewport.getScreenX(),viewport.getScreenY(),viewport.getScreenWidth(),viewport.getScreenHeight());click(pt.x,pt.y);}
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){if(flow.page()==Page.PLAY||flow.page()==Page.COUNTDOWN)flow.pauseRace();else if(flow.page()==Page.PAUSE)flow.resumeRace();else leaveFlow();}
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)&&(flow.page()==Page.PLAY||flow.page()==Page.COUNTDOWN))flow.pauseRace();
    }

    private void help(){
        draw.pic("bg_"+bg[flow.level()],0,0,W,H);draw.panel(300,32,680,656);
        draw.batch.begin();draw.centeredText("COMO JOGAR",640,625,2.0f,Color.WHITE);draw.batch.end();
        draw.rect(350,584,580,3,draw.rgba(.19f,.63f,.96f,.85f));
        String[] instructions={
                "Pedale para avan\u00e7ar\n(o jogo usa sua cad\u00eancia)",
                "Passe pelos itens na mesma faixa\npara coletar automaticamente",
                "Mude de faixa para evitar carros\nColis\u00f5es reduzem a velocidade",
                "Lixeiras reciclam de qualquer faixa\nUse W/S ou os bot\u00f5es de faixa",
                "Encha a energia para ativar o turbo\nRecicle para restaurar o ambiente!"
        };
        for(int i=0;i<helpIconNames.length;i++){
            float centerY=520-i*84;
            draw.pic("help_"+helpIconNames[i],355,centerY-32,64,64);
            String[] lines=instructions[i].split("\\n");
            if(lines.length>1){
                draw.fittedLabel(lines[0],438,centerY-5,520,24,1.25f,Color.WHITE);
                draw.fittedLabel(lines[1],438,centerY-28,520,24,1.25f,Color.WHITE);
            }else{
                draw.text(lines[0],445,centerY-4,1.25f,Color.WHITE);
            }
        }
        draw.rect(510,52,260,58,Color.LIME);
        draw.rect(516,58,248,46,draw.rgba(.05f,.62f,.08f,1));
        draw.batch.begin();draw.centeredTextInRect("OK",516,58,248,46,1.4f,Color.WHITE);draw.batch.end();
    }
    private void click(float x,float y){switch(flow.page()){
        case PROFILE -> {
            if(draw.hit(x,y,215,76,190,50)){
                leaveFlow();
            }else if(draw.hit(x,y,875,76,190,50))flow.goTo(Page.GOALS);
            else if(draw.hit(x,y,500,429,59,42))flow.profile().adjustAge(-1);
            else if(draw.hit(x,y,1019,429,56,42))flow.profile().adjustAge(1);
            else if(draw.hit(x,y,500,375,59,42))flow.profile().adjustWeight(-1);
            else if(draw.hit(x,y,1019,375,56,42))flow.profile().adjustWeight(1);
            else if(draw.hit(x,y,500,483,575,42))Gdx.input.getTextInput(new Input.TextInputListener(){
                @Override public void input(String value){if(value!=null&&!value.trim().isEmpty())flow.profile().rename(value);}
                @Override public void canceled(){}
            },"Nome do paciente",flow.profile().name(),"");
            else if(draw.hit(x,y,500,321,575,42))flow.profile().nextRehabilitationGoal();
            else if(draw.hit(x,y,500,267,575,42))flow.profile().nextActivity();
            else for(int i=0;i<4;i++)if(draw.hit(x,y,368+i*142,132,120,96))flow.profile().selectCharacter(i);
        }
        case GOALS -> {if(draw.hit(x,y,320,100,165,48))flow.goTo(Page.PROFILE);else if(draw.hit(x,y,815,100,165,48))flow.goTo(Page.EXPERIENCE);else for(int i=0;i<4;i++)if(draw.hit(x,y,320,450-i*82,650,65))flow.profile().toggleGoal(i);}
        case EXPERIENCE -> {if(draw.hit(x,y,320,100,165,48))flow.goTo(Page.GOALS);else if(draw.hit(x,y,815,100,165,48))flow.goTo(Page.CONFIRM);else for(int i=0;i<3;i++)if(draw.hit(x,y,350,410-i*85,560,66))flow.profile().selectExperience(i);}
        case CONFIRM -> {if(draw.hit(x,y,320,100,165,48))flow.goTo(Page.EXPERIENCE);else if(draw.hit(x,y,755,100,225,48))flow.goTo(Page.LEVELS);}
        case LEVELS -> {
            if(draw.hit(x,y,82,58,210,66))flow.goTo(Page.CONFIRM);
            else if(draw.hit(x,y,910,58,300,58))flow.start();
            else for(int i=0;i<stages.length;i++)if(draw.hit(x,y,70+i*300,202,280,350))flow.selectLevel(i);
        }
        case PLAY -> {
            if(draw.hit(x,y,1110,20,145,45))flow.pauseRace();
            else if(draw.hit(x,y,490,18,160,46))flow.changeLane(-1);
            else if(draw.hit(x,y,665,18,160,46))flow.changeLane(1);
        }
        case PAUSE -> {if(draw.hit(x,y,480,435,320,62))flow.resumeRace();else if(draw.hit(x,y,480,345,320,62))flow.start();else if(draw.hit(x,y,480,255,320,62))leaveFlow();}
        case RESULTS -> {if(draw.hit(x,y,345,135,185,55))flow.start();else if(draw.hit(x,y,550,135,220,55)){flow.selectLevel((flow.level()+1)%4);flow.start();}else if(draw.hit(x,y,785,135,160,55))leaveFlow();}
        case HELP -> {if(draw.hit(x,y,510,52,260,58))leaveFlow();}
    }}
    @Override public void resize(int w,int h){viewport.update(w,h,true);}
    @Override public void pause() {if(flow.page()==Page.PLAY||flow.page()==Page.COUNTDOWN)flow.pauseRace();flow.pauseAudio();}
    @Override public void resume(){if(flow.page()!=Page.PAUSE)flow.resumeAudio();}
    @Override public void hide(){flow.hide();}
    @Override public void dispose(){draw.close();}

}
