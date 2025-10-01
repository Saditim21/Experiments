package com.company.experiments.view.yearlybudget;

import com.company.experiments.entity.BudgetStatus;
import com.company.experiments.entity.YearlyBudget;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Route(value = "yearlyBudgets/:id", layout = MainView.class)
@ViewController(id = "YearlyBudget.detail")
@ViewDescriptor(path = "yearly-budget-detail-view.xml")
@EditedEntityContainer("yearlyBudgetDc")
public class YearlyBudgetDetailView extends StandardDetailView<YearlyBudget> {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private TypedTextField<BigDecimal> allocatedAmountField;

    @ViewComponent
    private TypedTextField<BigDecimal> spentAmountField;

    @ViewComponent
    private Span remainingAmountLabel;

    @ViewComponent
    private Span spentPercentageLabel;

    @ViewComponent
    private JmixButton approveButton;

    @ViewComponent
    private JmixButton rejectButton;

    @Subscribe
    public void onReady(ReadyEvent event) {
        updateCalculatedFields();
        updateWorkflowButtons();
    }

    @Subscribe("allocatedAmountField")
    public void onAllocatedAmountFieldTypedValueChange(TypedValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        updateCalculatedFields();
    }

    @Subscribe("spentAmountField")
    public void onSpentAmountFieldTypedValueChange(TypedValueChangeEvent<TypedTextField<BigDecimal>, BigDecimal> event) {
        updateCalculatedFields();
    }

    @Subscribe("approveButton")
    public void onApproveButtonClick(ClickEvent<JmixButton> event) {
        YearlyBudget budget = getEditedEntity();
        if (budget.getStatus() == BudgetStatus.DRAFT) {
            budget.setStatus(BudgetStatus.APPROVED);
            updateWorkflowButtons();
            Notification.show("Budget approved successfully", 3000, Notification.Position.MIDDLE);
        }
    }

    @Subscribe("rejectButton")
    public void onRejectButtonClick(ClickEvent<JmixButton> event) {
        YearlyBudget budget = getEditedEntity();
        if (budget.getStatus() == BudgetStatus.APPROVED) {
            budget.setStatus(BudgetStatus.DRAFT);
            updateWorkflowButtons();
            Notification.show("Budget rejected, moved back to draft", 3000, Notification.Position.MIDDLE);
        }
    }

    private void updateCalculatedFields() {
        YearlyBudget budget = getEditedEntity();
        if (budget != null) {
            BigDecimal remaining = budget.getRemainingAmount();
            BigDecimal percentage = budget.getSpentPercentage();

            remainingAmountLabel.setText(String.format("%.2f %s", remaining, budget.getCurrency()));
            spentPercentageLabel.setText(String.format("%.2f%%", percentage));

            // Color coding for percentage
            if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                spentPercentageLabel.getStyle().set("color", "red");
            } else if (percentage.compareTo(BigDecimal.valueOf(80)) > 0) {
                spentPercentageLabel.getStyle().set("color", "orange");
            } else {
                spentPercentageLabel.getStyle().set("color", "green");
            }
        }
    }

    private void updateWorkflowButtons() {
        YearlyBudget budget = getEditedEntity();
        if (budget != null) {
            BudgetStatus status = budget.getStatus();
            approveButton.setEnabled(status == BudgetStatus.DRAFT);
            rejectButton.setEnabled(status == BudgetStatus.APPROVED);
        }
    }
}
