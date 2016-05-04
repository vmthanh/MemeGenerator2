package com.example.cpu10924_local.memegenerator;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;

/**
 * Created by CPU10924-local on 4/4/2016.
 */
public class MyGifView extends View {
   public Movie mMovie;
    public long movieStart;
    private List<CaptionText> captionTextList = new ArrayList<CaptionText>();
    public List<ObjectDraw> objectDrawList = new ArrayList<>();
    private float initX;
    private float initY;
    private int myViewWidth;
    //private int myViewHeight;
    private int gifViewWidth;
    private int gifViewHeight;
    private Matrix imageViewMatrix;
    private Bitmap savedBitmap;
    private Canvas savedCanvas;
    private boolean isStart;
    private Uri videoUri;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      //  myViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        myViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMovie!=null)
        {
            gifViewWidth = myViewWidth;
            gifViewHeight = (gifViewWidth*mMovie.height())/mMovie.width();
            imageViewMatrix.reset();
            imageViewMatrix.setTranslate(0,0);
            savedBitmap  = Bitmap.createBitmap(gifViewWidth,gifViewHeight,Bitmap.Config.RGB_565);
            if (savedCanvas == null)
            {
                savedCanvas = new Canvas(savedBitmap);
            }else{
                savedCanvas.setBitmap(savedBitmap);
            }

        }
    }

    public interface MyGiftViewCustomListener{
        void onCaptionTextClicked(CaptionText captionText);
        void onStickerTextClicked(Sticker sticker);

    }
    private MyGiftViewCustomListener  listener;
    public MyGifView(Context context)
    {
        super(context);
    }
    public MyGifView(Context context,AttributeSet attrs)
    {
        super(context, attrs);
    }
    private void init(Uri videoUri)
    {

       try {
           InputStream is = getContext().getContentResolver().openInputStream(videoUri);
           mMovie = Movie.decodeStream(is);
           imageViewMatrix = new Matrix();
           isStart = true;

       }catch (Exception e)
       {

       }


    }
    public void setGiftVideo(Uri videoUri)
    {
        this.videoUri = videoUri;
        init(videoUri);
        invalidate();
    }

    public void setOnTouchCustomGifView(MyGiftViewCustomListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStart)
        {
            long now = android.os.SystemClock.uptimeMillis();
            if (movieStart ==0)
            {
                movieStart = now;

            }
            if (mMovie!=null && savedCanvas !=null)
            {
                savedCanvas.save();
                int relTime = (int)((now - movieStart)%mMovie.duration());
                mMovie.setTime(relTime);
                float scaleX = (float)gifViewWidth/(float)mMovie.width();
                float scaleY = ((float)gifViewHeight/(float)mMovie.height());
                savedCanvas.scale(scaleX,scaleY);
                mMovie.draw(savedCanvas,0,0);
                savedCanvas.restore();
                Collections.sort(this.objectDrawList,ObjectDraw.drawOrderComparator);
                for(int i=0; i<objectDrawList.size(); ++i)
                {
                    if (objectDrawList.get(i)instanceof CaptionText)
                    {
                        CaptionText captionText = (CaptionText)objectDrawList.get(i);
                        savedCanvas.drawText(captionText.content.toUpperCase(),captionText.x,captionText.y,captionText.strokePaint);
                        savedCanvas.drawText(captionText.content.toUpperCase(),captionText.x,captionText.y,captionText.paint);
                    }else{
                        Sticker sticker = (Sticker)objectDrawList.get(i);
                        savedCanvas.save();
                        savedCanvas.setMatrix(sticker.matrix);
                        savedCanvas.scale(sticker.mScaleFactor,sticker.mScaleFactor,sticker.bitmapWidth/2,sticker.bitmapHeigh/2);
                        sticker.drawable.draw(savedCanvas);
                        savedCanvas.restore();
                    }
                }
                canvas.drawBitmap(savedBitmap,imageViewMatrix,null);
                invalidate();
            }

        }

    }

    private class SaveGifAnimated extends AsyncTask<String,Integer, ByteArrayOutputStream> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        private int numFrame;
        private GifDecoder gifDecoder;
        public SaveGifAnimated(int numFrame,GifDecoder gifDecoder)
        {
            this.numFrame = numFrame;
            this.gifDecoder = gifDecoder;
        }
        @Override
        protected ByteArrayOutputStream doInBackground(String... params) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(bos);
            for(int k=0; k<this.numFrame;++k)
            {
                Log.v("Index frame:",String.valueOf(k));
                savedCanvas.save();
                float scaleX = (float)gifViewWidth/(float)mMovie.width();
                float scaleY = ((float)gifViewHeight/(float)mMovie.height());
                savedCanvas.scale(scaleX,scaleY);
                savedCanvas.drawBitmap(this.gifDecoder.getFrame(k),0,0,null);
                savedCanvas.restore();
                Collections.sort(objectDrawList,ObjectDraw.drawOrderComparator);
                for(int i=0; i<objectDrawList.size(); ++i)
                {
                    if (objectDrawList.get(i)instanceof CaptionText)
                    {
                        CaptionText captionText = (CaptionText)objectDrawList.get(i);
                        savedCanvas.drawText(captionText.content.toUpperCase(),captionText.x,captionText.y,captionText.strokePaint);
                        savedCanvas.drawText(captionText.content.toUpperCase(),captionText.x,captionText.y,captionText.paint);
                    }else{
                        Sticker sticker = (Sticker)objectDrawList.get(i);
                        savedCanvas.save();
                        savedCanvas.setMatrix(sticker.matrix);
                        savedCanvas.scale(sticker.mScaleFactor,sticker.mScaleFactor,sticker.bitmapWidth/2,sticker.bitmapHeigh/2);
                        sticker.drawable.draw(savedCanvas);
                        savedCanvas.restore();
                    }
                }
                    /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                    savedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));*/
                Bitmap outBmp = Bitmap.createScaledBitmap(savedBitmap,mMovie.width(),mMovie.height(),false);
                encoder.addFrame(outBmp);
                encoder.setDelay(this.gifDecoder.getDelay(k));
                publishProgress(1);
            }
            encoder.finish();
            return bos;
        }

        @Override
        protected void onPostExecute(ByteArrayOutputStream byteArrayOutputStream) {
            super.onPostExecute(byteArrayOutputStream);
            FileOutputStream outStream = null;
            try{
                //outStream = new FileOutputStream("/sdcard/test.gif");
                String filePath = videoUri.toString();
                File myFile = new File(filePath);
                String displayName = null;

                if (filePath.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContext().getContentResolver().query(videoUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (filePath.startsWith("file://")) {
                    displayName = myFile.getName();
                }

                outStream = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/meme_"+displayName);
                outStream.write(byteArrayOutputStream.toByteArray());
                outStream.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            progressDialog.dismiss();
            Toast.makeText(getContext(),"Save animated gif success in folder Pictures",Toast.LENGTH_SHORT).show();
            setWillNotDraw(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.incrementProgressBy(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setWillNotDraw(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Saving animated gif");
            progressDialog.setMessage("Saving frame in progess");
            progressDialog.setProgress(0);
            progressDialog.setMax(numFrame);
            progressDialog.show();
        }
    }

    public void saveGifVideo()
    {

        GifDecoder gifDecoder = new GifDecoder();
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(this.videoUri);
            gifDecoder.read(inputStream);
            int numFrame = gifDecoder.getFrameCount();

            if (numFrame !=0)
            {
                new SaveGifAnimated(numFrame,gifDecoder).execute();
            }

        }catch (Exception e)
        {

        }

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
                textCaption.y = gifViewHeight;
                break;
            //Move text to middle
            case 2:
                textCaption.y = gifViewHeight/2 - bound.height()/2;
                break;
            //Otherwise, move up
            default:
                break;

        }
        captionTextList.add(textCaption);
        objectDrawList.add(textCaption);
        invalidate();
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
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                if (listener!=null)
                {
                    captionTextClicked = getInitTextLocation(event.getX(),event.getY());
                    stickerClicked = getInitStickerLocation(event.getX(),event.getY());
                    if (captionTextClicked!=null && stickerClicked !=null)
                    {
                        if (captionTextClicked.drawOrder > stickerClicked.drawOrder)
                        {
                            stickerClicked = null;
                        }else{
                            captionTextClicked = null;
                        }
                    }
                    if (captionTextClicked == null)
                    {
                        listener.onCaptionTextClicked(null);
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
                if (stickerClicked !=null)
                {
                    stickerClicked.mStoreScaleFactor = stickerClicked.mScaleFactor;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist >5f)
                {
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode ==DRAG)
                {
                    float x = event.getX();
                    float y = event.getY();
                    moveObject(x,y);

                }else if (mode ==ZOOM)
                {
                    if (spacing(event)!=newDist)
                    {
                        newDist = spacing(event);
                        if ((Math.abs(newDist - oldDist)) >10f){
                            mScaleFactor = (newDist/oldDist);
                            scaleSticker(mScaleFactor);
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
    public void DeleteObject()
    {
        if (stickerClicked!=null)
        {
            objectDrawList.remove(stickerClicked);
        }
        if (captionTextClicked!=null)
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
    public void setSticker(Sticker sticker)
    {
        objectDrawList.add(sticker);
        invalidate();
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

}
