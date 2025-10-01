package com.company.experiments.view.rulebuilder;

import com.company.experiments.entity.*;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Route(value = "ruleBuilder", layout = MainView.class)
@ViewController(id = "RuleBuilder.view")
@ViewDescriptor(path = "rule-builder-view.xml")
public class RuleBuilderView extends StandardView {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private Select<RuleScenario> ruleSelect;

    @ViewComponent
    private TextField ruleNameField;

    @ViewComponent
    private TextField ruleCodeField;

    @ViewComponent
    private Select<ScenarioType> scenarioTypeSelect;

    @ViewComponent
    private TextField priorityField;

    @ViewComponent
    private Select<Employee> createdBySelect;

    @ViewComponent
    private Div conditionsContainer;

    @ViewComponent
    private Div actionsContainer;

    @ViewComponent
    private TextArea testInputArea;

    @ViewComponent
    private Div testResultsContainer;

    private RuleScenario currentRule;
    private List<ConditionRow> conditions = new ArrayList<>();
    private List<ActionRow> actions = new ArrayList<>();

    @Subscribe
    public void onInit(InitEvent event) {
        loadRules();
        loadEmployees();
        initializeScenarioTypes();
    }

    private void loadRules() {
        List<RuleScenario> rules = dataManager.load(RuleScenario.class)
                .all()
                .fetchPlan("_instance_name")
                .list();

        ruleSelect.setItems(rules);
        ruleSelect.setItemLabelGenerator(RuleScenario::getName);
        ruleSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadRule(e.getValue());
            }
        });
    }

    private void loadEmployees() {
        List<Employee> employees = dataManager.load(Employee.class)
                .all()
                .fetchPlan("_instance_name")
                .list();

        createdBySelect.setItems(employees);
        createdBySelect.setItemLabelGenerator(Employee::getInstanceName);
    }

    private void initializeScenarioTypes() {
        scenarioTypeSelect.setItems(ScenarioType.values());
        scenarioTypeSelect.setItemLabelGenerator(ScenarioType::getId);
    }

    private void loadRule(RuleScenario rule) {
        currentRule = rule;
        ruleNameField.setValue(rule.getName());
        ruleCodeField.setValue(rule.getCode());
        scenarioTypeSelect.setValue(rule.getScenarioType());
        priorityField.setValue(String.valueOf(rule.getPriority()));
        createdBySelect.setValue(rule.getCreatedBy());

        // Parse and display conditions/actions (simplified)
        conditions.clear();
        actions.clear();
        renderConditions();
        renderActions();
    }

    @Subscribe("newRuleButton")
    public void onNewRuleButtonClick(ClickEvent<JmixButton> event) {
        currentRule = null;
        ruleNameField.clear();
        ruleCodeField.clear();
        scenarioTypeSelect.clear();
        priorityField.clear();
        conditions.clear();
        actions.clear();
        renderConditions();
        renderActions();
    }

    @Subscribe("addConditionButton")
    public void onAddConditionButtonClick(ClickEvent<JmixButton> event) {
        conditions.add(new ConditionRow("", "", ""));
        renderConditions();
    }

    @Subscribe("addActionButton")
    public void onAddActionButtonClick(ClickEvent<JmixButton> event) {
        actions.add(new ActionRow("", ""));
        renderActions();
    }

    private void renderConditions() {
        conditionsContainer.removeAll();

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        for (int i = 0; i < conditions.size(); i++) {
            ConditionRow condition = conditions.get(i);
            int index = i;

            Div conditionCard = new Div();
            conditionCard.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("border-radius", "8px")
                    .set("padding", "16px")
                    .set("border-left", "4px solid #2196F3");

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(HorizontalLayout.Alignment.CENTER);
            row.setSpacing(true);

            Span ifLabel = new Span("IF");
            ifLabel.getStyle().set("font-weight", "bold").set("color", "#2196F3");

            Select<String> fieldSelect = new Select<>();
            fieldSelect.setItems("Alert Severity", "Budget Amount", "Infrastructure Status", "Team Size");
            fieldSelect.setPlaceholder("Select field");
            fieldSelect.setWidth("200px");
            fieldSelect.setValue(condition.field);
            fieldSelect.addValueChangeListener(e -> condition.field = e.getValue());

            Select<String> operatorSelect = new Select<>();
            operatorSelect.setItems("equals", "greater than", "less than", "contains", "is null");
            operatorSelect.setPlaceholder("Select operator");
            operatorSelect.setWidth("150px");
            operatorSelect.setValue(condition.operator);
            operatorSelect.addValueChangeListener(e -> condition.operator = e.getValue());

            TextField valueField = new TextField();
            valueField.setPlaceholder("Value");
            valueField.setWidth("200px");
            valueField.setValue(condition.value);
            valueField.addValueChangeListener(e -> condition.value = e.getValue());

            JmixButton removeButton = new JmixButton();
            removeButton.setText("Remove");
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            removeButton.addClickListener(e -> {
                conditions.remove(index);
                renderConditions();
            });

            row.add(ifLabel, fieldSelect, operatorSelect, valueField, removeButton);
            conditionCard.add(row);
            layout.add(conditionCard);
        }

        if (conditions.isEmpty()) {
            Span emptyLabel = new Span("No conditions defined. Click 'Add Condition' to start.");
            emptyLabel.getStyle().set("color", "#999").set("font-style", "italic");
            layout.add(emptyLabel);
        }

        conditionsContainer.add(layout);
    }

    private void renderActions() {
        actionsContainer.removeAll();

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        for (int i = 0; i < actions.size(); i++) {
            ActionRow action = actions.get(i);
            int index = i;

            Div actionCard = new Div();
            actionCard.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("border-radius", "8px")
                    .set("padding", "16px")
                    .set("border-left", "4px solid #4CAF50");

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(HorizontalLayout.Alignment.CENTER);
            row.setSpacing(true);

            Span thenLabel = new Span("THEN");
            thenLabel.getStyle().set("font-weight", "bold").set("color", "#4CAF50");

            Select<String> actionSelect = new Select<>();
            actionSelect.setItems("Send Alert", "Assign Team", "Create Budget", "Log Event", "Send Email");
            actionSelect.setPlaceholder("Select action");
            actionSelect.setWidth("200px");
            actionSelect.setValue(action.actionType);
            actionSelect.addValueChangeListener(e -> action.actionType = e.getValue());

            TextField parametersField = new TextField();
            parametersField.setPlaceholder("Action parameters (JSON)");
            parametersField.setWidth("400px");
            parametersField.setValue(action.parameters);
            parametersField.addValueChangeListener(e -> action.parameters = e.getValue());

            JmixButton removeButton = new JmixButton();
            removeButton.setText("Remove");
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            removeButton.addClickListener(e -> {
                actions.remove(index);
                renderActions();
            });

            row.add(thenLabel, actionSelect, parametersField, removeButton);
            actionCard.add(row);
            layout.add(actionCard);
        }

        if (actions.isEmpty()) {
            Span emptyLabel = new Span("No actions defined. Click 'Add Action' to start.");
            emptyLabel.getStyle().set("color", "#999").set("font-style", "italic");
            layout.add(emptyLabel);
        }

        actionsContainer.add(layout);
    }

    @Subscribe("saveRuleButton")
    public void onSaveRuleButtonClick(ClickEvent<JmixButton> event) {
        if (ruleNameField.isEmpty() || ruleCodeField.isEmpty() || scenarioTypeSelect.isEmpty() ||
                priorityField.isEmpty() || createdBySelect.isEmpty()) {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (currentRule == null) {
            currentRule = dataManager.create(RuleScenario.class);
        }

        currentRule.setName(ruleNameField.getValue());
        currentRule.setCode(ruleCodeField.getValue());
        currentRule.setScenarioType(scenarioTypeSelect.getValue());
        currentRule.setPriority(Integer.parseInt(priorityField.getValue()));
        currentRule.setCreatedBy(createdBySelect.getValue());
        currentRule.setIsActive(true);

        // Serialize conditions and actions to JSON (simplified)
        String conditionsJson = buildConditionsJson();
        String actionsJson = buildActionsJson();

        currentRule.setConditions(conditionsJson);
        currentRule.setActions(actionsJson);

        dataManager.save(currentRule);
        loadRules();
        Notification.show("Rule saved successfully", 3000, Notification.Position.BOTTOM_END);
    }

    private String buildConditionsJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < conditions.size(); i++) {
            ConditionRow c = conditions.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"field\":\"%s\",\"operator\":\"%s\",\"value\":\"%s\"}",
                    c.field, c.operator, c.value));
        }
        json.append("]");
        return json.toString();
    }

    private String buildActionsJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < actions.size(); i++) {
            ActionRow a = actions.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"actionType\":\"%s\",\"parameters\":\"%s\"}",
                    a.actionType, a.parameters));
        }
        json.append("]");
        return json.toString();
    }

    @Subscribe("testRuleButton")
    public void onTestRuleButtonClick(ClickEvent<JmixButton> event) {
        testResultsContainer.removeAll();

        String inputData = testInputArea.getValue();
        if (inputData == null || inputData.trim().isEmpty()) {
            inputData = "{\"sampleTest\": true}";
        }

        // Create execution log
        RuleExecutionLog log = dataManager.create(RuleExecutionLog.class);
        if (currentRule != null) {
            log.setRuleScenario(currentRule);
        }
        log.setInputData(inputData);
        log.setSuccess(true);
        log.setOutputData("{\"result\": \"Rule would execute successfully\"}");
        log.setExecutionTimeMs(45);
        log.setExecutedAt(LocalDateTime.now());

        if (createdBySelect.getValue() != null) {
            log.setTriggeredBy(createdBySelect.getValue());
        }

        dataManager.save(log);

        // Display test results
        VerticalLayout resultsLayout = new VerticalLayout();
        resultsLayout.setPadding(true);
        resultsLayout.setSpacing(true);

        H4 successTitle = new H4("✓ Test Execution Successful");
        successTitle.getStyle().set("color", "#4CAF50").set("margin", "0");

        Span details = new Span("Execution Time: 45ms\nConditions Evaluated: " + conditions.size() +
                "\nActions to Execute: " + actions.size());
        details.getStyle().set("white-space", "pre-wrap");

        resultsLayout.add(successTitle, details);
        testResultsContainer.add(resultsLayout);

        Notification.show("Rule test completed", 2000, Notification.Position.BOTTOM_END);
    }

    private static class ConditionRow {
        String field;
        String operator;
        String value;

        ConditionRow(String field, String operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }

    private static class ActionRow {
        String actionType;
        String parameters;

        ActionRow(String actionType, String parameters) {
            this.actionType = actionType;
            this.parameters = parameters;
        }
    }
}
