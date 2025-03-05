package com.example.tvcatalog

import Models.Catalog
import Models.USER_CATALOG
import Models.USER_NODE
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tvcatalog.databinding.ActivityCatalogDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class CatalogDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatalogDetailBinding
    private var storeName: String? = null
    private lateinit var catalogItem: Catalog
    private var isUserInteracting = false // Prevent unnecessary Firestore updates

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCatalogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Get the Catalog object from intent
        catalogItem = intent.getSerializableExtra("catalog_item") as? Catalog ?: return

        // Populate UI with catalog details
        binding.toolbar.title = "Catalog Details"
        binding.itemNo.text = "Item No: " + (catalogItem.itemid ?: "Unknown")
        binding.catalogName.text = "Catalog Name: " + (catalogItem.catalogname ?: "No Catalog Name Available")
        binding.productTitle.text = "Product Title: " + (catalogItem.producttitle ?: "No Title Available")
        binding.productDescription.text = "Product Description: " + (catalogItem.productdescription ?: "No Description")
        binding.pricePerKg.text = catalogItem.priceperkg ?: "Price Not Available"
        binding.mrpPrice.text = catalogItem.mrp?.let { "MRP: ₹$it" } ?: "No MRP"
        binding.customSwitch.isChecked = catalogItem.catalogstatus == "on"

        // Load the image using Glide
        Glide.with(this)
            .load(catalogItem.productimage)
            .placeholder(R.drawable.post) // Show loading placeholder
            .error(R.drawable.post) // Default image if loading fails
            .into(binding.productImage)

        binding.toolbar.setNavigationOnClickListener {
            finish() // Close activity
        }

        // Fetch store name and then update the details
        Firebase.firestore.collection(USER_NODE)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    storeName = document.getString("storename").toString()
                    updateDetails()
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.message.toString())
            }

        // Handle switch toggle to update Firestore
        binding.customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isUserInteracting) {
                updateFirestore(isChecked)
            }
        }
    }

    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
                val sharedPreferences = getSharedPreferences(getString(R.string.data_file), MODE_PRIVATE)
                sharedPreferences.edit().putString("activityselected", "editupdatecatalog").apply()

                // Start CatalogDetailActivity
                val intent = Intent(this, AddCatalogBannerActivity::class.java)
                intent.putExtra("catalog_item", catalogItem) // Ensure catalogItem is properly initialized
                startActivity(intent)

                finish()
                return true
            }
//            R.id.action_delete -> {
//                Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show()
//                return true
//            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun updateDetails() {
        val subCatalog = "${catalogItem.catalogname}${catalogItem.itemid}"
        val catalogRef = Firebase.firestore.collection(USER_CATALOG)
            .document(storeName!!)
            .collection(catalogItem.catalogname!!)
            .document(subCatalog)

        // Fetch the catalog status once
        catalogRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fetchedItem = document.toObject<Catalog>()
                    val isAvailable = if (fetchedItem?.catalogstatus == "on") true else false

                    isUserInteracting = false // Temporarily disable listener
                    binding.customSwitch.isChecked = isAvailable
                    isUserInteracting = true // Enable listener back
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching catalog details", e)
            }
    }

    private fun updateFirestore(isChecked: Boolean) {
        val subCatalog = "${catalogItem.catalogname}${catalogItem.itemid}"
        val catalogRef = Firebase.firestore.collection(USER_CATALOG)
            .document(storeName!!)
            .collection(catalogItem.catalogname!!)
            .document(subCatalog)
        val details = if (isChecked) "on" else "off"

        catalogRef.update("catalogstatus", details)
            .addOnSuccessListener {
                Log.d("Firestore", "Switch state updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating switch state, trying to add field", e)
                // If update fails, set the field with merge = true
                val updateData = mapOf("catalogstatus" to isChecked.toString())
                catalogRef.set(updateData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firestore", "Field added and switch state updated")
                    }
                    .addOnFailureListener { ex ->
                        Log.e("Firestore", "Error adding field and updating switch state", ex)
                    }
            }

    }
}
