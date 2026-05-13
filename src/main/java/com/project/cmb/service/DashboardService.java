package com.project.cmb.service;

import com.project.cmb.repo.EmployeeRepo;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final EmployeeRepo employeeRepo;

    DashboardService(EmployeeRepo employeeRepo){
        this.employeeRepo= employeeRepo;
    }

    public long getEmployeesCount(){
        return employeeRepo.count();
    }

}
