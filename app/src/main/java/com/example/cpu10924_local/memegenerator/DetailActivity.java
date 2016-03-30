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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
                }
                break;
            default:
                break;
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
        getAddCaptionBtn();

    /*    getSaveButton();
        getSticketButton();*/
    }
    CaptionText captionTextClicked;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()& MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                captionTextClicked =  MemeImageView.getInitLocation(event.getX(), event.getY());

                if (captionTextClicked ==null)
                {
                    TextSetting.setVisibility(View.GONE);
                }
                else {
                    TextSetting.setVisibility(View.VISIBLE);
                    getEditText();
                    addItemOnSpiner();
                    getColorSpinner();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (captionTextClicked !=null)
                {
                    float x =event.getX();
                    float y = event.getY();
                    MemeImageView.moveObject(x,y);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
       MemeImageView.invalidate();
        return true;
    }

    private void getAddCaptionBtn() {
        AddCaptionBtn = (Button)findViewById(R.id.AddCaptionBtn);
        AddCaptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextSetting = (LinearLayout)findViewById(R.id.TextSetting);
                TextSetting.setVisibility(View.VISIBLE);
                final Typeface blockFont = Typeface.createFromAsset(getAssets(), "fonts/ufonts.com_impact.ttf");
                Paint paintText = new Paint();
                paintText.setColor(Color.WHITE);
                paintText.setTextSize(100);
                paintText.setTypeface(blockFont);
                CaptionText captionText = new CaptionText("Caption", 50, 100, paintText);
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
      /* SaveImageButton = (Button)findViewById(R.id.SaveImageButton);
        SaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //drawStickerOnCanvas();
                //Save image
                ContentValues contentValues = new ContentValues(3);
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Draw image");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    OutputStream imageFileOS = getContentResolver().openOutputStream(imageUri);
                    alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
                    Toast t = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT);
                    t.show();
                } catch (Exception e) {
                    Log.v("Error: ", e.toString());
                }
            }
        });*/
    }

    private void drawStickerOnCanvas()
    {

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


    private void getMemeImageView() {
        MemeImageView = (MyView)findViewById(R.id.myview);
        MemeImageView.setCanvasBitmap(bmpImage);
        //getLoadDrawOnBitmap();

       /* MemeImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downx = event.getX();
                        downy = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        upx = event.getX();
                        upy = event.getY();
                        canvas.drawLine(downx, downy, upx, upy, paint);
                        MemeImageView.invalidate();
                        downx = upx;
                        downy = upy;
                        break;
                    case MotionEvent.ACTION_UP:
                        upx = event.getX();
                        upy = event.getY();
                        canvas.drawLine(downx, downy, upx, upy, paint);
                        MemeImageView.invalidate();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });*/

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
                MemeImageView.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
}
