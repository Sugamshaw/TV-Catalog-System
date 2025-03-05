package Adapter

import Models.Banner
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tvcatalog.databinding.CatalogProductOuterBinding



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

//class CatalogOuterAdapter(private val carddetaillist: MutableList<String, MutableList<Catalog>>) :RecyclerView.Adapter<CatalogOuterAdapter.CatalogOuterViewHolder>() {
//
//    private val catalogNames = carddetaillist.keys.toList() // Extract keys as list
//
//    inner class CatalogOuterViewHolder(val binding: CatalogProductOuterBinding):RecyclerView.ViewHolder(binding.root)
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogOuterViewHolder {
//        val binding=CatalogProductOuterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//        return CatalogOuterViewHolder(binding)
//    }
//    override fun getItemCount(): Int = carddetaillist.size
//
//    override fun onBindViewHolder(holder: CatalogOuterViewHolder, position: Int) {
//        val catalogname1 = carddetaillist[position].catalogtitle
//        holder.binding.catalogTitle.text=catalogname1
//
//        val innerAdapter=CatalogInnerAdapter(carddetaillist[position].carddetails)
//        holder.binding.innerRecyclerView.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
//        holder.binding.innerRecyclerView.adapter=innerAdapter
//    }
//}
//

