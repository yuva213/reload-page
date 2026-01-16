package com.autoreload.chrome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class FloatingButtonService extends Service {

    private static final String CHANNEL_ID = "AutoReloadChannel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    private Button btnStart;
    private Button btnStop;
    private TextView tvStatus;
    private View floatingDot;

    private boolean isReloading = false;
    private Handler reloadHandler;
    private Runnable reloadRunnable;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        reloadHandler = new Handler(Looper.getMainLooper());

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        createFloatingView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopReloading();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Auto Reload Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Auto page reload service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto Reload Chrome")
            .setContentText("Floating button is active")
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);
        return builder.build();
    }

    private void createFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null);

        // Set up layout parameters
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        windowManager.addView(floatingView, params);

        // Initialize views
        btnStart = floatingView.findViewById(R.id.btnStart);
        btnStop = floatingView.findViewById(R.id.btnStop);
        tvStatus = floatingView.findViewById(R.id.tvStatus);
        floatingDot = floatingView.findViewById(R.id.floatingDot);

        // Hide control panel initially
        View controlPanel = floatingView.findViewById(R.id.controlPanel);
        controlPanel.setVisibility(View.GONE);

        // Set up drag functionality
        setupDragFunctionality(controlPanel);

        // Set up click listeners
        floatingDot.setOnClickListener(v -> {
            // Toggle control panel visibility
            if (controlPanel.getVisibility() == View.VISIBLE) {
                controlPanel.setVisibility(View.GONE);
            } else {
                controlPanel.setVisibility(View.VISIBLE);
            }
        });

        btnStart.setOnClickListener(v -> startReloading());

        btnStop.setOnClickListener(v -> stopReloading());

        Button btnClose = floatingView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            stopReloading();
            stopSelf();
        });
    }

    private void setupDragFunctionality(View controlPanel) {
        floatingDot.setOnTouchListener(new View.OnTouchListener() {
            private long startTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTime = System.currentTimeMillis();
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        long clickDuration = System.currentTimeMillis() - startTime;
                        if (clickDuration < 200) {
                            // It's a click, let the OnClickListener handle it
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void startReloading() {
        if (!ChromeAccessibilityService.isServiceEnabled()) {
            tvStatus.setText("Please enable Accessibility Service!");
            tvStatus.setTextColor(Color.RED);
            return;
        }

        isReloading = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tvStatus.setText("Reloading...");
        tvStatus.setTextColor(Color.parseColor("#4CAF50"));

        ChromeAccessibilityService.setReloading(true);

        // Start reload interval (every 2.5 seconds)
        reloadRunnable = new Runnable() {
            @Override
            public void run() {
                if (isReloading) {
                    // Trigger reload through AccessibilityService
                    ChromeAccessibilityService.triggerReload();
                    reloadHandler.postDelayed(this, 2500);
                }
            }
        };
        reloadHandler.post(reloadRunnable);
    }

    private void stopReloading() {
        isReloading = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        tvStatus.setText("Stopped");
        tvStatus.setTextColor(Color.RED);

        ChromeAccessibilityService.setReloading(false);

        if (reloadHandler != null && reloadRunnable != null) {
            reloadHandler.removeCallbacks(reloadRunnable);
        }
    }
}
