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

package com.hippo.lib.glview.image;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.hippo.lib.image.Image;

/**
 * A wrapper for {@link Image}. It is useful for multi-usage.
 * It handles image recycle automatically.
 */
public class ImageWrapper {

    private final Image mImage;
    private int mReferences;

    /**
     * Create ImageWrapper
     *
     * @param image the image should not be obtained or recycled.
     */
    public ImageWrapper(@NonNull Image image) {
        mImage = image;
    }

    /**
     * Obtain the image
     *
     * @return false for the image is recycled and obtain failed
     */
    public synchronized boolean obtain() {
        if (mImage.isRecycled()) {
            return false;
        } else {
            ++mReferences;
            return true;
        }
    }

    /**
     * Release the image
     */
    public synchronized void release() {
        --mReferences;
        if (mReferences <= 0 && !mImage.isRecycled()) {
            mImage.recycle();
        }
    }

    /**
     * @see Image#getFormat()
     */
    public int getFormat() {
        return mImage.getFormat();
    }

    /**
     * @see Image#getFormat()
     */
    public int getWidth() {
        return mImage.getWidth();
    }

    /**
     * @see Image#getHeight()
     */
    public int getHeight() {
        return mImage.getHeight();
    }

    /**
     * @see Image#complete()
     */
    public boolean complete() {
        synchronized (mImage) {
            return mImage.complete();
        }
    }

    /**
     * @see Image#isCompleted()
     */
    public boolean isCompleted() {
        return mImage.isCompleted();
    }

    /**
     * @see Image#render(int, int, Bitmap, int, int, int, int, boolean, int)
     */
    public void render(int srcX, int srcY, Bitmap dst, int dstX, int dstY,
            int width, int height, boolean fillBlank, int defaultColor) {
        mImage.render(srcX, srcY, dst, dstX, dstY,
                width, height, fillBlank, defaultColor);
    }

    /**
     * @see Image#texImage(boolean, int, int, int, int)
     */
    public void texImage(boolean init, int offsetX, int offsetY, int width, int height) {
        mImage.texImage(init, offsetX, offsetY, width, height);
    }

    /**
     * @see Image#advance()
     */
    public void advance() {
        mImage.advance();
    }

    /**
     * @see Image#getDelay()
     */
    public int getDelay() {
        return mImage.getDelay();
    }

    /**
     * @see Image#getFrameCount()
     */
    public int getFrameCount() {
        return mImage.getFrameCount();
    }

    /**
     * @see Image#isOpaque()
     */
    public boolean isOpaque() {
        return mImage.isOpaque();
    }

    /**
     * @see Image#isRecycled()
     */
    public boolean isRecycled() {
        return mImage.isRecycled();
    }
}
