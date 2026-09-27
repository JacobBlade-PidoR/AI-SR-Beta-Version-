package com.jacob.aisrlite;
import android.graphics.Bitmap;
public final class ResultStore {
    private static Bitmap latest;
    public static synchronized void set(Bitmap b){ if(latest!=null) latest.recycle(); latest=b; }
    public static synchronized Bitmap get(){ return latest; }
    public static synchronized void clear(){ if(latest!=null) latest.recycle(); latest=null; }
}
