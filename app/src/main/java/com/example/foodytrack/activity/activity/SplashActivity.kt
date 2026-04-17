package com.example.foodytrack.activity.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.foodytrack.R
import com.example.foodytrack.activity.admin.AdminActivity
import com.example.foodytrack.activity.auth.LoginActivity
import com.example.foodytrack.activity.delivery.DeliveryBoyActivity
import com.example.foodytrack.activity.user.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        Handler(mainLooper).postDelayed({
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            val userType = sharedPreferences.getString("userType", "")

            val intent = when {
                isLoggedIn && userType == "Admin" -> Intent(this, AdminActivity::class.java)
                isLoggedIn && userType == "Delivery Boy" -> Intent(this, DeliveryBoyActivity::class.java)
                isLoggedIn -> Intent(this, MainActivity::class.java)
                else -> Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2000)
    }
}