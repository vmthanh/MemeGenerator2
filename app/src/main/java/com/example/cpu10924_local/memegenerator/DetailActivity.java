package com.example.cpu10924_local.memegenerator;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
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
    private static final int LOAD_IMAGE_VIEW = 2;
    private static final int LOAD_STICKER_VIEW = 3;
    private LinearLayout TextSetting;
    private ImageView deleteIcon;
    static {
        System.loadLibrary("native");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try{
                        String realFilePath = getRealFilePath(imageUri);
                        int angle = checkImageOrientation(realFilePath);
                        new LoadImageToView(angle,LOAD_STICKER_VIEW).execute(realFilePath);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private String getRealFilePath(Uri uri)
    {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
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

        Matrix matrixSticker = new Matrix();
        matrixSticker.postRotate(angle);
        bitmapSticker = Bitmap.createBitmap(bitmapSticker,0,0,bitmapSticker.getWidth(),bitmapSticker.getHeight(),matrixSticker,false);
        matrixSticker.setTranslate(100,100);

        Drawable drawable = new BitmapDrawable(getResources(),bitmapSticker);
        drawable.setBounds(0,0,bitmapSticker.getWidth(),bitmapSticker.getHeight());

        Sticker newSticker = new Sticker(bitmapSticker,100,100,matrixSticker,drawable);

        MemeImageView.setSticker(newSticker);
    }



    private class LoadImageToView extends AsyncTask<String,Void, Bitmap>{
        ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);
        private int typeLoad;
        private int angle;
        public LoadImageToView(int angle, int typeLoad)
        {
            this.angle = angle;
            this.typeLoad = typeLoad;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (typeLoad == LOAD_IMAGE_VIEW)
            {
                bmpImage = bitmap;
                getMemeImageView(angle);
            }else{
                addMemeSticker(bitmap,angle);
            }
            progressDialog.dismiss();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
                Bitmap loadBitmap;
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                loadBitmap = BitmapFactory.decodeFile(params[0], bmOptions);
                int newWidth = 300;
                int newHeight = newWidth*bmOptions.outHeight/bmOptions.outWidth;
                bmOptions.inSampleSize = calculateInSampleSize(bmOptions,newWidth,newHeight);
                bmOptions.inJustDecodeBounds = false;
                loadBitmap = BitmapFactory.decodeFile(params[0], bmOptions);
                return loadBitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading image");
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
            progressDialog.show();
        }

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
                int angle = checkImageOrientation(getRealFilePath(imageUri));
                new LoadImageToView(angle,LOAD_IMAGE_VIEW).execute(getRealFilePath(imageUri));

            }catch (Exception e)
            {
               e.printStackTrace();
            }
        }else if (imagePath !=null){
            int angle = checkImageOrientation(imagePath);
            new LoadImageToView(angle,LOAD_IMAGE_VIEW).execute(imagePath);

        }else {
            Log.v("Error:", "Cannot pass parameter");
        }


        TextSetting = (LinearLayout)findViewById(R.id.TextSetting);
        getFontSpinner();
        getDeleteButton();
        getAddCaptionBtn();
        getSticketButton();
        getSaveButton();
        getRotateButton();
    }

    private void getFontSpinner()
    {
        FontSpinner = (Spinner)findViewById(R.id.FontSpinner);
        ArrayAdapter fontSizeAdapter = ArrayAdapter.createFromResource(DetailActivity.this,R.array.font_size_array,R.layout.my_spinner_item);
        fontSizeAdapter.setDropDownViewResource(R.layout.my_simple_spinner_dropdown_item);
        FontSpinner.setAdapter(fontSizeAdapter);
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
        MemeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if(imm.isAcceptingText()) { // verify if the soft keyboard is open
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
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

    private void getMemeImageView(int angle) {
        MemeImageView = (MyView)findViewById(R.id.myview);
        if (angle!=0)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bmpImage = Bitmap.createBitmap(bmpImage,0,0,bmpImage.getWidth(),bmpImage.getHeight(),matrix,false);
        }
        MemeImageView.setCanvasBitmap(bmpImage);
        MemeImageView.setOnTouchCustomView(new MyView.MyViewCustomListener() {
            @Override
            public void onCaptionTextClicked(CaptionText captionText) {
                captionTextClicked = captionText;
                if (captionTextClicked ==null)
                {
                    TextSetting.setVisibility(View.INVISIBLE);
                }else{
                    deleteIcon.setVisibility(View.VISIBLE);
                    TextSetting.setVisibility(View.VISIBLE);
                    getEditText();
                    addItemOnSpiner();
                    getColorSpinner();
                }
            }

            @Override
            public void onStickerTextClicked(Sticker sticker) {
                if (sticker!=null)
                {
                    deleteIcon.setVisibility(View.VISIBLE);

                }else{
                    deleteIcon.setVisibility(View.INVISIBLE);
                }
            }
        });

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
