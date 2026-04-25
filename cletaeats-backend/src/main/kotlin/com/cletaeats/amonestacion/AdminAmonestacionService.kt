package com.cletaeats.amonestacion

import org.springframework.stereotype.Service

@Service
class AdminAmonestacionService(
    private val amonestacionRepository: AmonestacionRepartidorRepository
) {

    fun listarPorQueja(quejaId: Long): List<AmonestacionAdminResponse> {
        return amonestacionRepository.findByQueja_QuejaId(quejaId)
            .sortedByDescending { it.fechaAmonestacion }
            .map { it.toResponse() }
    }

    fun listarPorRepartidor(repartidorId: Long): List<AmonestacionAdminResponse> {
        return amonestacionRepository.findByRepartidor_RepartidorId(repartidorId)
            .sortedByDescending { it.fechaAmonestacion }
            .map { it.toResponse() }
    }

    private fun AmonestacionRepartidorEntity.toResponse(): AmonestacionAdminResponse {
        return AmonestacionAdminResponse(
            amonestacionId = this.amonestacionId,
            quejaId = this.queja?.quejaId,
            repartidorId = this.repartidor?.repartidorId,
            repartidorNombre = this.repartidor?.usuario?.nombreCompleto,
            adminUsuarioId = this.adminUsuario?.usuarioId,
            adminNombre = this.adminUsuario?.nombreCompleto,
            motivo = this.motivo,
            activa = this.activa,
            fechaAmonestacion = this.fechaAmonestacion
        )
    }
}