import dataIntegrationApi from './dataintegrationRestApi';

/** Business logic layer over the DataIntegration REST API. */
const dataIntegrationStore = {

    /**
     * Retrieves a task execution report.
     */
    getExecutionReport: (baseUrl, projectId, taskId) => {
        return dataIntegrationApi.activityResult(baseUrl, projectId, taskId, "ExecuteTransform")
            .then(({data}) => {
              return data;
            });
    },

    /**
     * Retrieves the current script code.
     */
    getScript: (baseUrl, projectId, scriptTaskId) => {
        return dataIntegrationApi.getTask(baseUrl, projectId, scriptTaskId)
            .then(({data}) => {
                return data.data.parameters.script;
            });
    },

    /**
     * Retrieves auto completions for the script code at a specific code position.
     */
    getCompletions: (baseUrl, projectId, scriptTaskId, line, column) => {
        const requestJson = {
            "line": line,
            "column": column
        };

        return dataIntegrationApi.completions(baseUrl, projectId, scriptTaskId, requestJson)
    },

    /**
     * Saves the script code, executes the script in context of the workflow and returns the result tables.
     *
     * Result is a JSON representation of OperatorExecutionResult {"inputs": [{...}, {...}], "output": {...}}
     * "output" Can be undefined if there is no output
     * "inputs" can have 0 to many entries
     * Entries have the following form: {"attributes": ["prop1", "prop2", ...], "values": [[["prop1 val"], ["prop2 val"]], [[...]], ...]}
     * Each entry has the table headers/attributes and array-arrays of strings for each attribute, so the number of entries in "attributes" matches those of "values".
     *
     **/
    evaluateScript: (baseUrl, projectId, scriptTaskId, workflowId, workflowOperatorId, scriptCode) => {
        const patchJson = {"data": {
            "parameters": {
                "script": scriptCode
            }
        }};
        const executeSparkOperatorActivity = "ExecuteSparkOperator";
        // Save script task with modified script, FIXME: Have separate 'Save' step, execute activity without the need of saving the script task
        return dataIntegrationApi.patchTask(baseUrl, projectId, scriptTaskId, patchJson)
            .then(() => {
                // Configure the script task's workflow operator ID for the activity execution (the same script task could appear multiple times in a workflow)
                return dataIntegrationApi.configureTaskActivity(baseUrl, projectId, workflowId, executeSparkOperatorActivity, {"operator": workflowOperatorId});
            })
            .then(() => {
                return dataIntegrationApi.executeTaskActivityBlocking(baseUrl, projectId, workflowId, executeSparkOperatorActivity);
            })
            .then(() => {
                return dataIntegrationApi.activityResult(baseUrl, projectId, workflowId, executeSparkOperatorActivity);
            })
    }
};

export default dataIntegrationStore;