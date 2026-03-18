// com/example/adhd_monitor/focus/YouTubeBlockerService.java
package com.example.adhd_monitor;

import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityService;

public class YouTubeBlockerService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;

        String pkg = pkgCs.toString();

        // Do nothing if Focus Mode is not active
        if (!FocusState.isActive(getApplicationContext())) return;

        // Block YouTube (existing), TikTok, and Instagram
        boolean isBlockedApp =
                BlockedTargets.YT_PACKAGES.contains(pkg) ||     // YouTube variants
                        pkg.equals("com.zhiliaoapp.musically") ||        // TikTok
                        pkg.equals("com.instagram.android");             // Instagram

        if (isBlockedApp) {
            Intent i = new Intent(this, BlockOverlayActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("pkg", pkg);
            startActivity(i);
        }
    }

    @Override
    public void onInterrupt() {
        // No action needed
    }
}
