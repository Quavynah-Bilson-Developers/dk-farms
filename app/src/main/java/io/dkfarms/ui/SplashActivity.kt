package io.dkfarms.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi

/**
 * Splash screen
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val api: FarmBaseApi = FarmBaseApi[this@SplashActivity]

        Handler().postDelayed({
           if (api.isLoggedIn){
               startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
               finishAfterTransition()
           }else{
               startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
               finishAfterTransition()
           }
        }, 2500)
    }
}
