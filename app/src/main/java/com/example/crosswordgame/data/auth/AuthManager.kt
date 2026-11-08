package com.example.crosswordgame.data.auth

import android.app.Activity
import android.content.Intent
import com.example.crosswordgame.R
import com.example.crosswordgame.config.FirebaseConfig
import com.example.crosswordgame.utils.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await

/**
 * AuthManager: Manages social and email-based authentication.
 */
class AuthManager(private val activity: Activity) {

    private val auth = FirebaseConfig.auth
    private val firestore = FirebaseConfig.firestore

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Auth failed: User is null")
            Result.success(user)
        } catch (e: Exception) {
            AppLogger.e("Auth", "Google Login Failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithFacebook(token: String): Result<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Facebook Login failed")
            Result.success(user)
        } catch (e: Exception) {
            AppLogger.e("Auth", "Facebook Login error", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            AppLogger.e("Auth", "Email login error", e)
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Registration failed")
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username).build()).await()
            Result.success(user)
        } catch (e: Exception) {
            AppLogger.e("Auth", "Registration error", e)
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadPlayerProfile(): Result<PlayerProfile> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val doc = firestore.collection("players").document(userId).get().await()
            val data = doc.data ?: return Result.failure(Exception("No data"))
            Result.success(PlayerProfile(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                username = data["username"] as? String ?: "",
                provider = data["provider"] as? String ?: "email"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    fun isLoggedIn(): Boolean = auth.currentUser != null
}
