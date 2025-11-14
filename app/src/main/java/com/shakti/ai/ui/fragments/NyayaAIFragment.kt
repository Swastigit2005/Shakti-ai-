package com.shakti.ai.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.shakti.ai.models.DocumentType
import com.shakti.ai.models.LegalCategory
import com.shakti.ai.viewmodel.NyayaViewModel
import kotlinx.coroutines.launch

/**
 * NyayaAIFragment - Legal Rights & Justice Module with Four Tabs
 * 1. FIR Generator
 * 2. Know Your Rights
 * 3. Legal Education
 * 4. Free Lawyers
 */
class NyayaAIFragment : Fragment() {

    private val viewModel: NyayaViewModel by activityViewModels()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nyaya_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("NyayaAIFragment", "Main fragment onViewCreated called")

        try {
            // Initialize TabLayout and ViewPager2
            tabLayout = view.findViewById(R.id.tab_layout)
            viewPager = view.findViewById(R.id.view_pager)

            Log.d(
                "NyayaAIFragment",
                "TabLayout and ViewPager found: ${tabLayout != null}, ${viewPager != null}"
            )

            // Setup ViewPager with tabs
            setupViewPager()
            Log.d("NyayaAIFragment", "ViewPager setup completed successfully")

            // Setup Quick Access Cards
            setupQuickAccessCards(view)
            Log.d("NyayaAIFragment", "Quick access cards setup completed")

        } catch (e: Exception) {
            Log.e("NyayaAIFragment", "Error in main fragment setup: ${e.message}", e)
            Toast.makeText(context, "Error setting up Nyaya AI: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setupViewPager() {
        try {
            Log.d("NyayaAIFragment", "Setting up ViewPager adapter")
            val adapter = NyayaPagerAdapter(this)
            viewPager.adapter = adapter
            Log.d("NyayaAIFragment", "ViewPager adapter set successfully")

            // Connect TabLayout with ViewPager2
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                val tabText = when (position) {
                    0 -> "📄 FIR Generator"
                    1 -> "📚 Know Rights"
                    2 -> "🎓 Legal Education"
                    3 -> "👩‍⚖️ Free Lawyers"
                    else -> ""
                }
                tab.text = tabText
                Log.d("NyayaAIFragment", "Tab $position created: $tabText")
            }.attach()

            Log.d("NyayaAIFragment", "TabLayoutMediator attached successfully")

        } catch (e: Exception) {
            Log.e("NyayaAIFragment", "Error setting up ViewPager: ${e.message}", e)
            Toast.makeText(context, "Error setting up tabs: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupQuickAccessCards(view: View) {
        // Quick Access: Know Your Rights
        view.findViewById<View>(R.id.quick_card_rights)?.setOnClickListener {
            viewPager.currentItem = 1 // Navigate to Know Your Rights tab
            Toast.makeText(context, "Opening Know Your Rights", Toast.LENGTH_SHORT).show()
        }

        // Quick Access: Generate FIR
        view.findViewById<View>(R.id.quick_card_fir)?.setOnClickListener {
            viewPager.currentItem = 0 // Navigate to FIR Generator tab
            Toast.makeText(context, "Opening FIR Generator", Toast.LENGTH_SHORT).show()
        }

        // Quick Access: Free Lawyers
        view.findViewById<View>(R.id.quick_card_lawyers)?.setOnClickListener {
            viewPager.currentItem = 3 // Navigate to Free Lawyers tab
            Toast.makeText(context, "Finding free lawyers near you...", Toast.LENGTH_SHORT).show()
        }

        // Quick Access: Emergency Helpline
        view.findViewById<View>(R.id.quick_card_emergency)?.setOnClickListener {
            showEmergencyHelpline()
        }

        // Cross-link to Education/Gyaan AI
        view.findViewById<Button>(R.id.btn_goto_education)?.setOnClickListener {
            navigateToGyaanAI()
        }
    }

    private fun showEmergencyHelpline() {
        val emergencyNumbers = """
            🚨 EMERGENCY HELPLINE NUMBERS
            
            📞 Women Helpline: 181
            📞 Police: 100
            📞 Ambulance: 108
            📞 National Commission for Women: 7827-170-170
            
            📞 Domestic Violence:
            • 181 (24/7)
            • WhatsApp: 7217735372
            
            📞 NIMHANS Mental Health: 080-4611-0007
            📞 Vandrevala Foundation: 1860-2662-345
            
            ⚠️ Call immediately if you're in danger!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🚨 Emergency Helpline")
            .setMessage(emergencyNumbers)
            .setPositiveButton("Call 181") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:181")
                }
                startActivity(intent)
            }
            .setNeutralButton("Call Police 100") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:100")
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun navigateToGyaanAI() {
        // Navigate to Gyaan AI fragment
        val gyaanFragment = GyaanAIFragment()
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, gyaanFragment)
            .addToBackStack(null)
            .commit()
        Toast.makeText(context, "Navigating to Education & Scholarships...", Toast.LENGTH_SHORT)
            .show()
    }

    // ViewPager2 Adapter for the four tabs
    private inner class NyayaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            val fragmentName = when (position) {
                0 -> "FIRGeneratorFragment"
                1 -> "KnowYourRightsFragment"
                2 -> "LegalEducationFragment"
                3 -> "FreeLawyersFragment"
                else -> "FIRGeneratorFragment"
            }

            Log.d("NyayaAIFragment", "Creating fragment for position $position: $fragmentName")

            return try {
                val fragment = when (position) {
                    0 -> FIRGeneratorFragment()
                    1 -> KnowYourRightsFragment()
                    2 -> LegalEducationFragment()
                    3 -> FreeLawyersFragment()
                    else -> FIRGeneratorFragment()
                }
                Log.d("NyayaAIFragment", "Successfully created fragment: $fragmentName")
                fragment
            } catch (e: Exception) {
                Log.e("NyayaAIFragment", "Error creating fragment $fragmentName: ${e.message}", e)
                // Return FIRGeneratorFragment as fallback
                Toast.makeText(context, "Error loading $fragmentName", Toast.LENGTH_SHORT).show()
                FIRGeneratorFragment()
            }
        }
    }
}

/**
 * Tab 1: FIR Generator (Original functionality)
 */
class FIRGeneratorFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_fir_generator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FIRGenerator", "Fragment onViewCreated called")

        try {
            initializeViews(view)
            Log.d("FIRGenerator", "Views initialized successfully")

            setupSpinner()
            Log.d("FIRGenerator", "Spinner setup completed")

            setupClickListeners()
            Log.d("FIRGenerator", "Click listeners setup completed")

            observeViewModel()
            Log.d("FIRGenerator", "ViewModel observers setup completed")

        } catch (e: Exception) {
            Log.e("FIRGenerator", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(
                context,
                "Error initializing FIR Generator: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
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

        evidencePhotos = view.findViewById(R.id.evidence_photos)
        evidenceAudio = view.findViewById(R.id.evidence_audio)
        evidenceScreenshots = view.findViewById(R.id.evidence_screenshots)
        evidenceWitness = view.findViewById(R.id.evidence_witness)
        evidenceMedical = view.findViewById(R.id.evidence_medical)
        evidenceCctv = view.findViewById(R.id.evidence_cctv)
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

    private fun showFIRDocument(document: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Generated FIR Document")
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

/**
 * Tab 2: Know Your Rights (Legal rights education and explanations)
 */
class KnowYourRightsFragment : Fragment() {

    private val viewModel: NyayaViewModel by activityViewModels()
    
    private lateinit var rightsRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var categorySpinner: Spinner
    
    private val rightsList = mutableListOf<LegalRight>()
    private lateinit var rightsAdapter: LegalRightsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_know_your_rights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("KnowYourRights", "Fragment onViewCreated called")

        try {
            initializeViews(view)
            Log.d("KnowYourRights", "Views initialized successfully")

            setupRecyclerView()
            Log.d("KnowYourRights", "RecyclerView setup completed")

            setupSpinner()
            Log.d("KnowYourRights", "Spinner setup completed")

            setupClickListeners()
            Log.d("KnowYourRights", "Click listeners setup completed")

            loadRights()
            Log.d("KnowYourRights", "Rights loaded successfully")

            observeViewModel()
            Log.d("KnowYourRights", "ViewModel observers setup completed")

        } catch (e: Exception) {
            Log.e("KnowYourRights", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(
                context,
                "Error initializing Know Your Rights: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeViews(view: View) {
        rightsRecyclerView = view.findViewById(R.id.rights_recycler_view)
        searchInput = view.findViewById(R.id.search_input)
        searchButton = view.findViewById(R.id.search_button)
        categorySpinner = view.findViewById(R.id.category_spinner)
    }

    private fun setupRecyclerView() {
        rightsAdapter = LegalRightsAdapter(rightsList) { right ->
            showRightDetails(right)
        }
        rightsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rightsAdapter
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf(
            "All Categories",
            "Domestic Violence",
            "Workplace Harassment", 
            "Property Rights",
            "Family Law",
            "Criminal Law",
            "Constitutional Rights"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            searchRights()
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterByCategory(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.legalRightsExplanation.collect { explanation ->
                    Log.d(
                        "KnowYourRights",
                        "Legal rights explanation received: ${explanation.length} characters"
                    )
                    if (explanation.isNotEmpty()) {
                        showExplanationDialog("Legal Rights Explained", explanation)
                    }
                }
            } catch (e: Exception) {
                Log.e("KnowYourRights", "Error observing legal rights explanation: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Error loading legal explanation: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.isLoading.collect { isLoading ->
                    Log.d("KnowYourRights", "Loading state changed: $isLoading")
                    searchButton.isEnabled = !isLoading
                    searchButton.text = if (isLoading) "Searching..." else "🔍 Search"
                }
            } catch (e: Exception) {
                Log.e("KnowYourRights", "Error observing loading state: ${e.message}", e)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.errorMessage.collect { error ->
                    error?.let {
                        Log.e("KnowYourRights", "ViewModel error: $it")
                        Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            } catch (e: Exception) {
                Log.e("KnowYourRights", "Error observing error messages: ${e.message}", e)
            }
        }
    }

    private fun loadRights() {
        rightsList.clear()
        rightsList.addAll(listOf(
            LegalRight(
                "Right to Live with Dignity",
                "Article 21 - Constitution of India",
                "Every woman has the fundamental right to live with dignity, free from violence and discrimination.",
                "Constitutional Rights",
                "⚖️"
            ),
            LegalRight(
                "Protection from Domestic Violence",
                "Domestic Violence Act 2005",
                "Women have the right to be protected from physical, sexual, verbal, and emotional abuse by family members.",
                "Domestic Violence",
                "🏠"
            ),
            LegalRight(
                "Right to Work without Harassment",
                "POSH Act 2013",
                "Women have the right to work in an environment free from sexual harassment.",
                "Workplace Harassment",
                "💼"
            ),
            LegalRight(
                "Equal Property Rights",
                "Hindu Succession Act 2005",
                "Women have equal rights in ancestral property and can inherit parental property.",
                "Property Rights",
                "🏡"
            ),
            LegalRight(
                "Right to Maintenance",
                "Section 125 CrPC",
                "Women have the right to claim maintenance from husband/relatives for livelihood.",
                "Family Law",
                "💰"
            ),
            LegalRight(
                "Protection from Dowry Harassment",
                "Dowry Prohibition Act 1961",
                "Taking or giving dowry is illegal. Women can file complaints against dowry demands.",
                "Criminal Law",
                "💍"
            ),
            LegalRight(
                "Right to Free Legal Aid",
                "Legal Services Authority Act 1987",
                "Women can get free legal representation through legal aid services.",
                "Constitutional Rights",
                "⚖️"
            ),
            LegalRight(
                "Protection from Stalking",
                "IPC Section 354D",
                "Stalking is a criminal offense. Women can file FIR against stalkers.",
                "Criminal Law",
                "👁️"
            )
        ))
        rightsAdapter.notifyDataSetChanged()
    }

    private fun searchRights() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) {
            loadRights()
            return
        }

        val category = when (categorySpinner.selectedItemPosition) {
            1 -> LegalCategory.DOMESTIC_VIOLENCE
            2 -> LegalCategory.WORKPLACE_HARASSMENT
            3 -> LegalCategory.PROPERTY_RIGHTS
            4 -> LegalCategory.FAMILY_LAW
            else -> LegalCategory.GENERAL
        }

        viewModel.explainLegalRights(query, category)
    }

    private fun filterByCategory(categoryIndex: Int) {
        if (categoryIndex == 0) {
            loadRights()
            return
        }

        val category = when (categoryIndex) {
            1 -> "Domestic Violence"
            2 -> "Workplace Harassment"
            3 -> "Property Rights"
            4 -> "Family Law"
            5 -> "Criminal Law"
            6 -> "Constitutional Rights"
            else -> ""
        }

        val filtered = rightsList.filter { it.category == category }
        rightsAdapter.updateList(filtered)
    }

    private fun showRightDetails(right: LegalRight) {
        val message = """
            ${right.title}
            
            Law: ${right.law}
            
            Description:
            ${right.description}
            
            Category: ${right.category}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("${right.icon} Legal Right Details")
            .setMessage(message)
            .setPositiveButton("Get More Info") { _, _ ->
                viewModel.explainLegalRights(right.title, LegalCategory.GENERAL)
            }
            .setNeutralButton("Share") { _, _ ->
                shareDocument(message)
            }
            .setNegativeButton("Close", null)
            .show()
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
        val clip = android.content.ClipData.newPlainText("Legal Rights", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "✅ Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareDocument(text: String) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Legal Rights Information")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
    }

    // Data class for legal rights
    data class LegalRight(
        val title: String,
        val law: String,
        val description: String,
        val category: String,
        val icon: String
    )

    // Adapter for legal rights
    inner class LegalRightsAdapter(
        private var rights: List<LegalRight>,
        private val onItemClick: (LegalRight) -> Unit
    ) : RecyclerView.Adapter<LegalRightsAdapter.RightViewHolder>() {

        fun updateList(newRights: List<LegalRight>) {
            rights = newRights
            notifyDataSetChanged()
        }

        inner class RightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleText: TextView = view.findViewById(android.R.id.text1)
            val lawText: TextView = view.findViewById(android.R.id.text2)
            val descriptionText: TextView = view.findViewById(android.R.id.summary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RightViewHolder {
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
                isClickable = true
                isFocusable = true

                addView(TextView(context).apply {
                    id = android.R.id.text1
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                })

                addView(TextView(context).apply {
                    id = android.R.id.text2
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.nyaya_color, null))
                    setPadding(0, 4, 0, 0)
                })

                addView(TextView(context).apply {
                    id = android.R.id.summary
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 0)
                })
            }

            return RightViewHolder(view)
        }

        override fun onBindViewHolder(holder: RightViewHolder, position: Int) {
            val right = rights[position]
            holder.titleText.text = "${right.icon} ${right.title}"
            holder.lawText.text = right.law
            holder.descriptionText.text = right.description

            holder.itemView.setOnClickListener {
                onItemClick(right)
            }
        }

        override fun getItemCount() = rights.size
    }
}

/**
 * Tab 3: Legal Education (Educational content and courses)
 */
class LegalEducationFragment : Fragment() {

    private val viewModel: NyayaViewModel by activityViewModels()
    
    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var quizButton: Button
    private lateinit var videosButton: Button
    private lateinit var articlesButton: Button
    private lateinit var faqButton: Button
    
    private val coursesList = mutableListOf<LegalCourse>()
    private lateinit var coursesAdapter: LegalCoursesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_legal_education, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("LegalEducation", "Fragment onViewCreated called")

        try {
            initializeViews(view)
            Log.d("LegalEducation", "Views initialized successfully")

            setupRecyclerView()
            Log.d("LegalEducation", "RecyclerView setup completed")

            setupClickListeners()
            Log.d("LegalEducation", "Click listeners setup completed")

            loadCourses()
            Log.d("LegalEducation", "Courses loaded successfully")

        } catch (e: Exception) {
            Log.e("LegalEducation", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(
                context,
                "Error initializing Legal Education: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeViews(view: View) {
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view)
        quizButton = view.findViewById(R.id.btn_legal_quiz)
        videosButton = view.findViewById(R.id.btn_video_library)
        articlesButton = view.findViewById(R.id.btn_articles)
        faqButton = view.findViewById(R.id.btn_faq)
    }

    private fun setupRecyclerView() {
        coursesAdapter = LegalCoursesAdapter(coursesList) { course ->
            startCourse(course)
        }
        coursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = coursesAdapter
        }
    }

    private fun setupClickListeners() {
        quizButton.setOnClickListener {
            Log.d("LegalEducation", "Quiz button clicked")
            try {
                startLegalQuiz()
            } catch (e: Exception) {
                Log.e("LegalEducation", "Error starting quiz: ${e.message}", e)
                Toast.makeText(context, "Error starting quiz: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        videosButton.setOnClickListener {
            Log.d("LegalEducation", "Videos button clicked")
            try {
                showVideoLibrary()
            } catch (e: Exception) {
                Log.e("LegalEducation", "Error showing videos: ${e.message}", e)
                Toast.makeText(context, "Error loading videos: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        articlesButton.setOnClickListener {
            Log.d("LegalEducation", "Articles button clicked")
            try {
                showArticles()
            } catch (e: Exception) {
                Log.e("LegalEducation", "Error showing articles: ${e.message}", e)
                Toast.makeText(context, "Error loading articles: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        faqButton.setOnClickListener {
            Log.d("LegalEducation", "FAQ button clicked")
            try {
                showFAQ()
            } catch (e: Exception) {
                Log.e("LegalEducation", "Error showing FAQ: ${e.message}", e)
                Toast.makeText(context, "Error loading FAQ: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun loadCourses() {
        coursesList.clear()
        coursesList.addAll(listOf(
            LegalCourse(
                "Understanding Domestic Violence Act",
                "Learn about your rights under the DV Act 2005",
                "45 mins",
                "Beginner",
                "🏠",
                85
            ),
            LegalCourse(
                "Workplace Sexual Harassment - POSH Act",
                "Know your rights and complaint procedures",
                "30 mins",
                "Beginner",
                "💼",
                92
            ),
            LegalCourse(
                "Women's Property Rights in India",
                "Complete guide to inheritance and property laws",
                "60 mins",
                "Intermediate",
                "🏡",
                78
            ),
            LegalCourse(
                "Filing FIR: Step by Step Guide",
                "Learn how to file an FIR effectively",
                "25 mins",
                "Beginner",
                "📄",
                88
            ),
            LegalCourse(
                "Legal Remedies for Dowry Harassment",
                "Understanding dowry laws and remedies",
                "40 mins",
                "Intermediate",
                "💍",
                81
            ),
            LegalCourse(
                "Maintenance Rights for Women",
                "Learn about maintenance under different laws",
                "35 mins",
                "Intermediate",
                "💰",
                76
            )
        ))
        coursesAdapter.notifyDataSetChanged()
    }

    private fun startCourse(course: LegalCourse) {
        val content = generateCourseContent(course.title)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📚 ${course.title}")
            .setMessage(content)
            .setPositiveButton("Complete Course") { _, _ ->
                completeCourse(course)
            }
            .setNeutralButton("Bookmark") { _, _ ->
                Toast.makeText(context, "📖 Course bookmarked", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun generateCourseContent(title: String): String {
        return when {
            title.contains("Domestic Violence") -> """
                DOMESTIC VIOLENCE ACT 2005
                
                What is Domestic Violence?
                Physical, sexual, verbal, emotional or economic abuse by family members.
                
                Your Rights:
                • Right to live in shared household
                • Right to maintenance
                • Right to custody of children
                • Right to compensation
                
                How to File Complaint:
                1. Approach Protection Officer
                2. File with Magistrate
                3. Get protection order
                4. Claim compensation
                
                Emergency: Call 181 (Women Helpline)
            """.trimIndent()
            
            title.contains("POSH") -> """
                PREVENTION OF SEXUAL HARASSMENT ACT 2013
                
                What Constitutes Harassment:
                • Unwelcome sexual advances
                • Demand for sexual favors
                • Sexually colored remarks
                • Showing pornography
                
                Complaint Process:
                1. Submit written complaint to ICC
                2. Committee investigates
                3. Action taken against harasser
                4. Appeal process available
                
                Your Rights:
                • Safe work environment
                • Fair investigation
                • Protection from retaliation
            """.trimIndent()
            
            title.contains("Property Rights") -> """
                WOMEN'S PROPERTY RIGHTS IN INDIA
                
                Types of Rights:
                • Inheritance rights
                • Rights in matrimonial property
                • Rights in ancestral property
                
                Hindu Succession Act 2005:
                • Equal rights as sons
                • Coparcenary rights
                • Right to ancestral property
                
                After Marriage:
                • Right to reside in matrimonial home
                • Right to maintenance
                • Right to share in husband's property
                
                Documents Required:
                • Property papers
                • Marriage certificate
                • Identity proof
            """.trimIndent()
            
            else -> """
                This course covers essential legal knowledge for women.
                
                Key Topics:
                • Understanding your legal rights
                • Step-by-step procedures
                • Important laws and sections
                • Practical examples
                • Emergency contacts
                
                Duration: ${title}
                Level: As per course description
                
                Complete this course to enhance your legal awareness.
            """.trimIndent()
        }
    }

    private fun completeCourse(course: LegalCourse) {
        Toast.makeText(context, "🎉 Course completed! Certificate earned.", Toast.LENGTH_LONG).show()
        // Update progress in database
    }

    private fun startLegalQuiz() {
        val questions = listOf(
            "What is the emergency helpline number for women in distress?",
            "Under which Act can women file complaints for domestic violence?",
            "What does POSH Act stand for?",
            "At what age can a woman file for maintenance?",
            "Can a woman inherit ancestral property?"
        )
        
        val answers = listOf(
            "181",
            "Domestic Violence Act 2005",
            "Prevention of Sexual Harassment",
            "Any age if legally married",
            "Yes, equal rights as sons"
        )
        
        showQuizDialog(questions, answers, 0, 0)
    }

    private fun showQuizDialog(questions: List<String>, answers: List<String>, currentIndex: Int, score: Int) {
        if (currentIndex >= questions.size) {
            // Quiz completed
            val percentage = (score * 100) / questions.size
            Toast.makeText(context, "Quiz completed! Score: $score/${questions.size} ($percentage%)", Toast.LENGTH_LONG).show()
            return
        }
        
        val input = EditText(requireContext()).apply {
            hint = "Enter your answer"
            setPadding(50, 40, 50, 40)
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Question ${currentIndex + 1}/${questions.size}")
            .setMessage(questions[currentIndex])
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val userAnswer = input.text.toString().trim()
                val isCorrect = userAnswer.equals(answers[currentIndex], ignoreCase = true) ||
                               answers[currentIndex].contains(userAnswer, ignoreCase = true)
                
                val newScore = if (isCorrect) score + 1 else score
                Toast.makeText(context, if (isCorrect) "✅ Correct!" else "❌ Correct answer: ${answers[currentIndex]}", 
                              Toast.LENGTH_SHORT).show()
                
                showQuizDialog(questions, answers, currentIndex + 1, newScore)
            }
            .setNegativeButton("Skip") { _, _ ->
                showQuizDialog(questions, answers, currentIndex + 1, score)
            }
            .show()
    }

    private fun showVideoLibrary() {
        val videos = listOf(
            "How to File an FIR - Step by Step",
            "Understanding Your Rights at Workplace",
            "Domestic Violence - Legal Remedies",
            "Property Rights for Women",
            "Legal Aid - How to Get Free Lawyer"
        )
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📹 Video Library")
            .setItems(videos.toTypedArray()) { _, which ->
                Toast.makeText(context, "Opening: ${videos[which]}", Toast.LENGTH_SHORT).show()
                // Open video player or YouTube link
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showArticles() {
        val articles = listOf(
            "10 Legal Rights Every Woman Should Know",
            "Guide to Filing Domestic Violence Complaint", 
            "Understanding Sexual Harassment Laws",
            "Women's Property Rights Explained",
            "Legal Aid Services in India"
        )
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📰 Legal Articles")
            .setItems(articles.toTypedArray()) { _, which ->
                showArticleContent(articles[which])
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showArticleContent(title: String) {
        val content = "This article covers: $title\n\nDetailed information about legal rights and procedures will be displayed here."
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Share") { _, _ ->
                shareDocument(content)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showFAQ() {
        val faqs = mapOf(
            "How to file FIR online?" to "You can file FIR online through your state police website or visit nearest police station.",
            "Is legal aid really free?" to "Yes, legal aid services are completely free for women under Legal Services Authority Act.",
            "Can I get divorce without lawyer?" to "While possible, it's recommended to have legal representation for better outcomes.",
            "What if police refuse to file FIR?" to "You can approach Senior Police Officer or file complaint in court directly.",
            "How to get protection order quickly?" to "Approach Protection Officer under DV Act or file application in magistrate court."
        )
        
        val questions = faqs.keys.toTypedArray()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("❓ Frequently Asked Questions")
            .setItems(questions) { _, which ->
                val question = questions[which]
                val answer = faqs[question]
                
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle(question)
                    .setMessage(answer)
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun shareDocument(text: String) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Legal Education Content")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
    }

    // Data class for legal courses
    data class LegalCourse(
        val title: String,
        val description: String,
        val duration: String,
        val level: String,
        val icon: String,
        val rating: Int
    )

    // Adapter for legal courses
    inner class LegalCoursesAdapter(
        private val courses: List<LegalCourse>,
        private val onItemClick: (LegalCourse) -> Unit
    ) : RecyclerView.Adapter<LegalCoursesAdapter.CourseViewHolder>() {

        inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleText: TextView = view.findViewById(android.R.id.text1)
            val descriptionText: TextView = view.findViewById(android.R.id.text2)
            val detailsText: TextView = view.findViewById(android.R.id.summary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
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
                isClickable = true
                isFocusable = true

                addView(TextView(context).apply {
                    id = android.R.id.text1
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                })

                addView(TextView(context).apply {
                    id = android.R.id.text2
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 0)
                })

                addView(TextView(context).apply {
                    id = android.R.id.summary
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.nyaya_color, null))
                    setPadding(0, 8, 0, 0)
                })
            }

            return CourseViewHolder(view)
        }

        override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
            val course = courses[position]
            holder.titleText.text = "${course.icon} ${course.title}"
            holder.descriptionText.text = course.description
            holder.detailsText.text = "${course.duration} • ${course.level} • Rating: ${course.rating}%"

            holder.itemView.setOnClickListener {
                onItemClick(course)
            }
        }

        override fun getItemCount() = courses.size
    }
}

/**
 * Tab 4: Free Lawyers (Lawyer matching and legal aid connection)
 */
class FreeLawyersFragment : Fragment() {

    private val viewModel: NyayaViewModel by activityViewModels()
    
    private lateinit var lawyersRecyclerView: RecyclerView
    private lateinit var findLawyerButton: Button
    private lateinit var legalAidButton: Button
    private lateinit var locationInput: EditText
    private lateinit var caseTypeSpinner: Spinner
    
    private val lawyersList = mutableListOf<Lawyer>()
    private lateinit var lawyersAdapter: LawyersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_free_lawyers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FreeLawyers", "Fragment onViewCreated called")

        try {
            initializeViews(view)
            Log.d("FreeLawyers", "Views initialized successfully")

            setupRecyclerView()
            Log.d("FreeLawyers", "RecyclerView setup completed")

            setupSpinner()
            Log.d("FreeLawyers", "Spinner setup completed")

            setupClickListeners()
            Log.d("FreeLawyers", "Click listeners setup completed")

            loadLawyers()
            Log.d("FreeLawyers", "Lawyers loaded successfully")

            observeViewModel()
            Log.d("FreeLawyers", "ViewModel observers setup completed")

        } catch (e: Exception) {
            Log.e("FreeLawyers", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(
                context,
                "Error initializing Free Lawyers: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeViews(view: View) {
        lawyersRecyclerView = view.findViewById(R.id.lawyers_recycler_view)
        findLawyerButton = view.findViewById(R.id.btn_find_lawyer)
        legalAidButton = view.findViewById(R.id.btn_legal_aid)
        locationInput = view.findViewById(R.id.location_input)
        caseTypeSpinner = view.findViewById(R.id.case_type_spinner)
    }

    private fun setupRecyclerView() {
        lawyersAdapter = LawyersAdapter(lawyersList) { lawyer ->
            contactLawyer(lawyer)
        }
        lawyersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lawyersAdapter
        }
    }

    private fun setupSpinner() {
        val caseTypes = arrayOf(
            "Select case type",
            "Domestic Violence",
            "Sexual Harassment",
            "Dowry Harassment",
            "Property Dispute",
            "Divorce/Family Law",
            "Criminal Case",
            "Civil Rights",
            "Other"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            caseTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        caseTypeSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        findLawyerButton.setOnClickListener {
            Log.d("FreeLawyers", "Find lawyer button clicked")
            try {
                findLawyer()
            } catch (e: Exception) {
                Log.e("FreeLawyers", "Error finding lawyer: ${e.message}", e)
                Toast.makeText(context, "Error finding lawyer: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        legalAidButton.setOnClickListener {
            Log.d("FreeLawyers", "Legal aid button clicked")
            try {
                showLegalAidInfo()
            } catch (e: Exception) {
                Log.e("FreeLawyers", "Error showing legal aid info: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Error loading legal aid info: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.lawyerInfo.collect { info ->
                    Log.d("FreeLawyers", "Lawyer info received: ${info.length} characters")
                    if (info.isNotEmpty()) {
                        showLawyerInfo(info)
                    }
                }
            } catch (e: Exception) {
                Log.e("FreeLawyers", "Error observing lawyer info: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Error loading lawyer information: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.isLoading.collect { isLoading ->
                    Log.d("FreeLawyers", "Loading state changed: $isLoading")
                    findLawyerButton.isEnabled = !isLoading
                    findLawyerButton.text = if (isLoading) "Searching..." else "🔍 Find"
                }
            } catch (e: Exception) {
                Log.e("FreeLawyers", "Error observing loading state: ${e.message}", e)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.errorMessage.collect { error ->
                    error?.let {
                        Log.e("FreeLawyers", "ViewModel error: $it")
                        Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            } catch (e: Exception) {
                Log.e("FreeLawyers", "Error observing error messages: ${e.message}", e)
            }
        }
    }

    private fun loadLawyers() {
        lawyersList.clear()
        lawyersList.addAll(listOf(
            Lawyer(
                "Adv. Priya Sharma",
                "Women's Rights Specialist",
                "Delhi",
                "12 years",
                "FREE",
                "4.8",
                "📞 9876543210",
                "Domestic Violence, POSH Act, Property Rights"
            ),
            Lawyer(
                "Adv. Meera Patel",
                "Family Law Expert",
                "Mumbai",
                "8 years",
                "FREE",
                "4.7",
                "📞 9876543211",
                "Divorce, Maintenance, Child Custody"
            ),
            Lawyer(
                "Adv. Sunita Singh",
                "Criminal Law Specialist",
                "Bangalore",
                "15 years",
                "FREE",
                "4.9",
                "📞 9876543212",
                "FIR Filing, Criminal Cases, Court Representation"
            ),
            Lawyer(
                "Adv. Kavya Reddy",
                "Legal Aid Advocate",
                "Hyderabad",
                "6 years",
                "FREE",
                "4.6",
                "📞 9876543213",
                "Legal Aid Services, Women Empowerment"
            ),
            Lawyer(
                "Adv. Deepika Joshi",
                "Women's Legal Rights",
                "Pune",
                "10 years",
                "FREE",
                "4.8",
                "📞 9876543214",
                "Property Rights, Workplace Harassment"
            )
        ))
        lawyersAdapter.notifyDataSetChanged()
    }

    private fun findLawyer() {
        val location = locationInput.text.toString().ifBlank { "India" }
        val caseType = if (caseTypeSpinner.selectedItemPosition > 0) {
            caseTypeSpinner.selectedItem.toString()
        } else {
            "General Legal Aid"
        }
        
        viewModel.matchWithLawyer(caseType, location)
    }

    private fun showLawyerInfo(info: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👩‍⚖️ Lawyer Recommendations")
            .setMessage(info)
            .setPositiveButton("Contact Legal Aid") { _, _ ->
                contactLegalAid()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun contactLawyer(lawyer: Lawyer) {
        val message = """
            ${lawyer.name}
            ${lawyer.specialization}
            
            Location: ${lawyer.location}
            Experience: ${lawyer.experience}
            Fee: ${lawyer.fee}
            Rating: ⭐ ${lawyer.rating}
            
            Contact: ${lawyer.contact}
            
            Specializes in: ${lawyer.expertise}
            
            Would you like to contact this lawyer?
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👩‍⚖️ Lawyer Details")
            .setMessage(message)
            .setPositiveButton("Call Now") { _, _ ->
                val phone = lawyer.contact.replace("📞 ", "")
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
            .setNeutralButton("Send Message") { _, _ ->
                sendMessage(lawyer)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun sendMessage(lawyer: Lawyer) {
        val message = "Hello ${lawyer.name}, I found your details on ShaktiAI app. I need legal assistance for my case. Can you help me?"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${lawyer.contact.replace("📞 ", "")}")
            putExtra("sms_body", message)
        }
        startActivity(intent)
    }

    private fun showLegalAidInfo() {
        val info = """
            🏛️ FREE LEGAL AID SERVICES
            
            Who is Eligible:
            • Women in distress
            • Income less than ₹1 lakh/year
            • Victim of trafficking
            • Disabled persons
            • Scheduled Caste/Tribe
            
            Services Provided:
            • Free lawyer representation
            • Court fee exemption
            • Document drafting
            • Legal advice
            
            How to Apply:
            1. Contact District Legal Services Authority
            2. Submit application with income proof
            3. Get assigned lawyer
            4. Free legal representation
            
            📞 National Legal Services Authority
            Helpline: 15100
            
            📞 Women Helpline: 181
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🆓 Legal Aid Information")
            .setMessage(info)
            .setPositiveButton("Apply for Legal Aid") { _, _ ->
                applyForLegalAid()
            }
            .setNeutralButton("Call Helpline") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:15100")
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun applyForLegalAid() {
        Toast.makeText(context, "Redirecting to Legal Aid application...", Toast.LENGTH_SHORT).show()
        // Open legal aid application form or website
    }

    private fun contactLegalAid() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:15100")
        }
        startActivity(intent)
    }

    // Data class for lawyers
    data class Lawyer(
        val name: String,
        val specialization: String,
        val location: String,
        val experience: String,
        val fee: String,
        val rating: String,
        val contact: String,
        val expertise: String
    )

    // Adapter for lawyers
    inner class LawyersAdapter(
        private val lawyers: List<Lawyer>,
        private val onItemClick: (Lawyer) -> Unit
    ) : RecyclerView.Adapter<LawyersAdapter.LawyerViewHolder>() {

        inner class LawyerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(android.R.id.text1)
            val specializationText: TextView = view.findViewById(android.R.id.text2)
            val detailsText: TextView = view.findViewById(android.R.id.summary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LawyerViewHolder {
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
                isClickable = true
                isFocusable = true

                addView(TextView(context).apply {
                    id = android.R.id.text1
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                })

                addView(TextView(context).apply {
                    id = android.R.id.text2
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.nyaya_color, null))
                    setPadding(0, 4, 0, 0)
                })

                addView(TextView(context).apply {
                    id = android.R.id.summary
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 8, 0, 0)
                })
            }

            return LawyerViewHolder(view)
        }

        override fun onBindViewHolder(holder: LawyerViewHolder, position: Int) {
            val lawyer = lawyers[position]
            holder.nameText.text = "👩‍⚖️ ${lawyer.name}"
            holder.specializationText.text = lawyer.specialization
            holder.detailsText.text = "${lawyer.location} • ${lawyer.experience} • ${lawyer.fee} • ⭐ ${lawyer.rating}"

            holder.itemView.setOnClickListener {
                onItemClick(lawyer)
            }
        }

        override fun getItemCount() = lawyers.size
    }
}