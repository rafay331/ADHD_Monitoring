// com/example/adhd_monitor/focus/BlockedTargets.java
package com.example.adhd_monitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class BlockedTargets {
    // Primary YouTube package + common variants
    public static final Set<String> YT_PACKAGES = new HashSet<>(Arrays.asList(
            "com.google.android.youtube",          // YouTube
            "com.google.android.apps.youtube.kids",// YouTube Kids (optional)
            "com.google.android.apps.youtube.music",// YouTube Music (optional)
            "com.vanced.android.youtube",        // Vanced (optional)
            // TikTok
            "com.zhiliaoapp.musically",

            // Instagram
            "com.instagram.android"
    ));
    private BlockedTargets(){}
}
