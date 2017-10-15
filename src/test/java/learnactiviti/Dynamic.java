/**
 * Copyright 2017 JINZAY All Rights Reserved.
 */
package learnactiviti;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * <B>系统名称：</B><BR>
 * <B>模块名称：</B><BR>
 * <B>中文类名：</B>Dynamic<BR>
 * <B>概要说明：</B><BR>
 * @author baihezhuo
 * @since 2017年3月3日 上午11:16:01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LearnActivitiApplication.class)
public class Dynamic {

	@Autowired
	private  RepositoryService repositoryService;
	
	@Test
	public void test(){
		
		//1 创建一个空的BpmnModel和Process对象
		BpmnModel model = new BpmnModel();
		Process process = new Process();
		
		model.addProcess(process);
		process.setId("matter");		//流程定义的KEY
		
		//PS: 创建 Flow元素 <所有的事件 任务都被认为是Flow>
		
		///////////创建节点///////////
		//1. start
		// flow: start - matterTask
		process.addFlowElement(BpmnCreaterUtil.createStartEvent());
		
		//2. matterTask
		// flow: matterTask - gateway1
		process.addFlowElement(BpmnCreaterUtil.createUserTask("matterTask","事项审批","xiao5", null));
		
		//3. createExclusiveGateway
		// flow: matterTask - gateway1
		process.addFlowElement(BpmnCreaterUtil.createExclusiveGateway("gateway1", "gateway1"));
		
		//4. createEndEvent 1
		// flow: gateway1 - end1
		process.addFlowElement(BpmnCreaterUtil.createEndEvent("end1"));
		
		//5. createEndEvent 2
		// flow: gateway1 - end2
		process.addFlowElement(BpmnCreaterUtil.createEndEvent("end2"));
		
		//////////创建顺序流//////////
		process.addFlowElement(BpmnCreaterUtil.createSequenceFlow("sequenceFlow1", "sequenceFlow1", "start", "matterTask"));
		process.addFlowElement(BpmnCreaterUtil.createSequenceFlow("sequenceFlow2", "sequenceFlow2", "matterTask", "gateway1"));
		process.addFlowElement(BpmnCreaterUtil.createSequenceFlowWithExpression("sequenceFlow3", "通过",   "gateway1", "end1", "${matterpass}"));
		process.addFlowElement(BpmnCreaterUtil.createSequenceFlowWithExpression("sequenceFlow4", "不通过", "gateway1", "end2", "${!matterpass}"));
		
		// 自动布局:
		new BpmnAutoLayout(model).execute();
		
		Deployment deployment = repositoryService
				.createDeployment()
				.addBpmnModel("matter.bpmn", model)
				.name("matter")
				.deploy();
		System.err.println(deployment.getId());
	}
	
	@Test
	public void test1() throws Exception {
		InputStream is = repositoryService.getResourceAsStream("132501", "matter.bpmn");
		FileUtils.copyInputStreamToFile(is, new File("D:/matter.bpmn"));
	}	


	
	/**
	//1.
	private FlowElement createStartEvent() {
		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");	
		startEvent.setInitiator("applyuser");
		return startEvent;
	}
	
	//2. create userTask
	private UserTask createUserTask(String id, String name, String assignee){
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setAssignee(assignee);
		return userTask;
	}
	
	//3. create SequenceFlow
	private SequenceFlow createSequenceFlow(String id, String name, String from, String to){
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.setId(id);
		sequenceFlow.setName(name);
		sequenceFlow.setSourceRef(from);
		sequenceFlow.setTargetRef(to);
		return sequenceFlow;
	}
	
	//4. create createSequenceFlowWithExpression
	private SequenceFlow createSequenceFlowWithExpression(String id, String name, String from, String to, String conditionExpression){
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.setId(id);
		sequenceFlow.setName(name);
		sequenceFlow.setSourceRef(from);
		sequenceFlow.setTargetRef(to);
		sequenceFlow.setConditionExpression(conditionExpression);
		return sequenceFlow;
	}
	
	//5. create GateWay
	private ExclusiveGateway createExclusiveGateway(String id, String name){
		ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
		exclusiveGateway.setId(id);
		exclusiveGateway.setName(name);
		return exclusiveGateway;
	}
	
	//3. 
	
	//-2.
	private FlowElement createEndEvent(String id) {
		EndEvent endEvent = new EndEvent();
		endEvent.setId(id);
		return endEvent;
	}
	*/
	
}









