package com.slayerhelper.ui.components;

import com.slayerhelper.data.LocationDataLoader;
import com.slayerhelper.domain.Location;
import com.slayerhelper.util.WikiUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Arrays;

public class Tab {
    @Getter
    private final ImageIcon icon;
    @Getter
    private final JPanel content;
    
    private static final LocationDataLoader locationDataLoader = new LocationDataLoader();

    public Tab(ImageIcon icon, String[] content, String type) {
        Arrays.sort(content, (a, b) -> Integer.compare(b.length(), a.length()));
        Objects.requireNonNull(icon, "icon cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(30, 30, 30));
        
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        typeLabel.setForeground(Color.ORANGE);
        typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        contentPanel.add(typeLabel);
        
        for (String s : content) {
            if (type.equalsIgnoreCase("Map Location")) {
                addLocationButton(contentPanel, s);
            } else {
                JLabel label = new JLabel(s);
                label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                label.setForeground(Color.WHITE);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                Container wikiButton = WikiUtil.createLinkButton(s, WikiUtil.getWikiUrl(type, s));
                contentPanel.add(wikiButton);
            }
        }
        
        this.icon = icon;
        this.content = contentPanel;
    }
    
    private JPanel detailsPanel;
    
    private void addLocationButton(JPanel panel, String locationName) {
        // Create details panel if it doesn't exist
        if (detailsPanel == null) {
            detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setBackground(new Color(40, 40, 40));
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            detailsPanel.setVisible(false); // Initially hidden
            detailsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            panel.add(detailsPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing after details panel
        }
        
        JButton locationButton = new JButton(locationName);
        locationButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        locationButton.setForeground(Color.WHITE);
        locationButton.setBackground(new Color(60, 60, 60));
        locationButton.setBorderPainted(false);
        locationButton.setFocusPainted(false);
        locationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        locationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        locationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLocationDetails(locationName);
            }
        });
        
        // Also add wiki link functionality
        locationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) { // Right click
                    WikiUtil.openWebpage(WikiUtil.getWikiUrl("Map Location", locationName));
                }
            }
        });
        
        panel.add(locationButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // Add some spacing
    }
    
    private void displayLocationDetails(String locationName) {
        Location location = locationDataLoader.getLocation(locationName);
        
        // Clear previous content
        detailsPanel.removeAll();
        
        if (location == null) {
            JLabel errorLabel = new JLabel("No additional information available for " + locationName);
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailsPanel.add(errorLabel);
        } else {
            // Add location name
            JLabel nameLabel = new JLabel(location.getName());
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            nameLabel.setForeground(Color.ORANGE);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailsPanel.add(nameLabel);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            // Add cannonable status
            String cannonableText = "Cannonable: " + (location.isCannonable() ? "Yes" : "No");
            JLabel cannonableLabel = new JLabel(cannonableText);
            cannonableLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            cannonableLabel.setForeground(Color.WHITE);
            cannonableLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailsPanel.add(cannonableLabel);
            
            // Add wiki link button
            JButton wikiButton = new JButton("View on Wiki");
            wikiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            wikiButton.addActionListener(e -> WikiUtil.openWebpage(WikiUtil.getWikiUrl("Map Location", locationName)));
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            detailsPanel.add(wikiButton);
        }
        
        // Make the panel visible and revalidate
        detailsPanel.setVisible(true);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

}
