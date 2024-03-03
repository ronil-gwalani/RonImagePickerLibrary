package com.ronil.imagepicker

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ronil.imagepicker.project.R


class MainActivity : AppCompatActivity() {
    val imageView: ImageView by lazy { findViewById(R.id.image) }
    val getImage: Button by lazy { findViewById(R.id.getImage) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getImage.setOnClickListener {
//            RonImagePicker(this, resultCallback).allowCrop(true).allowCompress(true).start()
        }

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
