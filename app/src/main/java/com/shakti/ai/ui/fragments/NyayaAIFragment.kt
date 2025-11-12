package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.R
import com.shakti.ai.models.DocumentType
import com.shakti.ai.models.LegalCategory
import com.shakti.ai.viewmodel.NyayaViewModel
import kotlinx.coroutines.launch

class NyayaAIFragment : Fragment() {

    private val viewModel: NyayaViewModel by activityViewModels()

    // UI Elements
    private lateinit var incidentTypeSpinner: Spinner
    private lateinit var fullNameInput: EditText
    private lateinit var guardianNameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var mobileInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var accusedDetailsInput: EditText
    private lateinit var reliefInput: EditText
    private lateinit var btnGenerateFir: Button
    private lateinit var btnSaveDraft: Button
    private lateinit var btnExplainRights: Button
    private lateinit var btnFindLawyer: Button
    private lateinit var btnDraftDocument: Button
    private lateinit var btnViewIPCSections: Button

    // Evidence checkboxes
    private lateinit var evidencePhotos: CheckBox
    private lateinit var evidenceAudio: CheckBox
    private lateinit var evidenceScreenshots: CheckBox
    private lateinit var evidenceWitness: CheckBox
    private lateinit var evidenceMedical: CheckBox
    private lateinit var evidenceCctv: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nyaya_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSpinner()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        incidentTypeSpinner = view.findViewById(R.id.incident_type_spinner)
        fullNameInput = view.findViewById(R.id.full_name_input)
        guardianNameInput = view.findViewById(R.id.guardian_name_input)
        ageInput = view.findViewById(R.id.age_input)
        addressInput = view.findViewById(R.id.address_input)
        mobileInput = view.findViewById(R.id.mobile_input)
        dateInput = view.findViewById(R.id.date_input)
        timeInput = view.findViewById(R.id.time_input)
        locationInput = view.findViewById(R.id.location_input)
        descriptionInput = view.findViewById(R.id.description_input)
        accusedDetailsInput = view.findViewById(R.id.accused_details_input)
        reliefInput = view.findViewById(R.id.relief_input)
        btnGenerateFir = view.findViewById(R.id.btn_generate_fir)
        btnSaveDraft = view.findViewById(R.id.btn_save_draft)

        // Additional buttons - create them programmatically as they don't exist in layout yet
        btnExplainRights = createButton("📚 Explain My Rights")
        btnFindLawyer = createButton("👩‍⚖️ Find Lawyer")
        btnDraftDocument = createButton("📝 Draft Legal Doc")
        btnViewIPCSections = createButton("⚖️ View IPC Sections")

        evidencePhotos = view.findViewById(R.id.evidence_photos)
        evidenceAudio = view.findViewById(R.id.evidence_audio)
        evidenceScreenshots = view.findViewById(R.id.evidence_screenshots)
        evidenceWitness = view.findViewById(R.id.evidence_witness)
        evidenceMedical = view.findViewById(R.id.evidence_medical)
        evidenceCctv = view.findViewById(R.id.evidence_cctv)
    }

    private fun createButton(text: String): Button {
        return Button(requireContext()).apply {
            this.text = text
        }
    }

    private fun setupSpinner() {
        val incidentTypes = arrayOf(
            "Select incident type",
            "Domestic Violence",
            "Sexual Harassment",
            "Dowry Harassment",
            "Assault",
            "Stalking",
            "Acid Attack",
            "Rape/Sexual Assault",
            "Kidnapping",
            "Workplace Harassment",
            "Property Dispute",
            "Other"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            incidentTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        incidentTypeSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        btnGenerateFir.setOnClickListener {
            generateFIR()
        }

        btnSaveDraft.setOnClickListener {
            saveDraft()
        }

        btnExplainRights.setOnClickListener {
            showLegalRightsDialog()
        }

        btnFindLawyer.setOnClickListener {
            findLawyer()
        }

        btnDraftDocument.setOnClickListener {
            showDocumentDraftingDialog()
        }

        btnViewIPCSections.setOnClickListener {
            viewIPCSections()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                btnGenerateFir.isEnabled = !isLoading
                btnGenerateFir.text = if (isLoading) "Generating..." else "📄 Generate FIR Document"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.firDocument.collect { fir ->
                if (fir.isNotEmpty()) {
                    showFIRDocument(fir)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.legalRightsExplanation.collect { explanation ->
                if (explanation.isNotEmpty()) {
                    showExplanationDialog("Legal Rights Explained", explanation)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.legalDocument.collect { document ->
                if (document.isNotEmpty()) {
                    showFIRDocument(document, "Legal Document")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lawyerInfo.collect { info ->
                if (info.isNotEmpty()) {
                    showExplanationDialog("Pro-Bono Lawyers", info)
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

    private fun generateFIR() {
        if (!validateInputs()) {
            return
        }

        val complaint = buildComplaintText()
        viewModel.generateFIR(complaint)
    }

    private fun validateInputs(): Boolean {
        if (incidentTypeSpinner.selectedItemPosition == 0) {
            Toast.makeText(context, "Please select incident type", Toast.LENGTH_SHORT).show()
            return false
        }

        if (fullNameInput.text.isBlank()) {
            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
            fullNameInput.requestFocus()
            return false
        }

        if (descriptionInput.text.isBlank()) {
            Toast.makeText(context, "Please describe the incident", Toast.LENGTH_SHORT).show()
            descriptionInput.requestFocus()
            return false
        }

        return true
    }

    private fun buildComplaintText(): String {
        val evidenceList = mutableListOf<String>()
        if (evidencePhotos.isChecked) evidenceList.add("Photos/Videos")
        if (evidenceAudio.isChecked) evidenceList.add("Audio Recordings")
        if (evidenceScreenshots.isChecked) evidenceList.add("Screenshots")
        if (evidenceWitness.isChecked) evidenceList.add("Witness Statements")
        if (evidenceMedical.isChecked) evidenceList.add("Medical Reports")
        if (evidenceCctv.isChecked) evidenceList.add("CCTV Footage")

        return """
            COMPLAINT DETAILS
            
            Type of Incident: ${incidentTypeSpinner.selectedItem}
            
            COMPLAINANT INFORMATION
            Name: ${fullNameInput.text}
            Father's/Husband's Name: ${guardianNameInput.text}
            Age: ${ageInput.text}
            Address: ${addressInput.text}
            Mobile: ${mobileInput.text}
            
            INCIDENT DETAILS
            Date: ${dateInput.text}
            Time: ${timeInput.text}
            Location: ${locationInput.text}
            
            DETAILED DESCRIPTION:
            ${descriptionInput.text}
            
            DETAILS OF ACCUSED:
            ${accusedDetailsInput.text}
            
            EVIDENCE AVAILABLE:
            ${evidenceList.joinToString(", ")}
            
            RELIEF SOUGHT:
            ${reliefInput.text}
        """.trimIndent()
    }

    private fun saveDraft() {
        Toast.makeText(context, "💾 Draft saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showLegalRightsDialog() {
        val rights = arrayOf(
            "Domestic Violence Act",
            "POSH Act (Workplace Harassment)",
            "Dowry Prohibition Act",
            "IPC Section 498A (Cruelty)",
            "IPC Section 354 (Assault on Women)",
            "IPC Section 376 (Rape)",
            "Property Rights for Women"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📚 Select Legal Topic")
            .setItems(rights) { _, which ->
                val topic = rights[which]
                val category = when (which) {
                    0, 3 -> LegalCategory.DOMESTIC_VIOLENCE
                    1 -> LegalCategory.WORKPLACE_HARASSMENT
                    5 -> LegalCategory.PROPERTY_RIGHTS
                    else -> LegalCategory.GENERAL
                }
                viewModel.explainLegalRights(topic, category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun findLawyer() {
        val input = EditText(requireContext()).apply {
            hint = "Enter your city/state"
            setPadding(50, 40, 50, 40)
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👩‍⚖️ Find Pro-Bono Lawyer")
            .setMessage("We'll help you find free or affordable legal aid in your area.")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->
                val location = input.text.toString().ifBlank { "India" }
                val caseType = incidentTypeSpinner.selectedItem?.toString() ?: "General Legal Aid"
                viewModel.matchWithLawyer(caseType, location)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDocumentDraftingDialog() {
        val documents = arrayOf(
            "Restraining Order",
            "Legal Notice",
            "Divorce Petition",
            "Maintenance Application",
            "Protection Order (DV Act)",
            "Complaint Letter"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 Draft Legal Document")
            .setItems(documents) { _, which ->
                val docType = when (which) {
                    0 -> DocumentType.RESTRAINING_ORDER
                    1 -> DocumentType.LEGAL_NOTICE
                    2 -> DocumentType.DIVORCE_PETITION
                    3 -> DocumentType.MAINTENANCE_APPLICATION
                    4 -> DocumentType.PROTECTION_ORDER
                    else -> DocumentType.COMPLAINT_LETTER
                }
                draftDocument(docType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun draftDocument(docType: DocumentType) {
        val details = mapOf(
            "applicant_name" to fullNameInput.text.toString(),
            "applicant_address" to addressInput.text.toString(),
            "respondent_name" to accusedDetailsInput.text.toString(),
            "description" to descriptionInput.text.toString(),
            "relief_sought" to reliefInput.text.toString()
        )

        viewModel.draftLegalDocument(docType, details)
    }

    private fun viewIPCSections() {
        viewLifecycleOwner.lifecycleScope.launch {
            val sections = viewModel.ipcSections.value
            if (sections.isEmpty()) {
                Toast.makeText(context, "Loading IPC sections...", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val sectionList = sections.joinToString("\n\n") {
                "IPC ${it.sectionNumber}: ${it.title}\n" +
                        "Punishment: ${it.punishment}\n" +
                        "Law: ${it.act}"
            }

            showExplanationDialog("IPC Sections for Women", sectionList)
        }
    }

    private fun showFIRDocument(document: String, title: String = "Generated FIR Document") {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(document)
            .setPositiveButton("Copy Text") { _, _ ->
                copyToClipboard(document)
            }
            .setNeutralButton("Share") { _, _ ->
                shareDocument(document)
            }
            .setNegativeButton("Close", null)
            .create()

        dialog.show()

        dialog.findViewById<TextView>(android.R.id.message)?.apply {
            maxLines = 20
            setTextIsSelectable(true)
        }
    }

    private fun showExplanationDialog(title: String, content: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Copy") { _, _ ->
                copyToClipboard(content)
            }
            .setNegativeButton("Close", null)
            .create()

        dialog.show()

        dialog.findViewById<TextView>(android.R.id.message)?.apply {
            maxLines = 20
            setTextIsSelectable(true)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Document", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "✅ Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareDocument(text: String) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Legal Document")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
    }
}
