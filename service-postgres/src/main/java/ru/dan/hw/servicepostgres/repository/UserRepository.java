package ru.dan.hw.servicepostgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dan.hw.servicepostgres.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
