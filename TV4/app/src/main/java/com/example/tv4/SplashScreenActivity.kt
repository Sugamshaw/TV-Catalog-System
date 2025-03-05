package com.example.tv4

import Models.Customer_settings
import Models.FirebaseCustomerSettings
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val settings = FirebaseCustomerSettings()
        settings.layouttype { it ->
            val customerSettings = it
            val selectedLayout = customerSettings?.layoutoption ?: "Catalog" // Default fallback
            val tvconnection = customerSettings?.connecttv ?: "false"

            println("selectedLayout : $selectedLayout")


            Handler(Looper.getMainLooper()).postDelayed({
                if (FirebaseAuth.getInstance().currentUser == null) {
                    Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    if (tvconnection == "false") {
                        // Directly go to MainActivity2 and return, skipping Handler block
                        startActivity(Intent(this, TvNotConnected::class.java))
                        finish()
                    }
                    val nextActivity = when (selectedLayout) {
                        "Banner" -> MainActivity4::class.java
                        "Catalog" -> MainActivity2::class.java
                        else -> MainActivity2::class.java
                    }
                    startActivity(Intent(this@SplashScreenActivity, nextActivity))
                }
                finish()
            }, 1000)
        }
    }
}
