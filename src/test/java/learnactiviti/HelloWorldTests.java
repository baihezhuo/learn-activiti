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
public class HelloWorldTests {

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
		  .addClasspathResource("processes/helloworld.bpmn")
		  .deploy();
		
	}
	
	// Const 常量定义  每一个流程<都有一个对应的KEY>
	public static final String HELLO_WORLD_PROCESSDEFINITIONKEY = "helloworld";
	
	
	
	@Test
	public void queryDeploymentAndProcessdefinition(){
		
		/**
		 * Deployment
		 */
		//Deployment 部署信息   ---> ACT_RE_DEPLOYMENT
		//Processdefinition 流程定义 ---> ACT_RE_PROCDEF
		//List<Deployment> list = this.repositoryService.createDeploymentQuery().deploymentId("1").list();
		
//		List<Deployment> list = this.repositoryService.createDeploymentQuery().deploymentNameLike("helloworld%").list();
//		for(Deployment d : list) {
//			System.err.println(d);
//		}
		
		//删除
		this.repositoryService.deleteDeployment("2501");
		
		/**
		 * Processdefinition
		 */
		
		/**
		ProcessDefinition processDefinition = this.repositoryService.createProcessDefinitionQuery()
		//.processDefinitionId(processDefinitionId)		//直接根据processDefinitionId去查询流程定义比较费劲
		// 非常方便的通过流程定义KEY 去查询当前的流程定义 <需要制定为最后的版本>
		.processDefinitionKey(HELLO_WORLD_PROCESSDEFINITIONKEY).latestVersion().singleResult();
		
		
		System.err.println(processDefinition);
		
		//挂起:  不允许新的流程实例创建提交
		//this.repositoryService.suspendProcessDefinitionByKey(HELLO_WORLD_PROCESSDEFINITIONKEY);
		//激活
		//this.repositoryService.activateProcessDefinitionByKey(HELLO_WORLD_PROCESSDEFINITIONKEY);
		 */
	}
	
	@Autowired
	private IdentityService identityService;
	@Autowired
	private RuntimeService runtimeService;
	
	
	@Test
	public void start() throws Exception {
		
		//
		//启动流程： 99.99% 业务主键 businessKey
		String businessKey = "20170923";
		
		//添加额外的参数 供整个流程去使用(从流程开始到结束 整个时间范围内都可以获取)
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("param1", "abc");
		variables.put("param2", "efg");
		
		//启动流程
		//设置流程的发起人:  //注意 在bpmn的 start节点里 要进行设置: activiti:initiator="applyuser"
		this.identityService.setAuthenticatedUserId("003");
		//启动流程
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey(HELLO_WORLD_PROCESSDEFINITIONKEY, businessKey, variables);
		
		System.err.println("processInstanceId: " + processInstance.getId());
		
		/**
		 * 当我们启动了一个流程实例的时候:
		 * ACT_RU_EXECUTION: 会有一条对应的流程实例数据信息:
		 * 
		 * ID_ :   EXECUTION_ID 
		 * PROC_INST_ID_ : 流程实例的ID
		 * BUSINESS_KEY_ : 业务主键 （一般来说 ，我们的一个业务流程, 对应一个流程实例, 也对应一个businessKey）
		 * ACT_ID_:  体现了当前流程的执行位置 
		 * 
		 * ACT_RU_IDENTITYLINK: 他维护了整个流程与用户与任务的关联关系
		 * 
		 * ACT_RU_TASK: 存储的就是当前的任务实例
		 * 
		 * ACT_RU_VARIABLE: 存储当前流程实例所需要的参数信息
		 * 
		 */
	}
	
	
	//任务这个事情 在工作流引擎里面是属于非常非常非常核心的概念和操作
	
	
	@Autowired
	private TaskService taskService;
	
	@Test
	public void completeTaskLevel_1() throws Exception {
		
		//添加预处理人
		
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("001").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			
			
			//添加一些业务操作: 存储数据.....对自己的业务表做一些持久化操作
			
			//把你的业务完成了以后,最后要推进工作流继续向下执行 [同意/不同意]
			
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("info", "又同意了");
			
			//绑定跟当前这个任务相关的参数信息: 
			this.taskService.setVariablesLocal(task.getId(), variables);
			
			//完成任务
			this.taskService.complete(task.getId());		//全局性
		}
	}
	
	
	//在工作流里面没有执行成功和执行失败  只有任务有没有被完成
	@Test
	public void completeTaskLevel_2() throws Exception {
		
		//添加预处理人
		
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("002").orderByTaskCreateTime().desc().listPage(0, 10);
		
		for(Task task : taskList) {
			System.err.println(task);
			
			//完成任务
			this.taskService.complete(task.getId());		//全局性
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
