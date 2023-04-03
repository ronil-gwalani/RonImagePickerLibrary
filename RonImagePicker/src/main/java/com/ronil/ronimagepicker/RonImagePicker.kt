package com.ronil.ronimagepicker

import android.content.Context
import android.content.Intent

class RonImagePicker(
    private val activity: Context,
    private var resultImage: ResultImage
) {


    fun getImage(
        crop: Boolean = true,
        cameraOnly: Boolean = false,
        galleyOnly: Boolean = false,
        compress: Boolean = false,
        compressionPercentage: Int = 100
    ) {
        val selection = if (cameraOnly && !galleyOnly) {
            CAMERA
        } else if (galleyOnly && !cameraOnly) {
            GALLERY
        } else {
            BOTH
        }

        resultImage.startForResult.launch(
            Intent(
                activity,
                ImagePickerMainActivity::class.java
            ).apply {
                putExtra(WANT_CROP, crop)
                putExtra(SELECTION_TYPE, selection)
                putExtra(WANT_COMPRESSION, compress)
                putExtra(COMPRESSION_PERCENTAGE, compressionPercentage)
            }
        )
    }


}

