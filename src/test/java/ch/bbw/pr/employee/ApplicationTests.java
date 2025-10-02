package ch.bbw.pr.employee;

import ch.bbw.pr.employee.repository.DepartmentRepository;
import ch.bbw.pr.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
// ChatGPT: Schaltet automatische DB Konfiguration aus, weil DB gar nicht läuft
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class ApplicationTests {

   @MockBean
   private EmployeeRepository employeeRepository;

   @MockBean
   private DepartmentRepository departmentRepository;

   @Test
   void contextLoads() {
   }

}
