package jp.co.cyberagent.android.gpuimage;

import java.util.Comparator;
import java.util.List;

/**
 * Created by CPU10924-local on 4/14/2016.
 */
public class ObjectDraw {
    private float x;
    private float y;
    private int drawOrder;
    public static Comparator<ObjectDraw> drawOrderComparator;
    public void sendToFront(List<ObjectDraw> objectDrawArrayList)
    {
        int newDrawOrder = 0;
        for(int i=0; i<objectDrawArrayList.size(); ++i)
        {
            if (objectDrawArrayList.get(i)!=null && newDrawOrder <=objectDrawArrayList.get(i).drawOrder)
            {
                newDrawOrder = objectDrawArrayList.get(i).drawOrder +1;
            }
        }
        this.drawOrder = newDrawOrder;
    }
    static {
        drawOrderComparator = new Comparator<ObjectDraw>() {
            public int compare(ObjectDraw objectDraw1, ObjectDraw objectDraw2) {
                return objectDraw1.drawOrder - objectDraw2.drawOrder;
            }
        };
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
    }
}
