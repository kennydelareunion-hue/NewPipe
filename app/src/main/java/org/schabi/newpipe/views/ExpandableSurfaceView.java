package org.schabi.newpipe.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;

public class ExpandableSurfaceView extends SurfaceView {
    private static final float MIN_USER_SCALE = 1.0f;
    private static final float MAX_USER_SCALE = 3.0f;

    private int resizeMode = RESIZE_MODE_FIT;
    private int baseHeight = 0;
    private int maxHeight = 0;
    private float videoAspectRatio = 0.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float userScale = 1.0f;
    private float userTranslationX = 0.0f;
    private float userTranslationY = 0.0f;

    public ExpandableSurfaceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoAspectRatio == 0.0f) {
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        final boolean verticalVideo = videoAspectRatio < 1;
        // Use maxHeight only on non-fit resize mode and in vertical videos
        int height = maxHeight != 0
                && resizeMode != RESIZE_MODE_FIT
                && verticalVideo ? maxHeight : baseHeight;

        if (width == 0 || height == 0) {
            return;
        }

        final float viewAspectRatio = width / ((float) height);
        final float aspectDeformation = (videoAspectRatio / viewAspectRatio) - 1;
        scaleX = 1.0f;
        scaleY = 1.0f;

        if (resizeMode == RESIZE_MODE_FIT) {
            if (aspectDeformation > 0) {
                height = (int) (width / videoAspectRatio);
            } else {
                width = (int) (height * videoAspectRatio);
            }
        } else if (resizeMode == RESIZE_MODE_ZOOM) {
            if (aspectDeformation < 0) {
                scaleY = viewAspectRatio / videoAspectRatio;
            } else {
                scaleX = videoAspectRatio / viewAspectRatio;
            }
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    /**
     * Scale view only in {@link #onLayout} to make transition for ZOOM mode as smooth as possible.
     * The user scale applied by pinch-to-zoom is combined with the base scale of the resize mode.
     */
    @Override
    protected void onLayout(final boolean changed,
                            final int left, final int top, final int right, final int bottom) {
        applyTransform();
    }

    private void applyTransform() {
        clampTranslation();
        setScaleX(scaleX * userScale);
        setScaleY(scaleY * userScale);
        setTranslationX(userTranslationX);
        setTranslationY(userTranslationY);
    }

    private void clampTranslation() {
        final float width = getWidth();
        final float height = getHeight();
        if (width <= 0 || height <= 0 || userScale <= MIN_USER_SCALE) {
            userTranslationX = 0.0f;
            userTranslationY = 0.0f;
            return;
        }

        final float maxTranslationX = (scaleX * userScale - 1.0f) * width / 2.0f;
        final float maxTranslationY = (scaleY * userScale - 1.0f) * height / 2.0f;
        userTranslationX = Math.max(-maxTranslationX, Math.min(maxTranslationX,
                userTranslationX));
        userTranslationY = Math.max(-maxTranslationY, Math.min(maxTranslationY,
                userTranslationY));
    }

    /**
     * @return the current user zoom scale, always between 1 and 3
     */
    public float getScale() {
        return userScale;
    }

    /**
     * Applies a zoom centered on the view.
     *
     * @param newScale the requested user scale
     */
    public void setScale(final float newScale) {
        setScale(newScale, getWidth() / 2.0f, getHeight() / 2.0f);
    }

    /**
     * Applies a continuous zoom while keeping the content below the focus point in place.
     *
     * @param newScale the requested user scale
     * @param focusX   horizontal focus coordinate in view coordinates
     * @param focusY   vertical focus coordinate in view coordinates
     */
    public void setScale(final float newScale, final float focusX, final float focusY) {
        final float clampedScale = Math.max(MIN_USER_SCALE, Math.min(MAX_USER_SCALE, newScale));
        if (clampedScale == userScale) {
            return;
        }

        final float width = getWidth();
        final float height = getHeight();
        if (width <= 0 || height <= 0) {
            userScale = clampedScale;
            applyTransform();
            return;
        }

        final float centerX = width / 2.0f;
        final float centerY = height / 2.0f;
        final float scaleRatio = clampedScale / userScale;
        userTranslationX = focusX - centerX
                - (focusX - centerX - userTranslationX) * scaleRatio;
        userTranslationY = focusY - centerY
                - (focusY - centerY - userTranslationY) * scaleRatio;
        userScale = clampedScale;
        applyTransform();
    }

    /**
     * Moves the zoomed content, keeping it inside the view bounds.
     *
     * @param deltaX horizontal translation in pixels
     * @param deltaY vertical translation in pixels
     */
    public void setTranslation(final float deltaX, final float deltaY) {
        if (userScale <= MIN_USER_SCALE) {
            return;
        }
        userTranslationX = deltaX;
        userTranslationY = deltaY;
        applyTransform();
    }

    /**
     * Translates the zoomed content by the given deltas.
     *
     * @param deltaX horizontal translation in pixels
     * @param deltaY vertical translation in pixels
     */
    public void panBy(final float deltaX, final float deltaY) {
        if (userScale <= MIN_USER_SCALE) {
            return;
        }
        userTranslationX += deltaX;
        userTranslationY += deltaY;
        applyTransform();
    }

    /**
     * Restores the original, unzoomed content.
     */
    public void resetScale() {
        userScale = MIN_USER_SCALE;
        userTranslationX = 0.0f;
        userTranslationY = 0.0f;
        applyTransform();
    }

    /**
     * @param base The height that will be used in every resize mode as a minimum height
     * @param max  The max height for vertical videos in non-FIT resize modes
     */
    public void setHeights(final int base, final int max) {
        if (baseHeight == base && maxHeight == max) {
            return;
        }
        baseHeight = base;
        maxHeight = max;
        requestLayout();
    }

    public void setResizeMode(@AspectRatioFrameLayout.ResizeMode final int newResizeMode) {
        if (resizeMode == newResizeMode) {
            return;
        }

        resizeMode = newResizeMode;
        requestLayout();
    }

    @AspectRatioFrameLayout.ResizeMode
    public int getResizeMode() {
        return resizeMode;
    }

    public void setAspectRatio(final float aspectRatio) {
        if (videoAspectRatio == aspectRatio || aspectRatio == 0 || !Float.isFinite(aspectRatio)) {
            return;
        }

        videoAspectRatio = aspectRatio;
        requestLayout();
    }
}
