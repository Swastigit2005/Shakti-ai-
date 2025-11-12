package com.shakti.ai

import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.reflect.Method

class ShaktiApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ShaktiApplication onCreate")

        // Initialize SDK asynchronously with proper error handling
        // This won't block app startup even if it fails
        applicationScope.launch(Dispatchers.IO) {
            try {
                initializeRunAnywhereSDK()
            } catch (e: Throwable) {
                // Catch all exceptions including NoClassDefFoundError
                Log.w(TAG, "RunAnywhere SDK initialization skipped: ${e.message}")
                // App continues to work even if SDK initialization fails - using Gemini API fallback
            }
        }
    }

    private suspend fun initializeRunAnywhereSDK() {
        try {
            Log.d(TAG, "Initializing RunAnywhere SDK...")

            // Import RunAnywhere SDK classes only when actually using them
            // This prevents crashes if AAR files are missing
            try {
                val runAnywhereClass = Class.forName("com.runanywhere.sdk.public.RunAnywhere")
                val sdkEnvironmentClass =
                    Class.forName("com.runanywhere.sdk.data.models.SDKEnvironment")
                val llamaCppProviderClass =
                    Class.forName("com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider")

                // Use reflection to initialize SDK safely
                val initMethod = runAnywhereClass.getMethod(
                    "initialize",
                    android.content.Context::class.java,
                    String::class.java,
                    sdkEnvironmentClass
                )
                val devEnvironment = sdkEnvironmentClass.getField("DEVELOPMENT").get(null)

                initMethod.invoke(null, this@ShaktiApplication, "dev", devEnvironment)
                Log.d(TAG, "SDK initialized")

                // Register LLM Service Provider
                val registerMethod = llamaCppProviderClass.getMethod("register")
                registerMethod.invoke(null)
                Log.d(TAG, "LLM Service Provider registered")

                // Register AI Models
                registerAIModels()
                Log.d(TAG, "Models registered")

                // Scan for previously downloaded models
                val scanMethod = runAnywhereClass.getMethod("scanForDownloadedModels")
                scanMethod.invoke(null)
                Log.d(TAG, "Scanned for downloaded models")

                Log.i(TAG, "RunAnywhere SDK initialized successfully!")

            } catch (e: ClassNotFoundException) {
                Log.w(TAG, "RunAnywhere SDK not found - using fallback Gemini API only", e)
            } catch (e: NoClassDefFoundError) {
                Log.w(TAG, "RunAnywhere SDK classes missing - using fallback Gemini API only", e)
            }

        } catch (e: Throwable) {
            Log.w(TAG, "SDK initialization failed - using fallback Gemini API only: ${e.message}")
        }
    }

    /**
     * Register all AI models for different ShaktiAI modules
     * Models are registered but not downloaded automatically
     */
    private suspend fun registerAIModels() {
        try {
            // Use reflection to call addModelFromURL
            val runAnywhereClass = Class.forName("com.runanywhere.sdk.public.RunAnywhere")
            val extensionsClass =
                Class.forName("com.runanywhere.sdk.public.extensions.ModelExtensionsKt")

            // ============ GENERAL PURPOSE MODELS ============

            // Small model - Fast responses, good for general chat (119 MB)
            registerModel(
                extensionsClass,
                runAnywhereClass,
                "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                "SmolLM2 360M Q8_0",
                "LLM"
            )

            // Medium model - Balanced quality and speed (374 MB)
            registerModel(
                extensionsClass,
                runAnywhereClass,
                "https://huggingface.co/Triangle104/Qwen2.5-0.5B-Instruct-Q6_K-GGUF/resolve/main/qwen2.5-0.5b-instruct-q6_k.gguf",
                "Qwen 2.5 0.5B Instruct Q6_K",
                "LLM"
            )

            // Large model - Best quality for complex conversations (815 MB)
            registerModel(
                extensionsClass,
                runAnywhereClass,
                "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                "Llama 3.2 1B Instruct Q6_K",
                "LLM"
            )

            // Premium model - Highest quality responses (1.2 GB)
            registerModel(
                extensionsClass,
                runAnywhereClass,
                "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q6_k.gguf",
                "Qwen 2.5 1.5B Instruct Q6_K",
                "LLM"
            )

            // Lightweight model for testing (210 MB)
            registerModel(
                extensionsClass,
                runAnywhereClass,
                "https://huggingface.co/prithivMLmods/LiquidAI-LFM-2-350M-GGUF/resolve/main/liquidai-lfm-2-350m.Q4_K_M.gguf",
                "LiquidAI LFM2 350M Q4_K_M",
                "LLM"
            )

            Log.i(TAG, "All AI models registered successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to register models: ${e.message}", e)
        }
    }

    private fun registerModel(
        extensionsClass: Class<*>,
        runAnywhereClass: Class<*>,
        url: String,
        name: String,
        type: String
    ) {
        try {
            val method = extensionsClass.getMethod(
                "addModelFromURL",
                runAnywhereClass,
                String::class.java,
                String::class.java,
                String::class.java
            )
            method.invoke(null, null, url, name, type)
            Log.d(TAG, "Registered: $name")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register $name: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ShaktiApplication"
    }
}
