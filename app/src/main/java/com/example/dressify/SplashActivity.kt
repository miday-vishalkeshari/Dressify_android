package com.example.dressify


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 1000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            // Start MainActivity after 2 seconds
            startActivity(Intent(this, MainActivity::class.java))
            finish()  // close SplashActivity
        }, SPLASH_TIME_OUT)
    }
}
