package com.applications.rabbitmq.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Employee {
    @JsonProperty("employee_id")
    private String employeeId;
    @JsonProperty("employee_name")
    private String name;
    @JsonProperty("joining_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }
    public Employee(String employeeId, String name, LocalDate joiningDate) {
        this.employeeId = employeeId;
        this.name = name;
        this.joiningDate = joiningDate;
    }

    @Override
    public String toString() {
        return "Employee{employeeId='" + employeeId + ", name='" + name + ", joiningDate=" + joiningDate +'}';
    }
}
