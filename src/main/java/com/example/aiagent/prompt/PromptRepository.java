package com.example.aiagent.prompt;

import com.example.aiagent.prompt.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptRepository extends JpaRepository<PromptTemplate, String> {
    Optional<PromptTemplate> findByNameAndActiveTrue(String name);
    Optional<PromptTemplate> findByNameAndVersion(String name, String version);
}