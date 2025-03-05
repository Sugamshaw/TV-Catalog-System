package Fragments

import Adapter.CatalogOuterAdapter
import Adapter.ViewPagerAdapter
import Models.Catalog
import Models.USER_CATALOG
import Models.USER_NODE
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tvcatalog.R
import com.example.tvcatalog.databinding.FragmentCatalogDisplayBinding
import com.example.tvcatalog.databinding.FragmentHome2Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class CatalogDisplayFragment : Fragment() {

    private lateinit var binding: FragmentCatalogDisplayBinding

    private var storeName: String? = null
    private lateinit var catalog: Catalog
    private var catalogDesignDetails = mutableMapOf<String, MutableList<String>>()
    private var cardDetailList = mutableMapOf<String, MutableList<Catalog>>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCatalogDisplayBinding.inflate(inflater, container, false)
        catalog = Catalog()
        setadapterOuter()
        Log.d("Store Name", storeName.toString())
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            Firebase.firestore.collection(USER_NODE)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        storeName = document.getString("storename").toString()
                        fetchCatalogDetails()

                    }
                }
                .addOnFailureListener {
                    Log.d("Error", it.message.toString())
                }
    }

    private fun fetchCatalogDetails() {
        storeName?.let { store ->
            val documentRef = Firebase.firestore.collection(USER_CATALOG).document(store)

            documentRef.addSnapshotListener() { documentSnapshot,error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for catalog updates", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // ✅ Fetch existing catalog details
                    val catalogs = documentSnapshot.get("Catalog Details") as? List<Map<String, Any>> ?: listOf()

                    for (catalog in catalogs) {
                        val name = catalog["Name"] as? String ?: "Unknown"
                        val totalItems = (catalog["Total Items"] as? String)?: "0"
                        if (!name.isNullOrEmpty() && !totalItems.isNullOrEmpty() && totalItems.toIntOrNull()!=0) {
                            // ✅ Initialize the list in the map if it's not already present
                            if (!catalogDesignDetails.containsKey(name)) {
                                catalogDesignDetails[name] = mutableListOf()
                            }

                            for(itemno in 1..totalItems.toInt()){
                                catalogDesignDetails[name]?.add(name + itemno.toString())
                            }
                            // ✅ Log the full catalog details
                            Log.d(
                                "Catalog Data",
                                "All Items for $catalog: ${catalogDesignDetails[name]}"
                            )
                        } else {
                            Log.d("Catalog Data", "No items found for '$catalog'.")
                        }

                    }
                    Log.d("Catalog Data", "Complete Data: $catalogDesignDetails")
                    makeCatalogsOuterandInnerAdapter()

                } else {
                    Log.d("Firestore", "Document does not exist")
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
//                val itemList:MutableList<Catalog>?=null
                val itemList= mutableListOf<Catalog>()

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
//                                    if(catalogItem.catalogstatus=="on")
//                                    {
//                                        itemList?.add(catalogItem)
//                                    }
                                    itemList?.add(catalogItem)
                                    Log.d("Data retrieved", "Catalog name: ${catalogItem.catalogname}\nItem id: ${catalogItem.itemid}\nMrp: ${catalogItem.mrp}\nProduct title: ${catalogItem.producttitle}\nProduct description: ${catalogItem.productdescription}")
                                }
                            }

                            // ✅ Update `cardDetailList` and refresh UI dynamically
                            if (itemList.isNotEmpty()) {
                                cardDetailList[catalogName] = itemList
                                Log.d("Adapter", "Updated Catalog: $catalogName, Items: $itemList")
                            }

                            Log.d("Adapter", "Updated Catalog: $catalogName, Items: $itemList")
                            setadapterOuter()
                        }
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setadapterOuter(){
        showProgressBar()
        val outerAdapter = CatalogOuterAdapter(cardDetailList)
        binding.catalogOuterAdapter.layoutManager=
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.catalogOuterAdapter.adapter = outerAdapter
        outerAdapter.notifyDataSetChanged()
        hideProgressBar()
    }
    private fun showProgressBar() {
        binding.progressLayout.visibility = View.VISIBLE
        binding.catalogOuterAdapter.visibility = View.GONE // Hide RecyclerView
    }
    private fun hideProgressBar() {
        binding.progressLayout.visibility = View.GONE
        binding.catalogOuterAdapter.visibility = View.VISIBLE // Show RecyclerView when data is ready
    }

}