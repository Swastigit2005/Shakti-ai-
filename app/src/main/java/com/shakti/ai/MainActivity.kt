package com.shakti.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shakti.ai.ui.fragments.*
import com.shakti.ai.viewmodel.RakshaViewModel

/**
 * MainActivity - Main entry point for ShaktiAI 3.0
 *
 * Features:
 * - ViewPager2 with 6 AI module tabs
 * - Material Design TabLayout
 * - Smooth tab transitions
 * - Fragment state preservation
 * - Global Emergency SOS Button
 *
 * Module Order: Raksha → Swasthya → Sathi → Sangam → Nyaya → Dhan Shakti
 *
 * Note: Raksha now includes both Domestic Violence Support AND Physical Safety (formerly Suraksha/Guardian)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fabSos: ExtendedFloatingActionButton

    private val rakshaViewModel: RakshaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        fabSos = findViewById(R.id.fab_sos)

        // Setup ViewPager2 with adapter for 6 AI modules
        val adapter = ShaktiPagerAdapter(this)
        viewPager.adapter = adapter

        // IMPORTANT: Enable nested scrolling and reduce sensitivity for better vertical scroll
        viewPager.isUserInputEnabled = true
        viewPager.offscreenPageLimit = 1

        // Reduce touch slop to make vertical scrolling easier inside fragments
        try {
            val recyclerView = viewPager.getChildAt(0)
            recyclerView?.apply {
                overScrollMode = ViewPager2.OVER_SCROLL_NEVER
                // This allows nested scrolling to work properly
                (this as? androidx.recyclerview.widget.RecyclerView)?.apply {
                    isNestedScrollingEnabled = true
                }
            }
        } catch (e: Exception) {
            // Ignore if optimization fails
        }

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "🛡️ Raksha"        // Unified: DV support + Physical safety (merged with Suraksha)
                1 -> "❤️ Swasthya"     // Health & period tracking
                2 -> "💬 Sathi"         // Mental health support
                3 -> "👥 Sangam"        // Community connections
                4 -> "⚖️ Nyaya"        // Legal advisor (includes Education access)
                5 -> "💰 Dhan Shakti"   // Financial literacy
                else -> "SHAKTI"
            }
        }.attach()

        // Setup Emergency SOS Button
        setupEmergencySOSButton()

        // Handle back press with new API
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If not on first page, go back to first page
                if (viewPager.currentItem == 0) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    viewPager.currentItem = viewPager.currentItem - 1
                }
            }
        })
    }

    private fun setupEmergencySOSButton() {
        fabSos.setOnClickListener {
            showEmergencySOSConfirmation()
        }
    }

    private fun showEmergencySOSConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚨 TRIGGER EMERGENCY SOS?")
            .setMessage(
                """
                This will immediately activate FULL emergency protocol:
                
                ✅ Alert ALL nearby guardians
                ✅ Start auto-recording evidence
                ✅ Activate flashlight strobe
                ✅ Share location continuously
                ✅ Notify emergency contacts
                ✅ Log to blockchain
                ✅ Prepare to call emergency services
                
                ⚠️ ONLY USE IN REAL EMERGENCIES!
                
                Are you in immediate danger?
            """.trimIndent()
            )
            .setPositiveButton("YES - ACTIVATE SOS") { _, _ ->
                activateEmergencySOS()
            }
            .setNeutralButton("Call 181 Now") { _, _ ->
                callEmergencyNumber("181")
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun activateEmergencySOS() {
        // Trigger emergency protocol through RakshaViewModel
        rakshaViewModel.triggerManualSOS()

        // Show emergency options dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚨 EMERGENCY SOS ACTIVATED!")
            .setMessage(
                """
                Full emergency protocol is now active:
                
                ✅ Evidence recording started
                ✅ Nearby guardians alerted
                ✅ Emergency contacts notified
                ✅ Location being shared
                ✅ Flashlight strobe activated
                ✅ Blockchain logging active
                
                HELP IS ON THE WAY!
                
                Call emergency services now?
            """.trimIndent()
            )
            .setPositiveButton("Call 100 (Police)") { _, _ ->
                callEmergencyNumber("100")
            }
            .setNeutralButton("Call 181 (Women's Helpline)") { _, _ ->
                callEmergencyNumber("181")
            }
            .setNegativeButton("I'm Safe Now") { _, _ ->
                confirmCancelEmergency()
            }
            .setCancelable(false)
            .show()
    }

    private fun callEmergencyNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    private fun confirmCancelEmergency() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancel Emergency?")
            .setMessage("Are you sure you're safe now? This will stop all emergency protocols.")
            .setPositiveButton("Yes, I'm Safe") { _, _ ->
                rakshaViewModel.resetEmergencyState()
                android.widget.Toast.makeText(
                    this,
                    "✅ Emergency cancelled. Stay safe!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("No, Keep Active", null)
            .show()
    }
}

/**
 * ShaktiPagerAdapter - Manages fragments for 6 AI modules
 *
 * Module Order: Raksha → Swasthya → Sathi → Sangam → Nyaya → Dhan Shakti
 *
 * Raksha (Unified Safety Module) includes:
 * - Domestic Violence Support (evidence, safe houses, legal aid)
 * - Physical Safety (AI threat detection, mesh network, emergency SOS)
 * - All features from former Suraksha/Guardian module
 */
class ShaktiPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RakshaAIFragment()     // Raksha - Unified: DV support + Physical safety
            1 -> SwasthyaAIFragment()   // Swasthya - Health & period tracking
            2 -> SathiAIFragment()      // Sathi - Mental health support
            3 -> SangamAIFragment()     // Sangam - Community connections
            4 -> NyayaAIFragment()      // Nyaya - Legal advisor (with Education access)
            5 -> DhanShaktiAIFragment() // Dhan Shakti - Financial literacy
            else -> RakshaAIFragment() // Default to Raksha
        }
    }
}
