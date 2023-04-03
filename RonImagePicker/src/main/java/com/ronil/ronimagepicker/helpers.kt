package com.ronil.ronimagepicker

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

const val RESULT_IMAGE_PATH = "RESULT_IMAGE_PATH"
const val RESULT_IMAGE_FILE = "RESULT_IMAGE_FILE"
const val CAMERA = "CAMERA"
const val GALLERY = "GALLERY"
const val BOTH = "BOTH"
const val FILE_PATH = "FILE_PATH"
const val SELECTION_TYPE = "SELECTION_TYPE"
const val WANT_CROP = "WANT_CROP"
const val WANT_COMPRESSION = "WANT_COMPRESSION"
const val COMPRESSION_PERCENTAGE = "COMPRESSION_PERCENTAGE"

interface ResultImage {
    val startForResult: ActivityResultLauncher<Intent>
}