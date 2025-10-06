package com.example.mediastyletransfer.api.backend

/**
 * Define la estructura de datos que la app recibirá de tu backend.
 * Contiene la imagen estilizada, codificada en Base64.
 */
data class StyleResponse(
    val stylized_image_base64: String
)
