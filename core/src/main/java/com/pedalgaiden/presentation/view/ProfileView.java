package com.pedalgaiden.presentation.view;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.pedalgaiden.presentation.viewmodel.FlowViewModel;
import com.pedalgaiden.presentation.viewmodel.RaceUiState.*;
import static com.pedalgaiden.domain.RaceRules.*;
import static com.pedalgaiden.presentation.view.ViewArt.*;

final class ProfileView {
    private final FlowViewModel flow;
    private final DrawContext draw;
    ProfileView(FlowViewModel flow,DrawContext draw){this.flow=flow;this.draw=draw;}

    private void surveyHeader(String heading,int activeStep){
        draw.centeredLabel(heading,W/2,625,2.7f,Color.WHITE);
        float progressWidth=125;
        float progressGap=25;
        float startX=(W-(progressWidth*4+progressGap*3))/2;
        for(int i=0;i<4;i++){
            float x=startX+i*(progressWidth+progressGap);
            draw.centeredLabel(Integer.toString(i+1),x+progressWidth/2,570,1.5f,Color.WHITE);
            draw.rect(x,550,progressWidth,6,i<=activeStep?Color.LIME:Color.GRAY);
        }
    }

    private void surveyBackdrop(){
        draw.pic("level_menu_bg",0,0,W,H);
        TextureRegion panel=draw.assets.artwork("survey_panel");
        draw.batch.begin();draw.batch.setColor(Color.WHITE);
        draw.batch.draw(panel,140,38,1000,644);
        draw.batch.end();
    }

    private void surveyBase(String heading,int step){surveyBackdrop();surveyHeader(heading,step);}

    void profile(){
        surveyBackdrop();
        surveyHeader("PERFIL DO PACIENTE",0);
        profileField("Nome",flow.profile().name(),485,false);
        profileStepper("Idade",flow.profile().age()+"",431);
        profileStepper("Peso (kg)",flow.profile().weight()+"",377);
        profileField("Objetivo da reabilita\u00e7\u00e3o",flow.profile().rehabilitationGoal(),323,true);
        profileField("Atividade semanal",flow.profile().activity(),269,true);
        draw.text("PERSONAGEM PREFERIDO",500,246,1.4f,Color.WHITE);
        for(int i=0;i<4;i++){
            float x=368+i*142;
            draw.rect(x,132,120,96,i==flow.profile().character()?Color.LIME:draw.rgba(.20f,.42f,.66f,1));
            draw.rect(x+3,135,114,90,draw.rgba(.025f,.09f,.19f,1));
            draw.pic(mainCharacters[i],x+19,139,82,82);
        }
        draw.button("VOLTAR",215,76,190,50,false);
        draw.button("AVAN\u00c7AR",875,76,190,50,true);
    }

    private void profileField(String label,String value,float y,boolean dropdown){
        draw.fittedLabel(label,215,y-2,275,42,1.45f,Color.WHITE);
        draw.rect(500,y-2,575,42,draw.rgba(.24f,.39f,.56f,1));
        draw.rect(503,y+1,569,36,draw.rgba(.025f,.075f,.15f,1));
        draw.fittedLabel(value,514,y+1,dropdown?510:546,36,1.35f,Color.WHITE);
        if(dropdown)draw.fittedLabel("v",1031,y+1,40,36,1.4f,Color.WHITE);
    }

    private void profileStepper(String label,String value,float y){
        draw.fittedLabel(label,215,y-2,275,42,1.45f,Color.WHITE);
        draw.rect(500,y-2,575,42,draw.rgba(.24f,.39f,.56f,1));
        draw.rect(503,y+1,56,36,draw.rgba(.12f,.25f,.40f,1));
        draw.rect(561,y+1,456,36,draw.rgba(.025f,.075f,.15f,1));
        draw.rect(1019,y+1,53,36,draw.rgba(.12f,.25f,.40f,1));
        draw.fittedLabel("<",503,y+1,56,36,1.5f,Color.WHITE);
        draw.fittedLabel(value,561,y+1,456,36,1.35f,Color.WHITE);
        draw.fittedLabel(">",1019,y+1,53,36,1.5f,Color.WHITE);
    }

    void goals(){
        surveyBase("SEUS OBJETIVOS",1);
        String[] names={"MELHORAR A RESIST\u00caNCIA CARDIOVASCULAR","AUMENTAR A MOBILIDADE DO JOELHO","FORTALECER OS MEMBROS INFERIORES","MELHORAR O EQUIL\u00cdBRIO E A COORDENA\u00c7\u00c3O"};
        for(int i=0;i<4;i++){
            float y=450-i*82;
            draw.rect(320,y,650,65,flow.profile().goal(i)?draw.rgba(.06f,.30f,.20f,1):draw.rgba(.08f,.15f,.28f,1));
            draw.pic(flow.profile().goal(i)?"goal_checkbox_on":"goal_checkbox_off",334,y+14,36,36);
            draw.pic("goal_"+goalIconNames[i],382,y+10,44,44);
            draw.fittedLabel(names[i],438,y+8,520,49,1.45f,Color.WHITE);
        }
        draw.button("VOLTAR",320,100,165,48,false);draw.button("AVAN\u00c7AR",815,100,165,48,true);
    }

    void experience(){surveyBase("EXPERI\u00caNCIA",2);draw.fittedLabel("ESCOLHA UM N\u00cdVEL CONFORT\u00c1VEL PARA VOC\u00ca",320,490,660,40,1.8f,Color.WHITE);String[] opts={"INICIANTE", "INTERMEDI\u00c1RIO", "EXPERIENTE"};for(int i=0;i<3;i++)draw.button((flow.profile().experience()==i?"> ":"  ")+opts[i],350,410-i*85,560,66,flow.profile().experience()==i);draw.fittedLabel("A dificuldade altera o ritmo dos itens, n\u00e3o as metas cl\u00ednicas.",320,150,660,35,1.3f,Color.LIGHT_GRAY);draw.button("VOLTAR",320,100,165,48,false);draw.button("AVAN\u00c7AR",815,100,165,48,true);}

    void confirm(){surveyBase("CONFIRMAR SESS\u00c3O",3);draw.fittedLabel("JOGADOR: "+flow.profile().name(),330,475,640,45,2,Color.WHITE);draw.text("IDADE: "+flow.profile().age()+"   SESS\u00c3O: "+flow.duration()+" MIN",330,445,2,Color.WHITE);draw.fittedLabel("EXPERI\u00caNCIA: "+new String[]{"INICIANTE","INTERMEDI\u00c1RIO","EXPERIENTE"}[flow.profile().experience()],330,355,640,45,2,Color.WHITE);draw.text("NINJA: "+ninjaLabels[flow.profile().character()],330,325,2,Color.WHITE);draw.fittedLabel("O jogo usa cad\u00eancia; n\u00e3o \u00e9 uma avalia\u00e7\u00e3o cl\u00ednica.",320,220,660,40,1.35f,Color.LIGHT_GRAY);draw.button("VOLTAR",320,100,165,48,false);draw.button("ESCOLHER FASE",755,100,225,48,true);}

    void levels(){
        draw.pic("level_menu_bg",0,0,W,H);
        draw.centeredLabel("ESCOLHA SUA FASE",640,660,2.7f,Color.WHITE);
        draw.centeredLabel("ESCOLHA O CEN\u00c1RIO DA SUA PR\u00d3XIMA PEDALADA",640,596,1.15f,draw.rgba(.92f,.91f,.86f,1));

        for(int i=0;i<stages.length;i++){
            float x=70+i*300;
            float y=202;
            float cardWidth=280;
            float cardHeight=350;
            boolean selected=i==flow.level();
            Color frame=selected?draw.rgba(.48f,1f,.12f,1):draw.rgba(.16f,.43f,.73f,1);

            draw.rect(x-5,y-5,cardWidth+10,cardHeight+10,frame);
            draw.rect(x,y,cardWidth,cardHeight,draw.rgba(.012f,.055f,.13f,.98f));
            draw.rect(x+5,y+5,cardWidth-10,cardHeight-10,draw.rgba(.018f,.09f,.20f,.98f));
            draw.pic("bg_"+bg[i],x+10,y+137,cardWidth-20,190);
            draw.rect(x+10,y+132,cardWidth-20,5,draw.rgba(.02f,.06f,.12f,1));

            draw.centeredLabel("FASE "+(i+1),x+cardWidth/2,y+112,1.05f,draw.rgba(.78f,.82f,.89f,1));
            draw.fittedLabel(stages[i],x+12,y+62,cardWidth-24,36,1.35f,Color.WHITE);

            float buttonX=x+20;
            float buttonY=y+17;
            float buttonWidth=cardWidth-40;
            float buttonHeight=46;
            draw.rect(buttonX-3,buttonY-3,buttonWidth+6,buttonHeight+6,selected?draw.rgba(.43f,.94f,.08f,1):draw.rgba(.19f,.43f,.70f,1));
            draw.rect(buttonX,buttonY,buttonWidth,buttonHeight,selected?draw.rgba(.08f,.48f,.05f,1):draw.rgba(.07f,.17f,.31f,1));
            draw.centeredLabel(selected?"SELECIONADA":"SELECIONAR",x+cardWidth/2,buttonY+15,1.3f,Color.WHITE);
        }

        float backX=82;
        float actionY=58;
        draw.rect(backX-4,actionY-4,210,66,draw.rgba(.16f,.43f,.73f,1));
        draw.rect(backX,actionY,202,58,draw.rgba(.06f,.15f,.28f,1));
        draw.centeredLabel("VOLTAR",backX+101,actionY+19,1.5f,Color.WHITE);

        float startX=910;
        draw.rect(startX-5,actionY-5,310,68,draw.rgba(.48f,1f,.12f,1));
        draw.rect(startX,actionY,300,58,draw.rgba(.07f,.46f,.04f,1));
        draw.pic("help_bike",startX+25,actionY+5,48,48);
        draw.centeredLabel("PEDALAR",startX+190,actionY+19,1.55f,Color.WHITE);
    }
}
