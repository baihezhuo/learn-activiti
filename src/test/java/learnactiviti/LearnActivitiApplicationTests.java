package learnactiviti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.UserEntity;
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
	
	@Autowired
	private IdentityService identityService;
	
	@Test
	public void contextLoads() {
		System.err.println(repositoryService);
	}
	
	@Test
	public void addUser() {
		User user = new UserEntity();
		user.setId("002");
		user.setFirstName("四");
		user.setLastName("李");
		user.setEmail("174754613@qq.com");
		user.setPassword("002");
		this.identityService.saveUser(user);
	}

}
