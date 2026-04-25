package com.cletaeats.usuario

import com.cletaeats.auth.MeResponse
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {

    fun listarUsuarios(): List<UsuarioResponse> {
        return usuarioRepository.findAll().map {
            UsuarioResponse(
                id = it.usuarioId,
                nombre = it.nombreCompleto,
                correo = it.correo,
                cedula = it.cedula,
                telefono = it.telefonoCelular,
                estado = it.estado,
                rol = it.rol?.codigo ?: ""
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