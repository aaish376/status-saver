package com.example.statussaver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StatusAdapter(
    private var items: List<StatusItem>,
    private val onSaveClick: (StatusItem) -> Unit,
    private val onItemClick: (StatusItem) -> Unit
) : RecyclerView.Adapter<StatusAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val thumb: ImageView = view.findViewById(R.id.imgThumb)
        val videoIcon: ImageView = view.findViewById(R.id.imgVideoIcon)
        val saveBtn: ImageButton = view.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        Glide.with(holder.thumb.context)
            .load(item.uri)
            .centerCrop()
            .placeholder(R.drawable.bg_thumb_placeholder)
            .into(holder.thumb)

        holder.videoIcon.visibility = if (item.isVideo) View.VISIBLE else View.GONE
        holder.saveBtn.setOnClickListener { onSaveClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<StatusItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
