package com.example.foodytrack.activity.delivery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foodytrack.R
import com.example.foodytrack.activity.auth.LoginActivity
import com.example.foodytrack.databinding.ActivityDeliveryBoyBinding
import android.widget.TextView

class DeliveryBoyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryBoyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBoyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userNameText = findViewById<TextView>(R.id.deliveryUserName)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "Unknown")

        userNameText.text = "User: $name"

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, OrderFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_orders -> {
                    replaceFragment(OrderFragment())
                    true
                }
                R.id.nav_complited -> {
                    replaceFragment(DeliveredFragment())
                    true
                }
                else -> false
            }
        }

        binding.deliveryboyLogoutIcon.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                clear()
                apply()
            }

            Intent(this, LoginActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}