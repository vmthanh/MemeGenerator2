package com.example.cpu10924_local.memegenerator;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by CPU10924-local on 3/24/2016.
 */
public class DetailActivity extends Activity {
    private Bitmap bmpImage;
    private Bitmap alteredBitmap;
    private ImageView MemeImageView;
    private Canvas canvas;
    private Paint paint;
    private Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    private int _xDelta;
    private int _yDelta;
    private Spinner FontSpinnerTop;
    private Spinner FontSpinnerBottom;
    private TextView topTextView;
    private TextView bottomTextView;
    private RelativeLayout RelativeLayoutHolder;
    private EditText TopEditText;
    private EditText BottomEditText;
    private Button ColorSpinnerTop;
    private Button ColorSpinnerBottom;
    private Button SaveImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meme_detail);
        Uri imageUri = getIntent().getParcelableExtra("bitmapUri");
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imageUri !=null)
        {
            try{
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                bmpImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmOptions);
                bmOptions.inSampleSize = calculateInSampleSize(bmOptions,300,300);
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
            Log.v("Error:","Cannot pass parameter");
        }
        RelativeLayoutHolder = (RelativeLayout)findViewById(R.id.RelativeLayoutHolder);
        getAddTextView();
        getEditText();
        addItemOnSpiner();
        getColorSpinner();
        getSaveButton();
    }

    private void getSaveButton() {
       SaveImageButton = (Button)findViewById(R.id.SaveImageButton);
        SaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawTextOnCanvas();
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
        });
    }
    private void drawTextOnCanvas()
    {
        //Draw Top Text
        Paint paintText = new Paint();
        paintText.setColor(topTextView.getCurrentTextColor());
        float sizePxOfText = topTextView.getTextSize();
        //sizePxOfText *= getResources().getDisplayMetrics().density;
        paintText.setTextSize(sizePxOfText);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) topTextView.getLayoutParams();
        //delta for padding
        int delta = (int) topTextView.getTop() - layoutParams.topMargin;
        canvas.drawText(topTextView.getText().toString(), layoutParams.leftMargin, layoutParams.topMargin + delta, paintText);

        //Draw Bottom Text
        paintText.setColor(bottomTextView.getCurrentTextColor());
        sizePxOfText = bottomTextView.getTextSize();
        //sizePxOfText *= getResources().getDisplayMetrics().density;
        paintText.setTextSize(sizePxOfText);
        layoutParams = (RelativeLayout.LayoutParams) bottomTextView.getLayoutParams();
        //delta for padding
        delta = (int) bottomTextView.getTop() - layoutParams.topMargin;
        canvas.drawText(bottomTextView.getText().toString(), layoutParams.leftMargin, layoutParams.topMargin + delta, paintText);

    }
    private void drawStickerOnCanvas()
    {

    }

    private void getColorSpinner() {
        final Paint colorPickerPaint = new Paint();
        ColorSpinnerTop =(Button)findViewById(R.id.ColorSpinnerTop);
        ColorSpinnerBottom = (Button)findViewById(R.id.ColorSpinnerBottom);
        ColorSpinnerTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initialColor = 0xff000000;
                AmbilWarnaDialog dialog =new AmbilWarnaDialog(DetailActivity.this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                       topTextView.setTextColor(color);
                    }
                });
                dialog.show();
            }
        });
        ColorSpinnerBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initialColor = 0xff000000; //black color
                AmbilWarnaDialog dialog =new AmbilWarnaDialog(DetailActivity.this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                       bottomTextView.setTextColor(color);
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
        TopEditText = (EditText)findViewById(R.id.TopMemeEditText);
        BottomEditText = (EditText)findViewById(R.id.BottomMemeEditText);
        TopEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topTextView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        BottomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bottomTextView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void getAddTextView() {
        topTextView = (TextView)findViewById(R.id.TopTextView);
        bottomTextView = (TextView)findViewById(R.id.BottomTextView);
        topTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        _xDelta = X - layoutParams.leftMargin;
                        _yDelta = Y - layoutParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams1.leftMargin = X - _xDelta;
                        layoutParams1.topMargin = Y - _yDelta;
                        v.setLayoutParams(layoutParams1);
                        break;
                }
                RelativeLayoutHolder.invalidate();
                return true;
            }
        });

        bottomTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        _xDelta = X - layoutParams.leftMargin;
                        _yDelta = Y - layoutParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:

                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams1.leftMargin = X - _xDelta;
                        layoutParams1.topMargin = Y - _yDelta;
                        v.setLayoutParams(layoutParams1);
                        break;
                }
                RelativeLayoutHolder.invalidate();
                return true;
            }
        });
    }

    private void getMemeImageView() {
        MemeImageView = (ImageView)findViewById(R.id.MemeImageView);
        getLoadDrawOnBitmap();
        MemeImageView.setOnTouchListener(new View.OnTouchListener() {
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
        });

    }

    private void getLoadDrawOnBitmap() {
        alteredBitmap = Bitmap.createBitmap(bmpImage.getWidth(), bmpImage.getHeight(), bmpImage.getConfig());
        //Write bitmap to canvas
        canvas = new Canvas(alteredBitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        matrix = new Matrix();
        canvas.drawBitmap(bmpImage, matrix, paint);
        MemeImageView.setImageBitmap(alteredBitmap);
    }

    public void addItemOnSpiner() {
        FontSpinnerTop = (Spinner)findViewById(R.id.FontSpinnerTop);
        FontSpinnerBottom = (Spinner)findViewById(R.id.FontSpinnerBottom);
        List<String> list = new ArrayList<String>();
        String[] fontSizeArray = getResources().getStringArray(R.array.font_size_array);
        for (String s:
                fontSizeArray) {
            list.add(s);
        }

        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,list);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FontSpinnerTop.setAdapter(fontSizeAdapter);
        FontSpinnerBottom.setAdapter(fontSizeAdapter);
        FontSpinnerTop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (String.valueOf(parent.getItemAtPosition(position))) {
                    case "Small":
                        topTextView.setTextSize(10);
                        break;
                    case "Medium":
                        topTextView.setTextSize(30);
                        break;
                    case "Large":
                        topTextView.setTextSize(50);
                        break;
                    default:
                        topTextView.setTextSize(30);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        FontSpinnerBottom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (String.valueOf(parent.getItemAtPosition(position))) {
                    case "Small":
                        bottomTextView.setTextSize(10);
                        break;
                    case "Medium":
                        bottomTextView.setTextSize(30);
                        break;
                    case "Large":
                        bottomTextView.setTextSize(50);
                        break;
                    default:
                        bottomTextView.setTextSize(30);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
      /*  spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (topTextView != null) {
                    topTextView.setTextSize(Float.parseFloat(parent.getItemAtPosition(position).toString()));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (bottomTextView != null) {
                    bottomTextView.setTextSize(Float.parseFloat(parent.getItemAtPosition(position).toString()));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/
    }
}
