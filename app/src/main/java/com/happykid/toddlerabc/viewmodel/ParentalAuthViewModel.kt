package com.happykid.toddlerabc.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.auth.ParentalAuthManager
import com.happykid.toddlerabc.model.AuthResult
import com.happykid.toddlerabc.model.AuthState
import com.happykid.toddlerabc.ui.auth.ParentalAuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Parental Authentication Screen
 * Phase 11b: Enhanced Parent Dashboard with Advanced Analytics Visualization
 *
 * Manages authentication state, PIN setup, biometric authentication,
 * and session management for parent dashboard access.
 */
@HiltViewModel
class ParentalAuthViewModel @Inject constructor(
    private val parentalAuthManager: ParentalAuthManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Authentication state from manager
    val authState: StateFlow<AuthState> = parentalAuthManager.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.NotConfigured
        )

    // UI state for the screen
    private val _uiState = MutableStateFlow(ParentalAuthUiState())
    val uiState: StateFlow<ParentalAuthUiState> = _uiState.asStateFlow()

    /**
     * Initialize the authentication manager and check biometric availability
     */
    fun initialize() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Initialize auth manager
                parentalAuthManager.initialize()

                // Check biometric availability
                val biometricAvailable = parentalAuthManager.isBiometricAvailable()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    biometricAvailable = biometricAvailable
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to initialize authentication: ${e.message}"
                )
            }
        }
    }

    /**
     * Setup PIN authentication for first-time users
     */
    fun setupPin(pin: String, confirmPin: String) {
        if (pin != confirmPin) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "PINs do not match"
            )
            return
        }

        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "PIN must be at least 4 digits"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = ""
                )

                val result = parentalAuthManager.setupPinAuth(pin)

                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = ""
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to setup PIN"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error setting up PIN: ${e.message}"
                )
            }
        }
    }

    /**
     * Enable biometric authentication
     */
    fun enableBiometric() {
        viewModelScope.launch {
            try {
                val result = parentalAuthManager.enableBiometricAuth()

                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication enabled"
                        )
                    }
                    is AuthResult.BiometricNotAvailable -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication not available on this device"
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to enable biometric authentication"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error enabling biometric: ${e.message}"
                )
            }
        }
    }

    /**
     * Authenticate with PIN
     */
    fun authenticateWithPin(pin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = ""
                )

                val result = parentalAuthManager.authenticateWithPin(pin)

                _uiState.value = _uiState.value.copy(isLoading = false)

                when (result) {
                    is AuthResult.Success -> {
                        // Success handled by authState flow
                    }
                    is AuthResult.InvalidCredentials -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Incorrect PIN"
                        )
                    }
                    is AuthResult.TooManyAttempts -> {
                        // Update lockout time
                        updateLockoutTime()
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Authentication failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error during authentication: ${e.message}"
                )
            }
        }
    }

    /**
     * Authenticate with biometric
     */
    fun authenticateWithBiometric(context: Context) {
        if (context !is FragmentActivity) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Biometric authentication requires activity context"
            )
            return
        }

        parentalAuthManager.authenticateWithBiometric(context) { result ->
            viewModelScope.launch {
                when (result) {
                    is AuthResult.Success -> {
                        // Success handled by authState flow
                    }
                    is AuthResult.BiometricNotAvailable -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication not available"
                        )
                    }
                    is AuthResult.BiometricError -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication error"
                        )
                    }
                    is AuthResult.InvalidCredentials -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication failed"
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Biometric authentication failed"
                        )
                    }
                }
            }
        }
    }

    /**
     * Update lockout time remaining
     */
    private fun updateLockoutTime() {
        viewModelScope.launch {
            try {
                val timeRemaining = parentalAuthManager.getLockoutTimeRemaining()
                _uiState.value = _uiState.value.copy(
                    lockoutTimeRemaining = timeRemaining
                )
            } catch (e: Exception) {
                // Handle error silently for lockout time
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
