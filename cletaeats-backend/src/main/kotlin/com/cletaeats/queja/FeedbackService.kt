package com.cletaeats.queja

import com.cletaeats.amonestacion.AmonestacionRepartidorEntity
import com.cletaeats.amonestacion.AmonestacionRepartidorRepository
import com.cletaeats.amonestacion.AmonestacionRequest
import com.cletaeats.amonestacion.AmonestacionResponse
import com.cletaeats.calificacion.CalificacionRepartidorEntity
import com.cletaeats.calificacion.CalificacionRepartidorRepository
import com.cletaeats.calificacion.CalificacionRequest
import com.cletaeats.calificacion.CalificacionResponse
import com.cletaeats.cliente.ClienteRepository
import com.cletaeats.pedido.PedidoEntity
import com.cletaeats.pedido.PedidoRepository
import com.cletaeats.usuario.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class FeedbackService(
    private val pedidoRepository: PedidoRepository,
    private val clienteRepository: ClienteRepository,
    private val usuarioRepository: UsuarioRepository,
    private val calificacionRepository: CalificacionRepartidorRepository,
    private val quejaRepository: QuejaRepartidorRepository,
    private val amonestacionRepository: AmonestacionRepartidorRepository
) {

    @Transactional
    fun registrarCalificacion(
        correoCliente: String,
        pedidoId: Long,
        request: CalificacionRequest
    ): CalificacionResponse {
        val cliente = clienteRepository.findByUsuario_Correo(correoCliente)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")

        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")
        }

        validarPedidoDelClienteEntregado(cliente.clienteId!!, pedido)

        if (calificacionRepository.existsByPedido_PedidoId(pedidoId)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ese pedido ya tiene una calificación registrada"
            )
        }

        val repartidor = pedido.repartidor
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido sin repartidor asociado")

        val calificacion = CalificacionRepartidorEntity(
            pedido = pedido,
            repartidor = repartidor,
            cliente = cliente,
            puntajeAmabilidad = request.puntajeAmabilidad,
            puntajeTiempo = request.puntajeTiempo,
            puntajePresentacion = request.puntajePresentacion,
            comentario = request.comentario?.trim()
        )

        return calificacionRepository.save(calificacion).toResponse()
    }

    @Transactional
    fun registrarQueja(
        correoCliente: String,
        pedidoId: Long,
        request: QuejaRequest
    ): QuejaResponse {
        val cliente = clienteRepository.findByUsuario_Correo(correoCliente)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")

        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")
        }

        validarPedidoDelClienteEntregado(cliente.clienteId!!, pedido)

        val repartidor = pedido.repartidor
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido sin repartidor asociado")

        val queja = QuejaRepartidorEntity(
            pedido = pedido,
            repartidor = repartidor,
            cliente = cliente,
            categoria = request.categoria.trim().uppercase(),
            descripcion = request.descripcion.trim(),
            estado = "PENDIENTE"
        )

        return quejaRepository.save(queja).toResponse()
    }

    fun listarQuejasAdmin(): List<QuejaResponse> {
        return quejaRepository.findAllByOrderByFechaRegistroDesc().map { it.toResponse() }
    }

    @Transactional
    fun crearAmonestacion(
        correoAdmin: String,
        quejaId: Long,
        request: AmonestacionRequest
    ): AmonestacionResponse {
        val admin = usuarioRepository.findByCorreo(correoAdmin)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado")

        val queja = quejaRepository.findById(quejaId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Queja no encontrada")
        }

        if (amonestacionRepository.existsByQueja_QuejaId(quejaId)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Esa queja ya tiene una amonestación registrada"
            )
        }

        val repartidor = queja.repartidor
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Queja sin repartidor asociado")

        val amonestacion = AmonestacionRepartidorEntity(
            repartidor = repartidor,
            queja = queja,
            adminUsuario = admin,
            motivo = request.motivo.trim(),
            activa = "S"
        )

        queja.estado = "RESUELTA"

        amonestacionRepository.save(amonestacion)
        quejaRepository.save(queja)

        return amonestacion.toResponse()
    }

    private fun validarPedidoDelClienteEntregado(clienteId: Long, pedido: PedidoEntity) {
        if (pedido.cliente?.clienteId != clienteId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "El pedido no pertenece al cliente autenticado")
        }

        if (pedido.estado != "ENTREGADO") {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Solo se puede calificar o registrar queja sobre pedidos entregados"
            )
        }
    }

    private fun CalificacionRepartidorEntity.toResponse(): CalificacionResponse {
        return CalificacionResponse(
            calificacionId = this.calificacionId,
            pedidoId = this.pedido?.pedidoId,
            repartidorId = this.repartidor?.repartidorId,
            clienteId = this.cliente?.clienteId,
            puntajeAmabilidad = this.puntajeAmabilidad,
            puntajeTiempo = this.puntajeTiempo,
            puntajePresentacion = this.puntajePresentacion,
            comentario = this.comentario,
            fechaRegistro = this.fechaRegistro
        )
    }

    private fun QuejaRepartidorEntity.toResponse(): QuejaResponse {
        return QuejaResponse(
            quejaId = this.quejaId,
            pedidoId = this.pedido?.pedidoId,
            repartidorId = this.repartidor?.repartidorId,
            repartidorNombre = this.repartidor?.usuario?.nombreCompleto,
            clienteId = this.cliente?.clienteId,
            clienteNombre = this.cliente?.usuario?.nombreCompleto,
            categoria = this.categoria,
            descripcion = this.descripcion,
            estado = this.estado,
            fechaRegistro = this.fechaRegistro
        )
    }

    private fun AmonestacionRepartidorEntity.toResponse(): AmonestacionResponse {
        return AmonestacionResponse(
            amonestacionId = this.amonestacionId,
            repartidorId = this.repartidor?.repartidorId,
            quejaId = this.queja?.quejaId,
            adminUsuarioId = this.adminUsuario?.usuarioId,
            motivo = this.motivo,
            activa = this.activa,
            fechaAmonestacion = this.fechaAmonestacion
        )
    }
}