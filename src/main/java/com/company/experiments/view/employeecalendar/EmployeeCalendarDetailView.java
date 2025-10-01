package com.company.experiments.view.employeecalendar;

import com.company.experiments.entity.EmployeeCalendar;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "employeeCalendars/:id", layout = MainView.class)
@ViewController(id = "EmployeeCalendar.detail")
@ViewDescriptor(path = "employee-calendar-detail-view.xml")
@EditedEntityContainer("employeeCalendarDc")
public class EmployeeCalendarDetailView extends StandardDetailView<EmployeeCalendar> {
}
