package com.example.foodytrack.activity.data

data class ActiveOrder(
    val orderId: String = "",
    val userName: String = "",
    val userMobile: String = "",
    val userAddress: String = "",
    val foodName: String = "",
    val quantity: String = "",
    val amount: String = "",
    val userLocation: Map<String, Double>? = null,
    val deliveryLocation: Map<String, Any>? = null,
    val status: String = "",
    val deliveryCode: String? = null,
    val deliveryTime: Long = System.currentTimeMillis()

)