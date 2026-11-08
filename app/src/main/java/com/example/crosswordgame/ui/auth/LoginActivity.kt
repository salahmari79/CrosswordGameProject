package com.example.crosswordgame.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crosswordgame.databinding.ActivityLoginBinding
import com.example.crosswordgame.ui.game.GameViewModel
import com.example.crosswordgame.ui.main.MainActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.launch

/**
 * LoginActivity: Handles secure onboarding and authentication.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: GameViewModel by viewModels()
    private val callbackManager = CallbackManager.Factory.create()

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            showLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel.initialize(this)

        if (viewModel.authManager?.isLoggedIn() == true) {
            navigateToMain()
            return
        }

        setupFacebookCallback()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tvToggleMode.setOnClickListener { toggleLoginMode() }
        
        binding.btnEmailAuth.setOnClickListener {
            if (binding.layoutRegister.visibility == View.VISIBLE) handleRegister() else handleLogin()
        }
        
        binding.btnGoogleSignIn.setOnClickListener { handleGoogleSignIn() }
        binding.btnFacebookSignIn.setOnClickListener { handleFacebookSignIn() }
        
        binding.tvSkipLogin.setOnClickListener { 
            viewModel.skipLogin()
            navigateToMain()
        }
    }

    private fun handleGoogleSignIn() {
        showLoading(true)
        viewModel.authManager?.getGoogleSignInIntent()?.let { googleSignInLauncher.launch(it) } ?: showLoading(false)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        lifecycleScope.launch {
            val result = viewModel.authManager?.handleGoogleSignInResult(data)
            showLoading(false)
            if (result?.isSuccess == true) navigateToMain() else showError(result?.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    private fun setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.signInWithFacebook(result.accessToken.token) { res ->
                    showLoading(false)
                    if (res.isSuccess) navigateToMain() else showError("Link failed")
                }
            }
            override fun onCancel() { showLoading(false) }
            override fun onError(error: FacebookException) { showLoading(false); showError(error.message ?: "FB Error") }
        })
    }

    private fun handleFacebookSignIn() {
        showLoading(true)
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        if (email.isEmpty() || pass.isEmpty()) return
        showLoading(true)
        viewModel.signInWithEmail(email, pass) { 
            if (it.isSuccess) navigateToMain() else { showLoading(false); showError("Login failed") }
        }
    }

    private fun handleRegister() {
        val user = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) return
        showLoading(true)
        viewModel.registerWithEmail(email, pass, user) { 
            if (it.isSuccess) navigateToMain() else { showLoading(false); showError("Registration failed") }
        }
    }

    private fun toggleLoginMode() {
        val isReg = binding.layoutRegister.visibility == View.VISIBLE
        binding.layoutRegister.visibility = if (isReg) View.GONE else View.VISIBLE
        binding.etConfirmPassword.visibility = if (isReg) View.GONE else View.VISIBLE
        binding.tvToggleMode.text = if (isReg) "Sign up" else "Log in"
        binding.btnEmailAuth.text = if (isReg) "LOG IN" else "SIGN UP"
    }

    private fun showLoading(s: Boolean) { binding.progressBar.visibility = if (s) View.VISIBLE else View.GONE }
    private fun showError(m: String) { Toast.makeText(this, m, Toast.LENGTH_LONG).show() }
    private fun navigateToMain() { startActivity(Intent(this, MainActivity::class.java)); finish() }
    
    override fun onActivityResult(r: Int, rc: Int, d: Intent?) { 
        super.onActivityResult(r, rc, d)
        callbackManager.onActivityResult(r, rc, d) 
    }
}
