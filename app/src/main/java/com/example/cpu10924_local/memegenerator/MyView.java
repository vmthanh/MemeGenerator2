package com.example.cpu10924_local.memegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
    private Matrix imageViewMatrix;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    public List<Sticker> stickerList = new ArrayList<Sticker>();
    private int indexClickText = -1;
    private int indexClickSticker = -1;
    private float initX;
    private float initY;
    private int imageViewWidth;
    private int imageViewHeight;
    private boolean isRotate;
    private int myviewWidth;
    private int myviewHeight;
    private Bitmap saveBitmap;
    private Canvas savedCanvas;

    private Paint boundRectPaint;
    private Rect boundRect;
    private boolean isSetting = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

            savedCanvas.drawBitmap(bmpImage, 0,0, null);


        //Draw text
        for (int i=0; i<captionTextList.size();i++)
        {

            savedCanvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x, captionTextList.get(i).y, captionTextList.get(i).strokepaint);
            savedCanvas.drawText(captionTextList.get(i).content.toUpperCase(), captionTextList.get(i).x, captionTextList.get(i).y, captionTextList.get(i).paint);
            if (indexClickText !=-1 && indexClickText ==i)
            {
                float left = captionTextList.get(i).x-30;
                float top = captionTextList.get(i).y-boundRect.height();
                float right = captionTextList.get(i).x+boundRect.width()+30;
                float bottom = captionTextList.get(i).y;
                savedCanvas.drawRect(left, top, right, bottom, boundRectPaint);

            }
            Log.v("index click text:",String.valueOf(indexClickText));

        }

        //Draw sticker
        for(int i=0; i<stickerList.size();i++)
        {
            savedCanvas.save();
            savedCanvas.setMatrix(stickerList.get(i).matrix);
            stickerList.get(i).drawable.draw(savedCanvas);
            savedCanvas.restore();
        }

        canvas.drawBitmap(saveBitmap,imageViewMatrix,null);

    }

    public Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight)
    {

        return Bitmap.createScaledBitmap(bmp,newWidth,newHeight,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isSetting == false)
        {
            myviewHeight = MeasureSpec.getSize(heightMeasureSpec);
            myviewWidth = MeasureSpec.getSize(widthMeasureSpec);
            if (bmpImage.getHeight() > bmpImage.getWidth())
            {
                imageViewHeight = myviewHeight;
                imageViewWidth = (imageViewHeight*bmpImage.getWidth())/bmpImage.getHeight();
                imageViewMatrix.setTranslate((int)imageViewWidth/4,0);
            }else{
                imageViewWidth = myviewWidth;
                imageViewHeight = (imageViewWidth*bmpImage.getHeight())/bmpImage.getWidth();
                imageViewMatrix.setTranslate(0,0);
            }

            bmpImage = getResizedBitmap(bmpImage, imageViewWidth, imageViewHeight);
            saveBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.RGB_565);
            savedCanvas = new Canvas(saveBitmap);

        }


    }

    public MyView(Context context,AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    private void init()
    {
        imageViewMatrix = new Matrix();
        imageViewMatrix.setTranslate(0,0);
        boundRect = new Rect();
        boundRectPaint = new Paint();

    }
    public void setCanvasBitmap(Bitmap bitmap,int angle)
    {
        bmpImage = bitmap;
        if (angle!=0)
        {
            isRotate = true;
        }

    }

    public void rotateImage(int angle)
    {
        isSetting = true;
        isRotate = !isRotate;

        matrix = new Matrix();
        matrix.postRotate(angle);
        bmpImage = Bitmap.createBitmap(bmpImage,0,0,bmpImage.getWidth(),bmpImage.getHeight(),matrix,false);
        if (imageViewWidth > imageViewHeight)
        {
            imageViewHeight = myviewHeight;
            imageViewWidth = (imageViewHeight*bmpImage.getWidth())/bmpImage.getHeight();
            imageViewMatrix.setTranslate((int)imageViewWidth/4,0);
        }
        else if (imageViewWidth <imageViewHeight){
            imageViewWidth = myviewWidth;
            imageViewHeight = (imageViewWidth*bmpImage.getHeight())/bmpImage.getWidth();
            imageViewMatrix.setTranslate(0,0);
        }

        bmpImage = getResizedBitmap(bmpImage, imageViewWidth, imageViewHeight);
        saveBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.RGB_565);
        savedCanvas = new Canvas(saveBitmap);
    }
    public void setSticker(Sticker sticker)
    {
        isSetting = true;
        sticker.bitmap = getResizedBitmap(sticker.bitmap,300,300);
        sticker.matrix.setTranslate(sticker.x, sticker.y);
        sticker.matrix.setScale(1.0f,1.0f);
        sticker.drawable.setBounds(0, 0, sticker.bitmap.getWidth(), sticker.bitmap.getHeight());
        stickerList.add(sticker);
    }

    public void addTextCaption(CaptionText textCaption)
    {
        isSetting = true;
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


    public void DeleteObject()
    {
        if (indexClickText!= -1)
        {
            captionTextList.remove(indexClickText);
        }
        if (indexClickSticker !=-1)
        {
            stickerList.remove(indexClickSticker);
        }
    }
    public CaptionText getInitTextLocation(float locX,float locY)
    {
        float x = locX;
        float y = locY;
        if (isRotate)
        {
            x = x- imageViewWidth/4;
        }
        for(int i=0;i<captionTextList.size();i++)
        {

            Rect bound = new Rect();
            captionTextList.get(i).paint.getTextBounds(captionTextList.get(i).content,0,captionTextList.get(i).content.length(),bound);
            float padding = 80;
            float left = captionTextList.get(i).x ;
            float top = captionTextList.get(i).y -2*padding;
            float right = captionTextList.get(i).x + bound.width() +padding;
            float bottom = captionTextList.get(i).y + bound.height() +padding;

            if (top <= y && y <=bottom)
            {
                if(left <=x && x <=right)
                {
                    initX = locX;
                    initY = locY;
                    indexClickText = i;
                    captionTextList.get(i).paint.getTextBounds(captionTextList.get(i).content,0,captionTextList.get(i).content.length(),boundRect);
                    boundRectPaint.setColor(Color.GRAY);
                    boundRectPaint.setStyle(Paint.Style.FILL);
                    boundRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    boundRectPaint.setStrokeWidth(10);
                    boundRectPaint.setAlpha(127);
                    return captionTextList.get(i);
                }
            }

        }
        indexClickText = -1;
        return null;

    }

    public Sticker getInitStickerLocation(float locX, float locY)
    {
        float x = locX;
        float y = locY;
        if (isRotate)
        {
            x = x- imageViewWidth/4;
        }
        for(int i=0; i<stickerList.size();++i)
        {
            Bitmap img = stickerList.get(i).bitmap;
            float padding = 80;
            float left = stickerList.get(i).x;
            float right = stickerList.get(i).x + img.getWidth() + padding;
            float top = stickerList.get(i).y - 2*padding;
            float bottom = stickerList.get(i).y + img.getHeight() + padding;
            if (top <= y && y <= bottom)
            {
                if (left  <= x && x <= right)
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
