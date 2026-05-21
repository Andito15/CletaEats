package com.cletaeats.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebMvcConfig(
    @Value("\${cletaeats.upload-dir:uploads/images}")
    private val uploadDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val path = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize()
            .toUri()
            .toString()

        registry.addResourceHandler("/uploads/images/**")
            .addResourceLocations(path)
    }
}