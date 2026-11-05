package com.pedalgaiden;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Main menu built with the REAL PedalGaiden assets; no flattened screenshot. */
public final class MainMenuScreen extends ScreenAdapter {
    private static final float W = 1280, H = 720;
    private static final float MENU_X = 420, MENU_WIDTH = 440, MENU_HEIGHT = 54;
    private static final float MENU_TOP_Y = 320, MENU_SPACING = 60;
    private static final String[] OPTIONS = {"Play Game", "Patient Profile", "Instructions", "Settings", "Credits"};
    private static final String[] ICON_FILES = {
            "ui_icon_play.png", "ui_icon_profile.png", "ui_icon_instructions.png",
            "ui_icon_settings.png", "ui_icon_credits.png"
    };
    private final Game game;
    private final FitViewport viewport = new FitViewport(W, H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 mouse = new Vector3();
    private Texture background;
    private Texture logo;
    private Texture mainNinjaCyclist;
    private final Texture[] menuIcons = new Texture[OPTIONS.length];
    private int selected;
    private String notice = "";

    public MainMenuScreen(Game game) {
        this.game = game;
        background = texture("backgrounds/fuji.png");
        logo = texture("backgrounds/pedal_gaiden_logo.png");
        mainNinjaCyclist = texture("characters/mainNinjaCyclist.png");
        for (int i = 0; i < ICON_FILES.length; i++) {
            menuIcons[i] = texture("menu/pedalgaiden_menu_icons/" + ICON_FILES[i]);
        }
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    private Texture texture(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }
    @Override public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.DOWN || key == Input.Keys.S) selected = (selected + 1) % OPTIONS.length;
                else if (key == Input.Keys.UP || key == Input.Keys.W) selected = (selected + OPTIONS.length - 1) % OPTIONS.length;
                else if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) activate();
                else return false;
                return true;
            }
            @Override public boolean mouseMoved(int x, int y) {
                int index = optionAt(x, y);
                if (index >= 0) selected = index;
                return false;
            }
            @Override public boolean touchDown(int x, int y, int pointer, int button) {
                int index = optionAt(x, y);
                if (index < 0) return false;
                selected = index;
                activate();
                return true;
            }
        });
    }
    private int optionAt(int sx, int sy) {
        mouse.set(sx, sy, 0);
        viewport.unproject(mouse);
        for (int i = 0; i < OPTIONS.length; i++) {
            float y = MENU_TOP_Y - i * MENU_SPACING;
            if (mouse.x >= MENU_X && mouse.x <= MENU_X + MENU_WIDTH && mouse.y >= y && mouse.y <= y + MENU_HEIGHT) return i;
        }
        return -1;
    }
    private void activate() {
        if (selected == 0) {
            // Existing prototype has its own menu/page flow; it remains available.
            game.setScreen(new PedalGaidenScreen(false));
            dispose();
        } else if (selected == 1) {
            game.setScreen(new PedalGaidenScreen(game, false, true));
            dispose();
        } else if (selected == 2) {
            game.setScreen(PedalGaidenScreen.instructions(game, false));
            dispose();
        } else {
            notice = OPTIONS[selected] + " - COMING NEXT";
        }
    }
    private void rect(float x,float y,float w,float h,Color c) {
        shapes.setColor(c); shapes.rect(x,y,w,h);
    }
    private void text(String s,float cx,float baseline,float scale,Color color) {
        font.getData().setScale(scale);
        layout.setText(font,s);
        font.setColor(color);
        font.draw(batch,s,cx-layout.width/2,baseline);
    }
    private void menuLabel(String s,float cx,float baseline,float scale,Color color) {
        font.getData().setScale(scale);
        layout.setText(font,s);
        font.setColor(color);
        float left=cx-(layout.width+1)/2;
        font.draw(batch,s,left,baseline);
        font.draw(batch,s,left+1,baseline);
    }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.02f,0.08f,0.15f,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(background,0,0,W,H);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float menuBottom = MENU_TOP_Y - (OPTIONS.length - 1) * MENU_SPACING;
        float menuTop = MENU_TOP_Y + MENU_HEIGHT;
        float menuPadding = 10;
        rect(MENU_X - menuPadding, menuBottom - menuPadding,
            MENU_WIDTH + menuPadding * 2, menuTop - menuBottom + menuPadding * 2,
            new Color(0.008f, 0.035f, 0.09f, 0.84f));
        for(int i=0;i<OPTIONS.length;i++) {
            float y=MENU_TOP_Y-i*MENU_SPACING;
            boolean active=i==selected;
            Color fill=active?new Color(0.13f,0.105f,0.025f,0.98f):new Color(0.008f,0.035f,0.09f,0.94f);
            rect(MENU_X,y,MENU_WIDTH,MENU_HEIGHT,fill);
        }
        shapes.end();

        batch.begin();
        for(int i=0;i<OPTIONS.length;i++) {
            float y=MENU_TOP_Y-i*MENU_SPACING;
            batch.draw(menuIcons[i],MENU_X+22,y+9,36,36);
            menuLabel(OPTIONS[i],MENU_X+MENU_WIDTH/2+18,y+MENU_HEIGHT/2+9,1.7f,i==selected?Color.GOLD:Color.WHITE);
        }
        batch.draw(logo,400,400,480,320);
        batch.draw(mainNinjaCyclist,105,42,210,210,0,0,mainNinjaCyclist.getWidth(),mainNinjaCyclist.getHeight()-10,false,false);
        text("PEDAL. RECYCLE. RESTORE THE WORLD.",640,35,1.35f,Color.WHITE);
        text("© 2025 PEDAL GAIDEN",640,13,1.0f,Color.LIGHT_GRAY);
        if(!notice.isEmpty()) text(notice,640,75,1.1f,Color.YELLOW);
        batch.end();
    }
    @Override public void resize(int width,int height) { viewport.update(width,height,true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        background.dispose(); logo.dispose(); mainNinjaCyclist.dispose();
        for (Texture icon : menuIcons) icon.dispose();
        font.dispose(); batch.dispose(); shapes.dispose();
    }
}
