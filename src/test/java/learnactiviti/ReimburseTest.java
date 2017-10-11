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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LearnActivitiApplication.class)
public class ReimburseTest {

	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void deployment() throws Exception {
		Deployment deploy = this.repositoryService.createDeployment()
		  .addClasspathResource("processes/reimburse.bpmn")
		  .deploy();
		
	}
	
	// Const 常量定义  每一个流程<都有一个对应的KEY>
	public static final String REIMBURSE_PROCESSDEFINITIONKEY = "reimburse";
	
	
	@Autowired
	private IdentityService identityService;		//提交流程的申请人 需要进行设置
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private TaskService taskService;
	
	
	/**
	 * 
	 * <B>方法名称：</B>强制删除正在运行的流程实例<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月11日 下午10:39:36
	 */
	@Test
	public void stopProcessInstance(){
		runtimeService.deleteProcessInstance("75001", "测试删除");
		
	}
	
	@Test
	@Transactional
	public void start() throws Exception {
		
		
		// currentUserId = 003 
		
		String currentUserId = "003";
				
		
		//启动流程： 99.99% 业务主键 businessKey
		String businessKey = "20171011-3";
		
		//添加额外的参数 供整个流程去使用(从流程开始到结束 整个时间范围内都可以获取)
		Map<String, Object> variables = new HashMap<String, Object>();
		
		//启动流程
		//设置流程的发起人:  //注意 在bpmn的 start节点里 要进行设置: activiti:initiator="applyuser"
		this.identityService.setAuthenticatedUserId(currentUserId);
		//启动流程
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(REIMBURSE_PROCESSDEFINITIONKEY, businessKey, variables);
		System.err.println("processInstanceId: " + processInstance.getId());
		Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
		this.taskService.complete(task.getId());
		 
		
		
	}
	
	/**
	 * <B>方法名称：</B>启动流程， 并且 自己执行完成第一步 申请任务 <BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月11日 下午10:49:23
	 * @throws Exception
	 */
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
	
	/**
	 * 
	 * <B>方法名称：</B>领导审批不通过 重新回退跟申请人<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月11日 下午10:49:07
	 * @throws Exception
	 */
	@Test
	public void completeTaskLevel_2() throws Exception {
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("002").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			//完成任务
			Map<String, Object> globalVariables = new HashMap<String, Object>();
			globalVariables.put("pass", false);
			this.taskService.complete(task.getId(), globalVariables);		//全局性
		}
	}
	
	//回退以后申请人 能看到自己的回退申请信息 [补充后重新提交申请 或者 放弃申请]
	@Test
	public void completeTaskLevel_3() throws Exception {
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("003").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			//少了一些业务的信息数据 进行补充... 
			this.taskService.complete(task.getId());		//全局性
		}
	}
	
	/**
	 * 
	 * <B>方法名称：</B>领导申请通过<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月11日 下午10:48:50
	 * @throws Exception
	 */
	@Test
	public void completeTaskLevel_4() throws Exception {
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("002").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			//完成任务
			Map<String, Object> globalVariables = new HashMap<String, Object>();
			globalVariables.put("pass", true);
			this.taskService.complete(task.getId(), globalVariables);		//全局性
		}
	}
}
