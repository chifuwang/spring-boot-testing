package com.example.springboot.controller;

import com.example.springboot.model.Employee;
import com.example.springboot.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.is;

@WebMvcTest(EmployeeController.class)
@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @MockitoBean
    private EmployeeService employeeService; // Mocked EmployeeService for testing purposes

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenEmployeeWithIdWhenSaveThenReturnBadRequestResponse() throws Exception {
        Employee employee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(employee)))
        .andExpect(status().isBadRequest())
        .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("Employee ID is not null"));
    }

    @Test
    public void givenEmployeeWithNullFirstNameWhenSaveThenReturnBadRequestResponse() throws Exception {

        Employee employee = Employee.builder()
                .firstName(null)
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    @Test
    public void givenEmployeeWithNullLastNameWhenSaveThenReturnBadRequestResponse() throws Exception {

        Employee employee = Employee.builder()
                .firstName("john")
                .lastName(null)
                .email("john.doe@example.com")
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    @Test
    public void givenEmployeeWithNullEmailWhenSaveThenReturnBadRequestResponse() throws Exception {

        Employee employee = Employee.builder()
                .firstName("john")
                .lastName("Doe")
                .email(null)
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    @Test
    public void givenEmployeeWithExistingEmailWhenSaveThenReturnBadRequestResponse() throws Exception {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.saveEmployee(any(Employee.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

       // Mocked saveEmployee method for testing purposes

        given(employeeService.findByEmail(employee.getEmail())).willReturn(Optional.of(employee));

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("Employee with the same email already exists"));
    }

    @Test
    public void givenEmployeeWithUniqueEmailWhenSaveThenReturnCreatedResponse() throws Exception {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.saveEmployee(any(Employee.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        // Mocked saveEmployee method for testing purposes

        ResultActions response = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk());

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())));

    }

    @Test
    public void givenEmployeeWhenSaveThenReturnException() throws Exception {

        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.saveEmployee(any(Employee.class)))
                .willThrow(new RuntimeException("Database error"));

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content, containsString("Error saving employee:"));


    }

    @Test
    public void givenAllEmployeesWhenGetAllThenReturnOkResponse() throws Exception {

        Employee employee1 = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(2)
                .firstName("Marry")
                .lastName("Doe")
                .email("marry.doe@example.com")
                .build();

        List<Employee> employees = List.of(employee1, employee2);

        given(employeeService.getAllEmployees())
               .willReturn(employees);

        mockMvc.perform(get("/api/employees"))
               .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.size()",
                        is(employees.size())));

    }

    @Test
    public void givenAllEmployeesWhenGetAllThenReturnInternalServerExceptionResponse() throws Exception {

        given(employeeService.getAllEmployees())
                .willThrow(new RuntimeException("Database error"));

        MvcResult result = mockMvc.perform(get("/api/employees"))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Error getting employees:"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenGetEmployeeById_thenReturnEmployeeObject() throws Exception {

        // given - precondition or setup
        long id = 1;

        Employee employee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.getEmployeeById(id)).willReturn(Optional.of(employee));

        // when - action or the behavior that we are going to test
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", id));


        // then - verify the output
        response.andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.id", is(employee.getId())))
               .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
               .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
               .andExpect(jsonPath("$.email", is(employee.getEmail())));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenGetEmployeeById_thenReturnNotFoundResponse() throws Exception {

        // given - precondition or setup
        long id = 1;

        given(employeeService.getEmployeeById(id)).willReturn(Optional.empty());

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(get("/api/employees/{id}", id))
               .andExpect(status().isNotFound())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee with ID 1 not found"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenGetEmployeeById_thenReturnInternalServerErrorResponse() throws Exception {

        // given - precondition or setup
        long id = 1;

        given(employeeService.getEmployeeById(id)).willThrow(new RuntimeException("Database error"));

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(get("/api/employees/{id}", id))
               .andExpect(status().isInternalServerError())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Error getting employee by ID"));

    }

    @Test
    public void givenEmployeeId_whenUpdateEmployeeIdWithBodyNullId_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
                .id(null)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee ID in request body should not be null"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenUpdateEmployeeIdNotMatch_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
               .id(2)
               .firstName("John")
               .lastName("Doe")
               .email("john.doe@example.com")
               .build();

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                       .contentType("application/json")
                       .content(objectMapper.writeValueAsString(updatedEmployee)))
               .andExpect(status().isBadRequest())
               .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee ID in request body must match the path variable"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenUpdateEmployeeWithNullFirstName_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
               .id(1)
               .firstName(null)
               .lastName("Doe")
               .email("john.doe@example.com")
               .build();

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                       .contentType("application/json")
                       .content(objectMapper.writeValueAsString(updatedEmployee)))
               .andExpect(status().isBadRequest())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    @Test
    public void givenEmployeeId_whenUpdateEmployeeWithNullLastName_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName(null)
                .email("john.doe@example.com")
                .build();

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    @Test
    public void givenEmployeeId_whenUpdateEmployeeWithNullEmail_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email(null)
                .build();

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("First Name, Last Name, and Email are required"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenUpdateEmployee_thenReturnNotFoundResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.getEmployeeById(id)).willReturn(Optional.empty());        // when - action or the behavior that we are going to test

        // then - verify the output
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee with ID 1 not found"));

    }

    @Test
    public void givenEmployeeId_whenUpdateEmployee_thenReturnUpdatedEmployeeObject() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
                .id(1)
                .firstName("Marry")
                .lastName("Sue")
                .email("marry.sue@example.com")
                .build();

        Employee existingEmployee  = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.new@example.com")
                .build();

        given(employeeService.getEmployeeById(id)).willReturn(Optional.of(existingEmployee));
        given(employeeService.updateEmployee(any(Employee.class)))
                .willAnswer(invocation -> invocation.getArgument(0));// when - action or the behavior that we are going to test

        // then - verify the output
        ResultActions response = mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedEmployee)));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is("Marry")))
                .andExpect(jsonPath("$.lastName", is("Sue")))
                .andExpect(jsonPath("$.email", is("marry.sue@example.com")));


    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenUpdateEmployeeGotException_thenReturnInternalServerErrorResponse() throws Exception {

        // given - precondition or setup
        long id = 1;
        Employee updatedEmployee = Employee.builder()
               .id(1)
               .firstName("Marry")
               .lastName("Sue")
               .email("marry.sue@example.com")
               .build();

        Employee existingEmployee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.getEmployeeById(id)).willReturn(Optional.of(existingEmployee));
        given(employeeService.updateEmployee(any(Employee.class)))
               .willThrow(new RuntimeException("Internal Server Error"));        // when - action or the behavior that we are going to test

        // then - verify the output
        MvcResult result = mockMvc.perform(put("/api/employees/{id}", id)
                       .contentType("application/json")
                       .content(objectMapper.writeValueAsString(updatedEmployee)))
               .andExpect(status().isInternalServerError())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Error updating employee:"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenDeleteEmployeeWithIdNotFound_thenReturnBadRequestResponse() throws Exception {

        // given - precondition or setup
        long id = 1;

        given(employeeService.getEmployeeById(id)).willReturn(Optional.empty());        // when - action or the behavior that we are going to test

        // then - verify the output
        MvcResult result = mockMvc.perform(delete("/api/employees/{id}", id))
               .andExpect(status().isBadRequest())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee with ID 1 not found"));

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenDeleteEmployee_thenReturnOkResponse() throws Exception {

        // given - precondition or setup
        long id = 1;

        Employee existingEmployee = Employee.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        given(employeeService.getEmployeeById(id)).willReturn(Optional.of(existingEmployee));

        willDoNothing().given(employeeService).deleteEmployee(id);

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(delete("/api/employees/{id}", id))
               .andExpect(status().isOk())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Employee with ID 1 deleted successfully"));

        // then - verify the output
        verify(employeeService, times(1)).deleteEmployee(id);

    }

    // JUnit test for
    @Test
    public void givenEmployeeId_whenDeleteEmployeeGetException_thenReturnInternalServerErrorResponse() throws Exception {

        // given - precondition or setup
        long id = 1;

        given(employeeService.getEmployeeById(id)).willThrow(new RuntimeException("Internal Server Error"));

        // when - action or the behavior that we are going to test
        MvcResult result = mockMvc.perform(delete("/api/employees/{id}", id))
               .andExpect(status().isInternalServerError())
               .andDo(print())
               .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Error deleting employee:"));

    }


}