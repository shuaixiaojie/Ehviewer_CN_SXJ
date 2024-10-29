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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A image with {@link com.hippo.lib.image.Image} for data and {@link Bitmap} for render.
 */
public final class ImageBitmap implements Animatable, Runnable {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    @Nullable
    private Image mImage;
    @NonNull
    private final Bitmap mBitmap;
    private final int mFormat;
    private final boolean mIsOpaque;
    private final int mByteCount;
    private final int mFrameCount;
    private int mReferences;
    private int mAnimationReferences;
    private boolean mRunning;
    private final Set<WeakReference<Callback>> mCallbackSet = new LinkedHashSet<>();

    private ImageBitmap(@NonNull Image image) throws OutOfMemoryError {
        int width = image.getWidth();
        int height = image.getHeight();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        image.render(0, 0, mBitmap, 0, 0, width, height, false, 0);
        mFormat = image.getFormat();
        mIsOpaque = image.isOpaque();
        mByteCount = image.getByteCount();
        mFrameCount = image.getFrameCount();

        if (mFrameCount > 1) {
            // For animated image, save image object
            mImage = image;
        } else {
            // Free the image
            image.recycle();
        }
    }

    private ImageBitmap(@NonNull Bitmap bitmap) {
        mFormat = Image.FORMAT_PLAIN;
        mBitmap = bitmap;
        mIsOpaque = !bitmap.hasAlpha();
        mByteCount = bitmap.getRowBytes() * bitmap.getHeight();
        mFrameCount = 1;
    }

    /**
     * Decode {@code InputStream}, then create image.
     */
    @Nullable
    public static ImageBitmap decode(@NonNull InputStream is) {
        Image image = Image.decode(is, false);
        if (image != null) {
            return create(image);
        } else {
            return null;
        }
    }

    /**
     * Create {@code ImageBitmap} from {@code Image}.
     * It is not recommended. Use {@link #decode(InputStream)} if you can.
     *
     * @param image the image should not be used before and
     *              it must not be recycled. And the image should
     *              not be used directly anymore.
     */
    @Nullable
    public static ImageBitmap create(@NonNull Image image) {
        if (!image.isRecycled()) {
            try {
                return new ImageBitmap(image);
            } catch (OutOfMemoryError e) {
                image.recycle();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Create {@code ImageBitmap} from {@code Bitmap}.
     *
     * @param bitmap the bitmap should not be recycled
     */
    @Nullable
    public static ImageBitmap create(@NonNull Bitmap bitmap) {
        if (!bitmap.isRecycled()) {
            return new ImageBitmap(bitmap);
        } else {
            return null;
        }
    }

    /**
     * Obtain the image bitmap
     *
     * @return false for the image is recycled and obtain failed
     */
    public synchronized boolean obtain() {
        if (mBitmap.isRecycled()) {
            return false;
        } else {
            ++mReferences;
            return true;
        }
    }

    /**
     * Release the image bitmap
     */
    public synchronized void release() {
        --mReferences;
        if (mReferences <= 0 && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            if (mImage != null) {
                mImage.recycle();
            }
        }
    }

    /**
     * Add a callback for invalidating
     */
    public void addCallback(@NonNull Callback callback) {
        final Iterator<WeakReference<Callback>> iterator = mCallbackSet.iterator();
        Callback c;
        while (iterator.hasNext()) {
            c = iterator.next().get();
            if (c == null) {
                // Remove from the set if the reference has been cleared or
                // it can't be used.
                iterator.remove();
            } else if (c == callback) {
                return;
            }
        }

        mCallbackSet.add(new WeakReference<>(callback));
    }

    /**
     * Remove a callback
     */
    public void removeCallback(@NonNull Callback callback) {
        final Iterator<WeakReference<Callback>> iterator = mCallbackSet.iterator();
        Callback c;
        while (iterator.hasNext()) {
            c = iterator.next().get();
            if (c == null) {
                // Remove from the set if the reference has been cleared or
                // it can't be used.
                iterator.remove();
            } else if (c == callback) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Return the format of the image
     */
    public int getFormat() {
        return mFormat;
    }

    /**
     * Return image width
     */
    public int getWidth() {
        return mBitmap.getWidth();
    }

    /**
     * Return image height
     */
    public int getHeight() {
        return mBitmap.getHeight();
    }

    /**
     * Return image is opaque
     */
    public boolean isOpaque() {
        return mIsOpaque;
    }

    /**
     * Return byte count of image
     */
    public int getByteCount() {
        return mByteCount;
    }

    /**
     * Return image is animated
     */
    public boolean isAnimated() {
        return mImage != null;
    }

    /**
     * Return image frame count
     */
    public int getFrameCount() {
        return mFrameCount;
    }

    /**
     * Draw image to canvas
     */
    public void draw(Canvas canvas, float left, float top, @Nullable Paint paint) {
        if (!mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, left, top, paint);
        }
    }

    /**
     * Draw image to canvas
     */
    public void draw(Canvas canvas, @Nullable Rect src, @NonNull Rect dst, @Nullable Paint paint) {
        if (!mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, src, dst, paint);
        }
    }

    /**
     * Draw image to canvas
     */
    public void draw(Canvas canvas, @Nullable Rect src, @NonNull RectF dst, @Nullable Paint paint) {
        if (!mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, src, dst, paint);
        }
    }

    /**
     * {@code start()} and {@code stop()} is a pair
     */
    @Override
    public void start() {
        mAnimationReferences++;
        if (mBitmap.isRecycled() || mImage == null || mRunning) {
            return;
        }
        mRunning = true;
        HANDLER.postDelayed(this, Math.max(0, mImage.getDelay()));
    }

    /**
     * {@code start()} and {@code stop()} is a pair
     */
    @Override
    public void stop() {
        mAnimationReferences--;
        if (mAnimationReferences <= 0) {
            mRunning = false;
            HANDLER.removeCallbacks(this);
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    private boolean notifyUpdate() {
        boolean hasCallback = false;
        final Iterator<WeakReference<Callback>> iterator = mCallbackSet.iterator();
        Callback callback;
        while (iterator.hasNext()) {
            callback = iterator.next().get();
            if (callback != null) {
                // Render bitmap int the first time
                if (!hasCallback) {
                    hasCallback = true;
                    mImage.render(0, 0, mBitmap, 0, 0, mImage.getWidth(), mImage.getHeight(), false, 0);
                }
                callback.invalidateImage(this);
            } else {
                // Remove from the set if the reference has been cleared or
                // it can't be used.
                iterator.remove();
            }
        }
        return hasCallback;
    }

    @Override
    public void run() {
        // Check recycled
        if (mBitmap.isRecycled() || mImage == null) {
            mRunning = false;
            return;
        }

        mImage.advance();

        if (notifyUpdate()) {
            if (mRunning) {
                HANDLER.postDelayed(this, Math.max(0, mImage.getDelay()));
            }
        } else {
            mRunning = false;
        }
    }

    public interface Callback {
        void invalidateImage(ImageBitmap who);
    }
}
