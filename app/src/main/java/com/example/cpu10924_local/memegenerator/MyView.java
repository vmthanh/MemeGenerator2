package com.example.cpu10924_local.memegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CPU10924-local on 3/29/2016.
 */
public class MyView extends View {
    private Bitmap bmpImage;
    private Matrix matrix;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    public List<Sticker> stickerList = new ArrayList<Sticker>();
    private int indexClickText = -1;
    private int indexClickSticker = -1;
    private float initX;
    private float initY;
    private int imageViewWidth;
    private int imageViewHeight;
    private Bitmap saveBitmap;
    private Canvas myCanvas;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        myCanvas.drawBitmap(bmpImage, 0, 0, null);
        //Draw text
        for (int i=0; i<captionTextList.size();i++)
        {

            myCanvas.drawText(captionTextList.get(i).content.toUpperCase(),captionTextList.get(i).x , captionTextList.get(i).y, captionTextList.get(i).strokepaint);
            myCanvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x , captionTextList.get(i).y, captionTextList.get(i).paint);

        }

        //Draw sticker
        for(int i=0; i<stickerList.size();i++)
        {
            myCanvas.save();
            myCanvas.setMatrix(stickerList.get(i).matrix);
            stickerList.get(i).drawable.draw(myCanvas);
            myCanvas.restore();
        }
        canvas.drawBitmap(saveBitmap,0,0,null);

    }

    public Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight)
    {

        return Bitmap.createScaledBitmap(bmp,newWidth,newHeight,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        imageViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        imageViewHeight = (imageViewWidth*bmpImage.getHeight())/bmpImage.getWidth();
        bmpImage = getResizedBitmap(bmpImage, imageViewWidth, imageViewHeight);
        saveBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.RGB_565);
        myCanvas = new Canvas(saveBitmap);
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
    public void setSticker(Sticker sticker)
    {
        sticker.bitmap = getResizedBitmap(sticker.bitmap,300,300);
        sticker.matrix.setTranslate(sticker.x, sticker.y);
        sticker.drawable.setBounds(0, 0, sticker.bitmap.getWidth(), sticker.bitmap.getHeight());
        stickerList.add(sticker);
    }

    public void addTextCaption(CaptionText textCaption)
    {
        Rect bound = new Rect();
       textCaption.paint.getTextBounds(textCaption.content,0,textCaption.content.length(),bound);
        //Move text to center
        textCaption.x = imageViewWidth /2 -bound.width()/2;
        switch (captionTextList.size())
        {
            //Move text to bottom
            case 1:
                textCaption.y = imageViewHeight;
                break;
            //Move text to middle
            case 2:
                textCaption.y = imageViewHeight/2 - bound.height()/2;
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

    public void saveImage()
    {
        ContentValues contentValues = new ContentValues(3);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Draw image");
        Uri imageUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        try {
            OutputStream imageFileOS = getContext().getContentResolver().openOutputStream(imageUri);
            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
            Toast t = Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT);
            t.show();
        } catch (Exception e) {
            Log.v("Error: ", e.toString());
        }
    }


}
