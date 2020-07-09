package io.horrorshow.soulhub.ui.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.horrorshow.soulhub.ui.MainLayout;
import io.horrorshow.soulhub.ui.UIConst;

@Route(value = UIConst.ROUTE_LOGIN, layout = MainLayout.class)
@PageTitle(UIConst.TITLE_LOGIN)
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 7631869443546476927L;
    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        loginForm.addForgotPasswordListener(event -> {
            Notification notification = new Notification("Password reset not available");
            notification.setDuration(5003);
            notification.open();
        });

        loginForm.setAction(UIConst.ROUTE_LOGIN);

        add(new H1("SOULHub Login"), loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

}
