package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataUserRepository;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * UserRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataUserRepository 를 호출해 실제 DB 저장·조회 기능을 위임
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByStudentNumber(String studentNumber) {
        return springDataUserRepository.existsByStudentNumber(studentNumber);
    }

    @Override
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public List<User> findByLab(Lab lab) {
        return springDataUserRepository.findByLab(lab);
    }

    @Override
    public Optional<User> findByLabAndStudentNumber(Lab lab, String studentNumber) {
        return springDataUserRepository.findByLabAndStudentNumber(lab, studentNumber);
    }
}