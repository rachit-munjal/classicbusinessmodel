package com.project.cmb.repo;

import com.project.cmb.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "employee")
public interface EmployeeRepo extends JpaRepository<Employee, Integer> {
}
