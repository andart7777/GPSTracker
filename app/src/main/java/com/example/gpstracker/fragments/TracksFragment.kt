package com.example.gpstracker.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpstracker.MainApp
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.databinding.TracksBinding
import com.example.gpstracker.db.TrackAdaptor
import com.example.gpstracker.db.TrackItem

class TracksFragment : Fragment(), TrackAdaptor.Listener {
    private lateinit var binding: TracksBinding
    private lateinit var adaptor: TrackAdaptor
    private val model: MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks() {
        model.tracks.observe(viewLifecycleOwner){
            adaptor.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initRcView() = with(binding) {
        adaptor = TrackAdaptor(this@TracksFragment)
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adaptor
    }

    companion object {
        @JvmStatic
        fun newInstance() = TracksFragment()
    }

    override fun onClick(track: TrackItem) {
        model.deleteTrack(track)
    }
}