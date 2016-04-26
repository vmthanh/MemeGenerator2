package com.example.cpu10924_local.memegenerator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by CPU10924-local on 4/4/2016.
 */
public class GiftImageActivity extends Activity {
    private MyGifView myGifView;
    private LinearLayout TextSetting;
    private EditText MemeEditText;
    private Button AddMemeStickerBtn;
    private static final int CHOOSE_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_gif_view);
        Uri videoUri = getIntent().getParcelableExtra("videoUri");
        setGifView(videoUri);
        FontSpinner = (Spinner)findViewById(R.id.FontSpinner);
        List<String> list = new ArrayList<String>();
        String[] fontSizeArray = getResources().getStringArray(R.array.font_size_array);
        for (String s:
                fontSizeArray) {
            list.add(s);
        }
        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,list);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FontSpinner.setAdapter(fontSizeAdapter);
        TextSetting = (LinearLayout)findViewById(R.id.TextSetting);
        setAddCaptionText();
        getSticketButton();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try{
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inJustDecodeBounds = true;
                        Bitmap bmpSticker = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri),null,bmOptions);
                        bmOptions.inSampleSize = calculateInSampleSize(bmOptions,250,250);
                        bmOptions.inJustDecodeBounds = false;
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        bmpSticker = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri),null,bmOptions);
                        addMemeSticker(bmpSticker);
                    }catch (Exception e){

                    }
                }
                break;
            default:
                break;
        }
    }
    private void addMemeSticker(Bitmap bitmapSticker)
    {
        Matrix matrix = new Matrix();
        Drawable drawable = new BitmapDrawable(getResources(),bitmapSticker);;
        Sticker newSticker = new Sticker(bitmapSticker,100,100,matrix,drawable);
        myGifView.setSticker(newSticker);
    }
    private int calculateInSampleSize(BitmapFactory.Options options,int Width, int Height)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > Height || width > Width) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > Height
                    && (halfWidth / inSampleSize) > Width) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
    private void setGifView(Uri videoUri)
    {
        myGifView = (MyGifView)findViewById(R.id.mygifview);
        myGifView.setGiftVideo(videoUri);
        myGifView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        captionTextClicked = myGifView.getInitTextLocation(event.getRawX(), event.getRawY());
                        if (captionTextClicked == null) {
                            TextSetting.setVisibility(View.GONE);
                            stickerClicked = myGifView.getInitStickerLocation(event.getRawX(), event.getRawY());
                            if (stickerClicked != null) {

                            }
                        } else {
                            TextSetting.setVisibility(View.VISIBLE);
                            getEditText();
                            addItemOnSpiner();
                            getColorSpinner();
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
                                myGifView.scaleSticker(mScaleFactor, mid.x, mid.y);
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
    private Spinner FontSpinner;
    private void addItemOnSpiner()
    {
        switch ((int)captionTextClicked.paint.getTextSize())
        {
            case 100:
                FontSpinner.setSelection(0);
                break;
            case 200:
                FontSpinner.setSelection(1);
                break;
            case 300:
                FontSpinner.setSelection(2);
                break;
            default:
                break;
        }
        FontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (String.valueOf(parent.getItemAtPosition(position))) {
                    case "Small":
                        captionTextClicked.paint.setTextSize(100);
                        captionTextClicked.strokePaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokePaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokePaint.setStrokeWidth(20);
                        captionTextClicked.strokePaint.setColor(Color.BLACK);
                        break;
                    case "Medium":
                        captionTextClicked.paint.setTextSize(200);
                        captionTextClicked.strokePaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokePaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokePaint.setStrokeWidth(20);
                        captionTextClicked.strokePaint.setColor(Color.BLACK);
                        break;
                    case "Large":
                        captionTextClicked.paint.setTextSize(300);
                        captionTextClicked.strokePaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokePaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokePaint.setStrokeWidth(20);
                        captionTextClicked.strokePaint.setColor(Color.BLACK);
                        break;
                    default:

                        break;

                }
                myGifView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private Button ColorSpinner;
    private void getColorSpinner()
    {
        ColorSpinner = (Button)findViewById(R.id.ColorSpinner);
        ColorSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initialColor = 0xff000000;
                AmbilWarnaDialog dialog =new AmbilWarnaDialog(GiftImageActivity.this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        captionTextClicked.paint.setColor(color);
                        myGifView.invalidate();
                    }
                });
                dialog.show();
            }
        });
    }

    private void getEditText()
    {
        MemeEditText = (EditText)findViewById(R.id.MemeEditText);
        MemeEditText.setText(captionTextClicked.content);
        MemeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                captionTextClicked.content = s.toString();
                myGifView.invalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        Button captionTextButton = (Button)findViewById(R.id.AddCaptionBtn);
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
                CaptionText captionText = new CaptionText("Caption", 50, 100, paintText, strokePaint);
                myGifView.addTextCaption(captionText);
            }
        });
    }
    private void getSticketButton()
    {
        AddMemeStickerBtn = (Button)findViewById(R.id.AddMemeStickerBtn);
        AddMemeStickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHOOSE_IMAGE_REQUEST);
            }
        });
    }

}
