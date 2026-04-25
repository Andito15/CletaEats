package com.cletaeats.feriado

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface FeriadoRepository : JpaRepository<FeriadoEntity, Long> {
    fun existsByFecha(fecha: LocalDate): Boolean
}