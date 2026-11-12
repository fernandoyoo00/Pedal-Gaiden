package com.pedalgaiden;

import com.pedalgaiden.domain.RaceRules;
import com.pedalgaiden.domain.RaceState;
import com.pedalgaiden.presentation.viewmodel.FlowViewModel;
import com.pedalgaiden.presentation.view.ViewLayout;
import com.pedalgaiden.infrastructure.audio.GameAudio;
import com.pedalgaiden.application.usecase.RideSession;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.nio.charset.StandardCharsets;

public final class GameplayRegressionTest {
    public static void main(String[] args) throws IOException {
        for(String name:new String[]{"mainsoundtrack","menuSound","clickMenu","collectCollectible","binCollected"})
            check(new File("audio/"+name+".mp3").length()>0,"Every requested sound must be included in assets");
        for(String name:new String[]{"countdown_3_red","countdown_2_amber","countdown_1_green","countdown_start_green"})
            check(ImageIO.read(new File("gameplay/countdown/"+name+".png"))!=null,"Countdown artwork must load");
        check(ImageIO.read(new File("menu/RetroPixelGameUIPanel.png"))!=null,"The profile panel must load");
        check(FlowViewModel.countdownNumber(0)==3,"Countdown must begin at three");
        check(FlowViewModel.countdownNumber(.99f)==3,"Each countdown number must last a full second");
        check(FlowViewModel.countdownNumber(1)==2,"Countdown must advance to two");
        check(FlowViewModel.countdownNumber(2)==1,"Countdown must advance to one");
        check(FlowViewModel.countdownNumber(3)==0,"Countdown must finish with the START artwork");
        check(GameAudio.RACE_VOLUME==.8f,"Race music must use 80 percent volume");
        for(String name:new String[]{"countdown_3_red","countdown_2_amber","countdown_1_green"}){
            BufferedImage image=ImageIO.read(new File("gameplay/countdown/"+name+".png"));
            check((image.getRGB(0,0)>>>24)==0,"Countdown artwork must have a transparent background");
        }
        boolean[] playing={false};
        java.util.ArrayList<String> musicCalls=new java.util.ArrayList<>();
        com.badlogic.gdx.audio.Music music=(com.badlogic.gdx.audio.Music)java.lang.reflect.Proxy.newProxyInstance(
                com.badlogic.gdx.audio.Music.class.getClassLoader(),
                new Class<?>[]{com.badlogic.gdx.audio.Music.class},(proxy,method,arguments)->{
                    if(method.getName().equals("isPlaying"))return playing[0];
                    musicCalls.add(method.getName());
                    playing[0]=method.getName().equals("play");
                    return null;
                });
        java.util.ArrayList<Float> soundVolumes=new java.util.ArrayList<>();
        com.badlogic.gdx.audio.Sound sound=(com.badlogic.gdx.audio.Sound)java.lang.reflect.Proxy.newProxyInstance(
                com.badlogic.gdx.audio.Sound.class.getClassLoader(),
                new Class<?>[]{com.badlogic.gdx.audio.Sound.class},(proxy,method,arguments)->{
                    if(method.getName().equals("play")){soundVolumes.add((Float)arguments[0]);return 1L;}
                    return null;
                });
        GameAudio audio=new GameAudio(music,music,sound,sound,sound);
        audio.menu();audio.race(false);audio.pause();audio.race(false);
        check(playing[0],"Race music must resume after pause");
        audio.resetRace();
        check(!playing[0],"Restarting must stop the previous race tracks");
        audio.click();audio.pickup();audio.recycled();
        check(soundVolumes.equals(java.util.Arrays.asList(.6f,.7f,.8f)),
                "Clicks, pickups and correct recycling must use their configured relative volumes");
        boolean[] menuPlaying={false},racePlaying={false};
        java.util.ArrayList<String> raceCalls=new java.util.ArrayList<>();
        GameAudio countdownAudio=new GameAudio(
                fakeMusic(menuPlaying,new java.util.ArrayList<>()),
                fakeMusic(racePlaying,raceCalls),sound,sound,sound);
        countdownAudio.menu();
        countdownAudio.countdown();
        countdownAudio.countdown();
        check(!menuPlaying[0]&&racePlaying[0],
                "Countdown must switch to race music");
        countdownAudio.race(false);
        check(racePlaying[0]&&raceCalls.stream().filter("play"::equals).count()==1,
                "The race must continue without restarting countdown music");
        countdownAudio.pause();countdownAudio.countdown();
        check(racePlaying[0],"Resuming a countdown must resume race music");
        try(var stream=GameplayRegressionTest.class.getResourceAsStream("/com/badlogic/gdx/utils/lsans-15.fnt")){
            check(stream!=null,"The default UI font must be available");
            String fontData=new String(stream.readAllBytes(),StandardCharsets.UTF_8);
            for(char letter:"\u00c0\u00c1\u00c2\u00c3\u00c7\u00c9\u00ca\u00cd\u00d3\u00d4\u00d5\u00da\u00e0\u00e1\u00e2\u00e3\u00e7\u00e9\u00ea\u00ed\u00f3\u00f4\u00f5\u00fa".toCharArray()){
                check(fontData.contains("char id="+(int)letter+" "),
                        "The UI font must support Portuguese accented letters");
            }
        }
        check(!RaceRules.stageFinished(6,5),"A stage must not finish after six seconds");
        check(!RaceRules.stageFinished(299.99f,5),"The full five minutes must elapse");
        check(RaceRules.stageFinished(300,5),"A five-minute stage must finish at 300 seconds");
        check(!RaceRules.stageFinished(300,10),"Completion must use the configured duration");
        check(RaceRules.stageFinished(600,10),"A ten-minute stage must finish at 600 seconds");
        check(RaceRules.cadenceRhythmTarget(0,60,60)==0,"Rhythm must be zero without pedalling");
        check(RaceRules.cadenceRhythmTarget(60,60,60)==100,"Steady cadence must produce full rhythm");
        check(RaceRules.cadenceRhythmTarget(60,70,60)==70,"Cadence changes must reduce rhythm");
        RideSession ride=new RideSession(new java.util.Random(42));
        ride.start(0,0,5);
        ride.update(.05f,0);
        check(ride.state().rhythm==0,"Stopping must immediately clear rhythm");
        for(int frame=0;frame<200;frame++)ride.update(.05f,60);
        check(ride.state().rhythm>99,"Sustained steady cadence must restore rhythm");
        ride.update(.05f,0);
        check(ride.state().rhythm==0,"Rhythm must clear as soon as cadence stops");
        RaceState.EcoTurbo turbo=new RaceState.EcoTurbo();
        for(int deposit=0;deposit<8;deposit++)check(!turbo.recycle(),"Turbo must wait until the energy meter is full");
        check(turbo.energy==96&&turbo.remaining==0,"Eight deposits must charge 96 energy without turbo");
        check(turbo.recycle()&&turbo.energy==0&&turbo.remaining==5,"The ninth deposit must automatically activate five-second turbo");
        check(turbo.multiplier(true)==1.5f,"Turbo must multiply riding speed by exactly 1.5");
        check(turbo.multiplier(false)==1,"Turbo must not propel a rider who is not pedalling");
        check(turbo.frame()==0,"Turbo animation must start at frame zero");
        turbo.update(.1f);
        check(turbo.frame()==1,"Turbo animation must advance at ten frames per second");
        turbo.update(.3f);
        check(turbo.frame()==0,"Turbo animation must loop through four frames");
        turbo.reset();
        for(int deposit=0;deposit<9;deposit++)turbo.recycle();
        turbo.update(0);
        check(turbo.remaining==5,"Pausing must freeze turbo");
        turbo.update(4.99f);
        check(turbo.remaining>0&&turbo.multiplier(true)==1.5f,"Turbo must remain active until five seconds elapse");
        turbo.update(.02f);
        check(turbo.remaining==0&&turbo.multiplier(true)==1,"Turbo must expire without negative time");
        turbo.recycle();turbo.reset();
        check(turbo.energy==0&&turbo.remaining==0&&turbo.frame()==0,"Restarting must clear turbo, animation and energy");
        check(com.pedalgaiden.presentation.view.ViewLayout.HUD_WIDTH==1000,"The compact HUD must be 20 percent narrower than the previous frame");
        for(String asset:new String[]{"gameplay/JapaneseRecyclingPavilion.png",
                "gameplay/Pixel-Art Fuji Blossom Status Bar.png",
                "gameplay/powerEffects/03_recycling_pickup_green.png"}){
            check(ImageIO.read(new File(asset))!=null,"The pavilion, HUD, and recycling effect must load");
        }
        for(String ninja:new String[]{"fire","ice","poison","oni"}){
            int width=0,height=0;
            for(int frame=0;frame<4;frame++){
                BufferedImage image=ImageIO.read(new File("characters/boostAnimation/"+ninja+"/"+ninja+"_boost_0"+frame+".png"));
                check(image!=null,"Every ninja must have all four boost animation frames");
                if(frame==0){width=image.getWidth();height=image.getHeight();}
                check(image.getWidth()==width&&image.getHeight()==height,
                        "Boost frames must share a stable canvas to avoid animation jitter");
            }
        }
        for(int player=0;player<4;player++){
            List<RaceState.NinjaBot> bots=RaceState.createNinjaBots(player);
            check(bots.size()==3,"Every ride must start with exactly three friendly bots");
            boolean[] seen=new boolean[4];
            seen[player]=true;
            for(RaceState.NinjaBot bot:bots){
                check(!seen[bot.character],"Bots must use the three ninjas not selected by the player");
                seen[bot.character]=true;
                check(bot.y>=318&&bot.y<=330,"Scenery bots must stay along the far road edge");
                check(bot.height>=40&&bot.height<=48,"Background bots must be at most half the player's height");
                check(bot.x==bot.anchorX&&bot.pedalCycle==0,"New bots must have fresh positions and animations");
                bot.update(1,0);
                check(bot.x>bot.anchorX&&bot.pedalCycle>0,"Scenery bots must ride right independently while the player is stopped");
                check(bot.x-bot.anchorX<=49,"Background cyclists must use the reduced 25-49 pixel per second speeds");
                float previousX=bot.x;
                bot.update(.25f,65);
                check(bot.x>previousX,"Scenery bots must continue toward the right exit while the player rides");
                float pausedX=bot.x, pausedCycle=bot.pedalCycle;
                bot.update(0,0);
                check(bot.x==pausedX&&bot.pedalCycle==pausedCycle,"Frozen time must not advance bots");
                boolean exited=false, reentered=false;
                for(int step=0;step<6000;step++){
                    float oldX=bot.x;
                    bot.update(.05f,12);
                    if(bot.x>1280)exited=true;
                    if(bot.x<oldX){
                        reentered=true;
                        check(oldX>1280&&bot.x<0,"Bots must only reappear on the left after leaving the right edge");
                    }
                    check(bot.pedalCycle>=0&&bot.pedalCycle<1,"Bot animations must stay within the six-frame cycle");
                }
                check(exited&&reentered,"Scenery bots must exit and return during a ride");
                bot.reentryDelay=0;
                bot.x=1360;
                bot.update(.05f,0);
                check(bot.reentryDelay>=8,"Cyclists must wait at least eight seconds before returning");
                float waitingX=bot.x;
                bot.update(1,0);
                check(bot.x==waitingX,"Cyclists must stay off-screen during their reappearance delay");
            }
            check(RaceRules.ROADSIDE_Y==318,"Bins must sit along the far road edge");
            RaceState.PathBin roadsideBin=new RaceState.PathBin(300,3);
            check(!roadsideBin.update(.05f,10),"A distant roadside bin must not trigger");
            check(roadsideBin.update(.05f,60),"Passing a roadside bin must trigger from any lane");
            check(!roadsideBin.update(.05f,10),"A roadside bin must trigger only once");
            for(int stage=0;stage<4;stage++){
                float interval=RaceRules.trafficInterval(stage);
                check(!RaceRules.trafficWarning(0,stage),"A new traffic cycle must clear its advance warning");
                check(!RaceRules.trafficWarning(interval-1.6f,stage),"Warnings must not appear too early");
                check(RaceRules.trafficWarning(interval-1.5f,stage),"Traffic must be announced 1.5 seconds before spawning");
            }
            List<RaceState.NinjaBot> restarted=RaceState.createNinjaBots(player);
            for(RaceState.NinjaBot bot:restarted){
                check(bot.x==bot.anchorX&&bot.pedalCycle==0&&bot.rideTime==0&&bot.reentryDelay==0,
                        "Restarting must reset every bot without retaining prior progress");
            }
        }

        check(!RaceRules.crossedPickup(400,300),"Distant items must not be collected");
        check(RaceRules.crossedPickup(260,240),"Items entering the rider area must be collected");
        check(RaceRules.crossedPickup(300,150),"Fast items must not skip the pickup area");
        check(!RaceRules.crossedPickup(170,160),"Items behind the rider must not be collected");
        check(!RaceRules.shouldSpawnCollectible(0),"Standing still must not spawn collectibles");
        check(!RaceRules.shouldSpawnCollectible(1599),"Collectibles must respect wider spacing");
        check(RaceRules.shouldSpawnCollectible(1600),"A collectible must spawn at the spacing threshold");
        check(RaceRules.BIN_FOLLOW_DISTANCE>80,
                "The matching bin must follow behind the collectible without overlapping it");
        check(RaceRules.trafficInterval(0)==8,"Introductory traffic must spawn eight seconds apart");
        check(RaceRules.trafficInterval(3)==5,"Later traffic must become more frequent");
        float warningHeight=com.pedalgaiden.presentation.view.ViewLayout.TRAFFIC_WARNING_HEIGHT;
        check(com.pedalgaiden.presentation.view.ViewLayout.TRAFFIC_WARNING_WIDTH==144&&warningHeight<=60,
                "The translated traffic warning must fit within one road lane");
        for(String collectible:new String[]{"plastic_bottle","can","paper","glass_bottle","tire"}){
            BufferedImage image=ImageIO.read(new File("collectibles/"+collectible+".png"));
            check(image!=null,"Updated collectible PNGs must load directly");
            float scale=ViewLayout.spriteScale(image.getWidth(),image.getHeight(),48,48);
            check(scale<=1&&scale>0,"Collectibles must not be enlarged beyond their native resolution");
            check(image.getWidth()*scale<=48.001f&&image.getHeight()*scale<=48.001f,
                    "Collectibles must fit proportionally inside 48 by 48 pixels");
        }
        check(ViewLayout.spriteScale(100,200,48,48)==.24f,"Tall sprites must retain their proportions");
        check(ViewLayout.spriteScale(200,100,48,48)==.24f,"Wide sprites must retain their proportions");
        check(ViewLayout.spriteScale(16,16,48,48)==1,"Small sprites must not be upscaled");
        for(int variant=0;variant<4;variant++){
            check(RaceRules.trafficVelocity(0,variant)>0,"Near-side traffic must travel right");
            check(RaceRules.trafficVelocity(3,variant)<0,"Far-side traffic must travel left");
        }
        check(RaceRules.trafficVelocity(0,0)!=RaceRules.trafficVelocity(0,3),
                "Traffic must have varied speeds");
        check(RaceRules.trafficCollision(250,240,RaceRules.laneY(0),0),
                "Overlapping traffic in the same lane must collide");
        check(!RaceRules.trafficCollision(250,240,RaceRules.laneY(1),0),
                "Adjacent-lane traffic must not collide");
        check(!RaceRules.trafficCollision(500,490,RaceRules.laneY(0),0),
                "Distant traffic must not collide");
        check(RaceRules.trafficCollision(400,0,RaceRules.laneY(0),0),
                "Fast oncoming traffic must not tunnel through the rider");
        check(RaceRules.trafficCollision(0,400,RaceRules.laneY(0),0),
                "Traffic moving right must also detect swept collisions");
        for(String car:new String[]{"red_sedan","blue_sedan","silver_suv","yellow_taxi"}){
            for(int frame=1;frame<=4;frame++){
                check(ImageIO.read(new File("obstacles/cars/"+car+"/"+car+"_"+frame+".png"))!=null,
                        "All sixteen traffic animation assets must load");
            }
        }
        check(RaceRules.cheerFrame(RaceRules.CHEER_DURATION)==0,
                "A new cheer must start at the first frame");
        check(RaceRules.cheerFrame(RaceRules.CHEER_DURATION/2)==4,
                "The cheer must advance through the animation");
        check(RaceRules.cheerFrame(.01f)==7,
                "The cheer must reach the final frame before ending");
        check(RaceRules.cheerFrame(0)==7,
                "The cheer frame must never exceed the available artwork");

        int[] expectedBins={3,1,0,2,4};
        Deque<Integer> queue=new ArrayDeque<>();
        queue.addLast(0);
        check(RaceRules.depositNext(queue,0)==RaceRules.DepositResult.WRONG_BIN,
                "A bin with no matching carried item must leave inventory unchanged");
        check(queue.size()==1&&queue.peekFirst()==0,"An unmatched bin must preserve inventory");
        queue.clear();
        for(int type=0;type<expectedBins.length;type++){
            check(RaceRules.matchingBin(type)==expectedBins[type],"Every waste type needs its matching bin");
            queue.addLast(type);
        }
        for(int bin:expectedBins){
            check(RaceRules.depositNext(queue,bin)==RaceRules.DepositResult.RECYCLED,
                    "Passing matching bins must recycle in pickup order");
        }
        check(queue.isEmpty(),"Successful deposits must remove each item exactly once");
        queue.addLast(0);
        queue.addLast(2);
        check(RaceRules.depositNext(queue,0)==RaceRules.DepositResult.RECYCLED,
                "A matching path bin must recycle even if an earlier item's bin was missed");
        check(queue.size()==1&&queue.peekFirst()==0,"Only the matching item must be removed");
        queue.clear();
        check(RaceRules.activeLanes(0)==4,"Stage one must enable all four lanes");
        for(int stage=1;stage<4;stage++)check(RaceRules.activeLanes(stage)==4,"Later stages must use four lanes");
        for(int lane=0;lane<4;lane++){
            check(RaceRules.canReachLane(RaceRules.laneY(lane),lane),"An aligned rider must reach the lane");
            check(!RaceRules.canReachLane(RaceRules.laneY(lane)+30,lane),"Changing lanes must not collect from both lanes");
        }
        check(RaceRules.depositNext(queue,0)==RaceRules.DepositResult.EMPTY,
                "Empty queues must not award another deposit");
        int spawned=0;
        float travelled=0;
        for(int second=0;second<300;second++){
            travelled+=65*.11f*36;
            if(RaceRules.shouldSpawnCollectible(travelled)){
                travelled-=RaceRules.COLLECTIBLE_SPACING;
                spawned++;
            }
        }
        check(spawned>=45&&spawned<=50,"Normal-speed five-minute rides must spawn roughly 48 items, not 214");
        for(int type=0;type<expectedBins.length;type++){
            float itemX=1330;
            float binX=itemX+RaceRules.BIN_FOLLOW_DISTANCE;
            boolean collected=false, deposited=false;
            for(int step=0;step<200;step++){
                float oldItemX=itemX, oldBinX=binX;
                itemX-=10;
                binX-=10;
                if(!collected&&RaceRules.crossedPickup(oldItemX,itemX)){
                    queue.addLast(type);
                    collected=true;
                }
                if(!deposited&&RaceRules.crossedPickup(oldBinX,binX)){
                    check(collected,"The item must reach the rider before its bin");
                    check(RaceRules.depositNext(queue,expectedBins[type])
                                    ==RaceRules.DepositResult.RECYCLED,
                            "A path bin must automatically recycle its preceding item");
                    deposited=true;
                }
            }
            check(collected&&deposited&&queue.isEmpty(),"Each path pair must collect and deposit exactly once");
        }
        for(String color:new String[]{"blue","yellow","green","red","gray"}){
            for(String state:new String[]{"closed","open"}){
                check(ImageIO.read(new File("gameplay/bins/"+color+"_"+state+".png"))!=null,
                        "Every recycling bin asset must be readable");
            }
        }
        for(String asset:new String[]{"gameplay/bins/blue_collect_sparkles.png",
                "gameplay/score/points_20.png","gameplay/score/points_100.png"}){
            check(ImageIO.read(new File(asset))!=null,"Effect and score assets must be readable");
        }
        BufferedImage composition=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas=composition.createGraphics();
        String[] layerNames={"01_sky_clouds","02_fuji_mountains","03_temples_forest",
                "04_sakura_trees","05_guardrail_vegetation","06_four_lane_road"};
        int[] layerBottoms={0,330,330,330,330,90};
        int[] layerHeights={720,260,160,180,55,240};
        for(int index=0;index<layerNames.length;index++){
            BufferedImage image=ImageIO.read(new File("backgrounds/stage1fuji_layers/"+layerNames[index]+".png"));
            int top=image.getHeight(),bottom=-1;
            for(int y=0;y<image.getHeight();y++){
                int visible=0;
                for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>25)visible++;
                if(visible>image.getWidth()/100){top=Math.min(top,y);bottom=y;}
            }
            check(bottom>=top,"Fuji layers must contain visible artwork");
            int cropHeight=bottom-top+1;
            float width=ViewLayout.layerWidth(image.getWidth(),cropHeight,layerHeights[index]);
            check(Math.abs(width/layerHeights[index]-(float)image.getWidth()/cropHeight)<.001f,
                    "Parallax layers must preserve the artwork aspect ratio");
            for(int x=0;x<1280;x+=(int)Math.ceil(width)){
                canvas.drawImage(image,x,720-layerBottoms[index]-layerHeights[index],
                        x+(int)Math.ceil(width),720-layerBottoms[index],
                        0,top,image.getWidth(),bottom+1,null);
            }
        }
        canvas.dispose();
        for(int y=0;y<630;y++)for(int x=0;x<1280;x++){
            check((composition.getRGB(x,y)>>>24)==255,"Sky must cover every transparent gap above the HUD");
        }
        if(args.length>0)ImageIO.write(composition,"png",new File(args[0]));
        for(String ninja:new String[]{"fire","ice","poison","oni"}){
            for(int frame=0;frame<6;frame++)check(ImageIO.read(new File("characters/ninja_sprites/"+ninja+"_ride_"+frame+".png"))!=null,
                    "New six-frame riding sprites must load for every ninja");
            for(int frame=0;frame<8;frame++){
                check(ImageIO.read(new File("gameplay/cheering/"+ninja+"_cheer_"+frame+".png"))!=null,
                        "Every ninja must have all eight readable cheering frames");
            }
            for(String layer:new String[]{"01_sky_clouds","02_fuji_mountains","03_temples_forest",
                    "04_sakura_trees","05_guardrail_vegetation","06_four_lane_road"}){
                check(ImageIO.read(new File("backgrounds/stage1fuji_layers/"+layer+".png"))!=null,
                        "Each Fuji parallax layer must be readable");
            }
        }
        for(String effect:new String[]{"01_lane_change_blue","04_car_collision"}){
            check(ImageIO.read(new File("gameplay/powerEffects/"+effect+".png"))!=null,
                    "New rider feedback effects must load without the removed burst assets");
        }
        System.out.println("Gameplay regression checks passed");
    }

    private static com.badlogic.gdx.audio.Music fakeMusic(boolean[] playing,java.util.List<String> calls){
        return (com.badlogic.gdx.audio.Music)java.lang.reflect.Proxy.newProxyInstance(
                com.badlogic.gdx.audio.Music.class.getClassLoader(),
                new Class<?>[]{com.badlogic.gdx.audio.Music.class},(proxy,method,arguments)->{
                    if(method.getName().equals("isPlaying"))return playing[0];
                    calls.add(method.getName());
                    playing[0]=method.getName().equals("play");
                    return null;
                });
    }
    private static void check(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
}
