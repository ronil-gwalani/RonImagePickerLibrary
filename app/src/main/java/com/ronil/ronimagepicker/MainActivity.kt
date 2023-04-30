package com.ronil.ronimagepicker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.ronil.ronimagepicker.project.R
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}
