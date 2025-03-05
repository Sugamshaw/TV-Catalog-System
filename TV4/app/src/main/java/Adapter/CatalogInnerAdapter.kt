package Adapter

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
import com.example.tv4.databinding.CardViewBinding


class CatalogInnerAdapter(private val carddetaillist: MutableList<Catalog>):RecyclerView.Adapter<CatalogInnerAdapter.CatalogInnerViewHolder>() {
    inner class CatalogInnerViewHolder(val binding: CardViewBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogInnerViewHolder {
        val binding=CardViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CatalogInnerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return carddetaillist.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CatalogInnerViewHolder, position: Int) {
        holder.binding.pricePerKg.text=carddetaillist[position].priceperkg
        holder.binding.productTitle.text=carddetaillist[position].producttitle
        holder.binding.productDescription.text=carddetaillist[position].productdescription
        holder.binding.mrpPrice.text=carddetaillist[position].mrp.toString()
//        holder.binding.itemNo.text=carddetaillist[position].itemid.toString()
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

        holder.binding.cardView.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.itemView.context, colorCardBackground)
        )

        val prizeColor = when (position%4) {
            3 -> R.color.prize_colour1
            2 -> R.color.prize_colour2
            1 -> R.color.prize_colour3
            0 -> R.color.prize_colour4
            else -> R.color.prize_colour1
        }

        val drawablePrize = ContextCompat.getDrawable(holder.itemView.context, R.drawable.price_bg)
        if (drawablePrize is GradientDrawable) {
            drawablePrize.setColor(ContextCompat.getColor(holder.itemView.context, prizeColor))
        }
        holder.binding.pricePerKg.background = drawablePrize

        // Set the name and description background dynamically
        val nameDescriptionColor = when (position%4) {
            3 -> R.color.card_name_description1
            2 -> R.color.card_name_description2
            1 -> R.color.card_name_description3
            0 -> R.color.card_name_description4
            else -> R.color.card_name_description1
        }

        val drawableCardNameDescription = ContextCompat.getDrawable(holder.itemView.context, R.drawable.card_name_description_bg)
        if (drawableCardNameDescription is GradientDrawable) {
            drawableCardNameDescription.setColor(ContextCompat.getColor(holder.itemView.context, nameDescriptionColor))
        }
        holder.binding.linearbackground.background = drawableCardNameDescription
    }
}

//class CatalogOuterAdapter(private val catalogname: List<OuterItem>) : RecyclerView.Adapter<CatalogOuterAdapter.CatalogOuterViewHolder>() {
//
//    inner class CatalogOuterViewHolder(val binding: CatalogProductOuterBinding): RecyclerView.ViewHolder(binding.root)
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogOuterViewHolder {
//        val binding=
//            CatalogProductOuterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//        return CatalogOuterViewHolder(binding)
//    }
//    override fun getItemCount(): Int = catalogname.size
//
//    override fun onBindViewHolder(holder: CatalogOuterViewHolder, position: Int) {
//        val catalogname1 = catalogname[position].title
//        holder.binding.catalogTitle.text=catalogname1
//
////        val inner
//    }
//}