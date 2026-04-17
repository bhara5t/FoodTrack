package com.example.foodytrack.activity.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.ActiveOrder
import com.example.foodytrack.activity.user.OrderTrackingActivity
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class LiveOrderAdapter(private var orders: List<ActiveOrder>) :
    RecyclerView.Adapter<LiveOrderAdapter.LiveOrderViewHolder>() {

    class LiveOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: MaterialTextView = itemView.findViewById(R.id.tvOrderId)
        val tvUserName: MaterialTextView = itemView.findViewById(R.id.tvUserName)
        val tvAddress: MaterialTextView = itemView.findViewById(R.id.tvAddress)
        val tvFoodName: MaterialTextView = itemView.findViewById(R.id.tvFoodName)
        val tvQuantity: MaterialTextView = itemView.findViewById(R.id.tvQuantity)
        val tvAmount: MaterialTextView = itemView.findViewById(R.id.tvAmount)
        val tvStatus: MaterialTextView = itemView.findViewById(R.id.tvStatus)
        val btnGenerateCode: Button = itemView.findViewById(R.id.btnGenerateCode)
        val tvDeliveryCode: TextView = itemView.findViewById(R.id.tvDeliveryCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_live_order, parent, false)
        return LiveOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveOrderViewHolder, position: Int) {
        val order = orders[position]

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, OrderTrackingActivity::class.java).apply {
                putExtra("ORDER_ID", order.orderId)
            }
            context.startActivity(intent)
        }


        holder.apply {
            tvOrderId.text = "Order ID: ${order.orderId}"
            tvUserName.text = "User: ${order.userName.ifEmpty { "N/A" }}"
            tvAddress.text = "Address: ${order.userAddress.ifEmpty { "N/A" }}"
            tvFoodName.text = order.foodName.ifEmpty { "N/A" }
            tvQuantity.text = "Qty: ${order.quantity.ifEmpty { "N/A" }}"
            tvAmount.text = "₹${order.amount.ifEmpty { "N/A" }}"
            tvStatus.text = "Status: ${order.status.ifEmpty { "N/A" }}"


            btnGenerateCode.setOnClickListener {
                generateDeliveryCode(order, holder)
            }


            if (!order.deliveryCode.isNullOrEmpty()) {
                tvDeliveryCode.text = order.deliveryCode
                btnGenerateCode.text = "Regenerate Code"
            } else {
                tvDeliveryCode.text = ""
                btnGenerateCode.text = "Generate Code"
            }
        }
    }

    private fun generateDeliveryCode(order: ActiveOrder, holder: LiveOrderViewHolder) {

        val code = String.format("%06d", Random.nextInt(1000000))


        holder.tvDeliveryCode.text = code
        holder.btnGenerateCode.text = "Regenerate Code"


        FirebaseDatabase.getInstance().reference
            .child("activeOrders")
            .child(order.orderId)
            .child("deliveryCode")
            .setValue(code)
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<ActiveOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}