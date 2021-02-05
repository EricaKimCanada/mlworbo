package com.example.mlworbo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerAdapter(private val context: Context, private val wordDataset: ArrayList<Word>) :
    RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var icons = itemView.findViewById<ImageView>(R.id.icon_image_view)
        var titles = itemView.findViewById<TextView>(R.id.title_text_view)
        var cardViews = itemView.findViewById<CardView>(R.id.word_card_view)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyRecyclerAdapter.MyViewHolder {
        // create a new item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_view_layout_items, parent, false) as View

        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val wordItem: Word = wordDataset[position]

        holder.icons.setImageURI(Uri.parse(wordItem.sourcePic))
        holder.titles.text = wordItem.sourceWord

        holder.cardViews.setOnClickListener {
            val intent = Intent(context, WordActivity::class.java)

            // passing data to the word activity
            intent.putExtra("sourcePic", wordItem.sourcePic)
            intent.putExtra("sourceWord", wordItem.sourceWord)
            intent.putExtra("sourceLang", wordItem.sourceLang)
            intent.putExtra("translatedWord", wordItem.translatedWord)

            context.startActivity(intent)
        }  
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = wordDataset.size
}