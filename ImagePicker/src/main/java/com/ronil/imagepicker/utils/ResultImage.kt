package com.ronil.imagepicker.utils

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

interface ResultImage {
    val result: ActivityResultLauncher<Intent>
}