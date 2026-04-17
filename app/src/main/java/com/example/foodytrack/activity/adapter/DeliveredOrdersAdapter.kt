package com.example.foodytrack.activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.ActiveOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveredOrdersAdapter(
    private val orders: List<ActiveOrder>
) : RecyclerView.Adapter<DeliveredOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val foodDetailsTextView: TextView = itemView.findViewById(R.id.foodDetailsTextView)
        val orderDetailsTextView: TextView = itemView.findViewById(R.id.orderDetailsTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item_view, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        val order = orders[position]
        holder.orderIdTextView.text = "Order ID: ${order.orderId}"
        holder.userNameTextView.text = "Customer: ${order.userName}"
        holder.foodDetailsTextView.text = "Food: ${order.foodName} (Qty: ${order.quantity})"
        holder.orderDetailsTextView.text = "Amount: ₹${order.amount}"
        holder.timestampTextView.text = "Delivered: ${formatTimestamp(order.deliveryTime)}"

    }
    private fun formatTimestamp(timestamp: Long?): String {
        return if (timestamp != null) {
            SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                .format(Date(timestamp))
        } else {
            "N/A"
        }
    }
}
