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
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
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
public class ParallelTests {

	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void deployment() throws Exception {
		//部署:
		Deployment deploy = this.repositoryService.createDeployment()
		  .addClasspathResource("processes/parallel.bpmn")
		  .deploy();
		
	}
	
	// Const 常量定义  每一个流程<都有一个对应的KEY>
	public static final String PARALLEL_PROCESSDEFINITIONKEY = "parallel";
	
	
	@Autowired
	private IdentityService identityService;		//提交流程的申请人 需要进行设置
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private ManagementService managementService;
	
	@Test
	public void start() throws Exception {
		String businessKey = "20171015-1";
		//添加额外的参数 供整个流程去使用(从流程开始到结束 整个时间范围内都可以获取)
		Map<String, Object> variables = new HashMap<String, Object>();
		//设置变量 ${userList} 
		List<String> userList = new ArrayList<String>(2);
		userList.add("001");
		userList.add("004");
		variables.put("userList", userList);
		//启动流程
		//设置流程的发起人:  //注意 在bpmn的 start节点里 要进行设置: activiti:initiator="applyuser"
		this.identityService.setAuthenticatedUserId("003");
		//启动流程
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(PARALLEL_PROCESSDEFINITIONKEY, businessKey, variables);
		
		System.err.println("processInstanceId: " + processInstance.getId());
	}
	
	@Autowired
	private TaskService taskService;
	
	
	/**
	 * <B>方法名称：</B>candidateTaskLevel_1  --->> 任务1 或者任务2 <BR>
	 * <B>概要说明：</B>签收环节<BR>
	 * @author bhz
	 * @since 2017年10月15日 下午2:23:01
	 * @throws Exception
	 */
	@Test
	public void candidateTaskLevel_1() throws Exception {
		
		//第一组: 001 002    
		//第二组: 001 004
//		List<Task> taskList = this.taskService.createTaskQuery().taskCandidateUser("001").orderByTaskCreateTime().desc().listPage(0, 10);
//		for(Task task : taskList) {
//			System.err.println(task);
//		}	
		//签收的方法   ---> 001 任务2签收了
//		this.taskService.claim("92517", "001");
		
		//签收 002 签收任务1
//		this.taskService.claim("92511", "002");
		
		//92511
		
		//1 强制设置 处理人  (只要在任务没有被执行之前 都可以进行此操作)
//		this.taskService.setAssignee("92511", "004");
		
		
		//2 
		List<Task> taskList = this.taskService.createTaskQuery().taskAssignee("004").orderByTaskCreateTime().desc().listPage(0, 10);
		for(Task task : taskList) {
			System.err.println(task.getOwner());		//92511
			System.err.println(task.getAssignee());
			
			//委派操作: 委派操作执行后 task表里面 owner 任务的所属人 为 004 但是处理人会变成委派人  也就是005
			this.taskService.delegateTask(task.getId(), "005");

		}	
		
	}
	
	
	/**
	 * <B>方法名称：</B>delegateTask_complete<BR>
	 * <B>概要说明：</B>执行委派的任务<BR>
	 * @author bhz
	 * @since 2017年10月15日 下午3:03:05
	 * @throws Exception
	 */
	@Test
	public void delegateTask_complete() throws Exception {
		
//		Task task = taskService.createTaskQuery().taskAssignee("005").taskDelegationState(DelegationState.PENDING).singleResult();
//		
//		System.err.println(task);
//		
//		// 被委派人完成任务
//		this.taskService.resolveTask(task.getId());
				
		// 查询已完成的委派任务 ----> 任务是归我所有的 , 也就是之前的任务是004 所有的  委派给了005 去做  当005 完成任务以后 那么当前委派任务已经完成
		
		//
		Task task1 = taskService.createTaskQuery().taskOwner("004").taskDelegationState(DelegationState.RESOLVED).singleResult();
		System.err.println(task1);
		taskService.complete(task1.getId());
		
//		List<Task> taskList = this.taskService.createTaskQuery().taskAssignee("004").orderByTaskCreateTime().desc().listPage(0, 10);
//		for(Task task : taskList) {
//			System.err.println(task.getOwner());		//92511
//			System.err.println(task.getAssignee());
//		}
		//taskService.complete(task1.getId());
	}
	
	
	@Test
	public void completeTaskLevel_2() throws Exception {
		
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("001").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			//完成任务
			this.taskService.complete(task.getId());		//全局性
		}
	}
	
	/**
	 * 
	 * <B>方法名称：</B>根据组 找到对应的 人员 判断具体是否可用 然后进行设置其任务的处理人 （也就是进行绑定）<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月15日 下午4:32:42
	 * @throws Exception
	 */
	@Test
	public void candidateTaskLevel_3() throws Exception {
		
		//004 005
		List<Task> taskList = taskService.createTaskQuery().taskCandidateGroup("1").orderByTaskCreateTime().desc().listPage(0, 10);
		
		//004 005
		List<User> userList = this.identityService.createUserQuery().memberOfGroup("1").list();
		
		
		Task task = taskList.get(0);
		
		for(User user: userList){
			System.err.println(user.getId());
			String value = this.identityService.getUserInfo(user.getId(), "active");
			System.err.println(value);
			// value = 0 表示 已经被禁用了 1 表示可用
			if("1".equals(value)){	//可用
				this.taskService.setAssignee(task.getId(), user.getId());
			}
		}
		
	}
	/**
	 * <B>方法名称：</B>完成任务<BR>
	 * <B>概要说明：</B><BR>
	 * @author bhz
	 * @since 2017年10月15日 下午4:32:34
	 * @throws Exception
	 */
	@Test
	public void completeTaskLevel_4() throws Exception {
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("005").orderByTaskCreateTime().desc().listPage(0, 10);
		this.taskService.complete(taskList.get(0).getId());
		
	}
	
	
	
	
	
	
}
