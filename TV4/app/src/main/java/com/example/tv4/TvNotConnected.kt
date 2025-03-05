package com.example.tv4

import Models.FirebaseCustomerSettings
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TvNotConnected : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tv_not_connected)
        val settings = FirebaseCustomerSettings()
        settings.layouttype { it ->
            val customerSettings = it
            val selectedLayout = customerSettings?.layoutoption ?: "Catalog" // Default fallback
            val tvconnection = customerSettings?.connecttv ?: "false"
            println("selectedLayout : $selectedLayout")
            println("tvconnection : $tvconnection")
            if (tvconnection == "false") {
                // Directly go to MainActivity2 and return, skipping Handler block
                val nextActivity = when (selectedLayout) {
                    "Banner" -> MainActivity4::class.java
                    "Catalog" -> MainActivity2::class.java
                    else -> MainActivity2::class.java
                }
                startActivity(Intent(this@TvNotConnected, nextActivity))
                finish()
            }


        }

    }
}