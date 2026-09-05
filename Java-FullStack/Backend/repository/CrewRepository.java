package com.jetlease.repository;

import com.jetlease.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, String> {
}
