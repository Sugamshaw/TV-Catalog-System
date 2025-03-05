package Fragments

import Models.CUSTOMER_SETTINGS
import Models.Customer_settings
import Models.USER_NODE
import Models.User
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.tvcatalog.LoginActivity
import com.example.tvcatalog.R
import com.example.tvcatalog.SignActivity
import com.example.tvcatalog.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso



class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var savedName:String?=null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =FragmentProfileBinding.inflate(inflater, container, false)

//        binding.editProfile.setOnClickListener{
//
//            val intent= Intent(activity, SignActivity::class.java)
//            intent.putExtra("MODE",1)
//            activity?.startActivity(intent)
//            activity?.finish()
//        }
        binding.autoscrollswitch.setOnClickListener {
            updateUserSetting()
        }
        binding.tvconnectswitch.setOnClickListener {
            updateUserSetting()
        }


        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            Toast.makeText(activity, "Logging Out", Toast.LENGTH_SHORT).show()
            activity?.startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfileDetails()
        loadUserSettings()
    }

    private fun loadUserSettings() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            Firebase.firestore.collection(USER_NODE)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject<User>()
                        user?.let {
                            Firebase.firestore.collection(CUSTOMER_SETTINGS)
                                .document(it.storename.toString())
                                .addSnapshotListener { settingsDoc, error ->
                                    if (error != null) {
                                        Toast.makeText(requireContext(), "Error loading settings", Toast.LENGTH_SHORT).show()
                                        return@addSnapshotListener
                                    }

                                    if (settingsDoc != null && settingsDoc.exists()) {
                                        val settings = settingsDoc.toObject<Customer_settings>()
                                        settings?.let { config ->
                                            binding.autoscrollswitch.isChecked = config.autoscroll == "true"
                                            binding.tvconnectswitch.isChecked = config.connecttv == "true"
                                            binding.layoutdetail.setText(config.layoutoption)
                                            binding.banneranimationoption.setText(config.banneranimationoption)
                                            binding.banneranimationspeed.setText(config.banneranimationspeed)

                                            setupBannerAnimationDropdown(config.layoutoption,config.banneranimationoption,config.banneranimationspeed)
                                        }
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
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
                            customerSettings.autoscroll = if (binding.autoscrollswitch.isChecked) "true" else "false"
                            customerSettings.connecttv = if (binding.tvconnectswitch.isChecked) "true" else "false"
                            customerSettings.layoutoption=binding.layoutdetail.text.toString()
                            customerSettings.banneranimationoption=binding.banneranimationoption.text.toString()
                            customerSettings.banneranimationspeed=binding.banneranimationspeed.text.toString()

                            Firebase.firestore.collection(CUSTOMER_SETTINGS)
                                .document(it.storename.toString())
                                .set(customerSettings)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Settings updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to update settings", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun setupBannerAnimationDropdown(selectedlayout: String?,selectedbanneranimation: String?,selectedbanneranimationspeed: String?) {

        val layoutList=mutableListOf<String>("Catalog","Banner")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, layoutList)
        binding.layoutdetail.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        selectedlayout?.let {
            binding.layoutdetail.setText(it, false)  // `false` prevents dropdown expansion
        }
        binding.layoutdetail.setOnItemClickListener { parent, view, position, id ->
            binding.layoutdetail.clearFocus()
            updateUserSetting()
        }

        val banneranimationlist=mutableListOf<String>("Default","Depth","Zoom-Out","Fade","Cube Rotation","Flip","Accordion","Rotate Down","Parallax","Custom","Scale Down","Rotate Up","Slide Over","Stack","Toss","Carousel")
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, banneranimationlist)
        binding.banneranimationoption.setAdapter(adapter2)
        adapter2.notifyDataSetChanged()
        selectedbanneranimation?.let {
            binding.banneranimationoption.setText(it, false)  // `false` prevents dropdown expansion
        }
        binding.banneranimationoption.setOnItemClickListener { parent, view, position, id ->
            binding.banneranimationoption.clearFocus()
            updateUserSetting()
        }

        val banneranimationoptionlist=mutableListOf<String>("0.25x","0.5x","0.75x","1x","1.25x","1.5x","1.75x","2x","2.5x","3x")
        val adapter3 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, banneranimationoptionlist)
        binding.banneranimationspeed.setAdapter(adapter3)
        selectedbanneranimationspeed?.let {
            binding.banneranimationspeed.setText(it, false)  // `false` prevents dropdown expansion
        }
        binding.banneranimationspeed.setOnItemClickListener { parent, view, position, id ->
            binding.banneranimationspeed.clearFocus()
            updateUserSetting()
        }


    }

    private fun loadProfileDetails() {
        val sharedPreferences = requireContext().getSharedPreferences(
            getString(R.string.data_file), Context.MODE_PRIVATE
        )
        savedName = sharedPreferences.getString("userdocumentname", "").toString()
        Log.d("savedName", savedName.toString())

        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            Firebase.firestore.collection(USER_NODE)
                .document(userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            val user = document.toObject<User>()
                            user?.let {
                                binding.name.text = it.storename
                                binding.bio.text = it.email
                                if (!it.image.isNullOrEmpty()) {
                                    Glide.with(this)
                                        .load(it.image)
                                        .error(R.drawable.user)
                                        .into(binding.profileImage)
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "User data not found!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Error fetching user data", task.exception)
                    }
                }
        }
    }
}