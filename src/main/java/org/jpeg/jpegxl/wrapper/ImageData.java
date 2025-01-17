// Copyright (c) the JPEG XL Project Authors. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package org.jpeg.jpegxl.wrapper;

import java.nio.ByteBuffer;

/**
 * POJO that contains necessary image data (dimensions, pixels,...).
 */
public class ImageData {
    public final int width;
    public final int height;
    public final ByteBuffer pixels;
    public final ByteBuffer icc;
    public final PixelFormat pixelFormat;

    ImageData(int width, int height, ByteBuffer pixels, ByteBuffer icc, PixelFormat pixelFormat) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.icc = icc;
        this.pixelFormat = pixelFormat;
    }
}
