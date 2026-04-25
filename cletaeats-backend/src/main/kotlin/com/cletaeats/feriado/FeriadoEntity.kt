package com.cletaeats.feriado

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "FERIADO")
class FeriadoEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FERIADO_ID")
    var feriadoId: Long? = null,

    @Column(name = "FECHA", nullable = false, unique = true)
    var fecha: LocalDate? = null,

    @Column(name = "DESCRIPCION", nullable = false, length = 150)
    var descripcion: String = ""
)