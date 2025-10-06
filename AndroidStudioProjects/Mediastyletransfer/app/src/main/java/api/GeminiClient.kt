package com.example.mediastyletransfer.api

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

/**
 * Esta clase tiene una única responsabilidad: hablar con la API de Gemini.
 * La hemos separado para mantener el código limpio y organizado.
 */
class GeminiClient {

    // 1. Creamos una instancia del modelo de IA que vamos a usar.
    //    'gemini-1.5-flash' es un modelo rápido y muy bueno para tareas visuales.
    //    Aquí es donde usamos el `ApiConfig.API_KEY` que preparamos antes.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = ApiConfig.API_KEY
    )

    /**
     * 2. Esta es una función 'suspend'. Significa que puede ser "pausada"
     *    mientras espera una respuesta de internet, sin que la aplicación se congele.
     *    Recibe:
     *      - image: La imagen original que el usuario seleccionó (en formato Bitmap).
     *      - style: El texto del estilo que el usuario eligió (ej. "Watercolor").
     *    Devuelve:
     *      - Un objeto 'Result', que puede ser un éxito (conteniendo la nueva imagen)
     *        o un fallo (conteniendo un error que explica qué pasó).
     */
    suspend fun applyStyleToImage(image: Bitmap, style: String): Result<Bitmap> {
        return try {
            // 3. Creamos el 'prompt': la instrucción exacta que le damos a la IA.
            //    Ser específico aquí es clave para obtener buenos resultados.
            val prompt = "Transform this image into a $style style. Respond with only the stylized image, without any text, borders, or explanations."

            // 4. Preparamos el contenido que vamos a enviar: la imagen y el texto del prompt.
            val inputContent = content {
                image(image)
                text(prompt)
            }

            // 5. ¡La llamada a la acción! Enviamos la petición y esperamos la respuesta.
            //    Como la función es 'suspend', Kotlin se encarga de esperar sin bloquear nada.
            val response = generativeModel.generateContent(inputContent)

            // 6. Una vez tenemos la respuesta, extraemos la imagen (Bitmap) de ella.
            val resultBitmap = response.candidates.firstOrNull()?.content?.parts?.filterIsInstance<Bitmap>()?.firstOrNull()

            if (resultBitmap != null) {
                // 7. Si obtuvimos una imagen, devolvemos un resultado de ÉXITO.
                Result.success(resultBitmap)
            } else {
                // 8. Si no, buscamos el texto del error que nos da la API y devolvemos un FALLO.
                val errorText = response.text ?: "La API no devolvió una imagen."
                Result.failure(Exception("Error de Gemini: $errorText"))
            }

        } catch (e: Exception) {
            // 9. Si ocurre un error de red o cualquier otro problema, lo capturamos y devolvemos un FALLO.
            Result.failure(e)
        }
    }
}