package com.example.tvcatalog

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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler(Looper.getMainLooper()).postDelayed({
            if(FirebaseAuth.getInstance().currentUser==null)
            {
                Toast.makeText(this,"Hello", Toast.LENGTH_LONG).show()
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
            else{
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
            }

        },1000)
    }
}