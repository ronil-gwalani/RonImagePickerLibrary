package com.ronil.imagepicker.utils.compreser.constraint

import com.ronil.imagepicker.utils.compreser.loadBitmap
import com.ronil.imagepicker.utils.compreser.overWrite
import java.io.File


internal class QualityConstraint(private val quality: Int) : Constraint {
    private var isResolved = false

    override fun isSatisfied(imageFile: File): Boolean {
        return isResolved
    }

    override fun satisfy(imageFile: File): File {
        val result = overWrite(imageFile, loadBitmap(imageFile), quality = quality)
        isResolved = true
        return result
    }
}

fun Compression.quality(quality: Int) {
    constraint(QualityConstraint(quality))
}

