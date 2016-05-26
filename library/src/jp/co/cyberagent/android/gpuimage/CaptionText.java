package jp.co.cyberagent.android.gpuimage;

import android.graphics.Paint;

import java.util.Comparator;
import java.util.List;

/**
 * Created by CPU10924-local on 3/30/2016.
 */
public class CaptionText extends ObjectDraw{
    public String content;
    public Paint paint;
    public Paint strokePaint;

    public CaptionText(String content,float x, float y, Paint paint)
    {
        this.content = content.toUpperCase();
        this.x = x;
        this.y = y;
        this.drawOrder = -1;
        this.paint = paint;
    }
    public static Comparator<CaptionText> drawOrderComparatorCaption;

    public CaptionText(String content,float x, float y,Paint paint,Paint strokePaint)
    {
        this(content,x,y,paint);
        this.strokePaint = strokePaint;
    }

    public void sendToFrontCaption(List<CaptionText> captionTextList)
    {
        int newDrawOrder = 0;
        for(int i=0; i<captionTextList.size(); ++i)
        {
            if (captionTextList.get(i)!=null && newDrawOrder <=captionTextList.get(i).drawOrder)
            {
                newDrawOrder = captionTextList.get(i).drawOrder +1;
            }
        }
        this.drawOrder = newDrawOrder;
    }

    static {
        drawOrderComparatorCaption = new Comparator<CaptionText>() {
            @Override
            public int compare(CaptionText lhs, CaptionText rhs) {
                return lhs.drawOrder - rhs.drawOrder;
            }
        };
    }
}
