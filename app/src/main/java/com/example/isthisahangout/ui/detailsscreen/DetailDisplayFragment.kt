package com.example.isthisahangout.ui.detailsscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.isthisahangout.R
import com.example.isthisahangout.databinding.FragmentDetailDisplayBinding
import com.example.isthisahangout.viewmodel.FavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailDisplayFragment : Fragment(R.layout.fragment_detail_display) {
    private var _binding: FragmentDetailDisplayBinding? = null
    private val binding get() = _binding!!
    private val viewModels by viewModels<FavouritesViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailDisplayBinding.bind(view)
        binding.apply {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}