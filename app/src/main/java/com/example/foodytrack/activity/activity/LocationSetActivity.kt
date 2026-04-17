package com.example.foodytrack.activity.activity

import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodytrack.R
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LocationSetActivity : AppCompatActivity() {

    private lateinit var locationStatus: TextView
    private lateinit var latitudeText: TextInputEditText
    private lateinit var longitudeText: TextInputEditText
    private lateinit var foodName: TextView
    private lateinit var quantity: TextView
    private lateinit var totalPrice: TextView
    private lateinit var setLocationButton: FloatingActionButton
    private lateinit var purchaseNowButton: Button
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: GeoPoint? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backArrow: ImageView


    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                this,
                "Location permission is required to show your current location",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_set)

        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            initializeViews()
            setupBackNavigation()
            displayIntentData()
            setupClickListeners()
            checkLocationPermission()
            initializeMap()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun setupBackNavigation() {
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }
    private fun initializeMap() {
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(15.0)
    }

    private fun setupClickListeners() {
        setLocationButton.setOnClickListener {
            getCurrentLocation()
        }

        purchaseNowButton.setOnClickListener {
            showPurchaseConfirmation()
        }
    }

    private fun showPurchaseConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Purchase")
            .setMessage("Are you sure you want to purchase?")
            .setPositiveButton("Yes") { _, _ ->
                saveOrderToFirestore()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveOrderToFirestore() {
        val orderId = UUID.randomUUID().toString()
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val orderData = hashMapOf(
            "orderId" to orderId,
            "userName" to sharedPreferences.getString("name", ""),
            "userMobile" to sharedPreferences.getString("mobile", ""),
            "userAddress" to sharedPreferences.getString("address", ""),
            "foodName" to intent.getStringExtra("food_name"),
            "quantity" to intent.getIntExtra("quantity", 0),
            "amount" to (intent.getDoubleExtra("price", 0.0) * intent.getIntExtra("quantity", 0)),
            "latitude" to currentLocation?.latitude,
            "longitude" to currentLocation?.longitude,
            "timestamp" to currentTime,
            "status" to "pending"
        )

        db.collection("orders")
            .document(orderId)
            .set(orderData)
            .addOnSuccessListener {
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayIntentData() {
        try {
            val foodNameStr = intent.getStringExtra("food_name") ?: "Unknown Food"
            val quantityVal = intent.getIntExtra("quantity", 0)
            val priceVal = intent.getDoubleExtra("price", 0.0)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

            foodName.text = foodNameStr
            quantity.text = "Quantity: $quantityVal"
            totalPrice.text = currencyFormat.format(priceVal * quantityVal)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        backArrow = findViewById(R.id.backArrow)
        locationStatus = findViewById(R.id.locationStatus)
        latitudeText = findViewById(R.id.latitudeText)
        longitudeText = findViewById(R.id.longitudeText)
        foodName = findViewById(R.id.foodName)
        quantity = findViewById(R.id.quantity)
        totalPrice = findViewById(R.id.totalPrice)
        setLocationButton = findViewById(R.id.setLocationButton)
        purchaseNowButton = findViewById(R.id.purchaseNowButton)
        mapView = findViewById(R.id.mapView)
    }
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            val cancellationToken = object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken)
                .addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = GeoPoint(it.latitude, it.longitude)
                        locationStatus.text = "Location Set"
                        latitudeText.setText(String.format("%.4f", it.latitude))
                        longitudeText.setText(String.format("%.4f", it.longitude))
                        updateMap(currentLocation!!)
                    } ?: Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMap(geoPoint: GeoPoint) {
        if (::mapController.isInitialized) {
            mapController.setCenter(geoPoint)
            mapView.overlays.clear()

            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Current Location"
            }
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }
}