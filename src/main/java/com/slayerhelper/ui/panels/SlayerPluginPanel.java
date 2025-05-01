package com.slayerhelper.ui.panels;

import com.slayerhelper.util.SlayerTasksFetcher;
import com.slayerhelper.ui.renderers.SlayerTasksRenderer;
import com.slayerhelper.data.LocationDataLoader;
import com.slayerhelper.data.SlayerDataLoader;
import com.slayerhelper.domain.Location;
import com.slayerhelper.domain.SlayerTask;
import com.slayerhelper.ui.components.*;
import com.slayerhelper.util.WikiUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
public class SlayerPluginPanel extends PluginPanel {

    private final SlayerTasksFetcher slayerTasksFetcher;
    private final SearchBar searchBar;
    private final DefaultListModel<SlayerTask> listModel = new DefaultListModel<>();
    private final String[] tabImageNames = {
            "world_map.png", "inventory.png", "protect_from_all.png", "combat.png", "slayer_icon.png"
    };
    private final LocationDataLoader locationDataLoader = new LocationDataLoader();

    public SlayerPluginPanel() {
        slayerTasksFetcher = new SlayerTasksFetcher(new SlayerDataLoader());
        searchBar = new SearchBar(this::filterList, this::clearFilter);
        createTaskListPanel(new ArrayList<>());
    }

    private JPanel createHeaderPanel(SlayerTask task) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
        String monsterName = task.getMonster();
        String monsterFileName = task.getMonsterFileName();

        try {
            BufferedImage img = ImageUtil.loadImageResource(getClass(), monsterFileName);
            BufferedImage resizedImg = ImageUtil.resizeImage(img, img.getWidth() / 2, img.getHeight() / 2);
            ImageIcon imageIcon = new ImageIcon(resizedImg);
            return new HeaderPanel(font, monsterName, Color.CYAN, imageIcon, SwingConstants.CENTER).getHeaderPanel();
        } catch (NullPointerException e) {
            log.info("Couldn't find image with name... {}", monsterFileName, e);
            return new HeaderPanel(font, monsterName, Color.ORANGE, SwingConstants.CENTER).getHeaderPanel();
        }
    }

    private JScrollPane createVerticalPanel(SlayerTask task) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        List<ImageIcon> icons = loadTabIcons();

        // Create section panels
        JPanel locationPanel = createSectionPanel(icons.get(0), task.getLocations(), "Map Location");
        JPanel itemPanel = createSectionPanel(icons.get(1), task.getItemsRequiredNames(), "Items Needed");
        JPanel attackStylesPanel = createSectionPanel(icons.get(2), task.getAttackStyles(), "Monster Attack Style");
        JPanel attributesPanel = createSectionPanel(icons.get(3), task.getAttributes(), "Monsters Attributes");
        JPanel masterPanel = createSectionPanel(icons.get(4), task.getSlayerMasters(), "Slayer Master");

        // Add all sections to the main panel with spacing
        mainPanel.add(locationPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(itemPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(attackStylesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(attributesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(masterPanel);

        // Configure scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private List<ImageIcon> loadTabIcons() {
        List<ImageIcon> icons = new ArrayList<>();
        for (String imageName : tabImageNames) {
            BufferedImage image = ImageUtil.loadImageResource(getClass(), "/images/" + imageName);
            icons.add(new ImageIcon(image));
        }
        return icons;
    }

    public void createTaskListPanel(Collection<SlayerTask> tasks) {
        removeComponents(null);

        // Add slayer icon
        BufferedImage image = ImageUtil.loadImageResource(getClass(), "/images/slayer_icon.png");
        JLabel searchBarIcon = new JLabel(new ImageIcon(image));
        searchBarIcon.setVerticalAlignment(SwingConstants.CENTER);
        searchBarIcon.setHorizontalAlignment(SwingConstants.CENTER);
        add(searchBarIcon);

        // Add title
        JLabel searchBarTitle = new JLabel("~ Slayer Helper ~");
        searchBarTitle.setForeground(Color.ORANGE);
        searchBarTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        searchBarTitle.setHorizontalAlignment(SwingConstants.CENTER);
        searchBarTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(searchBarTitle);

        // Add helper message
        JLabel searchBarHelperMsg = new JLabel("Search for a monster...");
        searchBarHelperMsg.setForeground(Color.WHITE);
        searchBarHelperMsg.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        add(searchBarHelperMsg);

        // Add search bar
        add(searchBar.getSearchBar());
        clearFilter();
        updateListModel(tasks);

        // Configure list
        JList<SlayerTask> monsterNames = new JList<>(listModel);
        monsterNames.setCellRenderer(new SlayerTasksRenderer());
        monsterNames.setFocusable(true);
        monsterNames.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        monsterNames.setBackground(new Color(30, 30, 30));
        monsterNames.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SlayerTask selectedTask = monsterNames.getSelectedValue();
                if (selectedTask != null) {
                    openTask(selectedTask);
                }
            }
        });

        add(monsterNames);
        revalidate();
        repaint();
    }

    private void openTask(SlayerTask task) {
        removeComponents(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30, 30, 30));

        JButton backButton = new JButton("<- Back");
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(50, 30));
        backButton.addActionListener(e -> {
            closeTask();
            remove(backButton);
        });
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topPanel.add(backButton, BorderLayout.NORTH);
        topPanel.add(createHeaderPanel(task), BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(createVerticalPanel(task), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void closeTask() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTaskListPanel(new ArrayList<>());
    }

    private void removeComponents(@Nullable Component[] excludedComponents) {
        Component[] components = getComponents();

        for (Component component : components) {
            if (excludedComponents != null && Arrays.asList(excludedComponents).contains(component)) {
                continue;
            }
            remove(component);
        }

        revalidate();
        repaint();
    }

    public void filterList(String searchText) {
        if (!searchText.isEmpty()) {
            Collection<SlayerTask> tasks = slayerTasksFetcher.getSlayerTasksByFilter(searchText);
            updateListModel(tasks);
        } else {
            listModel.clear();
        }
    }

    public void clearFilter() {
        searchBar.getSearchBar().setText("");
        updateListModel(slayerTasksFetcher.getAllSlayerTasks());
    }

    public void updateListModel(Collection<SlayerTask> tasks) {
        listModel.clear();
        tasks.forEach(listModel::addElement);
    }

    private JPanel createSectionPanel(ImageIcon icon, String[] content, String type) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create header with icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(50, 50, 50));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel iconLabel = new JLabel(icon);
        JLabel titleLabel = new JLabel(type);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLabel.setForeground(Color.ORANGE);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        sectionPanel.add(headerPanel);
        sectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add content based on type
        if (type.equalsIgnoreCase("Map Location")) {
            addLocationContent(sectionPanel, content);
        } else {
            addRegularContent(sectionPanel, content, type);
        }

        return sectionPanel;
    }

    private void addLocationContent(JPanel sectionPanel, String[] locations) {
        // Create details panel for locations
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(0, 1, 0, 5));
        detailsPanel.setBackground(new Color(45, 45, 65));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        detailsPanel.setVisible(false);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        sectionPanel.add(detailsPanel);

        // Add location buttons
        for (String locationName : locations) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.setBackground(new Color(55, 40, 40));
            buttonPanel.setPreferredSize(new Dimension(200, 30));

            JButton locationButton = new JButton(locationName);
            locationButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            locationButton.setForeground(Color.WHITE);
            locationButton.setBackground(new Color(20, 60, 60));
            locationButton.setBorderPainted(false);
            locationButton.setFocusPainted(false);
            locationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            locationButton.setMargin(new Insets(2, 5, 2, 5));

            // Add click handler for location details
            locationButton.addActionListener(e -> displayLocationDetails(detailsPanel, locationName));

            // Add right-click for wiki
            locationButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                        WikiUtil.openWebpage(WikiUtil.getWikiUrl("Map Location", locationName));
                    }
                }
            });

            buttonPanel.add(locationButton);
            sectionPanel.add(buttonPanel);
        }
    }

    private void addRegularContent(JPanel sectionPanel, String[] items, String type) {
        for (String item : items) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            itemPanel.setBackground(new Color(40, 40, 40));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JButton wikiButton = WikiUtil.createLinkButton(item, WikiUtil.getWikiUrl(type, item));
            itemPanel.add(wikiButton);

            sectionPanel.add(itemPanel);
            sectionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    private void displayLocationDetails(JPanel detailsPanel, String locationName) {
        // Get location data
        Location location = locationDataLoader.getLocation(locationName);

        // Clear previous content
        detailsPanel.removeAll();

        if (location == null) {
            JLabel errorLabel = new JLabel("No additional information available for " + locationName);
            errorLabel.setForeground(Color.RED);
            detailsPanel.add(errorLabel);
        } else {
            // Add location name
            JLabel nameLabel = new JLabel(location.getName());
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            nameLabel.setForeground(Color.ORANGE);
            detailsPanel.add(nameLabel);

            // Add cannonable status
            String cannonableText = "Cannonable: " + (location.isCannonable() ? "Yes" : "No");
            JLabel cannonableLabel = new JLabel(cannonableText);
            cannonableLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            cannonableLabel.setForeground(Color.WHITE);
            detailsPanel.add(cannonableLabel);

            // Add multicombat status
            String multicombatText = "MultiCombat: " + (location.isMulticombat() ? "Yes" : "No");
            JLabel multicombatLabel = new JLabel(multicombatText);
            multicombatLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            multicombatLabel.setForeground(Color.WHITE);
            detailsPanel.add(multicombatLabel);

            // Add wiki link button
            JButton wikiButton = new JButton("View on Wiki");
            wikiButton.addActionListener(e -> WikiUtil.openWebpage(WikiUtil.getWikiUrl("Map Location", locationName)));
            detailsPanel.add(wikiButton);
        }

        // Make the panel visible and update UI
        detailsPanel.setVisible(true);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
}