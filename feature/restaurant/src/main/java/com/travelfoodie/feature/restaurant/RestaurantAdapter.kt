package com.travelfoodie.feature.restaurant

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.feature.restaurant.databinding.ItemRestaurantBinding

class RestaurantAdapter(
    private val onRestaurantClick: ((RestaurantEntity) -> Unit)? = null,
    private val onShareClick: ((RestaurantEntity) -> Unit)? = null
) : ListAdapter<RestaurantEntity, RestaurantAdapter.RestaurantViewHolder>(RestaurantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RestaurantViewHolder(binding, onRestaurantClick, onShareClick)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RestaurantViewHolder(
        private val binding: ItemRestaurantBinding,
        private val onRestaurantClick: ((RestaurantEntity) -> Unit)?,
        private val onShareClick: ((RestaurantEntity) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: RestaurantEntity) {
            binding.apply {
                textViewRestaurantName.text = restaurant.name
                textViewRestaurantCategory.text = restaurant.category
                textViewRestaurantRating.text = "⭐ ${restaurant.rating}"
                textViewRestaurantDistance.text = restaurant.distance?.let { "%.1f km".format(it) } ?: "거리 정보 없음"

                // Click to open in Google Maps
                root.setOnClickListener {
                    openInGoogleMaps(restaurant)
                    onRestaurantClick?.invoke(restaurant)
                }

                // Long click to share
                root.setOnLongClickListener {
                    onShareClick?.invoke(restaurant)
                    true
                }
            }
        }

        private fun openInGoogleMaps(restaurant: RestaurantEntity) {
            // Create a search query for Google Maps
            val searchQuery = Uri.encode(restaurant.name)
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

    class RestaurantDiffCallback : DiffUtil.ItemCallback<RestaurantEntity>() {
        override fun areItemsTheSame(oldItem: RestaurantEntity, newItem: RestaurantEntity): Boolean {
            return oldItem.restaurantId == newItem.restaurantId
        }

        override fun areContentsTheSame(oldItem: RestaurantEntity, newItem: RestaurantEntity): Boolean {
            return oldItem == newItem
        }
    }
}
