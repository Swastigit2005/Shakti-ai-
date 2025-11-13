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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shakti.ai.BuildConfig
import com.shakti.ai.R
import com.shakti.ai.ai.GeminiService
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

    // Speech recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // Chat messages
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    // Permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.RECORD_AUDIO] == true -> {
                startVoiceRecognition()
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

        // Test API integration automatically (for debugging) - COMMENTED OUT TO PREVENT CRASHES
        // testGeminiAPIIntegration()
        setupMessageInput()
    }

    private fun initializeViews(view: View) {
        messageInput = view.findViewById(R.id.message_input)
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)

        // New UI elements from the redesigned layout
        val welcomeLayout = view.findViewById<LinearLayout>(R.id.welcome_layout)
        val btnMenu = view.findViewById<ImageView>(R.id.btn_menu)
        val btnScanner = view.findViewById<ImageView>(R.id.btn_scanner)
        val btnAddAttachment = view.findViewById<ImageView>(R.id.btn_add_attachment)
        val btnVoiceMessage = view.findViewById<ImageView>(R.id.btn_voice_message)
        val btnAudioWave = view.findViewById<ImageView>(R.id.btn_audio_wave)

        // Suggestion cards
        val cardEmotionalSupport = view.findViewById<CardView>(R.id.card_emotional_support)
        val cardMentalHealth = view.findViewById<CardView>(R.id.card_mental_health)
        val cardCopingStrategies = view.findViewById<CardView>(R.id.card_coping_strategies)
        val cardMoreOptions = view.findViewById<CardView>(R.id.card_more_options)

        // Set up suggestion card clicks
        cardEmotionalSupport.setOnClickListener {
            startConversationWithPrompt("I need emotional support. I'm feeling overwhelmed and could use someone to talk to.")
        }

        cardMentalHealth.setOnClickListener {
            startConversationWithPrompt("Can you help me understand mental health better? I want to learn about managing my emotions.")
        }

        cardCopingStrategies.setOnClickListener {
            startConversationWithPrompt("I need help with coping strategies. What techniques can help me deal with stress and anxiety?")
        }

        cardMoreOptions.setOnClickListener {
            showMoreOptionsMenu()
        }

        // Set up header button clicks
        btnMenu.setOnClickListener {
            Toast.makeText(context, "Menu coming soon", Toast.LENGTH_SHORT).show()
        }

        btnScanner.setOnClickListener {
            Toast.makeText(context, "QR Scanner coming soon", Toast.LENGTH_SHORT).show()
        }

        // Set up input bar button clicks
        btnAddAttachment.setOnClickListener {
            openMediaPicker()
        }

        btnVoiceMessage.setOnClickListener {
            handleVoiceRecording()
        }

        // Store references for later use - Create simple button instances
        this.sendButton = androidx.appcompat.widget.AppCompatImageButton(requireContext()).apply {
            setOnClickListener { sendMessage() }
        }

        this.voiceButton = androidx.appcompat.widget.AppCompatButton(requireContext()).apply {
            setOnClickListener { handleVoiceRecording() }
        }

        this.uploadButton = androidx.appcompat.widget.AppCompatButton(requireContext()).apply {
            setOnClickListener { openMediaPicker() }
        }

        // Create dummy buttons to maintain compatibility with existing code
        this.breathingButton = androidx.appcompat.widget.AppCompatButton(requireContext())
        this.gratitudeButton = androidx.appcompat.widget.AppCompatButton(requireContext())
    }

    private fun setupClickListeners() {
        // All button click listeners are now set in initializeViews using the new layout's UI elements.
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

    private fun setupMessageInput() {
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            ) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // Handle enter key press
        messageInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                event.action == android.view.KeyEvent.ACTION_DOWN
            ) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isNotEmpty()) {
            messageInput.text.clear()

            // Hide welcome screen and show chat if this is the first message
            if (view?.findViewById<LinearLayout>(R.id.welcome_layout)?.visibility == View.VISIBLE) {
                startConversationWithPrompt(message)
            } else {
                // Show mood selector for better context
                showMoodSelector(message)
            }
        } else {
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMoodSelector(message: String) {
        val moodOptions = arrayOf(
            "😢 Very Low (1-2)",
            "😔 Low (3-4)",
            "😐 Neutral (5-6)",
            "🙂 Good (7-8)",
            "😊 Great (9-10)"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling right now?")
            .setMessage("This helps me understand your mood better so I can provide more personalized support.")
            .setItems(moodOptions) { _, which ->
                val moodRating = when (which) {
                    0 -> 2  // Very Low
                    1 -> 4  // Low  
                    2 -> 5  // Neutral
                    3 -> 7  // Good
                    4 -> 9  // Great
                    else -> 5
                }

                // Send message with mood context
                viewModel.sendMessageToSathi(message, moodRating)
            }
            .setNegativeButton("Skip") { _, _ ->
                // Send with neutral mood
                viewModel.sendMessageToSathi(message, 5)
            }
            .show()
    }

    private fun getCurrentMoodRating(): Int {
        // Return the last known mood or show quick selector
        return viewModel.moodScore.value.coerceIn(1, 10)
    }

    private fun handleVoiceRecording() {
        if (!isListening) {
            startVoiceRecognition()
        } else {
            stopVoiceRecognition()
        }
    }

    private fun checkAudioPermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.RECORD_AUDIO)
                )
            }
        }
    }

    private fun startVoiceRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            }
            isListening = true
            voiceButton.text = "⏹️ Stop Listening"
            voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(android.R.color.holo_red_light, null)
            )
            Toast.makeText(context, "🎤 बोलिए... Listening in Hindi & English", Toast.LENGTH_SHORT).show()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // Support both Hindi and English
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                // Add English as fallback
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayOf("hi-IN", "en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "अपनी बात बताएं। Share your feelings in Hindi or English."
                )
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("VoiceInput", "✅ Ready for speech - Hindi & English supported")
                }

                override fun onBeginningOfSpeech() {
                    Log.d("VoiceInput", "🎤 User started speaking")
                    Toast.makeText(context, "🎧 सुन रहे हैं... Listening...", Toast.LENGTH_SHORT).show()
                }

                override fun onRmsChanged(rmsdB: Float) {
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {
                    Log.d("VoiceInput", "🎤 User finished speaking")
                }

                override fun onError(error: Int) {
                    isListening = false
                    voiceButton.text = "🎤 Voice Message"
                    voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.sathi_color, null)
                    )
                    
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please try again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error"
                    }
                    
                    Log.e("VoiceInput", "❌ Speech recognition error: $errorMessage (code: $error)")
                    Toast.makeText(context, "⚠️ $errorMessage", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    voiceButton.text = "🎤 Voice Message"
                    voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.sathi_color, null)
                    )
                    
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()
                    
                    Log.d("VoiceInput", "📝 Speech results: ${matches?.joinToString(", ")}")
                    
                    if (!spokenText.isNullOrEmpty()) {
                        Log.d("VoiceInput", "✅ Recognized speech: $spokenText")
                        Toast.makeText(context, "✅ समझा: $spokenText", Toast.LENGTH_SHORT).show()
                        
                        // Send voice message with context
                        val voiceMessage = "🎤 Voice Message: $spokenText"
                        
                        // Hide welcome screen if visible
                        if (view?.findViewById<LinearLayout>(R.id.welcome_layout)?.visibility == View.VISIBLE) {
                            startConversationWithPrompt(spokenText)
                        } else {
                            viewModel.sendMessageToSathi(spokenText, getCurrentMoodRating())
                        }
                    } else {
                        Log.w("VoiceInput", "⚠️ Empty speech result")
                        Toast.makeText(context, "कुछ सुनाई नहीं दिया। Please speak again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d("VoiceInput", "📝 Partial results: ${partial?.firstOrNull()}")
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d("VoiceInput", "📡 Speech event: $eventType")
                }
            })
            
            try {
                speechRecognizer?.startListening(intent)
                Log.d("VoiceInput", "🎤 Speech recognition started")
            } catch (e: Exception) {
                Log.e("VoiceInput", "❌ Failed to start speech recognition: ${e.message}", e)
                Toast.makeText(context, "Failed to start voice input: ${e.message}", Toast.LENGTH_SHORT).show()
                isListening = false
                voiceButton.text = "🎤 Voice Message"
            }
        } else {
            Log.e("VoiceInput", "❌ Speech recognition not available on this device")
            Toast.makeText(context, "Voice input not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceRecognition() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            voiceButton.text = "🎤 Voice Message"
            voiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.sathi_color, null)
            )
            Toast.makeText(context, "Stopped listening.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMediaUpload(uri: Uri) {
        try {
            val contentResolver = requireContext().contentResolver
            val mimeType = contentResolver.getType(uri)
            val fileName = uri.lastPathSegment ?: "unknown_file"

            Toast.makeText(context, "📎 Media uploaded: $fileName", Toast.LENGTH_SHORT).show()

            // Analyze media content type and send appropriate message
            val mediaMessage = when {
                mimeType?.startsWith("image/") == true -> {
                    """🖼️ I've shared an image with you. This picture captures something meaningful about how I'm feeling right now. 
                    
                    Can you help me explore:
                    • What emotions this image might represent
                    • How visual elements connect to my mental state
                    • What this choice of sharing says about my current needs
                    
                    I'd appreciate your compassionate insight into what I might be trying to express through this image."""
                }

                mimeType?.startsWith("video/") == true -> {
                    """🎬 I've shared a video with you. This video resonates with my current emotional state.
                    
                    Can you help me understand:
                    • Why this particular content speaks to me right now
                    • What feelings or experiences it might reflect
                    • How I can process what this video brings up for me
                    
                    Sometimes moving images capture feelings that words cannot express."""
                }

                mimeType?.startsWith("audio/") == true -> {
                    """🎵 I've shared an audio file with you. Music and sounds have a powerful impact on my emotions.
                    
                    Can you help me explore:
                    • How this audio reflects my current mood
                    • What memories or feelings it evokes
                    • How sound therapy or music can support my mental health
                    
                    I believe this audio choice says something important about where I am emotionally."""
                }

                mimeType?.startsWith("text/") == true -> {
                    """📝 I've shared a text document with you. The written word often captures thoughts I struggle to speak aloud.
                    
                    Can you help me:
                    • Process the emotions behind sharing this text
                    • Understand what these written words mean to me
                    • Explore how writing can be therapeutic
                    
                    Sometimes sharing text is easier than speaking our feelings directly."""
                }

                else -> {
                    """📎 I've shared a file with you that feels significant to me right now. Even though I can't describe exactly why, this file represents something important about my current emotional state.
                    
                    Can you help me:
                    • Understand why I felt compelled to share this
                    • Explore what this sharing behavior might indicate about my needs
                    • Process whatever feelings led me to choose this particular item
                    
                    Sometimes our unconscious choices reveal more than our conscious words."""
                }
            }

            // Send enhanced media context to AI with mood selector
            showMoodSelectorForMedia(mediaMessage, fileName, mimeType ?: "unknown")

        } catch (e: Exception) {
            Toast.makeText(context, "Error processing media: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showMoodSelectorForMedia(mediaMessage: String, fileName: String, mimeType: String) {
        val moodOptions = arrayOf(
            "😢 This media reflects my sadness/pain",
            "😰 This shows my anxiety/worry",
            "😐 This represents my current neutral state",
            "🤔 This makes me think/reflect",
            "😊 This brings me some comfort/joy"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("How does this media relate to your feelings?")
            .setMessage("Sharing media is a meaningful way to express emotions. Help me understand the connection between this $mimeType file and your current state.")
            .setItems(moodOptions) { _, which ->
                val moodRating = when (which) {
                    0 -> 2  // Sadness/Pain
                    1 -> 3  // Anxiety  
                    2 -> 5  // Neutral
                    3 -> 6  // Reflective
                    4 -> 8  // Comfort/Joy
                    else -> 5
                }

                val contextualMessage =
                    "$mediaMessage\n\n[Media context: $fileName - ${moodOptions[which].substring(2)}]"

                // Send message with media and mood context
                viewModel.sendMessageToSathi(contextualMessage, moodRating)
            }
            .setNegativeButton("Just shared it") { _, _ ->
                // Send with neutral mood and basic context
                viewModel.sendMessageToSathi("$mediaMessage\n\n[Media: $fileName]", 5)
            }
            .show()
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "image/*",
                    "video/*",
                    "audio/*",
                    "text/*"
                )
            )
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        try {
            pickMediaLauncher.launch(Intent.createChooser(intent, "Share with Sathi"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open file picker", Toast.LENGTH_SHORT).show()
        }
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

    private fun startConversationWithPrompt(message: String) {
        // Hide welcome screen and show chat
        view?.findViewById<LinearLayout>(R.id.welcome_layout)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.chat_recycler_view)?.visibility = View.VISIBLE

        // Send the message
        viewModel.sendMessageToSathi(message, 5)
    }

    private fun showMoreOptionsMenu() {
        val options = arrayOf(
            "🫁 Breathing Exercise",
            "💗 Gratitude Journal",
            "📊 Mood Tracker",
            "🎵 Relaxing Sounds",
            "📚 Self-Help Resources",
            "🆘 Crisis Support"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("More Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startBreathingExercise()
                    1 -> openGratitudeJournal()
                    2 -> startConversationWithPrompt("Can you help me track my mood? I want to understand my emotional patterns better.")
                    3 -> startConversationWithPrompt("I need some relaxation techniques. Can you guide me through some calming exercises?")
                    4 -> startConversationWithPrompt("I'm looking for self-help resources. What do you recommend for personal growth and mental wellness?")
                    5 -> startConversationWithPrompt("I'm in crisis and need immediate support. Please help me.")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // Data classes
    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: String
    )

    /**
     * Test function to verify Gemini API integration
     * Uncomment the call in onViewCreated to enable testing
     */
    private fun testGeminiAPIIntegration() {
        // Add a small delay to let the fragment initialize
        view?.postDelayed({
            Log.d("SathiChatTest", "Testing Gemini API integration...")

            // Test 1: Check BuildConfig
            try {
                Log.d(
                    "SathiChatTest",
                    "BuildConfig.GEMINI_API_KEY length: ${BuildConfig.GEMINI_API_KEY.length}"
                )
                Log.d(
                    "SathiChatTest",
                    "API key starts with: ${BuildConfig.GEMINI_API_KEY.take(15)}..."
                )

                if (BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "your_api_key_here") {
                    Log.e("SathiChatTest", "❌ API key not properly configured!")
                } else {
                    Log.d("SathiChatTest", "✅ API key appears to be configured")
                }
            } catch (e: Exception) {
                Log.e("SathiChatTest", "❌ Error accessing BuildConfig: ${e.message}")
            }

            // Test 2: Direct API call
            lifecycleScope.launch {
                try {
                    Log.d("SathiChatTest", "Making direct API call...")
                    val geminiService = GeminiService.getInstance(requireContext())
                    val testResponse =
                        geminiService.callSathiAI("Hello, this is a test message to verify the API is working properly.")

                    Log.d("SathiChatTest", "✅ API Response received: ${testResponse.take(100)}...")

                    if (testResponse.contains("Demo mode") || testResponse.contains("API key")) {
                        Log.w(
                            "SathiChatTest",
                            "⚠️ Received demo response - API might not be working"
                        )
                        Toast.makeText(context, "⚠️ API Test: Demo mode active", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Log.d("SathiChatTest", "✅ API test successful!")
                        Toast.makeText(context, "✅ API Test: Working properly", Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: Exception) {
                    Log.e("SathiChatTest", "❌ API test failed: ${e.message}", e)
                    Toast.makeText(context, "❌ API Test Failed: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }

            // Test 3: ViewModel test
            Log.d("SathiChatTest", "Testing ViewModel integration...")
            viewModel.sendMessageToSathi("Test message from automated testing", 5)

            // --- TEST 4: DIRECT Gemini API integration ---
            testDirectGeminiAPI()

        }, 2000) // Wait 2 seconds for initialization
    }

    /**
     * Direct Gemini API test - bypasses service layer for immediate testing
     */
    private fun testDirectGeminiAPI() {
        lifecycleScope.launch {
            try {
                Log.d("DirectGeminiTest", "🟢 Testing DIRECT Gemini API integration...")

                // Create direct Gemini model (uses API key from BuildConfig)
                val directModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )

                val testPrompt = """
                You are Sathi, a compassionate AI companion for Indian women. 

                User just said: "Hello, I need someone to talk to"

                Respond warmly in 1-2 sentences with some Hindi mixed in naturally. Be supportive and ask how they're feeling.
                """.trimIndent()

                Log.d("DirectGeminiTest", "🟢 Making direct API call...")
                val response = directModel.generateContent(testPrompt)
                val responseText = response.text ?: "Direct API test: No response text."

                Log.d("DirectGeminiTest", "🟢 DIRECT API SUCCESS! Response: $responseText")

                // Direct simple Toast and debug log to show output (no accessing private VM fields)
                Toast.makeText(
                    context,
                    "✅ Direct API Success: ${responseText.take(60)}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("DirectGeminiTest", "❌ Direct API test failed: ${e.message}", e)
                Toast.makeText(context, "❌ Direct API Failed: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

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
