package com.example.cpu10924_local.memegenerator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by CPU10924-local on 3/24/2016.
 */
public class DetailActivity extends Activity {
    private Bitmap bmpImage;
    private MyView MemeImageView;
    private EditText MemeEditText;
    private Button SaveImageButton;
    private Button AddMemeStickerBtn;
    private Button AddCaptionBtn;
    private Spinner FontSpinner;
    private Button ColorSpinner;
    private static final int CHOOSE_IMAGE_REQUEST = 1;
    private LinearLayout TextSetting;

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
        Sticker newSticker = new Sticker(bitmapSticker,50,100,matrix,drawable);
        MemeImageView.setSticker(newSticker);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_view);
        Uri imageUri = getIntent().getParcelableExtra("bitmapUri");
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imageUri !=null)
        {
            try{
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                bmpImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmOptions);
                bmOptions.inSampleSize = calculateInSampleSize(bmOptions,400,400);
                bmOptions.inJustDecodeBounds = false;
                bmpImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        imageUri), null, bmOptions);
                getMemeImageView();

            }catch (Exception e)
            {

            }
        }else if (imagePath !=null){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath,options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 300, 300);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bmpImage = BitmapFactory.decodeFile(imagePath, options);
            getMemeImageView();
        }else {
            Log.v("Error:", "Cannot pass parameter");
        }

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
        getAddCaptionBtn();


        getSticketButton();

        getSaveButton();
    }


    private void getAddCaptionBtn() {
        AddCaptionBtn = (Button)findViewById(R.id.AddCaptionBtn);
        AddCaptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextSetting.setVisibility(View.VISIBLE);
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
                MemeImageView.addTextCaption(captionText);
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

    private void getSaveButton() {
       SaveImageButton = (Button)findViewById(R.id.SaveImageButton);
        SaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               MemeImageView.saveImage();
            }
        });
    }

    private void getColorSpinner() {
        ColorSpinner = (Button)findViewById(R.id.ColorSpinner);
        ColorSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initialColor = 0xff000000;
                AmbilWarnaDialog dialog =new AmbilWarnaDialog(DetailActivity.this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        captionTextClicked.paint.setColor(color);
                        MemeImageView.invalidate();
                    }
                });
                dialog.show();
            }
        });


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

    private void getEditText() {
        MemeEditText = (EditText)findViewById(R.id.MemeEditText);
        MemeEditText.setText(captionTextClicked.content);
        MemeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                captionTextClicked.content = s.toString();
                MemeImageView.invalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    CaptionText captionTextClicked;
    Sticker stickerClicked;
    private float mScaleFactor = 1.f;
    static final int NONE = 100;
    static final int DRAG = 200;
    static final int ZOOM = 300;
    int mode = NONE;
    float oldDist = 1f;
    float mFocusX;
    float mFocusY;
    PointF mid = new PointF();
    protected static final int INVALID_POINTER_ID = -1;
    protected int mActivePointerId = INVALID_POINTER_ID;

    private void getMemeImageView() {
        MemeImageView = (MyView)findViewById(R.id.myview);
        MemeImageView.setCanvasBitmap(bmpImage);
        MemeImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // MemeImageView.mScaleDetector.onTouchEvent(event);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        captionTextClicked = MemeImageView.getInitTextLocation(event.getRawX(), event.getRawY());
                        if (captionTextClicked == null) {
                            TextSetting.setVisibility(View.GONE);
                            stickerClicked = MemeImageView.getInitStickerLocation(event.getRawX(), event.getRawY());
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
                                MemeImageView.moveObject(x, y);
                            } else if (stickerClicked != null) {
                                float x = event.getRawX();
                                float y = event.getRawY();
                                MemeImageView.moveObject(x, y);
                            }

                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 1f) {
                                mScaleFactor = newDist / oldDist;

                                mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
                                Log.v("Scale:",String.valueOf(mScaleFactor));
                                MemeImageView.scaleSticker(mScaleFactor, mid.x, mid.y);
                            }
                        }
                        break;
                    default:
                        break;
                }
                MemeImageView.invalidate();
                return true;
            }
        });

       /* MemeImageView.mScaleDetector = new ScaleGestureDetector(getApplicationContext(),new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mScaleFactor *= detector.getScaleFactor();
                mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
                mFocusX = detector.getFocusX();
                mFocusY = detector.getFocusY();
                MemeImageView.scaleSticker(mScaleFactor,mFocusX,mFocusY);
                return true;
            }
        });*/

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




    public void addItemOnSpiner() {
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
                        captionTextClicked.strokepaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokepaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokepaint.setStrokeWidth(20);
                        captionTextClicked.strokepaint.setColor(Color.BLACK);
                        break;
                    case "Medium":
                        captionTextClicked.paint.setTextSize(200);
                        captionTextClicked.strokepaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokepaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokepaint.setStrokeWidth(20);
                        captionTextClicked.strokepaint.setColor(Color.BLACK);
                        break;
                    case "Large":
                        captionTextClicked.paint.setTextSize(300);
                        captionTextClicked.strokepaint = new Paint(captionTextClicked.paint);
                        captionTextClicked.strokepaint.setStyle(Paint.Style.STROKE);
                        captionTextClicked.strokepaint.setStrokeWidth(20);
                        captionTextClicked.strokepaint.setColor(Color.BLACK);
                        break;
                    default:

                        break;

                }
                MemeImageView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
}
