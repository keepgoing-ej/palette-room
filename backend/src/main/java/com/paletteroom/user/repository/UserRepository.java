package com.paletteroom.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
