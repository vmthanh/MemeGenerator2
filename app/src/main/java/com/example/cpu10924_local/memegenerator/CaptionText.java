package com.example.cpu10924_local.memegenerator;

import android.graphics.Paint;

/**
 * Created by CPU10924-local on 3/30/2016.
 */
public class CaptionText {
    public String content;
    public float x;
    public float y;
    public Paint paint;
    public Paint strokepaint;
    public CaptionText(String content,float x, float y,Paint paint,Paint strokepaint)
    {
        this.content = content;
        this.x = x;
        this.y = y;
        this.paint = paint;
        this.strokepaint = strokepaint;
    }
}
