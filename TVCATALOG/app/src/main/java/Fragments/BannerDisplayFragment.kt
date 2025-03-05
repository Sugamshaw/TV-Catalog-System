package Fragments

import Adapter.BannerOuterAdapter
import Adapter.CatalogOuterAdapter
import Models.Banner
import Models.Catalog
import Models.USER_BANNER
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
import com.example.tvcatalog.databinding.FragmentBannerDisplayBinding
import com.example.tvcatalog.databinding.FragmentCatalogDisplayBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class BannerDisplayFragment : Fragment() {
    private lateinit var binding: FragmentBannerDisplayBinding

    private var storeName: String? = null
    private lateinit var banner: Banner
    private var bannerDesignDetails = mutableMapOf<String, MutableList<String>>()
    private var cardDetailList = mutableMapOf<String, MutableList<Banner>>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerDisplayBinding.inflate(inflater, container, false)
        banner = Banner()
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
                    fetchBannerDetails()
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.message.toString())
            }
    }

    private fun fetchBannerDetails() {
        storeName?.let { store ->
            val documentRef = Firebase.firestore.collection(USER_BANNER).document(store)

            documentRef.addSnapshotListener() { documentSnapshot,error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for catalog updates", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // ✅ Fetch existing catalog details
                    val banners = documentSnapshot.get("Banner Details") as? List<Map<String, Any>> ?: listOf()

                    for (banner in banners) {
                        val name = banner["Name"] as? String ?: "Unknown"
                        val totalItems = (banner["Total Items"] as? String)?: "0"
                        if (!name.isNullOrEmpty() && !totalItems.isNullOrEmpty() && totalItems.toIntOrNull()!=0) {
                            // ✅ Initialize the list in the map if it's not already present
                            if (!bannerDesignDetails.containsKey(name)) {
                                bannerDesignDetails[name] = mutableListOf()
                            }

                            for(itemno in 1..totalItems.toInt()){
                                bannerDesignDetails[name]?.add(name + itemno.toString())
                            }
                            // ✅ Log the full catalog details
                            Log.d(
                                "Catalog Data",
                                "All Items for $banner: ${bannerDesignDetails[name]}"
                            )
                        } else {
                            Log.d("Catalog Data", "No items found for '$banner'.")
                        }

                    }
                    Log.d("Catalog Data", "Complete Data: $bannerDesignDetails")
                    makeCatalogsOuterandInnerAdapter()

                } else {
                    Log.d("Firestore", "Document does not exist")
                }

            }
        }?: Log.e("Error", "storeName is null")
    }

    private fun makeCatalogsOuterandInnerAdapter() {
        // ✅ Ensure `catalogDesignDetails` is initialized before calling this function
        if (bannerDesignDetails.isEmpty()) {
            Log.e("AdapterError", "catalogDesignDetails is empty or not initialized!")
            hideProgressBar()
            return
        }

        for (bannerName in bannerDesignDetails.keys) {
            bannerDesignDetails[bannerName]?.let { subCatalogNames ->
                val itemList = mutableListOf<Banner>()

                for (subCatalog in subCatalogNames) {
                    Firebase.firestore.collection(USER_BANNER)
                        .document(storeName!!)
                        .collection(bannerName)
                        .document(subCatalog)
                        .addSnapshotListener { document, error ->
                            if (error != null) {
                                Log.e("Firestore", "Error listening for catalog updates", error)
                                hideProgressBar()
                                return@addSnapshotListener
                            }

                            if (document != null && document.exists()) {
                                document.toObject<Banner>()?.let { bannerItem ->
                                    // ✅ Prevent duplicate entries
                                    itemList.removeIf { it.itemid == bannerItem.itemid }
                                    itemList.add(bannerItem)

                                    Log.d("Data retrieved", "Catalog name: ${bannerItem.bannername}\nItem id: ${bannerItem.itemid}\n")
                                }
                            }

                            // ✅ Update `cardDetailList` and refresh UI dynamically
                            cardDetailList[bannerName] = itemList
                            Log.d("Adapter", "Updated Catalog: $bannerName, Items: $itemList")

                            setadapterOuter()
                        }
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setadapterOuter(){
        showProgressBar()
        val outerAdapter = BannerOuterAdapter(cardDetailList)
        binding.bannerOuterAdapter.layoutManager=
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.bannerOuterAdapter.adapter = outerAdapter
        outerAdapter.notifyDataSetChanged()
        hideProgressBar()
    }
    private fun showProgressBar() {
        binding.progressLayout.visibility = View.VISIBLE
        binding.bannerOuterAdapter.visibility = View.GONE // Hide RecyclerView
    }
    private fun hideProgressBar() {
        binding.progressLayout.visibility = View.GONE
        binding.bannerOuterAdapter.visibility = View.VISIBLE // Show RecyclerView when data is ready
    }

}