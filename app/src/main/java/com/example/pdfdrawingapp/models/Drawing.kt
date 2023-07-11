package com.example.pdfdrawingapp.models

data class Drawing(
    val id: Int,
    val text: String,
    val imageResource: Int,
    val type: DrawingType,
    val tutorialPoints: List<TutorialPoint> = emptyList()
)
