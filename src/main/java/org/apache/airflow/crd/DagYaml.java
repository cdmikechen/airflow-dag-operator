package org.apache.airflow.crd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DagYaml {

    public DagYaml() {
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /* ------------------------ import_libs ------------------------ */
    @JsonProperty("import_libs")
    private List<String> importLibs;

    /* ------------------------ default_args ------------------------ */
    @JsonProperty("default_args")
    private DefaultArg defaultArgs;

    public static class DefaultArg {

        public DefaultArg() {
        }

        private String owner;

        @JsonProperty("depends_on_past")
        private Boolean dependsOnPast;

        @JsonProperty("email")
        private List<String> email;

        @JsonProperty("email_on_failure")
        private Boolean emailOnFailure;

        @JsonProperty("email_on_retry")
        private Boolean emailOnRetry;

        private Integer retries;

        @JsonProperty("retry_delay")
        private String retryDelay;

        private String queue;

        private String pool;

        @JsonProperty("priority_weight")
        private Integer priorityWeight;

        @JsonProperty("wait_for_downstream")
        private Boolean waitForDownstream;

        private String sla;

        @JsonProperty("execution_timeout")
        private String executionTimeout;

        @JsonProperty("on_failure_callback")
        private String onFailureCallback;

        @JsonProperty("on_success_callback")
        private String onSuccessCallback;

        @JsonProperty("on_retry_callback")
        private String onRetryCallback;

        @JsonProperty("sla_miss_callback")
        private String slaMissCallback;

        @JsonProperty("trigger_rule")
        private String triggerRule;

        @JsonProperty("end_date")
        private String endDate;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("other_args")
        private List<Config> otherArgs;

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Boolean getDependsOnPast() {
            return dependsOnPast;
        }

        public void setDependsOnPast(Boolean dependsOnPast) {
            this.dependsOnPast = dependsOnPast;
        }

        public List<String> getEmail() {
            return email;
        }

        public void setEmail(List<String> email) {
            this.email = email;
        }

        public Boolean getEmailOnFailure() {
            return emailOnFailure;
        }

        public void setEmailOnFailure(Boolean emailOnFailure) {
            this.emailOnFailure = emailOnFailure;
        }

        public Boolean getEmailOnRetry() {
            return emailOnRetry;
        }

        public void setEmailOnRetry(Boolean emailOnRetry) {
            this.emailOnRetry = emailOnRetry;
        }

        public Integer getRetries() {
            return retries;
        }

        public void setRetries(Integer retries) {
            this.retries = retries;
        }

        public String getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(String retryDelay) {
            this.retryDelay = retryDelay;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getPool() {
            return pool;
        }

        public void setPool(String pool) {
            this.pool = pool;
        }

        public Integer getPriorityWeight() {
            return priorityWeight;
        }

        public void setPriorityWeight(Integer priorityWeight) {
            this.priorityWeight = priorityWeight;
        }

        public Boolean getWaitForDownstream() {
            return waitForDownstream;
        }

        public void setWaitForDownstream(Boolean waitForDownstream) {
            this.waitForDownstream = waitForDownstream;
        }

        public String getSla() {
            return sla;
        }

        public void setSla(String sla) {
            this.sla = sla;
        }

        public String getExecutionTimeout() {
            return executionTimeout;
        }

        public void setExecutionTimeout(String executionTimeout) {
            this.executionTimeout = executionTimeout;
        }

        public String getOnFailureCallback() {
            return onFailureCallback;
        }

        public void setOnFailureCallback(String onFailureCallback) {
            this.onFailureCallback = onFailureCallback;
        }

        public String getOnSuccessCallback() {
            return onSuccessCallback;
        }

        public void setOnSuccessCallback(String onSuccessCallback) {
            this.onSuccessCallback = onSuccessCallback;
        }

        public String getOnRetryCallback() {
            return onRetryCallback;
        }

        public void setOnRetryCallback(String onRetryCallback) {
            this.onRetryCallback = onRetryCallback;
        }

        public String getSlaMissCallback() {
            return slaMissCallback;
        }

        public void setSlaMissCallback(String slaMissCallback) {
            this.slaMissCallback = slaMissCallback;
        }

        public String getTriggerRule() {
            return triggerRule;
        }

        public void setTriggerRule(String triggerRule) {
            this.triggerRule = triggerRule;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public List<Config> getOtherArgs() {
            return otherArgs;
        }

        public void setOtherArgs(List<Config> otherArgs) {
            this.otherArgs = otherArgs;
        }
    }

    public static class Config {

        public Config() {
        }

        private String name;

        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public List<String> getImportLibs() {
        return importLibs;
    }

    public void setImportLibs(List<String> importLibs) {
        this.importLibs = importLibs;
    }

    public DefaultArg getDefaultArgs() {
        return defaultArgs;
    }

    public void setDefaultArgs(DefaultArg defaultArgs) {
        this.defaultArgs = defaultArgs;
    }

    /* ------------------------ DAG ------------------------ */
    private String description;

    @JsonProperty("schedule_interval")
    private String scheduleInterval;

    private String catchup;

    private List<String> tags;

    @JsonProperty("dagrun_timeout")
    private String dagrunTimeout;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScheduleInterval() {
        return scheduleInterval;
    }

    public void setScheduleInterval(String scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    public String getCatchup() {
        return catchup;
    }

    public void setCatchup(String catchup) {
        this.catchup = catchup;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDagrunTimeout() {
        return dagrunTimeout;
    }

    public void setDagrunTimeout(String dagrunTimeout) {
        this.dagrunTimeout = dagrunTimeout;
    }

    /* ------------------------ task ------------------------ */
    public static class Task {

        public Task() {
        }

        @JsonProperty("task_id")
        private String taskId;

        private String operator;

        private List<String> dependencies;

        @JsonProperty("depends_on_past")
        private Boolean dependsOnPast;

        private Integer retries;

        @JsonProperty("bash_command")
        private String bashCommand;

        @JsonProperty("python_callable")
        private String pythonCallable;

        private List<Config> params;

        @JsonProperty("other_params")
        private List<Config> otherParams;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }

        public String getBashCommand() {
            return bashCommand;
        }

        public void setBashCommand(String bashCommand) {
            this.bashCommand = bashCommand;
        }

        public String getPythonCallable() {
            return pythonCallable;
        }

        public void setPythonCallable(String pythonCallable) {
            this.pythonCallable = pythonCallable;
        }

        public Boolean getDependsOnPast() {
            return dependsOnPast;
        }

        public void setDependsOnPast(Boolean dependsOnPast) {
            this.dependsOnPast = dependsOnPast;
        }

        public Integer getRetries() {
            return retries;
        }

        public void setRetries(Integer retries) {
            this.retries = retries;
        }

        public List<Config> getParams() {
            return params;
        }

        public void setParams(List<Config> params) {
            this.params = params;
        }

        public List<Config> getOtherParams() {
            return otherParams;
        }

        public void setOtherParams(List<Config> otherParams) {
            this.otherParams = otherParams;
        }
    }

    private List<Task> tasks;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @JsonProperty("python_codes")
    private String pythonCodes;

    public String getPythonCodes() {
        return pythonCodes;
    }

    public void setPythonCodes(String pythonCodes) {
        this.pythonCodes = pythonCodes;
    }
}
