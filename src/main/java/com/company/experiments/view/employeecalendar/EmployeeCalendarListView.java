package com.company.experiments.view.employeecalendar;

import com.company.experiments.entity.EmployeeCalendar;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "employeeCalendars", layout = MainView.class)
@ViewController(id = "EmployeeCalendar.list")
@ViewDescriptor(path = "employee-calendar-list-view.xml")
@LookupComponent("employeeCalendarsDataGrid")
@DialogMode(width = "64em")
public class EmployeeCalendarListView extends StandardListView<EmployeeCalendar> {

    @Autowired
    private  ViewNavigators viewNavigators;

    @ViewComponent
    private JmixButton calendarViewButton;

    @Subscribe("calendarViewButton")
    public void onCalendarViewButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, EmployeeCalendarCalendarView.class).navigate();
    }
}
