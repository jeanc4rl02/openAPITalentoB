package com.openAPITalentoB.openAPITalentoB.repositories;

import com.openAPITalentoB.openAPITalentoB.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTextContentContaining(String keyword);
}
