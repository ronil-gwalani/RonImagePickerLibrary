package com.ronil.imagepicker.utils.compreser.constraint

import android.graphics.BitmapFactory
import com.ronil.imagepicker.utils.compreser.calculateInSampleSize
import com.ronil.imagepicker.utils.compreser.decodeSampledBitmapFromFile
import com.ronil.imagepicker.utils.compreser.determineImageRotation
import com.ronil.imagepicker.utils.compreser.overWrite
import java.io.File

class ResolutionConstraint(private val width: Int, private val height: Int) : Constraint {

    override fun isSatisfied(imageFile: File): Boolean {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(imageFile.absolutePath, this)
            calculateInSampleSize(this, width, height) <= 1
        }
    }

    override fun satisfy(imageFile: File): File {
        return decodeSampledBitmapFromFile(imageFile, width, height).run {
            determineImageRotation(imageFile, this).run {
                overWrite(imageFile, this)
            }
        }
    }
}

fun Compression.resolution(width: Int, height: Int) {
    constraint(ResolutionConstraint(width, height))
}