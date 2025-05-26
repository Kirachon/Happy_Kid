package com.happykid.toddlerabc.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.happykid.toddlerabc.data.ParentalAuthDao
import com.happykid.toddlerabc.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.security.KeyStore
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parental Authentication Manager for Phase 11
 * Handles secure PIN and biometric authentication for parental controls
 */
@Singleton
class ParentalAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parentalAuthDao: ParentalAuthDao
) {
    companion object {
        private const val TAG = "ParentalAuthManager"
        private const val KEYSTORE_ALIAS = "HappyKidParentalAuth"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val SESSION_TIMEOUT_DEFAULT = 30 // minutes
    }

    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotConfigured)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private var sessionStartTime: Long = 0L

    /**
     * Initialize authentication manager
     */
    suspend fun initialize() {
        val authSettings = parentalAuthDao.getParentalAuth()
        if (authSettings == null) {
            // Initialize default settings
            parentalAuthDao.initializeDefaultAuth()
            _authState.value = AuthState.NotConfigured
        } else {
            updateAuthState(authSettings)
        }
    }

    /**
     * Get authentication settings as Flow
     */
    fun getAuthSettingsFlow(): Flow<ParentalAuth?> {
        return parentalAuthDao.getParentalAuthFlow()
    }

    /**
     * Check if authentication is configured
     */
    suspend fun isAuthConfigured(): Boolean {
        val authSettings = parentalAuthDao.getParentalAuth()
        return authSettings?.isAuthEnabled == true && !authSettings.pinHash.isNullOrEmpty()
    }

    /**
     * Setup PIN authentication
     */
    suspend fun setupPinAuth(pin: String, sessionTimeoutMinutes: Int = SESSION_TIMEOUT_DEFAULT): AuthResult {
        if (pin.length < 4) {
            return AuthResult.Error("PIN must be at least 4 digits")
        }

        val pinHash = hashPin(pin)
        val authSettings = ParentalAuth(
            isAuthEnabled = true,
            authMethod = AuthMethod.PIN,
            pinHash = pinHash,
            biometricEnabled = false,
            sessionTimeoutMinutes = sessionTimeoutMinutes,
            lastAuthTimestamp = 0L,
            failedAttempts = 0,
            lockoutUntil = 0L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        parentalAuthDao.insertParentalAuth(authSettings)
        _authState.value = AuthState.NotAuthenticated
        return AuthResult.Success
    }

    /**
     * Enable biometric authentication (requires PIN to be set first)
     */
    suspend fun enableBiometricAuth(): AuthResult {
        val authSettings = parentalAuthDao.getParentalAuth()
        if (authSettings?.pinHash.isNullOrEmpty()) {
            return AuthResult.Error("PIN must be set before enabling biometric authentication")
        }

        if (!isBiometricAvailable()) {
            return AuthResult.BiometricNotAvailable
        }

        parentalAuthDao.updateBiometricEnabled(true)
        parentalAuthDao.updateAuthMethod(AuthMethod.PIN_AND_BIOMETRIC)
        return AuthResult.Success
    }

    /**
     * Authenticate with PIN
     */
    suspend fun authenticateWithPin(pin: String): AuthResult {
        val authSettings = parentalAuthDao.getParentalAuth() ?: return AuthResult.Error("Authentication not configured")

        // Check if locked out
        if (isLockedOut(authSettings)) {
            return AuthResult.TooManyAttempts
        }

        val pinHash = hashPin(pin)
        if (pinHash == authSettings.pinHash) {
            // Successful authentication
            parentalAuthDao.resetFailedAttempts()
            parentalAuthDao.updateLastAuthTimestamp(System.currentTimeMillis())
            startSession()
            _authState.value = AuthState.Authenticated
            return AuthResult.Success
        } else {
            // Failed authentication
            val newFailedAttempts = authSettings.failedAttempts + 1
            parentalAuthDao.updateFailedAttempts(newFailedAttempts)

            if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                parentalAuthDao.setLockoutUntil(lockoutUntil)
                _authState.value = AuthState.LockedOut
                return AuthResult.TooManyAttempts
            } else {
                val attemptsRemaining = MAX_FAILED_ATTEMPTS - newFailedAttempts
                _authState.value = AuthState.Failed(attemptsRemaining)
                return AuthResult.InvalidCredentials
            }
        }
    }

    /**
     * Authenticate with biometric
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onResult: (AuthResult) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onResult(AuthResult.BiometricNotAvailable)
            return
        }

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(AuthResult.BiometricError)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Update authentication timestamp
                    CoroutineScope(Dispatchers.IO).launch {
                        parentalAuthDao.updateLastAuthTimestamp(System.currentTimeMillis())
                        startSession()
                        _authState.value = AuthState.Authenticated
                    }
                    onResult(AuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(AuthResult.InvalidCredentials)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Parental Authentication")
            .setSubtitle("Authenticate to access parental controls")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Check if user is currently authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        if (!_isSessionActive.value) return false

        val authSettings = parentalAuthDao.getParentalAuth() ?: return false
        val sessionDurationMs = (authSettings.sessionTimeoutMinutes * 60 * 1000).toLong()
        val isSessionValid = (System.currentTimeMillis() - sessionStartTime) < sessionDurationMs

        if (!isSessionValid) {
            endSession()
            return false
        }

        return true
    }

    /**
     * End authentication session
     */
    fun endSession() {
        _isSessionActive.value = false
        sessionStartTime = 0L
        _authState.value = AuthState.NotAuthenticated
    }

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Start authentication session
     */
    private fun startSession() {
        _isSessionActive.value = true
        sessionStartTime = System.currentTimeMillis()
    }

    /**
     * Hash PIN for secure storage
     */
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val salt = "HappyKidParentalAuth2024" // Static salt for simplicity
        val hashBytes = digest.digest((pin + salt).toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    /**
     * Check if user is currently locked out
     */
    private fun isLockedOut(authSettings: ParentalAuth): Boolean {
        return authSettings.lockoutUntil > System.currentTimeMillis()
    }

    /**
     * Update authentication state based on settings
     */
    private suspend fun updateAuthState(authSettings: ParentalAuth) {
        when {
            !authSettings.isAuthEnabled || authSettings.pinHash.isNullOrEmpty() -> {
                _authState.value = AuthState.NotConfigured
            }
            isLockedOut(authSettings) -> {
                _authState.value = AuthState.LockedOut
            }
            isAuthenticated() -> {
                _authState.value = AuthState.Authenticated
            }
            else -> {
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    /**
     * Get time remaining until lockout expires
     */
    suspend fun getLockoutTimeRemaining(): Long {
        val authSettings = parentalAuthDao.getParentalAuth() ?: return 0L
        return maxOf(0L, authSettings.lockoutUntil - System.currentTimeMillis())
    }

    /**
     * Disable authentication (for testing or reset)
     */
    suspend fun disableAuth() {
        parentalAuthDao.setAuthEnabled(false)
        endSession()
        _authState.value = AuthState.NotConfigured
    }
}
