package acceptance.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import pro.taskana.common.api.KeyDomain;
import pro.taskana.common.internal.security.JaasExtension;
import pro.taskana.common.internal.security.WithAccessId;
import pro.taskana.task.api.TaskService;
import pro.taskana.task.api.models.TaskSummary;
import pro.taskana.workbasket.api.exceptions.NotAuthorizedToQueryWorkbasketException;

/** Acceptance test for all "query tasks by workbasket" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryTasksByWorkbasketAccTest extends AbstractAccTest {

  QueryTasksByWorkbasketAccTest() {
    super();
  }

  @WithAccessId(user = "teamlead_1", groups = "group_1")
  @Test
  void testQueryForWorkbasketKeyDomain() {
    TaskService taskService = taskanaEngine.getTaskService();
    List<KeyDomain> workbasketIdentifiers =
        Arrays.asList(new KeyDomain("GPK_KSC", "DOMAIN_A"), new KeyDomain("USER_1_2", "DOMAIN_A"));

    List<TaskSummary> results =
        taskService
            .createTaskQuery()
            .workbasketKeyDomainIn(workbasketIdentifiers.toArray(new KeyDomain[0]))
            .list();
    assertThat(results).hasSize(52);

    String[] ids =
        results.stream()
            .map(t -> t.getWorkbasketSummary().getId())
            .collect(Collectors.toList())
            .toArray(new String[0]);

    List<TaskSummary> result2 = taskService.createTaskQuery().workbasketIdIn(ids).list();
    assertThat(result2).hasSize(52);
  }

  @WithAccessId(user = "user_1_1", groups = "group_1")
  @Test
  void testThrowsExceptionIfNoOpenerPermissionOnQueriedWorkbasket() {
    TaskService taskService = taskanaEngine.getTaskService();

    ThrowingCallable call =
        () -> {
          taskService
              .createTaskQuery()
              .workbasketKeyDomainIn(new KeyDomain("USER_2_1", "DOMAIN_A"))
              .list();
        };
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedToQueryWorkbasketException.class);
  }

  @WithAccessId(user = "user_1_1", groups = "group_1")
  @Test
  void testThrowsExceptionIfNoOpenerPermissionOnAtLeastOneQueriedWorkbasket() {
    TaskService taskService = taskanaEngine.getTaskService();
    ThrowingCallable call =
        () -> {
          taskService
              .createTaskQuery()
              .workbasketKeyDomainIn(
                  new KeyDomain("USER_1_1", "DOMAIN_A"), new KeyDomain("USER_2_1", "DOMAIN_A"))
              .list();
        };
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedToQueryWorkbasketException.class);
  }
}
