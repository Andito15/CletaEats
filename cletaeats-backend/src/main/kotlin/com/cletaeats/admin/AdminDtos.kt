package com.cletaeats.admin

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class AdminUsuarioResponse(
    val usuarioId: Long?,
    val nombre: String,
    val correo: String,
    val cedula: String,
    val telefono: String,
    val rol: String,
    val estado: String
)

data class AdminClienteResponse(
    val clienteId: Long?,
    val usuarioId: Long?,
    val nombre: String,
    val correo: String,
    val cedula: String,
    val telefono: String,
    val estado: String,
    val direccionExacta: String
)

data class AdminRepartidorResponse(
    val repartidorId: Long?,
    val usuarioId: Long?,
    val nombre: String,
    val correo: String,
    val cedula: String,
    val telefono: String,
    val estadoUsuario: String,
    val disponibilidad: String,
    val kilometrosRecorridosDia: BigDecimal,
    val amonestacionesActivas: Long
)

data class UsuarioEstadoRequest(
    val estado: String
)

data class ClienteSuspensionRequest(
    val suspendido: Boolean
)

data class RepartidorDisponibilidadRequest(
    val disponibilidad: String
)

data class AdminCreateUserRequest(
    @field:NotBlank(message = "El rol es obligatorio")
    val rol: String,

    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 150)
    val nombre: String,

    @field:NotBlank(message = "La cédula es obligatoria")
    @field:Size(max = 20)
    val cedula: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "Correo inválido")
    @field:Size(max = 120)
    val correo: String,

    @field:NotBlank(message = "El teléfono es obligatorio")
    @field:Size(max = 20)
    val telefono: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, max = 50)
    val password: String,

    @field:Size(max = 250)
    val direccionExacta: String? = null,

    @field:Size(max = 20)
    val disponibilidad: String? = null,

    @field:Size(max = 500)
    val fotoUrl: String? = null
)
data class AdminUpdateUserRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 150)
    val nombre: String,

    @field:NotBlank(message = "La cédula es obligatoria")
    @field:Size(max = 20)
    val cedula: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "Correo inválido")
    @field:Size(max = 120)
    val correo: String,

    @field:NotBlank(message = "El teléfono es obligatorio")
    @field:Size(max = 20)
    val telefono: String,

    @field:Size(max = 250)
    val direccionExacta: String? = null,

    @field:Size(max = 20)
    val disponibilidad: String? = null,

    @field:Size(max = 500)
    val fotoUrl: String? = null
)