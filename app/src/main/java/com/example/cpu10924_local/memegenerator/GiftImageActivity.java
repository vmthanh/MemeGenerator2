package com.example.cpu10924_local.memegenerator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

/**
 * Created by CPU10924-local on 4/4/2016.
 */
public class GiftImageActivity extends Activity {
    private MyGifView myGifView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_gif_view);
        setGifView();

        setAddCaptionText();

    }
    CaptionText captionTextClicked;
    Sticker stickerClicked;
    private float mScaleFactor = 1.f;
    static final int NONE = 100;
    static final int DRAG = 200;
    static final int ZOOM = 300;
    int mode = NONE;
    float oldDist = 1f;
    PointF mid = new PointF();
    private void setGifView()
    {
        myGifView = (MyGifView)findViewById(R.id.mygifview);
        myGifView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        captionTextClicked = myGifView.getInitTextLocation(event.getRawX(), event.getRawY());
                        if (captionTextClicked == null) {
                            //TextSetting.setVisibility(View.GONE);
                            //stickerClicked = myGifView.getInitStickerLocation(event.getRawX(), event.getRawY());
                            if (stickerClicked != null) {

                            }
                        } else {
                           /* TextSetting.setVisibility(View.VISIBLE);
                            getEditText();
                            addItemOnSpiner();
                            getColorSpinner();*/
                        }
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_UP: //first finger lifted
                    case MotionEvent.ACTION_POINTER_UP: //second finger lifted
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 1f) {
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            if (captionTextClicked != null) {
                                float x = event.getRawX();
                                float y = event.getRawY();
                                myGifView.moveObject(x, y);
                            } else if (stickerClicked != null) {
                                float x = event.getRawX();
                                float y = event.getRawY();
                                myGifView.moveObject(x, y);
                            }

                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 1f) {
                                mScaleFactor = newDist / oldDist;

                                mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
                                Log.v("Scale:", String.valueOf(mScaleFactor));
                               // myGifView.scaleSticker(mScaleFactor, mid.x, mid.y);
                            }
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
    }
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        double res= (double)(x*x) + (double)(y*y);
        return (float)Math.sqrt(res);
    }
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    private void setAddCaptionText()
    {
        Button captionTextButton = (Button)findViewById(R.id.AddCaptionOnGifBtn);
        captionTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Typeface blockFont = Typeface.createFromAsset(getAssets(), "fonts/ufonts.com_impact.ttf");
                Paint paintText = new Paint();
                paintText.setColor(Color.WHITE);
                paintText.setTextSize(100);
                paintText.setTypeface(blockFont);
                paintText.setAntiAlias(true);

                Paint strokePaint = new Paint(paintText);
                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(20);
                strokePaint.setColor(Color.BLACK);
                strokePaint.setAntiAlias(true);
                CaptionText captionText = new CaptionText("Caption", 50, 100, paintText,strokePaint);
                myGifView.addTextCaption(captionText);
            }
        });
    }

}
