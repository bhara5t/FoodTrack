package com.example.foodytrack.activity

import org.mindrot.jbcrypt.BCrypt

fun main() {

    print("Enter Name: ")
    val name = readLine()

    print("Enter Password: ")
    val password = readLine()

    if (!name.isNullOrEmpty() && !password.isNullOrEmpty()) {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        println("\n===== GENERATED DATA =====")
        println("Name: $name")
        println("Hashed Password: $hash")
    } else {
        println("Invalid input")
    }
}