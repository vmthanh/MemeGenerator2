package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by CPU10924-local on 5/13/2016.
 */
public class ImageSprite {
    private float storeScaleFactore;
    private  float scaleFactor;
    public float X;
    public float Y;
    public float XScale =1.0f;
    public float YScale = 1.0f;
    private int width, height;
    private int textureId = -1;

    private ImageSprite(Bitmap bmp,float x, float y, float scaleFactor, float storeScaleFactor, int textureId)
    {
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
        this.X = x;
        this.Y = y;
        this.scaleFactor = scaleFactor;
        this.storeScaleFactore = storeScaleFactor;
        this.textureId = textureId;
        this.XScale = this.YScale =1f;
    }


    public static ImageSprite createGLSprite(Bitmap bitmap, float x,float y, float scaleFactor,float storeScaleFactor)
    {
        ImageSprite imageSprite = new ImageSprite(bitmap, x, y, scaleFactor, storeScaleFactor,createGlTexture());
        Log.d("G1", "image id = " + imageSprite.getTextureId());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);


        return imageSprite;
    }

    private static int createGlTexture() {
        int[] textureHandles = new int[1];
        GLES20.glGenTextures(1,textureHandles,0);
        if (textureHandles[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[0]);
            return textureHandles[0];
        } else {
            throw new RuntimeException("Error loading texture.");
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextureId() {
        return textureId;
    }
}
