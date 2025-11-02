package com.travelfoodie.feature.attraction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfoodie.core.data.local.entity.PoiEntity
import com.travelfoodie.feature.attraction.databinding.ItemAttractionBinding

class AttractionAdapter : ListAdapter<PoiEntity, AttractionAdapter.AttractionViewHolder>(AttractionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val binding = ItemAttractionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttractionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttractionViewHolder(
        private val binding: ItemAttractionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: PoiEntity) {
            binding.apply {
                textViewAttractionName.text = poi.name
                textViewAttractionCategory.text = poi.category
                textViewAttractionRating.text = "⭐ ${poi.rating}"
                textViewAttractionDescription.text = poi.description ?: ""
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
