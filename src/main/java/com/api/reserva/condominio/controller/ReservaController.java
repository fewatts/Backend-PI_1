package com.api.reserva.condominio.controller;

import com.api.reserva.condominio.domain.model.Reserva;
import com.api.reserva.condominio.domain.repository.ReservaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository repository;

    // CREATE - Criar uma nova reserva
    @PostMapping
    public ResponseEntity<Reserva> criar(@RequestBody Reserva reserva) {
        Reserva novaReserva = repository.save(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
    }

    // READ - Listar todas as reservas
    @GetMapping
    public List<Reserva> listarTodos() {
        return repository.findAll();
    }

    // READ - Buscar reserva por ID
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(record -> ResponseEntity.ok().body(record))
                .orElse(ResponseEntity.notFound().build());
    }

    // READ - Buscar reservas por nome do morador (ou parte do nome)
    @GetMapping("/buscar")
    public ResponseEntity<List<Reserva>> buscarPorMorador(@RequestParam String nome) {
        List<Reserva> reservas = repository.findByNomeMoradorContainingIgnoreCase(nome);
        return ResponseEntity.ok(reservas);
    }

    // UPDATE - Atualizar uma reserva existente
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> atualizar(@PathVariable Long id, @RequestBody Reserva reserva) {
        return repository.findById(id)
                .map(record -> {
                    record.setNomeMorador(reserva.getNomeMorador());
                    record.setTelefoneMorador(reserva.getTelefoneMorador());
                    record.setApartamento(reserva.getApartamento());
                    record.setEspaco(reserva.getEspaco());
                    record.setDataHoraInicio(reserva.getDataHoraInicio());
                    record.setDataHoraFim(reserva.getDataHoraFim());
                    Reserva atualizada = repository.save(record);
                    return ResponseEntity.ok().body(atualizada);
                }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE - Remover uma reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return repository.findById(id)
                .map(record -> {
                    repository.deleteById(id);
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
