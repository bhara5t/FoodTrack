package com.example.foodytrack.activity.user

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodytrack.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var database: DatabaseReference
    private var orderId: String = ""
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        database = FirebaseDatabase.getInstance().reference


        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        database.child("activeOrders").child(orderId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLocation = snapshot.child("userLocation")
                userLatitude = userLocation.child("latitude").getValue(Double::class.java) ?: 0.0
                userLongitude = userLocation.child("longitude").getValue(Double::class.java) ?: 0.0


                startDeliveryLocationUpdates()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun startDeliveryLocationUpdates() {
        progressBar.visibility = View.VISIBLE
        database.child("activeOrders").child(orderId).child("deliveryLocation")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val deliveryLatitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val deliveryLongitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0


                    val mapUrl = "https://www.google.com/maps/dir/?api=1" +
                            "&origin=$userLatitude,$userLongitude" +
                            "&destination=$deliveryLatitude,$deliveryLongitude" +
                            "&travelmode=driving" +
                            "&ts=${System.currentTimeMillis()}"
                    webView.loadUrl(mapUrl)

                    webView.settings.javaScriptEnabled = true
                    webView.loadUrl(mapUrl)
                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}