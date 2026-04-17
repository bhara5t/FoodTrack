package com.example.foodytrack.activity.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodytrack.R
import com.example.foodytrack.activity.user.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobileNumber: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtLogin: TextView

    private lateinit var progressLayout: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etLocation = findViewById(R.id.etDeliveryAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressLayout = findViewById(R.id.progressLayout)
        txtLogin = findViewById(R.id.txtLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val mobileNumber = etMobileNumber.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, mobileNumber, location, password, confirmPassword)) {
                registerUser(name, email, mobileNumber, location, password)
            }
        }

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        mobileNumber: String,
        location: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (TextUtils.isEmpty(name)) {
            etName.error = "Name is required"
            return false
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            return false
        }
        if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length != 10) {
            etMobileNumber.error = "Enter a valid 10-digit mobile number"
            return false
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.error = "Location is required"
            return false
        }
        if (TextUtils.isEmpty(password) || password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (TextUtils.isEmpty(confirmPassword) || password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    private fun registerUser(name: String, email: String, mobileNumber: String, location: String, password: String) {
        progressLayout.visibility = View.VISIBLE

        db.collection("users")
            .whereEqualTo("mobileNumber", mobileNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "mobileNumber" to mobileNumber,
                        "location" to location,
                        "password" to hashedPassword
                    )

                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener {

                            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            sharedPreferences.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putString("name", name)
                                putString("mobile", mobileNumber)
                                putString("address", location)
                                putString("email", email)
                                apply()
                            }
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                            progressLayout.visibility = View.GONE
                        }
                } else {
                    Toast.makeText(this, "Mobile number already registered", Toast.LENGTH_SHORT).show()
                    progressLayout.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking mobile number", Toast.LENGTH_SHORT).show()
                progressLayout.visibility = View.GONE
            }
    }

}
