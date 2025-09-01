package com.gogo.mcp_java_server.repository;

import com.gogo.mcp_java_server.model.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship,Long> {
}
