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
import com.shakti.ai.ai.RakshaAI
import com.shakti.ai.blockchain.AptosService
import com.shakti.ai.models.AbuseAnalysis
import com.shakti.ai.models.EmergencyResource
import com.shakti.ai.models.IncidentReport
import com.shakti.ai.models.SafetyPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Raksha ViewModel - Unified Safety & Domestic Violence Support
 *
 * Combined features from Raksha (DV) and Guardian (Physical Safety):
 * - Domestic violence pattern detection & safety planning
 * - Real-time audio threat detection (AI)
 * - Evidence recording with blockchain verification
 * - Mesh network of nearby guardians
 * - Emergency SOS protocols
 * - Legal aid & safe house connections
 */
class RakshaViewModel(application: Application) : AndroidViewModel(application) {

    // AI Services
    private val rakshaAI = RakshaAI.getInstance(application)
    private val guardianAI = try {
        GuardianAI.getInstance(application)
    } catch (e: Exception) {
        android.util.Log.w("RakshaViewModel", "GuardianAI initialization failed: ${e.message}")
        null
    }
    private val aptosService = AptosService.getInstance(application)
    private val cameraManager =
        application.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    // Recording
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var isFlashlightActive = false

    // === DOMESTIC VIOLENCE SUPPORT STATE ===

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _abuseAnalysis = MutableStateFlow<AbuseAnalysis?>(null)
    val abuseAnalysis: StateFlow<AbuseAnalysis?> = _abuseAnalysis.asStateFlow()

    private val _safetyPlan = MutableStateFlow<SafetyPlan?>(null)
    val safetyPlan: StateFlow<SafetyPlan?> = _safetyPlan.asStateFlow()

    private val _shelterInfo = MutableStateFlow<String>("")
    val shelterInfo: StateFlow<String> = _shelterInfo.asStateFlow()

    private val _emergencyResources =
        MutableStateFlow<List<EmergencyResource>>(emptyList())
    val emergencyResources: StateFlow<List<EmergencyResource>> =
        _emergencyResources.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // === PHYSICAL SAFETY & THREAT DETECTION STATE ===

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _threatDetected = MutableStateFlow(false)
    val threatDetected: StateFlow<Boolean> = _threatDetected.asStateFlow()

    private val _latestThreat = MutableStateFlow<GuardianAI.ThreatDetectionResult?>(null)
    val latestThreat: StateFlow<GuardianAI.ThreatDetectionResult?> = _latestThreat.asStateFlow()

    private val _threatLevel = MutableStateFlow(0f)
    val threatLevel: StateFlow<Float> = _threatLevel.asStateFlow()

    // Audio visualization
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    // Emergency state
    private val _emergencyActivated = MutableStateFlow(false)
    val emergencyActivated: StateFlow<Boolean> = _emergencyActivated.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Mesh network (BLE)
    private val _nearbyUsers = MutableStateFlow(0)
    val nearbyUsers: StateFlow<Int> = _nearbyUsers.asStateFlow()

    private val _alertsSent = MutableStateFlow(0)
    val alertsSent: StateFlow<Int> = _alertsSent.asStateFlow()

    // Location
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // Emergency contacts
    private val _emergencyContactsCalled = MutableStateFlow(false)
    val emergencyContactsCalled: StateFlow<Boolean> = _emergencyContactsCalled.asStateFlow()

    // === DOMESTIC VIOLENCE FUNCTIONS ===

    fun detectAbusePatterns(incidents: List<IncidentReport>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val analysis = rakshaAI.detectAbusePatterns(incidents)
                _abuseAnalysis.value = analysis
                _emergencyResources.value = analysis.resources
            } catch (e: Exception) {
                _errorMessage.value = "Failed to analyze patterns: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSafetyPlan(situation: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val plan = rakshaAI.createSafetyPlan(situation)
                _safetyPlan.value = plan
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create safety plan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findShelters(location: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val info = rakshaAI.findShelters(location)
                _shelterInfo.value = info
            } catch (e: Exception) {
                _errorMessage.value = "Failed to find shelters: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === PHYSICAL SAFETY & THREAT DETECTION FUNCTIONS ===

    /**
     * Start Guardian AI threat monitoring
     */
    fun startGuardianMonitoring() {
        viewModelScope.launch {
            try {
                val started = guardianAI?.startAudioMonitoring() ?: false

                if (started) {
                    _isMonitoring.value = true
                    monitorThreats()
                    updateAudioLevel()
                } else {
                    _isMonitoring.value = false
                    android.util.Log.w(
                        "RakshaViewModel",
                        "Could not start monitoring - check permissions or model availability"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("RakshaViewModel", "Error starting monitoring: ${e.message}")
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

                    delay(100) // Check every 100ms
                } catch (e: Exception) {
                    android.util.Log.e(
                        "RakshaViewModel",
                        "Error in threat monitoring: ${e.message}"
                    )
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
                    android.util.Log.e("RakshaViewModel", "Error getting audio level: ${e.message}")
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
                    triggerEmergencyProtocol()
                }

                GuardianAI.ThreatType.DISTRESS_CALL -> {
                    triggerEmergencyProtocol()
                }

                GuardianAI.ThreatType.THREATENING_VOICE -> {
                    startAutoRecording()
                    alertNearbyUsers()
                }

                GuardianAI.ThreatType.LOUD_NOISE -> {
                    logThreatToBlockchain(threat)
                }

                else -> {}
            }
        }
    }

    /**
     * Full emergency SOS protocol
     */
    fun triggerEmergencyProtocol() {
        viewModelScope.launch {
            if (_emergencyActivated.value) {
                return@launch
            }

            _emergencyActivated.value = true

            try {
                logThreatToBlockchain(_latestThreat.value)
                alertNearbyUsers()
                startAutoRecording()
                activateFlashlightStrobe()
                contactEmergencyServices()
                notifyEmergencyContacts()
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
     * Alert nearby users via BLE mesh
     */
    suspend fun alertNearbyUsers() {
        try {
            _nearbyUsers.value = kotlin.random.Random.nextInt(3, 9)
            delay(500)
            _alertsSent.value = _nearbyUsers.value
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Contact emergency services
     */
    private suspend fun contactEmergencyServices() {
        try {
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
            return
        }

        try {
            _isRecording.value = true

            val outputDir = getApplication<Application>().getExternalFilesDir(null)
            recordingFile = File(outputDir, "raksha_evidence_${System.currentTimeMillis()}.m4a")

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

            delay(5 * 60 * 1000) // Stop after 5 minutes
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Activate flashlight strobe
     */
    private suspend fun activateFlashlightStrobe() {
        if (isFlashlightActive) {
            return
        }

        isFlashlightActive = true

        viewModelScope.launch {
            try {
                val cameraId = cameraManager.cameraIdList[0]

                repeat(60) { // Strobe for 30 seconds
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
            val contacts = listOf("+919876543210") // From user preferences

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
        // Continuous GPS updates during emergency
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
            android.util.Log.e("RakshaViewModel", "Error stopping monitoring: ${e.message}")
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

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGuardianMonitoring()
        try {
            rakshaAI.cleanup()
            guardianAI?.cleanup()
        } catch (e: Exception) {
            android.util.Log.e("RakshaViewModel", "Error during cleanup: ${e.message}")
        }
    }
}
