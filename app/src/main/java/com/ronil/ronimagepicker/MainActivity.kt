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

class MainActivity : AppCompatActivity(), ResultImage {
    lateinit var image: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image = findViewById(R.id.image)
        findViewById<Button>(R.id.butt).setOnClickListener {
            callImagePicker()
        }
    }

    private fun callImagePicker() {
        val imagePicker = RonImagePicker(this, this)
        imagePicker.getImage(false, false, false, false, 100)
    }

    override val startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val imagePath = result.data!!.getStringExtra(RESULT_IMAGE_PATH)
                    val file: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data!!.getSerializableExtra(
                            RESULT_IMAGE_FILE, File::class.java
                        )
                    } else {
                        result.data!!.getSerializableExtra(RESULT_IMAGE_FILE) as File?
                    }
                    image.setImageURI(Uri.fromFile(file))
                }
            }
        }


}
