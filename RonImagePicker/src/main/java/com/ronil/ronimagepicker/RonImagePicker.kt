package com.ronil.ronimagepicker

import android.content.Context
import android.content.Intent
import com.ronil.ronimagepicker.activities.ImagePickerMainActivity
import com.ronil.ronimagepicker.utils.BOTH
import com.ronil.ronimagepicker.utils.CAMERA
import com.ronil.ronimagepicker.utils.COMPRESSION_PERCENTAGE
import com.ronil.ronimagepicker.utils.GALLERY
import com.ronil.ronimagepicker.utils.ResultImage
import com.ronil.ronimagepicker.utils.SELECTION_TYPE
import com.ronil.ronimagepicker.utils.WANT_COMPRESSION
import com.ronil.ronimagepicker.utils.WANT_CROP

class RonImagePicker(
    private val activity: Context,
    private var resultImageCallback: ResultImage

) {
    private var crop: Boolean = false
    private var cameraOnly: Boolean = false
    private var galleyOnly: Boolean = false
    private var compress: Boolean = false
    private var compressionPercentage: Int = 100


    fun allowCrop(crop: Boolean): RonImagePicker {
        this.crop = crop
        return this
    }

    fun allowCameraOnly(cameraOnly: Boolean): RonImagePicker {
        this.cameraOnly = cameraOnly
        return this
    }

    fun allowGalleyOnly(galleyOnly: Boolean): RonImagePicker {
        this.galleyOnly = galleyOnly
        return this
    }

    fun allowCompress(compress: Boolean, compressionPercentage: Int): RonImagePicker {
        this.compress = compress
        this.compressionPercentage = compressionPercentage
        return this
    }

    fun start() {
        val selection = if (cameraOnly && !galleyOnly) {
            CAMERA
        } else if (galleyOnly && !cameraOnly) {
            GALLERY
        } else {
            BOTH
        }

        resultImageCallback.result.launch(
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

