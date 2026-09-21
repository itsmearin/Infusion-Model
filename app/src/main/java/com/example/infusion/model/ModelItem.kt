package com.example.infusion.model

data class ModelItem (
    val id: String,
    val modelFile: String,

    var x: Float = 100f,
    var y: Float = 100f,

    var width: Float = 400f,
    var height: Float = 400f,

    var interactionMode: Boolean = false,
    var showLabels: Boolean = false
)
