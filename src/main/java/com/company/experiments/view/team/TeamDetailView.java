package com.company.experiments.view.team;

import com.company.experiments.entity.Team;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "teams/:id", layout = MainView.class)
@ViewController(id = "Team.detail")
@ViewDescriptor(path = "team-detail-view.xml")
@EditedEntityContainer("teamDc")
public class TeamDetailView extends StandardDetailView<Team> {
}
