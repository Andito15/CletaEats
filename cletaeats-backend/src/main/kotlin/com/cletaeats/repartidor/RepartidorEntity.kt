package com.cletaeats.repartidor

import com.cletaeats.usuario.UsuarioEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "REPARTIDOR")
class RepartidorEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPARTIDOR_ID")
    var repartidorId: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USUARIO_ID", nullable = false, unique = true)
    var usuario: UsuarioEntity? = null,

    @Column(name = "DIRECCION_EXACTA", nullable = false, length = 250)
    var direccionExacta: String = "",

    @Column(name = "TARJETA_ULTIMOS4", length = 4)
    var tarjetaUltimos4: String? = null,

    @Column(name = "TOKEN_PAGO", length = 255)
    var tokenPago: String? = null,

    @Column(name = "DISPONIBILIDAD", nullable = false, length = 20)
    var disponibilidad: String = "",

    @Column(name = "KILOMETROS_RECORRIDOS_DIA", precision = 10, scale = 2)
    var kilometrosRecorridosDia: BigDecimal = BigDecimal.ZERO,

    @Column(name = "COSTO_KM_HABIL", nullable = false, precision = 10, scale = 2)
    var costoKmHabil: BigDecimal = BigDecimal.ZERO,

    @Column(name = "COSTO_KM_FERIADO", nullable = false, precision = 10, scale = 2)
    var costoKmFeriado: BigDecimal = BigDecimal.ZERO,

    @Column(name = "FOTO_URL", length = 500)
    var fotoUrl: String? = null
)