package Adapter

import Models.Catalog
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvcatalog.CatalogDetailActivity
import com.example.tvcatalog.R
import com.example.tvcatalog.databinding.CardViewBinding

class CatalogInnerAdapter(private val carddetaillist: MutableList<Catalog>):RecyclerView.Adapter<CatalogInnerAdapter.CatalogInnerViewHolder>() {
    inner class CatalogInnerViewHolder(val binding:CardViewBinding):RecyclerView.ViewHolder(binding.root)

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
        holder.binding.mrpPrice.text=carddetaillist[position].mrp
        holder.binding.itemNo.text=carddetaillist[position].itemid
        Glide.with(holder.itemView.context)
            .load(carddetaillist[position].productimage.toString())
            .error(R.drawable.post)
            .into(holder.binding.productImage)

        var colorCardBackground = when (position%4) {
            3 -> R.color.card_background1
            2 -> R.color.card_background2
            1 -> R.color.card_background3
            0 -> R.color.card_background4
            else -> R.color.card_background1
        }

        // Set the name and description background dynamically
        var nameDescriptionColor = when (position%4) {
            3 -> R.color.card_name_description1
            2 -> R.color.card_name_description2
            1 -> R.color.card_name_description3
            0 -> R.color.card_name_description4
            else -> R.color.card_name_description1
        }

        var prizeColor = when (position%4) {
            3 -> R.color.prize_colour1
            2 -> R.color.prize_colour2
            1 -> R.color.prize_colour3
            0 -> R.color.prize_colour4
            else -> R.color.prize_colour1
        }

        if(carddetaillist[position].catalogstatus == "off")
        {
            colorCardBackground = R.color.card_background_grey
            prizeColor = R.color.prize_colour_grey
            nameDescriptionColor = R.color.card_name_description_grey
        }
        holder.binding.cardView.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.itemView.context, colorCardBackground)
        )

        val drawablePrize = ContextCompat.getDrawable(holder.itemView.context, R.drawable.price_bg)
        if (drawablePrize is GradientDrawable) {
            drawablePrize.setColor(ContextCompat.getColor(holder.itemView.context, prizeColor))
        }
        holder.binding.pricePerKg.background = drawablePrize

        val drawableCardNameDescription = ContextCompat.getDrawable(holder.itemView.context, R.drawable.card_name_description_bg)
        if (drawableCardNameDescription is GradientDrawable) {
            drawableCardNameDescription.setColor(ContextCompat.getColor(holder.itemView.context, nameDescriptionColor))
        }
        holder.binding.linearbackground.background = drawableCardNameDescription


        val drawableitemtext= ContextCompat.getDrawable(holder.itemView.context, R.drawable.circle_background)
        if (drawableitemtext is GradientDrawable) {
            drawableitemtext.setColor(ContextCompat.getColor(holder.itemView.context, nameDescriptionColor))
        }
        holder.binding.itemNo.background = drawableitemtext

        holder.binding.cardView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CatalogDetailActivity::class.java)
            intent.putExtra("catalog_item", carddetaillist[position])  // ✅ Fix: Pass the correct item
            holder.itemView.context.startActivity(intent)
        }

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