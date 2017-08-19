package org.willemsens.player.imagegenerators;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class ImageGenerator {
    public static byte[] generateAlbumCover() {
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Random random = new Random();
        canvas.drawRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
