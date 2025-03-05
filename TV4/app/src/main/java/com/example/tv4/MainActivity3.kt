package com.example.tv4

import Adapter.CatalogOuterAdapter
import Models.CATALOGNAMELIST
import Models.CATALOG_DESIGN
import Models.Catalog
import Models.USER_CATALOG
import Models.USER_NODE
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tv4.databinding.ActivityMain3Binding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

/**
 * Loads [MainFragment].
 */

class MainActivity3 : FragmentActivity() {

    private lateinit var binding: ActivityMain3Binding
    private var storeName: String? = null
    private lateinit var catalog: Catalog
    private var catalogDesignDetails = mutableMapOf<String, MutableList<String>>()
    private var cardDetailList = mutableMapOf<String, MutableList<Catalog>>()

    private lateinit var verticalGridView: VerticalGridView
    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0
    private var currentRow = 0
    private var currentColumn = 0
    private var numColumns:Int=0
    private val bgImageUrl = arrayOf(
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        numColumns=5
        getStoreName()
        startAutoScroll(numColumns)
    }
    private fun getStoreName() {
        val currentdoc = "81NTh7CMNGcFNh0I0BIoRXcD4yf1"
//        val currentdoc = FirebaseAuth.getInstance().currentUser!!.uid
        Firebase.firestore.collection(USER_NODE)
            .document(currentdoc).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    storeName = document.getString("storename").toString()
                    retrieveDataFromCatalogDesign()
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.message.toString())
            }
    }

    private fun retrieveDataFromCatalogDesign() {
        showProgressBar()
        val documentRef = Firebase.firestore.collection(CATALOG_DESIGN)
            .document(storeName!!)  // Ensure storeName is not null

        storeName?.let { store ->
            val documentRef = Firebase.firestore.collection(CATALOG_DESIGN).document(store)

            documentRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for catalog updates", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        for (catalog in CATALOGNAMELIST) {
                            if (data.containsKey(catalog)) {
                                val catalogItems = data[catalog] as? List<String> // Safe cast

                                if (!catalogItems.isNullOrEmpty()) {
                                    // ✅ Initialize the list in the map if it's not already present
                                    if (!catalogDesignDetails.containsKey(catalog)) {
                                        catalogDesignDetails[catalog] = mutableListOf()
                                    }

                                    // ✅ Append each item with catalog name
                                    catalogItems.forEach { item ->
                                        Log.d("Catalog Data", "Item: $item")
                                        catalogDesignDetails[catalog]?.add(catalog + item)
                                    }

                                    // ✅ Log the full catalog details
                                    Log.d(
                                        "Catalog Data",
                                        "All Items for $catalog: ${catalogDesignDetails[catalog]}"
                                    )
                                } else {
                                    Log.d("Catalog Data", "No items found for '$catalog'.")
                                }
                            } else {
                                Log.d("Catalog Data", "Key '$catalog' not found.")
                            }
                        }

                        // ✅ Final log of all retrieved data
                        Log.d("Catalog Data", "Complete Data: $catalogDesignDetails")
                        makeCatalogsOuterandInnerAdapter()
                    } else {
                        hideProgressBar()
                        Log.d("Catalog Data", "Document data is null.")
                    }
                } else {
                    hideProgressBar()
                    Log.d("Catalog Data", "Document does not exist.")
                }
            }
        }?: Log.e("Error", "storeName is null")
    }
    private fun makeCatalogsOuterandInnerAdapter() {
        // ✅ Ensure `catalogDesignDetails` is initialized before calling this function
        if (catalogDesignDetails.isEmpty()) {
            Log.e("AdapterError", "catalogDesignDetails is empty or not initialized!")
            hideProgressBar()
            return
        }

        for (catalogName in catalogDesignDetails.keys) {
            catalogDesignDetails[catalogName]?.let { subCatalogNames ->
                val itemList = mutableListOf<Catalog>()

                for (subCatalog in subCatalogNames) {
                    Firebase.firestore.collection(USER_CATALOG)
                        .document(storeName!!)
                        .collection(catalogName)
                        .document(subCatalog)
                        .addSnapshotListener { document, error ->
                            if (error != null) {
                                Log.e("Firestore", "Error listening for catalog updates", error)
                                hideProgressBar()
                                return@addSnapshotListener
                            }

                            if (document != null && document.exists()) {
                                document.toObject<Catalog>()?.let { catalogItem ->
                                    // ✅ Prevent duplicate entries
                                    itemList.removeIf { it.itemid == catalogItem.itemid }
                                    itemList.add(catalogItem)

                                    Log.d("Data retrieved", "Catalog name: ${catalogItem.catalogname}\nItem id: ${catalogItem.itemid}\nMrp: ${catalogItem.mrp}\nProduct title: ${catalogItem.producttitle}\nProduct description: ${catalogItem.productdescription}")
                                }
                            }

                            // ✅ Update `cardDetailList` and refresh UI dynamically
                            cardDetailList[catalogName] = itemList
                            Log.d("Adapter", "Updated Catalog: $catalogName, Items: $itemList")

                            setadapterOuter()
                        }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setadapterOuter(){
        hideProgressBar()
        binding.verticalGrid.addItemDecoration(GridSpacingDecoration(5, 0))

        // Set the number of columns and rows
        binding.verticalGrid.setNumColumns(numColumns)


        val outerAdapter = CatalogOuterAdapter(cardDetailList)
        binding.verticalGrid.adapter = outerAdapter
        val gridLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.verticalGrid.layoutManager = gridLayoutManager

        // Request focus for the grid view
        binding.verticalGrid.requestFocus()
        binding.verticalGrid.isFocusableInTouchMode = true

        outerAdapter.notifyDataSetChanged()

        startAutoScroll(5)
    }
    private fun showProgressBar() {
        binding.progressLayout.visibility = View.VISIBLE
        binding.verticalGrid.visibility = View.GONE // Hide RecyclerView
    }
    private fun hideProgressBar() {
        binding.progressLayout.visibility = View.GONE
        binding.verticalGrid.visibility = View.VISIBLE // Show RecyclerView when data is ready
    }


    private fun startAutoScroll(numColumns: Int) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (binding.verticalGrid.adapter != null) {
                    // Calculate the total number of rows
                    val totalRows = (binding.verticalGrid.adapter!!.itemCount + numColumns - 1) / numColumns

                    // Scroll to the next row
                    currentRow++

                    // Loop back to the first row if the last row is reached
                    if (currentRow >= totalRows) {
                        currentRow = 0
                    }

                    // Calculate the position of the first item in the current row
                    val position = currentRow * numColumns

                    // Scroll to the calculated position
                    binding.verticalGrid.smoothScrollToPosition(position)

                    // Schedule the next scroll
                    handler.postDelayed(this, 5000) // Scroll every 2 seconds
                }
            }
        }, 2000) // Initial delay of 2 seconds
    }


    override fun onDestroy() {
        super.onDestroy()
        // Stop the auto-scrolling when the activity is destroyed
        handler.removeCallbacksAndMessages(null)
    }
}
