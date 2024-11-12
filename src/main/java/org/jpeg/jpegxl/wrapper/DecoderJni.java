// Copyright (c) the JPEG XL Project Authors. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package org.jpeg.jpegxl.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;

/**
 * Low level JNI wrapper.
 * <p>
 * This class is package-private, should be only be used by high level wrapper.
 */
class DecoderJni {

    static {
        try {
            loadJarDll("/win32-x86-64/brotlicommon.dll");
            loadJarDll("/win32-x86-64/brotlidec.dll");
            loadJarDll("/win32-x86-64/jxl_threads.dll");
            loadJarDll("/win32-x86-64/jxl_jni.dll");
        } catch (UnsatisfiedLinkError ex) {
            String message =
                    "If the nested exception message says that some standard library (stdc++, tcmalloc, etc.) was not found, "
                            + "it is likely that JDK discovered by the build system overrides library search path. "
                            + "Try specifying a different JDK via JAVA_HOME environment variable and doing a clean build.";
            throw new RuntimeException(message, ex);
        } catch (IOException ignored) {
            System.loadLibrary("brotlicommon");
            System.loadLibrary("brotlidec");
            System.loadLibrary("jxl_threads");
            System.loadLibrary("jxl_jni");
        }
    }

    static void loadJarDll(String name) throws IOException {
        InputStream in = DecoderJni.class.getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        int read = -1;

        File temp = new File(new File(System.getProperty("java.io.tmpdir"), "AetherJFXJPEGXL"), name);
        if (!temp.exists()) {
            temp.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(temp);

            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
        }
        in.close();

        System.load(temp.getAbsolutePath());
    }

    private static native void nativeGetBasicInfo(int[] context, Buffer data);

    private static native void nativeGetPixels(int[] context, Buffer data, Buffer pixels, Buffer icc);

    static Status makeStatus(int statusCode) {
        return switch (statusCode) {
            case 0 -> Status.OK;
            case -1 -> Status.INVALID_STREAM;
            case 1 -> Status.NOT_ENOUGH_INPUT;
            default -> throw new IllegalStateException("Unknown status code");
        };
    }

    static StreamInfo makeStreamInfo(int[] context) {
        StreamInfo result = new StreamInfo();
        result.status = makeStatus(context[0]);
        result.width = context[1];
        result.height = context[2];
        result.pixelsSize = context[3];
        result.iccSize = context[4];
        result.alphaBits = context[5];
        return result;
    }

    /**
     * Decode stream information.
     */
    static StreamInfo getBasicInfo(Buffer data, PixelFormat pixelFormat) {
        if (!data.isDirect()) {
            throw new IllegalArgumentException("data must be direct buffer");
        }
        int[] context = new int[6];
        context[0] = (pixelFormat == null) ? -1 : pixelFormat.ordinal();
        nativeGetBasicInfo(context, data);
        return makeStreamInfo(context);
    }

    /**
     * One-shot decoding.
     */
    static Status getPixels(Buffer data, Buffer pixels, Buffer icc, PixelFormat pixelFormat) {
        if (!data.isDirect()) {
            throw new IllegalArgumentException("data must be direct buffer");
        }
        if (!pixels.isDirect()) {
            throw new IllegalArgumentException("pixels must be direct buffer");
        }
        if (!icc.isDirect()) {
            throw new IllegalArgumentException("icc must be direct buffer");
        }
        int[] context = new int[1];
        context[0] = pixelFormat.ordinal();
        nativeGetPixels(context, data, pixels, icc);
        return makeStatus(context[0]);
    }

    /**
     * Utility library, disable object construction.
     */
    private DecoderJni() {
    }
}
