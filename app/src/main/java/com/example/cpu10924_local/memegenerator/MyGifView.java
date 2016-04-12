package com.example.cpu10924_local.memegenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * Created by CPU10924-local on 4/4/2016.
 */
public class MyGifView extends View {
   public Movie mMovie;
    public long movieStart;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    public List<Sticker> stickerList = new ArrayList<Sticker>();
    private List<Bitmap> listFrame = new ArrayList<>();
    private int indexClickText = -1;
    private int indexClickSticker = -1;
    private float initX;
    private float initY;
    public MyGifView(Context context)
    {
        super(context);
        //init();
    }
    public MyGifView(Context context,AttributeSet attrs)
    {
        super(context, attrs);
        //init();
    }
    private void init(Uri videoUri)
    {

       try {
           InputStream is = getContext().getContentResolver().openInputStream(videoUri);
           mMovie = Movie.decodeStream(is);

       }catch (Exception e)
       {

       }


    }
    public void setGiftVideo(Uri videoUri)
    {
        init(videoUri);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = android.os.SystemClock.uptimeMillis();
        if (movieStart ==0)
        {
            movieStart = now;

        }
        if (mMovie!=null)
        {
            canvas.save();
            int relTime = (int)((now - movieStart)%mMovie.duration());
            mMovie.setTime(relTime);
            float scaleX = (float)getWidth()/(float)mMovie.width();
            float scaleY = (float)getHeight()/(float)mMovie.height();
            canvas.scale(scaleX,scaleY);
            mMovie.draw(canvas, 0, 0);
            canvas.restore();

            for(int i=0;i<captionTextList.size();++i)
            {
                canvas.drawText(captionTextList.get(i).content.toUpperCase(),captionTextList.get(i).x , captionTextList.get(i).y, captionTextList.get(i).strokePaint);
                canvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x , captionTextList.get(i).y, captionTextList.get(i).paint);
            }

            for(int i=0; i<stickerList.size();++i)
            {
                canvas.save();
                canvas.setMatrix(stickerList.get(i).matrix);
                stickerList.get(i).drawable.draw(canvas);
                canvas.restore();
            }
            invalidate();
        }

    }
    public Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight)
    {

        return Bitmap.createScaledBitmap(bmp,newWidth,newHeight,false);
    }
    public void addTextCaption(CaptionText textCaption)
    {
        Rect bound = new Rect();
        textCaption.paint.getTextBounds(textCaption.content,0,textCaption.content.length(),bound);
        //Move text to center
        textCaption.x = getWidth() /2 -bound.width()/2;
        switch (captionTextList.size())
        {
            //Move text to bottom
            case 1:
                textCaption.y = getHeight();
                break;
            //Move text to middle
            case 2:
                textCaption.y = getHeight()/2 - bound.height()/2;
                break;
            //Otherwise, move up
            default:
                break;

        }
        captionTextList.add(textCaption);
        invalidate();
    }

    public CaptionText getInitTextLocation(float locX,float locY)
    {
        for(int i=0;i<captionTextList.size();i++)
        {

            Rect bound = new Rect();
            captionTextList.get(i).paint.getTextBounds(captionTextList.get(i).content,0,captionTextList.get(i).content.length(),bound);
            float padding = 20;
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
    public Sticker getInitStickerLocation(float locX, float locY)
    {
        for(int i=0; i<stickerList.size();++i)
        {
            Bitmap img = stickerList.get(i).bitmap;
            float padding = 50;
            float left = stickerList.get(i).x - padding;
            float right = stickerList.get(i).x + img.getWidth() + padding;
            float top = stickerList.get(i).y - padding;
            float bottom = stickerList.get(i).y + img.getHeight() + padding;
            if (top <= locY && locY <= bottom)
            {
                if (left  <= locX && locX <= right)
                {
                    initX = locX;
                    initY = locY;
                    indexClickSticker =  i;
                    return stickerList.get(i);
                }
            }
        }
        indexClickSticker = -1;
        return  null;
    }
    public void setSticker(Sticker sticker)
    {
        sticker.bitmap = getResizedBitmap(sticker.bitmap,300,300);
        sticker.matrix.setTranslate(sticker.x, sticker.y);
        sticker.drawable.setBounds(0, 0, sticker.bitmap.getWidth(), sticker.bitmap.getHeight());
        stickerList.add(sticker);
    }
    public void scaleSticker(float mScaleFactor,float mFocusX, float mFocusY)
    {

        if (indexClickSticker != -1)
        {
            Sticker updateSticker= stickerList.get(indexClickSticker);
            float newWidth = updateSticker.bitmap.getWidth()*mScaleFactor;
            float newHeigt = updateSticker.bitmap.getHeight()*mScaleFactor;
            if(newWidth >200 && newHeigt >200)
            {
                if (newWidth <1200 && newHeigt <1200) {
                    updateSticker.matrix.postScale(mScaleFactor, mScaleFactor, updateSticker.x, updateSticker.y);
                    updateSticker.bitmap = getResizedBitmap(updateSticker.bitmap, (int) newWidth, (int) newHeigt);
                }
            }
            stickerList.set(indexClickSticker,updateSticker);
        }
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
        if (indexClickSticker !=-1)
        {
            Sticker updateSticker = stickerList.get(indexClickSticker);
            float deltaX = newX - initX;
            float deltaY = newY - initY;

            updateSticker.x +=deltaX;
            updateSticker.y +=deltaY;
            updateSticker.matrix.postTranslate(deltaX,deltaY);
            initX = newX;
            initY = newY;
            stickerList.set(indexClickSticker, updateSticker);
        }

    }

}
