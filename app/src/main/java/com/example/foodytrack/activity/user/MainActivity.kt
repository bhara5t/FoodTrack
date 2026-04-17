package com.example.foodytrack.activity.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodytrack.R
import com.example.foodytrack.activity.LiveOrderActivity
import com.example.foodytrack.activity.auth.LoginActivity
import com.example.foodytrack.activity.adapter.FoodAdapter
import com.example.foodytrack.activity.data.FoodItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private val foodList = mutableListOf<FoodItem>()
    private lateinit var db: FirebaseFirestore
    private lateinit var searchView: SearchView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var loadingView: ProgressBar
    private lateinit var noResultsView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupDrawerNavigation()
        setupRecyclerView()
        setupSearchView()
        setupFirestore()
        fetchFoodData()
        updateNavigationHeader()
    }
    private fun updateNavigationHeader() {

        val headerView: View = navigationView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.user_name_text_view)
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("name", "Welcome, User")
        userNameTextView.text = "Welcome, $userName"
    }
    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        searchView = findViewById(R.id.searchView)
        loadingView = findViewById(R.id.loadingView)
        noResultsView = findViewById(R.id.noResultsView)

        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        searchView.apply {
            setIconifiedByDefault(false)
            queryHint = "Search for food..."
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    val intent = Intent(this, OrderHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_tracking -> {
                    val intent = Intent(this, LiveOrderActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    handleLogout()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        foodAdapter = FoodAdapter(this, foodList)
        recyclerView.adapter = foodAdapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return true
            }
        })
    }

    private fun setupFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    private fun fetchFoodData() {
        showLoading(true)
        db.collection("foods")
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val newFoodList = documents.mapNotNull { it.toObject(FoodItem::class.java) }
                updateFoodList(newFoodList)
                showLoading(false)
            }
            .addOnFailureListener {
                handleError("Failed to fetch food items")
                showLoading(false)
            }
    }

    private fun performSearch(query: String) {
        showLoading(true)
        foodAdapter.filter(query)
        updateNoResultsVisibility(foodAdapter.itemCount == 0)
        showLoading(false)
    }

    private fun updateFoodList(newFoodList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newFoodList)
        foodAdapter.updateData(newFoodList)
        updateNoResultsVisibility(newFoodList.isEmpty())
    }

    private fun handleLogout() {

        getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
            .putBoolean("isLoggedIn", false)
            .remove("name")
            .remove("mobile")
            .remove("address")
            .remove("email")
            .apply()

        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateNoResultsVisibility(show: Boolean) {
        noResultsView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
