package com.cletaeats.upload

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

@Service
class UploadService(
    @Value("\${cletaeats.upload-dir:uploads/images}")
    private val uploadDir: String,

    @Value("\${cletaeats.public-base-url:http://localhost:8080}")
    private val publicBaseUrl: String
) {

    fun guardarImagen(file: MultipartFile): String {
        if (file.isEmpty) {
            throw IllegalArgumentException("El archivo está vacío.")
        }

        val originalName = file.originalFilename ?: "imagen.jpg"
        val extension = originalName.substringAfterLast('.', "jpg").lowercase()

        val allowed = setOf("jpg", "jpeg", "png", "webp")

        if (extension !in allowed) {
            throw IllegalArgumentException("Formato de imagen no permitido.")
        }

        val directory: Path = Paths.get(uploadDir)
        Files.createDirectories(directory)

        val fileName = "${UUID.randomUUID()}.$extension"
        val destination = directory.resolve(fileName)

        file.inputStream.use { input ->
            Files.copy(input, destination)
        }

        return "${publicBaseUrl.trimEnd('/')}/uploads/images/$fileName"
    }
}