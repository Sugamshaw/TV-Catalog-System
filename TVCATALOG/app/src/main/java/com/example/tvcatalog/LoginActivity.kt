package com.example.tvcatalog

import Models.CUSTOMER_SETTINGS
import Models.Customer_settings
import Models.USER_NODE
import Models.User
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tvcatalog.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var user = User()

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
                            updateUserSetting()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
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
    private fun updateUserSetting() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            Firebase.firestore.collection(USER_NODE)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject<User>()
                        user?.let {
                            val customerSettings = Customer_settings()
                            customerSettings.autoscroll = "true"
                            customerSettings.connecttv = "true"
                            customerSettings.layoutoption="Catalog"
                            customerSettings.banneranimationoption="Default"
                            customerSettings.banneranimationspeed="1x"

                            Firebase.firestore.collection(CUSTOMER_SETTINGS)
                                .document(it.storename.toString())
                                .set(customerSettings)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to update settings", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
        }
    }
}