package com.example.foodytrack.activity.delivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodytrack.activity.OrderDetailActivity
import com.example.foodytrack.activity.adapter.OrderAdapter
import com.example.foodytrack.activity.data.Order
import com.example.foodytrack.databinding.FragmentOrderBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderFragment : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private val ordersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderAdapter = OrderAdapter(ordersList) { selectedOrder ->
            val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                putExtra("orderId", selectedOrder.orderId)
                putExtra("userName", selectedOrder.userName)
                putExtra("userMobile", selectedOrder.userMobile)
                putExtra("userAddress", selectedOrder.userAddress)
                putExtra("foodName", selectedOrder.foodName)
                putExtra("userLatitude", selectedOrder.latitude)
                putExtra("userLongitude", selectedOrder.longitude)
                putExtra("quantity", selectedOrder.quantity.toString())
                putExtra("amount", selectedOrder.amount.toString())
                putExtra("status", selectedOrder.status)
            }
            startActivity(intent)
        }

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }

        fetchOrders()
    }

    private fun fetchOrders() {
        binding.progressBar.visibility = View.VISIBLE
        binding.ordersRecyclerView.visibility = View.GONE
        binding.noOrdersTextView.visibility = View.GONE

        db.collection("orders")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                ordersList.clear()
                for (document in querySnapshot.documents) {
                    val order = document.toObject(Order::class.java)
                    order?.let {
                        ordersList.add(it)
                    }
                }

                // Update UI
                if (ordersList.isEmpty()) {
                    binding.noOrdersTextView.visibility = View.VISIBLE
                } else {
                    binding.ordersRecyclerView.visibility = View.VISIBLE
                    orderAdapter.notifyDataSetChanged()

                    binding.ordersRecyclerView.scrollToPosition(ordersList.size - 1)
                }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.noOrdersTextView.text = "Error loading orders: ${e.message}"
                binding.noOrdersTextView.visibility = View.VISIBLE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}