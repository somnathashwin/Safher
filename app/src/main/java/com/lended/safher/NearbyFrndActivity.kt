package com.lended.safher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

class NearbyFrndActivity : AppCompatActivity() {


    private val pinIds = listOf(
        R.id.pin1, R.id.pin2, R.id.pin3, R.id.pin4, R.id.pin5,
        R.id.pin6, R.id.pin7, R.id.pin8, R.id.pin9, R.id.pin10
    )

    // Specific phone number for pin1 and random numbers for the rest
    private val pinNumbers = mutableListOf<String>().apply {
        add("9087252504")  // Fixed number for pin1
        addAll(List(pinIds.size - 1) { generateRandomPhoneNumber() })  // Random numbers for the rest
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nearby_frnd)

        setContentView(R.layout.nearby_frnd)
        supportActionBar?.hide()

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.dark_white)

        // Setup the pins with click listeners
        setupPins()
    }

    private fun setupPins() {
        pinIds.forEachIndexed { index, pinId ->
            findViewById<ImageButton>(pinId).setOnClickListener {
                // Handle pin click
                sendAlertSms(index)
            }
        }
    }

    private fun generateRandomPhoneNumber(): String {
        // Generates a random 10-digit phone number
        val random = Random
        val areaCode = random.nextInt(100, 999)  // Area code (e.g., 100-999)
        val exchangeCode = random.nextInt(100, 999)  // Exchange code (e.g., 100-999)
        val subscriberNumber = random.nextInt(1000, 9999)  // Subscriber number (e.g., 1000-9999)
        return "$areaCode$exchangeCode$subscriberNumber"
    }

    private fun sendAlertSms(pinIndex: Int) {
        if (pinIndex < pinNumbers.size) {
            val phoneNumber = pinNumbers[pinIndex]
            val message = "Alert! This is a notification from SafeHer app."

            // Check for SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
            } else {
                // Send SMS
                try {
                    SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
                    Toast.makeText(this, "Alert SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Invalid pin selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you may retry sending the SMS if needed
            } else {
                Toast.makeText(this, "SMS permission required to send alert", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
