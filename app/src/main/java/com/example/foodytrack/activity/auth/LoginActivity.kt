package com.example.foodytrack.activity.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodytrack.R
import com.example.foodytrack.activity.admin.AdminActivity
import com.example.foodytrack.activity.delivery.DeliveryBoyActivity
import com.example.foodytrack.activity.user.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView
    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var progressLayout: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        btnLogin = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        progressLayout = findViewById(R.id.progressLayout)

        btnLogin.setOnClickListener {
            val mobileNumber = etMobileNumber.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(mobileNumber, password)) {
                loginWithMobileAndPassword(mobileNumber, password)
            }
        }

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<ImageView>(R.id.adminIcon).setOnClickListener {
            showLoginDialog("Admin")
        }

        findViewById<ImageView>(R.id.deliveryBoyIcon).setOnClickListener {
            showLoginDialog("Delivery Boy")
        }
    }

    private fun validateInput(mobileNumber: String, password: String): Boolean {
        if (TextUtils.isEmpty(mobileNumber)) {
            etMobileNumber.error = "Mobile number is required"
            return false
        }
        if (mobileNumber.length != 10) {
            etMobileNumber.error = "Enter a valid 10-digit mobile number"
            return false
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun loginWithMobileAndPassword(mobileNumber: String, password: String) {
        progressLayout.visibility = View.VISIBLE

        db.collection("users")
            .whereEqualTo("mobileNumber", mobileNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0]
                    val storedHashedPassword = user.getString("password")

                    if (storedHashedPassword != null && BCrypt.checkpw(password, storedHashedPassword)) {
                        val userName = user.getString("name")
                        val userMobile = user.getString("mobileNumber")
                        val userAddress = user.getString("location")
                        val userEmail = user.getString("email")

                        sharedPreferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("name", userName)
                            .putString("mobile", userMobile)
                            .putString("address", userAddress)
                            .putString("email", userEmail)
                            .apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }else {
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Mobile number not registered", Toast.LENGTH_SHORT).show()
                }
                progressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progressLayout.visibility = View.GONE
            }
    }

    private fun showLoginDialog(userType: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressLayout1)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("$userType Login")
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.btnVerify).setOnClickListener {
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            val collectionPath = if (userType == "Admin") "admins" else "delivery_boys"

            db.collection(collectionPath)
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener { documents ->
                    progressBar.visibility = View.GONE
                    if (!documents.isEmpty) {
                        val user = documents.documents[0]
                        val storedHashedPassword = user.getString("password")

                        if (storedHashedPassword != null && BCrypt.checkpw(password, storedHashedPassword)) {

                            sharedPreferences.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("userType", userType)
                                .putString("name", user.getString("name"))
                                .apply()

                            Toast.makeText(this, "$userType Login Successful", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()

                            val intent = if (userType == "Admin") {
                                Intent(this, AdminActivity::class.java)
                            } else {
                                Intent(this, DeliveryBoyActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User Not Found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}