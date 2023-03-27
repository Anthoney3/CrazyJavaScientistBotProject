package com.crazy.scientist.crazyjavascientist.security.repos;

import com.crazy.scientist.crazyjavascientist.security.entities.AESAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AES_Auth extends JpaRepository<AESAuth,Long> {
}
