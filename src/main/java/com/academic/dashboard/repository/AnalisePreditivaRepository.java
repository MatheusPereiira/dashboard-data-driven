package com.academic.dashboard.repository;

import com.academic.dashboard.model.AnalisePreditiva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalisePreditivaRepository extends MongoRepository<AnalisePreditiva, String> {

    Optional<AnalisePreditiva> findByAlunoId(String alunoId);

    void deleteByAlunoId(String alunoId);
}

