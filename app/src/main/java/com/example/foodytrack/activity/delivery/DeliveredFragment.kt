package com.example.foodytrack.activity.delivery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.adapter.DeliveredOrdersAdapter
import com.example.foodytrack.activity.data.ActiveOrder

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DeliveredFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var adapter: DeliveredOrdersAdapter
    private val completedOrdersList = mutableListOf<ActiveOrder>()
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivered, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.deliveredOrdersRecyclerView)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DeliveredOrdersAdapter(completedOrdersList)
        recyclerView.adapter = adapter
        db = FirebaseFirestore.getInstance()
        loadCompletedOrders()

        return view
    }

    private fun loadCompletedOrders() {
        progressBar.visibility = View.VISIBLE
        completedOrdersList.clear()

        db.collection("delivered_orders") // Match your Firestore collection name
            .orderBy("deliveryTime", Query.Direction.DESCENDING) // Add timestamp field
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    showEmptyState()
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    completedOrdersList.clear()
                    for (document in it.documents) {
                        document.toObject(ActiveOrder::class.java)?.let { order ->
                            completedOrdersList.add(order)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE

                    if (completedOrdersList.isEmpty()) {
                        showEmptyState()
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyStateView.visibility = View.GONE
                    }
                }
            }
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
}