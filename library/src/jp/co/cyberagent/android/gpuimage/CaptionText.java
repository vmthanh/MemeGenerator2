package jp.co.cyberagent.android.gpuimage;

import android.graphics.Paint;

import java.util.Comparator;
import java.util.List;

/**
 * Created by CPU10924-local on 3/30/2016.
 */
public class CaptionText extends ObjectDraw{
    private String content;
    private Paint paint;
    private Paint strokePaint;

    public CaptionText(String content,float x, float y, Paint paint)
    {
        this.content = content.toUpperCase();
        this.setX(x);
        this.setY(y);
        this.setDrawOrder(-1);
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
            if (captionTextList.get(i)!=null && newDrawOrder <= captionTextList.get(i).getDrawOrder())
            {
                newDrawOrder = captionTextList.get(i).getDrawOrder() +1;
            }
        }
        this.setDrawOrder(newDrawOrder);
    }

    static {
        drawOrderComparatorCaption = new Comparator<CaptionText>() {
            @Override
            public int compare(CaptionText lhs, CaptionText rhs) {
                return lhs.getDrawOrder() - rhs.getDrawOrder();
            }
        };
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }

    public void setStrokePaint(Paint strokePaint) {
        this.strokePaint = strokePaint;
    }
}
