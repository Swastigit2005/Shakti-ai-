package com.shakti.ai.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
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
import com.shakti.ai.viewmodel.SathiViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * SathiAIFragment - Mental Health Support Module with Three Tabs
 * 1. AI Companion
 * 2. Mental Health Dashboard
 * 3. Support Resources
 */
class SathiAIFragment : Fragment() {

    private val viewModel: SathiViewModel by activityViewModels()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sathi_ai, container, false)
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
        val adapter = SathiPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💬 AI Companion"
                1 -> "📊 Dashboard"
                2 -> "🆘 Resources"
                else -> ""
            }
        }.attach()
    }

    // ViewPager2 Adapter for the three tabs
    private inner class SathiPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SathiChatFragment()
                1 -> MentalHealthDashboardFragment()
                2 -> SupportResourcesFragment()
                else -> SathiChatFragment()
            }
        }
    }
}

/**
 * Tab 1: AI Companion Chat Interface
 */
class SathiChatFragment : Fragment() {

    // Use ViewModel for Gemini API integration
    private val viewModel: SathiViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences

    // UI Elements
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var voiceButton: Button
    private lateinit var uploadButton: Button
    private lateinit var breathingButton: Button
    private lateinit var gratitudeButton: Button
    private lateinit var chatRecyclerView: RecyclerView

    // Media recording
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null

    // Chat messages
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    // Permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.RECORD_AUDIO] == true -> {
                startVoiceRecording()
            }
            else -> {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleMediaUpload(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sathi_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("SathiAI", Context.MODE_PRIVATE)

        initializeViews(view)
        setupClickListeners()
        setupRecyclerView()
        observeViewModel()

        // Initialize Sathi AI session via ViewModel
        if (viewModel.chatMessages.value.isEmpty()) {
            viewModel.initializeSathiSession()
        }
    }

    private fun initializeViews(view: View) {
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.btn_send_message)
        voiceButton = view.findViewById(R.id.btn_voice_message)
        uploadButton = view.findViewById(R.id.btn_upload_media)
        breathingButton = view.findViewById(R.id.btn_breathing_exercise)
        gratitudeButton = view.findViewById(R.id.btn_gratitude_journal)
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        voiceButton.setOnClickListener {
            handleVoiceRecording()
        }

        uploadButton.setOnClickListener {
            openMediaPicker()
        }

        breathingButton.setOnClickListener {
            startBreathingExercise()
        }

        gratitudeButton.setOnClickListener {
            openGratitudeJournal()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun observeViewModel() {
        // Observe chat messages from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatMessages.collect { messages ->
                chatMessages.clear()
                messages.forEach { (sender, text) ->
                    chatMessages.add(
                        ChatMessage(
                            text = text,
                            isUser = sender == "User",
                            timestamp = SimpleDateFormat(
                                "h:mm a",
                                Locale.getDefault()
                            ).format(Date())
                        )
                    )
                }
                chatAdapter.notifyDataSetChanged()
                if (chatMessages.isNotEmpty()) {
                    chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                sendButton.isEnabled = !isLoading
                messageInput.isEnabled = !isLoading

                if (isLoading) {
                    sendButton.alpha = 0.5f
                    messageInput.hint = "AI is thinking..."
                } else {
                    sendButton.alpha = 1.0f
                    messageInput.hint = "Type your message..."
                }
            }
        }

        // Observe crisis detection
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isCrisisDetected.collect { isCrisis ->
                if (isCrisis) {
                    Toast.makeText(
                        context,
                        "⚠️ Crisis detected. Emergency resources available in Support tab.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isNotEmpty()) {
            messageInput.text.clear()
            // Send with default mood rating of 5
            viewModel.sendMessageToSathi(message, 5)
        } else {
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleVoiceRecording() {
        if (isRecording) {
            stopVoiceRecording()
        } else {
            checkAudioPermissionAndRecord()
        }
    }

    private fun checkAudioPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecording()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.RECORD_AUDIO)
                )
            }
        }
    }

    private fun startVoiceRecording() {
        try {
            audioFilePath =
                "${requireContext().externalCacheDir?.absolutePath}/audio_${System.currentTimeMillis()}.3gp"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }

            isRecording = true
            voiceButton.text = "⏹️ Stop Recording"
            voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(android.R.color.holo_red_light, null)
            )
            Toast.makeText(context, "🎤 Recording started...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopVoiceRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            voiceButton.text = "🎤 Voice Message"
            voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.sathi_color, null)
            )

            Toast.makeText(context, "✅ Recording saved!", Toast.LENGTH_SHORT).show()

            // Send voice message indicator via ViewModel
            viewModel.sendMessageToSathi("🎤 Voice message recorded", 5)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to stop recording: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickMediaLauncher.launch(intent)
    }

    private fun handleMediaUpload(uri: Uri) {
        Toast.makeText(context, "📎 Media uploaded: ${uri.lastPathSegment}", Toast.LENGTH_SHORT)
            .show()

        // Send media upload indicator via ViewModel
        viewModel.sendMessageToSathi(
            "📎 Shared an image. Can you help me understand my feelings about it?",
            5
        )
    }

    private fun startBreathingExercise() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("🫁 Breathing Exercise")
            .setMessage(
                """
                Let's practice the 4-7-8 breathing technique:
                
                1. Breathe in slowly through your nose for 4 seconds
                2. Hold your breath for 7 seconds
                3. Breathe out slowly through your mouth for 8 seconds
                4. Repeat 4-5 times
                
                Focus on your breath and let go of tension.
                This technique helps reduce anxiety and promote relaxation.
            """.trimIndent()
            )
            .setPositiveButton("Start") { _, _ ->
                // Send via ViewModel - Gemini API integration
                viewModel.sendMessageToSathi(
                    "I just completed a breathing exercise. How can this help me manage my stress better?",
                    6
                )
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun openGratitudeJournal() {
        val input = EditText(requireContext())
        input.hint = "What are you grateful for today?"
        input.setPadding(50, 40, 50, 40)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("💗 Gratitude Journal")
            .setMessage("Taking time to appreciate the good things in life can significantly improve your mood and mental well-being.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val gratitude = input.text.toString()
                if (gratitude.isNotEmpty()) {
                    // Send via ViewModel - Gemini API integration
                    viewModel.sendMessageToSathi(
                        "I'm grateful for: $gratitude. Can you help me understand why gratitude is important?",
                        7
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    // Data classes
    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: String
    )

    // Chat Adapter
    inner class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val messageText: TextView = view.findViewById(android.R.id.text1)
            val timeText: TextView = view.findViewById(android.R.id.text2)
            val card: CardView = view as CardView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val card = CardView(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                radius = 16f
                cardElevation = 4f
                isClickable = true
                isFocusable = true

                val layout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 20, 24, 20)

                    addView(TextView(context).apply {
                        id = android.R.id.text1
                        textSize = 14f
                        setTextColor(resources.getColor(R.color.text_primary, null))
                        setLineSpacing(4f, 1.1f)
                    })

                    addView(TextView(context).apply {
                        id = android.R.id.text2
                        textSize = 10f
                        setTextColor(resources.getColor(R.color.text_secondary, null))
                        setPadding(0, 8, 0, 0)
                    })
                }

                addView(layout)
            }

            return MessageViewHolder(card)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.messageText.text = message.text
            holder.timeText.text = message.timestamp

            // Different styling for user and AI messages
            if (message.isUser) {
                holder.card.setCardBackgroundColor(resources.getColor(R.color.sathi_color, null))
                holder.messageText.setTextColor(resources.getColor(R.color.white, null))
                holder.timeText.setTextColor(resources.getColor(R.color.white, null))
            } else {
                holder.card.setCardBackgroundColor(resources.getColor(R.color.white, null))
                holder.messageText.setTextColor(resources.getColor(R.color.text_primary, null))
                holder.timeText.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        override fun getItemCount() = messages.size
    }
}

/**
 * Tab 2: Mental Health Dashboard
 */
class MentalHealthDashboardFragment : Fragment() {

    private val viewModel: SathiViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    // UI Elements
    private lateinit var moodScore: TextView
    private lateinit var anxietyScore: TextView
    private lateinit var stressScore: TextView
    private lateinit var sleepScore: TextView
    private lateinit var conversationCount: TextView
    private lateinit var moodProgress: ProgressBar
    private lateinit var anxietyProgress: ProgressBar
    private lateinit var stressProgress: ProgressBar
    private lateinit var sleepProgress: ProgressBar
    private lateinit var moodHistoryRecycler: RecyclerView
    private lateinit var insightsText: TextView
    private lateinit var analyzeButton: Button
    private lateinit var exportButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mental_health_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("SathiAI", Context.MODE_PRIVATE)

        initializeViews(view)
        setupClickListeners()
        loadDashboardData()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        moodScore = view.findViewById(R.id.mood_score)
        anxietyScore = view.findViewById(R.id.anxiety_score)
        stressScore = view.findViewById(R.id.stress_score)
        sleepScore = view.findViewById(R.id.sleep_score)
        conversationCount = view.findViewById(R.id.conversation_count)
        moodProgress = view.findViewById(R.id.mood_progress)
        anxietyProgress = view.findViewById(R.id.anxiety_progress)
        stressProgress = view.findViewById(R.id.stress_progress)
        sleepProgress = view.findViewById(R.id.sleep_progress)
        moodHistoryRecycler = view.findViewById(R.id.mood_history_recycler)
        insightsText = view.findViewById(R.id.insights_text)
        analyzeButton = view.findViewById(R.id.btn_analyze_trends)
        exportButton = view.findViewById(R.id.btn_export_data)
    }

    private fun setupClickListeners() {
        analyzeButton.setOnClickListener {
            viewModel.analyzeMoodTrends()
            Toast.makeText(context, "Analyzing your mental health patterns...", Toast.LENGTH_SHORT)
                .show()
        }

        exportButton.setOnClickListener {
            exportDashboardData()
        }
    }

    private fun loadDashboardData() {
        val savedMood = sharedPreferences.getInt("mood_score", 65)
        val savedAnxiety = sharedPreferences.getInt("anxiety_score", 35)
        val savedStress = sharedPreferences.getInt("stress_score", 40)
        val savedSleep = sharedPreferences.getInt("sleep_score", 70)
        val savedConversations = sharedPreferences.getInt("conversation_count", 0)

        moodProgress.progress = savedMood
        anxietyProgress.progress = savedAnxiety
        stressProgress.progress = savedStress
        sleepProgress.progress = savedSleep

        moodScore.text = "$savedMood%"
        anxietyScore.text = "$savedAnxiety%"
        stressScore.text = "$savedStress%"
        sleepScore.text = "$savedSleep%"
        conversationCount.text = savedConversations.toString()

        updateInsights(savedMood, savedAnxiety, savedStress, savedSleep)
    }

    private fun updateInsights(mood: Int, anxiety: Int, stress: Int, sleep: Int) {
        val insights = buildString {
            appendLine("📊 Mental Health Insights")
            appendLine()

            when {
                mood >= 75 -> appendLine("✅ Your mood is excellent! Keep up the good work.")
                mood >= 50 -> appendLine("😊 Your mood is stable. Consider stress-reducing activities.")
                else -> appendLine("⚠️ Your mood needs attention. Try breathing exercises or talk to someone.")
            }

            appendLine()

            when {
                anxiety <= 30 -> appendLine("✅ Anxiety levels are low. You're managing well!")
                anxiety <= 60 -> appendLine("⚠️ Moderate anxiety detected. Practice relaxation techniques.")
                else -> appendLine("🚨 High anxiety. Consider reaching out for professional support.")
            }

            appendLine()

            when {
                sleep >= 70 -> appendLine("😴 Sleep quality is good!")
                else -> appendLine("⚠️ Improve sleep hygiene. Aim for 7-8 hours nightly.")
            }
        }

        insightsText.text = insights
    }

    private fun observeViewModel() {
        // Observe mood score changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moodScore.collect { score ->
                val mood = (score * 10).coerceIn(0, 100)
                moodProgress.progress = mood
                moodScore.text = "$mood%"

                val anxiety = 100 - mood
                anxietyProgress.progress = anxiety
                anxietyScore.text = "$anxiety%"

                saveDashboardData()
            }
        }

        // Observe session analysis
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sessionAnalysis.collect { analysis ->
                if (analysis.isNotEmpty()) {
                    insightsText.text = "🔍 AI Analysis:\n\n$analysis"
                }
            }
        }
    }

    private fun saveDashboardData() {
        sharedPreferences.edit().apply {
            putInt("mood_score", moodProgress.progress)
            putInt("anxiety_score", anxietyProgress.progress)
            putInt("stress_score", stressProgress.progress)
            putInt("sleep_score", sleepProgress.progress)
            putInt("conversation_count", conversationCount.text.toString().toIntOrNull() ?: 0)
            apply()
        }
    }

    private fun exportDashboardData() {
        val summary = viewModel.getConversationSummary()
        val exportData = """
            📊 MENTAL HEALTH DASHBOARD EXPORT
            ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}
            
            📈 Scores:
            • Mood: ${moodScore.text}
            • Anxiety: ${anxietyScore.text}
            • Stress: ${stressScore.text}
            • Sleep: ${sleepScore.text}
            • Conversations: ${conversationCount.text}
            
            $summary
        """.trimIndent()

        // Show export dialog
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📤 Export Dashboard")
            .setMessage(exportData)
            .setPositiveButton("Share") { _, _ ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, exportData)
                    putExtra(Intent.EXTRA_SUBJECT, "Mental Health Dashboard")
                }
                startActivity(Intent.createChooser(shareIntent, "Share Dashboard"))
            }
            .setNegativeButton("Close", null)
            .show()
    }
}

/**
 * Tab 3: Support Resources
 */
class SupportResourcesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support_resources, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResourceButtons(view)
    }

    private fun setupResourceButtons(view: View) {
        // Emergency Helplines Button
        view.findViewById<Button>(R.id.btn_emergency_helplines).setOnClickListener {
            showEmergencyHelplines()
        }

        // Support Groups Button
        view.findViewById<Button>(R.id.btn_support_groups).setOnClickListener {
            showSupportGroups()
        }

        // Therapist Finder Button
        view.findViewById<Button>(R.id.btn_find_therapist).setOnClickListener {
            findTherapist()
        }

        // Self-Care Tips Button
        view.findViewById<Button>(R.id.btn_self_care_tips).setOnClickListener {
            showSelfCareTips()
        }

        // Mental Health Articles Button
        view.findViewById<Button>(R.id.btn_articles).setOnClickListener {
            showMentalHealthArticles()
        }

        // Crisis Chat Button
        view.findViewById<Button>(R.id.btn_crisis_chat).setOnClickListener {
            startCrisisChat()
        }
    }

    private fun showEmergencyHelplines() {
        val helplines = """
            🆘 24/7 Emergency Mental Health Helplines
            
            NIMHANS Helpline
            📞 080-4611-0007
            Available 24/7 for mental health emergencies
            
            Vandrevala Foundation
            📞 1860-2662-345
            24/7 Mental Health Support
            
            iCall Helpline
            📞 9152987821
            Psychosocial Support (English/Hindi)
            Mon-Sat: 10 AM - 8 PM
            
            AASRA
            📞 91-9820466726
            24/7 Crisis Helpline
            
            Connecting NGO
            📞 9922001122 / 9922004305
            12 PM - 8 PM (All days)
            
            Women Helpline
            📞 1091
            For women in distress
            
            National Emergency
            📞 112
            For immediate danger
            
            💜 Your life matters. Please reach out if you need help.
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🚨 Emergency Support")
            .setMessage(helplines)
            .setPositiveButton("Call NIMHANS") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:08046110007")
                }
                startActivity(intent)
            }
            .setNeutralButton("Call Vandrevala") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:18602662345")
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSupportGroups() {
        val groups = arrayOf(
            "Anxiety Support Group (45 members online)",
            "Depression Support Circle (32 members)",
            "Women's Wellness Community (128 members)",
            "Crisis Support Network (67 members)",
            "Post-Trauma Recovery Group (23 members)",
            "Bipolar Support Forum (41 members)"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👥 Join Support Group")
            .setItems(groups) { _, which ->
                val selectedGroup = groups[which]
                Toast.makeText(
                    context,
                    "✅ Joining: $selectedGroup",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun findTherapist() {
        val message = """
            🔍 Find a Therapist Near You
            
            Recommended Mental Health Professionals:
            
            1. Practo
               Search verified therapists & book online
               Website: www.practo.com
            
            2. Manastha
               Women-focused mental health
               Website: www.manastha.com
            
            3. YourDOST
               Online counseling platform
               Website: www.yourdost.com
            
            4. Wysa
               AI + Human therapist support
               Website: www.wysa.io
            
            5. InnerHour
               Evidence-based therapy
               Website: www.theinnerhour.com
            
            💡 Tip: Look for therapists specializing in:
            - Women's mental health
            - Trauma & PTSD
            - Anxiety & depression
            - Relationship counseling
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🩺 Professional Help")
            .setMessage(message)
            .setPositiveButton("Search Online") { _, _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.practo.com/search?specialization=Psychologist")
                )
                startActivity(browserIntent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSelfCareTips() {
        val tips = """
            💆‍♀️ Self-Care Tips for Mental Wellness
            
            🧘‍♀️ Daily Practices:
            • 10-minute morning meditation
            • Gratitude journaling (3 things daily)
            • 30 minutes of physical activity
            • 7-8 hours of sleep
            • Limit social media (max 1 hour/day)
            
            🫁 Stress Management:
            • 4-7-8 breathing technique
            • Progressive muscle relaxation
            • Nature walks
            • Listen to calming music
            • Talk to a trusted friend
            
            🍎 Lifestyle:
            • Balanced diet (reduce caffeine)
            • Stay hydrated (8 glasses water)
            • Regular sleep schedule
            • Limit alcohol
            • Spend time in sunlight
            
            💡 Mental Exercises:
            • Practice mindfulness
            • Challenge negative thoughts
            • Set realistic goals
            • Celebrate small wins
            • Learn to say "no"
            
            📱 Apps to Try:
            • Headspace (meditation)
            • Calm (sleep & relaxation)
            • Moodpath (mood tracking)
            • Sanvello (anxiety management)
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("💖 Self-Care Guide")
            .setMessage(tips)
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun showMentalHealthArticles() {
        val articles = """
            📚 Mental Health Resources
            
            Understanding Mental Health:
            • What is depression?
            • Anxiety disorders explained
            • PTSD and trauma
            • Bipolar disorder basics
            
            Coping Strategies:
            • Managing panic attacks
            • Dealing with grief
            • Overcoming social anxiety
            • Building resilience
            
            Women's Mental Health:
            • Postpartum depression
            • Menstrual health & mood
            • Work-life balance
            • Domestic violence support
            
            Recovery Stories:
            • "I overcame depression"
            • "Living with anxiety"
            • "My therapy journey"
            • "Finding hope again"
            
            📱 Recommended Websites:
            • NIMHANS (nimhans.ac.in)
            • Mind.org.uk
            • Moodcafe.co.uk
            • BeyondBlue.org.au
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📖 Learning Resources")
            .setMessage(articles)
            .setPositiveButton("Read Online") { _, _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.nimhans.ac.in/mental-health-info")
                )
                startActivity(browserIntent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun startCrisisChat() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("💬 Crisis Chat Support")
            .setMessage(
                """
                Connect with a crisis counselor via chat:
                
                📱 iCall WhatsApp
                +91-9152987821
                
                📱 Vandrevala Foundation
                Chat available on website
                www.vandrevalafoundation.com
                
                📱 7 Cups
                Free emotional support chat
                www.7cups.com
                
                These services are confidential and free.
                A trained counselor will respond within minutes.
            """.trimIndent()
            )
            .setPositiveButton("Open Chat") { _, _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.7cups.com")
                )
                startActivity(browserIntent)
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
