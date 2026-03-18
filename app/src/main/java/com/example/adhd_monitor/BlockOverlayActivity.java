// com/example/adhd_monitor/focus/BlockOverlayActivity.java
package com.example.adhd_monitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.adhd_monitor.R;

public class BlockOverlayActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_block_overlay);

        findViewById(R.id.btnHome).setOnClickListener(v -> goHome());
        // Immediately send to Home so the blocked app can’t be interacted with:
        goHome();
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }

    @Override public void onBackPressed() { goHome(); }
}
