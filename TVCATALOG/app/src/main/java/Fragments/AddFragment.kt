package Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.tvcatalog.AddCatalogBannerActivity
import com.example.tvcatalog.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)


        binding.catalog.setOnClickListener {
            saveSelectionAndNavigate("catalog")
        }

        binding.banner.setOnClickListener {
            saveSelectionAndNavigate("banner")
        }

        binding.catalognamelist.setOnClickListener {
            saveSelectionAndNavigate("catalog_name_list")
        }

        binding.bannernamelist.setOnClickListener {
            saveSelectionAndNavigate("banner_name_list")
        }
        return binding.root
    }

    private fun saveSelectionAndNavigate(selection: String) {
        val sharedPreferences = requireContext().getSharedPreferences(
            getString(com.example.tvcatalog.R.string.data_file),
            AppCompatActivity.MODE_PRIVATE
        )
        sharedPreferences.edit().putString("activityselected", selection).apply()
        startActivity(Intent(requireContext(), AddCatalogBannerActivity::class.java))
        dismiss() // Close the bottom sheet after selection
    }
}