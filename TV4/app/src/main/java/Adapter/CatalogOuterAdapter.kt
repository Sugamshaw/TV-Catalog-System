package Adapter

import Models.CardDetails
import Models.Catalog
import Models.OuterItem
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tv4.R
import com.example.tv4.databinding.CatalogProductOuterBinding
import java.util.logging.Handler



class CatalogOuterAdapter(private val cardDetailList: MutableMap<String, MutableList<Catalog>>) :
    RecyclerView.Adapter<CatalogOuterAdapter.CatalogOuterViewHolder>() {

    private val catalogNames = cardDetailList.keys.toList()
    private val handler = android.os.Handler(Looper.getMainLooper()) // Avoid memory leaks

    inner class CatalogOuterViewHolder(val binding: CatalogProductOuterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogOuterViewHolder {
        val binding = CatalogProductOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogOuterViewHolder(binding)
    }

    override fun getItemCount(): Int = catalogNames.size

    override fun onBindViewHolder(holder: CatalogOuterViewHolder, position: Int) {
        val catalogName = catalogNames[position]
        holder.binding.catalogTitle.text = catalogName

        val cardDetails = cardDetailList[catalogName] ?: mutableListOf()
        val innerAdapter = CatalogInnerAdapter(cardDetails)

        holder.binding.innerRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(6, StaggeredGridLayoutManager.VERTICAL)
            adapter = innerAdapter
        }

//        startAutoScrollInner(holder.binding.innerRecyclerView, innerAdapter, position, holder.itemView)
//        moveToNextOuterItem(position,  holder.itemView)
    }

    private fun startAutoScrollInner(innerRecyclerView: RecyclerView, adapter: CatalogInnerAdapter, outerPosition: Int, outerView: View) {
        var scrollPosition = 0
        val runnable = object : Runnable {
            override fun run() {
                Log.d("AutoScroll", "Running inner scroll at position: $scrollPosition")

                if (scrollPosition < adapter.itemCount - 1) {
                    scrollPosition++
                } else {
                    scrollPosition = 0 // Reset inner scroll
                    moveToNextOuterItem(outerPosition, outerView) // Move to next outer item
                    return // Stop further inner scrolling
                }

                innerRecyclerView.scrollToPosition(scrollPosition)
                handler.postDelayed(this, 1000) // Adjust scroll speed
            }
        }

        handler.postDelayed(runnable, 5000) // Start after 5 second
    }

    private fun moveToNextOuterItem(currentPosition: Int, outerView: View) {
        val outerRecyclerView = outerView.parent as? RecyclerView ?: return
        val adapter = outerRecyclerView.adapter as? CatalogOuterAdapter ?: return
        val nextPosition = if (currentPosition < adapter.itemCount - 1) currentPosition + 1 else 0 // Loop back to the first position
        Log.d("AutoScroll", "Running outer scroll at position:Running outer scroll at position $nextPosition")
        outerRecyclerView.postDelayed({
            outerRecyclerView.scrollToPosition(nextPosition)
            // Restart inner scroll after moving to the new outer item
            restartInnerScrollAtNextPosition(outerRecyclerView, nextPosition)
        }, 2000) // Delay before switching categories
    }

    private fun restartInnerScrollAtNextPosition(outerRecyclerView: RecyclerView, nextPosition: Int) {
        outerRecyclerView.postDelayed({
            val outerViewHolder = outerRecyclerView.findViewHolderForAdapterPosition(nextPosition) as? CatalogOuterViewHolder
            val innerRecyclerView = outerViewHolder?.binding?.innerRecyclerView
            val innerAdapter = innerRecyclerView?.adapter as? CatalogInnerAdapter

            if (innerRecyclerView != null && innerAdapter != null) {
                startAutoScrollInner(innerRecyclerView, innerAdapter, nextPosition, outerViewHolder.itemView)
            }
        }, 1000) // Small delay to ensure ViewHolder is attached
    }


    fun stopScrolling() {
        handler.removeCallbacksAndMessages(null)
    }
}



//start: 2
//class CatalogOuterAdapter(private val cardDetailList: MutableMap<String, MutableList<Catalog>>) :
//    RecyclerView.Adapter<CatalogOuterAdapter.CatalogOuterViewHolder>() {
//
//    private val catalogNames = cardDetailList.keys.toList() // Extract keys as list
//
//    inner class CatalogOuterViewHolder(val binding: CatalogProductOuterBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogOuterViewHolder {
//        val binding = CatalogProductOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CatalogOuterViewHolder(binding)
//    }
//
//    override fun getItemCount(): Int = catalogNames.size
//
//    override fun onBindViewHolder(holder: CatalogOuterViewHolder, position: Int) {
//        val catalogName = catalogNames[position] // Get catalog title
//        holder.binding.catalogTitle.text = catalogName
//
//        val cardDetails = cardDetailList[catalogName] ?: mutableListOf() // Get list safely
//
//        val innerAdapter = CatalogInnerAdapter(cardDetails)
//        holder.binding.innerRecyclerView.apply {
//            layoutManager = StaggeredGridLayoutManager(6, StaggeredGridLayoutManager.HORIZONTAL)
//
//            adapter = innerAdapter
//
//        }
//        startAutoScrollInner(holder.binding.innerRecyclerView, innerAdapter, position, holder.itemView)
//    }
//}
//
//private fun startAutoScrollInner(innerRecyclerView: RecyclerView, adapter: CatalogInnerAdapter, outerPosition: Int, outerView: View) {
//    val handler = android.os.Handler()
//    var scrollPosition = 0
//
//    val runnable = object : Runnable {
//        override fun run() {
//            Log.d("AutoScroll", "Running ${scrollPosition}")
//            Log.d("outerPosition ", "outerPosition: ${outerPosition}")
//            val outerRecyclerView = outerView.parent as? RecyclerView ?: return
//            val adapterouter = outerRecyclerView.adapter as? CatalogOuterAdapter ?: return
//            if (outerPosition >= adapterouter.itemCount-1)
//            {
//                outerRecyclerView.smoothScrollToPosition(0)
//                return
//            }
//            if (scrollPosition < adapter.itemCount - 1) {
//                scrollPosition++
//            } else {
//                scrollPosition = 0 // Reset inner scroll
//                moveToNextOuterItem(outerPosition, outerView) // Move to next outer item
//                return // Stop further inner scrolling
//            }
//            innerRecyclerView.smoothScrollToPosition(scrollPosition)
//            handler.postDelayed(this, 2000) // Adjust delay for scroll speed
//        }
//    }
//
//    handler.postDelayed(runnable, 5000)
//}
//
//private fun moveToNextOuterItem(currentPosition: Int, outerView: View) {
//    val outerRecyclerView = outerView.parent as? RecyclerView ?: return
//    val adapter = outerRecyclerView.adapter as? CatalogOuterAdapter ?: return
//    val nextPosition = currentPosition + 1
//
//    outerRecyclerView.postDelayed({
//        if (nextPosition < adapter.itemCount) { // Correct way to get item count
//            outerRecyclerView.smoothScrollToPosition(nextPosition)
//        } else {
//            outerRecyclerView.smoothScrollToPosition(0) // Reset to first item
//        }
//    }, 1000) // Delay before moving to next outer item
//}
//





//start: 1
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

