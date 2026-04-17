package com.example.foodytrack.activity.admin

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.foodytrack.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class AFoodAddActivity : AppCompatActivity() {

    private lateinit var ivFoodImage: ImageView
    private lateinit var etFoodName: EditText
    private lateinit var etFoodDescription: EditText
    private lateinit var etFoodPrice: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var btnSaveFood: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var backButton: ImageView
    private lateinit var mainLayout: ConstraintLayout

    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()

    private val IMGUR_CLIENT_ID = "49604f820671051"
    private val IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image"

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            ivFoodImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afood_add)

        ivFoodImage = findViewById(R.id.ivFoodImage)
        etFoodName = findViewById(R.id.etFoodName)
        etFoodDescription = findViewById(R.id.etFoodDescription)
        etFoodPrice = findViewById(R.id.etFoodPrice)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnSaveFood = findViewById(R.id.btnSaveFood)
        btnCancel = findViewById(R.id.btnCancelFood)
        progressBar = findViewById(R.id.progressBar)
        mainLayout = findViewById(R.id.mainLayout)
        backButton = findViewById(R.id.backArrow)

        btnUploadImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnSaveFood.setOnClickListener {
            saveFoodToFirestore()
        }
        backButton.setOnClickListener {
            showCancelConfirmationDialog()
        }
        btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Food Addition")
            .setMessage("Are you sure you want to discard the food item?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveFoodToFirestore() {
        val foodName = etFoodName.text.toString().trim()
        val foodDescription = etFoodDescription.text.toString().trim()
        val foodPrice = etFoodPrice.text.toString().trim()

        if (foodName.isEmpty() || foodDescription.isEmpty() || foodPrice.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            mainLayout.alpha = 0.5f
            btnSaveFood.isEnabled = false
        }

        val imageFile = File(getRealPathFromURI(imageUri!!))
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, RequestBody.create("image/*".toMediaTypeOrNull(), imageFile))
            .build()

        val request = Request.Builder()
            .url(IMGUR_UPLOAD_URL)
            .addHeader("Authorization", "Client-ID $IMGUR_CLIENT_ID")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    mainLayout.alpha = 1f
                    btnSaveFood.isEnabled = true
                    Toast.makeText(this@AFoodAddActivity, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val imageUrl = parseImgurImageUrl(responseBody)

                    if (imageUrl != null) {

                        val food = mapOf(
                            "name" to foodName,
                            "description" to foodDescription,
                            "price" to foodPrice.toDouble(),
                            "imageUrl" to imageUrl
                        )

                        firestore.collection("foods")
                            .add(food)
                            .addOnSuccessListener {
                                runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    mainLayout.alpha = 1f
                                    showSuccessDialog()
                                }
                            }
                            .addOnFailureListener { e ->
                                runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    mainLayout.alpha = 1f
                                    btnSaveFood.isEnabled = true
                                    Toast.makeText(this@AFoodAddActivity, "Failed to add food: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        mainLayout.alpha = 1f
                        btnSaveFood.isEnabled = true
                        Toast.makeText(this@AFoodAddActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Food")
            .setMessage("Do you want to add another food item?")
            .setPositiveButton("Yes") { _, _ ->
                // Reset form fields
                ivFoodImage.setImageResource(R.drawable.ic_email)
                etFoodName.text.clear()
                etFoodDescription.text.clear()
                etFoodPrice.text.clear()
                imageUri = null
            }
            .setNegativeButton("No") { _, _ ->
                finish()
            }
            .show()
    }

    private fun parseImgurImageUrl(responseBody: String?): String? {

        if (responseBody != null) {
            try {
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse.getJSONObject("data").getString("link")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex("_data")
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath ?: ""
    }
}