package jp.co.cyberagent.android.gpuimage;

import android.graphics.Paint;

/**
 * Created by CPU10924-local on 3/30/2016.
 */
public class CaptionText extends ObjectDraw{
    public String content;
    public Paint paint;
    public Paint strokePaint;
    public CaptionText(String content,float x, float y,Paint paint,Paint strokePaint)
    {
        this.content = content;
        this.x = x;
        this.y = y;
        this.drawOrder = -1;
        this.paint = paint;
        this.strokePaint = strokePaint;
    }
}
