package com.travelfoodie.feature.attraction

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfoodie.core.data.local.entity.PoiEntity
import com.travelfoodie.feature.attraction.databinding.ItemAttractionBinding

class AttractionAdapter(
    private val onShareClick: ((PoiEntity) -> Unit)? = null,
    private val onSpeakClick: ((PoiEntity) -> Unit)? = null
) : ListAdapter<PoiEntity, AttractionAdapter.AttractionViewHolder>(AttractionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val binding = ItemAttractionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttractionViewHolder(binding, onShareClick, onSpeakClick)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttractionViewHolder(
        private val binding: ItemAttractionBinding,
        private val onShareClick: ((PoiEntity) -> Unit)?,
        private val onSpeakClick: ((PoiEntity) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: PoiEntity) {
            binding.apply {
                textViewAttractionName.text = poi.name
                textViewAttractionCategory.text = poi.category
                textViewAttractionRating.text = "⭐ ${poi.rating}"
                textViewAttractionDescription.text = poi.description ?: ""

                // Make the entire card clickable to open in Google Maps
                root.setOnClickListener {
                    openInGoogleMaps(poi)
                }

                // Share button (if we add one later)
                root.setOnLongClickListener {
                    onShareClick?.invoke(poi)
                    true
                }

                // TTS button - read description aloud
                // Note: Add a speaker icon button to item_attraction.xml layout
                // For now, use double-tap on description
                textViewAttractionDescription.setOnClickListener {
                    if (!poi.description.isNullOrEmpty()) {
                        onSpeakClick?.invoke(poi)
                    }
                }
            }
        }

        private fun openInGoogleMaps(poi: PoiEntity) {
            // Create a search query for Google Maps
            val searchQuery = Uri.encode(poi.name)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$searchQuery")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(binding.root.context.packageManager) != null) {
                binding.root.context.startActivity(mapIntent)
            } else {
                // Fallback to web browser if Google Maps is not installed
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=$searchQuery")
                )
                binding.root.context.startActivity(browserIntent)
            }
        }
    }

    class AttractionDiffCallback : DiffUtil.ItemCallback<PoiEntity>() {
        override fun areItemsTheSame(oldItem: PoiEntity, newItem: PoiEntity): Boolean {
            return oldItem.poiId == newItem.poiId
        }

        override fun areContentsTheSame(oldItem: PoiEntity, newItem: PoiEntity): Boolean {
            return oldItem == newItem
        }
    }
}
