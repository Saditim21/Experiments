package com.company.experiments.view.healthalert;

import com.company.experiments.entity.AlertStatus;
import com.company.experiments.entity.HealthAlert;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route(value = "healthAlerts/:id", layout = MainView.class)
@ViewController(id = "HealthAlert.detail")
@ViewDescriptor(path = "health-alert-detail-view.xml")
@EditedEntityContainer("healthAlertDc")
public class HealthAlertDetailView extends StandardDetailView<HealthAlert> {

    @ViewComponent
    private Div timelineContainer;

    @ViewComponent
    private JmixButton startInvestigationButton;

    @ViewComponent
    private JmixButton resolveButton;

    @ViewComponent
    private JmixButton markFalsePositiveButton;

    @Subscribe
    public void onReady(ReadyEvent event) {
        updateTimeline();
        updateActionButtons();
    }

    @Subscribe("startInvestigationButton")
    public void onStartInvestigationButtonClick(ClickEvent<JmixButton> event) {
        HealthAlert alert = getEditedEntity();
        if (alert.getStatus() == AlertStatus.NEW) {
            alert.setStatus(AlertStatus.INVESTIGATING);
            updateTimeline();
            updateActionButtons();
            Notification.show("Alert status changed to INVESTIGATING", 3000, Notification.Position.MIDDLE);
        }
    }

    @Subscribe("resolveButton")
    public void onResolveButtonClick(ClickEvent<JmixButton> event) {
        HealthAlert alert = getEditedEntity();
        if (alert.getStatus() == AlertStatus.INVESTIGATING) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(LocalDateTime.now());
            updateTimeline();
            updateActionButtons();
            Notification.show("Alert marked as RESOLVED", 3000, Notification.Position.MIDDLE);
        }
    }

    @Subscribe("markFalsePositiveButton")
    public void onMarkFalsePositiveButtonClick(ClickEvent<JmixButton> event) {
        HealthAlert alert = getEditedEntity();
        alert.setStatus(AlertStatus.FALSE_POSITIVE);
        alert.setResolvedAt(LocalDateTime.now());
        updateTimeline();
        updateActionButtons();
        Notification.show("Alert marked as FALSE POSITIVE", 3000, Notification.Position.MIDDLE);
    }

    private void updateTimeline() {
        timelineContainer.removeAll();

        HealthAlert alert = getEditedEntity();
        if (alert == null) return;

        VerticalLayout timeline = new VerticalLayout();
        timeline.setPadding(false);
        timeline.setSpacing(true);
        timeline.setWidthFull();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Detected event
        if (alert.getDetectedAt() != null) {
            timeline.add(createTimelineEvent(
                    "Alert Detected",
                    alert.getDetectedAt().format(formatter),
                    "Alert created with severity: " + alert.getSeverity(),
                    "#2196F3"
            ));
        }

        // Investigation event
        if (alert.getStatus() == AlertStatus.INVESTIGATING ||
                alert.getStatus() == AlertStatus.RESOLVED ||
                alert.getStatus() == AlertStatus.FALSE_POSITIVE) {
            String timestamp = alert.getDetectedAt() != null ?
                    alert.getDetectedAt().plusMinutes(5).format(formatter) : "";
            timeline.add(createTimelineEvent(
                    "Investigation Started",
                    timestamp,
                    alert.getAssignedTo() != null ?
                            "Assigned to: " + alert.getAssignedTo().getInstanceName() : "Investigation in progress",
                    "#FF9800"
            ));
        }

        // Resolution event
        if (alert.getResolvedAt() != null) {
            String statusText = alert.getStatus() == AlertStatus.FALSE_POSITIVE ?
                    "Marked as False Positive" : "Alert Resolved";
            timeline.add(createTimelineEvent(
                    statusText,
                    alert.getResolvedAt().format(formatter),
                    alert.getResolutionNotes() != null ? alert.getResolutionNotes() : "No resolution notes",
                    "#4CAF50"
            ));
        }

        timelineContainer.add(timeline);
    }

    private Div createTimelineEvent(String title, String timestamp, String description, String color) {
        Div eventDiv = new Div();
        eventDiv.getStyle()
                .set("border-left", "4px solid " + color)
                .set("padding-left", "16px")
                .set("margin-bottom", "16px")
                .set("padding-bottom", "8px");

        H4 titleSpan = new H4(title);
        titleSpan.getStyle().set("margin", "0 0 4px 0").set("color", color);

        Span timeSpan = new Span(timestamp);
        timeSpan.getStyle().set("font-size", "0.85em").set("color", "#666");

        Span descSpan = new Span(description);
        descSpan.getStyle().set("display", "block").set("margin-top", "8px");

        eventDiv.add(titleSpan, timeSpan, descSpan);
        return eventDiv;
    }

    private void updateActionButtons() {
        HealthAlert alert = getEditedEntity();
        if (alert != null) {
            AlertStatus status = alert.getStatus();
            startInvestigationButton.setEnabled(status == AlertStatus.NEW);
            resolveButton.setEnabled(status == AlertStatus.INVESTIGATING);
            markFalsePositiveButton.setEnabled(status == AlertStatus.NEW || status == AlertStatus.INVESTIGATING);
        }
    }
}
