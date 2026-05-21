package com.cletaeats.upload

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

data class ImagenUploadResponse(
    val url: String,
    val filename: String
)

@RestController
@RequestMapping("/api/admin/uploads")
class UploadController(
    @Value("\${cletaeats.upload-dir:uploads/images}")
    private val uploadDir: String
) {

    @PostMapping("/imagen")
    fun subirImagen(
        @RequestParam("file") file: MultipartFile
    ): ImagenUploadResponse {
        if (file.isEmpty) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El archivo está vacío."
            )
        }

        val contentType = file.contentType ?: ""

        val extension = when (contentType.lowercase()) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            else -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Formato no permitido. Use PNG, JPG o WEBP."
            )
        }

        val directory: Path = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize()

        Files.createDirectories(directory)

        val filename = "${UUID.randomUUID()}.$extension"
        val target = directory.resolve(filename)

        file.inputStream.use { input ->
            Files.copy(
                input,
                target,
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        val baseUrl = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .build()
            .toUriString()

        val url = "$baseUrl/uploads/images/$filename"

        return ImagenUploadResponse(
            url = url,
            filename = filename
        )
    }
}