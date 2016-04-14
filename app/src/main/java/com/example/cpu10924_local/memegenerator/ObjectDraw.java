package com.example.cpu10924_local.memegenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CPU10924-local on 4/14/2016.
 */
public class ObjectDraw {
    public float x;
    public float y;
    public int drawOrder;
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

}
