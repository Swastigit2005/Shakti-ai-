package com.shakti.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakti.ai.ai.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SangamViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiService = GeminiService.getInstance(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _mentorMatches = MutableStateFlow<List<MentorProfile>>(emptyList())
    val mentorMatches: StateFlow<List<MentorProfile>> = _mentorMatches.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _reputationTokens = MutableStateFlow(850)
    val reputationTokens: StateFlow<Int> = _reputationTokens.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load sample communities
                _communities.value = listOf(
                    Community(
                        "1",
                        "Domestic Violence Support",
                        "Safe space for survivors",
                        45,
                        "Support"
                    ),
                    Community(
                        "2",
                        "Single Mothers Circle",
                        "Connect with other single moms",
                        32,
                        "Support"
                    ),
                    Community(
                        "3",
                        "Career Women Network",
                        "Professional networking",
                        128,
                        "Career"
                    ),
                    Community(
                        "4",
                        "Women Entrepreneurs",
                        "Business advice and mentorship",
                        67,
                        "Business"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findMentors(interest: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Generate mentor matches using AI
                val mentors = generateMentorMatches(interest)
                _mentorMatches.value = mentors
            } catch (e: Exception) {
                _errorMessage.value = "Failed to find mentors: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMentorMatches(interest: String): List<MentorProfile> {
        // In a real implementation, this would use AI to match based on interests, goals, etc.
        return listOf(
            MentorProfile("1", "Priya Sharma", interest, "5 years", 4.9f, "Mumbai", true),
            MentorProfile("2", "Anjali Mehta", interest, "8 years", 4.8f, "Delhi", true),
            MentorProfile("3", "Kavita Singh", interest, "3 years", 4.7f, "Bangalore", false)
        )
    }

    fun joinCommunity(communityId: String) {
        viewModelScope.launch {
            // In real implementation, would call blockchain smart contract
            earnReputationTokens(10) // Reward for joining
        }
    }

    fun earnReputationTokens(amount: Int) {
        viewModelScope.launch {
            _reputationTokens.value += amount
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    data class MentorProfile(
        val id: String,
        val name: String,
        val expertise: String,
        val experience: String,
        val rating: Float,
        val location: String,
        val verified: Boolean
    )

    data class Community(
        val id: String,
        val name: String,
        val description: String,
        val memberCount: Int,
        val category: String
    )
}
