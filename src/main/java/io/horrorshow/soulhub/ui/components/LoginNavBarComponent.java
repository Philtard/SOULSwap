package io.horrorshow.soulhub.ui.components;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import io.horrorshow.soulhub.ui.views.RegistrationView;

import javax.annotation.PostConstruct;

public class LoginNavBarComponent extends HorizontalLayout {
    private static final long serialVersionUID = 7056111594761246275L;

    private final Anchor login = new Anchor("login", "Login");
    private final RouterLink registration = new RouterLink("Registration", RegistrationView.class);

    public LoginNavBarComponent() {
        init();
    }

    @PostConstruct
    private void init() {
        initComponents();
        arrangeComponents();
    }

    private void initComponents() {
        registration.setHighlightCondition(HighlightConditions.sameLocation());
    }

    private void arrangeComponents() {
        add(login);
        add(registration);
    }

}
