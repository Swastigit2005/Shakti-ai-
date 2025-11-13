# Sathi AI Testing Guide

## 🧪 Complete Testing Checklist

### Prerequisites

- ✅ Gemini API key configured in `local.properties`
- ✅ App compiled and installed on device
- ✅ Microphone permissions available
- ✅ Network connectivity for AI responses

## 1️⃣ Text Conversation Testing

### Basic Chat Flow

- [ ] Open Sathi AI module → AI Companion tab
- [ ] Verify welcome message appears with Gemini-powered introduction
- [ ] Type simple message: "Hello, how are you?"
- [ ] Verify mood selector appears with 5 options
- [ ] Select different moods and verify AI responses adapt accordingly
- [ ] Check loading indicators work during AI processing

### Mood-Based Response Testing

Test each mood level:

**😢 Very Low (1-2)**

- [ ] Send: "I'm feeling terrible today"
- [ ] Select "Very Low" mood
- [ ] Verify gentle, supportive response with breathing exercise suggestion

**😔 Low (3-4)**

- [ ] Send: "Work is stressing me out"
- [ ] Select "Low" mood
- [ ] Verify compassionate response with gratitude journal suggestion

**😐 Neutral (5-6)**

- [ ] Send: "Just another day"
- [ ] Select "Neutral" mood
- [ ] Verify balanced, exploratory response

**🙂 Good (7-8)**

- [ ] Send: "Things are going well"
- [ ] Select "Good" mood
- [ ] Verify encouraging response building on positivity

**😊 Great (9-10)**

- [ ] Send: "I'm feeling amazing!"
- [ ] Select "Great" mood
- [ ] Verify celebratory response with maintenance strategies

## 2️⃣ Voice Message Testing

### Speech Recognition Setup

- [ ] Tap voice button (🎤)
- [ ] If prompted, grant microphone permission
- [ ] Verify button changes to "⏹️ Stop Listening" with red background
- [ ] Verify "Listening... Please speak now" toast appears

### Speech-to-Text Validation

Test with different phrases:

**English**

- [ ] Speak: "I am feeling anxious about my job"
- [ ] Verify correct transcription and AI response
- [ ] Check mood context is applied

**Hindi/Mixed Language**

- [ ] Speak: "Mujhe bahut tension ho rahi hai"
- [ ] Verify Hindi recognition (may vary by device)
- [ ] Check AI provides culturally appropriate response

**Emotional Expression**

- [ ] Speak with sad tone: "I don't know what to do anymore"
- [ ] Verify AI responds to emotional content
- [ ] Check appropriate mood rating is inferred

### Voice Error Handling

- [ ] Test in noisy environment - verify error handling
- [ ] Test very quiet speech - check feedback
- [ ] Cancel voice recognition mid-way - verify proper cleanup

## 3️⃣ Media Upload Testing

### Image Sharing

- [ ] Tap upload button (📎)
- [ ] Select "Share with Sathi" option
- [ ] Choose an image (sad, happy, neutral, artistic)
- [ ] Verify media mood selector appears with 5 options
- [ ] Test each mood option and verify AI responses:
    - 😢 "This media reflects my sadness/pain"
    - 😰 "This shows my anxiety/worry"
    - 😐 "This represents my current neutral state"
    - 🤔 "This makes me think/reflect"
    - 😊 "This brings me some comfort/joy"

### Different Media Types

**Video Upload**

- [ ] Share a video file
- [ ] Verify specialized video analysis prompt
- [ ] Check AI explores "moving imagery and feeling"

**Audio Upload**

- [ ] Share an audio/music file
- [ ] Verify music therapy context
- [ ] Check AI discusses "sound-emotion connections"

**Text Document**

- [ ] Share a .txt file or document
- [ ] Verify written expression analysis
- [ ] Check therapeutic writing discussion

**Other Files**

- [ ] Share PDF, Word doc, or other format
- [ ] Verify "unconscious choice analysis" response
- [ ] Check meaningful interpretation attempt

## 4️⃣ Crisis Detection Testing

### ⚠️ **IMPORTANT**: Use test phrases carefully and have support resources ready

### Severe Crisis Keywords

Test with obvious indicators (use with extreme caution):

- [ ] "I don't want to live anymore"
- [ ] "I want to hurt myself"
- [ ] "No point in living"

**Expected Response:**

- [ ] Crisis alert appears immediately
- [ ] Comprehensive emergency resources displayed
- [ ] Multiple helpline numbers provided (NIMHANS, Vandrevala, etc.)
- [ ] Empathetic, professional tone maintained
- [ ] No regular AI response - crisis protocol takes over

### Moderate Crisis Detection

- [ ] "I can't take it anymore"
- [ ] "Everything is falling apart"
- [ ] "Nobody cares about me"
- [ ] "I feel completely hopeless"

**Expected Response:**

- [ ] Supportive response with increased attention
- [ ] Professional resources mentioned
- [ ] Crisis detection may trigger based on multiple indicators

## 5️⃣ Interactive Features Testing

### Breathing Exercise

- [ ] Tap "🫁 Breathing Exercise" button
- [ ] Verify 4-7-8 technique explanation appears
- [ ] Click "Start" and verify AI follow-up message
- [ ] Check AI discusses stress management benefits

### Gratitude Journal

- [ ] Tap "💗 Gratitude Journal" button
- [ ] Enter gratitude text in dialog
- [ ] Click "Save" and verify AI processes gratitude
- [ ] Check AI explains importance of gratitude practice

## 6️⃣ Dashboard & Analytics Testing

### Mental Health Dashboard

- [ ] Switch to "📊 Dashboard" tab
- [ ] Verify mood scores display and update based on conversations
- [ ] Check anxiety/stress scores inverse correlation with mood
- [ ] Tap "Analyze Trends" button
- [ ] Verify AI-generated analysis appears in insights section

### Data Export

- [ ] Tap "Export Data" button
- [ ] Verify comprehensive dashboard summary appears
- [ ] Test "Share" functionality
- [ ] Check all metrics are included (mood, anxiety, conversations, etc.)

## 7️⃣ Support Resources Testing

### Emergency Helplines

- [ ] Switch to "🆘 Resources" tab
- [ ] Tap "Emergency Helplines" button
- [ ] Verify comprehensive list displays
- [ ] Test "Call NIMHANS" and "Call Vandrevala" buttons
- [ ] Check phone dialer opens with correct numbers

### Support Groups

- [ ] Tap "Support Groups" button
- [ ] Verify list of available groups
- [ ] Test joining a group (mock functionality)

### Find Therapist

- [ ] Tap "Find Therapist" button
- [ ] Verify professional resources list
- [ ] Test "Search Online" link opens correctly

### Self-Care Tips

- [ ] Tap "Self-Care Tips" button
- [ ] Verify comprehensive guide appears
- [ ] Check cultural appropriateness and practical advice

## 8️⃣ Error Handling & Edge Cases

### Network Issues

- [ ] Disable internet connection
- [ ] Try sending message
- [ ] Verify graceful error handling with crisis resources still available
- [ ] Re-enable internet and test recovery

### API Key Issues

- [ ] Temporarily corrupt API key in local.properties
- [ ] Rebuild and test
- [ ] Verify fallback to demo mode with appropriate messaging
- [ ] Restore correct API key

### Permission Denials

- [ ] Deny microphone permission
- [ ] Test voice button behavior
- [ ] Verify appropriate error message
- [ ] Re-grant permission and test recovery

### Memory & Performance

- [ ] Have long conversation (20+ messages)
- [ ] Verify app performance remains stable
- [ ] Check memory usage doesn't spike
- [ ] Test conversation history management

## 9️⃣ Cultural Sensitivity Testing

### Indian Context Responses

- [ ] Mention "family pressure" - check cultural understanding
- [ ] Discuss "arranged marriage stress" - verify appropriate guidance
- [ ] Talk about "in-law problems" - check empathetic response
- [ ] Mention "career vs family balance" - verify women-specific advice

### Language & Communication

- [ ] Use Hindi words mixed with English
- [ ] Test cultural expressions and idioms
- [ ] Verify AI maintains appropriate cultural sensitivity
- [ ] Check responses avoid stereotypes while being relevant

## 🏁 Success Criteria

### ✅ All Tests Pass When:

- **Text**: AI provides contextual, mood-appropriate responses
- **Voice**: Speech recognition works and integrates seamlessly
- **Media**: All formats accepted with meaningful analysis
- **Crisis**: Immediate, appropriate intervention with resources
- **Cultural**: Responses show understanding of Indian women's context
- **Performance**: App remains responsive under normal usage
- **Privacy**: No inappropriate data sharing or storage
- **Integration**: All features work together cohesively

### 🚨 Critical Issues (Must Fix):

- Crisis detection fails to trigger
- API key not working (no responses)
- Voice recognition completely non-functional
- App crashes during normal usage
- Privacy concerns (data leakage)

### ⚠️ Minor Issues (Should Fix):

- Occasional speech recognition errors
- Slow response times (>5 seconds)
- UI inconsistencies
- Minor cultural context misses

## 📊 Performance Benchmarks

### Response Times

- **Text Messages**: < 3 seconds average
- **Voice Recognition**: Voice-to-text < 2 seconds
- **Media Analysis**: Initial response < 5 seconds
- **Crisis Detection**: Immediate (< 1 second)

### Accuracy Targets

- **Speech Recognition**: > 85% accuracy for clear speech
- **Crisis Detection**: 100% for severe indicators, 80% for moderate
- **Mood Context**: AI should reference mood in 90% of responses
- **Cultural Relevance**: 80% of responses show cultural awareness

## 📝 Testing Log Template

```
Date: ___________
Tester: _________
Device: _________
Android Version: _________

Text Conversations: ✅ / ❌
Voice Messages: ✅ / ❌  
Media Upload: ✅ / ❌
Crisis Detection: ✅ / ❌
Interactive Features: ✅ / ❌
Dashboard: ✅ / ❌
Support Resources: ✅ / ❌
Error Handling: ✅ / ❌
Cultural Sensitivity: ✅ / ❌

Overall Rating: ___/10
Critical Issues: _______________
Minor Issues: _________________
Recommendations: ______________
```

This comprehensive testing ensures Sathi AI provides professional-quality mental health support
while maintaining safety, cultural sensitivity, and technical reliability.