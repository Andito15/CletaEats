package com.cletaeats.combo

import com.cletaeats.restaurante.RestauranteEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "COMBO")
class ComboEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMBO_ID")
    var comboId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANTE_ID", nullable = false)
    var restaurante: RestauranteEntity? = null,

    @Column(name = "NUMERO_COMBO", nullable = false)
    var numeroCombo: Int = 0,

    @Column(name = "NOMBRE", nullable = false, length = 120)
    var nombre: String = "",

    @Column(name = "DESCRIPCION", nullable = false, length = 300)
    var descripcion: String = "",

    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    var precio: BigDecimal = BigDecimal.ZERO,

    @Column(name = "ESTADO", nullable = false, length = 20)
    var estado: String = "",

    @Column(name = "IMAGEN_URL", length = 500)
    var imagenUrl: String? = null
)