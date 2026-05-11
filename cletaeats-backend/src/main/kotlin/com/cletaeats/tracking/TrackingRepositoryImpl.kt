package com.cletaeats.tracking

import com.cletaeats.tracking.dto.UbicacionRepartidorRequest
import com.cletaeats.tracking.dto.UbicacionRepartidorResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.CallableStatement
import java.sql.Types

@Repository
class TrackingRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : TrackingRepository {

    override fun actualizarUbicacionRepartidor(
        authName: String,
        request: UbicacionRepartidorRequest
    ): UbicacionRepartidorResponse {
        return jdbcTemplate.execute(
            """
            { call CLETAEATS.PKG_TRACKING_ENTREGA.ACTUALIZAR_UBICACION_REPARTIDOR(
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?
            ) }
            """.trimIndent()
        ) { cs: CallableStatement ->
            cs.setString(1, authName)
            cs.setLong(2, request.pedidoId ?: 0L)
            cs.setDouble(3, request.latitud)
            cs.setDouble(4, request.longitud)

            if (request.precisionMetros == null) {
                cs.setNull(5, Types.NUMERIC)
            } else {
                cs.setDouble(5, request.precisionMetros)
            }

            registerTrackingOutParams(
                cs = cs,
                startIndex = 6
            )

            cs.execute()

            readTrackingOutParams(
                cs = cs,
                startIndex = 6
            )
        }!!
    }

    override fun obtenerTrackingCliente(
        authName: String,
        pedidoId: Long
    ): UbicacionRepartidorResponse {
        return jdbcTemplate.execute(
            """
            { call CLETAEATS.PKG_TRACKING_ENTREGA.OBTENER_TRACKING_CLIENTE(
                ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?
            ) }
            """.trimIndent()
        ) { cs: CallableStatement ->
            cs.setString(1, authName)
            cs.setLong(2, pedidoId)

            registerTrackingOutParams(
                cs = cs,
                startIndex = 3
            )

            cs.execute()

            readTrackingOutParams(
                cs = cs,
                startIndex = 3
            )
        }!!
    }

    private fun registerTrackingOutParams(
        cs: CallableStatement,
        startIndex: Int
    ) {
        cs.registerOutParameter(startIndex, Types.NUMERIC)      // O_PEDIDO_ID
        cs.registerOutParameter(startIndex + 1, Types.NUMERIC)  // O_REPARTIDOR_ID
        cs.registerOutParameter(startIndex + 2, Types.VARCHAR)  // O_REPARTIDOR_NOMBRE
        cs.registerOutParameter(startIndex + 3, Types.VARCHAR)  // O_ESTADO_PEDIDO
        cs.registerOutParameter(startIndex + 4, Types.NUMERIC)  // O_LATITUD
        cs.registerOutParameter(startIndex + 5, Types.NUMERIC)  // O_LONGITUD
        cs.registerOutParameter(startIndex + 6, Types.NUMERIC)  // O_PRECISION_METROS
        cs.registerOutParameter(startIndex + 7, Types.TIMESTAMP) // O_ULTIMA_UBICACION_EN
    }

    private fun readTrackingOutParams(
        cs: CallableStatement,
        startIndex: Int
    ): UbicacionRepartidorResponse {
        return UbicacionRepartidorResponse(
            pedidoId = cs.getLongOrNull(startIndex),
            repartidorId = cs.getLongOrNull(startIndex + 1),
            repartidorNombre = cs.getString(startIndex + 2),
            estadoPedido = cs.getString(startIndex + 3),
            latitud = cs.getDoubleOrNull(startIndex + 4),
            longitud = cs.getDoubleOrNull(startIndex + 5),
            precisionMetros = cs.getDoubleOrNull(startIndex + 6),
            ultimaUbicacionEn = cs.getTimestamp(startIndex + 7)?.toLocalDateTime()
        )
    }

    private fun CallableStatement.getLongOrNull(index: Int): Long? {
        val value = getLong(index)
        return if (wasNull()) null else value
    }

    private fun CallableStatement.getDoubleOrNull(index: Int): Double? {
        val value = getDouble(index)
        return if (wasNull()) null else value
    }
}