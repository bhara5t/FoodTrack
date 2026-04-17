package com.example.foodytrack.activity.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val orderId: String = "",
    val userName: String = "",
    val userMobile: String = "",
    val userAddress: String = "",
    val foodName: String = "",
    val quantity: Int = 0,
    val amount: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: String = "",
    val status: String = ""
) : Parcelable
