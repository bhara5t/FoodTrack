package com.example.foodytrack.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.adapter.LiveOrderAdapter
import com.example.foodytrack.activity.data.ActiveOrder
import com.google.firebase.database.*

class LiveOrderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoOrders: TextView
    private lateinit var adapter: LiveOrderAdapter
    private lateinit var database: DatabaseReference
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_order)

        backArrow = findViewById(R.id.backArrow)

        backArrow.setOnClickListener {
            onBackPressed()
        }

        recyclerView = findViewById(R.id.ordersRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvNoOrders = findViewById(R.id.tvNoLiveOrders)

        database = FirebaseDatabase.getInstance().reference

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LiveOrderAdapter(mutableListOf())
        recyclerView.adapter = adapter

        fetchActiveOrders()
    }

    private fun fetchActiveOrders() {
        progressBar.visibility = View.VISIBLE
        tvNoOrders.visibility = View.GONE

        database.child("activeOrders")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<ActiveOrder>()

                    try {
                        for (orderSnapshot in snapshot.children) {

                            val orderId = orderSnapshot.child("orderId").getValue(String::class.java) ?: orderSnapshot.key ?: ""
                            val userName = orderSnapshot.child("userName").getValue(String::class.java) ?: ""
                            val userMobile = orderSnapshot.child("userMobile").getValue(String::class.java) ?: ""
                            val userAddress = orderSnapshot.child("userAddress").getValue(String::class.java) ?: ""
                            val foodName = orderSnapshot.child("foodName").getValue(String::class.java) ?: ""
                            val quantity = orderSnapshot.child("quantity").getValue(String::class.java) ?: ""
                            val amount = orderSnapshot.child("amount").getValue(String::class.java) ?: ""
                            val status = orderSnapshot.child("status").getValue(String::class.java) ?: ""
                            val deliveryCode = orderSnapshot.child("deliveryCode").getValue(String::class.java)


                            val userLocationSnapshot = orderSnapshot.child("userLocation")
                            val userLocation = if (userLocationSnapshot.exists()) {
                                val latitude = userLocationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                                val longitude = userLocationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                                mapOf("latitude" to latitude, "longitude" to longitude)
                            } else null


                            val deliveryLocationSnapshot = orderSnapshot.child("deliveryLocation")
                            val deliveryLocation = if (deliveryLocationSnapshot.exists()) {
                                val latitude = deliveryLocationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                                val longitude = deliveryLocationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                                val timestamp = deliveryLocationSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                                mapOf(
                                    "latitude" to latitude,
                                    "longitude" to longitude,
                                    "timestamp" to timestamp
                                )
                            } else null

                            val order = ActiveOrder(
                                orderId = orderId,
                                userName = userName,
                                userMobile = userMobile,
                                userAddress = userAddress,
                                foodName = foodName,
                                quantity = quantity,
                                amount = amount,
                                userLocation = userLocation,
                                deliveryLocation = deliveryLocation,
                                status = status,
                                deliveryCode = deliveryCode
                            )

                            orders.add(order)
                        }

                        progressBar.visibility = View.GONE
                        if (orders.isEmpty()) {
                            tvNoOrders.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            adapter.updateOrders(orders)
                            recyclerView.visibility = View.VISIBLE
                            tvNoOrders.visibility = View.GONE
                        }
                    } catch (e: Exception) {

                        progressBar.visibility = View.GONE
                        tvNoOrders.visibility = View.VISIBLE
                        tvNoOrders.text = "Error loading orders: ${e.message}"
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    tvNoOrders.visibility = View.VISIBLE
                    tvNoOrders.text = "Failed to load orders: ${error.message}"
                }
            })
    }
}