package com.ronil.imagepicker.utils.compreser.constraint

import java.io.File


interface Constraint {
    fun isSatisfied(imageFile: File): Boolean

    fun satisfy(imageFile: File): File
}