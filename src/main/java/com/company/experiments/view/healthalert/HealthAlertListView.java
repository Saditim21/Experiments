package com.company.experiments.view.healthalert;

import com.company.experiments.entity.HealthAlert;
import com.company.experiments.view.healthportal.HealthPortalView;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "healthAlerts", layout = MainView.class)
@ViewController(id = "HealthAlert.list")
@ViewDescriptor(path = "health-alert-list-view.xml")
@LookupComponent("healthAlertsDataGrid")
@DialogMode(width = "64em")
public class HealthAlertListView extends StandardListView<HealthAlert> {

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private JmixButton healthPortalButton;

    @Subscribe("healthPortalButton")
    public void onHealthPortalButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, HealthPortalView.class).navigate();
    }
}
