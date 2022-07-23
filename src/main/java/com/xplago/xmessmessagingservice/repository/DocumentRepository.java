package com.xplago.xmessmessagingservice.repository;

import com.xplago.xmessmessagingservice.model.document.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
}
