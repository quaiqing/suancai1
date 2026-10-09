package com.example.coordline;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class LineService extends Service {
    static final String PREF = "line_settings";
    private static LineService current;
    private WindowManager windowManager;
    private LineView lineView;
    private boolean attached;

    static void refreshIfRunning() {
        if (current != null) current.refresh();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        current = this;
        createNotification();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Settings.canDrawOverlays(this)) attachOverlay();
    }

    private void createNotification() {
        String channelId = "coordline_running";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, "坐标辅助线", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, channelId)
                : new Notification.Builder(this);
        builder.setContentTitle("坐标辅助线运行中")
                .setContentText("可返回应用调整线条")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true);
        startForeground(21, builder.build());
    }

    private void attachOverlay() {
        if (attached) {
            refresh();
            return;
        }
        lineView = new LineView(this);
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.alpha = 0.8f;
        try {
            windowManager.addView(lineView, params);
            attached = true;
        } catch (Exception ignored) {
            attached = false;
        }
        refresh();
    }

    private void refresh() {
        if (lineView != null) lineView.invalidate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Settings.canDrawOverlays(this)) attachOverlay();
        refresh();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (attached && lineView != null) {
            try {
                windowManager.removeView(lineView);
            } catch (Exception ignored) {
            }
        }
        attached = false;
        lineView = null;
        if (current == this) current = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class LineView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final android.content.SharedPreferences prefs;

        LineView(Context context) {
            super(context);
            prefs = context.getSharedPreferences(PREF, MODE_PRIVATE);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!prefs.getBoolean("visible", true)) return;
            float x1 = prefs.getInt("x1", 100);
            float y1 = prefs.getInt("y1", 1600);
            float x2 = prefs.getInt("x2", 1340);
            float y2 = prefs.getInt("y2", 1600);
            float stroke = prefs.getInt("width", 4);
            int color = prefs.getInt("color", Color.RED);
            int opacity = prefs.getInt("alpha", 85);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1, stroke));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            paint.setAlpha(Math.min(255, Math.max(0, opacity) * 255 / 100));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }
}
