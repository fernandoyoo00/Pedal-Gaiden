package com.pedalgaiden.presentation.view;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pedalgaiden.infrastructure.assets.GameAssets;

final class DrawContext implements AutoCloseable {
    final GameAssets assets=new GameAssets();
    final SpriteBatch batch=new SpriteBatch();
    final ShapeRenderer shapes=new ShapeRenderer();
    final BitmapFont font=new BitmapFont();
    final GlyphLayout textLayout=new GlyphLayout();
    @Override public void close(){assets.close();batch.dispose();shapes.dispose();font.dispose();}

    void rect(float x,float y,float w,float h,Color color){shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(color);shapes.rect(x,y,w,h);shapes.end();}

    Color rgba(float r,float g,float b,float a){return new Color(r,g,b,a);}

    void text(String s,float x,float y,float size,Color c){batch.begin();font.setColor(c);font.getData().setScale(size);font.draw(batch,s,x,y);font.getData().setScale(1);batch.end();}

    void centeredText(String s,float centerX,float y,float size,Color c){font.getData().setScale(size);textLayout.setText(font,s);font.setColor(c);font.draw(batch,s,centerX-textLayout.width/2,y);font.getData().setScale(1);}

    void centeredLabel(String s,float centerX,float y,float size,Color c){batch.begin();centeredText(s,centerX,y,size,c);batch.end();}

    void centeredTextInRect(String s,float x,float y,float w,float h,float size,Color c){
        font.getData().setScale(size);textLayout.setText(font,s);font.setColor(c);
        font.draw(batch,s,x+(w-textLayout.width)/2,y+(h+textLayout.height)/2);
        font.getData().setScale(1);
    }

    void pic(String name,float x,float y,float w,float h){batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.image(name),x,y,w,h);batch.end();}

    void panel(float x,float y,float w,float h){rect(x,y,w,h,rgba(.018f,.055f,.13f,.94f));rect(x,y+h-4,w,4,rgba(.19f,.63f,.96f,1));}

    void button(String s,float x,float y,float w,float h,boolean primary){rect(x,y,w,h,primary?rgba(.09f,.54f,.15f,1):rgba(.10f,.20f,.34f,1));rect(x,y+h-4,w,4,primary?Color.LIME:rgba(.30f,.62f,.9f,1));fittedLabel(s,x+8,y+5,w-16,h-10,1.7f,Color.WHITE);}

    boolean hit(float mx,float my,float x,float y,float w,float h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}

    void fittedLabel(String s,float x,float y,float w,float h,float size,Color color){
        font.getData().setScale(size);textLayout.setText(font,s);
        float fitted=size*Math.min(1,Math.min(w/Math.max(1,textLayout.width),h/Math.max(1,textLayout.height)));
        batch.begin();centeredTextInRect(s,x,y,w,h,fitted,color);batch.end();
    }
}
