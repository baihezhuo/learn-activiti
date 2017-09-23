package learnactiviti;

import org.activiti.engine.RepositoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LearnActivitiApplication.class)
public class LearnActivitiApplicationTests {

	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void contextLoads() {
		System.err.println(repositoryService);
	}

}
