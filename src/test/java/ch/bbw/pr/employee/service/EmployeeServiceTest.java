package ch.bbw.pr.employee.service;

import ch.bbw.pr.employee.model.Department;
import ch.bbw.pr.employee.model.Employee;
import ch.bbw.pr.employee.repository.DepartmentRepository;
import ch.bbw.pr.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmployeeServiceTest
 * Unit tests für EmployeeService mit gemockten Repositories
 * Tests mit Hilfe von KI generiert und manuell überarbeitet
 * @author Jannis Milz
 * @version 02.10.2025
 */
// ChatGPT: Fix, damit @Mock / @InjectMocks Annotationen funktionieren
@ExtendWith(MockitoExtension.class) 
class EmployeeServiceTest {

   @Mock 
   private EmployeeRepository employeeRepo;

   @Mock 
   private DepartmentRepository departmentRepo;

   @InjectMocks 
   private EmployeeService employeeService;

   private Employee testEmployee1;
   private Employee testEmployee2;
   private Department testDepartment;

   @BeforeEach
   void setUp() {
      testDepartment = Department.builder()
              .id(1)
              .description("IT")
              .build();

      testEmployee1 = Employee.builder()
              .id(1)
              .firstname("Max")
              .lastname("Mustermann")
              .department(testDepartment)
              .build();

      testEmployee2 = Employee.builder()
              .id(2)
              .firstname("Anna")
              .lastname("Schmidt")
              .department(testDepartment)
              .build();
   }

   @Test
   void findAllEmployees_shouldReturnAllEmployees() {
      List<Employee> employees = Arrays.asList(testEmployee1, testEmployee2);
      when(employeeRepo.findAll()).thenReturn(employees);

      Iterable<Employee> result = employeeService.findAllEmployees();

      assertNotNull(result);
      List<Employee> resultList = (List<Employee>) result;
      assertEquals(2, resultList.size());
      assertEquals("Max", resultList.get(0).getFirstname());
      assertEquals("Mustermann", resultList.get(0).getLastname());
      assertEquals("IT", resultList.get(0).getDepartment().getDescription());

      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void addEmployee_shouldSaveAndReturnAllEmployees() {
      List<Employee> employees = Arrays.asList(testEmployee1);
      when(employeeRepo.save(any(Employee.class))).thenReturn(testEmployee1);
      when(employeeRepo.findAll()).thenReturn(employees);

      Iterable<Employee> result = employeeService.addEmployee("Max", "Mustermann");

      assertNotNull(result);
      verify(employeeRepo, times(1)).save(any(Employee.class));
      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void addEmployeeToFirstDepartment_shouldAssignDepartmentAndSave() {
      List<Department> departments = Arrays.asList(testDepartment);
      when(departmentRepo.findAll()).thenReturn(departments);
      when(employeeRepo.save(any(Employee.class))).thenReturn(testEmployee1);
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(testEmployee1));
      Iterable<Employee> result = employeeService.addEmployeeToFirstDepartment("Max", "Mustermann");

      assertNotNull(result);
      verify(departmentRepo, times(1)).findAll();
      verify(employeeRepo, times(1)).save(any(Employee.class));
   }

   @Test
   void getEmployeeById_whenExists_shouldReturnEmployee() {
      when(employeeRepo.findById(1)).thenReturn(Optional.of(testEmployee1));

      Optional<Employee> result = employeeService.getEmployeeById(1);

      assertTrue(result.isPresent());
      assertEquals(1, result.get().getId());
      assertEquals("Max", result.get().getFirstname());
      assertEquals("Mustermann", result.get().getLastname());
      assertNotNull(result.get().getDepartment());
      assertEquals("IT", result.get().getDepartment().getDescription());
      verify(employeeRepo, times(1)).findById(1);
   }

   @Test
   void getEmployeeById_whenNotExists_shouldReturnEmpty() {
      when(employeeRepo.findById(999)).thenReturn(Optional.empty());

      Optional<Employee> result = employeeService.getEmployeeById(999);

      assertFalse(result.isPresent());
      verify(employeeRepo, times(1)).findById(999);
   }

   @Test
   void getEmployeeByName_whenExists_shouldReturnEmployee() {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(testEmployee1));

      Optional<Employee> result = employeeService.getEmployeeByName("Mustermann");

      assertTrue(result.isPresent());
      assertEquals("Mustermann", result.get().getLastname());
      assertEquals("Max", result.get().getFirstname());
      assertEquals("IT", result.get().getDepartment().getDescription());
      verify(employeeRepo, times(1)).findByLastname("Mustermann");
   }

   @Test
   void getEmployeeByName_whenNotExists_shouldReturnEmpty() {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());

      Optional<Employee> result = employeeService.getEmployeeByName("Unknown");

      assertFalse(result.isPresent());
      verify(employeeRepo, times(1)).findByLastname("Unknown");
   }

   @Test
   void updateEmployeeLastname_whenExists_shouldUpdateAndReturnAll() {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(testEmployee1));
      when(employeeRepo.save(any(Employee.class))).thenReturn(testEmployee1);
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(testEmployee1));
      Iterable<Employee> result = employeeService.updateEmployeeLastname("Mustermann", "Neumann");

      assertNotNull(result);
      verify(employeeRepo, times(1)).findByLastname("Mustermann");
      verify(employeeRepo, times(1)).save(any(Employee.class));
      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void updateEmployeeLastname_whenNotExists_shouldNotUpdate() {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(testEmployee1));
      Iterable<Employee> result = employeeService.updateEmployeeLastname("Unknown", "Neumann");

      assertNotNull(result);
      verify(employeeRepo, times(1)).findByLastname("Unknown");
      verify(employeeRepo, never()).save(any(Employee.class));
      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void deleteEmployeeByLastname_whenExists_shouldDeleteAndReturnRemaining() {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(testEmployee1));
      doNothing().when(employeeRepo).delete(testEmployee1);
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(testEmployee2));
      Iterable<Employee> result = employeeService.deleteEmployeeByLastname("Mustermann");

      assertNotNull(result);
      verify(employeeRepo, times(1)).findByLastname("Mustermann");
      verify(employeeRepo, times(1)).delete(testEmployee1);
      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void deleteEmployeeByLastname_whenNotExists_shouldNotDelete() {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(testEmployee1, testEmployee2));
      Iterable<Employee> result = employeeService.deleteEmployeeByLastname("Unknown");

      assertNotNull(result);
      verify(employeeRepo, times(1)).findByLastname("Unknown");
      verify(employeeRepo, never()).delete(any(Employee.class));
      verify(employeeRepo, times(1)).findAll();
   }

   @Test
   void saveEmployee_shouldSaveAndReturnEmployee() {
      when(employeeRepo.save(testEmployee1)).thenReturn(testEmployee1);
      Employee result = employeeService.saveEmployee(testEmployee1);

      assertNotNull(result);
      assertEquals(1, result.getId());
      assertEquals("Max", result.getFirstname());
      assertEquals("Mustermann", result.getLastname());
      assertEquals("IT", result.getDepartment().getDescription());
      verify(employeeRepo, times(1)).save(testEmployee1);
   }

   @Test
   void findEmployeesByDepartment_shouldReturnEmployeesInDepartment() {
      List<Employee> employees = Arrays.asList(testEmployee1, testEmployee2);
      when(employeeRepo.findByDepartment(testDepartment)).thenReturn(employees);
      List<Employee> result = employeeService.findEmployeesByDepartment(testDepartment);
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("IT", result.get(0).getDepartment().getDescription());
      assertEquals("IT", result.get(1).getDepartment().getDescription());
      verify(employeeRepo, times(1)).findByDepartment(testDepartment);
   }
}
