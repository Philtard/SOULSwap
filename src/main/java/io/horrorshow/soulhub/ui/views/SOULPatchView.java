package io.horrorshow.soulhub.ui.views;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.internal.AbstractFieldSupport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import io.horrorshow.soulhub.data.SOULPatch;
import io.horrorshow.soulhub.ui.MainLayout;
import io.horrorshow.soulhub.ui.UIConst;
import io.horrorshow.soulhub.ui.components.SOULPatchReadOnly;
import io.horrorshow.soulhub.ui.components.SpFileTabs;
import io.horrorshow.soulhub.ui.presenter.SOULPatchPresenter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static java.lang.String.format;

@Route(value = UIConst.ROUTE_SOULPATCH, layout = MainLayout.class)
@PageTitle(UIConst.TITLE_SOULPATCH)
@Log4j2
public class SOULPatchView extends VerticalLayout implements HasUrlParameter<String>,
        HasValueAndElement<
                AbstractField.ComponentValueChangeEvent<SOULPatchView, SOULPatch>, SOULPatch> {

    private static final long serialVersionUID = -6869511952510668506L;

    private final SOULPatchReadOnly soulPatchReadOnly = new SOULPatchReadOnly();
    private final SpFileTabs spFileTabs = new SpFileTabs();

    private final AbstractFieldSupport<SOULPatchView, SOULPatch> fieldSupport;
    private final SOULPatchPresenter soulPatchPresenter;

    public SOULPatchView(@Autowired SOULPatchPresenter soulPatchPresenter) {
        this.soulPatchPresenter = soulPatchPresenter;
        soulPatchPresenter.init(this);

        this.fieldSupport = new AbstractFieldSupport<>(this, null, Objects::equals, sp -> {
        });
        fieldSupport.addValueChangeListener(this::soulPatchChanged);

        setClassName("soulpatch-view");

        arrangeComponents();
    }

    public SOULPatchReadOnly getSoulPatchReadOnly() {
        return soulPatchReadOnly;
    }

    public SpFileTabs getSpFileTabs() {
        return spFileTabs;
    }

    private void arrangeComponents() {
        add(soulPatchReadOnly);
        add(spFileTabs);
    }

    private void soulPatchChanged(
            AbstractField
                    .ComponentValueChangeEvent<SOULPatchView, SOULPatch> event) {

        soulPatchReadOnly.setValue(event.getValue());
        spFileTabs.setValue(Lists.newArrayList(event.getValue().getSpFiles().iterator()));
    }

    @Override
    public SOULPatch getValue() {
        return fieldSupport.getValue();
    }

    @Override
    public void setValue(SOULPatch value) {
        fieldSupport.setValue(value);
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super AbstractField
                    .ComponentValueChangeEvent<SOULPatchView, SOULPatch>> listener) {

        return fieldSupport.addValueChangeListener(listener);
    }

    @Override
    public void setParameter(final BeforeEvent event, final String parameter) {
        var parameterMap = event.getLocation().getQueryParameters().getParameters();
        soulPatchPresenter.onNavigation(parameter, parameterMap);
    }

    public void createErrorView(final String msg) {
        removeAll();
        add(new H1(format("Error: %s", msg)));
        add(new RouterLink("to SOULPatches view", SOULPatchesView.class));
    }
}
