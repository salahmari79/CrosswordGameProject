package com.example.crosswordgame.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crosswordgame.data.auth.AuthManager
import com.example.crosswordgame.databinding.ActivityProfileBinding
import com.example.crosswordgame.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authManager = AuthManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.btnEmailAuth.setOnClickListener {
            if (authManager.isLoggedIn()) {
                // المستخدم مسجل دخول بالفعل، يمكن عرض إحصائياته أو معلوماته
            } else {
                // توجيه المستخدم لصفحة الدخول الموحدة
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // نغلق البروفايل لنعود له بعد الدخول
            }
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("تسجيل الخروج")
                .setMessage("هل أنت متأكد؟")
                .setPositiveButton("نعم") { _, _ ->
                    authManager.logout()
                    loadProfile() // لتحديث الواجهة بعد الخروج
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        loadProfile()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = authManager.loadPlayerProfile()
            binding.progressBar.visibility = View.GONE

            if (result.isSuccess) {
                val profile = result.getOrNull()
                binding.tvUsername.text = profile?.username ?: "لاعب"
                binding.tvEmail.text = profile?.email ?: ""
                binding.layoutStats.visibility = View.VISIBLE
                binding.btnLogout.visibility = View.VISIBLE
                binding.btnEmailAuth.visibility = View.GONE // نخفي زر الدخول لأنه مسجل
            } else {
                binding.tvUsername.text = "زائر"
                binding.tvEmail.text = "سجل الدخول لحفظ تقدمك"
                binding.layoutStats.visibility = View.GONE
                binding.btnLogout.visibility = View.GONE
                binding.btnEmailAuth.visibility = View.VISIBLE
                binding.btnEmailAuth.text = "تسجيل الدخول / إنشاء حساب"
            }
        }
    }
}
