package com.example.cpu10924_local.memegenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CPU10924-local on 3/29/2016.
 */
public class MyView extends View {
    private Bitmap bmpImage;
    private Matrix matrix;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    private int indexClickText = -1;
    private float initX;
    private float initY;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bmpImage, 0, 0, null);
        for (int i=0; i<captionTextList.size();i++)
        {
            canvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x, captionTextList.get(i).y, captionTextList.get(i).strokepaint);
            canvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x, captionTextList.get(i).y, captionTextList.get(i).paint);

        }


    }

    public Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight)
    {
        int width = bmpImage.getWidth();
        int height = bmpImage.getHeight();
        float scaleWidth = ((float)newWidth)/width;
        float scaleHeight = ((float)newHeight)/height;
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createScaledBitmap(bmp,newWidth,newWidth,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int newWidth = MeasureSpec.getSize(widthMeasureSpec);
        int newHeight = MeasureSpec.getSize(heightMeasureSpec);
        bmpImage = getResizedBitmap(bmpImage,newWidth, newHeight);
        for(CaptionText captionText:captionTextList)
        {
            captionText.x = newWidth/4;
        }

    }

    public MyView(Context context,AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }
    private void init()
    {
        matrix = new Matrix();
    }
    public void setCanvasBitmap(Bitmap bitmap)
    {
        bmpImage = bitmap;

    }
    public void addTextCaption(CaptionText textCaption)
    {
        captionTextList.add(textCaption);
        invalidate();
    }

    public CaptionText getInitLocation(float locX,float locY)
    {
        for(int i=0;i<captionTextList.size();i++)
        {

            Rect bound = new Rect();
            captionTextList.get(i).paint.getTextBounds(captionTextList.get(i).content,0,captionTextList.get(i).content.length(),bound);
            float padding = 10;
            float left = captionTextList.get(i).x - padding;
            float top = captionTextList.get(i).y -padding;
            float right = captionTextList.get(i).x + bound.width() +padding;
            float bottom = captionTextList.get(i).y + bound.height() +padding;

            if (top <= locY && locY <=bottom)
            {
                if(left <=locX && locX <=right)
                {
                    initX = locX;
                    initY = locY;
                    indexClickText = i;
                    return captionTextList.get(i);
                }
            }

        }
        indexClickText = -1;
        return null;

    }
    public void moveObject(float newX, float newY)
    {
        if (indexClickText !=-1)
        {

            CaptionText updateCaptionText = captionTextList.get(indexClickText);
            float deltaX = newX - initX;
            float deltaY = newY - initY;
            updateCaptionText.x +=deltaX;
            updateCaptionText.y +=deltaY;
            initX = newX;
            initY = newY;
            captionTextList.set(indexClickText, updateCaptionText);

        }

    }

}
