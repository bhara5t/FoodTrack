package com.example.foodytrack.activity.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodytrack.R
import com.example.foodytrack.activity.auth.LoginActivity
import com.google.android.material.card.MaterialCardView
import android.widget.TextView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val logoutIcon = findViewById<ImageView>(R.id.adminLogoutIcon)
        val addFoodCard = findViewById<MaterialCardView>(R.id.addFoodCard)
        val removeFoodCard = findViewById<MaterialCardView>(R.id.removeFoodCard)
        val userNameText = findViewById<TextView>(R.id.adminUserName)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val adminName = sharedPreferences.getString("name", "Unknown")

        userNameText.text = "User: $adminName"
        addFoodCard.setOnClickListener {
            val intent = Intent(this, AFoodAddActivity::class.java)
            startActivity(intent)
        }

        removeFoodCard.setOnClickListener {
            val intent = Intent(this, AFoodRemoveActivity::class.java)
            startActivity(intent)
        }

        logoutIcon.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                clear() // 🔥 clears EVERYTHING (best)
                apply()
            }

            Toast.makeText(this, "Admin logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}