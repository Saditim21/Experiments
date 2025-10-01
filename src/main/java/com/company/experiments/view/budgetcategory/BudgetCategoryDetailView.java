package com.company.experiments.view.budgetcategory;

import com.company.experiments.entity.BudgetCategory;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "budgetCategories/:id", layout = MainView.class)
@ViewController(id = "BudgetCategory.detail")
@ViewDescriptor(path = "budget-category-detail-view.xml")
@EditedEntityContainer("budgetCategoryDc")
public class BudgetCategoryDetailView extends StandardDetailView<BudgetCategory> {
}
