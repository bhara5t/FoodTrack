package com.example.foodytrack.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.ActiveOrder
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var viewLocationButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var deliveryCodeInput: EditText
    private lateinit var verifyCodeButton: Button
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var orderId: String = "N/A"
    private var isDeliveryActive: Boolean = false
    private var status: String = "pending"
    private var deliveryCode: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var backArrow: ImageView

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (isDeliveryActive) startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        initializeViews()
        setupLocationServices()
        setupButtonClickListeners()
    }

    private fun initializeViews() {
        val orderIdTextView = findViewById<TextView>(R.id.orderIdTextView)
        val userNameTextView = findViewById<TextView>(R.id.userNameTextView)
        val userMobileTextView = findViewById<TextView>(R.id.userMobileTextView)
        val userAddressTextView = findViewById<TextView>(R.id.userAddressTextView)
        val foodNameTextView = findViewById<TextView>(R.id.foodNameTextView)
        val quantityTextView = findViewById<TextView>(R.id.quantityTextView)
        val amountTextView = findViewById<TextView>(R.id.amountTextView)
        statusTextView = findViewById(R.id.statusTextView)

        startButton = findViewById(R.id.btnStart)
        stopButton = findViewById(R.id.btnStop)
        viewLocationButton = findViewById(R.id.btnSeeLocation)
        deliveryCodeInput = findViewById(R.id.deliveryCodeInput)
        verifyCodeButton = findViewById(R.id.btnVerifyCode)
        backArrow = findViewById(R.id.backArrow)


        // Retrieve and display order details
        orderId = intent.getStringExtra("orderId") ?: "N/A"
        val userName = intent.getStringExtra("userName") ?: "N/A"
        val userMobile = intent.getStringExtra("userMobile") ?: "N/A"
        val userAddress = intent.getStringExtra("userAddress") ?: "N/A"
        val foodName = intent.getStringExtra("foodName") ?: "N/A"
        val quantity = intent.getStringExtra("quantity") ?: "N/A"
        val amount = intent.getStringExtra("amount") ?: "N/A"
        status = intent.getStringExtra("status") ?: "pending"
        userLatitude = intent.getDoubleExtra("userLatitude", 0.0)
        userLongitude = intent.getDoubleExtra("userLongitude", 0.0)
        deliveryCode = intent.getStringExtra("deliveryCode")

        orderIdTextView.text = "Order ID: $orderId"
        userNameTextView.text = "User: $userName"
        userMobileTextView.text = "Mobile: $userMobile"
        userAddressTextView.text = "Address: $userAddress"
        foodNameTextView.text = "Food Item: $foodName"
        quantityTextView.text = "Quantity: $quantity"
        amountTextView.text = "Amount: ₹$amount"
        statusTextView.text = "Status: $status"

        backArrow.setOnClickListener {
            onBackPressed()
        }

        updateButtonStates()

    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5_000
            fastestInterval = 3_000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    FirebaseDatabase.getInstance().getReference("activeOrders")
                        .child(orderId)
                        .child("deliveryLocation")
                        .setValue(
                            hashMapOf(
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                }
            }
        }
    }

    private fun setupButtonClickListeners() {
        startButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startDeliveryTracking()
            } else {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        stopButton.setOnClickListener {
            stopDeliveryTracking()
        }

        viewLocationButton.setOnClickListener {
            showUserLocation()
        }

        verifyCodeButton.setOnClickListener {
            verifyDeliveryCode()
        }
    }
    @SuppressLint("MissingPermission")
    private fun startDeliveryTracking() {
        if (!isDeliveryActive) {
            FirebaseDatabase.getInstance().getReference("activeOrders")
                .child(orderId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {

                        startLocationUpdates()
                        status = "active"
                        isDeliveryActive = true
                        updateButtonStates()
                        Toast.makeText(this, "Location sharing resumed", Toast.LENGTH_SHORT).show()
                    } else {

                        val orderData = hashMapOf(
                            "orderId" to orderId,
                            "userName" to intent.getStringExtra("userName"),
                            "userMobile" to intent.getStringExtra("userMobile"),
                            "userAddress" to intent.getStringExtra("userAddress"),
                            "foodName" to intent.getStringExtra("foodName"),
                            "quantity" to intent.getStringExtra("quantity"),
                            "amount" to intent.getStringExtra("amount"),
                            "userLocation" to hashMapOf(
                                "latitude" to userLatitude,
                                "longitude" to userLongitude
                            ),
                            "deliveryLocation" to hashMapOf(
                                "latitude" to 0.0,
                                "longitude" to 0.0,
                                "timestamp" to System.currentTimeMillis()
                            ),
                            "status" to "active"
                        )

                        if (!deliveryCode.isNullOrEmpty()) {
                            orderData["deliveryCode"] = deliveryCode!!
                        }

                        FirebaseDatabase.getInstance().getReference("activeOrders")
                            .child(orderId)
                            .setValue(orderData)
                            .addOnSuccessListener {
                                startLocationUpdates()
                                status = "active"
                                isDeliveryActive = true
                                updateButtonStates()
                                Toast.makeText(this, "Delivery started", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to start tracking", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isDeliveryActive = true
    }

    private fun showUserLocation() {
        if (userLatitude == 0.0 && userLongitude == 0.0) {
            Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val uri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1" +
                            "&origin=${it.latitude},${it.longitude}" +
                            "&destination=${userLatitude},${userLongitude}" +
                            "&travelmode=driving"
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopDeliveryTracking() {
        if (!isDeliveryActive) return

        if (status == "completed") {
            val realtimeDb = FirebaseDatabase.getInstance()
            val firestoreDb = FirebaseFirestore.getInstance()

            realtimeDb.reference.child("activeOrders").child(orderId).get()
                .addOnSuccessListener { snapshot ->
                    val order = snapshot.getValue<ActiveOrder>() ?: return@addOnSuccessListener

                    firestoreDb.collection("delivered_orders")
                        .document(orderId)
                        .set(order.copy(status = "delivered"))
                        .addOnSuccessListener {

                            firestoreDb.collection("orders")
                                .whereEqualTo("orderId", orderId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    querySnapshot.documents.forEach { doc ->
                                        doc.reference.update("status", "delivered")
                                    }
                                }

                            realtimeDb.reference.child("activeOrders").child(orderId)
                                .removeValue()
                                .addOnSuccessListener {
                                    fusedLocationClient.removeLocationUpdates(locationCallback)
                                    isDeliveryActive = false
                                    updateButtonStates()
                                    Toast.makeText(this, "Delivery completed", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        } else {
            FirebaseDatabase.getInstance().getReference("activeOrders")
                .child(orderId)
                .removeValue()
                .addOnSuccessListener {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    isDeliveryActive = false
                    updateButtonStates()
                    Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun verifyDeliveryCode() {
        val enteredCode = deliveryCodeInput.text.toString().trim()
        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "Please enter delivery code", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseDatabase.getInstance().getReference("activeOrders")
            .child(orderId)
            .child("deliveryCode")
            .get()
            .addOnSuccessListener { snapshot ->
                val actualCode = snapshot.getValue(String::class.java)
                if (actualCode == enteredCode) {

                    FirebaseDatabase.getInstance().getReference("activeOrders")
                        .child(orderId)
                        .child("status")
                        .setValue("completed")
                        .addOnSuccessListener {
                            status = "completed"
                            updateButtonStates()
                            Toast.makeText(this, "Delivery completed!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateButtonStates() {
        startButton.isEnabled = !isDeliveryActive
        stopButton.isEnabled = isDeliveryActive
        viewLocationButton.isEnabled = isDeliveryActive
        verifyCodeButton.isEnabled = isDeliveryActive && status != "completed"
        statusTextView.text = "Status: $status"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isDeliveryActive) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}















