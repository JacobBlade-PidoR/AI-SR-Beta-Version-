package com.jacob.aisrlite;

import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import java.nio.*;

public class CaptureService extends Service {
    private static final int NOTIF_ID=17;
    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private HandlerThread thread;
    private Handler handler;
    private int dw, dh;
    private long last=0;

    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel ch = new NotificationChannel("aisr", "AI-SR capture", NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this,"aisr").setContentTitle("AI-SR Lite").setContentText("Screen capture + SR prototype running").setSmallIcon(android.R.drawable.ic_menu_view).build();
        startForeground(NOTIF_ID,n);
        thread = new HandlerThread("AI-SR"); thread.start(); handler = new Handler(thread.getLooper());
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        int rc=intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data=intent.getParcelableExtra("data");
        if (rc!=Activity.RESULT_OK || data==null) { stopSelf(); return START_NOT_STICKY; }
        MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection=m.getMediaProjection(rc,data);
        DisplayMetrics dm=Resources.getSystem().getDisplayMetrics();
        dw=dm.widthPixels; dh=dm.heightPixels;
        // Keep capture manageable on phones; SR operates on a 160x90 working image.
        reader=ImageReader.newInstance(dw,dh,PixelFormat.RGBA_8888,2);
        reader.setOnImageAvailableListener(r -> process(r), handler);
        display=projection.createVirtualDisplay("AI-SR-Capture",dw,dh,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
        projection.registerCallback(new MediaProjection.Callback(){ public void onStop(){ stopSelf(); } },handler);
        return START_NOT_STICKY;
    }
    private void process(ImageReader r) {
        long now=SystemClock.uptimeMillis();
        if(now-last<700){ Image im=r.acquireLatestImage(); if(im!=null) im.close(); return; }
        last=now;
        Image im=null;
        try {
            im=r.acquireLatestImage(); if(im==null) return;
            Image.Plane p=im.getPlanes()[0]; ByteBuffer buf=p.getBuffer(); int pixel=p.getPixelStride(); int row=p.getRowStride(); int pad=row-pixel*dw;
            Bitmap full=Bitmap.createBitmap(dw+pad/pixel,dh,Bitmap.Config.ARGB_8888); buf.rewind(); full.copyPixelsFromBuffer(buf);
            Bitmap cropped=Bitmap.createBitmap(full,0,0,dw,dh); full.recycle();
            Bitmap low=Bitmap.createScaledBitmap(cropped,160,90,true); cropped.recycle();
            Bitmap sr=TinySR.infer(low); low.recycle();
            Bitmap out=Bitmap.createScaledBitmap(sr,320,180,true); sr.recycle();
            // Keep result available for a future UI/overlay stage.
            ResultStore.set(out);
        } catch(Throwable t) { } finally { if(im!=null) im.close(); }
    }
    @Override public void onDestroy(){ if(display!=null)display.release(); if(reader!=null)reader.close(); if(projection!=null)projection.stop(); if(thread!=null)thread.quitSafely(); ResultStore.clear(); super.onDestroy(); }
    @Override public android.os.IBinder onBind(Intent i){ return null; }
}
