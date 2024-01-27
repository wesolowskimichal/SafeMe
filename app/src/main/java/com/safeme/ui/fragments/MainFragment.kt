package com.safeme.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.safeme.R
import com.safeme.ui.MainActivity
import com.safeme.ui.viewmodels.MainViewModel

class MainFragment: Fragment(R.layout.main_fragment) {
    lateinit var viewModel: MainViewModel
    private var currentMarker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as MainActivity).mainViewModel

        val btn = view.findViewById<Button>(R.id.button)
        btn.setOnClickListener {
            viewModel.start()
        }
        val stepsTextView = view.findViewById<TextView>(R.id.steps)
        viewModel.getSteps().observe(viewLifecycleOwner, Observer{steps ->
            if(steps == -1) {
                stepsTextView.text = "-"
            } else {
                stepsTextView.text = steps.toString()
            }
        })

        viewModel.isStarted.observe(viewLifecycleOwner, Observer {started ->
            if(started) {
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.stop_button))
                btn.text = "STOP JOURNEY"
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.start_button))
                btn.text = "START JOURNEY"
            }
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            viewModel.getLastPosition().observe(viewLifecycleOwner, Observer{position ->
                val targetPos = LatLng(position.latitude, position.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPos, 18f))
                viewModel.isStarted.observe(viewLifecycleOwner, Observer{started ->
                    if(started) {
                        currentMarker?.remove()
                        currentMarker = googleMap.addMarker(
                            MarkerOptions().position(targetPos)
                        )
                    }
                })
            })
            viewModel.getPositions().observe(viewLifecycleOwner, Observer{positions ->
                if (positions != null) {
                    for (i in 1 until positions.size) {
                        googleMap.addPolyline(
                            PolylineOptions()
                                .add(positions[i - 1], positions[i])
                        )
                    }
                }
            })
        }
    }
}