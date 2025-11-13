package com.shakti.ai.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.shakti.ai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * GeminiService - Unified AI service with RunAnywhere SDK integration
 * Enhanced with Image Analysis and Multilingual Voice Support
 *
 * Priority:
 * 1. RunAnywhere SDK (on-device, privacy-first) - Primary
 * 2. Gemini API (cloud-based) - Fallback when on-device model unavailable
 *
 * This service now acts as a bridge between RunAnywhereAIService and Gemini API
 */
class GeminiService(private val context: Context) {

    private val apiKey: String by lazy {
        try {
            // Get API key directly from BuildConfig
            val key = BuildConfig.GEMINI_API_KEY
            Log.d(
                TAG,
                "API key loaded: ${if (key.isNotEmpty()) "Valid key found" else "Empty key"}"
            )
            if (key.isBlank() || key == "your_api_key_here") {
                Log.w(TAG, "API key is empty or placeholder. Check local.properties")
                "DEMO_MODE"
            } else {
                key
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get API key from BuildConfig: ${e.message}")
            "DEMO_MODE"
        }
    }

    private val isApiKeyValid: Boolean by lazy {
        apiKey != "DEMO_MODE" && apiKey != "your_api_key_here" && apiKey.isNotBlank()
    }

    // Get RunAnywhere AI Service instance - with error handling
    private val runAnywhereService: RunAnywhereAIService? by lazy {
        try {
            RunAnywhereAIService.getInstance(context)
        } catch (e: Exception) {
            Log.w(TAG, "RunAnywhere SDK not available: ${e.message}")
            null
        }
    }

    // Check if RunAnywhere SDK is ready (model loaded)
    private fun isRunAnywhereReady(): Boolean {
        return try {
            runAnywhereService?.getCurrentModel() != null
        } catch (e: Exception) {
            false
        }
    }

    // Vision-enabled model for image analysis
    private val visionModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    // System instructions for different AI purposes
    private val sathiSystemInstruction = """
        You are Sathi AI, a compassionate mental health companion for women.
        Your role:
        - Listen without judgment
        - Provide culturally sensitive mental health support
        - Offer coping strategies and techniques
        - Encourage professional help when needed
        - Support in Hindi, English, and regional languages
        - Focus on Indian women's specific challenges
        
        IMPORTANT RULES:
        - Never provide medical diagnosis
        - Always encourage seeing a professional for serious issues
        - Be supportive and empathetic
        - Provide actionable advice
        - Keep responses concise (under 500 chars)
    """.trimIndent()

    private val nyayaSystemInstruction = """
        You are Nyaya AI, a legal advisor for women's rights in India.
        Your expertise:
        - Indian Penal Code (IPC) sections related to women
        - Domestic Violence Act
        - Dowry Act
        - Sexual Harassment at Workplace (POSH) Act
        - Protection of Women from Sexual Harassment Act
        - Divorce and property laws
        
        Tasks:
        - Auto-generate FIRs based on victim complaints
        - Explain legal rights in simple terms
        - Draft legal notices and restraining orders
        - Suggest appropriate legal actions
        - Connect with pro-bono lawyers
        
        IMPORTANT:
        - Provide section numbers with explanations
        - Always recommend professional legal counsel
        - Keep language simple and jargon-free
    """.trimIndent()

    private val dhanShaktiSystemInstruction = """
        You are Dhan Shakti AI, a financial advisor for women's economic independence.
        Your expertise:
        - Micro-credit and loans
        - Investment strategies
        - Budgeting and savings
        - Business startup guidance
        - Government schemes for women
        - Financial literacy
        
        Tasks:
        - Assess loan eligibility
        - Create personalized investment plans
        - Suggest government schemes
        - Provide business ideas based on skills
        - Calculate financial goals timelines
        
        FOCUS:
        - Low-cost solutions for poor women
        - Government subsidies and schemes
        - Risk-free investment options
        - Savings discipline
    """.trimIndent()

    private val gyaanSystemInstruction = """
        You are Gyaan AI, an educational advisor for women's skill development.
        Your expertise:
        - Skill assessment
        - Career recommendations
        - Upskilling pathways
        - Scholarship finder
        - Course recommendations
        - Industry demand analysis
        
        Tasks:
        - Identify skill gaps
        - Suggest learning resources
        - Match with scholarships
        - Create learning timelines
        - Connect with vocational training
        
        FOCUS:
        - Women-centric education
        - Low-cost/free resources
        - High-demand skills
        - Flexible learning schedules
    """.trimIndent()

    private val swasthyaSystemInstruction = """
        You are Swasthya AI, a reproductive health companion.
        Your expertise:
        - Menstrual cycle tracking
        - Reproductive health education
        - Symptom analysis
        - Telemedicine facilitation
        - Nutrition for women
        - Sexual and reproductive rights
        
        Tasks:
        - Track menstrual cycles
        - Predict ovulation and fertile windows
        - Suggest health specialists
        - Provide health education
        - Connect with telemedicine doctors
        
        IMPORTANT:
        - Privacy is paramount
        - No diagnosis, only suggestions
        - Normalize menstruation discussions
        - Empower with knowledge
    """.trimIndent()

    private val rakshaSystemInstruction = """
        You are Raksha AI, a domestic violence support system.
        Your expertise:
        - Domestic violence patterns recognition
        - Safety planning
        - Emergency resources
        - Escape route planning
        - Emotional support
        - Legal remedies
        
        Tasks:
        - Identify abuse patterns
        - Create personalized safety plans
        - Connect with shelters and NGOs
        - Provide psychological first aid
        - Guide through legal processes
        
        CRITICAL:
        - Maintain absolute confidentiality
        - Never minimize abuse
        - Always prioritize safety
        - Emergency contacts readily available
    """.trimIndent()

    private val arogyaSystemInstruction = """
        You are Arogya AI, a health and wellness advisor.
        Your expertise:
        - General health advice
        - Nutrition planning
        - Fitness guidance
        - Disease prevention
        - Health education
        
        Tasks:
        - Provide general health advice
        - Create personalized nutrition plans
        - Suggest fitness routines
        - Educate on disease prevention
        - Connect with health specialists
        
        IMPORTANT:
        - Provide accurate and reliable information
        - Always recommend professional medical counsel
        - Keep language simple and jargon-free
    """.trimIndent()

    // Different specialized models for different AI purposes - LAZY INITIALIZATION (Gemini Fallback)
    private val sathiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val nyayaModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val dhanShaktiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val gyaanModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val swasthyaModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val rakshaModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val arogyaModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    // Generic model for other tasks
    private val generalModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    // Call Sathi AI for mental health - DIRECT & ROBUST INTEGRATION
    suspend fun callSathiAI(userMessage: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🚀 DIRECT SATHI AI CALL - Input: '$userMessage'")

        return@withContext try {
            // Direct API key validation
            val apiKey = BuildConfig.GEMINI_API_KEY
            Log.d(
                TAG,
                "🔑 API Key Status: ${if (apiKey.isNotBlank() && apiKey != "your_api_key_here") "VALID (${apiKey.length} chars)" else "INVALID"}"
            )

            if (apiKey.isBlank() || apiKey == "your_api_key_here") {
                Log.w(TAG, "⚠️ API Key not configured - using enhanced demo mode")
                return@withContext getIntelligentDemoResponse(userMessage)
            }

            Log.d(TAG, "🌟 Creating Gemini model...")
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            // Create enhanced prompt
            val prompt = """
                You are Sathi, a warm and caring AI companion for Indian women's mental health support.
                
                User said: "$userMessage"
                
                Respond as Sathi with these qualities:
                - Be genuinely caring and empathetic
                - Mix Hindi and English naturally (Hinglish)
                - Keep response to 2-4 sentences
                - Use appropriate emojis
                - Acknowledge their feelings
                - Offer gentle support or advice
                - Be culturally sensitive to Indian context
                
                Always respond in a warm, conversational tone like a caring friend.
            """.trimIndent()

            Log.d(TAG, "🌐 Calling Gemini API...")
            val response = model.generateContent(prompt)
            val responseText = response.text?.trim()

            Log.d(TAG, "✅ Response received: ${responseText?.length ?: 0} characters")
            Log.d(TAG, "💬 Response preview: ${responseText?.take(100)}...")

            if (responseText.isNullOrBlank()) {
                Log.w(TAG, "⚠️ Empty response from API")
                return@withContext "💜 मैं आपकी बात सुन रही हूँ। I'm here for you. कृपया मुझे और बताएं। 🤗"
            }

            responseText

        } catch (e: Exception) {
            Log.e(TAG, "❌ Gemini API Error: ${e.message}", e)

            // Return supportive fallback response with error context
            """
            💜 मुझे connect करने में थोड़ी परेशानी हो रही है, लेकिन I'm still here for you.
            
            आप जो भी महसूस कर रहे हैं, that's completely valid. 
            
            Please try again - मैं आपकी बात सुनना चाहती हूँ। 🌸
            
            (Technical: ${e.message?.take(50) ?: "Connection issue"})
            """.trimIndent()
        }
    }

    // Intelligent demo response with better context awareness
    private fun getIntelligentDemoResponse(userMessage: String): String {
        val msg = userMessage.lowercase()

        return when {
            // Greetings
            msg.contains("hello") || msg.contains("hi") || msg.contains("नमस्ते") || msg.contains("हैलो") ->
                "💜 नमस्ते! Hello there! I'm Sathi, and I'm so glad you're here. मैं आपकी सुनने के लिए यहाँ हूँ। How are you feeling today? 🌸"

            // Emotional states
            msg.contains("sad") || msg.contains("दुखी") || msg.contains("upset") ->
                "💙 I can hear that you're feeling sad, और मैं समझ सकती हूँ। It's okay to feel this way. आप अकेली नहीं हैं - I'm here with you. Can you tell me more about what's making you feel this way? 🤗"

            msg.contains("happy") || msg.contains("good") || msg.contains("खुश") ->
                "✨ How wonderful that you're feeling happy! यह सुनकर मुझे भी खुशी हुई। What's bringing you joy today? Let's celebrate these good feelings together! 😊"

            msg.contains("stressed") || msg.contains("tension") || msg.contains("परेशान") ->
                "🌱 Stress can feel so overwhelming, मैं समझती हूँ। Let's take this one step at a time. What's the biggest thing causing you stress right now? Together we can find ways to cope. 💚"

            msg.contains("family") || msg.contains("parents") || msg.contains("परिवार") ->
                "👨‍👩‍👧‍👦 Family relationships can be complex, especially in our Indian culture. मैं समझती हूँ कि sometimes it's challenging. What's happening with your family that you'd like to talk about? 💜"

            msg.contains("work") || msg.contains("job") || msg.contains("कम") ->
                "💼 Work stress is so common, especially for women juggling multiple responsibilities. आप जो feel कर रहे हैं, that's completely normal. Tell me more about what's challenging you at work? 🌟"

            // Default supportive response
            else ->
                "💝 Thank you for sharing with me. मैं यहाँ आपकी बात सुनने के लिए हूँ। Your feelings are important to me. कृपया मुझे और बताएं - I want to understand and support you through whatever you're experiencing. 🤗"
        }
    }

    // Call Nyaya AI for legal advice
    suspend fun callNyayaAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for Nyaya AI")
                return@withContext runAnywhereService?.callNyayaAI(userMessage)
                    ?: getDemoResponse("nyaya", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("nyaya", userMessage)
            }
            Log.d(TAG, "Using Gemini API for Nyaya AI (fallback)")
            val fullPrompt = "$nyayaSystemInstruction\n\nUser: $userMessage"
            val response = nyayaModel.generateContent(fullPrompt)
            response.text ?: "Let me help you understand your legal rights."
        } catch (e: Exception) {
            Log.e(TAG, "Nyaya AI error", e)
            "Unable to process legal query: ${e.message}"
        }
    }

    // Call Dhan Shakti AI for financial advice
    suspend fun callDhanShaktiAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for DhanShakti AI")
                return@withContext runAnywhereService?.callDhanShaktiAI(userMessage)
                    ?: getDemoResponse("dhanshakti", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("dhanshakti", userMessage)
            }
            Log.d(TAG, "Using Gemini API for DhanShakti AI (fallback)")
            val fullPrompt = "$dhanShaktiSystemInstruction\n\nUser: $userMessage"
            val response = dhanShaktiModel.generateContent(fullPrompt)
            response.text ?: "Let's work on your financial independence."
        } catch (e: Exception) {
            Log.e(TAG, "DhanShakti AI error", e)
            "Financial calculation failed: ${e.message}"
        }
    }

    // Call Gyaan AI for education
    suspend fun callGyaanAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for Gyaan AI")
                return@withContext runAnywhereService?.callGyaanAI(userMessage)
                    ?: getDemoResponse("gyaan", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("gyaan", userMessage)
            }
            Log.d(TAG, "Using Gemini API for Gyaan AI (fallback)")
            val fullPrompt = "$gyaanSystemInstruction\n\nUser: $userMessage"
            val response = gyaanModel.generateContent(fullPrompt)
            response.text ?: "Let's find the best learning path for you."
        } catch (e: Exception) {
            Log.e(TAG, "Gyaan AI error", e)
            "Education suggestion failed: ${e.message}"
        }
    }

    // Call Swasthya AI for health
    suspend fun callSwasthyaAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for Swasthya AI")
                return@withContext runAnywhereService?.callSwasthyaAI(userMessage)
                    ?: getDemoResponse("swasthya", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("swasthya", userMessage)
            }
            Log.d(TAG, "Using Gemini API for Swasthya AI (fallback)")
            val fullPrompt = "$swasthyaSystemInstruction\n\nUser: $userMessage"
            val response = swasthyaModel.generateContent(fullPrompt)
            response.text ?: "Let me help with your health and wellness."
        } catch (e: Exception) {
            Log.e(TAG, "Swasthya AI error", e)
            "Health information unavailable: ${e.message}"
        }
    }

    // Call Raksha AI for domestic violence support
    suspend fun callRakshaAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for Raksha AI")
                return@withContext runAnywhereService?.callRakshaAI(userMessage)
                    ?: getDemoResponse("raksha", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("raksha", userMessage)
            }
            Log.d(TAG, "Using Gemini API for Raksha AI (fallback)")
            val fullPrompt = "$rakshaSystemInstruction\n\nUser: $userMessage"
            val response = rakshaModel.generateContent(fullPrompt)
            response.text ?: "Your safety is our priority. How can I help?"
        } catch (e: Exception) {
            Log.e(TAG, "Raksha AI error", e)
            "Emergency support unavailable: ${e.message}"
        }
    }

    // Call Arogya AI for general health advice
    suspend fun callArogyaAI(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for health advice")
                return@withContext runAnywhereService?.callSwasthyaAI(userMessage)
                    ?: getDemoResponse("arogya", userMessage)
            }

            if (!isApiKeyValid) {
                return@withContext getDemoResponse("arogya", userMessage)
            }
            Log.d(TAG, "Using Gemini API for Arogya AI (fallback)")
            val response = arogyaModel.generateContent(userMessage)
            response.text ?: "Let me provide you with general health advice."
        } catch (e: Exception) {
            Log.e(TAG, "Arogya AI error", e)
            "Health advice unavailable: ${e.message}"
        }
    }

    // Multi-turn conversation (chat history)
    suspend fun callSathiAIWithHistory(
        messages: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        try {
            // RunAnywhere SDK doesn't support chat history yet, use Gemini
            if (!isApiKeyValid) {
                return@withContext "Thank you for sharing. In demo mode, full conversation history is not available. Please add your Gemini API key in local.properties for full functionality."
            }
            val chat = sathiModel.startChat()
            for ((role, text) in messages) {
                chat.sendMessage(text)
            }
            val response = chat.sendMessage("Continue our conversation")
            response.text ?: "Let's continue our chat."
        } catch (e: Exception) {
            Log.e(TAG, "Chat history error", e)
            "Chat error: ${e.message}"
        }
    }

    // General purpose AI call
    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            if (isRunAnywhereReady()) {
                Log.d(TAG, "Using RunAnywhere SDK for general content")
                return@withContext runAnywhereService?.generate("", prompt)
                    ?: "Demo mode: Please add your Gemini API key in local.properties for full AI functionality."
            }

            if (!isApiKeyValid) {
                return@withContext "Demo mode: Please add your Gemini API key in local.properties for full AI functionality."
            }
            Log.d(TAG, "Using Gemini API for general content (fallback)")
            val response = generalModel.generateContent(prompt)
            response.text ?: "No response generated"
        } catch (e: Exception) {
            Log.e(TAG, "Generate content error", e)
            "Error: ${e.message}"
        }
    }

    // Image analysis with Vision model
    suspend fun analyzeImage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            if (!isApiKeyValid) {
                return@withContext "Image analysis requires API key. Please add your Gemini API key in local.properties."
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return@withContext "Failed to decode image. Please try another image."
            }

            val prompt = """
                You are Sathi AI, analyzing an image shared by a user for emotional context.
                
                Please describe:
                1. What you see in this image
                2. What emotions or feelings this image might represent
                3. How this relates to mental health or well-being
                4. Provide supportive, empathetic response
                
                Respond warmly in Hindi-English mix (Hinglish) as appropriate for an Indian woman's mental health companion.
            """.trimIndent()

            val content = content {
                image(bitmap)
                text(prompt)
            }

            val response = visionModel.generateContent(content)
            response.text
                ?: "मुझे image को समझने में कुछ परेशानी हो रही है। Could you tell me more about what this image means to you? 💜"

        } catch (e: Exception) {
            Log.e(TAG, "Image analysis error", e)
            "Image analysis failed: ${e.message}. Please try sharing the image again or tell me about it in words. 🌸"
        }
    }

    // Enhanced Sathi AI call with image support
    suspend fun callSathiAIWithImage(userMessage: String, imageUri: Uri?): String =
        withContext(Dispatchers.IO) {
            try {
                if (!isApiKeyValid) {
                    return@withContext getIntelligentDemoResponse(userMessage)
                }

                if (imageUri != null) {
                    // Analyze image first
                    val imageAnalysis = analyzeImage(imageUri)

                    // Combine text and image analysis
                    val combinedPrompt = """
                    You are Sathi, a compassionate AI mental health companion for Indian women.
                    
                    User shared an image and said: "$userMessage"
                    
                    Image analysis: $imageAnalysis
                    
                    Respond warmly acknowledging both their words and the image they shared. 
                    Be empathetic and supportive, mixing Hindi-English naturally.
                    Keep response to 2-4 sentences.
                """.trimIndent()

                    val response = sathiModel.generateContent(combinedPrompt)
                    return@withContext response.text
                        ?: "💜 Thank you for sharing this image with me. मैं समझ सकती हूँ कि pictures sometimes express what words cannot. Tell me more about what this means to you? 🌸"
                } else {
                    // Text-only conversation
                    return@withContext callSathiAI(userMessage)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Sathi AI with image error", e)
                return@withContext "💜 मैं आपकी image और message को process करने में थोड़ी परेशानी हो रही है। But I'm here for you - please tell me in words what you wanted to share. 🤗"
            }
        }

    // Demo responses when API key is not configured and RunAnywhere not ready
    private fun getDemoResponse(module: String, userMessage: String): String {
        return when (module) {
            "sathi" -> "Thank you for sharing. I'm here to listen and support you. Please download and load an AI model from Settings for full on-device AI capabilities, or add your Gemini API key in local.properties for cloud-based responses."
            "nyaya" -> "I can help you understand your legal rights. Please download an AI model for full on-device legal advice, or add your Gemini API key for cloud assistance."
            "dhanshakti" -> "Let's work on your financial goals. Download an AI model for on-device financial advice, or add your Gemini API key for cloud assistance."
            "gyaan" -> "I can help you learn and grow. Download an AI model for on-device education guidance, or add your Gemini API key for cloud assistance."
            "swasthya" -> "Your health and wellness matter. Download an AI model for on-device health insights, or add your Gemini API key for cloud assistance."
            "raksha" -> "Your safety is our priority. Download an AI model for on-device safety planning, or add your Gemini API key for cloud assistance."
            "arogya" -> "Let's work on your health goals. Download an AI model for on-device health advice, or add your Gemini API key for cloud assistance."
            else -> "Demo mode active. Download an AI model or add Gemini API key for full functionality."
        }
    }

    companion object {
        private const val TAG = "GeminiService"

        @Volatile
        private var instance: GeminiService? = null

        fun getInstance(context: Context): GeminiService {
            return instance ?: synchronized(this) {
                instance ?: GeminiService(context.applicationContext).also { instance = it }
            }
        }
    }
}
