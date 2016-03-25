package com.example.cpu10924_local.memegenerator;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {


    ImageView chooseImageView;
    Button choosePicture;
    Button savePicture;
    Bitmap alteredBitmap;
    Canvas canvas;
    RelativeLayout relativeLayout;
    EditText topEditText;
    EditText bottomEditText;
    Spinner spinner1;
    Spinner spinner2;
    private int _xDelta;
    private int _yDelta;

    //New paramters for recycle view
    private RecyclerView memeList;
    private  File[] files;
    private static final int CHOOSE_IMAGE_REQUEST = 3;
    private static final int OPEN_CAMERA_REQUEST = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 2;
    private String pictureImagePath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                openBackCamera();
            }
        });

       /* relativeLayout = (RelativeLayout) findViewById(R.id.myRelativeLayout);
        chooseImageView = (ImageView) findViewById(R.id.ChoosenImageView);
        getAddChoosePictureBtn();
        getAddSavePictureBtn();
        getTopEditText();
        getBottomEditText();
        addItemOnSpiner();*/

        memeList = (RecyclerView)findViewById(R.id.meme_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        memeList.setLayoutManager(llm);
        loadData();
        CustomListAdapter customListAdapter = new CustomListAdapter(files);
        CustomListAdapter.OnItemClickListener onItemClickListener = new CustomListAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getApplicationContext(),"Clicked" +files[position].getName(),Toast.LENGTH_SHORT).show();
                String fileName = files[position].getPath();
                Intent detailIntent = new Intent(MainActivity.this,DetailActivity.class);
                detailIntent.putExtra("imagePath", fileName);
                startActivity(detailIntent);
            }
        };
        customListAdapter.setOnItemClickListener(onItemClickListener);
        memeList.setAdapter(customListAdapter);

    }
    private void openBackCamera(){
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp +".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        pictureImagePath = storageDir.getAbsolutePath()+"/Camera/" +imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
        startActivityForResult(cameraIntent,OPEN_CAMERA_REQUEST);
    }



    private void loadData(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        String ExternalStorageDirectoryPath = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath();

        String targetPath = ExternalStorageDirectoryPath +"/DCIM/Camera";
        File targetDirector = new File(targetPath);
        if (targetDirector.listFiles()!=null)
        {
            files= targetDirector.listFiles();
            Log.v("Files:", String.valueOf(files.length));
        }


        /*for (File f: files){
            Log.v("Files:",f.getName());
        }*/


    }

  /*  private void getBottomEditText() {
        bottomEditText = (EditText) findViewById(R.id.BottomEditText);

        bottomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (bottomTextView == null) {
                    bottomTextView = new TextView(getApplicationContext());
                    bottomTextView.setText("Bottom Text");
                    relativeLayout.addView(bottomTextView);
                }

                bottomTextView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void getAddSavePictureBtn() {
        savePicture = (Button) findViewById(R.id.SavePictureButton);
        savePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alteredBitmap != null) {
                    //Set Style for text
                    Paint paintText = new Paint();
                    paintText.setColor(Color.BLUE);
                    float sizePxOfText = Float.parseFloat(spinner1.getSelectedItem().toString());
                    sizePxOfText *= getResources().getDisplayMetrics().density;
                    paintText.setTextSize(sizePxOfText);
                    Log.v("Font size:", spinner1.getSelectedItem().toString());
                    Rect rectText = new Rect();
                    paintText.getTextBounds(topTextView.getText().toString(), 0, topTextView.getText().length(), rectText);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) topTextView.getLayoutParams();
                    //delta for padding
                    int delta = (int) topTextView.getTop() - layoutParams.topMargin + 30;
                    canvas.drawText(topTextView.getText().toString(), layoutParams.leftMargin, layoutParams.topMargin + delta, paintText);
                    chooseImageView.invalidate();
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
            }
        });

    }


    private void getAddChoosePictureBtn() {
        choosePicture = (Button) findViewById(R.id.ChoosePictureButton);
        choosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

  */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    Intent detailIntent = new Intent(MainActivity.this,DetailActivity.class);
                    detailIntent.putExtra("bitmapUri",imageUri);
                    startActivity(detailIntent);
                }
                break;
            case OPEN_CAMERA_REQUEST:
                break;
            default:
                break;
        }


    }

}
