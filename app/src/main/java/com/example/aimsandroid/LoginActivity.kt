package com.example.aimsandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aimsandroid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginButton.setOnClickListener {
            validateLoginInfo()
        }

        setContentView(binding.root)
    }

    private fun validateLoginInfo() {
        var error = false
        if(binding.driverId.text.toString().isEmpty()) {
            error = true
            binding.driverId.error = "Required"
        }
        if(binding.driverKey.text.toString().isEmpty()) {
            error = true
            binding.driverKey.error = "Required"
        }
        if(!error) {
            val prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
            prefs.edit().putString("driverId", binding.driverId.text.toString()).apply()
            prefs.edit().putString("driverKey",binding.driverKey.text.toString()).apply()
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this,MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

}