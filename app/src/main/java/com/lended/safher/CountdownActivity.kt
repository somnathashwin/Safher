package com.lended.safher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt

class CountdownActivity : AppCompatActivity() {

    private lateinit var countdownTextView: TextView
    private lateinit var cancelSlider: SeekBar
    private lateinit var confirmButton: MaterialButton
    private var countdownTimer: Int = 10  // 10 seconds countdown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        countdownTextView = findViewById(R.id.countdownTextView)
        cancelSlider = findViewById(R.id.cancelSlider)
        confirmButton = findViewById(R.id.confirmButton)

        // Setup slider and countdown
        setupSlider()
        startCountdown()
    }

    private fun setupSlider() {
        cancelSlider.max = 100
        cancelSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    cancelEmergency()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun startCountdown() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                countdownTextView.text = "Time left: ${countdownTimer}s"
                if (countdownTimer > 0) {
                    countdownTimer--
                    handler.postDelayed(this, 1000)
                } else {
                    sendEmergencyMessage()
                }
            }
        })
    }

    private fun sendEmergencyMessage() {
        // Intent to send emergency message to MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("sendEmergency", true)
        }
        startActivity(intent)
        finish()
    }

    private fun cancelEmergency() {
        Toast.makeText(this, "Emergency canceled", Toast.LENGTH_SHORT).show()
        finish()
    }
}
