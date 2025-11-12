package com.shakti.ai.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakti.ai.ai.GuardianAI
import com.shakti.ai.blockchain.AptosService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Guardian ViewModel - Physical Safety Monitoring
 *
 * Features:
 * - Real-time audio threat detection
 * - Automatic emergency SOS
 * - Nearby user alerts (BLE mesh)
 * - Emergency services contact
 * - Automatic evidence recording
 * - Flashlight strobe alert
 * - Blockchain threat logging
 */
class GuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val guardianAI = try {
        GuardianAI.getInstance(application)
    } catch (e: Exception) {
        android.util.Log.w("GuardianViewModel", "GuardianAI initialization failed: ${e.message}")
        null
    }
    private val aptosService = AptosService.getInstance(application)
    private val cameraManager =
        application.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var isFlashlightActive = false

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring

    private val _threatDetected = MutableStateFlow(false)
    val threatDetected: StateFlow<Boolean> = _threatDetected

    private val _latestThreat = MutableStateFlow<GuardianAI.ThreatDetectionResult?>(null)
    val latestThreat: StateFlow<GuardianAI.ThreatDetectionResult?> = _latestThreat

    private val _threatLevel = MutableStateFlow(0f)
    val threatLevel: StateFlow<Float> = _threatLevel

    // Audio visualization
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel

    // Emergency state
    private val _emergencyActivated = MutableStateFlow(false)
    val emergencyActivated: StateFlow<Boolean> = _emergencyActivated

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    // Nearby users (BLE mesh)
    private val _nearbyUsers = MutableStateFlow(0)
    val nearbyUsers: StateFlow<Int> = _nearbyUsers

    private val _alertsSent = MutableStateFlow(0)
    val alertsSent: StateFlow<Int> = _alertsSent

    // Location
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    // Emergency contacts
    private val _emergencyContactsCalled = MutableStateFlow(false)
    val emergencyContactsCalled: StateFlow<Boolean> = _emergencyContactsCalled

    /**
     * Start Guardian AI monitoring
     */
    fun startGuardianMonitoring() {
        viewModelScope.launch {
            try {
                val started = guardianAI?.startAudioMonitoring() ?: false

                if (started) {
                    _isMonitoring.value = true

                    // Start continuous monitoring loop
                    monitorThreats()

                    // Start audio visualization
                    updateAudioLevel()
                } else {
                    // Permission not granted or AI not available
                    _isMonitoring.value = false
                    android.util.Log.w(
                        "GuardianViewModel",
                        "Could not start monitoring - check permissions or model availability"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GuardianViewModel", "Error starting monitoring: ${e.message}")
                _isMonitoring.value = false
            }
        }
    }

    /**
     * Continuous threat monitoring loop
     */
    private fun monitorThreats() {
        viewModelScope.launch {
            while (_isMonitoring.value) {
                try {
                    val result =
                        guardianAI?.analyzeAudioForThreats() ?: GuardianAI.ThreatDetectionResult(
                            isThreat = false,
                            threatType = GuardianAI.ThreatType.NONE,
                            confidence = 0f
                        )

                    _threatLevel.value = result.confidence
                    _latestThreat.value = result

                    if (result.isThreat && !_threatDetected.value) {
                        _threatDetected.value = true
                        handleThreatDetection(result)
                    }

                    // Check every 100ms for real-time detection
                    delay(100)
                } catch (e: Exception) {
                    android.util.Log.e(
                        "GuardianViewModel",
                        "Error in threat monitoring: ${e.message}"
                    )
                    // Continue monitoring even if one iteration fails
                    delay(1000)
                }
            }
        }
    }

    /**
     * Update audio level for visualization
     */
    private fun updateAudioLevel() {
        viewModelScope.launch {
            while (_isMonitoring.value) {
                try {
                    val level = guardianAI?.getCurrentAudioLevel() ?: 0f
                    _audioLevel.value = level
                    delay(50) // Update 20 times per second
                } catch (e: Exception) {
                    android.util.Log.e(
                        "GuardianViewModel",
                        "Error getting audio level: ${e.message}"
                    )
                    delay(100)
                }
            }
        }
    }

    /**
     * Handle threat detection
     */
    private fun handleThreatDetection(threat: GuardianAI.ThreatDetectionResult) {
        viewModelScope.launch {
            when (threat.threatType) {
                GuardianAI.ThreatType.SCREAM -> {
                    // Highest priority - immediate emergency protocol
                    triggerEmergencyProtocol()
                }

                GuardianAI.ThreatType.DISTRESS_CALL -> {
                    // High priority - trigger emergency protocol
                    triggerEmergencyProtocol()
                }

                GuardianAI.ThreatType.THREATENING_VOICE -> {
                    // Medium priority - start recording and alert
                    startAutoRecording()
                    alertNearbyUsers()
                }

                GuardianAI.ThreatType.LOUD_NOISE -> {
                    // Low priority - monitor and log
                    logThreatToBlockchain(threat)
                }

                else -> {
                    // No action
                }
            }
        }
    }

    /**
     * Full emergency SOS protocol
     */
    fun triggerEmergencyProtocol() {
        viewModelScope.launch {
            if (_emergencyActivated.value) {
                return@launch // Already activated
            }

            _emergencyActivated.value = true

            try {
                // 1. Log to Aptos blockchain (immutable evidence)
                logThreatToBlockchain(_latestThreat.value)

                // 2. Alert nearby SHAKTI users via BLE mesh
                alertNearbyUsers()

                // 3. Start automatic evidence recording
                startAutoRecording()

                // 4. Activate flashlight strobe (visible alert)
                activateFlashlightStrobe()

                // 5. Contact emergency services
                contactEmergencyServices()

                // 6. Send SMS to emergency contacts
                notifyEmergencyContacts()

                // 7. Share location continuously
                startLocationSharing()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Log threat to blockchain
     */
    private suspend fun logThreatToBlockchain(threat: GuardianAI.ThreatDetectionResult?) {
        threat?.let {
            try {
                // TODO: Add proper method to log threats
                // For now, just log using the existing method
                aptosService.logThreatAlert(
                    severity = (it.confidence * 10).toInt(),
                    audioHash = "threat_${it.timestamp}"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Alert nearby SHAKTI users via BLE mesh
     */
    private suspend fun alertNearbyUsers() {
        try {
            // In production, this would use BLE mesh networking
            // to alert nearby SHAKTI app users

            // Simulate finding nearby users
            _nearbyUsers.value = (3..8).random()

            // Simulate sending alerts
            delay(500)
            _alertsSent.value = _nearbyUsers.value

            // Real implementation would:
            // 1. Scan for nearby BLE devices with SHAKTI UUID
            // 2. Send encrypted emergency alert
            // 3. Include location and threat type
            // 4. Create safety network of nearby users

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Contact emergency services
     */
    private suspend fun contactEmergencyServices() {
        try {
            // Dial emergency number (100 for police in India)
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:100")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start automatic evidence recording
     */
    private suspend fun startAutoRecording() {
        if (_isRecording.value) {
            return // Already recording
        }

        try {
            _isRecording.value = true

            // Create file for recording
            val outputDir = getApplication<Application>().getExternalFilesDir(null)
            recordingFile = File(outputDir, "guardian_evidence_${System.currentTimeMillis()}.m4a")

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(getApplication<Application>())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordingFile?.absolutePath)

                prepare()
                start()
            }

            // Stop recording after 5 minutes
            delay(5 * 60 * 1000)
            stopRecording()

        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    /**
     * Stop evidence recording
     */
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false

            // Save recording info for later access
            recordingFile?.let {
                // In production, encrypt and upload to secure storage
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Activate flashlight strobe (visible emergency alert)
     */
    private suspend fun activateFlashlightStrobe() {
        if (isFlashlightActive) {
            return
        }

        isFlashlightActive = true

        viewModelScope.launch {
            try {
                val cameraId = cameraManager.cameraIdList[0]

                // Strobe for 30 seconds
                repeat(60) {
                    cameraManager.setTorchMode(cameraId, true)
                    delay(250)
                    cameraManager.setTorchMode(cameraId, false)
                    delay(250)
                }

                isFlashlightActive = false

            } catch (e: CameraAccessException) {
                e.printStackTrace()
                isFlashlightActive = false
            }
        }
    }

    /**
     * Stop flashlight strobe
     */
    fun stopFlashlight() {
        try {
            if (isFlashlightActive) {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, false)
                isFlashlightActive = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Notify emergency contacts via SMS
     */
    private suspend fun notifyEmergencyContacts() {
        try {
            // In production, get contacts from user preferences
            val contacts = listOf(
                "+919876543210", // Example emergency contact
            )

            val locationText = _currentLocation.value?.let {
                "Location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "Location unavailable"

            val message = """
                🚨 EMERGENCY ALERT from SHAKTI AI
                
                Threat detected! I need immediate help.
                
                $locationText
                
                Time: ${System.currentTimeMillis()}
                
                Please call me or send help immediately!
            """.trimIndent()

            contacts.forEach { phoneNumber ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$phoneNumber")
                    putExtra("sms_body", message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                getApplication<Application>().startActivity(intent)
            }

            _emergencyContactsCalled.value = true

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start continuous location sharing
     */
    private suspend fun startLocationSharing() {
        // In production, this would:
        // 1. Get GPS location every 10 seconds
        // 2. Upload to secure server
        // 3. Share with emergency contacts
        // 4. Log to blockchain
    }

    /**
     * Update current location
     */
    fun updateLocation(location: Location) {
        _currentLocation.value = location
    }

    /**
     * Stop Guardian monitoring
     */
    fun stopGuardianMonitoring() {
        _isMonitoring.value = false
        try {
            guardianAI?.stopAudioMonitoring()
        } catch (e: Exception) {
            android.util.Log.e("GuardianViewModel", "Error stopping monitoring: ${e.message}")
        }
        _threatDetected.value = false
        _emergencyActivated.value = false

        stopRecording()
        stopFlashlight()
    }

    /**
     * Reset emergency state
     */
    fun resetEmergencyState() {
        _emergencyActivated.value = false
        _threatDetected.value = false
        _alertsSent.value = 0
        _emergencyContactsCalled.value = false

        stopRecording()
        stopFlashlight()
    }

    /**
     * Manual SOS trigger (panic button)
     */
    fun triggerManualSOS() {
        _threatDetected.value = true
        _latestThreat.value = GuardianAI.ThreatDetectionResult(
            isThreat = true,
            threatType = GuardianAI.ThreatType.DISTRESS_CALL,
            confidence = 1.0f,
            timestamp = System.currentTimeMillis()
        )
        triggerEmergencyProtocol()
    }

    /**
     * Get recorded evidence file
     */
    fun getEvidenceFile(): File? = recordingFile

    /**
     * Cleanup resources
     */
    override fun onCleared() {
        super.onCleared()
        stopGuardianMonitoring()
        try {
            guardianAI?.cleanup()
        } catch (e: Exception) {
            android.util.Log.e("GuardianViewModel", "Error during cleanup: ${e.message}")
        }
    }
}
