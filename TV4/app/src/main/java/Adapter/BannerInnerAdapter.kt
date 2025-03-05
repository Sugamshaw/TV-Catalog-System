package Adapter

import Models.Banner
import Models.Catalog
import Models.OuterItem
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tv4.R
import com.example.tv4.databinding.BannerViewBinding


class BannerInnerAdapter(private val carddetaillist: MutableList<Banner>):RecyclerView.Adapter<BannerInnerAdapter.CatalogInnerViewHolder>() {
    inner class CatalogInnerViewHolder(val binding:BannerViewBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogInnerViewHolder {
        val binding=BannerViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CatalogInnerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return carddetaillist.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CatalogInnerViewHolder, position: Int) {

        holder.binding.itemNo.text=carddetaillist[position].itemid
        Glide.with(holder.itemView.context)
            .load(carddetaillist[position].productimage.toString())
            .error(R.drawable.post)
            .into(holder.binding.productImage)


        val colorCardBackground = when (position%4) {
            3 -> R.color.card_background1
            2 -> R.color.card_background2
            1 -> R.color.card_background3
            0 -> R.color.card_background4
            else -> R.color.card_background1
        }

        holder.binding.bannerView.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.itemView.context, colorCardBackground)
        )

        val textnumberColor = when (position%4) {
//            3 -> R.color.prize_colour1
//            2 -> R.color.prize_colour2
//            1 -> R.color.prize_colour3
//            0 -> R.color.prize_colour4
//            else -> R.color.prize_colour1
            3 -> R.color.card_name_description1
            2 -> R.color.card_name_description2
            1 -> R.color.card_name_description3
            0 -> R.color.card_name_description4
            else -> R.color.card_name_description1
        }

        val drawableCardNameDescription = ContextCompat.getDrawable(holder.itemView.context, R.drawable.circle_background)
        if (drawableCardNameDescription is GradientDrawable) {
            drawableCardNameDescription.setColor(ContextCompat.getColor(holder.itemView.context, textnumberColor))
        }
        holder.binding.itemNo.background = drawableCardNameDescription

    }
}

