package com.example.foodytrack.activity.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.FoodItem
import com.example.foodytrack.activity.user.FullFoodActivity
import java.util.Locale

class FoodAdapter(
    private val context: Context,
    private val foodList: MutableList<FoodItem>,
    private val isAdminMode: Boolean = false,
    private val onDeleteClick: ((FoodItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var filteredFoodList: MutableList<FoodItem> = mutableListOf()

    init {
        filteredFoodList.addAll(foodList)
    }

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.imgFood)
        val foodName: TextView = view.findViewById(R.id.txtFoodName)
        val foodPrice: TextView = view.findViewById(R.id.txtFoodPrice)
        val foodDescription: TextView = view.findViewById(R.id.txtFoodDescription)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = filteredFoodList[position]

        holder.foodName.text = foodItem.name
        holder.foodPrice.text = "₹${foodItem.price}"
        holder.foodDescription.text = foodItem.description

        Glide.with(context)
            .load(foodItem.imageUrl)
            .into(holder.foodImage)


        if (isAdminMode) {
            holder.deleteIcon.visibility = View.VISIBLE

            holder.deleteIcon.setOnClickListener {
                onDeleteClick?.invoke(foodItem, position)
            }


            holder.itemView.setOnClickListener(null)

        } else {

            holder.deleteIcon.visibility = View.GONE

            holder.itemView.setOnClickListener {
                val intent = Intent(context, FullFoodActivity::class.java)
                intent.putExtra("foodItem", foodItem)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredFoodList.size
    }

    fun updateData(newFoodList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newFoodList)

        filteredFoodList.clear()
        filteredFoodList.addAll(newFoodList)

        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredFoodList.clear()

        if (query.isEmpty()) {
            filteredFoodList.addAll(foodList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            for (food in foodList) {
                if (food.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredFoodList.add(food)
                }
            }
        }

        notifyDataSetChanged()
    }
}