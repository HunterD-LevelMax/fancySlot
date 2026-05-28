package com.slotmachineapp.fancyslotmachine

import android.annotation.SuppressLint
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import com.slotmachineapp.fancyslotmachine.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var progressBar: ProgressBar
    private val splashDurationMs = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = binding.progressBar
        animateLoading()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDurationMs)
    }

    private fun animateLoading() {
        ValueAnimator.ofInt(0, 100).apply {
            duration = splashDurationMs
            addUpdateListener { animator ->
                progressBar.progress = animator.animatedValue as Int
            }
            start()
        }
    }
}
