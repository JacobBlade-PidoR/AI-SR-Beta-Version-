package com.jacob.aisrlite;

import android.app.*;
import android.content.*;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import android.provider.Settings;

public class MainActivity extends Activity {
    private static final int REQ_CAPTURE = 7001;
    private MediaProjectionManager mgr;
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        mgr = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,28,28,28);
        root.setGravity(Gravity.CENTER_HORIZONTAL); root.setBackgroundColor(Color.rgb(9,10,18));
        TextView title = new TextView(this); title.setText("AI-SR Lite"); title.setTextSize(30); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this); info.setText("\nMediaProjection + Tiny learned SR\n2× preview • CPU prototype"); info.setTextSize(16); info.setTextColor(Color.LTGRAY); info.setGravity(Gravity.CENTER);
        Button start = new Button(this); start.setText("Start AI-SR capture");
        Button stop = new Button(this); stop.setText("Stop capture");
        Button overlay = new Button(this); overlay.setText("Allow overlay (optional)");
        status = new TextView(this); status.setText("\nStatus: idle"); status.setTextColor(Color.CYAN); status.setGravity(Gravity.CENTER);
        root.addView(title); root.addView(info); root.addView(start); root.addView(stop); root.addView(overlay); root.addView(status);
        setContentView(root);
        start.setOnClickListener(v -> requestCapture());
        stop.setOnClickListener(v -> { stopService(new Intent(this, CaptureService.class)); status.setText("\nStatus: stopped"); });
        overlay.setOnClickListener(v -> { if (Build.VERSION.SDK_INT >= 23) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))); });
    }
    private void requestCapture() { status.setText("\nRequesting screen capture permission…"); startActivityForResult(mgr.createScreenCaptureIntent(), REQ_CAPTURE); }
    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req,result,data);
        if (req != REQ_CAPTURE || result != RESULT_OK || data == null) { status.setText("\nStatus: capture permission denied"); return; }
        Intent s = new Intent(this, CaptureService.class);
        s.putExtra("resultCode", result); s.putExtra("data", data);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(s); else startService(s);
        status.setText("\nStatus: capturing + TinySR");
    }
}
