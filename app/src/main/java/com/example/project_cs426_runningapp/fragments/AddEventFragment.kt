package com.example.project_cs426_runningapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentAddEventBinding
import com.example.project_cs426_runningapp.databinding.FragmentEventBinding
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AddEventFragment: Fragment() {
    private lateinit var binding: FragmentAddEventBinding

    private lateinit var resultImage: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEventBinding.inflate(inflater, container, false)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.addEventScreenReturnButton -> {
                    findNavController().popBackStack()
                }
            }
        }

        binding.addEventScreenReturnButton.setOnClickListener(clickListener)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Date
        binding.startMonthPicker.minValue = 1
        binding.startMonthPicker.maxValue = 12

        binding.startDayPicker.minValue = 1
        binding.startDayPicker.maxValue = 31

        binding.startYearPicker.minValue = 2023
        binding.startYearPicker.maxValue = 2100

        binding.endMonthPicker.minValue = 1
        binding.endMonthPicker.maxValue = 12

        binding.endDayPicker.minValue = 1
        binding.endDayPicker.maxValue = 31

        binding.endYearPicker.minValue = 2023
        binding.endYearPicker.maxValue = 2100

        //Choose image
        binding.addEventImageButton.setOnClickListener {
            openGallery()
        }
    }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data

                if (selectedImageUri != null) {
                    resultImage = selectedImageUri
                }

                binding.eventIconImage.visibility = View.VISIBLE
                binding.eventLoadText.visibility = View.VISIBLE
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore. Images.Media.EXTERNAL_CONTENT_URI)
        getContentLauncher.launch(intent)
    }
}