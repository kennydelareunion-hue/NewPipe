package org.schabi.newpipe.player.helper;

import static org.junit.Assert.assertEquals;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import org.junit.Test;

public class PlayerHelperTest {

    @Test
    public void fullscreenResizeModeAvoidsLetterboxing() {
        assertEquals(
                AspectRatioFrameLayout.RESIZE_MODE_FILL,
                PlayerHelper.resolveResizeModeForFullscreen(
                        AspectRatioFrameLayout.RESIZE_MODE_FIT));
        assertEquals(
                AspectRatioFrameLayout.RESIZE_MODE_FILL,
                PlayerHelper.resolveResizeModeForFullscreen(
                        AspectRatioFrameLayout.RESIZE_MODE_FILL));
        assertEquals(
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                PlayerHelper.resolveResizeModeForFullscreen(
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM));
    }

    @Test
    public void zoomScaleIsClampedToSupportedRange() {
        assertEquals(PlayerHelper.MIN_ZOOM_SCALE,
                PlayerHelper.clampZoomScale(0.25f), 0.0f);
        assertEquals(PlayerHelper.MAX_ZOOM_SCALE,
                PlayerHelper.clampZoomScale(10.0f), 0.0f);
        assertEquals(1.75f, PlayerHelper.clampZoomScale(1.75f), 0.0f);
    }
}
