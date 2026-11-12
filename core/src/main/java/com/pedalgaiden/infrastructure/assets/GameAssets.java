package com.pedalgaiden.infrastructure.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.*;

public final class GameAssets implements AutoCloseable {
    private final Map<String,Texture> images=new HashMap<>();
    private final Map<String,TextureRegion> fujiRegions=new HashMap<>();
    private final Map<String,TextureRegion> artworkRegions=new HashMap<>();
    public Texture image(String key){return images.get(key);}
    public TextureRegion fuji(String key){return fujiRegions.get(key);}
    public TextureRegion artwork(String key){return artworkRegions.get(key);}
    @Override public void close(){for(Texture texture:images.values())texture.dispose();images.clear();fujiRegions.clear();artworkRegions.clear();}
    public void load(String key,String file){ Texture t=new Texture(Gdx.files.internal(file));t.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);images.put(key,t); }
    public void loadArtwork(String key,String file){
        Pixmap pixels=new Pixmap(Gdx.files.internal(file));
        int left=pixels.getWidth(), top=pixels.getHeight(), right=-1, bottom=-1;
        for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++){
            if((pixels.getPixel(x,y)&255)>25){
                left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
            }
        }
        if(right<left){pixels.dispose();throw new IllegalArgumentException("Empty artwork: "+file);}
        Texture texture=new Texture(pixels);pixels.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        images.put(key,texture);
        artworkRegions.put(key,new TextureRegion(texture,left,top,right-left+1,bottom-top+1));
    }
    public void loadFujiLayer(String name){
        Pixmap pixels=new Pixmap(Gdx.files.internal("backgrounds/stage1fuji_layers/"+name+".png"));
        int top=pixels.getHeight(), bottom=-1;
        for(int y=0;y<pixels.getHeight();y++){
            int visible=0;
            for(int x=0;x<pixels.getWidth();x++)if((pixels.getPixel(x,y)&255)>25)visible++;
            if(visible>pixels.getWidth()/100){top=Math.min(top,y);bottom=y;}
        }
        if(bottom<top){pixels.dispose();throw new IllegalArgumentException("Fuji layer has no visible artwork: "+name);}
        Texture texture=new Texture(pixels);
        pixels.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        images.put(name,texture);
        fujiRegions.put(name,new TextureRegion(texture,0,top,texture.getWidth(),bottom-top+1));
    }
}
