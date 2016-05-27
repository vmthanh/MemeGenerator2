package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ThanhVo on 3/31/2016.
 */
public class Sticker extends ObjectDraw {

    private Matrix matrix;
    private Drawable drawable;
    private float mScaleFactor;
    private float mStoreScaleFactor;
    private float canvasWidth;
    private float canvasHeight;
    private int bitmapWidth;
    private int bitmapHeigh;
    private Bitmap bitmap;


    public Sticker( float x, float y,Matrix matrix,int bitmapWidth, int bitmapHeigh, Bitmap bitmap,Drawable drawable)
    {

        this.setX(x);
        this.setY(y);
        this.setDrawOrder(-1);
        this.matrix= matrix;
        this.mScaleFactor = 1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = this.bitmapWidth =bitmapWidth;
        this.canvasHeight = this.bitmapHeigh = bitmapHeigh;
        this.bitmap = bitmap;
        this.drawable = drawable;

    }
    public Sticker(float x, float y,Bitmap bitmap)
    {
        this.setX(x);
        this.setY(y);
        this.bitmap = bitmap;
        this.mScaleFactor =1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = this.bitmapWidth = bitmap.getWidth();
        this.canvasHeight = this.bitmapHeigh = bitmap.getHeight();
    }
    public static Comparator<Sticker> drawOrderComparatorSticker;
    static {
        drawOrderComparatorSticker = new Comparator<Sticker>() {
            @Override
            public int compare(Sticker lhs, Sticker rhs) {
                return lhs.getDrawOrder() - rhs.getDrawOrder();
            }
        };
    }

    public void sendToFrontSticker(List<Sticker> stickerList)
    {
        int newDrawOrder = 0;
        for(int i=0; i<stickerList.size(); ++i)
        {
            if (stickerList.get(i)!=null && newDrawOrder <= stickerList.get(i).getDrawOrder())
            {
                newDrawOrder = stickerList.get(i).getDrawOrder() +1;
            }
        }
        this.setDrawOrder(newDrawOrder);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public float getmScaleFactor() {
        return mScaleFactor;
    }

    public void setmScaleFactor(float mScaleFactor) {
        this.mScaleFactor = mScaleFactor;
    }

    public float getmStoreScaleFactor() {
        return mStoreScaleFactor;
    }

    public void setmStoreScaleFactor(float mStoreScaleFactor) {
        this.mStoreScaleFactor = mStoreScaleFactor;
    }

    public float getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasWidth(float canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public float getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasHeight(float canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public int getBitmapHeigh() {
        return bitmapHeigh;
    }

    public void setBitmapHeigh(int bitmapHeigh) {
        this.bitmapHeigh = bitmapHeigh;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
