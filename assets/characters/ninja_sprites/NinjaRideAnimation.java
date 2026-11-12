package com.pedalgaiden.presentation.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Loads the 6-frame ride cycle (256x192 per frame) of a ninja: ice, fire, poison or oni. */
public final class NinjaRideAnimation {
    private static final int FRAMES = 6, FRAME_W = 256, FRAME_H = 192;

    private final Texture sheet;
    private final Animation<TextureRegion> ride;

    public NinjaRideAnimation(String ninja) {
        sheet = new Texture(Gdx.files.internal("sprites/ninja/" + ninja + "_ride_sheet.png"));
        sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // keeps pixel art crisp
        TextureRegion[] frames = TextureRegion.split(sheet, FRAME_W, FRAME_H)[0];
        ride = new Animation<>(1f / 12f, frames); // 12 fps; scale with cadence
        ride.setPlayMode(Animation.PlayMode.LOOP);
    }

    /** cadenceFactor = currentRpm / referenceRpm, so pedaling faster animates faster. */
    public TextureRegion frame(float stateTime, float cadenceFactor) {
        return ride.getKeyFrame(stateTime * Math.max(0.2f, cadenceFactor));
    }

    public void dispose() { sheet.dispose(); }
}
