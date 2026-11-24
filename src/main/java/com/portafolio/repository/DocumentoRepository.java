package com.portafolio.repository;

import com.portafolio.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findAllByOrderByIdDesc();
    List<Documento> findBySemanaAndBorradorFalseOrderByIdDesc(Integer semana);
    List<Documento> findByBorradorFalseOrderByIdDesc();
}
