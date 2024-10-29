/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.lib.image;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

/**
 * A drawable to draw {@link ImageBitmap}
 */
public class ImageDrawable extends Drawable implements Animatable, ImageBitmap.Callback {

    private final ImageBitmap mImageBitmap;
    private final Paint mPaint;

    public ImageDrawable(ImageBitmap imageBitmap) throws RecycledException {
        if (!imageBitmap.obtain()) {
            throw new RecycledException();
        }

        mImageBitmap = imageBitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        // Add callback
        imageBitmap.addCallback(this);
    }

    public void recycle() {
        mImageBitmap.removeCallback(this);
        mImageBitmap.release();
    }

    @Override
    public void draw(Canvas canvas) {
        mImageBitmap.draw(canvas, null, getBounds(), mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mImageBitmap.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.UNKNOWN;
    }

    @Override
    public int getIntrinsicWidth() {
        return mImageBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mImageBitmap.getHeight();
    }

    @Override
    public void start() {
        mImageBitmap.start();
    }

    @Override
    public void stop() {
        mImageBitmap.stop();
    }

    @Override
    public boolean isRunning() {
        return mImageBitmap.isRunning();
    }

    @Override
    public void invalidateImage(ImageBitmap who) {
        invalidateSelf();
    }
}
