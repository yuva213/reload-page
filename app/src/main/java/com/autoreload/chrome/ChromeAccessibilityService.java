package com.autoreload.chrome;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class ChromeAccessibilityService extends AccessibilityService {

    private static boolean isReloading = false;
    private static ChromeAccessibilityService instance;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences("AutoReloadPrefs", MODE_PRIVATE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Check if Chrome is active
        if (isReloading && event.getPackageName() != null) {
            String packageName = event.getPackageName().toString();
            if (packageName.contains("chrome") || packageName.contains("browser")) {
                // Chrome is active
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        // Configure service info programmatically
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);
    }

    public static boolean isServiceEnabled() {
        return instance != null;
    }

    public static void setReloading(boolean reloading) {
        isReloading = reloading;
    }

    public static void triggerReload() {
        if (instance != null) {
            instance.performReloadGesture();
        }
    }

    private void performReloadGesture() {
        // Method 1: Try to find and click the refresh button in Chrome's menu
        try {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                performThreeDotMenuReload();
                return;
            }

            // Try to find the URL bar first - Chrome specific
            String chromePackage = "com.android.chrome";
            String bravePackage = "com.brave.browser";
            String currentPackage = String.valueOf(rootNode.getPackageName());

            // Perform swipe down gesture for reload (universal refresh gesture)
            performSwipeDownRefresh();
            return;

        } catch (Exception e) {
            // Fallback to three dot menu method
            performThreeDotMenuReload();
        }
    }

    private void performSwipeDownRefresh() {
        // Perform a swipe down gesture in the middle-top of screen
        // This triggers pull-to-refresh in most modern browsers including Chrome
        Path path = new Path();

        // Get screen dimensions - use conservative values
        float startX = 540;  // Center of screen width
        float startY = 300;  // Top portion of screen (URL bar area)
        float endY = 1200;   // Swipe down significantly

        path.moveTo(startX, startY);
        path.lineTo(startX, endY);

        GestureDescription.StrokeDescription strokeDescription =
            new GestureDescription.StrokeDescription(path, 0, 300);

        GestureDescription gestureDescription = new GestureDescription.Builder()
            .addStroke(strokeDescription)
            .build();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private void performThreeDotMenuReload() {
        try {
            // First, try to simulate a content key down (F5 equivalent on some devices)
            // Or tap the menu button then reload

            Handler handler = new Handler(Looper.getMainLooper());

            // Step 1: Click the three-dot menu (top right corner)
            Path menuPath = new Path();
            menuPath.moveTo(950, 150);  // Approximate three-dot menu position
            GestureDescription.StrokeDescription menuStroke =
                new GestureDescription.StrokeDescription(menuPath, 0, 100);
            GestureDescription menuGesture = new GestureDescription.Builder()
                .addStroke(menuStroke)
                .build();

            dispatchGesture(menuGesture, null, null);

            // Step 2: Wait for menu to open, then click reload
            handler.postDelayed(() -> {
                Path reloadPath = new Path();
                reloadPath.moveTo(800, 400);  // Approximate reload option position
                GestureDescription.StrokeDescription reloadStroke =
                    new GestureDescription.StrokeDescription(reloadPath, 0, 100);
                GestureDescription reloadGesture = new GestureDescription.Builder()
                    .addStroke(reloadStroke)
                    .build();

                dispatchGesture(reloadGesture, null, null);

                // Step 3: Tap outside to close menu
                handler.postDelayed(() -> {
                    Path closePath = new Path();
                    closePath.moveTo(200, 500);
                    GestureDescription.StrokeDescription closeStroke =
                        new GestureDescription.StrokeDescription(closePath, 0, 50);
                    GestureDescription closeGesture = new GestureDescription.Builder()
                        .addStroke(closeStroke)
                        .build();

                    dispatchGesture(closeGesture, null, null);
                }, 200);
            }, 500);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAndClickRefreshButton(AccessibilityNodeInfo node) {
        if (node == null) return;

        // Recursively search for refresh button
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                CharSequence text = child.getText();
                CharSequence contentDesc = child.getContentDescription();
                String viewId = child.getViewIdResourceName();

                // Check for refresh-related identifiers
                boolean isRefreshButton = false;
                if (text != null && text.toString().toLowerCase().contains("refresh")) {
                    isRefreshButton = true;
                }
                if (contentDesc != null && contentDesc.toString().toLowerCase().contains("refresh")) {
                    isRefreshButton = true;
                }
                if (viewId != null && viewId.toLowerCase().contains("refresh")) {
                    isRefreshButton = true;
                }
                if (viewId != null && viewId.contains("reload_button")) {
                    isRefreshButton = true;
                }

                if (isRefreshButton && child.isClickable()) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                }

                findAndClickRefreshButton(child);
                if (child != node) { // Don't recycle the root
                    child.recycle();
                }
            }
        }
    }
}
