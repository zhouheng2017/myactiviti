package activiti.web.chapter9;

import activiti.chapter6.util.UserUtil;
import net.sf.ehcache.util.PropertyUtil;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhouheng
 * @Created: with IntelliJ IDEA.
 * @Description:
 * @Date: 2018-04-27
 * @Time: 16:18
 */
@Controller
//@RequestMapping("/chapter9/comment")
public class CommentControllerMy {

    @Autowired
    TaskService taskService;

    @Autowired
    IdentityService identityService;

    @Autowired
    HistoryService historyService;

    /**
     * 保存评论
     *
     * @return
     */
    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseBody
    public Boolean saveComment(HttpSession session, @RequestParam("taskId") String taskId, @RequestParam("message") String message, @RequestParam(value = "processInstance", required = false) String processInstance) {
//        保存用户信息
        identityService.setAuthenticatedUserId(UserUtil.getUserFromSession(session).getId());
        //添加评论
        taskService.addComment(taskId, processInstance, message);

        return true;
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String, Object> getComment(@RequestParam("taskId") String taskId, @RequestParam("processInstanceId")String processInstanceId) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> commentAndEventsMap = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(processInstanceId)) {

            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            for (Comment comment :
                    processInstanceComments) {

                String commentId = (String) PropertyUtils.getProperty(comment, "id");
                commentAndEventsMap.put(commentId, comment);
            }
            
            //提取任务名称
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
            Map<String, Object> taskNames = new HashMap<String, Object>();

            for (HistoricTaskInstance historicTaskInstance : list
                    ) {
                taskNames.put(historicTaskInstance.getId(), historicTaskInstance.getName());

            }
            result.put("taskNames", taskNames);
        }

        //添加时间
        if (StringUtils.isNotBlank(taskId)) {
            //获取任务时间
            List<Event> taskEvents = taskService.getTaskEvents(taskId);
            //遍历任务时间
            for (Event event :
                    taskEvents) {
                String eventIds = (String) PropertyUtils.getProperty(event, "id");
                
                commentAndEventsMap.put(eventIds, event);
            }
        }


        result.put("events", commentAndEventsMap.values());
        return result;
    }
}