package com.ronil.imagepicker.utils.compreser.constraint

import android.graphics.Bitmap
import com.ronil.imagepicker.utils.compreser.compressFormat
import com.ronil.imagepicker.utils.compreser.loadBitmap
import com.ronil.imagepicker.utils.compreser.overWrite
import java.io.File


internal class FormatConstraint(private val format: Bitmap.CompressFormat) : Constraint {

    override fun isSatisfied(imageFile: File): Boolean {
        return format == imageFile.compressFormat()
    }

    override fun satisfy(imageFile: File): File {
        return overWrite(imageFile, loadBitmap(imageFile), format)
    }
}

fun Compression.format(format: Bitmap.CompressFormat) {
    constraint(FormatConstraint(format))
}