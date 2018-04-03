package io.dkfarms

import android.app.Activity
import android.os.Bundle

/**
 * Splash screen
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
}
