package org.mitre.tdp.boogie;

import org.springframework.context.ApplicationListener;

public interface Boogie extends RouteExpansionService, NavDataService, FirUirService, ApplicationListener<BoogieEvent.New424File> { }
