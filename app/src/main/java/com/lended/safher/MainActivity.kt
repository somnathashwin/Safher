package com.lended.safher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import java.io.File
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var fallThreshold = 25.0f  // Adjust based on testing
    private var videoCapture: VideoCapture<Recorder>? = null
    private var isEmergencyProcedureRunning = false  // Prevent multiple recordings
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var emergencyContacts: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First launch check
        sharedPreferences = getSharedPreferences("com.lended.safher", Context.MODE_PRIVATE)
        if (isFirstLaunch()) {
            val intent = Intent(this, PersonalDatActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()  // Hide action bar

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.dark_white)

        // Setup location client and sensor manager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Load emergency contacts from SharedPreferences or defaults
        loadEmergencyContacts()

        val emergencyBtn = findViewById<MaterialButton>(R.id.emergencybtn)
        val heavyEmergencyBtn = findViewById<MaterialButton>(R.id.heavyemergencybutton)
        val mapView = findViewById<ImageButton>(R.id.mapView)
        val editContactsBtn = findViewById<ImageButton>(R.id.edit_emergency_numbers_button)

        editContactsBtn.setOnClickListener {
            val intent = Intent(this, EditEmergencyNumbersActivity::class.java)
            startActivity(intent)
        }

        mapView.setOnClickListener {
            val intent = Intent(this, NearbyFrndActivity::class.java)
            startActivity(intent)
        }

        emergencyBtn.setOnClickListener {
            checkPermissionsAndStart(false)
        }

        heavyEmergencyBtn.setOnClickListener {
            checkPermissionsAndStart(true)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("isFirstStart", true).also { isFirst ->
            if (isFirst) {
                sharedPreferences.edit().putBoolean("isFirstStart", false).apply()
            }
        }
    }

    private fun loadEmergencyContacts() {
        emergencyContacts = (1..5).map { index ->
            sharedPreferences.getString("emergency_contact_$index", "") ?: ""
        }.filter { it.isNotEmpty() }

        if (emergencyContacts.isEmpty()) {
            emergencyContacts = listOf("9087252504", "7010162326", "9940091908", "6789012345", "7890123456")
        }

        Log.d("MainActivity", "Loaded emergency contacts: $emergencyContacts")
    }



    private fun checkPermissionsAndStart(isHeavyEmergency: Boolean = false) {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            startEmergencyProcedure(isHeavyEmergency)
        }
    }

    private fun startEmergencyProcedure(isHeavyEmergency: Boolean = false) {
        if (isEmergencyProcedureRunning) {
            Toast.makeText(this, "Emergency procedure already running", Toast.LENGTH_SHORT).show()
            return
        }
        isEmergencyProcedureRunning = true

        startRecordingVideo(shouldUseFrontCamera())
        getLastLocation(isHeavyEmergency)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(isHeavyEmergency: Boolean) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val videoFile = File(externalCacheDir?.absolutePath, "emergency_video.mp4")
                    stopRecordingAndSendEmergency(videoFile, it, isHeavyEmergency)
                } ?: run {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun stopRecordingAndSendEmergency(videoFile: File, location: Location, isHeavyEmergency: Boolean) {
        val videoLink = uploadVideoAndGetLink(videoFile)
        sendEmergencyMessage(location, videoLink, isHeavyEmergency)
    }

    private fun sendEmergencyMessage(location: Location, videoLink: String, isHeavyEmergency: Boolean) {
        val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
        val message = "Emergency! Location: ${location.latitude}, ${location.longitude}, $googleMapsLink. Video: $videoLink"

        if (isHeavyEmergency) {
            emergencyContacts.forEach { sendSms(it, message) }
        } else {
            val emergencyNumber = sharedPreferences.getString("emergency_number", emergencyContacts[0]) ?: ""
            sendSms(emergencyNumber, message)
        }
    }

    private fun sendSms(number: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            Toast.makeText(this, "Emergency message sent to $number", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to send SMS to $number", e)
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadVideoAndGetLink(videoFile: File): String {
        // Placeholder for video upload logic, e.g., Firebase Storage or Google Drive
        return "https://example.com/emergency_video.mp4"
    }

    @SuppressLint("CheckResult")
    private fun startRecordingVideo(useFrontCamera: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))  // Select HD quality
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture)

                val videoFile = File(externalCacheDir?.absolutePath, "emergency_video.mp4")
                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                videoCapture?.output?.prepareRecording(this, outputOptions)?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                Toast.makeText(this, "Recording saved successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("MainActivity", "Recording failed: ${recordEvent.error}")
                                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to start recording", e)
                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun redirectToCountdownPage() {
        val intent = Intent(this, CountdownActivity::class.java)
        startActivity(intent)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val acceleration = sqrt(it.values[0] * it.values[0] + it.values[1] * it.values[1] + it.values[2] * it.values[2])
            if (acceleration > fallThreshold) {
                Toast.makeText(this, "Fall detected", Toast.LENGTH_SHORT).show()
                redirectToCountdownPage()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed for now
    }

    private fun shouldUseFrontCamera(): Boolean {
        return sharedPreferences.getBoolean("use_front_camera", true)
    }
}
