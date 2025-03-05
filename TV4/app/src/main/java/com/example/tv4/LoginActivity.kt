package com.example.tv4


import Models.FirebaseCustomerSettings
import Models.User
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tv4.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {
    private var selectedLayout: String ?= null
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var user = User()

        val settings = FirebaseCustomerSettings()
        settings.layouttype { it ->
            val customerSettings = it
            selectedLayout = customerSettings?.layoutoption ?: "Catalog" // Default fallback
            println("selectedLayout : $selectedLayout")
        }
        

        binding.btnLogin.setOnClickListener {
            user.email = binding.txtEmail.editText?.text.toString()
            user.password = binding.txtPassword.editText?.text.toString()
            if (binding.txtEmail.toString().equals("") or binding.txtPassword.toString()
                    .equals("")

            ) {
                Toast.makeText(this@LoginActivity, "Enter values", Toast.LENGTH_SHORT).show()
            } else {
                Firebase.auth.signInWithEmailAndPassword(
                    binding.txtEmail.editText?.text.toString(),
                    binding.txtPassword.editText?.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            val nextActivity = when (selectedLayout) {
                                "Banner" -> MainActivity4::class.java
                                "Catalog" -> MainActivity2::class.java
                                else -> MainActivity2::class.java
                            }
                            startActivity(Intent(this@LoginActivity, nextActivity))
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "not successful",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

}