package com.example.tv4

import Adapter.BannerInnerAdapter
import Adapter.BannerOuterAdapter
import Models.Banner
import Models.Catalog
import Models.FirebaseCustomerSettings
import Models.USER_BANNER
import Models.USER_CATALOG
import Models.USER_NODE
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.tv4.databinding.ActivityMain4Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs



class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding

    private var storeName: String? = null
    private lateinit var banner: Banner
    private var bannerDesignDetails = mutableMapOf<String, MutableList<String>>()
    private var cardDetailList = mutableMapOf<String, MutableList<Banner>>()
    private  val cardlistforinneradapter=mutableListOf<Banner>()

    private lateinit var outerAdapter: BannerInnerAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private var autoScrollSlidePermission:String = "false"
    private var animationName:String?="Default"
    private var animationSpeed: Double =1.0

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (autoScrollSlidePermission == "false") return
            if (::outerAdapter.isInitialized) {
                if (currentPage == outerAdapter.itemCount - 1) {
                    currentPage = 0
                    binding.viewPager2.setCurrentItem(currentPage, false) // No smooth scroll
                } else {
                    currentPage++
                    binding.viewPager2.setCurrentItem(currentPage, true) // Smooth scroll
                }
                handler.postDelayed(this, (3000*animationSpeed).toLong())
            }
        }
    }

//    private val autoScrollRunnable = object : Runnable {
//        override fun run() {
//            if (autoScrollSlidePermission == "false") return
//            if (::outerAdapter.isInitialized) {
//                if (currentPage == outerAdapter.itemCount - 1) {
//                    currentPage = 0
//                } else {
//                    currentPage++
//                }
//                binding.viewPager2.setCurrentItem(currentPage, true)
//                handler.postDelayed(this, 3000)
//            }
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val settings = FirebaseCustomerSettings()
        settings.layouttype { it ->
            val customerSettings = it
            val selectedLayout = customerSettings?.layoutoption ?: "Catalog"
            val tvconnection = customerSettings?.connecttv ?: "false"
            animationName=customerSettings?.banneranimationoption?:"Default"
            val customeranimationSpeed=customerSettings?.banneranimationspeed?:"1x"
            animationSpeed = when (customeranimationSpeed) {
                "3x" -> 0.1
                "2.5x" -> 0.2
                "2x" -> 0.3
                "1.75x" -> 0.4
                "1.5x" -> 0.6
                "1.25x" -> 0.8
                "1x" -> 1.0
                "0.75x" -> 2.0
                "0.5x" -> 3.0
                "0.25x" -> 5.0
                else -> 1.0 // Default speed if input doesn't match
            }


            println("selectedLayout : $selectedLayout")

            if (tvconnection == "false") {
                // Directly go to MainActivity2 and return, skipping Handler block
                startActivity(Intent(this, TvNotConnected::class.java))
                finish()
                return@layouttype
            }

            autoScrollSlidePermission = customerSettings?.autoscroll.toString()
            if (selectedLayout != null) {
                println("Layout Type: $selectedLayout")
            } else {
                if(selectedLayout == "Catalog")
                {
                    startActivity(Intent(this,MainActivity2::class.java))
                }
            }
        }


        banner = Banner()
        setadapterOuter()
        Log.d("Store Name", storeName.toString())
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
//                                hideProgressBar()
                                return@addSnapshotListener
                            }

                            if (document != null && document.exists()) {
                                document.toObject<Banner>()?.let { bannerItem ->
                                    // ✅ Prevent duplicate entries
                                    itemList.removeIf { it.itemid == bannerItem.itemid }
                                    itemList.add(bannerItem)
                                    cardlistforinneradapter.add(bannerItem)
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
    private fun setadapterOuter() {
        outerAdapter = BannerInnerAdapter(cardlistforinneradapter) // Ensure adapter is assigned
        binding.viewPager2.adapter = outerAdapter
        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.dotsIndicator.attachTo(binding.viewPager2)
        // Enable preloading to smooth scrolling
        binding.viewPager2.offscreenPageLimit = 3

         // Change this dynamically at runtime

        // Apply PageTransformer dynamically based on animationName
        binding.viewPager2.setPageTransformer { page, position ->
            when (animationName) {
                "Default" -> {
                    // Standard ViewPager2 transition
                    page.translationX = -position * page.width
                }
                "Depth" -> {
                    // Smooth fading depth effect
                    page.alpha = if (position <= 0) 1f else (1 - position)
                    page.translationX = page.width * -position
                }
                "Zoom-Out" -> {
                    // Shrinking away effect
                    val zoomFactor = max(0.85f, 1 - abs(position))
                    page.scaleX = zoomFactor
                    page.scaleY = zoomFactor
                    page.alpha = zoomFactor
                }
                "Fade" -> {
                    // Fades in/out smoothly
                    page.alpha = 1 - abs(position)
                }
                "Cube Rotation" -> {
                    // 3D Cube Effect
                    page.pivotX = if (position < 0) page.width.toFloat() else 0f
                    page.pivotY = page.height * 0.5f
                    page.rotationY = -90f * position
                }
                "Flip" -> {
                    // Book-style flip effect
                    page.rotationY = position * -180
                    page.alpha = if (abs(position) < 0.5) 1f else 0f
                }
                "Accordion" -> {
                    // Stretching pages effect
                    page.scaleX = max(0.85f, 1 - abs(position))
                }
                "Rotate Down" -> {
                    // Slanted rotation effect
                    page.pivotX = page.width * 0.5f
                    page.pivotY = page.height.toFloat()
                    page.rotation = -15f * position
                }
                "Parallax" -> {
                    // Background moves slower than foreground
                    val parallaxFactor = 0.5f
                    page.translationX = -position * page.width * parallaxFactor
                }
                "Custom" -> {
                    // Mixed effect: Zoom-In, Zoom-Out, Rotation
                    val scaleFactor = max(0.85f, 1 - abs(position))
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                    page.rotationY = position * -60 // Partial flip effect
                    page.alpha = scaleFactor
                }
                "Scale Down" -> {
                    // Scale down effect
                    val scaleFactor = max(0.8f, 1 - abs(position))
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                }
                "Rotate Up" -> {
                    // Opposite of Rotate Down
                    page.pivotX = page.width * 0.5f
                    page.pivotY = 0f
                    page.rotation = 15f * position
                }
                "Slide Over" -> {
                    // One page smoothly covers the other
                    page.translationX = position * -page.width
                }
                "Stack" -> {
                    // Pages stack over each other
                    page.translationY = abs(position) * -page.height
                }
                "Toss" -> {
                    // Tossing effect
                    page.rotation = position * -25
                    page.translationY = abs(position) * -page.height * 0.3f
                }
                "Carousel" -> {
                    // Smooth circular motion
                    val scaleFactor = max(0.85f, 1 - abs(position))
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                    page.translationX = position * -page.width * 0.5f
                    page.alpha = scaleFactor
                }
                else -> {
                    val scaleFactor = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
                    page.scaleX = min(1f, max(scaleFactor, 0f))
                    page.scaleY = min(1f, max(scaleFactor, 0f))
                }
            }
        }

        // Start auto-scroll only after adapter is set
        if (autoScrollSlidePermission == "true") {
            startAutoScroll()
        } else {
            stopAutoScroll()
        }
    }


//    private fun setadapterOuter() {
//        outerAdapter = BannerInnerAdapter(cardlistforinneradapter) // Ensure adapter is assigned
//        binding.viewPager2.adapter = outerAdapter
//        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
//
//        // Enable preloading to smooth scrolling
//        binding.viewPager2.offscreenPageLimit = 3
//
//        val animationName="Flip"
//        // Apply PageTransformer for Scaling Effect
//        binding.viewPager2.setPageTransformer { page, position ->
//            val scaleFactor = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
//            page.scaleX = min(1f, max(scaleFactor, 0f))
//            page.scaleY = min(1f, max(scaleFactor, 0f))
//        }
//
//        // Start auto-scroll only after adapter is set
//        if(autoScrollSlidePermission == "true")
//        {
//            startAutoScroll()
//        }
//        else{
//            stopAutoScroll()
//        }
//    }


    private fun startAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable)
        handler.postDelayed(autoScrollRunnable, (3000 * animationSpeed).toLong())
    }
    private fun stopAutoScroll() {
        Log.d("AutoScroll", "Stopping auto-scroll")
        handler.removeCallbacks(autoScrollRunnable) // Stop auto-scroll
    }


}