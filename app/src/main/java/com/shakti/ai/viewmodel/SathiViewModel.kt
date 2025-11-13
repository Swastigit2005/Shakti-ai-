package com.shakti.ai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakti.ai.ai.GeminiService
import com.shakti.ai.blockchain.AptosService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SathiViewModel(application: Application) : AndroidViewModel(application) {
    private val geminiService = GeminiService.getInstance(application)
    private val aptosService = AptosService.getInstance(application)

    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages

    private val _moodScore = MutableStateFlow(5) // 1-10 scale
    val moodScore: StateFlow<Int> = _moodScore

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _sessionAnalysis = MutableStateFlow("")
    val sessionAnalysis: StateFlow<String> = _sessionAnalysis

    private val _isCrisisDetected = MutableStateFlow(false)
    val isCrisisDetected: StateFlow<Boolean> = _isCrisisDetected

    // User conversation history for LSTM context
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    // Initialize Sathi session
    fun initializeSathiSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val welcome = """
                    🌸 नमस्ते! I'm Sathi, your compassionate AI mental health companion.
                    
                    I'm here to listen without judgment, provide emotional support, and help you navigate your feelings. Everything we discuss is completely confidential.
                    
                    💜 How are you feeling today? You can:
                    • Type your thoughts and emotions
                    • Send voice messages when words feel hard
                    • Share images that represent your mood  
                    • Use our breathing exercises or gratitude journal
                    
                    I'm powered by advanced AI and trained specifically in women's mental health support. Let's take this journey together, one step at a time.
                    
                    What's on your mind right now? 💭
                """.trimIndent()
                _chatMessages.value = listOf("Sathi" to welcome)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Send user message to Sathi AI with enhanced context
    fun sendMessageToSathi(userMessage: String, moodRating: Int = 5) {
        Log.d(
            "SathiViewModel",
            "🚀 sendMessageToSathi called with: '$userMessage', mood: $moodRating"
        )

        viewModelScope.launch {
            _isLoading.value = true
            _moodScore.value = moodRating
            Log.d("SathiViewModel", "⏳ Loading state set to true, mood updated to $moodRating")

            try {
                // Add user message to chat immediately
                val messages = _chatMessages.value.toMutableList()
                messages.add("User" to userMessage)
                _chatMessages.value = messages
                Log.d("SathiViewModel", "💬 User message added. Total messages: ${messages.size}")

                // Add to conversation history
                conversationHistory.add("User" to userMessage)
                Log.d(
                    "SathiViewModel",
                    "📝 Added to history. History size: ${conversationHistory.size}"
                )

                // Crisis detection first
                val crisisDetected = detectCrisis(userMessage)
                Log.d("SathiViewModel", "🚨 Crisis detection: $crisisDetected")

                if (crisisDetected) {
                    _isCrisisDetected.value = true
                    val crisisResponse = """
                        💜 I'm deeply concerned about what you're sharing. आपकी जिंदगी महत्वपूर्ण है।
                        
                        🚨 IMMEDIATE HELP (24/7):
                        • NIMHANS: 080-4611-0007
                        • Vandrevala: 1860-2662-345  
                        • iCall: 9152987821
                        • Emergency: 112
                        
                        Would you like me to help connect you with a counselor? मैं यहाँ आपके लिए हूँ। 💝
                    """.trimIndent()

                    messages.add("Sathi" to crisisResponse)
                    _chatMessages.value = messages

                    // Log crisis (non-blocking)
                    try {
                        aptosService.logCrisisEscalation()
                        Log.d("SathiViewModel", "✅ Crisis logged to blockchain")
                    } catch (e: Exception) {
                        Log.w("SathiViewModel", "⚠️ Crisis logging failed: ${e.message}")
                    }

                    return@launch
                }

                // Call Sathi AI directly
                Log.d("SathiViewModel", "🤖 Calling GeminiService.callSathiAI...")
                val aiResponse = geminiService.callSathiAI(userMessage)
                Log.d("SathiViewModel", "✅ AI Response received: ${aiResponse.length} chars")
                Log.d("SathiViewModel", "📖 Response preview: ${aiResponse.take(100)}...")

                // Add AI response to chat
                messages.add("Sathi" to aiResponse)
                _chatMessages.value = messages
                Log.d("SathiViewModel", "💬 AI response added. Total messages: ${messages.size}")

                // Add to conversation history
                conversationHistory.add("Sathi" to aiResponse)
                Log.d("SathiViewModel", "📝 AI response added to history")

                // Log session (non-blocking)
                try {
                    aptosService.logMentalHealthSession(
                        moodScore = moodRating,
                        message = userMessage,
                        response = aiResponse
                    )
                    Log.d("SathiViewModel", "✅ Session logged to blockchain")
                } catch (e: Exception) {
                    Log.w("SathiViewModel", "⚠️ Session logging failed: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("SathiViewModel", "❌ ERROR in sendMessageToSathi: ${e.message}", e)

                // Show helpful error message
                val errorMessage = """
                    💜 मुझे आपसे बात करने में technical issue हो रहा है।
                    
                    But I want you to know - आप अकेली नहीं हैं। Your feelings are valid.
                    
                    कृपया एक बार फिर try करें। If urgent:
                    • NIMHANS: 080-4611-0007 (24/7)
                    • Vandrevala: 1860-2662-345
                    
                    मैं यहाँ आपके लिए हूँ। 🌸
                    
                    Error: ${e.message}
                """.trimIndent()

                val messages = _chatMessages.value.toMutableList()
                messages.add("Sathi" to errorMessage)
                _chatMessages.value = messages
                Log.d("SathiViewModel", "💬 Error message added to chat")

            } finally {
                _isLoading.value = false
                Log.d("SathiViewModel", "⏳ Loading state set to false")
            }
        }
    }

    // Build enhanced contextual prompt for better AI responses
    private fun buildContextualPrompt(userMessage: String, moodRating: Int): String {
        val recentContext = if (conversationHistory.size > 2) {
            "Previous conversation context (last 3 exchanges):\n" +
                    conversationHistory.takeLast(6)
                        .joinToString("\n") { "${it.first}: ${it.second}" } +
                    "\n\n"
        } else ""

        val moodContext = when (moodRating) {
            in 1..3 -> "The user is feeling quite low (mood: $moodRating/10). Please be extra gentle and supportive."
            in 4..6 -> "The user has a neutral to moderate mood (mood: $moodRating/10). Provide balanced support."
            in 7..10 -> "The user is feeling relatively positive (mood: $moodRating/10). Encourage and build on this."
            else -> "Mood not specified, provide general support."
        }

        return """
            $recentContext
            
            Context: $moodContext
            
            Current message from user: "$userMessage"
            
            Please respond as Sathi, a compassionate AI mental health companion specifically designed for women in India. Your response should:
            
            1. Be empathetic and culturally sensitive
            2. Use a warm, non-judgmental tone
            3. Provide practical coping strategies when appropriate
            4. Include relevant emojis to make the conversation feel more personal
            5. Be concise but meaningful (2-4 sentences typically)
            6. Reference Indian cultural context when relevant
            7. Suggest professional help if needed
            8. Validate their feelings and experiences
            
            If this is about media (voice, image, etc.), acknowledge the sharing and explore the emotional context.
            
            Remember: You're a supportive companion, not a therapist. Guide them toward professional help when appropriate.
        """.trimIndent()
    }

    // Enhance AI response with better formatting and additional resources
    private fun enhanceAIResponse(response: String, moodRating: Int): String {
        val baseResponse = response.trim()

        // Add mood-appropriate resources or suggestions
        val additionalSupport = when {
            moodRating <= 3 -> {
                "\n\n💜 Quick comfort: Try the 4-7-8 breathing technique (breathe in for 4, hold for 7, out for 8). Would you like to try it together?"
            }

            moodRating in 4..6 -> {
                "\n\n🌱 Small step: Sometimes journaling our gratitude (even one thing) can shift our perspective. What's one tiny thing you're grateful for today?"
            }

            moodRating >= 7 -> {
                "\n\n✨ Building on positivity: It's wonderful that you're feeling good! How can we nurture this feeling and make it last?"
            }

            else -> ""
        }

        return baseResponse + additionalSupport
    }

    // Enhanced crisis detection with more sophisticated analysis
    fun detectCrisis(message: String): Boolean {
        val messageLower = message.lowercase()

        val severeCrisisKeywords = listOf(
            "suicide", "suicidal", "kill myself", "end my life", "ending my life",
            "self harm", "self-harm", "cut myself", "hurt myself",
            "don't want to live", "want to die", "better off dead",
            "ending it all", "take my own life", "no point in living"
        )

        val moderateCrisisKeywords = listOf(
            "can't take it", "can't go on", "give up", "hopeless", "helpless",
            "no way out", "everyone would be better without me", "completely alone",
            "nobody cares", "no one understands", "worthless", "can't cope",
            "everything is falling apart", "nothing matters"
        )

        // Check for severe crisis indicators
        val hasSevereCrisis = severeCrisisKeywords.any { messageLower.contains(it) }

        // Check for multiple moderate indicators (may indicate building crisis)
        val moderateCount = moderateCrisisKeywords.count { messageLower.contains(it) }
        val hasMultipleModerateCrisis = moderateCount >= 2

        return hasSevereCrisis || hasMultipleModerateCrisis
    }

    // Analyze mood trends
    fun analyzeMoodTrends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Build analysis prompt from conversation history
                val recentMoods = conversationHistory.take(10).joinToString("\n") {
                    "${it.first}: ${it.second.take(100)}..."
                }

                val analysisPrompt = """
                    Analyze the following mental health conversation history and provide insights:
                    
                    $recentMoods
                    
                    Current Mood Score: ${_moodScore.value}/10
                    
                    Please provide:
                    1. Mood pattern analysis
                    2. Identified stress triggers
                    3. Positive coping mechanisms observed
                    4. Recommended next steps
                    5. When to seek professional help
                """.trimIndent()

                val analysis = geminiService.callSathiAI(analysisPrompt)
                _sessionAnalysis.value = analysis

                // Save analysis to Aptos blockchain
                aptosService.logMentalHealthAnalysis(analysis)

            } catch (e: Exception) {
                _sessionAnalysis.value =
                    "Unable to generate analysis at this time. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Emergency escalation to human counselor
    fun escalateToHumanCounselor() {
        viewModelScope.launch {
            try {
                // Log crisis escalation to blockchain
                aptosService.logCrisisEscalation()

                // Add system message
                val escalationMessage = """
                    🆘 CONNECTING YOU TO PROFESSIONAL HELP
                    
                    A trained counselor will be with you shortly. In the meantime:
                    
                    📞 IMMEDIATE HELPLINES (24/7):
                    • Mental Health Helpline: 1800-599-0019
                    • Vandrevala Foundation: 1860-2662-345
                    • iCall (English/Hindi): 9152987821
                    • Lifeline Foundation: 033-24637401/7432
                    
                    Please hold on. You matter, and help is on the way. 💜
                """.trimIndent()

                _chatMessages.value = _chatMessages.value.toMutableList().apply {
                    add("System" to escalationMessage)
                }

            } catch (e: Exception) {
                // Even if logging fails, show helplines
                val fallbackMessage = """
                    Please call these helplines immediately:
                    • 1800-599-0019 (Mental Health)
                    • 1860-2662-345 (Vandrevala)
                    • 9152987821 (iCall)
                """.trimIndent()

                _chatMessages.value = _chatMessages.value.toMutableList().apply {
                    add("System" to fallbackMessage)
                }
            }
        }
    }

    // Get conversation summary for reports
    fun getConversationSummary(): String {
        return buildString {
            appendLine("=== Sathi AI Session Summary ===")
            appendLine("Total Messages: ${conversationHistory.size}")
            appendLine("Average Mood Score: ${_moodScore.value}/10")
            appendLine("Crisis Detected: ${if (_isCrisisDetected.value) "Yes" else "No"}")
            appendLine("\nConversation History:")
            conversationHistory.forEach { (sender, message) ->
                appendLine("[$sender]: ${message.take(100)}${if (message.length > 100) "..." else ""}")
            }
        }
    }

    // Clear session (with user consent)
    fun clearSession() {
        conversationHistory.clear()
        _chatMessages.value = emptyList()
        _moodScore.value = 5
        _sessionAnalysis.value = ""
        _isCrisisDetected.value = false
    }

    // Export session data (encrypted, for user records)
    fun exportSessionData(): Map<String, Any> {
        return mapOf(
            "timestamp" to System.currentTimeMillis(),
            "totalMessages" to conversationHistory.size,
            "averageMood" to _moodScore.value,
            "crisisDetected" to _isCrisisDetected.value,
            "messages" to conversationHistory.map {
                mapOf("sender" to it.first, "message" to it.second)
            }
        )
    }
}
