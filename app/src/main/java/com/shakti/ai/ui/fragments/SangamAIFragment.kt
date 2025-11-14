package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shakti.ai.R
import com.shakti.ai.ui.adapters.FeatureCard
import com.shakti.ai.ui.adapters.FeatureCardAdapter
import com.shakti.ai.viewmodel.SangamViewModel
import kotlinx.coroutines.launch

class SangamAIFragment : Fragment() {

    private val viewModel: SangamViewModel by activityViewModels()

    // UI Elements
    private lateinit var rvFeatureCards: RecyclerView
    private lateinit var tvReputationTokens: TextView
    private lateinit var tvBadgeLevel: TextView
    private lateinit var btnViewAll: Button
    private lateinit var btnCareerGuidance: Button
    private lateinit var btnBusinessAdvice: Button
    private lateinit var btnTechSkills: Button
    private lateinit var btnFinancialPlanning: Button
    private lateinit var btnDomesticViolenceSupport: LinearLayout
    private lateinit var btnSingleMothers: LinearLayout
    private lateinit var btnCareerWomen: LinearLayout
    private lateinit var btnEntrepreneurs: LinearLayout
    private lateinit var btnUnlockLeadership: Button
    private lateinit var mentorsRecyclerView: RecyclerView

    // Adapters
    private lateinit var featureCardAdapter: FeatureCardAdapter

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
        setupFeatureCards()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        rvFeatureCards = view.findViewById(R.id.rv_feature_cards)
        tvReputationTokens = view.findViewById(R.id.tv_reputation_tokens)
        tvBadgeLevel = view.findViewById(R.id.tv_badge_level)
        btnViewAll = view.findViewById(R.id.btn_view_all)
        btnCareerGuidance = view.findViewById(R.id.btn_career_guidance)
        btnBusinessAdvice = view.findViewById(R.id.btn_business_advice)
        btnTechSkills = view.findViewById(R.id.btn_tech_skills)
        btnFinancialPlanning = view.findViewById(R.id.btn_financial_planning)
        btnDomesticViolenceSupport = view.findViewById(R.id.btn_domestic_violence_support)
        btnSingleMothers = view.findViewById(R.id.btn_single_mothers)
        btnCareerWomen = view.findViewById(R.id.btn_career_women)
        btnEntrepreneurs = view.findViewById(R.id.btn_entrepreneurs)
        btnUnlockLeadership = view.findViewById(R.id.btn_unlock_leadership)
        mentorsRecyclerView = view.findViewById(R.id.mentors_recycler_view)

        // Set up mentors recycler view
        mentorsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupFeatureCards() {
        val features = listOf(
            FeatureCard(
                title = "Meet friends like you, near you.",
                description = "Connect with women nearby who share your interests",
                backgroundColor = "#FFE5EC",
                iconResId = android.R.drawable.ic_menu_myplaces
            ),
            FeatureCard(
                title = "Find meaningful connections.",
                description = "Build lasting friendships and support networks",
                backgroundColor = "#FFC5DD",
                iconResId = android.R.drawable.ic_menu_compass
            ),
            FeatureCard(
                title = "Chat and get to know each other.",
                description = "Safe, private conversations with verified women",
                backgroundColor = "#E8D5F2",
                iconResId = android.R.drawable.ic_menu_send
            ),
            FeatureCard(
                title = "Grow together.",
                description = "Share experiences, learn, and support each other",
                backgroundColor = "#D5E8F2",
                iconResId = android.R.drawable.ic_menu_info_details
            )
        )

        featureCardAdapter = FeatureCardAdapter(features) { feature ->
            onFeatureCardClicked(feature)
        }

        rvFeatureCards.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featureCardAdapter
        }
    }

    private fun setupClickListeners() {
        btnViewAll.setOnClickListener {
            showProfileDialog()
        }

        btnCareerGuidance.setOnClickListener {
            viewModel.findMentors("Career Guidance")
            Toast.makeText(context, "Finding career mentors...", Toast.LENGTH_SHORT).show()
        }

        btnBusinessAdvice.setOnClickListener {
            viewModel.findMentors("Business Advice")
            Toast.makeText(context, "Finding business mentors...", Toast.LENGTH_SHORT).show()
        }

        btnTechSkills.setOnClickListener {
            viewModel.findMentors("Tech Skills")
            Toast.makeText(context, "Finding tech mentors...", Toast.LENGTH_SHORT).show()
        }

        btnFinancialPlanning.setOnClickListener {
            viewModel.findMentors("Financial Planning")
            Toast.makeText(context, "Finding financial mentors...", Toast.LENGTH_SHORT).show()
        }

        btnDomesticViolenceSupport.setOnClickListener {
            joinCommunity(
                "Domestic Violence Support",
                "A safe, confidential space for survivors to connect and heal together."
            )
        }

        btnSingleMothers.setOnClickListener {
            joinCommunity(
                "Single Mothers Circle",
                "Connect with other single moms for support, advice, and friendship."
            )
        }

        btnCareerWomen.setOnClickListener {
            joinCommunity(
                "Career Women Network",
                "Network with professional women, share career advice, and grow together."
            )
        }

        btnEntrepreneurs.setOnClickListener {
            joinCommunity(
                "Women Entrepreneurs",
                "Build your business alongside other women entrepreneurs."
            )
        }

        btnUnlockLeadership.setOnClickListener {
            showLeadershipInfo()
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
                tvReputationTokens.text = "$tokens Reputation Tokens"

                // Update badge based on tokens
                val badge = when {
                    tokens >= 1000 -> "⭐ Leadership"
                    tokens >= 500 -> "⭐ Expert"
                    tokens >= 200 -> "⭐ Contributor"
                    else -> "⭐ Member"
                }
                tvBadgeLevel.text = badge
            }
        }
    }

    private fun onFeatureCardClicked(feature: FeatureCard) {
        when {
            feature.title.contains("Meet friends") -> {
                Toast.makeText(
                    context,
                    "Finding women near you with similar interests...",
                    Toast.LENGTH_SHORT
                ).show()
                // Show nearby women
            }

            feature.title.contains("meaningful connections") -> {
                Toast.makeText(
                    context,
                    "AI matching you with compatible connections...",
                    Toast.LENGTH_SHORT
                ).show()
                // Show recommended connections
            }

            feature.title.contains("Chat") -> {
                Toast.makeText(context, "Opening chat...", Toast.LENGTH_SHORT).show()
                // Open chat interface
            }

            feature.title.contains("Grow") -> {
                Toast.makeText(context, "Explore growth opportunities...", Toast.LENGTH_SHORT)
                    .show()
                // Show growth resources
            }
        }
    }

    private fun showProfileDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Your Sangam Profile")
            .setMessage(
                """
                👤 Profile Stats:
                
                🌟 Reputation: 850 tokens
                🏆 Badge: Expert Level
                👥 Circles: 3 joined
                💬 Connections: 12 active
                🎯 Mentors: 5 matched
                
                Keep engaging to unlock Leadership badge at 1000 tokens!
                
                How to earn tokens:
                • Help other women: +50 tokens
                • Join circles: +20 tokens
                • Complete your profile: +30 tokens
                • Active for 7 days: +100 tokens
                """.trimIndent()
            )
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun showMentorMatches(mentors: List<SangamViewModel.MentorProfile>) {
        val mentorList = mentors.joinToString("\n\n") {
            "👤 ${it.name}\n${it.expertise} • ${it.experience}\n⭐ ${it.rating} • 📍 ${it.location}\n"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("✨ Recommended Mentors for You")
            .setMessage(
                """
                Based on your profile and goals:
                
                $mentorList
                
                All mentors are verified and blockchain-certified.
                """.trimIndent()
            )
            .setPositiveButton("Connect with Mentor") { _, _ ->
                Toast.makeText(context, "✅ Connection request sent!", Toast.LENGTH_SHORT).show()
                // Increment reputation
                viewModel.earnReputationTokens(20)
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    private fun joinCommunity(communityName: String, description: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Join $communityName")
            .setMessage(
                """
                $description
                
                This is a safe, moderated space where:
                ✅ All members are verified
                ✅ Conversations are confidential
                ✅ Support is available 24/7
                ✅ Blockchain ensures privacy
                
                Would you like to join?
                """.trimIndent()
            )
            .setPositiveButton("Join Circle") { _, _ ->
                viewModel.joinCommunity(communityName)
                Toast.makeText(context, "✅ Welcome to $communityName!", Toast.LENGTH_SHORT).show()
                // Earn tokens for joining
                viewModel.earnReputationTokens(20)
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    private fun showLeadershipInfo() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🏆 Leadership Badge")
            .setMessage(
                """
                You're 150 tokens away from Leadership!
                
                Leadership benefits:
                🌟 Start your own circles
                🎯 Featured mentor profile
                💎 Exclusive networking events
                🔒 Priority support
                📢 Verified leadership badge
                
                How to earn 150 tokens:
                • Help 3 women: +150 tokens
                • Mentor someone: +100 tokens
                • Host a circle meeting: +75 tokens
                • Be active for 14 days: +50 tokens
                
                Keep supporting other women to unlock!
                """.trimIndent()
            )
            .setPositiveButton("Let's Do It!", null)
            .show()
    }
}
