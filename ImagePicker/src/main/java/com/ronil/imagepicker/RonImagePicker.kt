package com.ronil.imagepicker

import android.content.Context
import android.content.Intent
import com.ronil.imagepicker.activities.ImagePickerMainActivity
import com.ronil.imagepicker.utils.BOTH
import com.ronil.imagepicker.utils.CAMERA
import com.ronil.imagepicker.utils.COMPRESSION_PERCENTAGE
import com.ronil.imagepicker.utils.GALLERY
import com.ronil.imagepicker.utils.ResultImage
import com.ronil.imagepicker.utils.SELECTION_TYPE
import com.ronil.imagepicker.utils.WANT_COMPRESSION
import com.ronil.imagepicker.utils.WANT_CROP

class RonImagePicker(
    private val activity: Context,
    private var resultImageCallback: ResultImage

) {
    private var crop: Boolean = false
    private var cameraOnly: Boolean = false
    private var galleryOnly: Boolean = false
    private var compress: Boolean = false


    fun allowCrop(crop: Boolean): RonImagePicker {
        this.crop = crop
        return this
    }

    fun allowCameraOnly(cameraOnly: Boolean): RonImagePicker {
        this.cameraOnly = cameraOnly
        return this
    }

    fun allowGalleryOnly(galleryOnly: Boolean): RonImagePicker {
        this.galleryOnly = galleryOnly
        return this
    }

    fun allowCompress(compress: Boolean): RonImagePicker {
        this.compress = compress
        return this
    }

    fun start() {
        val selection = if (cameraOnly && !galleryOnly) {
            CAMERA
        } else if (galleryOnly && !cameraOnly) {
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
            }
        )
    }


}

