package com.example.mediastyletransfer.api.backend

/**
 * Define la estructura de datos que la app enviará a tu backend.
 * Contiene la imagen codificada en Base64 y el nombre del estilo.
 */
data class StyleRequest(
    val image_base64: String,
    val style: String
)
