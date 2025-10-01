package com.company.experiments.view.team;

import com.company.experiments.entity.Team;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "teams", layout = MainView.class)
@ViewController(id = "Team.list")
@ViewDescriptor(path = "team-list-view.xml")
@LookupComponent("teamsDataGrid")
@DialogMode(width = "64em")
public class TeamListView extends StandardListView<Team> {
}
