package com.example.tv4

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.VerticalGridView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    private lateinit var verticalGridView: VerticalGridView
    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0
    private var currentRow = 0
    private var currentColumn = 0
    private var numColumns:Int=0
    private val bgImageUrl = arrayOf(
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verticalGridView = findViewById(R.id.browse_grid)
        verticalGridView.addItemDecoration(GridSpacingDecoration(5, 0))

        // Set the number of columns and rows
        numColumns = 5
        verticalGridView.setNumColumns(numColumns)

        val items: Map<Int, List<CardItem>> = loadRows()
        // Initialize GridAdapter with flattened items
        val gridAdapter = GridAdapter(items)

        verticalGridView.adapter = gridAdapter
        // Set the GridLayoutManager for vertical and horizontal scrolling
        val gridLayoutManager = StaggeredGridLayoutManager( numColumns, StaggeredGridLayoutManager.VERTICAL)
        verticalGridView.layoutManager = gridLayoutManager

        // Request focus for the grid view
        verticalGridView.requestFocus()
        verticalGridView.isFocusableInTouchMode = true
        // Start auto-scrolling
        startAutoScroll(numColumns)
    }

    private fun loadRows(): Map<Int, List<CardItem>> {
        val dictionary: MutableMap<Int, List<CardItem>> = mutableMapOf()  // MutableMap to modify

        var count=0
        for (i in 1..10 * numColumns) { // Example loop for 10 rows
            val price = "99/kg"
            val title = "Item $i"
            val description = "Description for $title"
            val imageUrl = bgImageUrl[(i) % bgImageUrl.size]  // Cycle through the bg images
            val mrpPrice = "Mrp ₹450"

            // Add CardItem to the map
            dictionary[i] = listOf(CardItem(price, imageUrl, title, description, mrpPrice, count % numColumns))
            if(i%numColumns==0)
            {
                count+=1
            }
        }

        return dictionary  // Return the map
    }
    private fun startAutoScroll(numColumns: Int) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (verticalGridView.adapter != null) {
                    // Calculate the total number of rows
                    val totalRows = (verticalGridView.adapter!!.itemCount + numColumns - 1) / numColumns

                    // Scroll to the next row
                    currentRow++

                    // Loop back to the first row if the last row is reached
                    if (currentRow >= totalRows) {
                        currentRow = 0
                    }

                    // Calculate the position of the first item in the current row
                    val position = currentRow * numColumns

                    // Scroll to the calculated position
                    verticalGridView.smoothScrollToPosition(position)

                    // Schedule the next scroll
                    handler.postDelayed(this, 5000) // Scroll every 2 seconds
                }
            }
        }, 2000) // Initial delay of 2 seconds
    }


    override fun onDestroy() {
        super.onDestroy()
        // Stop the auto-scrolling when the activity is destroyed
        handler.removeCallbacksAndMessages(null)
    }
}

class GridAdapter(private val items: Map<Int, List<CardItem>>) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    // Flattening the Map to a single list for easy handling
    private val flattenedItems: List<CardItem> = items.values.flatten()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = flattenedItems[position] // Get the corresponding CardItem
        val priceView = holder.view.findViewById<TextView>(R.id.price_per_kg)
        val imageView = holder.view.findViewById<ImageView>(R.id.product_image)
        val titleView = holder.view.findViewById<TextView>(R.id.product_title)
        val descriptionView = holder.view.findViewById<TextView>(R.id.product_description)
        val mrpView = holder.view.findViewById<TextView>(R.id.mrp_price)
        val linearBackground = holder.view.findViewById<LinearLayout>(R.id.linearbackground)

        // Set CardItem data
        priceView.text = card.price
        Glide.with(holder.view.context)
            .load(card.productUrl)
            .error(R.drawable.movie)
            .into(imageView)

        titleView.text = card.productTitle
        descriptionView.text = card.productDescription
        mrpView.text = card.mrpPrice

        // Set background color dynamically based on 'choice'
        val cardView = holder.view.findViewById<CardView>(R.id.card_view)
        val colorCardBackground = when (card.choice) {
            3 -> R.color.card_background1
            2 -> R.color.card_background2
            1 -> R.color.card_background3
            0 -> R.color.card_background4
            else -> R.color.card_background1
        }

        cardView.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.view.context, colorCardBackground)
        )

        // Set prize color background dynamically
        val prizeColor = when (card.choice) {
            3 -> R.color.prize_colour1
            2 -> R.color.prize_colour2
            1 -> R.color.prize_colour3
            0 -> R.color.prize_colour4
            else -> R.color.prize_colour1
        }

        val drawablePrize = ContextCompat.getDrawable(holder.view.context, R.drawable.price_bg)
        if (drawablePrize is GradientDrawable) {
            drawablePrize.setColor(ContextCompat.getColor(holder.view.context, prizeColor))
        }
        priceView.background = drawablePrize

        // Set the name and description background dynamically
        val nameDescriptionColor = when (card.choice) {
            3 -> R.color.card_name_description1
            2 -> R.color.card_name_description2
            1 -> R.color.card_name_description3
            0 -> R.color.card_name_description4
            else -> R.color.card_name_description1
        }

        val drawableCardNameDescription = ContextCompat.getDrawable(holder.view.context, R.drawable.card_name_description_bg)
        if (drawableCardNameDescription is GradientDrawable) {
            drawableCardNameDescription.setColor(ContextCompat.getColor(holder.view.context, nameDescriptionColor))
        }
        linearBackground.background = drawableCardNameDescription
    }

    override fun getItemCount(): Int = flattenedItems.size  // Return the size of the flattened list

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // You can add more views here if needed for future expansion
    }
}

class GridSpacingDecoration(private val horizontalSpacing: Int, private val verticalSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = horizontalSpacing
        outRect.right = horizontalSpacing
        outRect.top = verticalSpacing
        outRect.bottom = verticalSpacing
    }
}

