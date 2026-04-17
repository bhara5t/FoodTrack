package com.example.foodytrack.activity.user

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodytrack.R
import com.example.foodytrack.activity.data.FoodItem
import com.example.foodytrack.activity.activity.LocationSetActivity

class FullFoodActivity : AppCompatActivity() {

    private lateinit var foodImage: ImageView
    private lateinit var foodName: TextView
    private lateinit var foodDescription: TextView
    private lateinit var perPiecePrice: TextView
    private lateinit var totalPrice: TextView
    private lateinit var quantityText: TextView
    private lateinit var increaseQuantity: ImageView
    private lateinit var decreaseQuantity: ImageView
    private lateinit var deliveryNowButton: Button
    private lateinit var backView: ImageView

    private var foodItem: FoodItem? = null
    private var quantity: Int = 1
    private var pricePerPiece: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_food)

        initializeViews()


        foodItem = intent.getParcelableExtra("foodItem", FoodItem::class.java)


        displayFoodDetails()


        setupClickListeners()
    }

    private fun initializeViews() {
        foodImage = findViewById(R.id.foodDetailImage)
        foodName = findViewById(R.id.foodDetailName)
        foodDescription = findViewById(R.id.foodDetailDescription)
        perPiecePrice = findViewById(R.id.foodDetailPrice)
        totalPrice = findViewById(R.id.totalAmountView)
        quantityText = findViewById(R.id.quantityText)
        increaseQuantity = findViewById(R.id.increaseQuantity)
        decreaseQuantity = findViewById(R.id.decreaseQuantity)
        deliveryNowButton = findViewById(R.id.deliveryNowButton)
        backView = findViewById(R.id.backButton)
    }

    private fun displayFoodDetails() {
        foodItem?.let {
            try {
                Glide.with(this)
                    .load(it.imageUrl)
                    .error(R.drawable.ic_email)
                    .into(foodImage)

                foodName.text = it.name
                foodDescription.text = it.description
                pricePerPiece = it.price
                perPiecePrice.text = getString(R.string.price_per_piece_format, pricePerPiece)
                updateQuantityAndPrice()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error displaying food details", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Error loading food details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        increaseQuantity.setOnClickListener {
            if (quantity < 99) {
                quantity++
                updateQuantityAndPrice()
            } else {
                Toast.makeText(this, "Maximum quantity reached", Toast.LENGTH_SHORT).show()
            }
        }

        decreaseQuantity.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityAndPrice()
            } else {
                Toast.makeText(this, "Minimum quantity is 1", Toast.LENGTH_SHORT).show()
            }
        }

        deliveryNowButton.setOnClickListener {
            foodItem?.let {
                val intent = Intent(this, LocationSetActivity::class.java).apply {
                    putExtra("food_name", it.name)
                    putExtra("quantity", quantity)
                    putExtra("price", it.price)
                }
                startActivity(intent)
            }
        }

        backView.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


    }

    private fun updateQuantityAndPrice() {
        quantityText.text = quantity.toString()
        val total = pricePerPiece * quantity
        totalPrice.text = getString(R.string.price_format, total)
    }
}