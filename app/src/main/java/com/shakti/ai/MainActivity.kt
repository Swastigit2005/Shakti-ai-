package com.shakti.ai

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shakti.ai.ui.fragments.*

/**
 * MainActivity - Main entry point for ShaktiAI 3.0
 *
 * Features:
 * - ViewPager2 with 7 AI module tabs (Gyaan AI accessible via Nyaya AI)
 * - Material Design TabLayout
 * - Smooth tab transitions
 * - Fragment state preservation
 *
 * Module Order: Suraksha → Raksha → Swasthya → Sathi → Sangam → Nyaya → Dhan Shakti
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        // Setup ViewPager2 with adapter for 7 AI modules (Gyaan AI accessible via Nyaya AI)
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
                0 -> "🛡️ Suraksha"      // Physical safety (Guardian AI)
                1 -> "🔒 Raksha"        // DV support
                2 -> "❤️ Swasthya"     // Health & period tracking
                3 -> "💬 Sathi"         // Mental health support
                4 -> "👥 Sangam"        // Community connections
                5 -> "⚖️ Nyaya"        // Legal advisor (includes Education access)
                6 -> "💰 Dhan Shakti"   // Financial literacy
                // Note: 📚 Gyaan (Education) is now accessible via Nyaya AI
                else -> "SHAKTI"
            }

            // Set custom icon if needed (icons not yet created)
            // TODO: Add icon drawables and uncomment
            /*
            tab.setIcon(
                when (position) {
                    0 -> R.drawable.ic_shield
                    1 -> R.drawable.ic_protection
                    2 -> R.drawable.ic_health
                    3 -> R.drawable.ic_mental_health
                    4 -> R.drawable.ic_community
                    5 -> R.drawable.ic_legal
                    6 -> R.drawable.ic_finance
                    else -> null
                }
            )
            */
        }.attach()

        // Remove page transformer to avoid interfering with scrolling
        // viewPager.setPageTransformer { page, position ->
        //     page.apply {
        //         translationX = -position * width
        //         alpha = 1 - kotlin.math.abs(position)
        //     }
        // }

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
}

/**
 * ShaktiPagerAdapter - Manages fragments for 7 AI modules
 * Module Order: Suraksha → Raksha → Swasthya → Sathi → Sangam → Nyaya → Dhan Shakti
 * Note: Gyaan AI (Education) is accessible via Nyaya AI's "Go to Education" button
 */
class ShaktiPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 7

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GuardianAIFragment()   // Suraksha - Physical safety
            1 -> RakshaAIFragment()     // Raksha - DV support
            2 -> SwasthyaAIFragment()   // Swasthya - Health & period tracking
            3 -> SathiAIFragment()      // Sathi - Mental health support
            4 -> SangamAIFragment()     // Sangam - Community connections
            5 -> NyayaAIFragment()      // Nyaya - Legal advisor (with Education access)
            6 -> DhanShaktiAIFragment() // Dhan Shakti - Financial literacy
            // Gyaan AI (Education) removed from main tabs - accessible via Nyaya AI
            else -> GuardianAIFragment() // Default to first module
        }
    }
}
