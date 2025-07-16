package com.lended.safher

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditEmergencyNumbersActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_emergency_numbers)

        sharedPreferences = getSharedPreferences("com.lended.safher", Context.MODE_PRIVATE)

        val number1 = findViewById<EditText>(R.id.emergency_number_1)
        val number2 = findViewById<EditText>(R.id.emergency_number_2)
        val number3 = findViewById<EditText>(R.id.emergency_number_3)
        val number4 = findViewById<EditText>(R.id.emergency_number_4)
        val number5 = findViewById<EditText>(R.id.emergency_number_5)
        val saveButton = findViewById<Button>(R.id.save_button)

        loadEmergencyNumbers(number1, number2, number3, number4, number5)

        saveButton.setOnClickListener {
            val contactList = listOf(
                number1.text.toString(),
                number2.text.toString(),
                number3.text.toString(),
                number4.text.toString(),
                number5.text.toString()
            )
            val editor = sharedPreferences.edit()
            for (i in contactList.indices) {
                editor.putString("emergency_contact_${i + 1}", contactList[i])
            }
            editor.apply()

            Log.d("EmergencyContactsActivity", "Contacts saved: $contactList")
            Toast.makeText(this, "Emergency contacts updated", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun loadEmergencyNumbers(vararg editTexts: EditText) {
        (1..5).forEachIndexed { index, _ ->
            val number = sharedPreferences.getString("emergency_contact_${index + 1}", "")
            editTexts[index].setText(number)
        }
    }

    private fun saveEmergencyNumbers(vararg numbers: String) {
        val editor = sharedPreferences.edit()
        numbers.forEachIndexed { index, number ->
            editor.putString("emergency_contact_${index + 1}", number)
        }
        editor.apply()
        finish()  // Close activity and return to the previous screen
    }
}
