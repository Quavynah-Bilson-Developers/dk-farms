package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import io.dkfarms.R

/**
 * Splash screen
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
}
