package com.example.project_cs426_runningapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.transition.TransitionInflater
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentAddEventBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date


class AddEventFragment: Fragment() {
    private lateinit var binding: FragmentAddEventBinding

    private lateinit var resultImage: Uri

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        db = FirebaseFirestore.getInstance()

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

        binding.addEventAddButton.setOnClickListener {
            var start_month = binding.startMonthPicker.value
            var start_day = binding.startDayPicker.value
            var start_year = binding.startYearPicker.value

            var end_month = binding.endMonthPicker.value
            var end_day = binding.endDayPicker.value
            var end_year = binding.endYearPicker.value

            if (binding.addEventEventNameEditText.text.toString().isEmpty()) {
                createDialog("Add Error!!!", "Please enter event name")
            }
            else if (!isDateStringValid("$start_month-$start_day-$start_year", "MM-dd-yyyy") ||
                    !isDateStringValid("$end_month-$end_day-$end_year", "MM-dd-yyyy")) {
                createDialog("Add Error!!!", "Please enter a valid date")
            }
            else if (!isAfter("$start_month-$start_day-$start_year", "$end_month-$end_day-$end_year", "MM-dd-yyyy")) {
                createDialog("Add Error!!!", "End date must be the same or after start date")
            }
            else if (binding.eventIconImage.visibility == View.INVISIBLE) {
                createDialog("Add Error!!!", "Please choose an image for the event")
            }
            else {
                val sharedPreferences = requireContext().getSharedPreferences("total_event", 0)
                var total = sharedPreferences.getString("total", null)?.toInt()
                Log.d("Total", total.toString())
                if (total != null) {
                    saveImage(total)
                    addEventSuccess(binding.addEventEventNameEditText.text.toString(), total,
                        "$start_month/$start_day/$start_year", "$end_month-$end_day-$end_year")

                    Toast.makeText(
                        requireContext(),
                        "Added successfully.",
                        Toast.LENGTH_SHORT
                    ).show()

                    createDialog("Successfully added new event!", "")
                }
            }
        }
    }

    private fun saveImage(position: Int) {
        val storage = Firebase.storage("gs://cs426-project.appspot.com")
        var storageRef = storage.reference

        var eventRef = storageRef.child("events/" + "eid" + position + ".jpg")

        var bitmap = uriToBitmap(requireActivity().contentResolver, resultImage)
        val baos = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }
        val data = baos.toByteArray()

        var uploadTask = eventRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("Image upload to storage", "It's there bro")
        }
    }

    private fun addEventSuccess(event_name: String, position: Int, start_date: String, end_date: String) {
        val event = hashMapOf(
                "event_name" to event_name,
                "image_url" to "event_image_" + position + ".jpg",
                "status" to 1,
                "start_date" to start_date,
                "end_date" to end_date,
                "event_id" to "eid00" + position
            )

        db.collection("events")
            .document("eid00" + position)
            .set(event)

        db.collection("events")
            .document("eid00" + position)
            .collection("participants")
            .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))
    }

    fun uriToBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

    private fun isDateStringValid(input: String, dateFormat: String): Boolean {
        val sdf = SimpleDateFormat(dateFormat)
        sdf.isLenient = false

        try {
            val parsedDate = sdf.parse(input)
            val current_date_string = sdf.format(Date())
            val current_date = sdf.parse(current_date_string)

            return parsedDate.after(current_date)
        } catch (e: Exception) {
            return false
        }

        return false
    }

    private fun isAfter(start_date: String, end_date:String, dateFormat: String): Boolean {
        val sdf = SimpleDateFormat(dateFormat)
        sdf.isLenient = false

        val parsed_start_date = sdf.parse(start_date)
        val parsed_end_date = sdf.parse(end_date)

        return parsed_end_date.equals(parsed_start_date) || parsed_end_date.after(parsed_start_date)
    }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data

                if (selectedImageUri != null) {
                    resultImage = selectedImageUri

                    binding.eventIconImage.visibility = View.VISIBLE
                    binding.eventLoadText.visibility = View.VISIBLE
                }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore. Images.Media.EXTERNAL_CONTENT_URI)
        getContentLauncher.launch(intent)
    }

    private fun createDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (title == "Successfully added new event!")
                    findNavController().popBackStack()
            }
        alertDialogBuilder.show()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}