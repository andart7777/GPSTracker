package com.example.gpstracker.db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gpstracker.R
import com.example.gpstracker.databinding.TrackItemBinding

class TrackAdaptor(private val listener: Listener) : ListAdapter<TrackItem, TrackAdaptor.Holder>(Comparator()) {

    class Holder(view: View, private var listener: Listener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val binding = TrackItemBinding.bind(view)
        private var trackTemp: TrackItem? = null

        init {
            binding.ibDeleteTrackList.setOnClickListener(this)
            binding.item.setOnClickListener(this)
        }

        fun bind(track: TrackItem) = with(binding) {
            trackTemp = track
            val speed = "${track.velocity} km/h"
            val distance = "${track.distance} km"
            tvDateTrackList.text = track.date
            tvSpeedTrackList.text = speed
            tvTimeTrackList.text = track.time
            tvDistanceTrackList.text = distance
        }

        override fun onClick(v: View?) {
            val type = when(v?.id) {
                R.id.ibDeleteTrackList -> ClickType.DELETE
                R.id.item -> ClickType.OPEN
                else -> ClickType.OPEN
            }
            trackTemp?.let { listener.onClick(it, type) }
        }
    }

    class Comparator : DiffUtil.ItemCallback<TrackItem>() {
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)

        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(track: TrackItem, type: ClickType)
    }

    enum class ClickType{
        DELETE,
        OPEN
    }
}