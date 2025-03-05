package Adapter

import Models.Banner
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tv4.databinding.CatalogProductOuterBinding




class BannerOuterAdapter(private val cardDetailList: MutableMap<String, MutableList<Banner>>) :
    RecyclerView.Adapter<BannerOuterAdapter.CatalogOuterViewHolder>() {

    private val bannerNames = cardDetailList.keys.toList() // Extract keys as list

    inner class CatalogOuterViewHolder(val binding: CatalogProductOuterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogOuterViewHolder {
        val binding = CatalogProductOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogOuterViewHolder(binding)
    }

    override fun getItemCount(): Int = bannerNames.size

    override fun onBindViewHolder(holder: CatalogOuterViewHolder, position: Int) {
        val bannerName = bannerNames[position] // Get catalog title
        holder.binding.catalogTitle.text = bannerName

        val cardDetails = cardDetailList[bannerName] ?: mutableListOf() // Get list safely

        val innerAdapter = BannerInnerAdapter(cardDetails)
        holder.binding.innerRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = innerAdapter
        }
    }
}


