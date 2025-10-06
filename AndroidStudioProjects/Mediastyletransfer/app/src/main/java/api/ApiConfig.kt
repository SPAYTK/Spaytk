package com.example.mediastyletransfer.api

import com.example.mediastyletransfer.BuildConfig

/**
 * Un 'object' es como una clase de la que solo puede existir una única instancia.
 * Es perfecto para configuraciones globales. Aquí guardamos nuestra API key
 * que leímos de forma segura desde el archivo build.gradle.kts.
 */
object ApiConfig {
    const val API_KEY = BuildConfig.GEMINI_API_KEY
}
