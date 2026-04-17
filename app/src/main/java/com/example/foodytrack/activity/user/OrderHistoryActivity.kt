package com.example.foodytrack.activity.user

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.adapter.OrderHistoryAdapter
import com.example.foodytrack.activity.data.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale


class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoOrders: TextView
    private lateinit var adapter: OrderHistoryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerView = findViewById(R.id.rvOrderHistory)
        progressBar = findViewById(R.id.progressBar)
        tvNoOrders = findViewById(R.id.tvNoOrders)

        backArrow = findViewById(R.id.backArrow)

        backArrow.setOnClickListener {
            onBackPressed()
        }

        db = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderHistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchOrders()
    }

    private fun fetchOrders() {

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoOrders.visibility = View.GONE

        val userMobile = sharedPreferences.getString("mobile", null)
        if (userMobile == null) {

            progressBar.visibility = View.GONE
            tvNoOrders.visibility = View.VISIBLE
            tvNoOrders.text = "User not logged in."
            return
        }

        db.collection("orders")
            .whereEqualTo("userMobile", userMobile)
            .get()
            .addOnSuccessListener { result ->
                val orders = result.toObjects(Order::class.java)
                if (orders.isEmpty()) {

                    tvNoOrders.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {

                    val sortedOrders = orders.sortedByDescending { order ->

                        try {

                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            sdf.parse(order.timestamp)?.time ?: 0
                        } catch (e: Exception) {
                            0
                        }
                    }

                    adapter = OrderHistoryAdapter(sortedOrders)
                    recyclerView.adapter = adapter
                    recyclerView.visibility = View.VISIBLE
                    tvNoOrders.visibility = View.GONE
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->

                progressBar.visibility = View.GONE
                tvNoOrders.visibility = View.VISIBLE
                tvNoOrders.text = "Failed to load orders: ${exception.message}"
            }
    }
}
