package com.company.experiments.view.main;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("")
@ViewController(id = "MainView")
@ViewDescriptor(path = "main-view.xml")
@Uses(DrawerToggle.class)
@Uses(JmixTabSheet.class)
@Uses(EntityComboBox.class)
@Uses(JmixValuePicker.class)
public class MainView extends StandardMainView {
}
