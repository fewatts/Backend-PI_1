package com.api.reserva.condominio.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.reserva.condominio.domain.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // Aqui já tem: save(), findAll(), findById(), deleteById(), etc.
}
