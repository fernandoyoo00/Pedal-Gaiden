package com.pedalgaiden.presentation.view;

public final class ViewLayout {
    public static final float HUD_WIDTH=1000, TRAFFIC_WARNING_WIDTH=144, TRAFFIC_WARNING_HEIGHT=44;
    private ViewLayout(){}
    public static float spriteScale(int width,int height,float maxWidth,float maxHeight){
        return Math.min(1,Math.min(maxWidth/width,maxHeight/height));
    }
    public static float layerWidth(int sourceWidth,int sourceHeight,float height){return height*sourceWidth/sourceHeight;}
}
