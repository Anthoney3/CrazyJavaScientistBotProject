package com.crazy.scientist.crazyjavascientist.security.repos;

import com.crazy.scientist.crazyjavascientist.security.entities.CJSConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CJSConfigRepo extends JpaRepository<CJSConfigEntity, Long> {}
