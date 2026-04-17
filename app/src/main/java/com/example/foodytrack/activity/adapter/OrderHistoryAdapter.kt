package com.example.foodytrack.activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.Order
import com.google.android.material.textview.MaterialTextView


class OrderHistoryAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history_layout, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: MaterialTextView = itemView.findViewById(R.id.tvFoodName)
        private val tvQuantity: MaterialTextView = itemView.findViewById(R.id.tvQuantity)
        private val tvOrderTime: MaterialTextView = itemView.findViewById(R.id.tvOrderTime)
        private val tvPending: MaterialTextView = itemView.findViewById(R.id.tvPending)
        private val tvTotalPrice: MaterialTextView = itemView.findViewById(R.id.tvTotalPrice)


        fun bind(order: Order) {
            tvFoodName.text = order.foodName
            tvQuantity.text = "Qty: ${order.quantity}"
            tvOrderTime.text = order.timestamp
            tvPending.text = when(order.status) {
                "delivered" -> "Delivered ✅"
                "pending" -> "Pending ⏳"
                else -> order.status
            }
            tvTotalPrice.text = "₹${order.amount}"
        }
    }
}