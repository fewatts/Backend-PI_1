package com.api.reserva.condominio.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter @Setter 
@NoArgsConstructor
@AllArgsConstructor 
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Espaco espaco; 

    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
}