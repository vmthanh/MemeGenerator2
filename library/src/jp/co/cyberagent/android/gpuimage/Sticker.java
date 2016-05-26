package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import java.util.Comparator;
import java.util.List;

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
    public int bitmapWidth;
    public int bitmapHeigh;
    public Bitmap bitmap;


    public Sticker( float x, float y,Matrix matrix,int bitmapWidth, int bitmapHeigh, Bitmap bitmap,Drawable drawable)
    {

        this.x = x;
        this.y = y;
        this.drawOrder = -1;
        this.matrix = matrix;
        this.mScaleFactor = 1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = this.bitmapWidth =bitmapWidth;
        this.canvasHeight = this.bitmapHeigh = bitmapHeigh;
        this.bitmap = bitmap;
        this.drawable = drawable;

    }
    public Sticker(float x, float y,Bitmap bitmap)
    {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
        this.mScaleFactor = 1f;
        this.mStoreScaleFactor = 1f;
        this.canvasWidth = this.bitmapWidth = bitmap.getWidth();
        this.canvasHeight = this.bitmapHeigh = bitmap.getHeight();
    }
    public static Comparator<Sticker> drawOrderComparatorSticker;
    static {
        drawOrderComparatorSticker = new Comparator<Sticker>() {
            @Override
            public int compare(Sticker lhs, Sticker rhs) {
                return lhs.drawOrder - rhs.drawOrder;
            }
        };
    }

    public void sendToFrontSticker(List<Sticker> stickerList)
    {
        int newDrawOrder = 0;
        for(int i=0; i<stickerList.size(); ++i)
        {
            if (stickerList.get(i)!=null && newDrawOrder <=stickerList.get(i).drawOrder)
            {
                newDrawOrder = stickerList.get(i).drawOrder +1;
            }
        }
        this.drawOrder = newDrawOrder;
    }
}
