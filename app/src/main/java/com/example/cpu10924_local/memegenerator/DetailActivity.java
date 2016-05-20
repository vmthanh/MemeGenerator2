package com.example.cpu10924_local.memegenerator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import jp.co.cyberagent.android.gpuimage.CaptionText;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageTransformFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.Sticker;
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
    private ImageView ShareBtn;
    private Spinner FontSpinner;
    private Button ColorSpinner;
    private static final int CHOOSE_IMAGE_REQUEST = 1;
    private static final int LOAD_IMAGE_VIEW = 2;
    private static final int LOAD_STICKER_VIEW = 3;
    private LinearLayout TextSetting;
    private ImageView deleteIcon;
    /*--------------------------------------------*/
    private GPUImageTransformFilter mFilter;
    private GPUImageView gpuImageView;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    loadImageFromSource(getRealFilePath(imageUri),LOAD_STICKER_VIEW);
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
        matrixSticker.setTranslate(50,50);
        Sticker newSticker = new Sticker(300f,300f,bitmapSticker);
        gpuImageView.addSticker(newSticker);
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
               // bmpImage = bitmap;
                getMemeImageView(bitmap,angle);
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
                int newWidth, newHeight;
                if (typeLoad == LOAD_IMAGE_VIEW)
                {
                     newWidth = 600;
                     newHeight = newWidth*bmOptions.outHeight/bmOptions.outWidth;
                }else{
                     newWidth = 300;
                     newHeight = newWidth*bmOptions.outHeight/bmOptions.outWidth;
                }
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
        setContentView(R.layout.custom_gpu_view);
        Uri imageUri = getIntent().getParcelableExtra("bitmapUri");
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imageUri !=null)
        {
            loadImageFromSource(getRealFilePath(imageUri),LOAD_IMAGE_VIEW);
        }else if (imagePath !=null){
            loadImageFromSource(imagePath,LOAD_IMAGE_VIEW);
        }else {
            Log.v("Error:", "Cannot pass parameter");
        }
        loadComponentButtons();

    }

    private void loadImageFromSource(String imagePath,int typeLoad) {
        try{
            int angle = checkImageOrientation(imagePath);
            new LoadImageToView(angle,typeLoad).execute(imagePath);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadComponentButtons() {
        TextSetting = (LinearLayout)findViewById(R.id.TextSetting);
        getFontSpinner();
        getDeleteButton();
        getSticketButton();
        getAddCaptionBtn();
        getSaveButton();
        getRotateButton();
        getShareButton();
    }

    private void getShareButton()
    {
        ShareBtn = (ImageView)findViewById(R.id.ShareImageButton);
        ShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIt();
            }
        });
    }
    private void shareIt()
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/jpeg");
       sharingIntent.putExtra(Intent.EXTRA_STREAM,getImageUri(getApplicationContext(),MemeImageView.getSaveBitmap()));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Toast.makeText(getApplicationContext(),"Image is saved",Toast.LENGTH_SHORT).show();
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
                gpuImageView.deleteClckedObject();
                deleteIcon.setVisibility(View.INVISIBLE);
                TextSetting.setVisibility(View.INVISIBLE);
            }
        });
    }
    private int angle = 0;
    private void getRotateButton() {
        RotateBtn = (ImageView)findViewById(R.id.RotateBtn);
        RotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = (angle + 90)%360;
                switch (angle)
                {
                    case 90:
                        gpuImageView.setRotation(Rotation.ROTATION_90);
                        break;
                    case 180:
                        gpuImageView.setRotation(Rotation.ROTATION_180);
                        break;
                    case 270:
                        gpuImageView.setRotation(Rotation.ROTATION_270);
                        break;
                    default:
                        gpuImageView.setRotation(Rotation.NORMAL);
                        break;
                }

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
                CaptionText captionText = new CaptionText("Caption", 200, 0, paintText,strokePaint);
                gpuImageView.addCaptionText(captionText);

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

    GPUImageView.OnPictureSavedListener picSaveListener = new GPUImageView.OnPictureSavedListener() {
        @Override
        public void onPictureSaved(Uri uri) {
            Toast.makeText(getApplicationContext(),"Save imaged GPU",Toast.LENGTH_SHORT).show();
        }
    };
    private void getSaveButton() {
       SaveImageButton = (ImageView)findViewById(R.id.SaveImageButton);
        SaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = System.currentTimeMillis()+".jpg";
                try{
                 gpuImageView.saveToPictures("GPUImage", fileName, new GPUImageView.OnPictureSavedListener() {
                     @Override
                     public void onPictureSaved(Uri uri) {
                         Toast.makeText(getApplicationContext(),"Save GPUImage!",Toast.LENGTH_SHORT).show();
                     }
                 });
                }catch (Exception e)
                {

                }
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
                        gpuImageView.requestRender();
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
        MemeEditText.setText(captionTextClicked.content.toLowerCase());
        MemeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                captionTextClicked.content = s.toString().toUpperCase();
                gpuImageView.requestRender();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private CaptionText captionTextClicked;


    private void getMemeImageView(Bitmap bitmap,int angle) {
        bmpImage = bitmap;
        gpuImageView = (GPUImageView)findViewById(R.id.surfaceView);
        if (angle!=0)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bmpImage = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);
        }

        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        gpuImageView.setImage(bmpImage);
        gpuImageView.setOnTouchGPUImageView(new GPUImageView.GPUImageViewListerner() {
            @Override
            public void onCaptionTextClicked(CaptionText captionText) {
                if (captionText == null)
                {
                    TextSetting.setVisibility(View.INVISIBLE);
                    deleteIcon.setVisibility(View.INVISIBLE);
                }
                else{
                    captionTextClicked = captionText;
                    deleteIcon.setVisibility(View.VISIBLE);
                    TextSetting.setVisibility(View.VISIBLE);
                    getEditText();
                    addItemOnSpiner();
                    getColorSpinner();
                }
            }

            @Override
            public void onStickerClicked(Sticker sticker) {
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
        getCurrentItemValue();
        FontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (String.valueOf(parent.getItemAtPosition(position))) {
                    case "Small":
                        captionTextClicked.paint.setTextSize(100);
                        break;
                    case "Medium":
                        captionTextClicked.paint.setTextSize(200);
                        break;
                    case "Large":
                        captionTextClicked.paint.setTextSize(300);
                        break;
                    default:
                        break;

                }
                gpuImageView.requestRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void getCurrentItemValue() {
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
    }
}
