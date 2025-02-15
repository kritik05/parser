package com.parser.Parser.Application.repository;

import com.parser.Parser.Application.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
}