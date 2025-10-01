package com.company.experiments.view.employeecalendar;

import com.company.experiments.entity.AvailabilityType;
import com.company.experiments.entity.Employee;
import com.company.experiments.entity.EmployeeCalendar;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "employeeCalendars/calendar", layout = MainView.class)
@ViewController(id = "EmployeeCalendar.calendar")
@ViewDescriptor(path = "employee-calendar-calendar-view.xml")
public class EmployeeCalendarCalendarView extends StandardView {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private Div calendarContainer;

    @ViewComponent
    private Span monthYearLabel;

    @ViewComponent
    private JmixButton prevMonthButton;

    @ViewComponent
    private JmixButton nextMonthButton;

    @ViewComponent
    private CollectionContainer<EmployeeCalendar> employeeCalendarsDc;

    private YearMonth currentMonth;

    @Subscribe
    public void onInit(InitEvent event) {
        currentMonth = YearMonth.now();
        loadCalendarData();
    }

    @Subscribe("prevMonthButton")
    public void onPrevMonthButtonClick(ClickEvent<JmixButton> event) {
        currentMonth = currentMonth.minusMonths(1);
        loadCalendarData();
    }

    @Subscribe("nextMonthButton")
    public void onNextMonthButtonClick(ClickEvent<JmixButton> event) {
        currentMonth = currentMonth.plusMonths(1);
        loadCalendarData();
    }

    private void loadCalendarData() {
        monthYearLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<EmployeeCalendar> calendars = dataManager.load(EmployeeCalendar.class)
                .query("select e from EmployeeCalendar e " +
                        "where e.date >= :startDate and e.date <= :endDate " +
                        "order by e.employee.lastName, e.employee.firstName, e.date")
                .parameter("startDate", startDate)
                .parameter("endDate", endDate)
                .fetchPlan("employeeCalendar-calendar-view")
                .list();

        employeeCalendarsDc.setItems(calendars);
        renderCalendar(calendars);
    }

    private void renderCalendar(List<EmployeeCalendar> calendars) {
        calendarContainer.removeAll();

        // Group calendars by employee
        Map<Employee, List<EmployeeCalendar>> calendarsByEmployee = new LinkedHashMap<>();
        for (EmployeeCalendar calendar : calendars) {
            calendarsByEmployee.computeIfAbsent(calendar.getEmployee(), k -> new ArrayList<>()).add(calendar);
        }

        // Create calendar grid
        VerticalLayout calendarLayout = new VerticalLayout();
        calendarLayout.setPadding(false);
        calendarLayout.setSpacing(false);
        calendarLayout.setWidthFull();

        // Header row with day names
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.getStyle().set("border-bottom", "2px solid #ccc");

        Span employeeHeader = new Span("Employee");
        employeeHeader.getStyle().set("width", "150px");
        employeeHeader.getStyle().set("font-weight", "bold");
        employeeHeader.getStyle().set("padding", "8px");
        headerRow.add(employeeHeader);

        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            Span dayHeader = new Span(date.format(DateTimeFormatter.ofPattern("dd EEE")));
            dayHeader.getStyle().set("width", "60px");
            dayHeader.getStyle().set("font-weight", "bold");
            dayHeader.getStyle().set("text-align", "center");
            dayHeader.getStyle().set("padding", "8px");
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                dayHeader.getStyle().set("background-color", "#f0f0f0");
            }
            headerRow.add(dayHeader);
        }
        calendarLayout.add(headerRow);

        // Employee rows
        for (Map.Entry<Employee, List<EmployeeCalendar>> entry : calendarsByEmployee.entrySet()) {
            Employee employee = entry.getKey();
            List<EmployeeCalendar> employeeCalendars = entry.getValue();

            HorizontalLayout employeeRow = new HorizontalLayout();
            employeeRow.setWidthFull();
            employeeRow.getStyle().set("border-bottom", "1px solid #eee");

            Span employeeName = new Span(employee.getInstanceName());
            employeeName.getStyle().set("width", "150px");
            employeeName.getStyle().set("padding", "8px");
            employeeRow.add(employeeName);

            Map<LocalDate, EmployeeCalendar> calendarMap = new HashMap<>();
            for (EmployeeCalendar cal : employeeCalendars) {
                calendarMap.put(cal.getDate(), cal);
            }

            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                Div dayCell = new Div();
                dayCell.getStyle().set("width", "60px");
                dayCell.getStyle().set("height", "40px");
                dayCell.getStyle().set("text-align", "center");
                dayCell.getStyle().set("padding", "4px");
                dayCell.getStyle().set("border-left", "1px solid #eee");

                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    dayCell.getStyle().set("background-color", "#f9f9f9");
                }

                EmployeeCalendar calendar = calendarMap.get(date);
                if (calendar != null) {
                    AvailabilityType type = calendar.getAvailabilityType();
                    String color = getColorForAvailabilityType(type);
                    String abbreviation = getAbbreviationForAvailabilityType(type);

                    Span statusSpan = new Span(abbreviation);
                    statusSpan.getStyle().set("background-color", color);
                    statusSpan.getStyle().set("color", "white");
                    statusSpan.getStyle().set("padding", "2px 4px");
                    statusSpan.getStyle().set("border-radius", "3px");
                    statusSpan.getStyle().set("font-size", "0.8em");
                    statusSpan.setTitle(type.getId() + (calendar.getHoursAvailable() != null ?
                            " (" + calendar.getHoursAvailable() + "h)" : ""));
                    dayCell.add(statusSpan);
                }

                employeeRow.add(dayCell);
            }

            calendarLayout.add(employeeRow);
        }

        calendarContainer.add(calendarLayout);
    }

    private String getColorForAvailabilityType(AvailabilityType type) {
        return switch (type) {
            case AVAILABLE -> "#4CAF50";
            case VACATION -> "#2196F3";
            case SICK -> "#F44336";
            case TRAINING -> "#FF9800";
        };
    }

    private String getAbbreviationForAvailabilityType(AvailabilityType type) {
        return switch (type) {
            case AVAILABLE -> "A";
            case VACATION -> "V";
            case SICK -> "S";
            case TRAINING -> "T";
        };
    }
}
