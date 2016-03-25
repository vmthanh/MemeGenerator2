package com.example.cpu10924_local.memegenerator;

import android.graphics.Bitmap;

/**
 * Created by CPU10924-local on 3/23/2016.
 */
public class MemeImage {
    private String Id;
    private String ImageName;
    private Bitmap BitmapImage;
    public MemeImage(){

    }
    public MemeImage(String Id, String ImageName, Bitmap BitmapImage){
        this.Id = Id;
        this.ImageName = ImageName;
        this.BitmapImage = BitmapImage;
    }
    public String getId(){
        return Id;
    }
    public void setId(String Id){
        this.Id = Id;
    }

    public String getImageName(){
        return ImageName;
    }
    public void setImageName(String ImageName){
        this.ImageName = ImageName;

    }

    public Bitmap getBitmapImage(){
        return BitmapImage;
    }
    public void setBitmapImage(Bitmap BitmapImage){
        this.BitmapImage = BitmapImage;
    }
}
