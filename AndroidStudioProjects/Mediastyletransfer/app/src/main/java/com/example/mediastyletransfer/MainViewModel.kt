package com.example.mediastyletransfer

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastyletransfer.api.backend.RetrofitClient
import com.example.mediastyletransfer.api.backend.StyleRequest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

// 1. Definimos una clase de datos 'UiState'.
//    Piensa en esto como una "foto" del estado completo de nuestra pantalla en un momento dado.
//    Contiene toda la información necesaria para dibujar la interfaz.
data class UiState(
    val previewUri: Uri? = null,              // La URI de la imagen/video seleccionado para la vista previa.
    val isGenerateButtonEnabled: Boolean = false, // Si el botón "Generar" debe estar activo.
    val isLoading: Boolean = false,               // Si estamos mostrando la barra de progreso.
    val loadingMessage: String = "",              // El mensaje que se muestra junto a la barra de progreso.
    val resultBitmap: Bitmap? = null,             // La imagen estilizada generada por la IA.
    val errorMessage: String? = null              // Un mensaje de error para mostrar al usuario, si lo hay.
)

// 2. Nuestro ViewModel. Hereda de 'ViewModel' de Android para obtener sus "superpoderes".
class MainViewModel : ViewModel() {

    // 3. '_uiState' es un LiveData "privado" y "mutable". Solo el ViewModel puede cambiar su valor.
    //    Lo inicializamos con un estado por defecto (todo vacío o deshabilitado).
    private val _uiState = MutableLiveData(UiState())

    // 4. 'uiState' es la versión "pública" y "no mutable" que expondremos a la MainActivity.
    //    La MainActivity podrá "observar" los cambios en este LiveData, pero no podrá modificarlos.
    val uiState: LiveData<UiState> = _uiState

    private var selectedBitmap: Bitmap? = null   // Aquí guardaremos la imagen cargada en memoria.

    // 5. Función que se llama cuando el usuario selecciona un archivo de la galería.
    fun onMediaSelected(uri: Uri, contentResolver: ContentResolver) {
        try {
            // Convertimos la URI del archivo en un Bitmap (una imagen en memoria).
            val inputStream = contentResolver.openInputStream(uri)
            selectedBitmap = BitmapFactory.decodeStream(inputStream)

            // Actualizamos el estado de la UI:
            //  - Guardamos la URI para mostrar la vista previa.
            //  - Habilitamos el botón de generar.
            //  - Limpiamos cualquier resultado anterior.
            _uiState.value = _uiState.value?.copy(
                previewUri = uri,
                isGenerateButtonEnabled = true,
                resultBitmap = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value?.copy(errorMessage = "Error al cargar el medio.")
        }
    }

    // 6. Función que se llama cuando el usuario pulsa el botón "Generar".
    fun onGenerateClicked(style: String) {
        val imageToProcess = selectedBitmap ?: return // Si no hay imagen, no hacemos nada.

        // Actualizamos la UI para mostrar que estamos trabajando.
        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            loadingMessage = "Contactando al servidor...",
            isGenerateButtonEnabled = false // Deshabilitamos el botón para evitar clics múltiples.
        )

        // 7. Usamos 'viewModelScope.launch' para iniciar una corrutina.
        //    Esto ejecuta la llamada a la API en un hilo de fondo, para no congelar la app.
        viewModelScope.launch {
            try {
                // Convertimos la imagen a Base64 para enviarla como texto
                val imageBase64 = imageToProcess.toBase64()
                val request = StyleRequest(image_base64 = imageBase64, style = style)

                _uiState.postValue(_uiState.value?.copy(loadingMessage = "Aplicando estilo..."))

                // Hacemos la llamada a nuestro backend
                val response = RetrofitClient.apiService.stylizeImage(request)

                // Decodificamos la respuesta Base64 a una imagen
                val resultBytes = Base64.decode(response.stylized_image_base64, Base64.DEFAULT)
                val resultBitmap = BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.size)

                // Actualizamos la UI con el resultado
                _uiState.postValue(
                    _uiState.value?.copy(
                        isLoading = false,
                        resultBitmap = resultBitmap,
                        isGenerateButtonEnabled = true
                    )
                )

            } catch (e: Exception) {
                // Si algo falla, mostramos un error MÁS DETALLADO.
                val errorType = e.javaClass.simpleName
                _uiState.postValue(
                    _uiState.value?.copy(
                        isLoading = false,
                        errorMessage = "Error [$errorType]: ${e.message}",
                        isGenerateButtonEnabled = true
                    )
                )
            }
        }
    }

    // 8. Una función de utilidad para "resetear" el mensaje de error una vez que se ha mostrado.
    fun onErrorShown() {
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    // 9. Función de extensión para convertir un Bitmap a un string Base64.
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}