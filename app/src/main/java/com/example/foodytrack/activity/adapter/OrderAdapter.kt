package com.example.foodytrack.activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.Order

class OrderAdapter(
    private val ordersList: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val foodDetailsTextView: TextView = itemView.findViewById(R.id.foodDetailsTextView)
        private val orderDetailsTextView: TextView = itemView.findViewById(R.id.orderDetailsTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(order: Order) {
            orderIdTextView.text = "Order ID: ${order.orderId}"
            userNameTextView.text = "Customer: ${order.userName}"
            foodDetailsTextView.text = "Food: ${order.foodName} (Qty: ${order.quantity})"
            orderDetailsTextView.text = "Amount: ₹${order.amount} | Status: ${order.status}"
            timestampTextView.text = "Timestamp: ${order.timestamp}"

            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item_view, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(ordersList[position])
    }

    override fun getItemCount() = ordersList.size
}
