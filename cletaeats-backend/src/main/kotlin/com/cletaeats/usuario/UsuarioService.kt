package com.cletaeats.usuario

import com.cletaeats.auth.MeResponse
import com.cletaeats.repartidor.RepartidorRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val repartidorRepository: RepartidorRepository
) {

    fun listarUsuarios(): List<UsuarioResponse> {
        return usuarioRepository.findAll().map { usuario ->
            val rolCodigo = usuario.rol?.codigo ?: ""

            val fotoUrl = if (rolCodigo == "REPARTIDOR" && usuario.usuarioId != null) {
                repartidorRepository.findByUsuario_UsuarioId(usuario.usuarioId!!)?.fotoUrl
            } else {
                null
            }

            UsuarioResponse(
                id = usuario.usuarioId,
                nombre = usuario.nombreCompleto,
                correo = usuario.correo,
                cedula = usuario.cedula,
                telefono = usuario.telefonoCelular,
                estado = usuario.estado,
                rol = rolCodigo,
                fotoUrl = fotoUrl
            )
        }
    }

    fun obtenerPorCorreo(correo: String): UsuarioEntity? {
        return usuarioRepository.findByCorreo(correo)
    }

    fun obtenerMe(correo: String): MeResponse? {
        val usuario = usuarioRepository.findByCorreo(correo) ?: return null

        return MeResponse(
            usuarioId = usuario.usuarioId,
            nombre = usuario.nombreCompleto,
            correo = usuario.correo,
            rol = usuario.rol?.codigo ?: "",
            estado = usuario.estado
        )
    }
}