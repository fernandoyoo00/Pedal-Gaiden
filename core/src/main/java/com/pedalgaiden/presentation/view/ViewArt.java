package com.pedalgaiden.presentation.view;

import java.util.Locale;
import static com.pedalgaiden.presentation.view.ViewLayout.*;

final class ViewArt {
    private ViewArt(){}
    static final float H=720;
    static final Locale PT_BR=Locale.forLanguageTag("pt-BR");
    static final String[] CAR_NAMES={"red_sedan","blue_sedan","silver_suv","yellow_taxi"};
    static final String[] FUJI_LAYERS={"01_sky_clouds","02_fuji_mountains","03_temples_forest",
            "04_sakura_trees","05_guardrail_vegetation","06_four_lane_road"};
    static final float[] LAYER_SPEEDS={.03f,.08f,.16f,.30f,.65f,1f};
    static final String[] BIN_COLORS={"blue","yellow","green","red","gray"};
    static final String[] BIN_LABELS={"PAPEL","METAL","VIDRO","PL\u00c1STICO","PNEUS"};
    static final String[] stages={"DIA NO FUJI","TEMPLO AO P\u00d4R DO SOL","NOITE DAS LANTERNAS","FLORESTA DE BAMBU"};
    static final String[] ninjaLabels={"FOGO","GELO","VENENO","ONI"};
    static final String[] bg={"fuji","sunset","night","bamboo"};
    static final String[] ninjas={"fire","ice","poison","oni"};
    static final String[] mainCharacters={"mainFire","mainBlue","mainGreen","mainWhite"};
    static final String[] waste={"plastic_bottle","can","paper","glass_bottle","tire"};
    static final String[] goalIconNames={"cardio","knee","strength","balance"};
    static final String[] helpIconNames={"bike","recycle","boost","bin","trophy"};

}
