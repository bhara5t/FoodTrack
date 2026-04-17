package com.example.foodytrack.activity.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.adapter.FoodAdapter
import com.example.foodytrack.activity.data.FoodItem
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore


class AFoodRemoveActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var backButton: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val foodList = mutableListOf<FoodItem>()
    private val docIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afood_remove)

        recyclerView = findViewById(R.id.recyclerViewFoods)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backArrow)

        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener { finish() }

        loadFoods()
    }

    private fun loadFoods() {
        progressBar.visibility = View.VISIBLE

        db.collection("foods")
            .get()
            .addOnSuccessListener { result ->

                foodList.clear()
                docIds.clear()

                for (doc in result) {
                    val item = doc.toObject(FoodItem::class.java)
                    foodList.add(item)
                    docIds.add(doc.id)
                }

                recyclerView.adapter = FoodAdapter(
                    this,
                    foodList,
                    isAdminMode = true
                ) { food, position ->
                    showDeleteDialog(position)
                }

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load foods", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Food")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFood(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFood(position: Int) {
        progressBar.visibility = View.VISIBLE

        val docId = docIds[position]

        db.collection("foods")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                loadFoods()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
            }
    }
}