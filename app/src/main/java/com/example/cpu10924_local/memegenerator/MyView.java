package com.example.cpu10924_local.memegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by CPU10924-local on 3/29/2016.
 */
public class MyView extends View {
    private Bitmap bmpImage;
    private Matrix matrix;
    private Matrix imageViewMatrix;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    public List<ObjectDraw> objectDrawList = new ArrayList<>();
    private float initX;
    private float initY;
    private int imageViewWidth;
    private int imageViewHeight;
    private int myViewWidth;
    private int myViewHeight;
    private Bitmap saveBitmap;
    private Canvas savedCanvas;
    private final JniBitmapHolder bitmapHolder = new JniBitmapHolder();
   // private final JniBitmapHolder bitmapHolderSavedBitmap = new JniBitmapHolder();

    public interface MyViewCustomListener{
         void onCaptionTextClicked(CaptionText captionText);
         void onStickerTextClicked(Sticker sticker);
    }
    private MyViewCustomListener listener;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bmpImage!=null)
        {
            savedCanvas.drawBitmap(bmpImage, 0,0, null);

            Collections.sort(this.objectDrawList, ObjectDraw.drawOrderComparator);
            for(int i=0; i<objectDrawList.size();++i)
            {
                if (objectDrawList.get(i) instanceof CaptionText)
                {
                    CaptionText captionText = (CaptionText)objectDrawList.get(i);
                    savedCanvas.drawText(captionText.content.toUpperCase(), captionText.x, captionText.y, captionText.strokePaint);
                    savedCanvas.drawText(captionText.content.toUpperCase(), captionText.x, captionText.y, captionText.paint);

                }else{
                    Sticker sticker = (Sticker)objectDrawList.get(i);
                    savedCanvas.save();
                    savedCanvas.setMatrix(sticker.matrix);
                    savedCanvas.scale(sticker.mScaleFactor,sticker.mScaleFactor,sticker.bitmapWidth/2, sticker.bitmapHeigh/2);
                    sticker.drawable.draw(savedCanvas);
                    savedCanvas.restore();
                }
            }
            canvas.drawBitmap(saveBitmap,imageViewMatrix,null);
        }
    }

    public void setOnTouchCustomView(MyViewCustomListener listener)
    {
        this.listener = listener;
    }

    public Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight)
    {
        return Bitmap.createScaledBitmap(bmp,newWidth,newHeight,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            myViewHeight = MeasureSpec.getSize(heightMeasureSpec);
            myViewWidth = MeasureSpec.getSize(widthMeasureSpec);

    }
    private CaptionText captionTextClicked = null;
    private Sticker stickerClicked = null;
    private float mScaleFactor = 1f;
    static final int NONE = 100;
    static final int DRAG = 200;
    static final int ZOOM = 300;
    int mode = NONE;
    float oldDist = 1f;
    float newDist = 1f;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    if (listener!=null)
                    {
                        captionTextClicked = getInitTextLocation(event.getX(),event.getY());
                        stickerClicked = getInitStickerLocation(event.getX(),event.getY());
                        if (captionTextClicked !=null && stickerClicked !=null)
                        {
                            if (captionTextClicked.drawOrder > stickerClicked.drawOrder)
                            {
                                stickerClicked = null;
                            }
                            else{
                                captionTextClicked = null;
                            }
                        }
                        if (captionTextClicked == null)
                        {
                            listener.onCaptionTextClicked(null);
                            //stickerClicked = getInitStickerLocation(event.getX(),event.getY());
                            if (stickerClicked!=null)
                            {
                                listener.onStickerTextClicked(stickerClicked);
                                stickerClicked.sendToFront(objectDrawList);
                            }else{
                                listener.onStickerTextClicked(null);
                            }
                        }else{
                            listener.onCaptionTextClicked(captionTextClicked);
                            captionTextClicked.sendToFront(objectDrawList);
                        }
                    }
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    oldDist = newDist = 1f;
                    if (stickerClicked!=null)
                    {
                        stickerClicked.mStoreScaleFactor = stickerClicked.mScaleFactor;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:

                    oldDist = spacing(event);
                    Log.v("Old dis:",String.valueOf(oldDist));
                    if (oldDist > 5f) {
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                            float x = event.getX();
                            float y = event.getY();
                            this.moveObject(x, y);
                    } else if (mode == ZOOM) {
                        if(spacing(event)!=newDist)
                        {
                            newDist = spacing(event);
                            Log.v("New dis:",String.valueOf(newDist));
                            Log.v("Distance move",String.valueOf(Math.abs(newDist - oldDist)));
                            if ((Math.abs(newDist - oldDist)) > 10f) {
                                mScaleFactor = (newDist / oldDist);
                                Log.v("Scale:",String.valueOf(mScaleFactor));
                                this.scaleSticker(mScaleFactor);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        invalidate();
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        double res= (double)(x*x) + (double)(y*y);
        return (float)Math.sqrt(res);
    }

    public MyView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        imageViewMatrix = new Matrix();
        imageViewMatrix.setTranslate(0,0);


    }
    public void freeMemCanvas()
    {
        bitmapHolder.freeBitmap();
       // bitmapHolderSavedBitmap.freeBitmap();

    }

    public void setCanvasBitmap(Bitmap bitmap)
    {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        bitmapHolder.storeBitmap(bitmap);
        bitmap.recycle();
        //bmpImage = bitmap;
        if (bitmapHeight > bitmapWidth)
        {
            imageViewHeight = myViewHeight;
            imageViewWidth = (imageViewHeight*bitmapWidth)/bitmapHeight;
            int locX = myViewWidth /2-imageViewWidth/2;
            int locY = 0;
            imageViewMatrix.reset();
            imageViewMatrix.setTranslate(locX,locY);
        }else{
            imageViewWidth = myViewWidth;
            imageViewHeight = (imageViewWidth*bitmapHeight)/bitmapWidth;
            imageViewMatrix.reset();
            imageViewMatrix.setTranslate(0,0);
        }
        bitmapHolder.scaleBitmap(imageViewWidth,imageViewHeight, JniBitmapHolder.ScaleMethod.NearestNeighbour);
       // bmpImage = getResizedBitmap(bmpImage, imageViewWidth, imageViewHeight);
        bmpImage = bitmapHolder.getBitmap();
        if (saveBitmap==null)
        {
            saveBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.RGB_565);
        }
        if (savedCanvas==null)
        {
            savedCanvas = new Canvas(saveBitmap);
        }
        invalidate();

    }

    public void rotateImage(int angle)
    {
        bmpImage.recycle();
        bitmapHolder.rotateBitmapCw90();
        //matrix = new Matrix();
        //matrix.postRotate(angle);
        //bmpImage = Bitmap.createBitmap(bmpImage,0,0,bmpImage.getWidth(),bmpImage.getHeight(),matrix,false);

       if (imageViewWidth > imageViewHeight)
        {
            imageViewHeight = myViewHeight;
            imageViewWidth = (imageViewHeight*bitmapHolder.getBitmap().getWidth())/bitmapHolder.getBitmap().getHeight();
            int locX = myViewWidth /2-imageViewWidth/2;
            int locY = 0;
            imageViewMatrix.reset();
            imageViewMatrix.setTranslate(locX,locY);

        }
        else if (imageViewWidth <imageViewHeight){
            imageViewWidth = myViewWidth;
            imageViewHeight = (imageViewWidth*bitmapHolder.getBitmap().getHeight())/bitmapHolder.getBitmap().getWidth();
            imageViewMatrix.reset();
            imageViewMatrix.setTranslate(0,0);
        }
        bitmapHolder.scaleBitmap(imageViewWidth,imageViewHeight, JniBitmapHolder.ScaleMethod.NearestNeighbour);
       // bmpImage = getResizedBitmap(bmpImage, imageViewWidth, imageViewHeight);
        bmpImage = bitmapHolder.getBitmap();

       // saveBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.RGB_565);
        saveBitmap = getResizedBitmap(saveBitmap,imageViewWidth,imageViewHeight);
       // bitmapHolderSavedBitmap.scaleBitmap(imageViewWidth,imageViewHeight, JniBitmapHolder.ScaleMethod.NearestNeighbour);
       // saveBitmap = bitmapHolderSavedBitmap.getBitmap();
        savedCanvas = new Canvas(saveBitmap);
    }
    public void setSticker(Sticker sticker)
    {
        objectDrawList.add(sticker);
        invalidate();
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
        objectDrawList.add(textCaption);
        invalidate();
    }


    public void DeleteObject()
    {
        if (stickerClicked!=null)
        {
            objectDrawList.remove(stickerClicked);
        }
        if (captionTextClicked !=null)
        {
            objectDrawList.remove(captionTextClicked);
        }
    }
    public CaptionText getInitTextLocation(float locX,float locY)
    {
        for(int i=objectDrawList.size()-1;i>=0;i--)
        {
            if (objectDrawList.get(i) instanceof CaptionText)
            {
                CaptionText captionText = (CaptionText)objectDrawList.get(i);
                Rect bound = new Rect();
                captionText.paint.getTextBounds(captionText.content,0,captionText.content.length(),bound);
                float padding = 50;
                float left = captionText.x -padding;
                float top = captionText.y- bound.height()-padding;
                float right = captionText.x + bound.width() +2*padding;
                float bottom = captionText.y  ;

                if (top <= locY && locY <=bottom)
                {
                    if(left <=locX && locX <=right)
                    {
                        initX = locX;
                        initY = locY;
                        return (CaptionText)objectDrawList.get(i);
                    }
                }
            }


        }
        return null;

    }

    public Sticker getInitStickerLocation(float locX, float locY)
    {
        for(int i=objectDrawList.size()-1; i>=0;i--)
        {
            if (objectDrawList.get(i) instanceof Sticker)
            {
                Sticker sticker = (Sticker) objectDrawList.get(i);

                float padding = 80;
                float left = sticker.x-padding;
                float right = sticker.x + sticker.canvasWidth +padding;
                float top = sticker.y - padding;
                float bottom = sticker.y + sticker.canvasHeight +padding;
                if (top <= locY && locY <= bottom)
                {
                    if (left  <= locX && locX <= right)
                    {
                        initX = locX;
                        initY = locY;
                        return (Sticker) objectDrawList.get(i);
                    }
                }
            }

        }

        return  null;
    }
    public void moveObject(float newX, float newY)
    {
        if (captionTextClicked !=null)
        {
            float deltaX = newX - initX;
            float deltaY = newY - initY;
            captionTextClicked.x +=deltaX;
            captionTextClicked.y +=deltaY;
            initX = newX;
            initY = newY;
        }
        if (stickerClicked !=null)
        {
            float deltaX = newX - initX;
            float deltaY = newY - initY;
            stickerClicked.x +=deltaX;
            stickerClicked.y +=deltaY;
            stickerClicked.matrix.postTranslate(deltaX,deltaY);
            initX = newX;
            initY = newY;
        }

    }


    public void scaleSticker(float mScaleFactor)
    {

        if (stickerClicked != null)
        {
                stickerClicked.mScaleFactor = mScaleFactor;
                if (stickerClicked.mStoreScaleFactor != 1f)
                      stickerClicked.mScaleFactor *=stickerClicked.mStoreScaleFactor;
                stickerClicked.mScaleFactor = Math.max(0.3f, Math.min(stickerClicked.mScaleFactor, 3.0f));
                stickerClicked.canvasHeight =  stickerClicked.mScaleFactor*stickerClicked.bitmapHeigh;
                stickerClicked.canvasWidth =  stickerClicked.mScaleFactor*stickerClicked.bitmapWidth;

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
