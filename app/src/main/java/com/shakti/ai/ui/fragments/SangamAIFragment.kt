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
import com.shakti.ai.viewmodel.SangamViewModel
import kotlinx.coroutines.launch

class SangamAIFragment : Fragment() {

    private val viewModel: SangamViewModel by activityViewModels()

    private lateinit var btnCareerGuidance: Button
    private lateinit var btnBusinessAdvice: Button
    private lateinit var btnTechSkills: Button
    private lateinit var btnFinancialPlanning: Button
    private lateinit var btnDomesticViolenceSupport: Button
    private lateinit var btnSingleMothers: Button
    private lateinit var btnCareerWomen: Button
    private lateinit var btnEntrepreneurs: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sangam_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        btnCareerGuidance = view.findViewById(R.id.btn_career_guidance)
        btnBusinessAdvice = view.findViewById(R.id.btn_business_advice)
        btnTechSkills = view.findViewById(R.id.btn_tech_skills)
        btnFinancialPlanning = view.findViewById(R.id.btn_financial_planning)
        btnDomesticViolenceSupport = view.findViewById(R.id.btn_domestic_violence_support)
        btnSingleMothers = view.findViewById(R.id.btn_single_mothers)
        btnCareerWomen = view.findViewById(R.id.btn_career_women)
        btnEntrepreneurs = view.findViewById(R.id.btn_entrepreneurs)
    }

    private fun setupClickListeners() {
        btnCareerGuidance.setOnClickListener {
            viewModel.findMentors("Career Guidance")
        }

        btnBusinessAdvice.setOnClickListener {
            viewModel.findMentors("Business Advice")
        }

        btnTechSkills.setOnClickListener {
            viewModel.findMentors("Tech Skills")
        }

        btnFinancialPlanning.setOnClickListener {
            viewModel.findMentors("Financial Planning")
        }

        btnDomesticViolenceSupport.setOnClickListener {
            joinCommunity("Domestic Violence Support")
        }

        btnSingleMothers.setOnClickListener {
            joinCommunity("Single Mothers Circle")
        }

        btnCareerWomen.setOnClickListener {
            joinCommunity("Career Women Network")
        }

        btnEntrepreneurs.setOnClickListener {
            joinCommunity("Women Entrepreneurs")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mentorMatches.collect { mentors ->
                if (mentors.isNotEmpty()) {
                    showMentorMatches(mentors)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reputationTokens.collect { tokens ->
                // Update UI with reputation tokens
            }
        }
    }

    private fun showMentorMatches(mentors: List<SangamViewModel.MentorProfile>) {
        val mentorList = mentors.joinToString("\n\n") {
            "${it.name}\n${it.expertise} • ${it.experience}\n⭐ ${it.rating} • ${it.location}"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Recommended Mentors")
            .setMessage(mentorList)
            .setPositiveButton("Connect") { _, _ ->
                Toast.makeText(context, "✅ Connection request sent!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun joinCommunity(communityName: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Join $communityName")
            .setMessage("Connect with women who share similar experiences and support each other.")
            .setPositiveButton("Join") { _, _ ->
                viewModel.joinCommunity(communityName)
                Toast.makeText(context, "✅ Joined $communityName!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
