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

        private val shortDateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        fun bind(trip: TripEntity) {
            binding.apply {
                textViewTripTitle.text = trip.title
                textViewTripTheme.text = trip.theme

                // Calculate dates
                val currentTime = System.currentTimeMillis()
                val startDate = Date(trip.startDate)
                val endDate = Date(trip.endDate)
                val daysUntil = TimeUnit.MILLISECONDS.toDays(trip.startDate - currentTime)
                val tripDuration = TimeUnit.MILLISECONDS.toDays(trip.endDate - trip.startDate) + 1

                // D-Day badge
                textViewDDay.text = when {
                    daysUntil < 0 -> "완료"
                    daysUntil == 0L -> "D-Day"
                    else -> "D-$daysUntil"
                }

                // Date range display
                textViewStartDate.text = shortDateFormat.format(startDate)
                textViewEndDate.text = shortDateFormat.format(endDate)
                textViewDuration.text = "${tripDuration}일"

                // Visual date range bar
                setupDateRangeBar(trip, currentTime)

                root.setOnClickListener { onTripClick(trip) }
                root.setOnLongClickListener {
                    // Vibration feedback
                    val vibrator = root.context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(50)
                    }
                    onTripLongClick(trip)
                    true
                }
            }
        }

        private fun ItemTripBinding.setupDateRangeBar(trip: TripEntity, currentTime: Long) {
            val context = root.context

            // Determine trip status and color
            val (barColor, showCurrentIndicator, indicatorPosition) = when {
                currentTime < trip.startDate -> {
                    // Upcoming trip - Blue
                    Triple(
                        android.graphics.Color.parseColor("#2196F3"),
                        false,
                        0f
                    )
                }
                currentTime > trip.endDate -> {
                    // Past trip - Gray
                    Triple(
                        android.graphics.Color.parseColor("#9E9E9E"),
                        false,
                        0f
                    )
                }
                else -> {
                    // Ongoing trip - Green with current position indicator
                    val totalDuration = trip.endDate - trip.startDate
                    val elapsed = currentTime - trip.startDate
                    val progress = elapsed.toFloat() / totalDuration.toFloat()

                    Triple(
                        android.graphics.Color.parseColor("#4CAF50"),
                        true,
                        progress
                    )
                }
            }

            // Set bar color
            dateRangeBar.setBackgroundColor(barColor)

            // Show/hide and position current date indicator
            if (showCurrentIndicator) {
                currentDateIndicator.visibility = android.view.View.VISIBLE
                currentDateIndicator.post {
                    val parentWidth = (currentDateIndicator.parent as android.view.View).width
                    currentDateIndicator.x = (parentWidth * indicatorPosition) - (currentDateIndicator.width / 2f)
                }
            } else {
                currentDateIndicator.visibility = android.view.View.GONE
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
