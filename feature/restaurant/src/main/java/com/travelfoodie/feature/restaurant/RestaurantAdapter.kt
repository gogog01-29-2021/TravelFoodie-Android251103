package com.travelfoodie.feature.restaurant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.feature.restaurant.databinding.ItemRestaurantBinding

class RestaurantAdapter(
    private val onRestaurantClick: (RestaurantEntity) -> Unit
) : ListAdapter<RestaurantEntity, RestaurantAdapter.RestaurantViewHolder>(RestaurantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RestaurantViewHolder(binding, onRestaurantClick)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RestaurantViewHolder(
        private val binding: ItemRestaurantBinding,
        private val onRestaurantClick: (RestaurantEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: RestaurantEntity) {
            binding.apply {
                textViewRestaurantName.text = restaurant.name
                textViewRestaurantCategory.text = restaurant.category
                textViewRestaurantRating.text = "⭐ ${restaurant.rating}"
                textViewRestaurantDistance.text = restaurant.distance?.let { "%.1f km".format(it) } ?: "거리 정보 없음"
                
                root.setOnClickListener { onRestaurantClick(restaurant) }
            }
        }
    }

    class RestaurantDiffCallback : DiffUtil.ItemCallback<RestaurantEntity>() {
        override fun areItemsTheSame(oldItem: RestaurantEntity, newItem: RestaurantEntity): Boolean {
            return oldItem.restaurantId == newItem.restaurantId
        }

        override fun areContentsTheSame(oldItem: RestaurantEntity, newItem: RestaurantEntity): Boolean {
            return oldItem == newItem
        }
    }
}
