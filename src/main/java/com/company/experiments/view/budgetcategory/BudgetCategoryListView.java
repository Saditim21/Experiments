package com.company.experiments.view.budgetcategory;

import com.company.experiments.entity.BudgetCategory;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "budgetCategories", layout = MainView.class)
@ViewController(id = "BudgetCategory.list")
@ViewDescriptor(path = "budget-category-list-view.xml")
@LookupComponent("budgetCategoriesTreeDataGrid")
@DialogMode(width = "64em")
public class BudgetCategoryListView extends StandardListView<BudgetCategory> {
}
