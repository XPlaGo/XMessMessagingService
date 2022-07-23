package com.xplago.xmessmessagingservice.repository;

import com.xplago.xmessmessagingservice.model.user.XMessUser;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface XMessUserRepository extends MongoRepository<XMessUser, String> {

    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

    Optional<XMessUser> findByEmail(String email);
    Optional<XMessUser> findByUsername(String username);

    List<XMessUser> findXMessUsersByUsernameContainsIgnoreCase(String part);

    @Override
    <S extends XMessUser> List<S> findAll(Example<S> example);

    @Transactional
    void deleteByEmail(String email);
    @Transactional
    void deleteByUsername(String username);
}
