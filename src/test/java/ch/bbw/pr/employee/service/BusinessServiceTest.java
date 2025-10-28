package ch.bbw.pr.employee.service;

import ch.bbw.pr.employee.model.Department;
import ch.bbw.pr.employee.model.Employee;
import ch.bbw.pr.employee.repository.DepartmentRepository;
import ch.bbw.pr.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BusinessServiceTest - Komponententests
 * EmployeeService wird NICHT gemockt (echter Service mit gemockten Repositories)
 * Tests mit Hilfe von KI generiert und manuell überarbeitet
 * @author Jannis Milz
 * @version 02.10.2025
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

   @Mock
   private DepartmentRepository departmentRepo;

   @Mock
   private EmployeeRepository employeeRepo;

   private EmployeeService employeeService;
   private BusinessService businessService;

   private Department itDept, hrDept;
   private Employee emp1, emp2;

   @BeforeEach
   void setUp() {
      itDept = Department.builder().id(1).description("IT").build();
      hrDept = Department.builder().id(2).description("HR").build();
      emp1 = Employee.builder().id(1).firstname("Max").lastname("Mustermann").department(itDept).build();
      emp2 = Employee.builder().id(2).firstname("Anna").lastname("Schmidt").department(itDept).build();

      employeeService = new EmployeeService(employeeRepo, departmentRepo);
      businessService = new BusinessService(employeeService, departmentRepo);
   }

   @Test
   void createEmployeeWithDepartment_whenDepartmentExists_shouldCreateEmployee() {
      when(departmentRepo.findByDescription("IT")).thenReturn(Optional.of(itDept));
      when(employeeRepo.save(any(Employee.class))).thenReturn(emp1);
      Employee result = businessService.createEmployeeWithDepartment("Max", "Mustermann", "IT");

      assertNotNull(result);
      assertEquals("Max", result.getFirstname());
      assertEquals("IT", result.getDepartment().getDescription());
      verify(departmentRepo).findByDescription("IT");
      verify(employeeRepo).save(any(Employee.class));
   }

   @Test
   void createEmployeeWithDepartment_whenDepartmentNotExists_shouldThrowException() {
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());
      assertThrows(NoSuchElementException.class, () ->
          businessService.createEmployeeWithDepartment("Max", "Mustermann", "Unknown")
      );
      verify(employeeRepo, never()).save(any());
   }

   @Test
   void findEmployeesByDepartmentDescription_whenDepartmentExists_shouldReturnEmployees() {
      when(departmentRepo.findByDescription("IT")).thenReturn(Optional.of(itDept));
      when(employeeRepo.findByDepartment(itDept)).thenReturn(Arrays.asList(emp1, emp2));
      List<Employee> result = businessService.findEmployeesByDepartmentDescription("IT");

      assertEquals(2, result.size());
      assertTrue(result.stream().allMatch(e -> "IT".equals(e.getDepartment().getDescription())));
   }

   @Test
   void findEmployeesByDepartmentDescription_whenDepartmentNotExists_shouldReturnEmptyList() {
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());
      List<Employee> result = businessService.findEmployeesByDepartmentDescription("Unknown");

      assertTrue(result.isEmpty());
      verify(employeeRepo, never()).findByDepartment(any());
   }

   @Test
   void transferEmployeeToDepartment_whenBothExist_shouldTransferSuccessfully() {
      when(employeeRepo.findById(1)).thenReturn(Optional.of(emp1));
      when(departmentRepo.findByDescription("HR")).thenReturn(Optional.of(hrDept));
      when(employeeRepo.save(any(Employee.class))).thenReturn(emp1);
      boolean result = businessService.transferEmployeeToDepartment(1, "HR");

      assertTrue(result);
      verify(employeeRepo).save(any(Employee.class));
   }

   @Test
   void transferEmployeeToDepartment_whenEmployeeNotExists_shouldReturnFalse() {
      when(employeeRepo.findById(999)).thenReturn(Optional.empty());
      when(departmentRepo.findByDescription("HR")).thenReturn(Optional.of(hrDept));

      boolean result = businessService.transferEmployeeToDepartment(999, "HR");

      assertFalse(result);
      verify(employeeRepo, never()).save(any());
   }

   @Test
   void transferEmployeeToDepartment_whenDepartmentNotExists_shouldReturnFalse() {
      when(employeeRepo.findById(1)).thenReturn(Optional.of(emp1));
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());

      boolean result = businessService.transferEmployeeToDepartment(1, "Unknown");

      assertFalse(result);
      verify(employeeRepo, never()).save(any());
   }

   @Test
   void transferEmployeeToDepartment_whenNeitherExists_shouldReturnFalse() {
      when(employeeRepo.findById(999)).thenReturn(Optional.empty());
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());

      boolean result = businessService.transferEmployeeToDepartment(999, "Unknown");

      assertFalse(result);
      verify(employeeRepo, never()).save(any());
   }
}
