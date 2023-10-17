package com.ronil.imagepicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ronil.imagepicker.project.R
import com.ronil.imagepicker.utils.RESULT_IMAGE_FILE
import com.ronil.imagepicker.utils.RESULT_IMAGE_PATH
import com.ronil.imagepicker.utils.ResultImage
import java.io.File


class MainActivity : AppCompatActivity() {
    val imageView: ImageView by lazy { findViewById(R.id.image) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        RonImagePicker(this, resultCallback).allowGalleryOnly(true).start()

    }

/*
    private val resultCallback = object : ResultImage {
        override val result: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imagePath = result.data?.getStringExtra(RESULT_IMAGE_PATH)
                val imageFile: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra(
                        RESULT_IMAGE_FILE, File::class.java
                    )
                } else {
                    result.data?.getSerializableExtra(RESULT_IMAGE_FILE) as File
                }

                imageView.setImageURI(Uri.fromFile(imageFile))

            }


        }
    }
*/

}
