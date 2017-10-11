package learnactiviti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LearnActivitiApplication.class)
public class ExclusiveTests {

	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void deployment() throws Exception {
		//部署:
		/**
		 * 调用部署方法:  会把指定路径的bpmn 文件 存储到activiti 工作流引擎的数据库表中:
		 * ACT_GE_BYTEARRAY		他存储的是二进制的数据文件（也就是bpmn.xml）
		 * ACT_RE_DEPLOYMENT	他存储的是部署的结果信息(比如 部署编号..)
		 * ACT_RE_PROCDEF		他存储的是流程定义信息
		 * 
		 * ACT_RE_PROCDEF表:   processdefinition<流程定义>
		 * ID_  流程定义ID     
		 * KEY_ 流程定义KEY
		 * DEPLOYMENT_ID_ 部署ID <关联ACT_RE_DEPLOYMENT表>
		 * SUSPENSION_STATE_ 流程定义是否为挂起状态   1为可用状态  2为挂起状态
		 */
		Deployment deploy = this.repositoryService.createDeployment()
		  .addClasspathResource("processes/exclusive.bpmn")
		  .deploy();
		
	}
	
	// Const 常量定义  每一个流程<都有一个对应的KEY>
	public static final String EXCLUSIVE_PROCESSDEFINITIONKEY = "exclusive";
	
	
	@Autowired
	private IdentityService identityService;		//提交流程的申请人 需要进行设置
	@Autowired
	private RuntimeService runtimeService;
	
	
	
	@Test
	public void start() throws Exception {
		//启动流程： 99.99% 业务主键 businessKey
		String businessKey = "20171011-1";
		
		//添加额外的参数 供整个流程去使用(从流程开始到结束 整个时间范围内都可以获取)
		Map<String, Object> variables = new HashMap<String, Object>();
		
		//设置变量 ${user1} 
		variables.put("user1", "004");
		
		//启动流程
		//设置流程的发起人:  //注意 在bpmn的 start节点里 要进行设置: activiti:initiator="applyuser"
		this.identityService.setAuthenticatedUserId("003");
		//启动流程
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(EXCLUSIVE_PROCESSDEFINITIONKEY, businessKey, variables);
		
		System.err.println("processInstanceId: " + processInstance.getId());
	}
	
	@Autowired
	private TaskService taskService;
	
	@Test
	public void completeTaskLevel_1() throws Exception {
		
		//添加预处理人
		
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("004").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			
			// 设置局部变量 与当前任务绑定
			Map<String, Object> localVariables = new HashMap<String, Object>();
			localVariables.put("info", "前置任务通过啦!! 我是审批信息!");
			this.taskService.setVariablesLocal(task.getId(), localVariables);
			
			//设置全局变量。整个流程内可见
			Map<String, Object> globalVariables = new HashMap<String, Object>();
			globalVariables.put("pass", "3");
			
			//完成任务
			this.taskService.complete(task.getId(), globalVariables);		//全局性
		}
	}
	
	
	@Test
	public void completeTaskLevel_2() throws Exception {
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("002").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			//完成任务
			this.taskService.complete(task.getId());		//全局性
		}
	}
	
	
	
	
	
	
	
	
	
}
