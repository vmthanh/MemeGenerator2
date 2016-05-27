package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by CPU10924-local on 5/13/2016.
 */
public class ImageSprite {
    private float scaleFactor;
    private float X;
    private float Y;

    private int width, height;
    private int textureId = -1;

    private ImageSprite(Sticker sticker,int textureId)
    {
        this.width = sticker.getBitmapWidth();
        this.height = sticker.getBitmapHeigh();
        this.X = sticker.getX();
        this.Y = sticker.getY();
        this.scaleFactor = sticker.getmScaleFactor();
        this.textureId = textureId;
    }

    public float getX()
    {
        return X;
    }

    public float getY()
    {
        return Y;
    }


    public static ImageSprite createGLSprite(Sticker sticker)
    {
        ImageSprite imageSprite = new ImageSprite(sticker,createGlTexture());
        Log.d("G1", "image id = " + imageSprite.getTextureId());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, sticker.getBitmap(), 0);


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
