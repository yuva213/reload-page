package com.autoreload.chrome;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private TextView statusOverlay;
    private TextView statusAccessibility;
    private Button btnStartService;
    private Button btnStopService;
    private Button btnOpenAccessibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusOverlay = findViewById(R.id.statusOverlay);
        statusAccessibility = findViewById(R.id.statusAccessibility);
        btnStartService = findViewById(R.id.btnStartService);
        btnStopService = findViewById(R.id.btnStopService);
        btnOpenAccessibility = findViewById(R.id.btnOpenAccessibility);

        updateStatus();

        btnStartService.setOnClickListener(v -> {
            if (checkOverlayPermission()) {
                startFloatingService();
            } else {
                requestOverlayPermission();
            }
        });

        btnStopService.setOnClickListener(v -> stopFloatingService());

        btnOpenAccessibility.setOnClickListener(v -> openAccessibilitySettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            updateStatus();
        }
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingButtonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Floating button started!", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void stopFloatingService() {
        Intent intent = new Intent(this, FloatingButtonService.class);
        stopService(intent);
        Toast.makeText(this, "Floating button stopped", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    private void updateStatus() {
        // Update overlay permission status
        boolean hasOverlay = checkOverlayPermission();
        statusOverlay.setText("Overlay Permission: " + (hasOverlay ? "✓ Granted" : "✗ Not Granted"));
        statusOverlay.setTextColor(hasOverlay ?
            getResources().getColor(android.R.color.holo_green_dark) :
            getResources().getColor(android.R.color.holo_red_dark));

        // Update accessibility service status
        boolean hasAccessibility = ChromeAccessibilityService.isServiceEnabled();
        statusAccessibility.setText("Accessibility Service: " + (hasAccessibility ? "✓ Enabled" : "✗ Disabled"));
        statusAccessibility.setTextColor(hasAccessibility ?
            getResources().getColor(android.R.color.holo_green_dark) :
            getResources().getColor(android.R.color.holo_red_dark));
    }
}
