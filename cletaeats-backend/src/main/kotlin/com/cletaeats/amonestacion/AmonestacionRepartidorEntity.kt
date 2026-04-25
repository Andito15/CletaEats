package com.cletaeats.amonestacion

import com.cletaeats.queja.QuejaRepartidorEntity
import com.cletaeats.repartidor.RepartidorEntity
import com.cletaeats.usuario.UsuarioEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "AMONESTACION_REPARTIDOR")
class  AmonestacionRepartidorEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AMONESTACION_ID")
    var amonestacionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPARTIDOR_ID", nullable = false)
    var repartidor: RepartidorEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUEJA_ID")
    var queja: QuejaRepartidorEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADMIN_USUARIO_ID", nullable = false)
    var adminUsuario: UsuarioEntity? = null,

    @Column(name = "MOTIVO", nullable = false, length = 300)
    var motivo: String = "",

    @Column(name = "ACTIVA", nullable = false, length = 1)
    var activa: String = "S",

    @Column(name = "FECHA_AMONESTACION", nullable = false)
    var fechaAmonestacion: LocalDateTime = LocalDateTime.now()
)