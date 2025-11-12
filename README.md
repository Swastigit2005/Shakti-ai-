# ShaktiAI 3.0 - Women Safety & Empowerment Platform

ShaktiAI 3.0 is a comprehensive Android application that leverages multiple AI technologies and
blockchain to empower and protect women. The platform integrates 8 specialized AI modules, each
designed to address specific challenges faced by women in India.

> **Update**: The project now uses **Traditional Android Views** (XML layouts with Fragments)
> instead of Jetpack Compose for better compatibility and stability.

> **Build Fix (Latest)**: The RunAnywhere SDK AAR files have been removed to fix build errors.
> The app now uses Gemini AI as the primary AI service. See `BUILD_FIX_GUIDE.md` for details.

## 🌟 Features

### 1. **Sathi AI** - Emotional Support

- LSTM-based emotional analysis
- Real-time mental health support
- Empathetic conversation interface
- Emotion intensity tracking

### 2. **Guardian AI** - Safety Monitoring

- Audio-based distress detection using YOLOv5
- Real-time safety alerts
- Emergency contact notification
- Location tracking and sharing

### 3. **Nyaya AI** - Legal Assistance

- NLP-powered legal query analysis
- Information about women's rights
- Legal procedure guidance
- Helpline and resource directory

### 4. **DhanShakti AI** - Financial Literacy

- XGBoost-based financial analysis
- Personalized budgeting advice
- Investment recommendations
- Financial empowerment tips

### 5. **Sangam AI** - Community Connections

- Recommendation system for safe communities
- Women's support group discovery
- Event notifications
- Verified member connections

### 6. **Gyaan AI** - Educational Content

- Content classification and recommendation
- Personalized learning paths
- Multi-category educational resources
- Progress tracking

### 7. **Swasthya AI** - Health Monitoring

- Health metrics analysis
- Wellness recommendations
- Emergency contact quick access
- Mental and physical health tips

### 8. **Raksha AI** - Pattern Recognition

- Behavior pattern analysis
- Risk prediction and prevention
- Safety recommendations
- Proactive alert system

## 🏗️ Architecture

```
ShaktiAI3.0/
├── app/
│   ├── build.gradle.kts (Updated dependencies)
│   ├── src/main/
│   │   ├── java/com/shakti/ai/
│   │   │   ├── ui/                    # Fragments (Traditional Views)
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── HomeFragment.kt
│   │   │   │   ├── SathiAIFragment.kt
│   │   │   │   └── ... (other fragments)
│   │   │   ├── viewmodel/             # ViewModels for MVVM
│   │   │   │   ├── SathiViewModel.kt
│   │   │   │   └── ... (other viewmodels)
│   │   │   ├── ai/                    # AI Service modules
│   │   │   │   ├── GeminiService.kt
│   │   │   │   ├── SathiAI.kt         (LSTM)
│   │   │   │   ├── GuardianAI.kt      (YOLOv5)
│   │   │   │   ├── NyayaAI.kt         (NLP)
│   │   │   │   ├── DhanShaktiAI.kt    (XGBoost)
│   │   │   │   ├── SangamAI.kt        (Recommendation)
│   │   │   │   ├── GyaanAI.kt         (Classification)
│   │   │   │   ├── SwasthyaAI.kt      (Health)
│   │   │   │   └── RakshaAI.kt        (Pattern)
│   │   │   ├── blockchain/            # Aptos blockchain integration
│   │   │   │   ├── AptosService.kt
│   │   │   │   ├── TransactionBuilder.kt
│   │   │   │   └── SmartContractManager.kt
│   │   │   └── models/                # Data models
│   │   │       ├── DataModels.kt
│   │   │       └── ResponseModels.kt
│   │   ├── res/                       # XML layouts & resources
│   │   │   ├── layout/                # Fragment & Activity layouts
│   │   │   ├── values/                # Strings, colors, themes
│   │   │   ├── drawable/              # Icons and images
│   │   │   └── menu/                  # Navigation menus
│   │   └── AndroidManifest.xml
│   └── ml/                            # TensorFlow Lite models
│       ├── sathi_lstm.tflite
│       ├── guardian_audio.tflite
│       └── ... (other models)
└── build.gradle.kts
```

## 🛠️ Tech Stack

### Frontend
- **Kotlin** - Primary language
- **Android Views** - Traditional XML-based UI
- **Fragments** - Modern Fragment architecture
- **Material Design** - Material Design Components
- **RecyclerView** - Efficient list displays
- **ConstraintLayout** - Flexible layouts

### AI/ML

- **TensorFlow Lite 2.13.0** - On-device ML inference
- **Google Gemini AI 0.2.0** - Generative AI capabilities
- **ML Kit** - Audio processing
- Custom trained models for each module

### Backend & Services

- **Kotlin Coroutines 1.7.3** - Asynchronous programming
- **StateFlow** - Reactive state management
- **ViewModel** - MVVM architecture
- **Retrofit 2.9.0** - Network calls
- **OkHttp 4.11.0** - HTTP client

### Blockchain

- **Kaptos (Aptos SDK)** - Kotlin Aptos blockchain integration
- Secure data storage and verification
- Smart contracts for data integrity

### Security

- **AndroidX Security Crypto** - Secure data storage
- **End-to-end encryption** for sensitive data

## 📦 Dependencies

Key dependencies (from `app/build.gradle.kts`):

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.10.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")

// Gemini AI
implementation("com.google.ai.client.generativeai:generativeai:0.2.0")

// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.13.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")

// Aptos Blockchain
implementation("xyz.mcxross.kaptos:kaptos-android:1.0.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
implementation("com.google.firebase:firebase-vertexai")
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or higher
- Android SDK 24+ (minSdk 24, targetSdk 34)
- Gemini AI API Key (for full functionality)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ShaktiAI3.0.git
   cd ShaktiAI3.0
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an Existing Project"
    - Navigate to the cloned directory

3. **Configure API Keys**
    - Create `local.properties` if not exists
    - Add your Gemini AI API key:
      ```properties
      GEMINI_API_KEY=your_api_key_here
      ```

4. **Add ML Models**
    - Place your trained TensorFlow Lite models in `app/src/main/ml/`
    - Models needed:
        - `sathi_lstm.tflite` - Emotional analysis
        - `guardian_audio.tflite` - Audio distress detection
        - Additional models for other modules

5. **Create Required XML Files**
    - Follow the instructions in `MIGRATION_TO_TRADITIONAL_VIEW.md`
    - Create all necessary layout files, adapters, and resources
    - See the migration guide for complete templates

6. **Sync Gradle & Build**
   ```bash
   ./gradlew sync
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Sync and Build buttons

## 📱 Project Structure

The project follows **MVVM (Model-View-ViewModel)** architecture:

- **View Layer**: Fragments with XML layouts
- **ViewModel Layer**: Handles UI logic and state
- **Model Layer**: Data models and business logic
- **Service Layer**: AI services and blockchain integration

See `PROJECT_STRUCTURE.md` for detailed architecture information.

## 🎯 Usage

### For Users

1. **Launch the app** - Open ShaktiAI 3.0
2. **Choose a module** - Select from 8 AI-powered features
3. **Grant permissions** - Allow necessary permissions for full functionality
4. **Start using** - Each module provides specialized assistance

### Emergency Features

- **SOS Button** - Quick access to emergency services
- **Location Sharing** - Share location with trusted contacts
- **Silent Alert** - Discreet distress signal
- **Audio Monitoring** - Continuous safety monitoring

## 🔒 Security & Privacy

- **End-to-end encryption** for sensitive data
- **Blockchain verification** for critical records
- **Local processing** for privacy-sensitive features
- **No data sharing** without explicit consent
- **Secure storage** using Android Keystore

## 📱 Permissions

The app requires the following permissions:

- **Internet** - For AI services and emergency alerts
- **Location** - For safety features and emergency response
- **Microphone** - For audio-based distress detection
- **Phone/SMS** - For emergency contact features
- **Contacts** - To manage emergency contacts

All permissions can be managed in app settings.

## 🧪 Testing

Run tests with:

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumentation tests
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Google Gemini AI for generative capabilities
- TensorFlow team for ML framework
- Aptos and Kaptos for blockchain infrastructure
- Material Design team for UI components
- All contributors and supporters

## 📞 Support & Contact

- **Email**: support@shaktiai.org
- **Website**: https://shaktiai.org
- **Emergency Helpline**: 181 (Women's Helpline India)
- **NCW Helpline**: 7827170170

## 🗺️ Roadmap

- [x] Complete AI service layer
- [x] Traditional Android Views migration
- [x] All 8 AI modules implemented
- [ ] Complete XML layouts
- [ ] RecyclerView adapters
- [ ] Multi-language support (Hindi, Tamil, Bengali, etc.)
- [ ] Offline mode capabilities
- [ ] Integration with more emergency services
- [ ] Wearable device support
- [ ] Voice-activated features
- [ ] Enhanced community features
- [ ] iOS version

## ⚠️ Disclaimer

ShaktiAI is a support tool and should not replace professional medical, legal, or financial advice.
In case of emergency, always contact local emergency services.

## 📚 Additional Documentation

- `README.md` (this file) - Main project documentation
- `PROJECT_STRUCTURE.md` - Detailed architecture guide
- `MIGRATION_TO_TRADITIONAL_VIEW.md` - Migration guide and remaining tasks
- `app/src/main/ml/README.md` - ML models documentation
- `BUILD_FIX_GUIDE.md` - Build fix guide

---

**Made with ❤️ for women's safety and empowerment**
