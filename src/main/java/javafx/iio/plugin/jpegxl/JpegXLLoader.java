package javafx.iio.plugin.jpegxl;

import javafx.iio.*;
import org.jpeg.jpegxl.wrapper.Decoder;
import org.jpeg.jpegxl.wrapper.ImageData;
import org.jpeg.jpegxl.wrapper.PixelFormat;

import javax.imageio.IIOException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class JpegXLLoader extends IIOLoader {
    private static final String FORMAT_NAME = "JpegXL";
    private static final List<String> EXTENSIONS = List.of("jxl");
    private static final List<String> MIME_TYPES = List.of("image/jpeg-xl");
    private static final List<IIOSignature> SIGNATURES = List.of(
            new IIOSignature((byte) 0xFF, (byte) 0x0A),
            new IIOSignature((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x4A, (byte) 0x58, (byte) 0x4C, (byte) 0x20, (byte) 0x0D, (byte) 0x0A, (byte) 0x87, (byte) 0x0A)
    );

    public static void register() {
        IIO.registerImageLoader(FORMAT_NAME, EXTENSIONS, SIGNATURES, MIME_TYPES, JpegXLLoader::new);
    }

    private JpegXLLoader(InputStream stream) {
        super(stream);
    }

    @Override
    public IIOImageFrame decode(int imageIndex, int rWidth, int rHeight, boolean preserveAspectRatio, boolean smooth) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[8192];
            while (true) {
                int read = stream.read(bytes, 0, bytes.length);
                if (read < 0) break;
                baos.write(bytes, 0, read);
            }

            int length = baos.size();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
            byteBuffer.put(baos.toByteArray(), 0, length);

            ImageData imageData = Decoder.decode(byteBuffer, PixelFormat.RGBA_8888);
            IIOImageFrame imageFrame = new IIOImageFrame(
                    IIOImageType.RGBA,
                    imageData.pixels,
                    imageData.width,
                    imageData.height,
                    imageData.width * 4,
                    null,
                    new IIOImageMetadata(
                            null, Boolean.TRUE, null, null, null, null, null,
                            imageData.width, imageData.height,
                            null, null, null
                    )
            );

            int[] outWH = IIOImageTools.computeDimensions(imageFrame.getWidth(), imageFrame.getHeight(), rWidth, rHeight, preserveAspectRatio);
            rWidth = outWH[0];
            rHeight = outWH[1];

            return imageFrame.getWidth() != rWidth || imageFrame.getHeight() != rHeight
                    ? IIOImageTools.scaleImageFrame(imageFrame, rWidth, rHeight, smooth)
                    : imageFrame;
        } catch (Exception e) {
            throw new IIOException(e.getMessage(), e);
        }
    }
}