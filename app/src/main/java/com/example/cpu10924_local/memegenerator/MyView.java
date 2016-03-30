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
    private Bitmap resizedBmp;
    private Matrix matrix;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    private Paint paintText;
    private boolean isClickObject;
    private boolean isMoving;
    private int indexClickText;
    private int initX;
    private int initY;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bmpImage, 0, 0, null);
        for (CaptionText captionText:captionTextList
             ) {

            Paint strokePaint = new Paint(captionText.paint);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(10);
            strokePaint.setColor(Color.BLACK);
            canvas.drawText(captionText.content.toUpperCase(), captionText.x, captionText.y, strokePaint);
            canvas.drawText(captionText.content.toUpperCase(), captionText.x, captionText.y, captionText.paint);

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

    public CaptionText getInitLocation(float initX,float initY)
    {
        for(int i=0;i<captionTextList.size();i++)
        {

            Rect bound = new Rect();
             captionTextList.get(i).paint.getTextBounds(captionTextList.get(i).content,0,captionTextList.get(i).content.length(),bound);
            float rangeY =bound.height()+captionTextList.get(i).y;
            float rangeX = bound.width() +captionTextList.get(i).x +10;
            if (captionTextList.get(i).y <= initY && initY<=rangeY)
            {
                if(captionTextList.get(i).x<=initX && initX <= rangeX)
                {
                    indexClickText = i;
                    return captionTextList.get(i);
                }

            }

        }
        return null;

    }
    public void moveObject(float newX, float newY)
    {
        Rect bound = new Rect();
        captionTextList.get(indexClickText).paint.getTextBounds(captionTextList.get(indexClickText).content,0,captionTextList.get(indexClickText).content.length(),bound);
            CaptionText updateCaptionText = captionTextList.get(indexClickText);
                updateCaptionText.x =newX-(bound.height()/2);
                updateCaptionText.y =newY;
        captionTextList.set(indexClickText, updateCaptionText);
            //invalidate();


    }

}
