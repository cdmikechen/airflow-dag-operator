package org.apache.airflow.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.airflow.crd.DagYaml;
import org.apache.airflow.util.StringUtils;

/**
 * Creating DAG content based on yaml
 */
public class DagTemplate {

    private final String dagName;

    private final DagYaml dagYaml;

    private final static String True = "True";
    private final static String False = "False";

    public DagTemplate(String dagName, DagYaml dagYaml) {
        this.dagName = dagName;
        this.dagYaml = dagYaml;
    }

    /**
     * Generate code from yaml
     */
    public String createDagContent() {
        StringBuilder content = new StringBuilder();
        // title
        if (StringUtils.notBlank(dagYaml.getTitle())) {
            content.append("\"\"\"\n");
            content.append(dagYaml.getTitle());
            content.append("\"\"\"\n");
        }

        // import
        if (!collectEmpty(dagYaml.getImportLibs())) {
            for (String importLib : dagYaml.getImportLibs()) {
                content.append(importLib).append("\n");
            }
        }

        // default_args
        createDefaultArgs(content);

        // dag
        content.append("\n");
        content.append("dag = DAG(\n");
        content.append("    '").append(dagName).append("',\n");
        content.append("    default_args=default_args,\n");
        if (StringUtils.notBlank(dagYaml.getDescription()))
            content.append("    description='").append(dagYaml.getDescription()).append("',\n");
        content.append("    schedule_interval=").append(dagYaml.getScheduleInterval()).append(",\n");
        content.append("    start_date=").append(dagYaml.getDefaultArgs().getStartDate()).append(",\n");
        if (!collectEmpty(dagYaml.getTags())) {
            content.append("    tags=['").append(String.join("','", dagYaml.getTags())).append("'],\n");
        }
        content.append(")\n");

        // custom code
        if (StringUtils.notBlank(dagYaml.getPythonCodes())) {
            content.append("\n");
            content.append(dagYaml.getPythonCodes());
            content.append("\n");
        }

        // task instance
        HashMap<String, Integer> taskMap = new HashMap<>();
        for (int i = 0, row = dagYaml.getTasks().size(); i < row; i++) {
            DagYaml.Task task = dagYaml.getTasks().get(i);
            taskMap.put(task.getTaskId(), i);
            content.append("\n");
            content.append("t_").append(i).append(" = ").append(task.getOperator()).append("(\n");
            content.append("    task_id='").append(task.getTaskId()).append("',\n");
            content.append("    dag=dag,\n");
            if (!collectEmpty(task.getParams())) {
                content.append("    params={ ");
                for (int j = 0, size = task.getParams().size(); j < size; j++) {
                    if (j > 0)
                        content.append(", ");
                    DagYaml.Config param = task.getParams().get(j);
                    content.append("'").append(param.getName()).append("': ").append(param.getValue());
                }
                content.append(" },\n");
            }
            if (StringUtils.notBlank(task.getBashCommand())) {
                content.append("    bash_command=").append(task.getBashCommand()).append(",\n");
            }
            if (StringUtils.notBlank(task.getPythonCallable())) {
                content.append("    python_callable=").append(task.getPythonCallable()).append(",\n");
            }
            if (!collectEmpty(task.getOtherParams())) {
                for (DagYaml.Config otherParam : task.getOtherParams()) {
                    content.append("    ").append(otherParam.getName())
                            .append("=").append(otherParam.getValue()).append(",\n");
                }
            }
            content.append(")\n");
        }

        // task relationship
        content.append("\n");
        for (DagYaml.Task task : dagYaml.getTasks()) {
            if (!collectEmpty(task.getDependencies())) {
                int taskIndex = taskMap.get(task.getTaskId());
                for (String dep : task.getDependencies()) {
                    content.append("t_").append(taskMap.get(dep)).append(" >> t_").append(taskIndex).append("\n");
                }
            }
        }

        return content.toString();
    }

    /**
     * Generate default_args
     */
    private void createDefaultArgs(StringBuilder content) {
        content.append("\n");
        content.append("default_args = {\n");
        DagYaml.DefaultArg defaultArgs = dagYaml.getDefaultArgs();
        createArg(content, "owner", defaultArgs.getOwner());
        createArg(content, "depends_on_past", defaultArgs.getDependsOnPast());
        createArg(content, "email", defaultArgs.getEmail());
        createArg(content, "email_on_failure", defaultArgs.getEmailOnFailure());
        createArg(content, "email_on_retry", defaultArgs.getEmailOnRetry());
        createArg(content, "retries", defaultArgs.getRetries());
        createArg(content, "retry_delay", defaultArgs.getRetryDelay());
        createArg(content, "queue", defaultArgs.getQueue());
        createArg(content, "pool", defaultArgs.getPool());
        createArg(content, "priority_weight", defaultArgs.getPriorityWeight());
        createArg(content, "wait_for_downstream", defaultArgs.getWaitForDownstream());
        createArg(content, "sla", defaultArgs.getSla());
        createArg(content, "execution_timeout", defaultArgs.getExecutionTimeout());
        createArg(content, "on_failure_callback", defaultArgs.getOnFailureCallback());
        createArg(content, "on_success_callback", defaultArgs.getOnSuccessCallback());
        createArg(content, "on_retry_callback", defaultArgs.getOnRetryCallback());
        createArg(content, "sla_miss_callback", defaultArgs.getSlaMissCallback());
        createArg(content, "trigger_rule", defaultArgs.getTriggerRule());
        createArg(content, "end_date", defaultArgs.getEndDate());
        if (!collectEmpty(defaultArgs.getOtherArgs())) {
            for (DagYaml.Config arg : defaultArgs.getOtherArgs()) {
                createArg(content, arg.getName(), arg.getValue());
            }
        }
        content.append("}\n");
    }

    private void createArg(StringBuilder content, String key, Integer value) {
        if (value != null) {
            content.append("    '").append(key).append("': ").append(value).append(",\n");
        }
    }

    private void createArg(StringBuilder content, String key, String value) {
        if (StringUtils.notBlank(value)) {
            content.append("    '").append(key).append("': ").append(value).append(",\n");
        }
    }

    private void createArg(StringBuilder content, String key, Boolean value) {
        if (value != null) {
            content.append("    '").append(key).append("': ").append(value ? True : False).append(",\n");
        }
    }

    private void createArg(StringBuilder content, String key, List<String> collection) {
        if (!collectEmpty(collection)) {
            content.append("    '").append(key).append("': ['")
                    .append(String.join("','", collection))
                    .append("'],\n");
        }
    }

    /**
     * check if collection is empty
     */
    private <T> boolean collectEmpty(Collection<T> list) {
        if (list == null)
            return true;
        return list.isEmpty();
    }
}
