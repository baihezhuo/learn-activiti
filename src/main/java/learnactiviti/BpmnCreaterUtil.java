package learnactiviti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;
/**
 * <B>系统名称：</B><BR>
 * <B>模块名称：</B><BR>
 * <B>中文类名：</B><BR>
 * <B>概要说明：</B><BR>
 * @author bhz
 * @since 2017年12月13日 上午11:17:11
 */
public class BpmnCreaterUtil {
	 
	/**
	 * <B>方法名称：</B>createStartEvent<BR>
	 * <B>概要说明：</B>创建开始节点<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:06:50
	 * @return FlowElement
	 */
	public static FlowElement createStartEvent() {
		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");	
		startEvent.setInitiator("applyuser");
		return startEvent;
	}
	
	/**
	 * <B>方法名称：</B>createUserTask<BR>
	 * <B>概要说明：</B>创建用户任务<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:06:00
	 * @param id taskDefinitionID 任务定义ID
	 * @param name 任务名称
	 * @param assignee 执行人ID
	 * @return UserTask
	 */
	public static UserTask createUserTask(String id, String name, String assignee, List<String> groups){
		UserTask userTask = new UserTask();
		userTask.setId(id);
		userTask.setName(name);
		if(!StringUtils.isBlank(assignee)){
			userTask.setAssignee(assignee);
		}
		
		if(null != groups && groups.size() > 0){
			userTask.setCandidateGroups(groups);
		}		
		
		return userTask;
	}
	
	/**
	 * <B>方法名称：</B>addExtensionTaskListener<BR>
	 * <B>概要说明：</B>为用户任务添加监听事件<BR>
	 * @author baihezhuo
	 * @since 2017年5月27日 下午1:39:48
	 * @param userTask
	 * @param eventType
	 * @param className
	 * @return
	 */
	public static void addTaskListener(UserTask userTask, String eventType, String className){
		
		List<ActivitiListener> executionListeners = userTask.getExecutionListeners();
		ActivitiListener act = new ActivitiListener();
		act.setEvent(eventType);
		act.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		act.setImplementation(className);	
		
		if(null != executionListeners && executionListeners.size() >0) {
			executionListeners.add(act);
		} else {
			executionListeners = new ArrayList<ActivitiListener>();
			executionListeners.add(act);
		}
		userTask.setTaskListeners(executionListeners);
		
	}
	
	/**
	 * <B>方法名称：</B>createServiceTask<BR>
	 * <B>概要说明：</B>创建服务任务<BR>
	 * @author baihezhuo
	 * @since 2017年3月13日 下午5:05:16
	 * @param id 任务定义ID
	 * @param name 任务名称
	 * @param className 调用类
	 * @return ServiceTask
	 */
	public static ServiceTask createServiceTask(String id, String name, String className) {
		ServiceTask serviceTask = new ServiceTask();
		serviceTask.setId(id);
		serviceTask.setName(name);
		serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		serviceTask.setImplementation(className);
		return serviceTask;
	}
	
	/**
	 * <B>方法名称：</B>createSequenceFlow<BR>
	 * <B>概要说明：</B>创建顺序流<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:05:37
	 * @param id 顺序流ID 
	 * @param name 顺序流名称
	 * @param from 起始节点
	 * @param to 目标节点
	 * @return SequenceFlow
	 */
	public static SequenceFlow createSequenceFlow(String id, String name, String from, String to){
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.setId(id);
		sequenceFlow.setName(name);
		sequenceFlow.setSourceRef(from);
		sequenceFlow.setTargetRef(to);
		return sequenceFlow;
	}
	
	/**
	 * <B>方法名称：</B>createSequenceFlowWithExpression<BR>
	 * <B>概要说明：</B>创建顺序流 + 顺序流 condition参数<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:04:36
	 * @param id 顺序流ID 
	 * @param name 顺序流名称
	 * @param from 起始节点
	 * @param to 目标节点
	 * @param conditionExpression
	 * @return SequenceFlow
	 */
	public static SequenceFlow createSequenceFlowWithExpression(String id, String name, String from, String to, String conditionExpression){
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.setId(id);
		sequenceFlow.setName(name);
		sequenceFlow.setSourceRef(from);
		sequenceFlow.setTargetRef(to);
		sequenceFlow.setConditionExpression(conditionExpression);
		return sequenceFlow;
	}
	
	/**
	 * <B>方法名称：</B>createExclusiveGateway<BR>
	 * <B>概要说明：</B>创建分支网关<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:03:55
	 * @param id 网关ID
	 * @param name 网关名称
	 * @return FlowElement --> ExclusiveGateway
	 */
	public static ExclusiveGateway createExclusiveGateway(String id, String name){
		ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
		exclusiveGateway.setId(id);
		exclusiveGateway.setName(name);
		return exclusiveGateway;
	}
	
	/**
	 * <B>方法名称：</B>createEndEvent<BR>
	 * <B>概要说明：</B>创建结束节点<BR>
	 * @author baihezhuo
	 * @since 2017年3月3日 下午7:03:35
	 * @param id 结束节点ID
	 * @return FlowElement
	 */
	public static FlowElement createEndEvent(String id) {
		EndEvent endEvent = new EndEvent();
		endEvent.setId(id);
		return endEvent;
	}
	
	
	
	
}
