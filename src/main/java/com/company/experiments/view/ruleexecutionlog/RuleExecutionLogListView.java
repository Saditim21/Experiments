package com.company.experiments.view.ruleexecutionlog;

import com.company.experiments.entity.RuleExecutionLog;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "ruleExecutionLogs", layout = MainView.class)
@ViewController(id = "RuleExecutionLog.list")
@ViewDescriptor(path = "rule-execution-log-list-view.xml")
@LookupComponent("ruleExecutionLogsDataGrid")
@DialogMode(width = "64em")
public class RuleExecutionLogListView extends StandardListView<RuleExecutionLog> {
}
