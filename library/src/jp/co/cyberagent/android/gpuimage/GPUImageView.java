/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.*;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GPUImageView extends FrameLayout {

    private GPUImageGLSurfaceView mGLSurfaceView;
    private GPUImage mGPUImage;
    private GPUImageFilter mFilter;
    public Size mForceSize = null;
    private float mRatio = 0.0f;


    public GPUImageView(Context context) {
        super(context);
        init(context, null);
    }

    public GPUImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mGLSurfaceView = new GPUImageGLSurfaceView(context, attrs);
        addView(mGLSurfaceView);
        mGPUImage = new GPUImage(getContext());
        mGPUImage.setGLSurfaceView(mGLSurfaceView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio != 0.0f) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

            int newHeight;
            int newWidth;
            if (width / mRatio < height) {
                newWidth = width;
                newHeight = Math.round(width / mRatio);
            } else {
                newHeight = height;
                newWidth = Math.round(height * mRatio);
            }

            int newWidthSpec = MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY);
            int newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, newHeightSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Retrieve the GPUImage instance used by this view.
     *
     * @return used GPUImage instance
     */
    public GPUImage getGPUImage() {
        return mGPUImage;
    }

    /**
     * Sets the background color
     *
     * @param red red color value
     * @param green green color value
     * @param blue red color value
     */
    public void setBackgroundColor(float red, float green, float blue) {
        mGPUImage.setBackgroundColor(red, green, blue);
    }

    // TODO Should be an xml attribute. But then GPUImage can not be distributed as .jar anymore.
    public void setRatio(float ratio) {
        mRatio = ratio;
        mGLSurfaceView.requestLayout();
        mGPUImage.deleteImage();
    }

    public void setGPUImageViewRatio(float ratio)
    {
        mRatio = ratio;
        mGLSurfaceView.requestLayout();
    }

    /**
     * Set the scale type of GPUImage.
     *
     * @param scaleType the new ScaleType
     */
    public void setScaleType(GPUImage.ScaleType scaleType) {
        mGPUImage.setScaleType(scaleType);
    }

    /**
     * Sets the rotation of the displayed image.
     *
     * @param rotation new rotation
     */
    public void setRotation(Rotation rotation) {
        mGPUImage.setRotation(rotation);
        requestRender();
    }

    /**
     * Set the filter to be applied on the image.
     *
     * @param filter Filter that should be applied on the image.
     */
    public void setFilter(GPUImageFilter filter) {
        mFilter = filter;
        mGPUImage.setFilter(filter);
        requestRender();
    }

    /**
     * Get the current applied filter.
     *
     * @return the current filter
     */
    public GPUImageFilter getFilter() {
        return mFilter;
    }

    /**
     * Sets the image on which the filter should be applied.
     *
     * @param bitmap the new image
     */
    public void setImage(final Bitmap bitmap) {
        mGPUImage.setImage(bitmap);
    }

    /**
     * Sets the image on which the filter should be applied from a Uri.
     *
     * @param uri the uri of the new image
     */
    public void setImage(final Uri uri) {
        mGPUImage.setImage(uri);
    }

    /**
     * Sets the image on which the filter should be applied from a File.
     *
     * @param file the file of the new image
     */
    public void setImage(final File file) {
        mGPUImage.setImage(file);
    }

    public void requestRender() {
        mGLSurfaceView.requestRender();
    }

    /**
     * Save current image with applied filter to Pictures. It will be stored on
     * the default Picture folder on the phone below the given folderName and
     * fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param folderName the folder name
     * @param fileName the file name
     * @param listener the listener
     */
    public void saveToPictures(final String folderName, final String fileName,
                               final OnPictureSavedListener listener) {
        new SaveTask(folderName, fileName, listener).execute();
    }

    /**
     * Save current image with applied filter to Pictures. It will be stored on
     * the default Picture folder on the phone below the given folderName and
     * fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param folderName the folder name
     * @param fileName   the file name
     * @param width      requested output width
     * @param height     requested output height
     * @param listener   the listener
     */
    public void saveToPictures(final String folderName, final String fileName,
                               int width, int height,
                               final OnPictureSavedListener listener) {
        new SaveTask(folderName, fileName, width, height, listener).execute();
    }

    /**
     * Retrieve current image with filter applied and given size as Bitmap.
     *
     * @param width  requested Bitmap width
     * @param height requested Bitmap height
     * @return Bitmap of picture with given size
     * @throws InterruptedException
     */
    public Bitmap capture(final int width, final int height) throws InterruptedException {
        // This method needs to run on a background thread because it will take a longer time
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Do not call this method from the UI thread!");
        }

        mForceSize = new Size(width, height);

        final Semaphore waiter = new Semaphore(0);

        // Layout with new size
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                waiter.release();
            }
        });
        post(new Runnable() {
            @Override
            public void run() {
                // Show loading
                addView(new LoadingView(getContext()));

                mGLSurfaceView.requestLayout();
            }
        });
        waiter.acquire();

        // Run one render pass
        mGPUImage.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                waiter.release();
            }
        });
        requestRender();
        waiter.acquire();
        Bitmap bitmap = capture();


        mForceSize = null;
        post(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView.requestLayout();
            }
        });
        requestRender();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                // Remove loading view
                removeViewAt(1);
            }
        }, 300);

        return bitmap;
    }

    /**
     * Capture the current image with the size as it is displayed and retrieve it as Bitmap.
     * @return current output as Bitmap
     * @throws InterruptedException
     */
    public Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);

        final int width = mGLSurfaceView.getMeasuredWidth();
        final int height = mGLSurfaceView.getMeasuredHeight();

        // Take picture on OpenGL thread
        final int[] pixelMirroredArray = new int[width * height];
        mGPUImage.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                final IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
                int[] pixelArray = pixelBuffer.array();

                // Convert upside down mirror-reversed image to right-side up normal image.
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                    }
                }
                waiter.release();
            }
        });
        requestRender();
        waiter.acquire();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }

    /**
     * Pauses the GLSurfaceView.
     */
    public void onPause() {
        mGLSurfaceView.onPause();
    }

    /**
     * Resumes the GLSurfaceView.
     */
    public void onResume() {
        mGLSurfaceView.onResume();
    }


    public void addCaptionText(CaptionText captionText) {
        mGLSurfaceView.addCaptionText(captionText);

    }

    public void deleteClckedObject() {
        mGLSurfaceView.deleteClickedObject();
    }

    public void addSticker(Sticker sticker) {
        mGLSurfaceView.addSticker(sticker);
    }


    public static class Size {
        int width;
        int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public interface GPUImageViewListerner{
        void onCaptionTextClicked(CaptionText captionText);
        void onStickerClicked(Sticker sticker);
    }
    private GPUImageViewListerner listener;
    public void setOnTouchGPUImageView(GPUImageViewListerner listener)
    {
        this.listener = listener;
    }


    private class GPUImageGLSurfaceView extends GLSurfaceView {
        private int gpuImageGLSurfaceViewWidth;
        private int gpuImageGLSurfaceViewHeight;
        private List<CaptionText> captionTextList = new ArrayList<>();
        private List<Sticker> stickerList = new ArrayList<>();
        public GPUImageGLSurfaceView(Context context) {
            super(context);
        }

        public GPUImageGLSurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            gpuImageGLSurfaceViewHeight = MeasureSpec.getSize(heightMeasureSpec);
            gpuImageGLSurfaceViewWidth = MeasureSpec.getSize(widthMeasureSpec);
            if (mForceSize != null) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(mForceSize.width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(mForceSize.height, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        private float initX;
        private float initY;
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
                        captionTextClicked = getCaptionTextLocation(event.getX(),gpuImageGLSurfaceViewHeight - event.getY());
                        if (captionTextClicked !=null)
                        {
                            stickerClicked = null;
                            listener.onCaptionTextClicked(captionTextClicked);
                            listener.onStickerClicked(null);
                            captionTextClicked.sendToFrontCaption(captionTextList);
                            updateCaptionText(captionTextClicked);
                            Collections.sort(captionTextList,CaptionText.drawOrderComparatorCaption);
                        }
                        else if (captionTextClicked == null)
                        {
                            stickerClicked = getStickerLocation(event.getX(),event.getY());
                            listener.onCaptionTextClicked(null);
                            if (stickerClicked!=null)
                            {
                                listener.onStickerClicked(stickerClicked);
                                stickerClicked.sendToFrontSticker(stickerList);
                                updateSticker(stickerClicked);
                                Collections.sort(stickerList,Sticker.drawOrderComparatorSticker);
                            }else{
                                listener.onStickerClicked(null);
                            }
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
                        stickerClicked.setmStoreScaleFactor(stickerClicked.getmScaleFactor());

                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 5f)
                    {
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (mode == DRAG)
                    {
                        float x = event.getX();
                        float y = event.getY();
                        moveObject(x,y);
                    }else if (mode == ZOOM)
                    {
                        float temp = spacing(event);
                        if(temp!=newDist)
                        {
                            newDist =temp;
                            if (Math.abs(newDist - oldDist)>10f)
                            {
                                mScaleFactor = (newDist/oldDist);
                                scaleSticker(mScaleFactor);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            return true;
        }

        private void scaleSticker(float mScaleFactor) {
            if (stickerClicked !=null)
            {
                float focusX = stickerClicked.getX() + stickerClicked.getCanvasWidth() /2;
                float focusY = stickerClicked.getY() + stickerClicked.getCanvasHeight() /2;

                stickerClicked.setmScaleFactor(mScaleFactor);
                if (stickerClicked.getmStoreScaleFactor() != 1f)
                    stickerClicked.setmScaleFactor(stickerClicked.getmScaleFactor() * stickerClicked.getmStoreScaleFactor());
                stickerClicked.setmScaleFactor(Math.max(0.5f, Math.min(stickerClicked.getmScaleFactor(), 3.0f)));

                stickerClicked.setCanvasHeight(stickerClicked.getmScaleFactor() * stickerClicked.getBitmapHeigh());
                stickerClicked.setCanvasWidth(stickerClicked.getmScaleFactor() * stickerClicked.getBitmapWidth());

                stickerClicked.setX(focusX - stickerClicked.getCanvasWidth() /2);
                stickerClicked.setY(focusY - stickerClicked.getCanvasHeight() /2);

                int indexStickerClicked = stickerList.indexOf(stickerClicked);
                updateSticker(stickerClicked);
            }
        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            double res= (double)(x*x) + (double)(y*y);
            return (float)Math.sqrt(res);
        }


        private void
        moveObject(float newX, float newY) {
            if (captionTextClicked != null)
            {
                newY = gpuImageGLSurfaceViewHeight -newY;
                float deltaX = newX - initX;
                float deltaY = newY - initY;
                captionTextClicked.setX(captionTextClicked.getX() + deltaX);
                captionTextClicked.setY(captionTextClicked.getY() + deltaY);
                initX = newX;
                initY = newY;
                updateCaptionText(captionTextClicked);
            }
            if (stickerClicked !=null)
            {
                float deltaX = newX - initX;
                float deltaY = newY - initY;
                int indexStickerClicked = stickerList.indexOf(stickerClicked);
                stickerClicked.setX(stickerClicked.getX() + deltaX);
                stickerClicked.setY(stickerClicked.getY() + deltaY);
                initX = newX;
                initY = newY;
                updateSticker(stickerClicked);
            }
        }

        private void updateSticker(Sticker stickerClicked) {
            mGPUImage.updateSticker(stickerClicked);
        }

        private void updateCaptionText(CaptionText captionTextClicked) {
            mGPUImage.updateCaptionText(captionTextClicked);
        }

        private Sticker getStickerLocation(float locX, float locY) {
            for(int i=stickerList.size()-1;i>=0; --i)
            {
                Sticker sticker = stickerList.get(i);
                float padding = 80;
                float left = sticker.getX();
                float right = sticker.getX() + sticker.getCanvasWidth() +padding;
                float top = sticker.getY();
                float bottom = sticker.getY() + sticker.getCanvasHeight() +padding;
                if (top <= locY && locY <= bottom)
                {
                    if (left  <= locX && locX <= right)
                    {
                        initX = locX;
                        initY = locY;
                        return sticker;
                    }
                }
                
            }
            return null;
        }
        private CaptionText getCaptionTextLocation(float locX, float locY) {

            for(int i=captionTextList.size()-1;i>=0; i--)
            {
                CaptionText captionText = captionTextList.get(i);
                Rect bound = new Rect();
                captionText.getPaint().getTextBounds(captionText.getContent(),0, captionText.getContent().length(),bound);
                float padding = 50;
                float left = captionText.getX() -padding;
                float top = captionText.getY() - bound.height()-padding;
                float right = captionText.getX() + bound.width() +2*padding;
                float bottom = captionText.getY() + bound.height() ;

                if (top <= locY && locY <=bottom)
                {
                    if(left <=locX && locX <=right)
                    {
                        initX = locX;
                        initY = locY;
                        return captionText;
                    }
                }
            }
            return  null;
        }

        public void addCaptionText(CaptionText captionText)
        {

            Rect bound = new Rect();
            captionText.getPaint().getTextBounds(captionText.getContent(),0, captionText.getContent().length(),bound);
            //Move text to center
            captionText.setX(gpuImageGLSurfaceViewWidth /2 -bound.width()/2);
            switch (captionTextList.size())
            {
                //Move text to bottom
                case 1:
                    captionText.setY(0);
                    break;
                //Move text to middle
                case 2:
                    captionText.setY(gpuImageGLSurfaceViewHeight/2 - bound.height());
                    break;
                //Otherwise, move top
                default:
                    captionText.setY(gpuImageGLSurfaceViewHeight-bound.height()-40);
                    break;

            }
            captionTextList.add(captionText);
            mGPUImage.addCaptionText(captionText);
            Collections.sort(captionTextList,CaptionText.drawOrderComparatorCaption);
        }

        public void deleteClickedObject() {
            if (stickerClicked !=null)
            {
                stickerList.remove(stickerClicked);
                mGPUImage.deleteSticker(stickerClicked);
                requestRender();
            }
            if (captionTextClicked !=null)
            {
                captionTextList.remove(captionTextClicked);
                mGPUImage.deleteCaptionText(captionTextClicked);
                requestRender();
            }
        }

        public void addSticker(Sticker sticker) {
            stickerList.add(sticker);
            mGPUImage.addSticker(sticker);
            Collections.sort(stickerList,Sticker.drawOrderComparatorSticker);
        }
    }



    private class LoadingView extends FrameLayout {
        public LoadingView(Context context) {
            super(context);
            init();
        }

        public LoadingView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public LoadingView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            ProgressBar view = new ProgressBar(getContext());
            view.setLayoutParams(
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            addView(view);
            setBackgroundColor(Color.BLACK);
        }
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final String mFolderName;
        private final String mFileName;
        private final int mWidth;
        private final int mHeight;
        private final OnPictureSavedListener mListener;
        private final Handler mHandler;

        public SaveTask(final String folderName, final String fileName,
                        final OnPictureSavedListener listener) {
            this(folderName, fileName, 0, 0, listener);
        }

        public SaveTask(final String folderName, final String fileName, int width, int height,
                        final OnPictureSavedListener listener) {
            mFolderName = folderName;
            mFileName = fileName;
            mWidth = width;
            mHeight = height;
            mListener = listener;
            mHandler = new Handler();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                Bitmap result = mWidth != 0 ? capture(mWidth, mHeight) : capture();
                saveImage(mFolderName, mFileName, result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);
            try {
                file.getParentFile().mkdirs();
                image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(getContext(),
                        new String[]{
                                file.toString()
                        }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, final Uri uri) {
                                if (mListener != null) {
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            mListener.onPictureSaved(uri);
                                        }
                                    });
                                }
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnPictureSavedListener {
        void onPictureSaved(Uri uri);
    }
}
