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
import android.media.ExifInterface;
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
import java.util.Collections;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by CPU10924-local on 3/24/2016.
 */
public class DetailActivity extends Activity {
    private Bitmap bmpImage;
    private MyView MemeImageView;
    private EditText MemeEditText;
    private ImageView SaveImageButton;
    private Button AddMemeStickerBtn;
    private Button AddCaptionBtn;
    private ImageView RotateBtn;
    private Spinner FontSpinner;
    private Button ColorSpinner;
    private static final int CHOOSE_IMAGE_REQUEST = 1;
    private LinearLayout TextSetting;
    private ImageView deleteIcon;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();

                    try{
                        int angle = checkImageOrientation(imageUri.getPath());
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inJustDecodeBounds = true;
                        Bitmap bmpSticker = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmOptions);
                        int newWidth = 250;
                        int newHeight = newWidth*bmOptions.outHeight/bmOptions.outWidth;
                        bmOptions.inSampleSize = calculateInSampleSize(bmOptions,newWidth,newHeight);
                        bmOptions.inJustDecodeBounds = false;
                        bmpSticker = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmOptions);
                        addMemeSticker(bmpSticker,angle);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private int checkImageOrientation(String path)
    {
        try{
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;
            }
            return angle;

        }catch (Exception e)
        {
            return -1;
        }
    }

    private void addMemeSticker(Bitmap bitmapSticker,int angle)
    {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.setTranslate(50,100);
        matrix.setScale(1.0f,1.0f);

        bitmapSticker = Bitmap.createBitmap(bitmapSticker,0,0,bitmapSticker.getWidth(),bitmapSticker.getHeight(),matrix,false);

        Drawable drawable = new BitmapDrawable(getResources(),bitmapSticker);
        drawable.setBounds(0,0,bitmapSticker.getWidth(),bitmapSticker.getHeight());

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
                int angle = checkImageOrientation(imageUri.getPath());
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = false;
                bmpImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        imageUri), null, bmOptions);
                getMemeImageView(angle);

            }catch (Exception e)
            {
               e.printStackTrace();
            }
        }else if (imagePath !=null){
            int angle = checkImageOrientation(imagePath);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bmpImage = BitmapFactory.decodeFile(imagePath, options);
            getMemeImageView(angle);

        }else {
            Log.v("Error:", "Cannot pass parameter");
        }

        FontSpinner = (Spinner)findViewById(R.id.FontSpinner);
        List<String> list = new ArrayList<>();
        String[] fontSizeArray = getResources().getStringArray(R.array.font_size_array);
        Collections.addAll(list, fontSizeArray);
        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(getBaseContext(),android.R.layout.simple_spinner_item,list);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FontSpinner.setAdapter(fontSizeAdapter);
        TextSetting = (LinearLayout)findViewById(R.id.TextSetting);
        getDeleteButton();

        getAddCaptionBtn();
        getSticketButton();
        getSaveButton();
        getRotateButton();
    }

    private void getDeleteButton() {
        deleteIcon = (ImageView)findViewById(R.id.deleteIcon);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeImageView.DeleteObject();
                MemeImageView.invalidate();
                deleteIcon.setVisibility(View.INVISIBLE);
                TextSetting.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getRotateButton() {
        RotateBtn = (ImageView)findViewById(R.id.RotateBtn);
        RotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeImageView.rotateImage(90);
                MemeImageView.invalidate();
            }
        });

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
       SaveImageButton = (ImageView)findViewById(R.id.SaveImageButton);
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
    float newDist = 1f;
    PointF mid = new PointF();

    private void getMemeImageView(int angle) {

        MemeImageView = (MyView)findViewById(R.id.myview);
        if (angle!=0)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bmpImage = Bitmap.createBitmap(bmpImage,0,0,bmpImage.getWidth(),bmpImage.getHeight(),matrix,false);
        }
        MemeImageView.setCanvasBitmap(bmpImage);
      //  MemeImageView.rotateImage(90);

        MemeImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        captionTextClicked = MemeImageView.getInitTextLocation(event.getX(), event.getY());
                        if (captionTextClicked == null) {
                            TextSetting.setVisibility(View.INVISIBLE);

                            stickerClicked = MemeImageView.getInitStickerLocation(event.getX(), event.getY());
                            if (stickerClicked != null) {
                                deleteIcon.setVisibility(View.VISIBLE);
                            }else{
                                deleteIcon.setVisibility(View.INVISIBLE);
                            }
                        } else {

                            deleteIcon.setVisibility(View.VISIBLE);
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
                        Log.v("Old dis:",String.valueOf(oldDist));
                        if (oldDist > 2f) {
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            if (captionTextClicked != null) {
                                float x = event.getX();
                                float y = event.getY();
                                MemeImageView.moveObject(x, y);
                            } else if (stickerClicked != null) {
                                float x = event.getX();
                                float y = event.getY();
                                MemeImageView.moveObject(x, y);
                            }

                        } else if (mode == ZOOM) {
                            if(spacing(event)!=newDist)
                            {
                                newDist = spacing(event);
                                Log.v("New dis:",String.valueOf(newDist));
                                if ((Math.abs(newDist - oldDist)) > 2f) {
                                    mScaleFactor = newDist / oldDist;
                                    mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
                                    Log.v("Scale:",String.valueOf(mScaleFactor));
                                    MemeImageView.scaleSticker(mScaleFactor);
                                }
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
                MemeImageView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
}
