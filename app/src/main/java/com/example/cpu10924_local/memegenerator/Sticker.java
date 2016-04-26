package com.example.cpu10924_local.memegenerator;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.InputStream;

/**
 * Created by ThanhVo on 3/31/2016.
 */
public class Sticker extends ObjectDraw{
    public Bitmap bitmap;
    public Matrix matrix;
    public Drawable drawable;
    public float mScaleFactor;
    public float mStoreScaleFactor;
    public float canvasWidth;
    public float canvasHeight;


    public Sticker(Bitmap bmp, float x, float y,Matrix matrix, Drawable drawable)
    {
        this.bitmap = bmp;
        this.x = x;
        this.y = y;
        this.drawOrder = -1;
        this.matrix = matrix;
        this.drawable = drawable;
        this.mScaleFactor = 1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = bmp.getWidth();
        this.canvasHeight = bmp.getHeight();

    }
}
