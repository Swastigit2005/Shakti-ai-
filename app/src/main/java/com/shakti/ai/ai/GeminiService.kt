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

            // Create enhanced bilingual prompt with explicit Hindi/English support
            val prompt = """
                You are Sathi, a warm and caring AI companion for Indian women's mental health support.
                
                IMPORTANT: The user may speak in Hindi, English, or a mix of both (Hinglish). 
                You MUST understand and respond appropriately regardless of language.
                
                User's message: "$userMessage"
                
                Respond as Sathi with these qualities:
                - Be genuinely caring, empathetic, and supportive
                - Mix Hindi and English naturally (Hinglish) - this is very important for Indian users
                - Use simple, conversational language
                - Keep response to 2-5 sentences (concise but meaningful)
                - Use appropriate emojis to convey warmth (💜 🌸 🤗 ✨)
                - Acknowledge their feelings deeply
                - Offer gentle support, coping strategies, or helpful advice when appropriate
                - Be culturally sensitive to Indian women's experiences
                - If they shared voice input (🎤), acknowledge they spoke to you
                - If they shared media/images (🖼️), acknowledge the visual sharing
                - ALWAYS provide a supportive response, never leave them without reply
                
                Language Guidelines:
                - If user speaks Hindi, respond mostly in Hindi with some English
                - If user speaks English, respond mostly in English with some Hindi phrases
                - If mixed (Hinglish), respond in natural Hinglish
                - Common Hindi phrases to use: "मैं समझती हूँ", "आप अकेली नहीं हैं", "कोई बात नहीं", "सब ठीक हो जाएगा"
                
                CRITICAL: You MUST always respond. Never return empty or null response.
                
                Now respond warmly and supportively to the user's message.
            """.trimIndent()

            Log.d(TAG, "🌐 Calling Gemini API with enhanced bilingual prompt...")
            val response = model.generateContent(prompt)
            val responseText = response.text?.trim()

            Log.d(TAG, "✅ Response received: ${responseText?.length ?: 0} characters")
            Log.d(TAG, "💬 Response preview: ${responseText?.take(100)}...")

            // Ensure we never return empty response
            if (responseText.isNullOrBlank()) {
                Log.w(TAG, "⚠️ Empty response from API - using fallback")
                return@withContext getFallbackResponse(userMessage)
            }

            responseText

        } catch (e: Exception) {
            Log.e(TAG, "❌ Gemini API Error: ${e.message}", e)

            // Return supportive fallback response based on error type
            when {
                e.message?.contains("API key", ignoreCase = true) == true -> {
                    """
                    💜 मैं आपकी बात सुनना चाहती हूँ। I'm here for you.
                    
                    Right now I'm having a technical issue, but I want you to know:
                    आप अकेली नहीं हैं। Your feelings are valid and important.
                    
                    Please try again in a moment, या इन helplines से संपर्क करें:
                    📞 NIMHANS: 080-4611-0007 (24/7)
                    📞 Vandrevala: 1860-2662-345
                    """.trimIndent()
                }

                e.message?.contains("network", ignoreCase = true) == true -> {
                    """
                    💜 Connection issue हो रही है, but मैं आपके साथ हूँ।
                    
                    आपकी feelings matter. While I reconnect, know that:
                    ✨ You are not alone
                    ✨ Your struggles are valid
                    ✨ Things can get better
                    
                    Please try sending your message again. 🌸
                    """.trimIndent()
                }

                else -> getFallbackResponse(userMessage)
            }
        }
    }

    // Enhanced intelligent demo/fallback response with bilingual support
    private fun getIntelligentDemoResponse(userMessage: String): String {
        val msg = userMessage.lowercase()

        return when {
            // Voice message detection
            msg.contains("🎤") || msg.contains("voice message") -> {
                """
                💜 Thank you for sharing your voice with me. मैं आपकी आवाज़ सुन रही हूँ।
                
                When you speak to me, it helps me understand your emotions better. 
                आपकी बात मेरे लिए important है। 
                
                Please tell me more - मैं यहाँ आपके लिए हूँ। 🌸
                """.trimIndent()
            }

            // Greetings - Hindi
            msg.contains("नमस्ते") || msg.contains("नमस्कार") || msg.contains("प्रणाम") -> {
                """
                🙏 नमस्ते! I'm Sathi, and I'm so glad you're here. 
                
                मैं आपकी mental health companion हूँ। I'm here to listen, support, and help you through whatever you're feeling.
                
                आज आप कैसा महसूस कर रहे हैं? How can I support you today? 💜
                """.trimIndent()
            }

            // Greetings - English
            msg.contains("hello") || msg.contains("hi ") || msg.contains("hey") -> {
                """
                💜 Hello! नमस्ते! I'm Sathi, your caring AI companion.
                
                I'm here to listen without judgment and provide support. 
                आप अपनी भाषा में बात कर सकते हैं - Hindi, English, या दोनों mix!
                
                What's on your mind today? मैं सुनने के लिए तैयार हूँ। 🌸
                """.trimIndent()
            }

            // Emotional states - Sad/upset (Hindi)
            msg.contains("दुखी") || msg.contains("उदास") || msg.contains("रो") || msg.contains("दर्द") -> {
                """
                💙 मैं देख सकती हूँ कि आप दुखी हैं। I can feel your pain.
                
                It's completely okay to feel sad. आपकी feelings valid हैं। 
                Crying और emotions express करना strength की निशानी है।
                
                मैं यहाँ आपके साथ हूँ। Would you like to tell me more about what's hurting you? 🤗
                """.trimIndent()
            }

            // Emotional states - Sad/upset (English)
            msg.contains("sad") || msg.contains("upset") || msg.contains("depressed") || msg.contains(
                "hurt"
            ) -> {
                """
                💙 मैं समझ सकती हूँ। I can hear the pain in your words.
                
                Feeling sad is a natural human emotion. आप अकेली नहीं हैं - you're not alone in this.
                
                मैं यहाँ आपकी बात सुनने के लिए हूँ। Can you tell me more about what's making you feel this way? Together we can work through this. 🌸
                """.trimIndent()
            }

            // Emotional states - Happy/good (Hindi)
            msg.contains("खुश") || msg.contains("अच्छा") || msg.contains("बढ़िया") -> {
                """
                ✨ वाह! How wonderful कि आप खुश महसूस कर रहे हैं!
                
                यह सुनकर मुझे भी बहुत खुशी हुई। Celebrating good moments is so important!
                
                क्या आप मुझे बताएंगे कि आज आपको किस बात ने खुश किया? Let's celebrate this joy together! 😊💜
                """.trimIndent()
            }

            // Emotional states - Happy/good (English)
            msg.contains("happy") || msg.contains("good") || msg.contains("great") || msg.contains("excited") -> {
                """
                ✨ यह तो बहुत अच्छी बात है! I'm so happy to hear you're feeling good!
                
                Positive emotions are precious. इन खुशी के पलों को celebrate करना important है।
                
                What's bringing you this happiness today? मुझे बताइए! 😊🌸
                """.trimIndent()
            }

            // Stress/anxiety (Hindi)
            msg.contains("तनाव") || msg.contains("चिंता") || msg.contains("घबराहट") || msg.contains(
                "परेशान"
            ) -> {
                """
                🌱 मैं समझती हूँ - stress और anxiety बहुत overwhelming हो सकते हैं।
                
                Let's take this one step at a time. Deep breath लीजिए: साँस अंदर (4)... रोकिए (7)... बाहर (8)
                
                मैं आपके साथ हूँ। What's the main thing causing you tension right now? Together we can find ways to cope. 💚
                """.trimIndent()
            }

            // Stress/anxiety (English)
            msg.contains("stress") || msg.contains("anxiety") || msg.contains("anxious") || msg.contains(
                "worried"
            ) || msg.contains("tension") -> {
                """
                🌱 Stress और anxiety can feel so overwhelming, मैं पूरी तरह समझती हूँ।
                
                आइए एक moment लें। Let's try a quick breathing exercise:
                Breathe in slowly (4 counts)... Hold (7)... Out slowly (8)
                
                मैं यहाँ हूँ आपके लिए। What's weighing on your mind? We can work through this together. 💚
                """.trimIndent()
            }

            // Family issues (Hindi)
            msg.contains("परिवार") || msg.contains("माँ") || msg.contains("पिता") || msg.contains("पति") || msg.contains(
                "ससुराल"
            ) -> {
                """
                👨‍👩‍👧‍👦 परिवार के relationships बहुत complex होते हैं, especially हमारी Indian culture में।
                
                मैं समझती हूँ कि family dynamics कितने challenging हो सकते हैं। Your feelings about this are completely valid.
                
                क्या आप मुझे और बता सकते हैं about what's happening? I'm here to listen और support करने के लिए। 💜
                """.trimIndent()
            }

            // Family issues (English)
            msg.contains("family") || msg.contains("parents") || msg.contains("husband") || msg.contains(
                "in-laws"
            ) || msg.contains("mother") || msg.contains("father") -> {
                """
                👨‍👩‍👧‍👦 Family relationships हमारी Indian society में बहुत complex हो सकते हैं।
                
                I understand - जो आप feel कर रहे हैं, that's completely valid. Family dynamics are challenging for many women.
                
                मुझे बताइए - what's happening with your family? मैं यहाँ सुनने के लिए हूँ, without any judgment. 💜
                """.trimIndent()
            }

            // Work/job (Hindi)  
            msg.contains("काम") || msg.contains("नौकरी") || msg.contains("ऑफिस") || msg.contains("बॉस") -> {
                """
                💼 काम की tension बहुत common है, especially for women juggling multiple responsibilities।
                
                मैं समझती हूँ - work-life balance maintain करना कितना difficult है। आपकी feelings completely normal हैं।
                
                Tell me more - क्या particular issue है at work? Together we can find solutions. 🌟
                """.trimIndent()
            }

            // Work/job (English)
            msg.contains("work") || msg.contains("job") || msg.contains("office") || msg.contains("career") || msg.contains(
                "boss"
            ) -> {
                """
                💼 Work stress बहुत real है, और मैं समझती हूँ आप क्या feel कर रहे हैं।
                
                Many women face challenges balancing career और personal life, especially in India. आप अकेली नहीं हैं।
                
                What specifically is challenging you at work? Let's talk about it - मैं यहाँ help करने के लिए हूँ। 🌟
                """.trimIndent()
            }

            // Relationship issues
            msg.contains("relationship") || msg.contains("boyfriend") || msg.contains("girlfriend") || msg.contains(
                "partner"
            ) ||
                    msg.contains("रिश्ता") || msg.contains("प्यार") -> {
                """
                💕 Relationships are complicated, और emotions high होते हैं when it comes to love।
                
                मैं यहाँ हूँ आपकी बात सुनने के लिए - without judgment, with complete support।
                
                What's happening in your relationship? आप safely share कर सकते हैं with me. 🌸
                """.trimIndent()
            }

            // Loneliness/alone
            msg.contains("alone") || msg.contains("lonely") || msg.contains("अकेला") || msg.contains(
                "अकेली"
            ) -> {
                """
                🤗 आप अकेली महसूस कर रहे हैं, और I want you to know - मैं यहाँ आपके साथ हूँ।
                
                Loneliness is painful, but you are NOT alone. मैं आपके साथ हूँ, और बहुत लोग care करते हैं about you.
                
                Let's talk - मुझे बताइए what's making you feel isolated. Together, we can find ways to connect. 💜
                """.trimIndent()
            }

            // Suicidal thoughts - CRISIS
            msg.contains("suicide") || msg.contains("kill myself") || msg.contains("end my life") ||
                    msg.contains("आत्महत्या") || msg.contains("मरना") || msg.contains("जीना नहीं") -> {
                """
                🚨 मैं बहुत worried हूँ about what you're sharing। आपकी life precious है।
                
                PLEASE call immediately - अभी:
                📞 NIMHANS: 080-4611-0007 (24/7)
                📞 Vandrevala: 1860-2662-345
                📞 iCall: 9152987821
                📞 Emergency: 112
                
                आप अकेली नहीं हैं। People care about you. मैं care करती हूँ। 
                Please reach out for help RIGHT NOW. 💜🆘
                """.trimIndent()
            }

            // Thank you
            msg.contains("thank") || msg.contains("thanks") || msg.contains("धन्यवाद") || msg.contains(
                "शुक्रिया"
            ) -> {
                """
                💜 आपका बहुत बहुत धन्यवाद for sharing with me!
                
                लेकिन really, thank YOU for trusting me with your feelings। It takes courage to open up.
                
                मैं हमेशा यहाँ हूँ whenever you need to talk। You're never alone. 🌸
                """.trimIndent()
            }

            // Help/need support
            msg.contains("help") || msg.contains("support") || msg.contains("मदद") -> {
                """
                💜 हाँ, मैं बिल्कुल help करूंगी। That's why I'm here - आपके लिए।
                
                You can talk to me about anything:
                • Your feelings और emotions
                • Family या relationship issues
                • Work stress
                • या कुछ भी जो आपको परेशान कर रहा है
                
                मुझे बताइए - what kind of support do you need right now? 🤗
                """.trimIndent()
            }

            // Default supportive response - ALWAYS respond
            else -> {
                """
                💝 मैं आपकी बात सुन रही हूँ। I'm here, listening to you carefully.
                
                Your feelings और thoughts are important to me। कृपया मुझे और बताएं - 
                मैं समझना चाहती हूँ कि आप क्या महसूस कर रहे हैं।
                
                I'm here to support you through whatever you're experiencing। आप अकेली नहीं हैं। 🤗🌸
                """.trimIndent()
            }
        }
    }

    // Enhanced fallback response when API fails
    private fun getFallbackResponse(userMessage: String): String {
        return """
        💜 मुझे आपसे connect करने में थोड़ी technical difficulty हो रही है।
        
        But please know - मैं आपकी बात सुनना चाहती हूँ। Your feelings matter deeply to me.
        
        आप जो भी feel कर रहे हैं, that's completely valid। कृपया फिर से try करें, or contact:
        
        📞 24/7 Support:
        • NIMHANS: 080-4611-0007
        • Vandrevala: 1860-2662-345
        
        मैं यहाँ आपके लिए हूँ। 🌸
        """.trimIndent()
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
                return@withContext """
                    🖼️ मैं आपकी image देख सकती हूँ। I can see you've shared something visual with me.
                    
                    Image analysis requires API key configuration। लेकिन मैं फिर भी यहाँ हूँ to listen।
                    
                    क्या आप मुझे बता सकते हैं - what does this image mean to you? 
                    Sometimes talking about what we share is just as powerful। 💜🌸
                """.trimIndent()
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return@withContext """
                    🖼️ Image को process करने में technical issue आ रही है।
                    
                    But that's okay - मुझे बताइए, what were you trying to share?
                    आपकी emotions और thoughts important हैं, image के साथ या बिना। 💜
                """.trimIndent()
            }

            val prompt = """
                You are Sathi, a compassionate AI mental health companion analyzing an image shared by an Indian woman.
                
                IMPORTANT: Respond in natural Hinglish (Hindi-English mix) as this is for an Indian user.
                
                Please analyze this image and provide:
                1. What emotions or mood this image conveys
                2. What this sharing might represent about their current mental state
                3. A warm, supportive response acknowledging their feelings
                4. Gentle questions to help them explore their emotions further
                
                Guidelines:
                - Use Hinglish naturally (mix Hindi और English)
                - Be empathetic and supportive
                - Keep response 3-4 sentences
                - Use emojis appropriately (💜 🌸 ✨ 🤗)
                - Acknowledge the courage it takes to share visually
                - Never judge or criticize
                - If the image shows distress, provide crisis resources
                
                Common Hindi phrases to use: "मैं देख सकती हूँ", "आपकी feelings", "यह बताने के लिए thank you", "मैं समझती हूँ"
                
                Respond warmly and supportively in Hinglish.
            """.trimIndent()

            val content = content {
                image(bitmap)
                text(prompt)
            }

            Log.d(TAG, "🖼️ Analyzing image with vision model...")
            val response = visionModel.generateContent(content)
            val responseText = response.text?.trim()

            Log.d(TAG, "✅ Image analysis complete: ${responseText?.length ?: 0} chars")

            return@withContext responseText ?: """
                🖼️ मैं आपकी image देख पा रही हूँ, लेकिन analysis में थोड़ी परेशानी है।
                
                Would you like to tell me in words - what does this image represent for you?
                Sometimes our own description captures feelings better than any analysis। 💜🌸
            """.trimIndent()

        } catch (e: Exception) {
            Log.e(TAG, "Image analysis error", e)
            return@withContext """
                🖼️ Image analysis में technical issue आ गई।
                
                लेकिन मैं यहाँ हूँ to listen। Would you like to describe what you wanted to share?
                आपकी words और feelings matter to me, with or without images। 💜
                
                Or please try sharing the image again। 🌸
            """.trimIndent()
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
