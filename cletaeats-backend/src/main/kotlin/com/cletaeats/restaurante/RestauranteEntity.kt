package com.cletaeats.restaurante

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "RESTAURANTE")
class RestauranteEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANTE_ID")
    var restauranteId: Long? = null,

    @Column(name = "NOMBRE", nullable = false, length = 120)
    var nombre: String = "",

    @Column(name = "CEDULA_JURIDICA", nullable = false, unique = true, length = 30)
    var cedulaJuridica: String = "",

    @Column(name = "DIRECCION", nullable = false, length = 250)
    var direccion: String = "",

    @Column(name = "TIPO_COMIDA", nullable = false, length = 50)
    var tipoComida: String = "",

    @Column(name = "ESTADO", nullable = false, length = 20)
    var estado: String = "",

    @Column(name = "IMAGEN_URL", length = 500)
    var imagenUrl: String? = null,

    @Column(name = "LATITUD")
    var latitud: Double? = null,

    @Column(name = "LONGITUD")
    var longitud: Double? = null,

    @Column(name = "UBICACION_ACTUALIZADA_EN")
    var ubicacionActualizadaEn: LocalDateTime? = null,


    )