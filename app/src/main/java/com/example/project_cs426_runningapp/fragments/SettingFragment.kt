package com.example.project_cs426_runningapp.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    val fileName = "SettingData.txt"
    var fileBody = "000000"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadFromFile()

        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        val switches = arrayOf(
            binding.switchGeneral,
            binding.switchSound,
            binding.switchVibrate,
            binding.switchUpdate,
            binding.switchService,
            binding.switchTip
        )

        //First setting
        firstSet(switches)
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.SettingEditBack -> {
                    saveToFile()
                    findNavController().popBackStack()
                }
                binding.LogoutBtn -> {
                    saveToFile()
                    findNavController().popBackStack(R.id.onboardingFragment,false)
                    Toast.makeText(requireContext(), "Log out successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.SettingEditBack.setOnClickListener(clickListener)
        binding.LogoutBtn.setOnClickListener(clickListener)


        for((index, switch) in switches.withIndex()) {
            switch.setOnCheckedChangeListener { _, isChecked ->
                val switchName = switch.tag // You can use a tag to identify the switch
                if (isChecked) {
                    // Switch is turned on
                    fileBody = fileBody.substring(0, index) + "1" + fileBody.substring(index + 1)
                    // Do something when the switch is turned on
                    Log.d("TAG", "$switchName turned on")
                } else {
                    // Switch is turned off
                    fileBody = fileBody.substring(0, index) + "0" + fileBody.substring(index + 1)
                    // Do something when the switch is turned off
                    Log.d("TAG", "$switchName turned off")
                }
            }
        }
        return binding.root
    }

    private fun saveToFile(){
        context?.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output?.write(fileBody.toByteArray())
        }
    }

    private fun loadFromFile(){
        try {
            val fileInputStream = context?.openFileInput(fileName)
            if (fileInputStream != null) {
                fileBody = fileInputStream.bufferedReader().use {
                    it.readText()
                }
                fileInputStream.close()
                Log.d("TAG", "LOADED: $fileBody")
            } else {
                val initialContent = "000000"
                context?.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                    output?.write(initialContent.toByteArray())
                }
                fileBody = initialContent
                Log.d("TAG", "File created with initial content: $initialContent")
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur when working with files
            Log.e("TAG", "Error loading file: ${e.message}", e)
        }
    }

    private fun firstSet(Switches: Array<Switch>){
        for((index,Switch) in Switches.withIndex()) {
            if (fileBody.get(index) == '0')
                Switch.isChecked = false
            else Switch.isChecked = true
        }
    }
}