package com.example.cpu10924_local.memegenerator;

import android.graphics.Bitmap;

/**
 * Created by ThanhVo on 3/31/2016.
 */
public class Sticker {
    public Bitmap bitmap;
    public float x;
    public float y;
    public float factorSize;
    public Sticker(Bitmap bmp, float x, float y,float factorSize)
    {
        this.bitmap = bmp;
        this.x = x;
        this.y = y;
        this.factorSize = factorSize;
    }
}
