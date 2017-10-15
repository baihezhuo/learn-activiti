package learnactiviti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LearnActivitiApplication.class)
public class IncludeTests {

	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void deployment() throws Exception {
		//部署:
		Deployment deploy = this.repositoryService.createDeployment()
		  .addClasspathResource("processes/include.bpmn")
		  .deploy();
		
	}
	
	// Const 常量定义  每一个流程<都有一个对应的KEY>
	public static final String INCLUDE_PROCESSDEFINITIONKEY = "include";
	
	
	@Autowired
	private IdentityService identityService;		//提交流程的申请人 需要进行设置
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private ManagementService managementService;
	
	@Test
	public void start() throws Exception {
		String businessKey = "20171015-3";
		//添加额外的参数 供整个流程去使用(从流程开始到结束 整个时间范围内都可以获取)
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("a", "1");
		variables.put("b", "2");
		//启动流程
		//设置流程的发起人:  //注意 在bpmn的 start节点里 要进行设置: activiti:initiator="applyuser"
		this.identityService.setAuthenticatedUserId("003");
		//启动流程
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(INCLUDE_PROCESSDEFINITIONKEY, businessKey, variables);
		
		System.err.println("processInstanceId: " + processInstance.getId());
	}
	
	@Autowired
	private TaskService taskService;
	
	/**
	 * 
	 * <B>方法名称：</B>根据组 找到对应的 人员 判断具体是否可用 然后进行设置其任务的处理人 （也就是进行绑定）<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月15日 下午4:32:42
	 * @throws Exception
	 */
	@Test
	public void exec() throws Exception {
		
		Task task = taskService.createTaskQuery().taskAssignee("001").singleResult();
		this.taskService.complete(task.getId());
		
	}
	
	
	
	
	
	
}
