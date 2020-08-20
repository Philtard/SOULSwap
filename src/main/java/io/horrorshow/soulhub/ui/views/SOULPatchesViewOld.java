package io.horrorshow.soulhub.ui.views;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BrowserWindowResizeEvent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import io.horrorshow.soulhub.data.AppUser;
import io.horrorshow.soulhub.data.SOULPatch;
import io.horrorshow.soulhub.data.SOULPatchRating;
import io.horrorshow.soulhub.data.SPFile;
import io.horrorshow.soulhub.security.SecurityUtils;
import io.horrorshow.soulhub.service.SOULPatchService;
import io.horrorshow.soulhub.service.UserService;
import io.horrorshow.soulhub.ui.MainLayout;
import io.horrorshow.soulhub.ui.UIConst;
import io.horrorshow.soulhub.ui.components.SOULPatchReadOnlyDialog;
import io.horrorshow.soulhub.ui.components.SPFileReadOnlyDialog;
import io.horrorshow.soulhub.ui.components.StarsRating;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;

@Route(value = UIConst.ROUTE_PLAYGROUND, layout = MainLayout.class)
//@RouteAlias(value = UIConst.ROUTE_EMPTY, layout = MainLayout.class)
@Getter
@PageTitle(UIConst.TITLE_PLAYGROUND)
@Log4j2
@Deprecated
public class SOULPatchesViewOld
        extends VerticalLayout
        implements HasUrlParameter<String> {

    private static final long serialVersionUID = 3981631233877217865L;

    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_FILES = "files";
    private static final String COL_VIEWS = "views";
    private static final String COL_AUTHOR = "author";
    private static final String COL_RATINGS = "rating";

    private static final int PAGE_SIZE = 50;
    private static final int PAGINATOR_SIZE = 5;

    private final SOULPatchService service;
    private final UserService userService;
    private final PaginatedGrid<SOULPatch> grid = new PaginatedGrid<>();
    private final TextField filterText = new TextField("filter by (regex)");
    private final Checkbox filterOwnedSoulpatches = new Checkbox("show only my soulpatches");
    private final Button addSOULPatch = new Button("add SOULPatch", VaadinIcon.FILE_ADD.create());
    private final SOULPatchReadOnlyDialog soulPatchReadOnlyDialog = new SOULPatchReadOnlyDialog();
    private final SPFileReadOnlyDialog spFileReadOnlyDialog = new SPFileReadOnlyDialog();
    private final Span userGreeting = new Span("Hello!");

    private final TextField fullTextSearch = new TextField("full text search");
    private final Button fullTextSearchBtn = new Button("full text soulpatches");
    private final Button fullTextSearchSPFiles = new Button("full text search SPFiles");

    public SOULPatchesViewOld(@Autowired SOULPatchService service, @Autowired UserService userService) {

        this.service = service;
        this.userService = userService;

//        soulPatchReadOnlyDialog = new SOULPatchReadOnlyDialog(service, userService);

        addClassName("soulpatches-view");

        UI.getCurrent().getPage().addBrowserWindowResizeListener(this::browserWindowResized);

        initFields();

        arrangeComponents();

        updateList();
    }

    private void browserWindowResized(BrowserWindowResizeEvent event) {
        grid.getColumnByKey(COL_VIEWS).setVisible(event.getWidth() >= 1050);
        grid.getColumnByKey(COL_AUTHOR).setVisible(event.getWidth() >= 950);
        grid.getColumnByKey(COL_DESCRIPTION).setVisible(event.getWidth() >= 600);
    }

    private void initFields() {
        initFilters();

        initSOULPatchesGrid();

        initAddSOULPatchLink();

        initGreeting();
    }

    private void initGreeting() {
        if (SecurityUtils.isUserLoggedIn()) {
            userService.getCurrentAppUser()
                    .ifPresentOrElse(
                            u -> userGreeting.setText(format("Hello %s",
                                    u.getUserName()))
                            ,
                            () -> userGreeting.setText(format("Hello %s",
                                    SecurityUtils.getUserEmail())));
        } else {
            userGreeting.setVisible(false);
        }
    }

    private void initAddSOULPatchLink() {
        addSOULPatch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addSOULPatch.addClickListener(e -> UI.getCurrent().navigate(EditSOULPatchView.class, "new"));
    }

    private void initSOULPatchesGrid() {

        addSOULPatchesGridColumns();

        grid.setClassName("soulpatches-grid");

        grid.setColumnReorderingAllowed(true);

        grid.setPageSize(PAGE_SIZE);

        grid.setPaginatorSize(PAGINATOR_SIZE);

        grid.asSingleSelect().addValueChangeListener(event ->
                grid.asSingleSelect().getOptionalValue()
                        .ifPresentOrElse(
                                soulPatchReadOnlyDialog::open,
                                soulPatchReadOnlyDialog::close));
    }

    private void addSOULPatchesGridColumns() {

        grid.addColumn(SOULPatch::getName)
                .setHeader(COL_NAME)
                .setResizable(true)
                .setAutoWidth(true)
                .setFrozen(true)
                .setKey(COL_NAME)
                .setSortable(true);

        grid.addColumn(getColDescriptionRenderer())
                .setHeader(COL_DESCRIPTION)
                .setKey(COL_DESCRIPTION)
                .setFlexGrow(10)
                .setResizable(true)
                .setSortable(true)
                .setComparator(Comparator.comparingInt(sp -> sp.getDescription().length()));

        grid.addColumn(getColSpFilesRenderer())
                .setHeader(COL_FILES)
                .setAutoWidth(true)
                .setResizable(true)
                .setSortable(true)
                .setKey(COL_FILES)
                .setComparator(Comparator.comparingInt(sp -> sp.getSpFiles().size()));

        grid.addColumn(getColRatingRenderer())
                .setHeader(COL_RATINGS)
                .setKey(COL_RATINGS)
                .setWidth("14em")
                .setSortable(true)
                .setResizable(true)
                .setComparator(Comparator.comparingDouble(soulPatch ->
                        soulPatch.getRatings().stream()
                                .mapToDouble(SOULPatchRating::getStars)
                                .average().orElse(0d)));

        grid.addColumn(soulPatch -> valueOf(soulPatch.getNoViews()))
                .setHeader(COL_VIEWS)
                .setResizable(true)
                .setAutoWidth(true)
                .setKey(COL_VIEWS)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(SOULPatch::getNoViews));

        grid.addColumn(soulPatch -> soulPatch.getAuthor().getUserName())
                .setHeader(COL_AUTHOR)
                .setResizable(true)
                .setAutoWidth(true)
                .setKey(COL_AUTHOR)
                .setSortable(true)
                .setComparator(Comparator.comparing(sp -> sp.getAuthor().getUserName()));
    }

    private ComponentRenderer<VerticalLayout, SOULPatch> getColSpFilesRenderer() {
        return new ComponentRenderer<>(sp -> {
            VerticalLayout spFilesLayout = new VerticalLayout();
            if (!sp.getSpFiles().isEmpty()) {
                sp.getSpFiles().forEach(spFile -> {
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.add(
                            new Button(
                                    format("%s [%s]", spFile.getName(),
                                            (spFile.getFileType() != null) ? spFile.getFileType().toString() : ""),
                                    VaadinIcon.FILE_CODE.create(),
                                    event -> spFileReadOnlyDialog.open(spFile)));

                    spFilesLayout.add(layout);
                });
            } else {
                spFilesLayout.add(new Span("no files attached"));
            }
            return spFilesLayout;
        });
    }

    private ComponentRenderer<HorizontalLayout, SOULPatch> getColRatingRenderer() {
        return new ComponentRenderer<>(sp -> {
            Span rating = new Span(String.format("%.2f", sp.getAverageRating()));
            StarsRating starsRating = new StarsRating();
            starsRating.setValue((int) Math.round(sp.getRatings().stream()
                    .mapToDouble(SOULPatchRating::getStars)
                    .average().orElse(0d)));
            starsRating.setNumstars(5);
            starsRating.setManual(true);
            starsRating.setReadOnly(!SecurityUtils.isUserLoggedIn());
            starsRating.addValueChangeListener(
                    ratingEvent -> currentUserSOULPatchRating(sp, ratingEvent));
            return new HorizontalLayout(rating, starsRating);
        });
    }

    private ComponentRenderer<Paragraph, SOULPatch> getColDescriptionRenderer() {
        return new ComponentRenderer<>(sp -> {
            Paragraph p = new Paragraph();
            p.setText(sp.getDescription());
            p.addClassName("sp-grid-col-description");
            p.setWidthFull();
            return p;
        });
    }

    private void currentUserSOULPatchRating(SOULPatch soulPatch,
                                            AbstractField.ComponentValueChangeEvent<StarsRating, Integer> event) {
        log.debug("soulpatch rating event {} for soulpatch {}", event, soulPatch);

        if (SecurityUtils.isUserLoggedIn() && userService.getCurrentAppUser().isPresent()) {
            AppUser currentUser = userService.getCurrentAppUser().get();
            soulPatch.getRatings().stream()
                    .filter(soulPatchRating ->
                            soulPatchRating.getAppUser()
                                    .equals(currentUser)).distinct().findAny()
                    .ifPresentOrElse(soulPatchRating -> {
                                // user rating present
                                soulPatchRating.setStars(event.getValue());
                                service.save(soulPatch);

                                log.debug("rating by {} exists {}",
                                        currentUser.getUserName(),
                                        soulPatchRating.toString());
                            },
                            () -> {
                                // no rating present
                                SOULPatchRating rating = new SOULPatchRating();
                                rating.setAppUser(currentUser);
                                rating.setSoulPatch(soulPatch);
                                rating.setStars(event.getValue());
                                soulPatch.getRatings().add(rating);
                                service.save(soulPatch);

                                log.debug("no rating by {} exists",
                                        currentUser.getUserName());
                            });
        } else {
            event.getSource().setNumstars(event.getOldValue());
            new Notification("log in to rate soulpatches", 3000, Notification.Position.MIDDLE).open();
        }
    }

    private void initFilters() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);

        filterText.addValueChangeListener(e -> updateList());

        filterOwnedSoulpatches.addValueChangeListener(event -> updateList());

        fullTextSearchBtn.addClickListener(event -> {
            List<SOULPatch> fullTextSearchResult =
                    service.fullTextSearchSOULPatches(fullTextSearch.getValue());

            String resultString = fullTextSearchResult.stream()
                    .map(soulPatch ->
                            format("Name: %s\nDescription: %s",
                                    soulPatch.getName(), soulPatch.getDescription()))
                    .collect(Collectors.joining("\n\n"));
            new Notification(
                    format("Full Text Search Result: \n%s", resultString),
                    3000, Notification.Position.MIDDLE).open();
            log.debug("fullTextSearchResult:\n{}", resultString);
        });

        fullTextSearchSPFiles.addClickListener(event -> {
            List<SPFile> res = service.fullTextSearchSPFiles(fullTextSearch.getValue());

            String resultString = res.stream().map(SPFile::getName)
                    .collect(Collectors.joining(", "));

            new Notification(
                    format("Full Text Search through spFiles results: %s", resultString),
                    3000, Notification.Position.MIDDLE).open();

            log.debug("full text search spfiles: {}",
                    res.stream().map(SPFile::toString).collect(Collectors.joining()));
        });
    }

    private void arrangeComponents() {
        VerticalLayout fullText =
                new VerticalLayout(
                        fullTextSearch,
                        fullTextSearchBtn,
                        fullTextSearchSPFiles);

        HorizontalLayout toolbar =
                new HorizontalLayout(
                        filterText,
                        filterOwnedSoulpatches,
                        addSOULPatch,
                        fullText);

        VerticalLayout gridLayout = new VerticalLayout(grid);

        HorizontalLayout mainContent = new HorizontalLayout(gridLayout);
        mainContent.setSizeFull();

        add(userGreeting, toolbar, mainContent);
        setSizeFull();
    }

    public void updateList() {
        grid.setItems(
                service.findAll(filterText.getValue()).stream()
                        .filter(soulPatch -> (filterOwnedSoulpatches.getValue() &&
                                soulPatch.getAuthor().getEmail()
                                        .equals(SecurityUtils.getUserEmail()))

                                ||
                                !filterOwnedSoulpatches.getValue())
                        .collect(Collectors.toList()));
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Location location = event.getLocation();
        QueryParameters locationQueryParameters
                = location.getQueryParameters();
        log.debug("parameter: {}", parameter);
        Map<String, List<String>> parametersMap
                = locationQueryParameters.getParameters();
        log.debug("parametersMap: {}", parametersMap.toString());

        if (parametersMap.containsKey(UIConst.PARAM_SHOW_BY_CURRENT_USER)
                && parametersMap.get(UIConst.PARAM_SHOW_BY_CURRENT_USER)
                .stream().anyMatch(Boolean.TRUE.toString()::equalsIgnoreCase)) {
            filterOwnedSoulpatches.setValue(Boolean.TRUE);
        }
    }
}