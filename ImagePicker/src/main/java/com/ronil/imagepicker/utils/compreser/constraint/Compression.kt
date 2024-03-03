package com.ronil.imagepicker.utils.compreser.constraint

import com.ronil.imagepicker.utils.compreser.constraint.Constraint

class Compression {
    internal val constraints: MutableList<Constraint> = mutableListOf()

    fun constraint(constraint: Constraint) {
        constraints.add(constraint)
    }
}