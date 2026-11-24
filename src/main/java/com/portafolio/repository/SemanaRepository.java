package com.portafolio.repository;

import com.portafolio.model.Semana;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SemanaRepository extends JpaRepository<Semana, Integer> {
    List<Semana> findAllByOrderByNumeroAsc();
}
