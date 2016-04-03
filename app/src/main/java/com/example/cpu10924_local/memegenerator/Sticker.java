package com.example.cpu10924_local.memegenerator;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.InputStream;

/**
 * Created by ThanhVo on 3/31/2016.
 */
public class Sticker {
    public Bitmap bitmap;
    public float x;
    public float y;
    public Matrix matrix;
    public Drawable drawable;

    public Sticker(Bitmap bmp, float x, float y,Matrix matrix, Drawable drawable)
    {
        this.bitmap = bmp;
        this.x = x;
        this.y = y;
        this.matrix = matrix;
        this.drawable = drawable;

    }
}
