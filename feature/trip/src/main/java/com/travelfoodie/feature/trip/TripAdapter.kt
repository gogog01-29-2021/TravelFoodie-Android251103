package com.travelfoodie.feature.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.feature.trip.databinding.ItemTripBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TripAdapter(
    private val onTripClick: (TripEntity) -> Unit,
    private val onTripLongClick: (TripEntity) -> Unit
) : ListAdapter<TripEntity, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding, onTripClick, onTripLongClick)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TripViewHolder(
        private val binding: ItemTripBinding,
        private val onTripClick: (TripEntity) -> Unit,
        private val onTripLongClick: (TripEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        fun bind(trip: TripEntity) {
            binding.apply {
                textViewTripTitle.text = trip.title
                textViewTripDates.text = "${dateFormat.format(Date(trip.startDate))} - ${dateFormat.format(Date(trip.endDate))}"
                textViewTripTheme.text = trip.theme
                
                val daysUntil = TimeUnit.MILLISECONDS.toDays(trip.startDate - System.currentTimeMillis())
                textViewDDay.text = when {
                    daysUntil < 0 -> "완료"
                    daysUntil == 0L -> "D-Day"
                    else -> "D-$daysUntil"
                }

                root.setOnClickListener { onTripClick(trip) }
                root.setOnLongClickListener {
                    onTripLongClick(trip)
                    true
                }
            }
        }
    }

    class TripDiffCallback : DiffUtil.ItemCallback<TripEntity>() {
        override fun areItemsTheSame(oldItem: TripEntity, newItem: TripEntity): Boolean {
            return oldItem.tripId == newItem.tripId
        }

        override fun areContentsTheSame(oldItem: TripEntity, newItem: TripEntity): Boolean {
            return oldItem == newItem
        }
    }
}
