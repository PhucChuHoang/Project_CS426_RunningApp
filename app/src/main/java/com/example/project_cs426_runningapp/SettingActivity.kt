package com.example.project_cs426_runningapp

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState);
        supportActionBar?.hide()
        setContentView(R.layout.activity_setting);

        val backBtn: ImageView = findViewById(R.id.SettingEditBack);
        backBtn.setOnClickListener{onBackPressed();}
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // This will finish the current activity and remove it from the stack.
    }
}