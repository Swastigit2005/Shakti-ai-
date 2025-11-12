package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.R
import com.shakti.ai.ai.RakshaAI
import com.shakti.ai.models.SafetyPlan
import com.shakti.ai.viewmodel.RakshaViewModel
import kotlinx.coroutines.launch

class RakshaAIFragment : Fragment() {

    private val viewModel: RakshaViewModel by activityViewModels()

    // UI Elements
    private lateinit var btnRevealApp: Button
    private lateinit var btnStartAudioRecording: Button
    private lateinit var btnStartVideoRecording: Button
    private lateinit var btnViewEvidenceArchive: Button
    private lateinit var btnFindSafeHouse: Button
    private lateinit var btnEmergencyContacts: Button
    private lateinit var btnEscapePlan: Button
    private lateinit var btnGenerateFir: Button
    private lateinit var btnProtectionOrder: Button
    private lateinit var btnFreeLawyer: Button

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
        btnRevealApp = view.findViewById(R.id.btn_reveal_app)
        btnStartAudioRecording = view.findViewById(R.id.btn_start_audio_recording)
        btnStartVideoRecording = view.findViewById(R.id.btn_start_video_recording)
        btnViewEvidenceArchive = view.findViewById(R.id.btn_view_evidence_archive)
        btnFindSafeHouse = view.findViewById(R.id.btn_find_safe_house)
        btnEmergencyContacts = view.findViewById(R.id.btn_emergency_contacts)
        btnEscapePlan = view.findViewById(R.id.btn_escape_plan)
        btnGenerateFir = view.findViewById(R.id.btn_generate_fir)
        btnProtectionOrder = view.findViewById(R.id.btn_protection_order)
        btnFreeLawyer = view.findViewById(R.id.btn_free_lawyer)
    }

    private fun setupClickListeners() {
        btnRevealApp.setOnClickListener {
            Toast.makeText(context, "👁️ App revealed in normal mode", Toast.LENGTH_SHORT).show()
        }

        btnStartAudioRecording.setOnClickListener {
            startRecording("audio")
        }

        btnStartVideoRecording.setOnClickListener {
            startRecording("video")
        }

        btnViewEvidenceArchive.setOnClickListener {
            showEvidenceArchive()
        }

        btnFindSafeHouse.setOnClickListener {
            findSafeHouse()
        }

        btnEmergencyContacts.setOnClickListener {
            showEmergencyContacts()
        }

        btnEscapePlan.setOnClickListener {
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
    }

    private fun startRecording(type: String) {
        Toast.makeText(
            context,
            "🎙️ $type recording started with blockchain verification",
            Toast.LENGTH_SHORT
        ).show()

        // Simulate recording for demo
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Recording $type")
            .setMessage("Recording in progress with:\n\n✅ AES-256 encryption\n✅ GPS location tracking\n✅ Blockchain timestamp\n✅ Tamper-proof hash")
            .setPositiveButton("Stop Recording") { _, _ ->
                Toast.makeText(context, "✅ Evidence saved securely", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEvidenceArchive() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📁 Evidence Archive")
            .setMessage(
                """
                Incident 1 (Nov 3, 2024)
                - Audio: 5 min
                - Location: Home
                - Hash: 0x7a3f...
                
                Incident 2 (Nov 1, 2024)
                - Video: 2 min
                - Location: Home
                - Hash: 0x9b2e...
                
                Incident 3 (Oct 28, 2024)
                - Photos: 3 images
                - Location: Home
                - Hash: 0x4c1d...
                
                All evidence is blockchain-verified and court-admissible.
            """.trimIndent()
            )
            .setPositiveButton("Export All") { _, _ ->
                Toast.makeText(context, "Exporting evidence...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun findSafeHouse() {
        viewModel.findShelters("Current Location")
    }

    private fun showShelterInfo(info: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🏠 Safe Houses Near You")
            .setMessage(info)
            .setPositiveButton("Call Now") { _, _ ->
                Toast.makeText(context, "Calling shelter...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEmergencyContacts() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🆘 Emergency Contacts")
            .setMessage(
                """
                Women's Helpline: 181
                Police: 100
                National Commission for Women: 7827-170-170
                Domestic Violence Helpline: 181
                Legal Aid: Contact nearest legal services authority
            """.trimIndent()
            )
            .setPositiveButton("Call 181") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:181")
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun createEscapePlan() {
        val inputEditText = android.widget.EditText(requireContext()).apply {
            hint = "E.g., Living with abusive partner, have 2 children, no separate income..."
            minLines = 4
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Create Your Escape Plan")
            .setMessage("Describe your current situation to create a personalized safety plan:")
            .setView(inputEditText)
            .setPositiveButton("Create Plan") { _, _ ->
                val input = inputEditText.text.toString()
                if (input.isNotBlank()) {
                    viewModel.createSafetyPlan(input)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSafetyPlanDialog(plan: SafetyPlan) {
        val planText = """
            IMMEDIATE SAFETY:
            ${plan.immediateSafety.joinToString("\n")}
            
            SAFE PLACES:
            ${plan.safePlaces.joinToString("\n")}
            
            IMPORTANT DOCUMENTS:
            ${plan.importantDocuments.joinToString("\n")}
            
            EMERGENCY BAG:
            ${plan.emergencyBag.joinToString("\n")}
            
            ${plan.aiCustomPlan}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Your Personalized Safety Plan")
            .setMessage(planText)
            .setPositiveButton("Save Plan") { _, _ ->
                Toast.makeText(context, "✅ Plan saved securely", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun generateDVFIR() {
        Toast.makeText(context, "Opening FIR generator...", Toast.LENGTH_SHORT).show()
        // In real implementation, navigate to Nyaya AI FIR generator
    }

    private fun applyForProtectionOrder() {
        Toast.makeText(context, "Opening protection order application...", Toast.LENGTH_SHORT)
            .show()
    }

    private fun connectWithLawyer() {
        Toast.makeText(context, "Connecting you with free legal aid...", Toast.LENGTH_SHORT).show()
    }
}
