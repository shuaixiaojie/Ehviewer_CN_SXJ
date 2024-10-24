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

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.getkeepsafe.relinker.ReLinker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code Image} is a image which stored pixel data in native heap
 */
public final class Image {

    /**
     * Unknown image format
     */
    public static final int FORMAT_UNKNOWN = -1;

    /**
     * Plain image format, for {@code Image} from {@link #create(Bitmap)}
     */
    public static final int FORMAT_PLAIN = 0;

    /**
     * JPEG image format
     */
    public static final int FORMAT_JPEG = 1;

    /**
     * PNG image format
     */
    public static final int FORMAT_PNG = 2;

    /**
     * GIF image format
     */
    public static final int FORMAT_GIF = 3;

    /**
     * WEBP image format
     */
    public static final int FORMAT_WEBP = 4;

    private static final AtomicInteger sImageCount = new AtomicInteger();

    private long mNativePtr;
    private final int mFormat;
    private final int mWidth;
    private final int mHeight;

    private Throwable mRecycleTracker;

    private Image(long nativePtr, int format, int width, int height) {
        mNativePtr = nativePtr;
        mFormat = format;
        mWidth = width;
        mHeight = height;

        sImageCount.getAndIncrement();
    }

    /**
     * Return the format of the image
     */
    public int getFormat() {
        return mFormat;
    }

    /**
     * Return the width of the image
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Return the height of the image
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Return the minimum number of bytes that can be used to store this image's pixels.
     */
    public int getByteCount() {
        checkRecycled();
        return nativeGetByteCount(mNativePtr, mFormat);
    }

    private void checkRecycled() {
        if (mNativePtr == 0) {
            if (mRecycleTracker != null) {
                throw new IllegalStateException("The image is recycled.", mRecycleTracker);
            } else {
                throw new IllegalStateException("The image is recycled.");
            }
        }
    }

    /**
     * Complete the image decoding
     */
    public boolean complete() {
        checkRecycled();
        return nativeComplete(mNativePtr, mFormat);
    }

    /**
     * Is the image decoding completed
     */
    public boolean isCompleted() {
        checkRecycled();
        return nativeIsCompleted(mNativePtr, mFormat);
    }

    /**
     * Render the image to {@code Bitmap}
     */
    public void render(int srcX, int srcY, Bitmap dst, int dstX, int dstY,
                       int width, int height, boolean fillBlank, int defaultColor) {
        checkRecycled();
        nativeRender(mNativePtr, mFormat, srcX, srcY, dst, dstX, dstY,
                width, height, fillBlank, defaultColor);
    }

    /**
     * Call {@code glTexImage2D} for init is true and
     * call {@code glTexSubImage2D} for init is false.
     * width * height must <= 512 * 512 or do nothing
     */
    public void texImage(boolean init, int offsetX, int offsetY, int width, int height) {
        checkRecycled();
        nativeTexImage(mNativePtr, mFormat, init, offsetX, offsetY, width, height);
    }

    /**
     * Move to next frame. Do nothing for non-animation image
     */
    public void advance() {
        checkRecycled();
        nativeAdvance(mNativePtr, mFormat);
    }

    /**
     * Return current frame delay. 0 for non-animation image
     */
    public int getDelay() {
        checkRecycled();
        int delay = nativeGetDelay(mNativePtr, mFormat);
        return delay <= 10 ? 100 : delay;
    }

    /**
     * Return frame count. 1 for non-animation image
     */
    public int getFrameCount() {
        checkRecycled();
        return nativeFrameCount(mNativePtr, mFormat);
    }

    /**
     * Return is the image opaque
     */
    public boolean isOpaque() {
        checkRecycled();
        return nativeIsOpaque(mNativePtr, mFormat);
    }

    /**
     * Return {@code true} if the image is gray.
     */
    public boolean isGray(int error) {
        checkRecycled();
        return nativeIsGray(mNativePtr, mFormat, error);
    }

    /**
     * Improves contrast in this image with CLAHE.
     */
    public void clahe(boolean toGray) {
        checkRecycled();
        nativeClahe(mNativePtr, mFormat, toGray);
    }

    /**
     * Free the native object associated with this image.
     * It must be called when the image will not be used.
     * The image can't be used after this method is called.
     */
    public void recycle() {
        if (mNativePtr != 0) {
            nativeRecycle(mNativePtr, mFormat);
            mNativePtr = 0;

            sImageCount.getAndDecrement();

            mRecycleTracker = new Throwable("It's a ImageRecycleTracker");
        }
    }

    /**
     * Returns true if this image has been recycled.
     */
    public boolean isRecycled() {
        return mNativePtr == 0;
    }

    /**
     * Decode image from {@code InputStream}
     */
    @Nullable
    public static Image decode(InputStream is, boolean partially) {
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        return nativeDecode(is, partially);
    }

    /**
     * Create a plain image from Bitmap
     */
    @Nullable
    public static Image create(Bitmap bitmap) {
        return nativeCreate(bitmap);
    }

    /**
     * Return all un-recycled {@code Image} instance count.
     * It is useful for debug.
     */
    public static int getImageCount() {
        return sImageCount.get();
    }

    /**
     * Return all supported image formats, exclude {@link #FORMAT_PLAIN}
     */
    public static int[] getSupportedImageFormats() {
        return nativeGetSupportedImageFormats();
    }

    /**
     * Return decoder description of the image format,
     * {@code null} for invalid image format.
     */
    public static String getDecoderDescription(int format) {
        return nativeGetDecoderDescription(format);
    }

    public static void initialize(Context context) {
        ReLinker.loadLibrary(context, "image");
    }

    private static native Image nativeDecode(InputStream is, boolean partially);

    private static native Image nativeCreate(Bitmap bitmap);

    private static native int nativeGetByteCount(long nativePtr, int format);

    private static native boolean nativeComplete(long nativePtr, int format);

    private static native boolean nativeIsCompleted(long nativePtr, int format);

    private static native void nativeRender(long nativePtr, int format,
                                            int srcX, int srcY, Bitmap dst, int dstX, int dstY,
                                            int width, int height, boolean fillBlank, int defaultColor);

    private static native void nativeTexImage(long nativePtr, int format,
                                              boolean init, int offsetX, int offsetY, int width, int height);

    private static native void nativeAdvance(long nativePtr, int format);

    private static native int nativeGetDelay(long nativePtr, int format);

    private static native int nativeFrameCount(long nativePtr, int format);

    private static native boolean nativeIsOpaque(long nativePtr, int format);

    private static native boolean nativeIsGray(long nativePtr, int format, int error);

    private static native void nativeClahe(long nativePtr, int format, boolean toGray);

    private static native void nativeRecycle(long nativePtr, int format);

    private static native int[] nativeGetSupportedImageFormats();

    private static native String nativeGetDecoderDescription(int format);
}
