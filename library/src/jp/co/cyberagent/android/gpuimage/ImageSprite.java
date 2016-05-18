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

    private int width, height;
    private int textureId = -1;

    private ImageSprite(Sticker sticker,int textureId)
    {
        this.width = sticker.bitmapWidth;
        this.height = sticker.bitmapHeigh;
        this.X = sticker.x;
        this.Y = sticker.y;
        this.scaleFactor = sticker.mScaleFactor;
        this.storeScaleFactore = sticker.mStoreScaleFactor;
        this.textureId = textureId;
    }



    public static ImageSprite createGLSprite(Sticker sticker)
    {
        ImageSprite imageSprite = new ImageSprite(sticker,createGlTexture());
        Log.d("G1", "image id = " + imageSprite.getTextureId());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, sticker.bitmap, 0);


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

    public float getScale()
    {
        return scaleFactor;
    }
}
