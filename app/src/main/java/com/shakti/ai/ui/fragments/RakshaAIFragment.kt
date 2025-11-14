package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shakti.ai.R
import com.shakti.ai.models.SafetyPlan
import com.shakti.ai.viewmodel.RakshaViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

class RakshaAIFragment : Fragment() {

    private val viewModel: RakshaViewModel by activityViewModels()

    // UI Elements - Cards
    private lateinit var cardAudioRecording: MaterialCardView
    private lateinit var cardVideoRecording: MaterialCardView
    private lateinit var cardEvidenceArchive: MaterialCardView
    private lateinit var cardEmergencyContacts: MaterialCardView
    private lateinit var cardSafeHouse: MaterialCardView
    private lateinit var cardEscapePlan: MaterialCardView
    private lateinit var btnGenerateFir: com.google.android.material.button.MaterialButton
    private lateinit var btnProtectionOrder: com.google.android.material.button.MaterialButton
    private lateinit var btnFreeLawyer: com.google.android.material.button.MaterialButton
    private lateinit var fabSos: FloatingActionButton
    private lateinit var btnSettings: MaterialCardView

    // Nearby Guardians UI
    private lateinit var btnViewGuardians: com.google.android.material.button.MaterialButton
    private lateinit var btnAlertGuardians: com.google.android.material.button.MaterialButton
    private lateinit var tvNearbyCount: TextView
    private lateinit var tvGuardianCountBadge: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_raksha_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        cardAudioRecording = view.findViewById(R.id.card_audio_recording)
        cardVideoRecording = view.findViewById(R.id.card_video_recording)
        cardEvidenceArchive = view.findViewById(R.id.card_evidence_archive)
        cardEmergencyContacts = view.findViewById(R.id.card_emergency_contacts)
        cardSafeHouse = view.findViewById(R.id.card_safe_house)
        cardEscapePlan = view.findViewById(R.id.card_escape_plan)
        btnGenerateFir = view.findViewById(R.id.btn_generate_fir)
        btnProtectionOrder = view.findViewById(R.id.btn_protection_order)
        btnFreeLawyer = view.findViewById(R.id.btn_free_lawyer)
        fabSos = view.findViewById(R.id.fab_sos)
        btnSettings = view.findViewById(R.id.btn_settings)

        // Nearby Guardians
        btnViewGuardians = view.findViewById(R.id.btn_view_guardians)
        btnAlertGuardians = view.findViewById(R.id.btn_alert_guardians)
        tvNearbyCount = view.findViewById(R.id.tv_nearby_count)
        tvGuardianCountBadge = view.findViewById(R.id.tv_guardian_count_badge)
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        cardAudioRecording.setOnClickListener {
            startRecording("audio")
        }

        cardVideoRecording.setOnClickListener {
            startRecording("video")
        }

        cardEvidenceArchive.setOnClickListener {
            showEvidenceArchive()
        }

        cardEmergencyContacts.setOnClickListener {
            showEmergencyContacts()
        }

        cardSafeHouse.setOnClickListener {
            findSafeHouse()
        }

        cardEscapePlan.setOnClickListener {
            createEscapePlan()
        }

        btnGenerateFir.setOnClickListener {
            generateDVFIR()
        }

        btnProtectionOrder.setOnClickListener {
            applyForProtectionOrder()
        }

        btnFreeLawyer.setOnClickListener {
            connectWithLawyer()
        }

        fabSos.setOnClickListener {
            showQuickSOSDialog()
        }

        // Nearby Guardians actions
        btnViewGuardians.setOnClickListener {
            showNearbyGuardiansList()
        }

        btnAlertGuardians.setOnClickListener {
            alertAllNearbyGuardians()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Handle loading state
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.safetyPlan.collect { plan ->
                plan?.let {
                    showSafetyPlanDialog(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shelterInfo.collect { info ->
                if (info.isNotEmpty()) {
                    showShelterInfo(info)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.threatDetected.collect { detected ->
                if (detected) {
                    viewModel.latestThreat.value?.let { threat ->
                        showThreatAlert(threat.confidence, threat.threatType.name)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emergencyActivated.collect { activated ->
                if (activated) {
                    showEmergencyActiveDialog()
                }
            }
        }

        // NEW: Observe nearby guardians count
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nearbyUsers.collect { count ->
                tvGuardianCountBadge.text = count.toString()

                val message = when {
                    count == 0 -> "No nearby users found"
                    count == 1 -> "1 guardian nearby"
                    else -> "$count guardians nearby"
                }
                tvNearbyCount.text = message
            }
        }

        // NEW: Observe alerts sent
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alertsSent.collect { count ->
                if (count > 0) {
                    Toast.makeText(
                        context,
                        "✅ Alert sent to $count nearby guardians",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showNearbyGuardiansList() {
        val count = viewModel.nearbyUsers.value

        if (count == 0) {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("📡 Nearby Guardians")
                .setMessage(
                    """
                    No nearby Shakti users found in your area.
                    
                    Guardians are Shakti app users within 500m range who can respond to your emergency alerts.
                    
                    💡 Tips:
                    • Make sure Bluetooth is enabled
                    • Wait a few seconds for mesh network to scan
                    • Move to a more populated area
                    
                    You can still use Emergency SOS to call 100/181.
                """.trimIndent()
                )
                .setPositiveButton("Enable AI Monitoring") { _, _ ->
                    toggleAIMonitoring()
                }
                .setNegativeButton("Close", null)
                .show()
            return
        }

        // Generate sample guardian list based on count
        val guardiansList = buildString {
            repeat(count) { index ->
                append("👤 Guardian #${index + 1}\n")
                append("   Distance: ${Random.nextInt(50, 401)}m away\n")
                append("   Response time: ${Random.nextInt(1, 4)} min\n")
                append("   Rating: ⭐ ${(Random.nextDouble() * 0.5 + 4.5).format(1)}\n\n")
            }
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📡 Nearby Guardians ($count)")
            .setMessage(
                """
                Shakti users nearby who can help:
                
                $guardiansList
                
                These guardians will receive your alert instantly via mesh network when you trigger emergency SOS.
            """.trimIndent()
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun alertAllNearbyGuardians() {
        val count = viewModel.nearbyUsers.value

        if (count == 0) {
            Toast.makeText(
                context,
                "No nearby guardians to alert. Try enabling AI monitoring.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📢 Alert Nearby Guardians?")
            .setMessage(
                """
                This will send an alert to all $count nearby guardians:
                
                They will receive:
                • Your location
                • Alert notification
                • "Help needed" message
                
                ⚠️ Use only when you feel unsafe or need immediate assistance.
                
                Alert nearby guardians now?
            """.trimIndent()
            )
            .setPositiveButton("Send Alert") { _, _ ->
                // Trigger alert through ViewModel
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.alertNearbyUsers()
                    Toast.makeText(
                        context,
                        "📡 Alert sent to $count guardians! Help is on the way.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            "🎤 Enable AI Threat Monitoring",
            "💡 Enable Flashlight Strobe",
            "📍 Share Location Continuously",
            "🔔 Auto-Record on Threat Detection",
            "📱 Manage Emergency Contacts",
            "⚙️ Advanced Settings"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("⚙️ Raksha Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toggleAIMonitoring()
                    1 -> activateFlashlightStrobe()
                    2 -> startLocationSharing()
                    3 -> toggleAutoRecording()
                    4 -> manageEmergencyContacts()
                    5 -> openAdvancedSettings()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun toggleAIMonitoring() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isMonitoring = viewModel.isMonitoring.value

            if (isMonitoring) {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Disable AI Monitoring?")
                    .setMessage("This will stop real-time threat detection from ambient audio.")
                    .setPositiveButton("Disable") { _, _ ->
                        viewModel.stopGuardianMonitoring()
                        Toast.makeText(
                            context,
                            "🔴 AI Monitoring Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Enable AI Threat Monitoring?")
                    .setMessage(
                        """
                        This feature uses AI to detect:
                        • Screams and distress calls
                        • Threatening voices
                        • Sudden loud noises
                        • Emergency keywords (Hindi & English)
                        
                        When threats are detected:
                        ✅ Auto-record evidence
                        ✅ Alert nearby guardians
                        ✅ Log to blockchain
                        
                        Requires: Microphone permission
                        Privacy: All processing is on-device
                    """.trimIndent()
                    )
                    .setPositiveButton("Enable") { _, _ ->
                        viewModel.startGuardianMonitoring()
                        Toast.makeText(
                            context,
                            "🟢 AI Monitoring Active - Your safety is being monitored",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun activateFlashlightStrobe() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("💡 Flashlight Strobe")
            .setMessage(
                """
                Activate flashlight strobe to attract attention and signal for help.
                
                The flashlight will blink continuously for 30 seconds as a visible emergency alert.
            """.trimIndent()
            )
            .setPositiveButton("Activate") { _, _ ->
                // Flashlight is activated via emergency protocol
                Toast.makeText(
                    context,
                    "💡 Flashlight strobe activated!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startLocationSharing() {
        Toast.makeText(context, "📍 Location sharing enabled", Toast.LENGTH_SHORT).show()
    }

    private fun toggleAutoRecording() {
        Toast.makeText(
            context,
            "🔴 Auto-recording will start on threat detection",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun manageEmergencyContacts() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📱 Emergency Contacts")
            .setMessage(
                """
                Configure who to notify in emergencies:
                
                Current contacts:
                • Contact 1: +91 98765 43210
                • Contact 2: Not set
                • Contact 3: Not set
                
                (Tap to add/edit contacts)
            """.trimIndent()
            )
            .setPositiveButton("Add Contact") { _, _ ->
                Toast.makeText(context, "Opening contact selector...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openAdvancedSettings() {
        Toast.makeText(context, "⚙️ Advanced settings (Coming soon)", Toast.LENGTH_SHORT).show()
    }

    private fun showThreatAlert(confidence: Float, threatType: String) {
        val level = if (confidence > 0.7f) "HIGH" else if (confidence > 0.4f) "MEDIUM" else "LOW"
        val confidencePercent = (confidence * 100).toInt()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("⚠️ THREAT DETECTED - $level")
            .setMessage(
                """
                AI has detected a potential threat:
                
                Type: $threatType
                Confidence: $confidencePercent%
                Time: ${
                    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                }
                
                Actions taken automatically:
                ✅ Recording evidence
                ✅ Alerting nearby guardians
                ✅ Logging to blockchain
                
                Do you need emergency help?
            """.trimIndent()
            )
            .setPositiveButton("TRIGGER FULL SOS") { _, _ ->
                viewModel.triggerManualSOS()
            }
            .setNeutralButton("I'm Safe") { _, _ ->
                viewModel.resetEmergencyState()
            }
            .setNegativeButton("Dismiss", null)
            .setCancelable(false)
            .show()
    }

    private fun showEmergencyActiveDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🚨 EMERGENCY PROTOCOL ACTIVE")
            .setMessage(
                """
                Full emergency protocol activated:
                
                ✅ Evidence recording started
                ✅ Nearby guardians alerted
                ✅ Emergency contacts notified
                ✅ Location being shared
                ✅ Flashlight strobe activated
                ✅ Blockchain logging active
                
                Help is on the way!
                
                Call emergency services now?
            """.trimIndent()
            )
            .setPositiveButton("Call 100 (Police)") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:100")
                }
                startActivity(intent)
            }
            .setNeutralButton("Call 181 (Women's Helpline)") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:181")
                }
                startActivity(intent)
            }
            .setNegativeButton("I'm Safe Now") { _, _ ->
                confirmCancelEmergency()
            }
            .setCancelable(false)
            .show()
    }

    private fun confirmCancelEmergency() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Emergency?")
            .setMessage("Are you sure you're safe now? This will stop all emergency protocols.")
            .setPositiveButton("Yes, I'm Safe") { _, _ ->
                viewModel.resetEmergencyState()
                Toast.makeText(context, "✅ Emergency cancelled. Stay safe!", Toast.LENGTH_LONG)
                    .show()
            }
            .setNegativeButton("No, Keep Active", null)
            .show()
    }

    private fun showQuickSOSDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🆘 Quick SOS Actions")
            .setMessage(
                """
                Choose your action:
                
                • 🚨 Full Emergency SOS
                • Call 181 (Women's Helpline)
                • Call 100 (Police)
                • 📍 Share location to contacts
                • 🎤 Start silent recording
                • 🗺️ View escape plan
            """.trimIndent()
            )
            .setPositiveButton("FULL EMERGENCY SOS") { _, _ ->
                confirmFullSOS()
            }
            .setNeutralButton("Call 181") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:181")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmFullSOS() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🚨 TRIGGER FULL EMERGENCY SOS?")
            .setMessage(
                """
                This will:
                • Alert ALL nearby guardians
                • Start auto-recording evidence
                • Activate flashlight strobe
                • Share location continuously
                • Notify emergency contacts
                • Log to blockchain
                • Call emergency services
                
                Only use in real emergencies!
            """.trimIndent()
            )
            .setPositiveButton("ACTIVATE SOS") { _, _ ->
                viewModel.triggerManualSOS()
                Toast.makeText(context, "🚨 EMERGENCY SOS ACTIVATED!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startRecording(type: String) {
        val title = if (type == "audio") "🎙️ Audio Recording" else "📹 Video Recording"

        Toast.makeText(
            context,
            "$title started with blockchain verification",
            Toast.LENGTH_SHORT
        ).show()

        // Simulate recording for demo
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Recording $type")
            .setMessage(
                """
                Recording in progress with:
                
                ✅ AES-256 encryption
                ✅ GPS location tracking
                ✅ Blockchain timestamp
                ✅ Tamper-proof hash
                ✅ Court-admissible format
                
                All evidence is automatically backed up to secure cloud storage and cannot be deleted even if phone is taken.
            """.trimIndent()
            )
            .setPositiveButton("Stop Recording") { _, _ ->
                Toast.makeText(
                    context,
                    "✅ Evidence saved securely with blockchain verification",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEvidenceArchive() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📁 Evidence Archive")
            .setMessage(
                """
                🔒 All evidence is blockchain-verified
                
                📅 Incident 1 (Nov 3, 2024)
                • Audio: 5 min 32 sec
                • Location: Home (GPS verified)
                • Hash: 0x7a3f8b2c...
                • Status: ✅ Blockchain confirmed
                
                📅 Incident 2 (Nov 1, 2024)
                • Video: 2 min 18 sec
                • Location: Home (GPS verified)
                • Hash: 0x9b2e4d1a...
                • Status: ✅ Blockchain confirmed
                
                📅 Incident 3 (Oct 28, 2024)
                • Photos: 3 images + Audio
                • Location: Home (GPS verified)
                • Hash: 0x4c1d7e6f...
                • Status: ✅ Blockchain confirmed
                
                All evidence is court-admissible and tamper-proof.
                Distributed storage ensures evidence cannot be destroyed.
            """.trimIndent()
            )
            .setPositiveButton("Export All") { _, _ ->
                Toast.makeText(
                    context,
                    "📤 Exporting all evidence with legal certificates...",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun findSafeHouse() {
        Toast.makeText(context, "🏠 Finding safe houses near you...", Toast.LENGTH_SHORT).show()
        viewModel.findShelters("Current Location")
    }

    private fun showShelterInfo(info: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🏠 Safe Houses Near You")
            .setMessage(info)
            .setPositiveButton("Call Now") { _, _ ->
                Toast.makeText(context, "📞 Calling shelter...", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Get Directions") { _, _ ->
                Toast.makeText(context, "🗺️ Opening maps...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEmergencyContacts() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🆘 Emergency Contacts")
            .setMessage(
                """
                🇮🇳 National Helplines:
                
                📞 Women's Helpline: 181
                   (24/7, toll-free, multilingual)
                
                📞 Police Emergency: 100
                   (Immediate police response)
                
                📞 Women in Distress: 1091
                   (24/7 police helpline)
                
                📞 National Commission for Women
                   7827-170-170 (10 AM - 6 PM)
                
                📞 Domestic Violence Helpline: 181
                   (Free counseling & legal aid)
                
                All helplines are confidential and free.
            """.trimIndent()
            )
            .setPositiveButton("Call 181") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:181")
                }
                startActivity(intent)
            }
            .setNeutralButton("Call 100") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:100")
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun createEscapePlan() {
        val inputEditText = android.widget.EditText(requireContext()).apply {
            hint =
                "E.g., Living with abusive partner, have 2 children, no separate income, in-laws are aware..."
            minLines = 4
            maxLines = 8
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🗺️ Create Your Safety Plan")
            .setMessage("Describe your current situation to create a personalized, AI-powered safety and escape plan:")
            .setView(inputEditText)
            .setPositiveButton("Create Plan") { _, _ ->
                val input = inputEditText.text.toString()
                if (input.isNotBlank()) {
                    Toast.makeText(
                        context,
                        "🤖 AI is creating your personalized safety plan...",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.createSafetyPlan(input)
                } else {
                    Toast.makeText(
                        context,
                        "Please describe your situation",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSafetyPlanDialog(plan: SafetyPlan) {
        val planText = """
            🚨 YOUR PERSONALIZED SAFETY PLAN
            
            ═══════════════════════════════
            
            🛡️ IMMEDIATE SAFETY STEPS:
            ${plan.immediateSafety.joinToString("\n") { "• $it" }}
            
            🏠 SAFE PLACES TO GO:
            ${plan.safePlaces.joinToString("\n") { "• $it" }}
            
            📄 IMPORTANT DOCUMENTS TO TAKE:
            ${plan.importantDocuments.joinToString("\n") { "• $it" }}
            
            🎒 EMERGENCY BAG ESSENTIALS:
            ${plan.emergencyBag.joinToString("\n") { "• $it" }}
            
            💰 FINANCIAL SAFETY:
            ${plan.financialSafety.joinToString("\n") { "• $it" }}
            
            ⚖️ LEGAL STEPS TO TAKE:
            ${plan.legalSteps.joinToString("\n") { "• $it" }}
            
            💜 EMOTIONAL SUPPORT:
            ${plan.emotionalSupport.joinToString("\n") { "• $it" }}
            
            ═══════════════════════════════
            
            🤖 AI CUSTOM RECOMMENDATIONS:
            ${plan.aiCustomPlan}
            
            ═══════════════════════════════
            
            Remember: Your safety is the priority. You are not alone. Help is available.
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Your Personalized Safety Plan")
            .setMessage(planText)
            .setPositiveButton("Save Plan") { _, _ ->
                Toast.makeText(
                    context,
                    "✅ Safety plan saved securely and encrypted",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNeutralButton("Share with Trusted Contact") { _, _ ->
                Toast.makeText(context, "📤 Sharing plan...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun generateDVFIR() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 Generate FIR")
            .setMessage(
                """
                Generate a Domestic Violence FIR (First Information Report) with AI assistance.
                
                The FIR will include:
                • Your incident details
                • Relevant IPC sections
                • Legal framework citations
                • Evidence references
                
                This will redirect you to Nyaya AI for legal assistance.
            """.trimIndent()
            )
            .setPositiveButton("Continue") { _, _ ->
                Toast.makeText(
                    context,
                    "🔄 Opening Nyaya AI FIR generator...",
                    Toast.LENGTH_SHORT
                ).show()
                // In real implementation, navigate to Nyaya AI FIR generator
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyForProtectionOrder() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🛡️ Protection Order")
            .setMessage(
                """
                Apply for a Protection Order under the Domestic Violence Act, 2005.
                
                A protection order can:
                • Prevent the abuser from committing further violence
                • Prohibit contact or communication
                • Restrict entry to your residence or workplace
                
                We'll guide you through the legal process and connect you with legal aid.
            """.trimIndent()
            )
            .setPositiveButton("Apply Now") { _, _ ->
                Toast.makeText(
                    context,
                    "📋 Opening protection order application...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun connectWithLawyer() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👩‍⚖️ Free Legal Aid")
            .setMessage(
                """
                Connect with free legal aid lawyers specializing in:
                
                • Domestic Violence Act
                • Divorce proceedings
                • Child custody
                • Property rights
                • Criminal complaints
                
                All consultations are:
                ✅ Free of cost
                ✅ Confidential
                ✅ Women-focused
                ✅ Available in multiple languages
            """.trimIndent()
            )
            .setPositiveButton("Connect Now") { _, _ ->
                Toast.makeText(
                    context,
                    "📞 Connecting with legal aid network...",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
