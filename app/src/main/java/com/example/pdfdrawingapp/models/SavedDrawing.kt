package com.example.pdfdrawingapp.models

import java.io.File
import java.util.UUID

data class SavedDrawing(
    val id: String = UUID.randomUUID().toString(),
    val file: File,
    val isSelected: Boolean = false,
    val isSelectionActive: Boolean = false
)
