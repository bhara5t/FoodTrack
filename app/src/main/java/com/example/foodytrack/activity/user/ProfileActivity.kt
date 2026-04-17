package com.example.foodytrack.activity.user


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mobileEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var saveChangesButton: com.google.android.material.button.MaterialButton
    private lateinit var progressBar: View
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        mobileEditText = findViewById(R.id.mobileEditText)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        locationEditText = findViewById(R.id.locationEditText)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        progressBar = findViewById(R.id.progressBar)
        backArrow = findViewById(R.id.backArrow)

        backArrow.setOnClickListener {
            onBackPressed()
        }

        fetchUserData()

        saveChangesButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun fetchUserData() {
        val mobileNumber = sharedPreferences.getString("mobile", "")
        if (mobileNumber.isNullOrEmpty()) {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE

        db.collection("users")
            .whereEqualTo("mobileNumber", mobileNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.first().toObject(User::class.java)

                    mobileEditText.setText(user.mobileNumber)
                    nameEditText.setText(user.name)
                    emailEditText.setText(user.email)
                    locationEditText.setText(user.location)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                finish()
            }
    }

    private fun saveChanges() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        val mobileNumber = sharedPreferences.getString("mobile", "")
        if (mobileNumber.isNullOrEmpty()) {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("users")
            .whereEqualTo("mobileNumber", mobileNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documentId = documents.first().id
                    val updatedUser = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "location" to location
                    )

                    db.collection("users")
                        .document(documentId)
                        .update(updatedUser as Map<String, Any>)
                        .addOnSuccessListener {
                            // Update SharedPreferences
                            sharedPreferences.edit().apply {
                                putString("name", name)
                                putString("email", email)
                                putString("address", location)
                                apply()
                            }

                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
}