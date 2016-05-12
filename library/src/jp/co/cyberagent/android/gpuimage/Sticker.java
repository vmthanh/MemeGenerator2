package jp.co.cyberagent.android.gpuimage;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import jp.co.cyberagent.android.gpuimage.ObjectDraw;

/**
 * Created by ThanhVo on 3/31/2016.
 */
public class Sticker extends ObjectDraw {

    public Matrix matrix;
    public Drawable drawable;
    public float mScaleFactor;
    public float mStoreScaleFactor;
    public float canvasWidth;
    public float canvasHeight;
    public float bitmapWidth;
    public float bitmapHeigh;


    public Sticker( float x, float y,Matrix matrix, Drawable drawable,int bitmapWidth, int bitmapHeigh)
    {

        this.x = x;
        this.y = y;
        this.drawOrder = -1;
        this.matrix = matrix;
        this.drawable = drawable;
        this.mScaleFactor = 1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = this.bitmapWidth =bitmapWidth;
        this.canvasHeight = this.bitmapHeigh = bitmapHeigh;

    }
}
