package com.example.cpu10924_local.memegenerator;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //New paramters for recycle view
    private RecyclerView memeList;
    private File[] files;
    private static final int PICK_VIDEO_REQUEST  = 4;
    private static final int CHOOSE_IMAGE_REQUEST = 3;
    private static final int OPEN_CAMERA_REQUEST = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 2;
    private String pictureImagePath = "";
    private PopupWindow mpopup;


    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        CustomListAdapter customListAdapter = new CustomListAdapter(files);
        CustomListAdapter.OnItemClickListener onItemClickListener = new CustomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getApplicationContext(), "Clicked" + files[position].getName(), Toast.LENGTH_SHORT).show();
                String fileName = files[position].getPath();
                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                detailIntent.putExtra("imagePath", fileName);
                startActivity(detailIntent);
            }
        };
        customListAdapter.setOnItemClickListener(onItemClickListener);
        memeList.swapAdapter(customListAdapter, false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL);

        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL);

                }else{
                    View popUpView = getLayoutInflater().inflate(R.layout.popup, null);
                    mpopup = new PopupWindow(popUpView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    mpopup.setAnimationStyle(android.R.style.Animation_Dialog);

                    mpopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mpopup.setOutsideTouchable(true);
                    mpopup.showAtLocation(popUpView, Gravity.BOTTOM, 0, 0);

                    // Removes default background.
                    Button ChooseImageBtn = (Button) popUpView.findViewById(R.id.ChooseImageBtn);
                    ChooseImageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getChooseImage();
                        }
                    });
                    Button CameraCaptureBtn = (Button) popUpView.findViewById(R.id.CameraCaptureBtn);
                    CameraCaptureBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openBackCamera();
                        }
                    });
                    Button LoadGifImageBtn = (Button) popUpView.findViewById(R.id.LoadGifImageBtn);
                    LoadGifImageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openGiftImage();
                        }
                    });
                    Button PopupCancle = (Button) popUpView.findViewById(R.id.PopupCancle);
                    PopupCancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mpopup.dismiss();
                        }
                    });
                }


            }
        });

        loadView();


    }

    private void loadView() {
        memeList = (RecyclerView) findViewById(R.id.meme_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        memeList.setLayoutManager(llm);
        loadData();
        CustomListAdapter customListAdapter = new CustomListAdapter(files);
        CustomListAdapter.OnItemClickListener onItemClickListener = new CustomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getApplicationContext(), "Clicked" + files[position].getName(), Toast.LENGTH_SHORT).show();
                String fileName = files[position].getPath();
                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                detailIntent.putExtra("imagePath", fileName);
                startActivity(detailIntent);
            }
        };
        customListAdapter.setOnItemClickListener(onItemClickListener);
        memeList.setAdapter(customListAdapter);
    }

    private void openGiftImage()
    {
        Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseVideoIntent.setType("image/gif");
        startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);

    }

    private void openBackCamera() {
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        pictureImagePath = storageDir.getAbsolutePath() + "/Camera/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, OPEN_CAMERA_REQUEST);
    }

    private void getChooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOOSE_IMAGE_REQUEST);
    }


    private void loadData() {
        String ExternalStorageDirectoryPath = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath();

        String targetPath = ExternalStorageDirectoryPath + "/DCIM/Camera";
        File targetDirector = new File(targetPath);
        if (targetDirector.listFiles() != null) {
            files = targetDirector.listFiles();
        }
    }

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
        switch (requestCode) {
            case CHOOSE_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {

                    Uri imageUri = data.getData();
                    Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                    detailIntent.putExtra("bitmapUri", imageUri);
                    startActivity(detailIntent);
                }
                break;
            case OPEN_CAMERA_REQUEST:
                break;
            case PICK_VIDEO_REQUEST:
                if (resultCode == RESULT_OK)
                {
                    Uri videoUri = data.getData();
                    Intent giftImageActivity = new Intent(MainActivity.this,GiftImageActivity.class);
                    giftImageActivity.putExtra("videoUri",videoUri);
                    startActivity(giftImageActivity);
                }

            default:
                break;
        }


    }

}
