# Sathi AI Troubleshooting Guide

## 🚨 AI Companion Not Responding - Diagnostic Steps

### Step 1: Check API Key Configuration

#### Verify local.properties

Check that your `local.properties` file contains:

```properties
GEMINI_API_KEY=AIzaSyC8r5bh2LW-a37nWIEbL9JjIyktvMIBoxs
```

#### Verify Build Configuration

1. Clean and rebuild the project:

```bash
./gradlew clean assembleDebug
```

2. Check logcat for API key loading:

```bash
adb logcat -s GeminiService:D
```

Expected logs:

```
D/GeminiService: API key loaded: Valid key found
D/GeminiService: API key valid: true
```

### Step 2: Enable Debug Logging

#### Filter Logcat for Sathi AI

```bash
adb logcat -s SathiViewModel:D GeminiService:D
```

#### Expected Log Flow

When sending a message, you should see:

```
D/SathiViewModel: sendMessageToSathi called with message: 'Hello', mood: 5
D/SathiViewModel: Loading state set to true, mood score updated to 5
D/SathiViewModel: User message added to chat. Total messages: 1
D/SathiViewModel: Building contextual prompt for AI...
D/SathiViewModel: Calling Gemini Service...
D/GeminiService: callSathiAI called with message length: 1234
D/GeminiService: API key valid: true
D/GeminiService: RunAnywhere ready: false
D/GeminiService: Using Gemini API for Sathi AI (fallback)
D/GeminiService: Calling Gemini API...
D/GeminiService: Gemini API response received. Length: 456
D/SathiViewModel: AI response received. Length: 456 characters
D/SathiViewModel: AI response added to chat. Total messages now: 2
```

### Step 3: Common Issues & Solutions

#### Issue 1: API Key Not Valid

**Symptoms:**

```
W/GeminiService: API key not valid, returning demo response
```

**Solutions:**

1. Verify API key is correctly copied (no extra spaces)
2. Ensure local.properties is in project root
3. Clean and rebuild project
4. Check that API key has proper Google Cloud permissions

#### Issue 2: Network/Connection Issues

**Symptoms:**

```
E/GeminiService: Sathi AI error: Unable to resolve host
```

**Solutions:**

1. Check internet connectivity
2. Try on different network (WiFi vs mobile data)
3. Check if corporate firewall blocks API calls
4. Verify Gemini API service status

#### Issue 3: API Quota/Rate Limiting

**Symptoms:**

```
E/GeminiService: Sathi AI error: Quota exceeded
```

**Solutions:**

1. Check Google Cloud Console for API quotas
2. Wait for quota reset (usually daily)
3. Upgrade API plan if needed
4. Implement request throttling

#### Issue 4: Malformed Requests

**Symptoms:**

```
E/GeminiService: Sathi AI error: Invalid request format
```

**Solutions:**

1. Check prompt length (stay under token limits)
2. Verify special characters are properly escaped
3. Ensure UTF-8 encoding for international text

#### Issue 5: App Crashes During AI Calls

**Symptoms:**

```
E/AndroidRuntime: FATAL EXCEPTION: DefaultDispatcher-worker
```

**Solutions:**

1. Add more try-catch blocks around Gemini calls
2. Check memory usage during large responses
3. Implement timeout handling for long requests

### Step 4: Manual Testing

#### Test API Key Directly

Create a simple test in your app:

```kotlin
// Add this to test API integration
private fun testGeminiAPI() {
    lifecycleScope.launch {
        try {
            val service = GeminiService.getInstance(requireContext())
            val response = service.callSathiAI("Hello, this is a test message")
            Log.d("TEST", "API Response: $response")
            Toast.makeText(context, "API works: $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("TEST", "API failed: ${e.message}", e)
            Toast.makeText(context, "API failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
```

#### Test Network Connectivity

```kotlin
private fun testNetworkConnectivity() {
    lifecycleScope.launch {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models")
                .build()
            
            val response = client.newCall(request).execute()
            Log.d("TEST", "Network test: ${response.code}")
        } catch (e: Exception) {
            Log.e("TEST", "Network failed: ${e.message}", e)
        }
    }
}
```

### Step 5: Alternative Debugging Methods

#### Check BuildConfig Values

Add this to your fragment:

```kotlin
Log.d("DEBUG", "API Key length: ${BuildConfig.GEMINI_API_KEY.length}")
Log.d("DEBUG", "API Key starts with: ${BuildConfig.GEMINI_API_KEY.take(10)}")
```

#### Verify ViewModel State

```kotlin
// Add observers to check ViewModel state
viewModel.isLoading.observe(this) { loading ->
    Log.d("DEBUG", "Loading state: $loading")
}

viewModel.chatMessages.observe(this) { messages ->
    Log.d("DEBUG", "Chat messages count: ${messages.size}")
}
```

### Step 6: Fallback Solutions

#### If Gemini API Continues to Fail

1. **Use Demo Mode Temporarily:**
    - Modify GeminiService to return meaningful demo responses
    - Test UI functionality without API dependency

2. **Implement Local Responses:**
    - Create a set of predefined responses based on keywords
    - Use as backup when API fails

3. **Add Retry Logic:**
    - Implement exponential backoff for failed requests
    - Queue messages for retry when network is restored

#### Demo Response Enhancement

```kotlin
private fun getEnhancedDemoResponse(userMessage: String): String {
    val lowerMessage = userMessage.lowercase()
    return when {
        lowerMessage.contains("sad") || lowerMessage.contains("depressed") -> 
            "💜 I hear that you're feeling sad. It's okay to feel this way - your emotions are valid. Would you like to try a breathing exercise together?"
        
        lowerMessage.contains("anxious") || lowerMessage.contains("worried") ->
            "🌱 Anxiety can feel overwhelming, but you're not alone. Let's take this one step at a time. What's your biggest worry right now?"
        
        lowerMessage.contains("happy") || lowerMessage.contains("good") ->
            "✨ I'm so glad to hear you're feeling positive! What's bringing you joy today? Let's celebrate these good feelings."
        
        else ->
            "💝 Thank you for sharing with me. I'm here to listen and support you. Can you tell me more about what's on your mind?"
    }
}
```

### Step 7: Performance Optimization

#### Reduce API Call Frequency

```kotlin
// Add debouncing to prevent excessive API calls
private fun sendMessageWithDebounce(message: String, mood: Int) {
    messageJob?.cancel()
    messageJob = lifecycleScope.launch {
        delay(500) // Wait 500ms before sending
        viewModel.sendMessageToSathi(message, mood)
    }
}
```

#### Implement Caching

```kotlin
// Cache recent AI responses to reduce API calls
private val responseCache = mutableMapOf<String, String>()

private fun getCachedResponse(message: String): String? {
    val key = message.lowercase().take(50)
    return responseCache[key]
}
```

### Step 8: Final Checklist

#### ✅ Verification Steps

- [ ] API key is correctly configured in local.properties
- [ ] Project rebuilds successfully without errors
- [ ] Logcat shows "API key valid: true"
- [ ] Network connectivity is available
- [ ] Gemini API service is accessible
- [ ] No quota/rate limiting issues
- [ ] App doesn't crash during AI calls
- [ ] Loading states work correctly
- [ ] Error messages are displayed appropriately

#### 🚨 Emergency Fallbacks

- [ ] Demo responses are meaningful and helpful
- [ ] Crisis detection still works without API
- [ ] Emergency resources are always available
- [ ] User can still navigate the app normally

### Step 9: Contact Support

If none of these solutions work:

1. **Collect Debug Information:**
    - Full logcat output during message sending
    - Device information (Android version, model)
    - Network environment details
    - Exact error messages

2. **Test on Different Devices:**
    - Try on emulator vs physical device
    - Test on different Android versions
    - Verify on different network connections

3. **API Service Status:**
    - Check Google Cloud Console
    - Verify Gemini API service status
    - Confirm billing/quota settings

### Quick Fix Checklist

If the AI companion isn't responding, try these in order:

1. **🔄 Restart App** - Close and reopen the application
2. **📶 Check Network** - Ensure stable internet connection
3. **🔑 Verify API Key** - Check local.properties configuration
4. **🔨 Clean Build** - Run `./gradlew clean assembleDebug`
5. **📱 Restart Device** - Full device reboot
6. **🌐 Test Network** - Try different WiFi/mobile network
7. **📊 Check Logs** - Enable debug logging and check output
8. **⏱️ Wait & Retry** - API might be temporarily unavailable

### Success Indicators

When working properly, you should see:

- ✅ Loading indicator appears when sending message
- ✅ User message appears immediately in chat
- ✅ AI response appears within 2-5 seconds
- ✅ Responses are contextual and relevant
- ✅ No error messages or crashes
- ✅ Mood selector works correctly
- ✅ Voice and media features integrate smoothly

This troubleshooting guide should help identify and resolve most AI companion response issues.