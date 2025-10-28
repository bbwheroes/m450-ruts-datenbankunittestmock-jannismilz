package ch.bbw.pr.employee.control;

import ch.bbw.pr.employee.model.Department;
import ch.bbw.pr.employee.model.Employee;
import ch.bbw.pr.employee.repository.DepartmentRepository;
import ch.bbw.pr.employee.repository.EmployeeRepository;
import ch.bbw.pr.employee.service.BusinessService;
import ch.bbw.pr.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ViewControllerTest - Integration Tests mit MockMvc
 * Repositories werden gemockt, Services sind echt (bessere Integration)
 * @Import wird benötigt, da @WebMvcTest nur Controller lädt, nicht Services
 * Tests mit Hilfe von KI generiert und manuell überarbeitet
 * @author Jannis Milz
 * @version 02.10.2025
 */
@WebMvcTest(ViewController.class)
@Import({EmployeeService.class, BusinessService.class})
class ViewControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private EmployeeRepository employeeRepo;

   @MockBean
   private DepartmentRepository departmentRepo;

   private Department itDept, hrDept;
   private Employee emp1, emp2;

   @BeforeEach
   void setUp() {
      itDept = Department.builder().id(1).description("IT").build();
      hrDept = Department.builder().id(2).description("HR").build();
      emp1 = Employee.builder().id(1).firstname("Max").lastname("Mustermann").department(itDept).build();
      emp2 = Employee.builder().id(2).firstname("Anna").lastname("Schmidt").department(itDept).build();
   }

   @Test
   void home_shouldRedirectToFindAll() throws Exception {
      mockMvc.perform(get("/"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/findAll"));
   }

   @Test
   void findAll_shouldReturnAllEmployees() throws Exception {
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(emp1, emp2));

      mockMvc.perform(get("/findAll"))
              .andExpect(status().isOk())
              .andExpect(view().name("index.html"))
              .andExpect(model().attribute("information", hasSize(2)))
              .andExpect(model().attribute("information", hasItem(
                      allOf(
                              hasProperty("firstname", is("Max")),
                              hasProperty("department", hasProperty("description", is("IT")))
                      )
              )));

      verify(employeeRepo).findAll();
   }

   @Test
   void readById_whenEmployeeExists_shouldReturnEmployee() throws Exception {
      when(employeeRepo.findById(1)).thenReturn(Optional.of(emp1));

      mockMvc.perform(get("/readById").param("id", "1"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasProperty("firstname", is("Max"))))
              .andExpect(model().attribute("information", hasProperty("department", hasProperty("description", is("IT")))));
   }

   @Test
   void readById_whenEmployeeNotExists_shouldReturnError() throws Exception {
      when(employeeRepo.findById(999)).thenReturn(Optional.empty());

      mockMvc.perform(get("/readById").param("id", "999"))
              .andExpect(model().attributeExists("error"))
              .andExpect(model().attribute("error", containsString("not found")));
   }

   @Test
   void readByName_whenEmployeeExists_shouldReturnEmployee() throws Exception {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(emp1));

      mockMvc.perform(get("/readByName").param("lastname", "Mustermann"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasProperty("lastname", is("Mustermann"))))
              .andExpect(model().attribute("information", hasProperty("department", hasProperty("description", is("IT")))));
   }

   @Test
   void readByName_whenEmployeeNotExists_shouldReturnError() throws Exception {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());

      mockMvc.perform(get("/readByName").param("lastname", "Unknown"))
              .andExpect(model().attributeExists("error"));
   }

   @Test
   void update_whenEmployeeExists_shouldUpdateAndReturnAll() throws Exception {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(emp1));
      when(employeeRepo.save(any(Employee.class))).thenReturn(emp1);
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(emp1, emp2));

      mockMvc.perform(get("/update")
                      .param("oldlastname", "Mustermann")
                      .param("newlastname", "Neumann"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasSize(2)));
   }

   @Test
   void update_whenEmployeeNotExists_shouldReturnError() throws Exception {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());
      when(employeeRepo.findAll()).thenReturn(Arrays.asList());

      mockMvc.perform(get("/update")
                      .param("oldlastname", "Unknown")
                      .param("newlastname", "Neumann"))
              .andExpect(model().attributeExists("error"));
   }

   @Test
   void delete_whenEmployeeExists_shouldDeleteAndReturnRemaining() throws Exception {
      when(employeeRepo.findByLastname("Mustermann")).thenReturn(Arrays.asList(emp1));
      doNothing().when(employeeRepo).delete(emp1);
      when(employeeRepo.findAll()).thenReturn(Arrays.asList(emp2));

      mockMvc.perform(get("/delete").param("lastname", "Mustermann"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasSize(1)));
   }

   @Test
   void delete_whenEmployeeNotExists_shouldReturnError() throws Exception {
      when(employeeRepo.findByLastname("Unknown")).thenReturn(Arrays.asList());
      when(employeeRepo.findAll()).thenReturn(Arrays.asList());

      mockMvc.perform(get("/delete").param("lastname", "Unknown"))
              .andExpect(model().attributeExists("error"));
   }

   @Test
   void createEmployeeWithDepartment_whenDepartmentExists_shouldCreateEmployee() throws Exception {
      when(departmentRepo.findByDescription("IT")).thenReturn(Optional.of(itDept));
      when(employeeRepo.save(any(Employee.class))).thenReturn(emp1);

      mockMvc.perform(get("/createEmployeeWithDepartment")
                      .param("firstname", "Max")
                      .param("lastname", "Mustermann")
                      .param("departmentName", "IT"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasProperty("department", hasProperty("description", is("IT")))));
   }

   @Test
   void createEmployeeWithDepartment_whenDepartmentNotExists_shouldReturnError() throws Exception {
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());

      mockMvc.perform(get("/createEmployeeWithDepartment")
                      .param("firstname", "Max")
                      .param("lastname", "Mustermann")
                      .param("departmentName", "Unknown"))
              .andExpect(model().attributeExists("error"));
   }

   @Test
   void findEmployeesByDepartment_whenDepartmentExists_shouldReturnEmployees() throws Exception {
      when(departmentRepo.findByDescription("IT")).thenReturn(Optional.of(itDept));
      when(employeeRepo.findByDepartment(itDept)).thenReturn(Arrays.asList(emp1));

      mockMvc.perform(get("/findEmployeesByDepartment")
                      .param("departmentDescription", "IT"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", hasSize(1)))
              .andExpect(model().attribute("information", hasItem(
                      hasProperty("department", hasProperty("description", is("IT")))
              )));
   }

   @Test
   void findEmployeesByDepartment_whenDepartmentNotExists_shouldReturnError() throws Exception {
      when(departmentRepo.findByDescription("Unknown")).thenReturn(Optional.empty());

      mockMvc.perform(get("/findEmployeesByDepartment")
                      .param("departmentDescription", "Unknown"))
              .andExpect(model().attributeExists("error"));
   }

   @Test
   void transferEmployee_whenBothExist_shouldTransferSuccessfully() throws Exception {
      when(employeeRepo.findById(1)).thenReturn(Optional.of(emp1));
      when(departmentRepo.findByDescription("HR")).thenReturn(Optional.of(hrDept));
      when(employeeRepo.save(any(Employee.class))).thenReturn(emp1);

      mockMvc.perform(get("/transferEmployee")
                      .param("employeeId", "1")
                      .param("newDepartmentDescription", "HR"))
              .andExpect(status().isOk())
              .andExpect(model().attribute("information", containsString("transferred successfully")));
   }

   @Test
   void transferEmployee_whenEmployeeNotExists_shouldReturnError() throws Exception {
      when(employeeRepo.findById(999)).thenReturn(Optional.empty());
      when(departmentRepo.findByDescription("HR")).thenReturn(Optional.of(hrDept));

      mockMvc.perform(get("/transferEmployee")
                      .param("employeeId", "999")
                      .param("newDepartmentDescription", "HR"))
              .andExpect(model().attributeExists("error"));
   }
}
