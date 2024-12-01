package com.example.springboot.controller;

import com.example.springboot.dto.GenericListResult;
import com.example.springboot.model.Employee;
import com.example.springboot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> saveEmployee(@RequestBody Employee employee) {

        if (employee.getId() != null) {
            return ResponseEntity.badRequest().body("Employee ID is not null");
        }

        if (employee.getFirstName() == null || employee.getLastName() == null || employee.getEmail() == null) {
            return ResponseEntity.badRequest().body("First Name, Last Name, and Email are required");
        }

        Optional<Employee> existingEmployeeOptional = employeeService.findByEmail(employee.getEmail());
        if (existingEmployeeOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Employee with the same email already exists");
        }

        Employee savedEmployee;
        try {
            savedEmployee = employeeService.saveEmployee(employee);
            return ResponseEntity.ok(savedEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving employee: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees() {

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            GenericListResult<Employee> result = new GenericListResult<>();
            Employee[] employeeArray = new Employee[employees.size()];
            result.setData(employees.toArray(employeeArray));
            result.setTotal((long) employees.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting employees: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable("id") long id) {
        try {
            Optional<Employee> employeeOptional = employeeService.getEmployeeById(id);
            if (employeeOptional.isPresent()) {
                return ResponseEntity.ok(employeeOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + id + " not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting employee by ID: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable("id") long id, @RequestBody Employee employee) {

        if (employee.getId() == null) {
            return ResponseEntity.badRequest().body("Employee ID in request body should not be null");
        }

        if (employee.getId()!= id) {
            return ResponseEntity.badRequest().body("Employee ID in request body must match the path variable");
        }

        if (employee.getFirstName() == null || employee.getLastName() == null
                || employee.getEmail() == null) {
            return ResponseEntity.badRequest().body("First Name, Last Name, and Email are required");
        }

        Optional<Employee> employeeOptional = employeeService.getEmployeeById(id);
        if (employeeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + id + " not found");
        }

        Employee employeeToUpdate = employeeOptional.get();

        employeeToUpdate.setFirstName(employee.getFirstName());
        employeeToUpdate.setLastName(employee.getLastName());
        employeeToUpdate.setEmail(employee.getEmail());

        try {
            Employee employeeUpdated = employeeService.updateEmployee(employeeToUpdate);
            return ResponseEntity.ok(employeeUpdated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating employee: " + e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable("id") long id) {
        try {
            Optional<Employee> employeeToDelete = employeeService.getEmployeeById(id);

            if (employeeToDelete.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee with ID " + id + " not found");
            }

            employeeService.deleteEmployee(id);

            return ResponseEntity.status(HttpStatus.OK).body("Employee with ID " + id + " deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting employee: " + e.getMessage());
        }
    }
}
