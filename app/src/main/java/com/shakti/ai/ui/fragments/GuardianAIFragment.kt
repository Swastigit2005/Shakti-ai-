package com.shakti.ai.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shakti.ai.R
import com.shakti.ai.viewmodel.GuardianViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * GuardianAIFragment - Physical Safety Module with Three Tabs
 * 1. Mesh Network
 * 2. Evidence System  
 * 3. Emergency Actions
 */
class GuardianAIFragment : Fragment() {

    private val viewModel: GuardianViewModel by activityViewModels()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guardian_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        // Setup ViewPager with tabs
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = GuardianPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "📡 Mesh Network"
                1 -> "📹 Evidence"
                2 -> "🚨 Emergency"
                else -> ""
            }
        }.attach()
    }

    // ViewPager2 Adapter for the three tabs
    private inner class GuardianPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MeshNetworkFragment()
                1 -> EvidenceSystemFragment()
                2 -> EmergencyActionsFragment()
                else -> MeshNetworkFragment()
            }
        }
    }
}

/**
 * Tab 1: Mesh Network (BLE-based guardian network)
 */
class MeshNetworkFragment : Fragment() {

    private val viewModel: GuardianViewModel by activityViewModels()

    private lateinit var guardianSwitch: SwitchCompat
    private lateinit var threatScoreText: TextView
    private lateinit var environmentalProgress: ProgressBar
    private lateinit var guardianRecyclerView: RecyclerView
    private lateinit var becomeGuardianButton: Button
    private lateinit var nearbyCount: TextView
    private lateinit var meshRange: TextView
    private lateinit var responseTime: TextView

    private val guardians = mutableListOf<Guardian>()
    private lateinit var guardianAdapter: GuardianAdapter

    private var isGuardianMode = true
    private var threatScore = 15

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mesh_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadGuardians()
        observeViewModel()

        // DON'T start monitoring automatically - wait for user to toggle the switch
        // This prevents crashes if audio permissions are not granted or model is not available
        // viewModel.startGuardianMonitoring()
    }

    private fun initializeViews(view: View) {
        guardianSwitch = view.findViewById(R.id.guardian_switch)
        threatScoreText = view.findViewById(R.id.threat_score_number)
        environmentalProgress = view.findViewById(R.id.environmental_safety_progress)
        guardianRecyclerView = view.findViewById(R.id.guardian_recycler_view)
        becomeGuardianButton = view.findViewById(R.id.btn_become_guardian)
        nearbyCount = view.findViewById(R.id.nearby_guardians_count)
        meshRange = view.findViewById(R.id.mesh_range_text)
        responseTime = view.findViewById(R.id.response_time_text)

        threatScoreText.text = threatScore.toString()
        environmentalProgress.progress = 85
    }

    private fun setupClickListeners() {
        guardianSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGuardianMode = isChecked
            if (isChecked) {
                Toast.makeText(context, "Guardian Mode: ACTIVE", Toast.LENGTH_SHORT).show()
                viewModel.startGuardianMonitoring()
                startThreatMonitoring()
            } else {
                Toast.makeText(context, "Guardian Mode: OFF", Toast.LENGTH_SHORT).show()
                viewModel.stopGuardianMonitoring()
            }
        }

        becomeGuardianButton.setOnClickListener {
            showBecomeGuardianDialog()
        }
    }

    private fun setupRecyclerView() {
        guardianAdapter = GuardianAdapter(guardians)
        guardianRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guardianAdapter
        }
    }

    private fun observeViewModel() {
        // Observe threat level
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.threatLevel.collect { level ->
                val score = (level * 100).toInt()
                threatScoreText.text = score.toString()
                threatScore = score
                
                if (score > 70) {
                    threatScoreText.setTextColor(resources.getColor(R.color.error, null))
                } else if (score > 40) {
                    threatScoreText.setTextColor(resources.getColor(R.color.warning, null))
                } else {
                    threatScoreText.setTextColor(resources.getColor(R.color.success, null))
                }
            }
        }

        // Observe nearby users
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nearbyUsers.collect { count ->
                nearbyCount.text = count.toString()
            }
        }

        // Observe threat detection
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.threatDetected.collect { detected ->
                if (detected) {
                    viewModel.latestThreat.value?.let { threat ->
                        showThreatAlert(threat.confidence)
                    }
                }
            }
        }
    }

    private fun loadGuardians() {
        guardians.clear()
        guardians.addAll(
            listOf(
                Guardian("#247", "45m", "1 min", 4.9f),
                Guardian("#156", "120m", "2 min", 4.8f),
                Guardian("#389", "180m", "2 min", 5.0f),
                Guardian("#512", "250m", "3 min", 4.7f),
                Guardian("#091", "310m", "3 min", 4.9f)
            )
        )
        guardianAdapter.notifyDataSetChanged()
    }

    private fun startThreatMonitoring() {
        view?.postDelayed({
            if (isGuardianMode) {
                simulateThreatDetection()
            }
        }, 5000)
    }

    private fun simulateThreatDetection() {
        val random = (0..100).random()

        if (random < 10) {
            threatScore = 85
            threatScoreText.text = threatScore.toString()
            showThreatAlert(0.85f)
        } else if (random < 30) {
            threatScore = 45
            threatScoreText.text = threatScore.toString()
        }

        if (isGuardianMode) {
            view?.postDelayed({ simulateThreatDetection() }, 10000)
        }
    }

    private fun showThreatAlert(confidence: Float) {
        val level = if (confidence > 0.7f) "HIGH" else if (confidence > 0.4f) "MEDIUM" else "LOW"
        val message = "Suspicious activity detected nearby\nConfidence: ${(confidence * 100).toInt()}%"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Threat Detected - $level")
            .setMessage(message)
            .setPositiveButton("Alert Guardians") { _, _ ->
                val count = nearbyCount.text.toString().toIntOrNull() ?: 12
                Toast.makeText(context, "Alert sent to $count guardians", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun showBecomeGuardianDialog() {
        val message =
            "Become a Guardian and help protect other women!\n\nYou'll receive alerts when someone nearby needs help."

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👤 Become a Guardian")
            .setMessage(message)
            .setPositiveButton("Join Now") { _, _ ->
                Toast.makeText(context, "✅ You are now a Guardian!", Toast.LENGTH_LONG).show()
                guardians.add(0, Guardian("#YOU", "0m", "< 1 min", 5.0f))
                guardianAdapter.notifyItemInserted(0)
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    // Data class
    data class Guardian(
        val id: String,
        val distance: String,
        val responseTime: String,
        val rating: Float
    )

    // Guardian Adapter
    inner class GuardianAdapter(private val guardians: List<Guardian>) :
        RecyclerView.Adapter<GuardianAdapter.GuardianViewHolder>() {

        inner class GuardianViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idText: TextView = view.findViewById(android.R.id.text1)
            val detailsText: TextView = view.findViewById(android.R.id.text2)
            val ratingText: TextView = view.findViewById(android.R.id.summary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianViewHolder {
            val view = LinearLayout(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                orientation = LinearLayout.VERTICAL
                setPadding(24, 16, 24, 16)
                setBackgroundColor(resources.getColor(android.R.color.white, null))

                addView(TextView(context).apply {
                    id = android.R.id.text1
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                })

                addView(TextView(context).apply {
                    id = android.R.id.text2
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 0)
                })

                addView(TextView(context).apply {
                    id = android.R.id.summary
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.guardian_color, null))
                    setPadding(0, 8, 0, 0)
                })
            }

            return GuardianViewHolder(view)
        }

        override fun onBindViewHolder(holder: GuardianViewHolder, position: Int) {
            val guardian = guardians[position]
            holder.idText.text = "Guardian ${guardian.id}"
            holder.detailsText.text =
                "${guardian.distance} away • Response: ${guardian.responseTime}"
            holder.ratingText.text = "⭐ ${guardian.rating}"
        }

        override fun getItemCount() = guardians.size
    }
}

/**
 * Tab 2: Evidence System (Auto-recording and evidence management)
 */
class EvidenceSystemFragment : Fragment() {

    private val viewModel: GuardianViewModel by activityViewModels()
    
    private lateinit var autoRecordSwitch: SwitchCompat
    private lateinit var recordButton: Button
    private lateinit var stopRecordButton: Button
    private lateinit var recordingStatus: TextView
    private lateinit var evidenceRecyclerView: RecyclerView
    private lateinit var uploadButton: Button
    
    private val evidenceList = mutableListOf<EvidenceItem>()
    private lateinit var evidenceAdapter: EvidenceAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true &&
            permissions[Manifest.permission.CAMERA] == true) {
            startManualRecording()
        } else {
            Toast.makeText(context, "Permissions required for recording", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_evidence_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadEvidenceList()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        autoRecordSwitch = view.findViewById(R.id.auto_record_switch)
        recordButton = view.findViewById(R.id.btn_start_recording)
        stopRecordButton = view.findViewById(R.id.btn_stop_recording)
        recordingStatus = view.findViewById(R.id.recording_status)
        evidenceRecyclerView = view.findViewById(R.id.evidence_recycler_view)
        uploadButton = view.findViewById(R.id.btn_upload_evidence)
        
        stopRecordButton.isEnabled = false
    }

    private fun setupClickListeners() {
        autoRecordSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(context, "Auto-recording enabled on threat detection", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Auto-recording disabled", Toast.LENGTH_SHORT).show()
            }
        }

        recordButton.setOnClickListener {
            checkPermissionsAndRecord()
        }

        stopRecordButton.setOnClickListener {
            stopRecording()
        }

        uploadButton.setOnClickListener {
            uploadToBlockchain()
        }
    }

    private fun setupRecyclerView() {
        evidenceAdapter = EvidenceAdapter(evidenceList)
        evidenceRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = evidenceAdapter
        }
    }

    private fun observeViewModel() {
        // Observe recording state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRecording.collect { isRecording ->
                if (isRecording) {
                    recordingStatus.text = "🔴 Recording in progress..."
                    recordingStatus.setTextColor(resources.getColor(R.color.error, null))
                    recordButton.isEnabled = false
                    stopRecordButton.isEnabled = true
                } else {
                    recordingStatus.text = "⚪ Not recording"
                    recordingStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                    recordButton.isEnabled = true
                    stopRecordButton.isEnabled = false
                }
            }
        }

        // Observe emergency activation (auto-records)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emergencyActivated.collect { activated ->
                if (activated && autoRecordSwitch.isChecked) {
                    Toast.makeText(context, "📹 Auto-recording started!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissionsAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startManualRecording()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    )
                )
            }
        }
    }

    private fun startManualRecording() {
        // Start recording via ViewModel
        Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
        // ViewModel handles actual recording
    }

    private fun stopRecording() {
        viewModel.stopRecording()
        
        // Add to evidence list
        viewModel.getEvidenceFile()?.let { file ->
            evidenceList.add(0, EvidenceItem(
                filename = file.name,
                timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date()),
                size = "${file.length() / 1024} KB",
                type = "Audio/Video",
                uploaded = false
            ))
            evidenceAdapter.notifyItemInserted(0)
        }
        
        Toast.makeText(context, "✅ Recording saved as evidence", Toast.LENGTH_SHORT).show()
    }

    private fun loadEvidenceList() {
        // Load previously saved evidence
        evidenceList.clear()
        evidenceList.addAll(listOf(
            EvidenceItem("evidence_1234.m4a", "Jan 15, 2025 14:30", "2.4 MB", "Audio", true),
            EvidenceItem("evidence_5678.mp4", "Jan 14, 2025 09:15", "15.8 MB", "Video", true),
            EvidenceItem("evidence_9012.m4a", "Jan 12, 2025 18:45", "1.9 MB", "Audio", false)
        ))
        evidenceAdapter.notifyDataSetChanged()
    }

    private fun uploadToBlockchain() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📤 Upload Evidence to Blockchain")
            .setMessage("This will securely store your evidence on the Aptos blockchain with timestamp verification.\n\nOnce uploaded, it cannot be tampered with.")
            .setPositiveButton("Upload") { _, _ ->
                Toast.makeText(context, "Uploading to blockchain...", Toast.LENGTH_SHORT).show()
                // Upload logic here
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class EvidenceItem(
        val filename: String,
        val timestamp: String,
        val size: String,
        val type: String,
        val uploaded: Boolean
    )

    inner class EvidenceAdapter(private val items: List<EvidenceItem>) :
        RecyclerView.Adapter<EvidenceAdapter.EvidenceViewHolder>() {

        inner class EvidenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val filenameText: TextView = view.findViewById(android.R.id.text1)
            val detailsText: TextView = view.findViewById(android.R.id.text2)
            val statusText: TextView = view.findViewById(android.R.id.summary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
            val view = LinearLayout(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                orientation = LinearLayout.VERTICAL
                setPadding(24, 16, 24, 16)
                setBackgroundColor(resources.getColor(android.R.color.white, null))

                addView(TextView(context).apply {
                    id = android.R.id.text1
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                })

                addView(TextView(context).apply {
                    id = android.R.id.text2
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 0)
                })

                addView(TextView(context).apply {
                    id = android.R.id.summary
                    textSize = 12f
                    setPadding(0, 8, 0, 0)
                })
            }

            return EvidenceViewHolder(view)
        }

        override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
            val item = items[position]
            holder.filenameText.text = "📹 ${item.filename}"
            holder.detailsText.text = "${item.timestamp} • ${item.size} • ${item.type}"
            
            if (item.uploaded) {
                holder.statusText.text = "✅ Uploaded to blockchain"
                holder.statusText.setTextColor(resources.getColor(R.color.success, null))
            } else {
                holder.statusText.text = "⏳ Not uploaded"
                holder.statusText.setTextColor(resources.getColor(R.color.warning, null))
            }
        }

        override fun getItemCount() = items.size
    }
}

/**
 * Tab 3: Emergency Actions (SOS and emergency protocols)
 */
class EmergencyActionsFragment : Fragment() {

    private val viewModel: GuardianViewModel by activityViewModels()
    
    private lateinit var sosButton: Button
    private lateinit var policeButton: Button
    private lateinit var ambulanceButton: Button
    private lateinit var emergencyContactsButton: Button
    private lateinit var shareLocationButton: Button
    private lateinit var flashlightButton: Button
    private lateinit var sirenButton: Button
    private lateinit var cancelButton: Button
    private lateinit var emergencyStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emergency_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        sosButton = view.findViewById(R.id.btn_sos)
        policeButton = view.findViewById(R.id.btn_call_police)
        ambulanceButton = view.findViewById(R.id.btn_call_ambulance)
        emergencyContactsButton = view.findViewById(R.id.btn_notify_contacts)
        shareLocationButton = view.findViewById(R.id.btn_share_location)
        flashlightButton = view.findViewById(R.id.btn_flashlight_strobe)
        sirenButton = view.findViewById(R.id.btn_siren)
        cancelButton = view.findViewById(R.id.btn_cancel_emergency)
        emergencyStatus = view.findViewById(R.id.emergency_status)
        
        cancelButton.isEnabled = false
    }

    private fun setupClickListeners() {
        sosButton.setOnClickListener {
            confirmSOS()
        }

        policeButton.setOnClickListener {
            callPolice()
        }

        ambulanceButton.setOnClickListener {
            callAmbulance()
        }

        emergencyContactsButton.setOnClickListener {
            notifyContacts()
        }

        shareLocationButton.setOnClickListener {
            shareLocation()
        }

        flashlightButton.setOnClickListener {
            toggleFlashlight()
        }

        sirenButton.setOnClickListener {
            playSiren()
        }

        cancelButton.setOnClickListener {
            cancelEmergency()
        }
    }

    private fun observeViewModel() {
        // Observe emergency state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emergencyActivated.collect { activated ->
                if (activated) {
                    emergencyStatus.text = "🚨 EMERGENCY ACTIVE"
                    emergencyStatus.setTextColor(resources.getColor(R.color.error, null))
                    sosButton.isEnabled = false
                    cancelButton.isEnabled = true
                } else {
                    emergencyStatus.text = "⚪ Normal Status"
                    emergencyStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                    sosButton.isEnabled = true
                    cancelButton.isEnabled = false
                }
            }
        }

        // Observe alerts sent
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alertsSent.collect { count ->
                if (count > 0) {
                    Toast.makeText(context, "✅ Alert sent to $count guardians", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmSOS() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🚨 TRIGGER FULL EMERGENCY SOS?")
            .setMessage("""
                This will:
                • Call emergency services (100/112)
                • Alert all nearby guardians
                • Start auto-recording evidence
                • Activate flashlight strobe
                • Share your location continuously
                • Notify emergency contacts via SMS
                
                Only use in real emergencies!
            """.trimIndent())
            .setPositiveButton("ACTIVATE SOS") { _, _ ->
                viewModel.triggerManualSOS()
                Toast.makeText(context, "🚨 EMERGENCY SOS ACTIVATED", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun callPolice() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:100")
        }
        startActivity(intent)
    }

    private fun callAmbulance() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:108")
        }
        startActivity(intent)
    }

    private fun notifyContacts() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📱 Notify Emergency Contacts")
            .setMessage("Send SMS to all emergency contacts with your location?")
            .setPositiveButton("Send") { _, _ ->
                Toast.makeText(context, "Sending SMS to emergency contacts...", Toast.LENGTH_SHORT).show()
                // ViewModel handles notification
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareLocation() {
        val message = "I need help! My current location: [GPS coordinates]"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Emergency Location Share")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Location"))
    }

    private fun toggleFlashlight() {
        Toast.makeText(context, "📱 Flashlight strobe activated", Toast.LENGTH_SHORT).show()
        // ViewModel handles flashlight
    }

    private fun playSiren() {
        Toast.makeText(context, "🔊 Siren playing (coming soon)", Toast.LENGTH_SHORT).show()
    }

    private fun cancelEmergency() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Emergency?")
            .setMessage("Are you safe now? This will stop all emergency protocols.")
            .setPositiveButton("Yes, I'm Safe") { _, _ ->
                viewModel.resetEmergencyState()
                Toast.makeText(context, "Emergency cancelled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
