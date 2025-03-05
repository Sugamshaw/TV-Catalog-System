package com.example.tvcatalog


import Models.Banner
import Models.Catalog
import Models.USER_BANNER
import Models.USER_CATALOG
import Models.USER_NODE
import Models.User
import android.R
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tvcatalog.databinding.ActivityAddCatalogBannerBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddCatalogBannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCatalogBannerBinding

    private lateinit var galleryImage: ActivityResultLauncher<String>
    private var uri: Uri? = null
    private var storename: String? = null

    private lateinit var catalog: Catalog
    private val catalognamelist = mutableListOf<String>()
    private val catalogtotalsizelist = mutableListOf<String>()

    private lateinit var banner: Banner
    private val bannernamelist = mutableListOf<String>()
    private val bannertotalsizelist = mutableListOf<String>()
    private lateinit var catalogUpdateItem: Catalog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCatalogBannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Firebase.firestore.disableNetwork().addOnCompleteListener {
            Firebase.firestore.enableNetwork()
        }

        galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    binding.selectProductImage.setImageURI(it)
                    uri = it
                }
            }
        )

        catalog = Catalog()
        banner = Banner()
        val sharedPreferences = getSharedPreferences(
            getString(com.example.tvcatalog.R.string.data_file),
            AppCompatActivity.MODE_PRIVATE
        )
        val activityselected = sharedPreferences.getString("activityselected", null)
        Log.d("activityselected", activityselected.toString())
        catalogUpdateItem = intent.getSerializableExtra("catalog_item") as? Catalog ?: return

        if (activityselected == "catalog") {
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storename = document.getString("storename").toString()
                        onActivitySelected(false)
                        Log.d("storename", storename.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        } else if (activityselected == "banner") {
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storename = document.getString("storename").toString()
                        onActivitySelected(true)
                        Log.d("storename", storename.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        } else if (activityselected == "catalog_name_list") {
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storename = document.getString("storename").toString()
                        onActivityNameListSelected("Catalog", USER_CATALOG, "Catalog Details")
                        Log.d("storename", storename.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        } else if (activityselected == "banner_name_list") {
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storename = document.getString("storename").toString()
                        onActivityNameListSelected("Banner", USER_BANNER, "Banner Details")
                        Log.d("storename", storename.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        } else if (activityselected == "editupdatecatalog") {
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storename = document.getString("storename").toString()
                        onActivityEditUpdateCatalog()
                        Log.d("storename", storename.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.materialToolbar.setNavigationOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        binding.cancelButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back press manually
        }

    }

    private fun onActivityEditUpdateCatalog() {
        val details= catalogUpdateItem

        binding.materialToolbar.title = "Update Catalog"
        binding.catalogdetailTextInputLayout.editText?.setText(details.catalogname)
        binding.catalogdetailTextInputLayout.hint = "Catalog"
        binding.ItemNo.editText?.setText(details.itemid)
        Log.d("details.itemid ", details.itemid.toString())
        Log.d("details.catalogname ", details.catalogname.toString())
        binding.ItemNo.hint = "Item No."
        binding.catalogdetail.isEnabled = false

        binding.producttitle.setText(details.producttitle.toString())
        binding.productdescription.setText(details.productdescription.toString())
        binding.mrp.setText(details.mrp.toString())
        binding.priceperkg.setText(details.priceperkg.toString())

        binding.postButton.setText("Update")


        Glide.with(this)
            .load(details.productimage)
            .error(com.example.tvcatalog.R.drawable.post)
            .into(binding.selectProductImage)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading, please wait...")
        progressDialog.setCancelable(false)

        binding.postButton.setOnClickListener {
            progressDialog.show()

            val newcatalogdetails = Catalog()

            newcatalogdetails.catalogname = binding.catalogdetail.text.toString().trim()
            newcatalogdetails.itemid = binding.itemno.text.toString().trim()
            newcatalogdetails.priceperkg = binding.priceperkg.text.toString().trim()
            newcatalogdetails.producttitle = binding.producttitle.text.toString().trim()
            newcatalogdetails.productdescription = binding.productdescription.text.toString().trim()
            newcatalogdetails.mrp = binding.mrp.text.toString().trim()
            newcatalogdetails.catalogstatus="on"
            newcatalogdetails.productimage = details.productimage.toString()

            val documentfoldername = newcatalogdetails.catalogname + newcatalogdetails.itemid


            val catalogRef = Firebase.firestore.collection(USER_CATALOG).document(storename.toString())
                .collection(newcatalogdetails.catalogname.toString()).document(documentfoldername)

            // Check if the document exists
            catalogRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Document exists, update it
                        catalogRef.set(newcatalogdetails, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Catalog Updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Document does not exist, do nothing or show a message
                        Toast.makeText(this, "Catalog does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking document", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    progressDialog.dismiss()
                }

        }
    }

    private fun onActivitySelected(isBanner: Boolean) {
        if (isBanner) {
            fetchDetails(USER_BANNER, "Banner Details", bannernamelist, bannertotalsizelist, -1)
            binding.materialToolbar.title = "Add Banner"
            binding.catalogdetailTextInputLayout.editText?.setText("Select Banner")
            binding.catalogdetailTextInputLayout.hint = "Banner"
            binding.ProductTitle.visibility = View.GONE
            binding.ProductDescription.visibility = View.GONE
            binding.Mrp.visibility = View.GONE
            binding.PricePerKg.visibility = View.GONE
        } else {
            binding.catalogdetail.isFocusable = true
            binding.itemno.isEnabled = true
            binding.itemno.isFocusable = true
            fetchDetails(USER_CATALOG, "Catalog Details", catalognamelist, catalogtotalsizelist, -1)
        }

        val nameList = if (isBanner) bannernamelist else catalognamelist
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, nameList)
        binding.catalogdetail.setAdapter(adapter)
        adapter.notifyDataSetChanged()

        binding.selectProductImage.setOnClickListener {
            selectImage()
            Log.d("uri", uri.toString())
        }

        binding.postButton.setOnClickListener {
            if (isBanner) addBannerValuesToDatabase() else addCatalogValuesToDatabase()
        }

        binding.catalogdetail.setOnItemClickListener { _, _, position, _ ->
            if (isBanner) {
                updateAutoCompleteTextView(position, bannernamelist, bannertotalsizelist)

            }else {
                updateAutoCompleteTextView(position, catalognamelist, catalogtotalsizelist)
            }

        }
    }

    private fun fetchDetails(collection: String, fieldName: String, nameList: MutableList<String>, totalSizeList: MutableList<String>, position: Int)
    {
        storename?.let { store ->
            val documentRef = Firebase.firestore.collection(collection).document(store)

            documentRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val details =
                        documentSnapshot.get(fieldName) as? List<Map<String, Any>> ?: listOf()

                    nameList.clear()
                    totalSizeList.clear()

                    for (item in details)
                    {
                        val name = item["Name"] as? String ?: "Unknown"
                        val totalItems = item["Total Items"] as? String ?: "0"

                        nameList.add(name)
                        totalSizeList.add(totalItems)
                    }
                    val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, nameList)
                    binding.catalogdetail.setAdapter(adapter)
                    adapter.notifyDataSetChanged()
                    if (nameList.isNotEmpty()) {
                        if(collection== USER_BANNER)
                        {
                            updateAutoCompleteTextView(position, bannernamelist, bannertotalsizelist)
                        }
                        else{
                            updateAutoCompleteTextView(position, catalognamelist, catalogtotalsizelist)
                        }
                    }
                    Log.d("Firestore", "$collection List: $nameList")
                } else {
                    Log.d("Firestore", "$collection Document does not exist")
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching $collection data: ${e.message}")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAutoCompleteTextView(
        position: Int,
        nameList: MutableList<String>,
        totalSizeList: MutableList<String>
    ) {
        if (nameList.isEmpty() || totalSizeList.isEmpty()) {
            Log.e("Error", "Lists are empty!")
            Toast.makeText(this, "No items available", Toast.LENGTH_SHORT).show()
            return
        }

        if (position < 0 || position >= totalSizeList.size) {
            if(position==-1)
            {
                binding.itemno.setText("Please select a name above")
                return
            }
            Log.e("Error", "Invalid selection index: $position")
            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = position
        val itemNumber = totalSizeList[selectedIndex].toIntOrNull() ?: 0

        Log.d("Size:", "Length of ${nameList[selectedIndex]}: $itemNumber")

        binding.itemno.setText((itemNumber + 1).toString())
        binding.catalogdetail.clearFocus()
    }

    private fun uploadImage(folderType: String, itemName: String?, onSuccess: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val user = User()

        Firebase.firestore.collection(USER_NODE).document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                user.storename = documentSnapshot.getString("storename").toString()
                uri?.let { imageUri ->
                    val folderName = "$folderType/${user.storename}_$userId/$itemName/"
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("$folderName/${UUID.randomUUID()}")

                    storageRef.putFile(imageUri)
                        .addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                onSuccess(downloadUrl.toString())  // Return the URL after upload completes
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Upload failed: ${e.message}")
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error: ${exception.message}")
            }
    }

    private fun addCatalogValuesToDatabase() {
        if (uri != null && !binding.catalogdetail.text.isNullOrEmpty() && binding.catalogdetail.text.toString() != "Select Catalog" && !binding.itemno.text.isNullOrEmpty() && !binding.producttitle.text.isNullOrEmpty() && !binding.productdescription.text.isNullOrEmpty() && !binding.priceperkg.text.isNullOrEmpty() && !binding.mrp.text.isNullOrEmpty() && catalognamelist.isNotEmpty() && catalogtotalsizelist.isNotEmpty()) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Uploading, please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val index = catalognamelist.indexOf(binding.catalogdetail.text.toString())
            val itemNumber = catalogtotalsizelist[index].toIntOrNull() ?: 0
            Log.d("Size: ", "length of Quality walls: ${itemNumber} ")
            catalog.catalogname = binding.catalogdetail.text.toString().trim()
            catalog.itemid = (itemNumber + 1).toString().trim()
            catalog.priceperkg = binding.priceperkg.text.toString().trim()
            catalog.producttitle = binding.producttitle.text.toString().trim()
            catalog.productdescription = binding.productdescription.text.toString().trim()
            catalog.mrp = binding.mrp.text.toString().trim()
            catalog.catalogstatus="on"

            uploadImage("catalog", catalog.catalogname)
            { url ->
                if (url != null) {
                    catalog.productimage = url
                    Log.d("catalog.productimage", catalog.productimage.toString())
                    val documentfoldername = catalog.catalogname + catalog.itemid
                    Firebase.firestore.collection(USER_CATALOG).document(storename.toString())
                        .collection(catalog.catalogname.toString()).document(documentfoldername)
                        .set(catalog)
                        .addOnSuccessListener {
                            storename?.let { store ->
                                val documentRef =
                                    Firebase.firestore.collection(USER_CATALOG).document(store)

                                documentRef.get().addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        // ✅ Fetch existing catalog details
                                        val existingCatalogs =
                                            documentSnapshot.get("Catalog Details") as? List<Map<String, Any>> ?: listOf()

                                        // ✅ Check if catalog name already exists
                                        val existingCatalogIndex =
                                            existingCatalogs.indexOfFirst { it["Name"] == catalog.catalogname }

                                        if (existingCatalogIndex == -1) {
                                            // ✅ Add only if name does NOT exist
                                            val newEntry = mapOf(
                                                "Name" to catalog.catalogname,
                                                "Total Items" to "1"  // Store as a string
                                            )
                                            documentRef.update("Catalog Details", FieldValue.arrayUnion(newEntry))
                                                .addOnSuccessListener {
                                                progressDialog.dismiss()
                                                fetchDetails(USER_CATALOG, "Catalog Details", catalognamelist, catalogtotalsizelist, -1)
                                                cleanDetails()
                                                Toast.makeText(this, "Uploaded Sucessfully", Toast.LENGTH_SHORT).show()
                                            }.addOnFailureListener { e ->
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "Failed to Append: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // ✅ Name exists, update "Total Items"
                                            val updatedCatalogs = existingCatalogs.toMutableList()
                                            val existingCatalog = existingCatalogs[existingCatalogIndex]
                                            val updatedCatalog = existingCatalog.toMutableMap()

                                            // ✅ Convert "Total Items" to Long, Increment, and Store as String
                                            val totalItems =
                                                (existingCatalog["Total Items"] as? String)?.toIntOrNull()
                                                    ?.plus(1) ?: 1
                                            updatedCatalog["Total Items"] =
                                                totalItems.toString() // Store as String

                                            // ✅ Replace the old entry with the updated one
                                            updatedCatalogs[existingCatalogIndex] = updatedCatalog

                                            // ✅ Update Firestore with the new list
                                            documentRef.update("Catalog Details", updatedCatalogs)
                                                .addOnSuccessListener {
                                                    progressDialog.dismiss()
                                                    fetchDetails(USER_CATALOG, "Catalog Details", catalognamelist, catalogtotalsizelist, -1)
                                                    cleanDetails()
                                                    Toast.makeText(this, "Uploaded Sucessfully", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    progressDialog.dismiss()
                                                    Toast.makeText(this, "Failed to Update: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        // ❌ Document doesn't exist → Create with an array of objects
                                        val data = mapOf("Catalog Details" to listOf(mapOf("Name" to catalog.catalogname, "Total Items" to "1")))
                                        documentRef.set(data)
                                            .addOnSuccessListener {
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "New Catalog Created", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "Creation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Not Uploaded", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            Toast.makeText(this, "Fill the data", Toast.LENGTH_SHORT).show()
        }

    }


    private fun addBannerValuesToDatabase() {
        if (uri != null && !binding.catalogdetail.text.isNullOrEmpty() && binding.catalogdetail.text.toString() != "Select Banner" && !binding.itemno.text.isNullOrEmpty() && bannernamelist.isNotEmpty() && bannertotalsizelist.isNotEmpty()) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Uploading, please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val index = bannernamelist.indexOf(binding.catalogdetail.text.toString())
            val itemNumber = bannertotalsizelist[index].toIntOrNull() ?: 0
            Log.d("Size: ", "length of Quality walls: ${itemNumber} ")
            banner.bannername = binding.catalogdetail.text.toString().trim()
            banner.itemid = (itemNumber + 1).toString().trim()

            uploadImage("banner", banner.bannername)
            { url ->
                if (url != null) {
                    banner.productimage = url
                    Log.d("banner.productimage", banner.productimage.toString())
                    val documentfoldername = banner.bannername + banner.itemid
                    Firebase.firestore.collection(USER_BANNER).document(storename.toString())
                        .collection(banner.bannername.toString()).document(documentfoldername)
                        .set(banner)
                        .addOnSuccessListener {
                            storename?.let { store ->
                                val documentRef =
                                    Firebase.firestore.collection(USER_BANNER).document(store)
                                documentRef.get().addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        // ✅ Fetch existing catalog details
                                        val existingBanners =
                                            documentSnapshot.get("Banner Details") as? List<Map<String, Any>>
                                                ?: listOf()

                                        // ✅ Check if catalog name already exists
                                        val existingBannerIndex =
                                            existingBanners.indexOfFirst { it["Name"] == banner.bannername }

                                        if (existingBannerIndex == -1) {
                                            // ✅ Add only if name does NOT exist
                                            val newEntry = mapOf(
                                                "Name" to banner.bannername,
                                                "Total Items" to "1"  // Store as a string
                                            )
                                            documentRef.update(
                                                "Banner Details",
                                                FieldValue.arrayUnion(newEntry) // Add as object
                                            ).addOnSuccessListener {
                                                progressDialog.dismiss()
                                                fetchDetails(USER_BANNER,"Banner Details",bannernamelist,bannertotalsizelist, -1)
                                                cleanDetails()
                                                Toast.makeText(this, "Uploaded Sucessfully", Toast.LENGTH_SHORT).show()
                                            }.addOnFailureListener { e ->
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "Failed to Append: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // ✅ Name exists, update "Total Items"
                                            val updatedBanners = existingBanners.toMutableList()
                                            val existingBanner = existingBanners[existingBannerIndex]
                                            val updatedBanner = existingBanner.toMutableMap()

                                            val totalItems =(existingBanner["Total Items"] as? String)?.toIntOrNull()?.plus(1) ?: 1
                                            updatedBanner["Total Items"] = totalItems.toString() // Store as String

                                            updatedBanners[existingBannerIndex] = updatedBanner

                                            documentRef.update("Banner Details", updatedBanners)
                                                .addOnSuccessListener {
                                                    progressDialog.dismiss()
                                                    fetchDetails(USER_BANNER, "Banner Details",bannernamelist, bannertotalsizelist, -1)
                                                    cleanDetails()
                                                    Toast.makeText(this, "Uploaded Sucessfully", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    progressDialog.dismiss()
                                                    Toast.makeText(this, "Failed to Update: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        // ❌ Document doesn't exist → Create with an array of objects
                                        val data = mapOf(
                                            "Banner Details" to listOf(
                                                mapOf(
                                                    "Name" to catalog.catalogname,
                                                    "Total Items" to "1"
                                                ) // Store as a string
                                            )
                                        )
                                        documentRef.set(data)
                                            .addOnSuccessListener {
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "New Banner Created", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                progressDialog.dismiss()
                                                Toast.makeText(this, "Creation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Not Uploaded", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            Toast.makeText(this, "Fill the data", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun addCatalogValuesToDatabase() {
//
//        if (uri != null &&
//            !binding.catalogdetail.text.isNullOrEmpty() &&
//            binding.catalogdetail.text.toString() != "Select Catalog" &&
//            !binding.itemno.text.isNullOrEmpty() &&
//            !binding.producttitle.text.isNullOrEmpty() &&
//            !binding.productdescription.text.isNullOrEmpty() &&
//            !binding.priceperkg.text.isNullOrEmpty() &&
//            !binding.mrp.text.isNullOrEmpty() &&
//            catalognamelist.isNotEmpty() &&
//            catalogtotalsizelist.isNotEmpty()
//        ) {
//            val progressDialog = ProgressDialog(this)
//            progressDialog.setMessage("Uploading, please wait...")
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//
//            val index = catalognamelist.indexOf(binding.catalogdetail.text.toString())
//            val itemNumber = catalogtotalsizelist[index].toIntOrNull() ?: 0
//            Log.d("Size: ", "length of Quality walls: ${itemNumber} ")
//            catalog.catalogname = binding.catalogdetail.text.toString().trim()
//            catalog.itemid = (itemNumber + 1).toString().trim()
//            catalog.priceperkg = binding.priceperkg.text.toString().trim()
//            catalog.producttitle = binding.producttitle.text.toString().trim()
//            catalog.productdescription = binding.productdescription.text.toString().trim()
//            catalog.mrp = binding.mrp.text.toString().trim()
//
//            uploadImage("catalog", catalog.catalogname)
//            { url ->
//                if (url != null) {
//                    catalog.productimage = url
//                    Log.d("catalog.productimage", catalog.productimage.toString())
//                    val documentfoldername = catalog.catalogname + catalog.itemid
//                    Firebase.firestore.collection(USER_CATALOG).document(storename.toString())
//                        .collection(catalog.catalogname.toString()).document(documentfoldername)
//                        .set(catalog)
//                        .addOnSuccessListener {
//                            storename?.let { store ->
//                                val documentRef =
//                                    Firebase.firestore.collection(USER_CATALOG).document(store)
//
//                                documentRef.get().addOnSuccessListener { documentSnapshot ->
//                                    if (documentSnapshot.exists()) {
//                                        // ✅ Fetch existing catalog details
//                                        val existingCatalogs =
//                                            documentSnapshot.get("Catalog Details") as? List<Map<String, Any>>
//                                                ?: listOf()
//
//                                        // ✅ Check if catalog name already exists
//                                        val existingCatalogIndex =
//                                            existingCatalogs.indexOfFirst { it["Name"] == catalog.catalogname }
//
//                                        if (existingCatalogIndex == -1) {
//                                            // ✅ Add only if name does NOT exist
//                                            val newEntry = mapOf(
//                                                "Name" to catalog.catalogname,
//                                                "Total Items" to "1"  // Store as a string
//                                            )
//                                            documentRef.update(
//                                                "Catalog Details",
//                                                FieldValue.arrayUnion(newEntry) // Add as object
//                                            ).addOnSuccessListener {
//                                                progressDialog.dismiss()
//                                                fetchDetails(
//                                                    USER_CATALOG,
//                                                    "Catalog Details",
//                                                    catalognamelist,
//                                                    catalogtotalsizelist,
//                                                    ::updateAutoCompleteTextView(
//                                                        position,
//                                                        catalognamelist,
//                                                        catalogtotalsizelist
//                                                    )
//                                                )
//                                                cleanDetails()
//                                                Toast.makeText(
//                                                    this,
//                                                    "Uploaded Sucessfully",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }.addOnFailureListener { e ->
//                                                progressDialog.dismiss()
//                                                Toast.makeText(
//                                                    this,
//                                                    "Failed to Append: ${e.message}",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }
//                                        } else {
//                                            // ✅ Name exists, update "Total Items"
//                                            val updatedCatalogs = existingCatalogs.toMutableList()
//                                            val existingCatalog =
//                                                existingCatalogs[existingCatalogIndex]
//                                            val updatedCatalog = existingCatalog.toMutableMap()
//
//                                            // ✅ Convert "Total Items" to Long, Increment, and Store as String
//                                            val totalItems =
//                                                (existingCatalog["Total Items"] as? String)?.toIntOrNull()
//                                                    ?.plus(1) ?: 1
//                                            updatedCatalog["Total Items"] =
//                                                totalItems.toString() // Store as String
//
//                                            // ✅ Replace the old entry with the updated one
//                                            updatedCatalogs[existingCatalogIndex] = updatedCatalog
//
//                                            // ✅ Update Firestore with the new list
//                                            documentRef.update("Catalog Details", updatedCatalogs)
//                                                .addOnSuccessListener {
//                                                    progressDialog.dismiss()
//                                                    fetchDetails(
//                                                        USER_CATALOG,
//                                                        "Catalog Details",
//                                                        catalognamelist,
//                                                        catalogtotalsizelist,
//                                                        ::updateAutoCompleteTextView(
//                                                            position,
//                                                            catalognamelist,
//                                                            catalogtotalsizelist
//                                                        )
//                                                    )
//                                                    cleanDetails()
//                                                    Toast.makeText(
//                                                        this,
//                                                        "Uploaded Sucessfully",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                }
//                                                .addOnFailureListener { e ->
//                                                    progressDialog.dismiss()
//                                                    Toast.makeText(
//                                                        this,
//                                                        "Failed to Update: ${e.message}",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                }
//                                        }
//                                    } else {
//                                        // ❌ Document doesn't exist → Create with an array of objects
//                                        val data = mapOf(
//                                            "Catalog Details" to listOf(
//                                                mapOf(
//                                                    "Name" to catalog.catalogname,
//                                                    "Total Items" to "1"
//                                                ) // Store as a string
//                                            )
//                                        )
//                                        documentRef.set(data)
//                                            .addOnSuccessListener {
//                                                progressDialog.dismiss()
//                                                Toast.makeText(
//                                                    this,
//                                                    "New Catalog Created",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }
//                                            .addOnFailureListener { e ->
//                                                progressDialog.dismiss()
//                                                Toast.makeText(
//                                                    this,
//                                                    "Creation Failed: ${e.message}",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }
//                                    }
//                                }
//                            }
//                        }
//                        .addOnFailureListener {
//                            progressDialog.dismiss()
//                            Toast.makeText(this, "Not Uploaded", Toast.LENGTH_SHORT).show()
//                        }
//                } else {
//                    progressDialog.dismiss()
//                    Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        } else {
//            Toast.makeText(this, "Fill the data", Toast.LENGTH_SHORT).show()
//        }
//
//    }



//    private fun onactivityBannerSelected() {
//        fetchBannerDetails()
//
//        binding.materialToolbar.title = "Add Banner"
//        binding.catalogdetailTextInputLayout.editText?.setText("Select Banner")
//        binding.catalogdetailTextInputLayout.hint="Banner"
//        binding.ProductTitle.visibility = View.GONE
//        binding.ProductDescription.visibility = View.GONE
//        binding.Mrp.visibility = View.GONE
//        binding.PricePerKg.visibility = View.GONE
//
//
//        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, bannernamelist)
//        binding.catalogdetail.setAdapter(adapter)
//
//        binding.selectProductImage.setOnClickListener {
//            selectImage()
//            Log.d("uri",uri.toString())
//        }
//        binding.postButton.setOnClickListener {
//            addBannerValuesToDatabase()
//        }
//        binding.catalogdetail.setOnItemClickListener { _, _, position, _ ->
//            updateBannerAutoCompleteTextView()
//        }
//    }

//    private fun fetchBannerDetails() {
//        storename?.let { store ->
//            val documentRef = Firebase.firestore.collection(USER_BANNER).document(store)
//
//            documentRef.get().addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    // ✅ Fetch existing catalog details
//                    val banners = documentSnapshot.get("Banner Details") as? List<Map<String, Any>> ?: listOf()
//
//                    bannernamelist.clear() // Clear the previous data
//                    bannertotalsizelist.clear()
//                    for (banner in banners) {
//                        val name = banner["Name"] as? String ?: "Unknown"
//                        val totalItems = (banner["Total Items"] as? String)?: "0"
//
//                        bannernamelist.add(name) // Store in MutableList
//                        bannertotalsizelist.add(totalItems) // Store in MutableList
//                    }
//                    if(bannernamelist.size!=0)
//                    {
//                        updateBannerAutoCompleteTextView(0)
//                    }
//                    Log.d("Firestore", "Banner List: $bannernamelist") // Debugging log
//                } else {
//                    Log.d("Firestore", "Document does not exist")
//                }
//            }.addOnFailureListener { e ->
//                Log.e("Firestore", "Error fetching data: ${e.message}")
//            }
//        }
//    }

//    private fun updateBannerAutoCompleteTextView(position: Int) {
//        if (bannernamelist.isEmpty() || bannertotalsizelist.isEmpty()) {
//            Log.e("Error", "Catalog lists are empty!")
//            Toast.makeText(this, "No banners available", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (position < 0 || position >= bannertotalsizelist.size) {
//            Log.e("Error", "Invalid selection index: $position")
//            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val selectedIndex = position
//        val itemNumber = bannertotalsizelist[selectedIndex].toIntOrNull() ?: 0
//
//        Log.d("Size:", "Length of ${bannernamelist[selectedIndex]}: $itemNumber")
//
//        binding.itemno.setText((itemNumber+1).toString())
//        binding.catalogdetail.clearFocus()
//    }



//    private fun uploadBannerImage(bannerName: String?, onSuccess: (String) -> Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//        val user= User()
//        var imageUrl: String? = null
//        Firebase.firestore.collection(USER_NODE).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
//            .addOnSuccessListener {
//                user.storename=it.getString("storename").toString()
//                uri?.let { imageUri ->
//                    val foldername="catalog/${user.storename}_$userId/$bannerName/"
//                    val storageRef = FirebaseStorage.getInstance().reference
//                        .child("$foldername/${UUID.randomUUID().toString()}")
//
//                    storageRef.putFile(imageUri)
//                        .addOnSuccessListener { taskSnapshot ->
//                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
//                                imageUrl=downloadUrl.toString()
//                                onSuccess(imageUrl!!)  // Return the URL after upload completes
//                            }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("Firebase", "Upload failed: ${e.message}")
//                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
//                        }
//                } ?: run {
//                    Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("TAG", "Error: ${exception.message}")
//            }
//
//    }

//    private fun onactivityCatalogSelected() {
//        fetchDetails(USER_CATALOG, "Catalog Details", catalognamelist, catalogtotalsizelist, ::updateCatalogAutoCompleteTextView)
//
//        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, catalognamelist)
//        binding.catalogdetail.setAdapter(adapter)
//
//        binding.selectProductImage.setOnClickListener {
//            selectImage()
//            Log.d("uri",uri.toString())
//        }
//        binding.postButton.setOnClickListener {
//            addCatalogValuesToDatabase()
//        }
//        binding.catalogdetail.setOnItemClickListener { _, _, position, _ ->
//            updateCatalogAutoCompleteTextView(position)
//        }
//
//    }
//    private fun fetchCatalogDetails() {
//        storename?.let { store ->
//            val documentRef = Firebase.firestore.collection(USER_CATALOG).document(store)
//
//            documentRef.get().addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    // ✅ Fetch existing catalog details
//                    val catalogs = documentSnapshot.get("Catalog Details") as? List<Map<String, Any>> ?: listOf()
//
//                    catalognamelist.clear() // Clear the previous data
//                    catalogtotalsizelist.clear()
//                    for (catalog in catalogs) {
//                        val name = catalog["Name"] as? String ?: "Unknown"
//                        val totalItems = (catalog["Total Items"] as? String)?: "0"
//
//                        catalognamelist.add(name) // Store in MutableList
//                        catalogtotalsizelist.add(totalItems) // Store in MutableList
//                    }
//                    if(catalognamelist.size!=0)
//                    {
//                        updateCatalogAutoCompleteTextView(0)
//                    }
//                    Log.d("Firestore", "Catalog List: $catalognamelist") // Debugging log
//                } else {
//                    Log.d("Firestore", "Document does not exist")
//                }
//            }.addOnFailureListener { e ->
//                Log.e("Firestore", "Error fetching data: ${e.message}")
//            }
//        }
//    }

//    private fun updateCatalogAutoCompleteTextView(position: Int) {
//        if (catalognamelist.isEmpty() || catalogtotalsizelist.isEmpty()) {
//            Log.e("Error", "Catalog lists are empty!")
//            Toast.makeText(this, "No banners available", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (position < 0 || position >= catalogtotalsizelist.size) {
//            Log.e("Error", "Invalid selection index: $position")
//            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val selectedIndex = position
//        val itemNumber = catalogtotalsizelist[selectedIndex].toIntOrNull() ?: 0
//
//        Log.d("Size:", "Length of ${catalognamelist[selectedIndex]}: $itemNumber")
//
//        binding.itemno.setText((itemNumber+1).toString())
//        binding.catalogdetail.clearFocus()
//    }



    //    private fun uploadCatalogImage(catalogname: String?, onSuccess: (String) -> Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//        val user= User()
//        var imageUrl: String? = null
//        Firebase.firestore.collection(USER_NODE).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
//            .addOnSuccessListener {
//                user.storename=it.getString("storename").toString()
//                uri?.let { imageUri ->
//                    val foldername="catalog/${user.storename}_$userId/$catalogname/"
//                    val storageRef = FirebaseStorage.getInstance().reference
//                        .child("$foldername/${UUID.randomUUID().toString()}")
//
//                    storageRef.putFile(imageUri)
//                        .addOnSuccessListener { taskSnapshot ->
//                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
//                                imageUrl=downloadUrl.toString()
//                                onSuccess(imageUrl!!)  // Return the URL after upload completes
//                            }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("Firebase", "Upload failed: ${e.message}")
//                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
//                        }
//                } ?: run {
//                    Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("TAG", "Error: ${exception.message}")
//            }
//
//    }
    private fun onActivityNameListSelected(title: String, collection: String, fieldName: String) {
        binding.materialToolbar.title = "Add $title Name"
        binding.selectProductImage.visibility = View.GONE
        binding.catalogdetailTextInputLayout.visibility = View.GONE
        binding.itemno.visibility = View.GONE
        binding.productdescription.visibility = View.GONE
        binding.mrp.visibility = View.GONE
        binding.priceperkg.visibility = View.GONE

        binding.ProductTitle.hint = "$title Name"
        binding.cancelButton.text = "Cancel"
        binding.postButton.text = "Add"

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading, please wait...")
        progressDialog.setCancelable(false)

        binding.postButton.setOnClickListener {
            val name = binding.producttitle.text.toString().trim()

            if (name.isNotEmpty()) {
                progressDialog.show()

                storename?.let { store ->
                    val documentRef = Firebase.firestore.collection(collection).document(store)

                    documentRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val existingList =
                                documentSnapshot.get(fieldName) as? List<Map<String, Any>>
                                    ?: listOf()
                            val nameExists = existingList.any { it["Name"] == name }

                            if (!nameExists) {
                                val newEntry = mapOf("Name" to name, "Total Items" to "0")
                                documentRef.update(fieldName, FieldValue.arrayUnion(newEntry))
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()
                                        cleanDetails()
                                        Toast.makeText(
                                            this,
                                            "$title Added Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        progressDialog.dismiss()
                                        Toast.makeText(
                                            this,
                                            "Failed to Append: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "$title Name Already Exists!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val newData = mapOf(
                                fieldName to listOf(
                                    mapOf(
                                        "Name" to name,
                                        "Total Items" to "0"
                                    )
                                )
                            )

                            documentRef.set(newData)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "New $title Created", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this,
                                        "Creation Failed: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }.addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.d("Error occurred", "Error: ${e.message}")
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "$title Name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImage() {
        galleryImage.launch("image/*")
        return
    }

    private fun cleanDetails() {
        Glide.with(this)
            .load(com.example.tvcatalog.R.drawable.post)
            .error(com.example.tvcatalog.R.drawable.post)
            .into(binding.selectProductImage)


        binding.catalogdetail.text.clear()
        binding.itemno.text?.clear()
        binding.priceperkg.text?.clear()
        binding.producttitle.text?.clear()
        binding.productdescription.text?.clear()
        binding.mrp.text?.clear()
    }
}
//    private fun onactivityCatalogNameListSelected() {
//        binding.materialToolbar.title = "Add Catalog Name"
//        binding.selectProductImage.visibility = View.GONE
//        binding.catalogdetailTextInputLayout.visibility = View.GONE
//        binding.itemno.visibility = View.GONE
//        binding.productdescription.visibility = View.GONE
//        binding.mrp.visibility = View.GONE
//        binding.priceperkg.visibility = View.GONE
//
//        binding.ProductTitle.hint = "Catalog Name"
//        binding.cancelButton.text = "Cancel"
//        binding.postButton.text = "Add"
//
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Uploading, please wait...")
//        progressDialog.setCancelable(false)
//
//        binding.postButton.setOnClickListener {
//            val catalogName = binding.producttitle.text.toString().trim() // Get the text value
//
//            if (catalogName.isNotEmpty()) {
//                progressDialog.show()
//
//                storename?.let { store ->
//                    val documentRef = Firebase.firestore.collection(USER_CATALOG)
//                        .document(store)
//
//                    documentRef.get().addOnSuccessListener { documentSnapshot ->
//                        if (documentSnapshot.exists()) {
//                            // ✅ Fetch existing catalog details
//                            val existingCatalogs = documentSnapshot.get("Catalog Details") as? List<Map<String, Any>> ?: listOf()
//
//                            // ✅ Check if catalog name already exists
//                            val catalogExists = existingCatalogs.any { it["Name"] == catalogName }
//
//                            if (!catalogExists) {
//                                // ✅ Add only if name does NOT exist
//                                val newEntry = mapOf(
//                                    "Name" to catalogName,
//                                    "Total Items" to "0"  // Initially, size is 0
//                                )
//                                documentRef.update(
//                                    "Catalog Details", FieldValue.arrayUnion(newEntry) // Add as object
//                                ).addOnSuccessListener {
//                                    progressDialog.dismiss()
//                                    cleanDetails()
//                                    Toast.makeText(this, "Catalog Added Successfully", Toast.LENGTH_SHORT).show()
//                                }.addOnFailureListener { e ->
//                                    progressDialog.dismiss()
//                                    Toast.makeText(this, "Failed to Append: ${e.message}", Toast.LENGTH_SHORT).show()
//                                }
//                            } else {
//                                // ❌ Name already exists
//                                progressDialog.dismiss()
//                                Toast.makeText(this, "Catalog Name Already Exists!", Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            // ❌ Document doesn't exist → Create with an array of objects
//                            val data = mapOf(
//                                "Catalog Details" to listOf(
//                                    mapOf("Name" to catalogName, "Total Items" to "0") // First entry
//                                )
//                            )
//                            documentRef.set(data)
//                                .addOnSuccessListener {
//                                    progressDialog.dismiss()
//                                    Toast.makeText(this, "New Catalog Created", Toast.LENGTH_SHORT).show()
//                                }
//                                .addOnFailureListener { e ->
//                                    progressDialog.dismiss()
//                                    Toast.makeText(this, "Creation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                                }
//                        }
//                    }.addOnFailureListener {e->
//                        progressDialog.dismiss()
//                        Log.d("Error occured ","Error: ${e.message}")
//                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Catalog Name cannot be empty!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//    private fun onActivityBannerNameListSelected() {
//        binding.materialToolbar.title = "Add Banner Name"
//        binding.selectProductImage.visibility = View.GONE
//        binding.catalogdetailTextInputLayout.visibility = View.GONE
//        binding.itemno.visibility = View.GONE
//        binding.productdescription.visibility = View.GONE
//        binding.mrp.visibility = View.GONE
//        binding.priceperkg.visibility = View.GONE
//
//        binding.ProductTitle.hint = "Banner Name"
//        binding.cancelButton.text = "Cancel"
//        binding.postButton.text = "Add"
//
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Uploading, please wait...")
//        progressDialog.setCancelable(false)
//
//        binding.postButton.setOnClickListener {
//            val bannerName = binding.producttitle.text.toString().trim()
//
//            if (bannerName.isNotEmpty()) {
//                progressDialog.show()
//
//                storename?.let { store ->
//                    val documentRef = Firebase.firestore.collection(USER_BANNER).document(store)
//
//                    documentRef.get().addOnSuccessListener { documentSnapshot ->
//                        if (documentSnapshot.exists()) {
//                            // ✅ Fetch existing banners
//                            val existingBanners = documentSnapshot.get("Banner Details") as? List<Map<String, Any>> ?: listOf()
//
//                            // ✅ Check if the banner already exists
//                            val bannerExists = existingBanners.any { it["Name"] == bannerName }
//
//                            if (!bannerExists) {
//                                val newEntry = mapOf(
//                                    "Name" to bannerName,
//                                    "Total Items" to "0"
//                                )
//                                documentRef.update("Banner Details", FieldValue.arrayUnion(newEntry))
//                                    .addOnSuccessListener {
//                                        progressDialog.dismiss()
//                                        cleanDetails()
//                                        Toast.makeText(this, "Banner Added Successfully", Toast.LENGTH_SHORT).show()
//                                    }
//                                    .addOnFailureListener { e ->
//                                        progressDialog.dismiss()
//                                        Toast.makeText(this, "Failed to Append: ${e.message}", Toast.LENGTH_SHORT).show()
//                                    }
//                            } else {
//                                progressDialog.dismiss()
//                                Toast.makeText(this, "Banner Name Already Exists!", Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            // ❌ Document doesn't exist → Create it along with the collection
//                            val newBannerData = mapOf(
//                                "Banner Details" to listOf(
//                                    mapOf("Name" to bannerName, "Total Items" to "0")
//                                )
//                            )
//
//                            documentRef.set(newBannerData)
//                                .addOnSuccessListener {
//                                    progressDialog.dismiss()
//                                    Toast.makeText(this, "New Banner Created", Toast.LENGTH_SHORT).show()
//                                }
//                                .addOnFailureListener { e ->
//                                    progressDialog.dismiss()
//                                    Toast.makeText(this, "Creation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                                }
//                        }
//                    }.addOnFailureListener { e ->
//                        progressDialog.dismiss()
//                        Log.d("Error occured ","Error: ${e.message}")
//                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Banner Name cannot be empty!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
