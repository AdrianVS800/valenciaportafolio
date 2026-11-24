package com.portafolio.repository;

import com.portafolio.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    List<Proyecto> findAllByOrderByIdDesc();
}
