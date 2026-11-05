package com.pedalgaiden;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Runnable reference implementation. UI navigation is mouse-driven; riding is cadence-driven. */
public final class PedalGaidenScreen implements Screen {
    private enum Page { MENU, PROFILE, GOALS, EXPERIENCE, CONFIRM, LEVELS, PLAY, PAUSE, RESULTS, HELP }
    private static final float W=1280, H=720;
    private final boolean iot;
    private final Game hostGame;
    private final OrthographicCamera cam=new OrthographicCamera();
    private final FitViewport viewport=new FitViewport(W,H,cam);
    private final SpriteBatch batch=new SpriteBatch();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final BitmapFont font=new BitmapFont();
    private final GlyphLayout textLayout=new GlyphLayout();
    private final Map<String,Texture> images=new HashMap<>();
    private final Random random=new Random(42);
    private final ArrayList<Item> items=new ArrayList<>();
    private Page page=Page.MENU, previous=Page.MENU;
    private int level=0, character=0, score=0, recycled=0, age=28, weight=70, duration=5, goal=0, experience=0;
    private String playerName="Player 1", rehabGoal="Knee Mobility", weeklyActivity="Low";
    private final String[] rehabGoals={"Knee Mobility", "Cardiovascular Endurance", "Lower Limb Strength", "Balance & Coordination"};
    private final String[] activityLevels={"Low", "Moderate", "High"};
    private final boolean[] goals={true,false,false,false};
    private float distance=0, elapsed=0, speed=0, cadence=0, lastCadence=0, rhythm=100, eco=0, spawn=0, feedbackTime=0;
    private String feedback="";
    private UdpSensor sensor;
    private final String[] stages={"FUJI DAY","SUNSET TEMPLE","LANTERN NIGHT","BAMBOO FOREST"};
    private final String[] bg={"fuji","sunset","night","bamboo"};
    private final String[] ninjas={"fire","ice","poison","oni"};
    private final String[] mainCharacters={"mainFire","mainBlue","mainGreen","mainWhite"};
    private final String[] waste={"plastic_bottle","can","paper","glass_bottle","tire"};
    private final String[] goalIconNames={"cardio","knee","strength","balance"};
    private final String[] helpIconNames={"bike","recycle","boost","bin","trophy"};
    private static final class Item { float x; int type; Item(float x,int type){this.x=x;this.type=type;} }
    public PedalGaidenScreen(boolean iot){this(null,iot,false);}
    public PedalGaidenScreen(Game hostGame,boolean iot,boolean startAtProfile){this.hostGame=hostGame;this.iot=iot;if(startAtProfile)page=Page.PROFILE;}
    public static PedalGaidenScreen instructions(Game hostGame,boolean iot){PedalGaidenScreen screen=new PedalGaidenScreen(hostGame,iot,false);screen.page=Page.HELP;return screen;}
    private void returnToMainMenu(){hostGame.setScreen(new MainMenuScreen(hostGame));dispose();}
    private void load(String key,String file){ Texture t=new Texture(Gdx.files.internal(file));t.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);images.put(key,t); }
    @Override public void show(){
        cam.position.set(W/2,H/2,0);viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        for(String name:bg)load("bg_"+name,"backgrounds/"+name+".png");
        for(String n:ninjas)for(int i=0;i<8;i++)load(n+i,"characters/"+n+"_"+i+".png");
        for(String name:mainCharacters)load(name,"characters/"+name+".png");
        for(String n:waste)load(n,"collectibles/"+n+".png");
        for(String name:goalIconNames)load("goal_"+name,"menu/pedalgaiden_goal_icons/ui_icon_goal_"+name+".png");
        for(String name:helpIconNames)load("help_"+name,"menu/pedalgaiden_howtoplay_icons/ui_icon_howto_"+name+".png");
        load("goal_checkbox_on","menu/pedalgaiden_goal_icons/ui_checkbox_on.png");
        load("goal_checkbox_off","menu/pedalgaiden_goal_icons/ui_checkbox_off.png");
        if(iot)try{sensor=new UdpSensor(5055);}catch(Exception ex){feedback="SENSOR PORT UNAVAILABLE";}
    }
    private void reset(){distance=elapsed=speed=cadence=spawn=eco=0;rhythm=100;score=recycled=0;items.clear();feedback="";feedbackTime=0;}
    private void start(){reset();page=Page.PLAY;}
    @Override public void render(float delta){
        float dt=Math.min(.05f,delta);
        if(page==Page.PLAY)updateGame(dt);
        Gdx.gl.glClearColor(.025f,.055f,.12f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();batch.setProjectionMatrix(cam.combined);shapes.setProjectionMatrix(cam.combined);
        switch(page){
            case MENU -> menu(); case PROFILE -> profile(); case GOALS -> goals();
            case EXPERIENCE -> experience(); case CONFIRM -> confirm(); case LEVELS -> levels();
            case PLAY -> gameplay(); case PAUSE -> {gameplay();overlay();pauseMenu();}
            case RESULTS -> results(); case HELP -> help();
        }
        if(Gdx.input.justTouched()){Vector3 pt=cam.unproject(new Vector3(Gdx.input.getX(),Gdx.input.getY(),0),viewport.getScreenX(),viewport.getScreenY(),viewport.getScreenWidth(),viewport.getScreenHeight());click(pt.x,pt.y);}
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){if(page==Page.PLAY)page=Page.PAUSE;else if(page==Page.PAUSE)page=Page.PLAY;else if((page==Page.PROFILE||page==Page.HELP)&&hostGame!=null)returnToMainMenu();else page=Page.MENU;}
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)&&page==Page.PLAY)page=Page.PAUSE;
    }
    private void updateGame(float dt){
        float rpm= iot ? (sensor==null?0:sensor.rpm()) : (Gdx.input.isKeyPressed(Input.Keys.SPACE)||Gdx.input.isKeyPressed(Input.Keys.UP)?(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)?90:65):0);
        cadence+=(rpm-cadence)*Math.min(1,dt*3);
        float targetSpeed=cadence*.11f;speed+=(targetSpeed-speed)*Math.min(1,dt*2.5f);
        rhythm+=(Math.max(0,100-Math.abs(cadence-lastCadence)*3)-rhythm)*Math.min(1,dt*1.5f);lastCadence=cadence;
        float movement=speed*dt*36;distance+=movement;elapsed+=dt;spawn+=dt;feedbackTime=Math.max(0,feedbackTime-dt);
        if(spawn>1.4f){spawn=0;items.add(new Item(W+50,random.nextInt(waste.length)));}
        Iterator<Item> it=items.iterator();while(it.hasNext()){
            Item item=it.next();item.x-=movement;
            if(item.x>175&&item.x<255){it.remove();score+=25;recycled++;eco=Math.min(100,eco+12);feedback="+25 RECYCLED";feedbackTime=1.2f;}
            else if(item.x< -100)it.remove();
        }
        if(eco>=100){score+=100;eco=0;feedback="ECO BURST +100";feedbackTime=1.8f;}
        if(distance>=1200+level*350 || elapsed>=duration*60){page=Page.RESULTS;}
    }
    private void rect(float x,float y,float w,float h,Color color){shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(color);shapes.rect(x,y,w,h);shapes.end();}
    private Color rgba(float r,float g,float b,float a){return new Color(r,g,b,a);}
    private void text(String s,float x,float y,float size,Color c){batch.begin();font.setColor(c);font.getData().setScale(size);font.draw(batch,s,x,y);font.getData().setScale(1);batch.end();}
    private void centeredText(String s,float centerX,float y,float size,Color c){font.getData().setScale(size);textLayout.setText(font,s);font.setColor(c);font.draw(batch,s,centerX-textLayout.width/2,y);font.getData().setScale(1);}
    private void centeredTextInRect(String s,float x,float y,float w,float h,float size,Color c){
        font.getData().setScale(size);textLayout.setText(font,s);font.setColor(c);
        font.draw(batch,s,x+(w-textLayout.width)/2,y+(h+textLayout.height)/2);
        font.getData().setScale(1);
    }
    private void pic(String name,float x,float y,float w,float h){batch.begin();batch.setColor(Color.WHITE);batch.draw(images.get(name),x,y,w,h);batch.end();}
    private void background(){pic("bg_"+bg[level],0,0,W,H);}
    private void panel(float x,float y,float w,float h){rect(x,y,w,h,rgba(.018f,.055f,.13f,.94f));rect(x,y+h-4,w,4,rgba(.19f,.63f,.96f,1));}
    private void button(String s,float x,float y,float w,float h,boolean primary){rect(x,y,w,h,primary?rgba(.09f,.54f,.15f,1):rgba(.10f,.20f,.34f,1));rect(x,y+h-4,w,4,primary?Color.LIME:rgba(.30f,.62f,.9f,1));text(s,x+18,y+h/2+6,1.7f,Color.WHITE);}
    private boolean hit(float mx,float my,float x,float y,float w,float h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}
    private void title(String s){text(s,470,640,3,Color.WHITE);}
    private void menu(){background();rect(0,0,W,H,rgba(.01f,.04f,.12f,.16f));pic("fire0",70,125,330,280);panel(590,115,590,510);text("PEDAL GAIDEN",630,545,4,Color.LIME);text("RIDE. RECYCLE. RESTORE THE WORLD.",650,493,1.6f,Color.WHITE);
        button("PLAY",715,385,350,62,true);button("PLAYER PROFILE",715,305,350,62,false);button("HOW TO PLAY",715,225,350,62,false);button("SELECT LEVEL",715,145,350,62,false);
        text(iot?"INPUT: IoT SENSOR (UDP 5055)":"DEMO INPUT: HOLD SPACE TO PEDAL",55,46,1.5f,Color.WHITE);
    }
    private void surveyBase(String heading,int step){background();rect(0,0,W,H,rgba(.01f,.03f,.09f,.70f));panel(245,85,790,555);text(heading,470,630,3,Color.WHITE);for(int i=0;i<4;i++){rect(375+i*150,565,120,6,i<=step?Color.LIME:Color.GRAY);text(""+(i+1),421+i*150,589,1.6f,Color.WHITE);}}
    private void profile(){
        background();
        rect(0,0,W,H,rgba(.01f,.03f,.09f,.70f));
        panel(150,55,980,610);
        text("PATIENT PROFILE",640,615,2.8f,Color.WHITE);
        for(int i=0;i<4;i++){
            float x=352+i*145;
            rect(x,568,125,6,i==0?Color.LIME:Color.GRAY);
            text(""+(i+1),x+55,590,1.6f,Color.WHITE);
        }
        profileField("Name",playerName,485,false);
        profileStepper("Age",age+"",431);
        profileStepper("Weight (kg)",weight+"",377);
        profileField("Rehabilitation goal",rehabGoal,323,true);
        profileField("Weekly activity level",weeklyActivity,269,true);
        text("PREFERRED GAME CHARACTER",500,246,1.4f,Color.WHITE);
        for(int i=0;i<4;i++){
            float x=368+i*142;
            rect(x,132,120,96,i==character?Color.LIME:rgba(.20f,.42f,.66f,1));
            rect(x+3,135,114,90,rgba(.025f,.09f,.19f,1));
            pic(mainCharacters[i],x+19,139,82,82);
        }
        button("BACK",215,76,190,50,false);
        button("NEXT",875,76,190,50,true);
    }
    private void profileField(String label,String value,float y,boolean dropdown){
        text(label,220,y+13,1.45f,Color.WHITE);
        rect(500,y-2,575,42,rgba(.24f,.39f,.56f,1));
        rect(503,y+1,569,36,rgba(.025f,.075f,.15f,1));
        text(value,518,y+13,1.35f,Color.WHITE);
        if(dropdown)text("v",1046,y+13,1.4f,Color.WHITE);
    }
    private void profileStepper(String label,String value,float y){
        text(label,220,y+13,1.45f,Color.WHITE);
        rect(500,y-2,575,42,rgba(.24f,.39f,.56f,1));
        rect(503,y+1,56,36,rgba(.12f,.25f,.40f,1));
        rect(561,y+1,456,36,rgba(.025f,.075f,.15f,1));
        rect(1019,y+1,53,36,rgba(.12f,.25f,.40f,1));
        text("<",525,y+13,1.5f,Color.WHITE);
        text(value,780,y+13,1.35f,Color.WHITE);
        text(">",1040,y+13,1.5f,Color.WHITE);
    }
    private void goals(){
        surveyBase("YOUR GOALS",1);
        String[] names={"IMPROVE CARDIOVASCULAR ENDURANCE","INCREASE KNEE MOBILITY","BUILD LOWER LIMB STRENGTH","IMPROVE BALANCE AND COORDINATION"};
        for(int i=0;i<4;i++){
            float y=450-i*82;
            rect(320,y,650,65,goals[i]?rgba(.06f,.30f,.20f,1):rgba(.08f,.15f,.28f,1));
            pic(goals[i]?"goal_checkbox_on":"goal_checkbox_off",334,y+14,36,36);
            pic("goal_"+goalIconNames[i],382,y+10,44,44);
            text(names[i],444,y+39,1.45f,Color.WHITE);
        }
        button("BACK",320,100,165,48,false);button("NEXT",815,100,165,48,true);
    }
    private void experience(){surveyBase("EXPERIENCE",2);text("CHOOSE YOUR COMFORTABLE EXPERIENCE LEVEL",330,515,1.8f,Color.WHITE);String[] opts={"BEGINNER", "INTERMEDIATE", "EXPERIENCED"};for(int i=0;i<3;i++)button((experience==i?"> ":"  ")+opts[i],350,410-i*85,560,66,experience==i);text("Difficulty changes collectible pacing, not medical targets.",330,174,1.3f,Color.LIGHT_GRAY);button("BACK",320,100,165,48,false);button("NEXT",815,100,165,48,true);}
    private void confirm(){surveyBase("CONFIRM SESSION",3);text("PLAYER: "+playerName,330,505,2,Color.WHITE);text("AGE: "+age+"   SESSION: "+duration+" MIN",330,445,2,Color.WHITE);text("EXPERIENCE: "+new String[]{"BEGINNER","INTERMEDIATE","EXPERIENCED"}[experience],330,385,2,Color.WHITE);text("NINJA: "+ninjas[character].toUpperCase(Locale.ROOT),330,325,2,Color.WHITE);text("Gameplay uses cadence; this is not a medical assessment.",330,245,1.35f,Color.LIGHT_GRAY);button("BACK",320,100,165,48,false);button("CHOOSE LEVEL",755,100,225,48,true);}
    private void levels(){background();rect(0,0,W,H,rgba(.01f,.03f,.09f,.70f));panel(110,100,1060,540);title("SELECT LEVEL");for(int i=0;i<4;i++){float x=155+i*255;rect(x,250,220,260,rgba(.04f,.12f,.27f,.95f));pic("bg_"+bg[i],x+8,345,204,155);text((i+1)+". "+stages[i],x+12,315,1.4f,Color.WHITE);button(level==i?"SELECTED":"SELECT",x+10,265,200,42,level==i);}button("BACK",155,140,190,48,false);button("START RIDE",870,140,230,48,true);}
    private void gameplay(){background();rect(0,0,W,83,rgba(.015f,.045f,.09f,.72f));for(int i=0;i<16;i++)rect(i*84,75,80,14,rgba(.17f,.22f,.28f,1));
        for(Item item:items)pic(waste[item.type],item.x,94,54,64);
        pic(ninjas[character]+((int)(elapsed*(cadence<20?3:8))%8),115,82,210,188);
        panel(15,594,450,110);panel(478,594,350,110);panel(841,594,423,110);
        text("LEVEL "+(level+1)+" - "+stages[level],35,671,2,Color.WHITE);
        text("SCORE "+score+"     RECYCLED "+recycled,35,625,1.65f,Color.WHITE);
        text(String.format(Locale.US,"CADENCE %.0f RPM",cadence),495,670,1.7f,Color.WHITE);
        text(String.format(Locale.US,"RHYTHM %.0f%%",rhythm),495,628,1.7f,Color.LIME);
        text("ECO ENERGY",862,670,1.9f,Color.WHITE);rect(864,622,340,20,Color.DARK_GRAY);rect(864,622,340*eco/100,20,Color.LIME);
        text(String.format(Locale.US,"DISTANCE %.0f     TIME %02d:%02d",distance,(int)elapsed/60,(int)elapsed%60),24,28,1.5f,Color.WHITE);
        button("PAUSE",1110,20,145,45,false);
        if(feedbackTime>0)text(feedback,520,390,2.5f,Color.YELLOW);
        if(!iot)text("HOLD SPACE TO PEDAL / SHIFT TO BOOST",405,108,1.4f,Color.WHITE);
        else text(sensor!=null&&sensor.connected()?"SENSOR CONNECTED":"WAITING FOR SENSOR",450,108,1.4f,Color.WHITE);
    }
    private void overlay(){rect(0,0,W,H,rgba(0,0,.05f,.70f));}
    private void pauseMenu(){panel(430,170,420,440);text("PAUSED",545,550,3,Color.WHITE);button("RESUME",480,435,320,62,true);button("RESTART",480,345,320,62,false);button("MAIN MENU",480,255,320,62,false);}
    private void results(){background();overlay();panel(310,95,660,545);text("LEVEL COMPLETE!",450,560,3,Color.YELLOW);pic(ninjas[character]+"0",340,250,240,230);text("SCORE: "+score,610,455,2,Color.WHITE);text("RECYCLED: "+recycled,610,410,2,Color.WHITE);text(String.format(Locale.US,"DISTANCE: %.0f",distance),610,365,2,Color.WHITE);text(String.format(Locale.US,"RHYTHM: %.0f%%",rhythm),610,320,2,Color.WHITE);button("RETRY",345,135,185,55,false);button("NEXT LEVEL",550,135,220,55,true);button("MENU",785,135,160,55,false);}
    private void help(){
        background();panel(300,32,680,656);
        batch.begin();centeredText("HOW TO PLAY",640,625,2.0f,Color.WHITE);batch.end();
        rect(350,584,580,3,rgba(.19f,.63f,.96f,.85f));
        String[] instructions={
                "Pedal to move forward\n(the game uses your cadence)",
                "Collect recyclables to gain points\nand fill your Eco Energy",
                "Maintain a steady rhythm\nto activate Eco Boost",
                "Drop recyclables in matching bins",
                "Complete all levels to restore\nthe environment!"
        };
        for(int i=0;i<helpIconNames.length;i++){
            float centerY=520-i*84;
            pic("help_"+helpIconNames[i],355,centerY-32,64,64);
            String[] lines=instructions[i].split("\\n");
            if(lines.length>1){
                text(lines[0],445,centerY+7,1.25f,Color.WHITE);
                text(lines[1],445,centerY-13,1.25f,Color.WHITE);
            }else{
                text(lines[0],445,centerY-4,1.25f,Color.WHITE);
            }
        }
        rect(510,52,260,58,Color.LIME);
        rect(516,58,248,46,rgba(.05f,.62f,.08f,1));
        batch.begin();centeredTextInRect("OK",516,58,248,46,1.4f,Color.WHITE);batch.end();
    }
    private void click(float x,float y){switch(page){
        case MENU -> {if(hit(x,y,715,385,350,62))page=Page.LEVELS;else if(hit(x,y,715,305,350,62))page=Page.PROFILE;else if(hit(x,y,715,225,350,62))page=Page.HELP;else if(hit(x,y,715,145,350,62))page=Page.LEVELS;}
        case PROFILE -> {
            if(hit(x,y,215,76,190,50)){
                if(hostGame!=null)returnToMainMenu();else page=Page.MENU;
            }else if(hit(x,y,875,76,190,50))page=Page.GOALS;
            else if(hit(x,y,500,429,59,42))age=Math.max(12,age-1);
            else if(hit(x,y,1019,429,56,42))age=Math.min(100,age+1);
            else if(hit(x,y,500,375,59,42))weight=Math.max(25,weight-1);
            else if(hit(x,y,1019,375,56,42))weight=Math.min(250,weight+1);
            else if(hit(x,y,500,483,575,42))Gdx.input.getTextInput(new Input.TextInputListener(){
                @Override public void input(String value){if(value!=null&&!value.trim().isEmpty())playerName=value.trim();}
                @Override public void canceled(){}
            },"Patient name",playerName,"");
            else if(hit(x,y,500,321,575,42))rehabGoal=rehabGoals[(Arrays.asList(rehabGoals).indexOf(rehabGoal)+1)%rehabGoals.length];
            else if(hit(x,y,500,267,575,42))weeklyActivity=activityLevels[(Arrays.asList(activityLevels).indexOf(weeklyActivity)+1)%activityLevels.length];
            else for(int i=0;i<4;i++)if(hit(x,y,368+i*142,132,120,96))character=i;
        }
        case GOALS -> {if(hit(x,y,320,100,165,48))page=Page.PROFILE;else if(hit(x,y,815,100,165,48))page=Page.EXPERIENCE;else for(int i=0;i<4;i++)if(hit(x,y,320,450-i*82,650,65))goals[i]=!goals[i];}
        case EXPERIENCE -> {if(hit(x,y,320,100,165,48))page=Page.GOALS;else if(hit(x,y,815,100,165,48))page=Page.CONFIRM;else for(int i=0;i<3;i++)if(hit(x,y,350,410-i*85,560,66))experience=i;}
        case CONFIRM -> {if(hit(x,y,320,100,165,48))page=Page.EXPERIENCE;else if(hit(x,y,755,100,225,48))page=Page.LEVELS;}
        case LEVELS -> {if(hit(x,y,155,140,190,48))page=Page.MENU;else if(hit(x,y,870,140,230,48))start();else for(int i=0;i<4;i++)if(hit(x,y,165+i*255,265,200,42))level=i;}
        case PLAY -> {if(hit(x,y,1110,20,145,45))page=Page.PAUSE;}
        case PAUSE -> {if(hit(x,y,480,435,320,62))page=Page.PLAY;else if(hit(x,y,480,345,320,62))start();else if(hit(x,y,480,255,320,62))page=Page.MENU;}
        case RESULTS -> {if(hit(x,y,345,135,185,55))start();else if(hit(x,y,550,135,220,55)){level=(level+1)%4;start();}else if(hit(x,y,785,135,160,55))page=Page.MENU;}
        case HELP -> {if(hit(x,y,510,52,260,58)){if(hostGame!=null)returnToMainMenu();else page=Page.MENU;}}
    }}
    @Override public void resize(int w,int h){viewport.update(w,h,true);}
    @Override public void pause() {if(page==Page.PLAY)page=Page.PAUSE;}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){if(sensor!=null)sensor.close();for(Texture t:images.values())t.dispose();batch.dispose();shapes.dispose();font.dispose();}
    private static final class UdpSensor {
        private final DatagramSocket socket;
        private volatile float rpm=0;private volatile long last=0;
        UdpSensor(int port)throws Exception{socket=new DatagramSocket(new InetSocketAddress("127.0.0.1",port));Thread t=new Thread(()->{byte[] data=new byte[128];while(!socket.isClosed())try{DatagramPacket p=new DatagramPacket(data,data.length);socket.receive(p);String s=new String(p.getData(),p.getOffset(),p.getLength(),StandardCharsets.US_ASCII).trim();if(s.startsWith("RPM:")){float v=Float.parseFloat(s.substring(4));if(Float.isFinite(v)&&v>=0&&v<=160){rpm=v;last=System.nanoTime();}}}catch(Exception ignored){}},"pedal-sensor");t.setDaemon(true);t.start();}
        float rpm(){return connected()?rpm:0;}
        boolean connected(){return System.nanoTime()-last<2_000_000_000L;}
        void close(){socket.close();}
    }
}
