package com.example.mediastyletransfer.api.backend

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Define la API de tu backend usando anotaciones de Retrofit.
 */
interface BackendApiService {

    /**
     * Envía una petición POST al endpoint "/stylize" de tu servidor Vercel.
     * @param request El cuerpo de la petición, que contiene la imagen y el estilo.
     * @return La respuesta del servidor, que contiene la imagen estilizada.
     */
    @POST("stylize")
    suspend fun stylizeImage(@Body request: StyleRequest): StyleResponse
}
