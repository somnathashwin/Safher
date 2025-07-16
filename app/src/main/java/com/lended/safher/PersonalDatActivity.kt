package com.lended.safher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PersonalDatActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personaldet_screen)  // Use your layout file

        sharedPreferences = getSharedPreferences("com.lended.safher", Context.MODE_PRIVATE)

        // Find views
        val emergencyInput1 = findViewById<TextInputEditText>(R.id.emergencyInput1)
        val emergencyInput2 = findViewById<TextInputEditText>(R.id.emergencyInput2)
        val emergencyInput3 = findViewById<TextInputEditText>(R.id.emergencyInput3)
        val emergencyInput4 = findViewById<TextInputEditText>(R.id.emergencyInput4)
        val emergencyInput5 = findViewById<TextInputEditText>(R.id.emergencyInput5)
        val continueButton = findViewById<MaterialButton>(R.id.cntBtn)

        // Pre-fill inputs with saved values
        emergencyInput1.setText(sharedPreferences.getString("emergency_contact_1", ""))
        emergencyInput2.setText(sharedPreferences.getString("emergency_contact_2", ""))
        emergencyInput3.setText(sharedPreferences.getString("emergency_contact_3", ""))
        emergencyInput4.setText(sharedPreferences.getString("emergency_contact_4", ""))
        emergencyInput5.setText(sharedPreferences.getString("emergency_contact_5", ""))

        continueButton.setOnClickListener {
            // Save the inputs
            val editor = sharedPreferences.edit()
            editor.putString("emergency_contact_1", emergencyInput1.text.toString())
            editor.putString("emergency_contact_2", emergencyInput2.text.toString())
            editor.putString("emergency_contact_3", emergencyInput3.text.toString())
            editor.putString("emergency_contact_4", emergencyInput4.text.toString())
            editor.putString("emergency_contact_5", emergencyInput5.text.toString())
            editor.apply()  // Save asynchronously

            Toast.makeText(this, "Emergency contacts updated", Toast.LENGTH_SHORT).show()

            // Start the next activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Close current activity
        }
    }
}
