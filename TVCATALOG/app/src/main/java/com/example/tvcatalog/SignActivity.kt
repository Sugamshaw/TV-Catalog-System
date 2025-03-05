package com.example.tvcatalog

import Models.USER_NODE
import Models.User
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tvcatalog.databinding.ActivitySignBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class SignActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignBinding
    private lateinit var user: User
    private val db: FirebaseFirestore = Firebase.firestore
    private var uri: Uri? = null
    private lateinit var galleryImage: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = User()

        val loginText =
            "<font color=#FF000000>Already have an Account</font> <font color=#1E88E5> Login?</font>"
        binding.txtLogin.text = Html.fromHtml(loginText)

        galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    binding.profileImage.setImageURI(it)
                    uri = it
                }
            }
        )

        binding.profileImage.setOnClickListener { selectImage() }
        binding.addImage.setOnClickListener { selectImage() }
        binding.btnSignup.setOnClickListener {

            handleSignupOrUpgrade()
        }
    }

    private fun selectImage() {
        galleryImage.launch("image/*")
    }

    private fun uploadImage(onSuccess: (String) -> Unit) {
        var imageUrl: String? = null
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        uri?.let { imageUri ->
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profileImage/${user.storename}_$userId")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        imageUrl=downloadUrl.toString()
                        onSuccess(imageUrl!!)  // Return the URL after upload completes
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Upload failed: ${e.message}")
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
//                    onFailure()
                }
        } ?: run {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
//            onFailure()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun handleSignupOrUpgrade() {
        if (binding.txtName.editText?.text.isNullOrBlank() ||
            binding.txtEmail.editText?.text.isNullOrBlank() ||
            binding.txtPassword.editText?.text.isNullOrBlank()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (intent.hasExtra("MODE") && intent.getIntExtra("MODE", -1) == 1) {
            if (currentUserId != null) {
                db.collection(USER_NODE).document(currentUserId).set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        navigateToHome()
                    }
            }
        } else {
            val email = binding.txtEmail.editText?.text.toString()
            val password = binding.txtPassword.editText?.text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.storename = binding.txtName.editText?.text.toString()
                        user.email = email
                        user.password = password
                        if (uri != null) {
                            uploadImage(){
                                imageUrl ->
                                user.image = imageUrl
                                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                                    db.collection(USER_NODE).document(uid).set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT)
                                                .show()
                                            navigateToHome()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.d("TAG", "Signup failed", e)
                                            Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                val sharedPreferences =
                                    getSharedPreferences(getString(R.string.data_file), MODE_PRIVATE)
                                sharedPreferences.edit().putString("userdocumentname", user.storename)
                                    .apply()
                            }
                        }

                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "This email is already in use."
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                            is FirebaseAuthWeakPasswordException -> "Weak password. Choose a stronger one."
                            else -> task.exception?.localizedMessage ?: "Signup failed. Try again."
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("Error","Error occurred: ${e.message}")

                }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

