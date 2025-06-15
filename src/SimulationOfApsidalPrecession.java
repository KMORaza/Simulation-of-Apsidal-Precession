import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationOfApsidalPrecession extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 900;
    
    // Simulation parameters for orbital mechanics
    private double semiMajorAxis = 150; // Semi-major axis of the orbit in pixels (represents orbital size)
    private double eccentricity = 0.6;  // Orbital eccentricity (0 = circle, <1 = ellipse)
    private double precessionRate = 0.5; // Base precession rate in degrees per orbit
    private double angle = 0;           // Current angular position in orbit (radians)
    private double precessionAngle = 0; // Accumulated precession angle (radians)
    private double relativisticFactor = 0.1; // Contribution to precession from general relativity
    private double oblatenessFactor = 0.05; // Contribution to precession from central body oblateness
    private double thirdBodyInfluence = 0.0; // Contribution to precession from a third body
    private boolean showRelativity = true;   // Toggle relativistic precession effect
    private boolean showOblateness = true;   // Toggle oblateness precession effect
    private boolean showThirdBody = false;   // Toggle third body perturbation effect
    
    // History tracking for visualizing orbital path
    private List<Point2D.Double> orbitHistory = new ArrayList<>();
    private static final int HISTORY_LENGTH = 500; // Maximum points in orbit trail
    
    // UI components
    private JPanel displayPanel;
    private JSlider eccentricitySlider;
    private JSlider precessionSlider;
    private JSlider relativitySlider;
    private JSlider oblatenessSlider;
    private JSlider thirdBodySlider;
    private JButton startStopButton;
    private JCheckBox relativityCheckbox;
    private JCheckBox oblatenessCheckbox;
    private JCheckBox thirdBodyCheckbox;
    private JComboBox<String> bodySelector;
    private boolean isRunning = false;
    
    public SimulationOfApsidalPrecession() {
        try {
            // Set Windows Classic look and feel for consistent UI
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("Apsidal Precession Simulation");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        createEnhancedUI();
        
        // Timer for real-time orbital animation
        Timer timer = new Timer(30, e -> {
            if (isRunning) {
                // Increment orbital angle (simulates orbital motion)
                angle += 0.02;
                
                // Calculate total precession rate from combined physical effects
                // Precession is the rotation of the orbit's major axis
                double totalPrecession = precessionRate / 100; // Base precession (arbitrary for simulation)
                if (showRelativity) totalPrecession += relativisticFactor / 500; // General relativistic effect (e.g., Mercury's perihelion advance)
                if (showOblateness) totalPrecession += oblatenessFactor / 400;   // Precession due to central body's oblateness (e.g., J2 perturbation)
                if (showThirdBody) totalPrecession += thirdBodyInfluence / 300;  // Perturbation from a third body (e.g., external gravitational influence)
                
                // Update accumulated precession angle
                precessionAngle += totalPrecession;
                
                // Record current position for orbit trail
                Point2D.Double currentPos = calculateOrbitPosition();
                orbitHistory.add(currentPos);
                if (orbitHistory.size() > HISTORY_LENGTH) {
                    orbitHistory.remove(0);
                }
                
                displayPanel.repaint();
            }
        });
        timer.start();
    }
    
    // Calculate the position of the orbiting body using elliptical orbit equations
    private Point2D.Double calculateOrbitPosition() {
        int centerX = displayPanel.getWidth() / 2;
        int centerY = displayPanel.getHeight() / 2;
        // Calculate semi-minor axis based on eccentricity (b = a * sqrt(1 - e^2))
        double semiMinorAxis = semiMajorAxis * Math.sqrt(1 - eccentricity * eccentricity);
        
        // Parametric equations for an ellipse centered at focus (star)
        // x = a * cos(θ) * (1 - e), y = b * sin(θ)
        double x = centerX + semiMajorAxis * Math.cos(angle) * (1 - eccentricity);
        double y = centerY + semiMinorAxis * Math.sin(angle);
        
        // Apply rotation matrix to account for apsidal precession
        // Rotates the orbit around the central body by precessionAngle
        double rotatedX = centerX + (x - centerX) * Math.cos(precessionAngle) - (y - centerY) * Math.sin(precessionAngle);
        double rotatedY = centerY + (x - centerX) * Math.sin(precessionAngle) + (y - centerY) * Math.cos(precessionAngle);
        
        return new Point2D.Double(rotatedX, rotatedY);
    }
    
    private void createEnhancedUI() {
        // Main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create display panel for rendering the orbit
        displayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw dark display background
                g2d.setColor(new Color(20, 20, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                // Draw grid lines for reference
                drawGrid(g2d, centerX, centerY);
                
                // Draw the elliptical orbit and orbiting body
                drawOrbit(g2d, centerX, centerY);
                
                // Draw historical orbit trail
                drawHistoryTrail(g2d);
                
                // Draw UI elements (readouts, legend)
                drawDisplayElements(g2d);
                
                // Draw third body if enabled
                if (showThirdBody) {
                    drawThirdBody(g2d, centerX, centerY);
                }
            }
            
            private void drawGrid(Graphics2D g2d, int centerX, int centerY) {
                g2d.setColor(new Color(50, 50, 70));
                
                // Draw crosshair for central body position
                g2d.drawLine(centerX, 0, centerX, getHeight());
                g2d.drawLine(0, centerY, getWidth(), centerY);
                
                // Draw polar grid for scale reference
                g2d.setColor(new Color(50, 50, 70, 100));
                int maxRadius = Math.min(getWidth(), getHeight()) / 2;
                for (int r = 50; r < maxRadius; r += 50) {
                    g2d.drawOval(centerX - r, centerY - r, r * 2, r * 2);
                }
            }
            
            private void drawOrbit(Graphics2D g2d, int centerX, int centerY) {
                // Calculate semi-minor axis for elliptical orbit
                double semiMinorAxis = semiMajorAxis * Math.sqrt(1 - eccentricity * eccentricity);
                
                // Save original transform for precession rotation
                AffineTransform oldTransform = g2d.getTransform();
                
                // Apply precession rotation to orbit
                g2d.rotate(precessionAngle, centerX, centerY);
                
                // Draw orbit path (ellipse)
                g2d.setColor(new Color(0, 180, 255, 100));
                g2d.draw(new Ellipse2D.Double(
                    centerX - semiMajorAxis, 
                    centerY - semiMinorAxis, 
                    semiMajorAxis * 2, 
                    semiMinorAxis * 2
                ));
                
                // Draw central body (star) at focus
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(centerX - 10, centerY - 10, 20, 20);
                
                // Draw oblate central body if enabled
                // Oblateness modeled as a flattened ellipsoid
                if (showOblateness) {
                    g2d.setColor(new Color(255, 255, 0, 100));
                    int oblateWidth = 20 + (int)(oblatenessFactor * 10);
                    int oblateHeight = 20 - (int)(oblatenessFactor * 5);
                    g2d.fillOval(centerX - oblateWidth/2, centerY - oblateHeight/2, oblateWidth, oblateHeight);
                }
                
                // Draw orbiting body at current position
                Point2D.Double orbitPos = calculateOrbitPosition();
                g2d.setColor(Color.CYAN);
                g2d.fillOval((int)orbitPos.x - 6, (int)orbitPos.y - 6, 12, 12);
                
                // Draw apsides line (line connecting periapsis and apoapsis)
                g2d.setColor(new Color(255, 100, 100, 150));
                g2d.drawLine(
                    centerX - (int)(semiMajorAxis * (1 + eccentricity)), // Periapsis
                    centerY, 
                    centerX + (int)(semiMajorAxis * (1 - eccentricity)), // Apoapsis
                    centerY
                );
                
                // Draw periapsis and apoapsis markers
                g2d.setColor(Color.RED);
                g2d.fillOval(centerX - (int)(semiMajorAxis * (1 + eccentricity)) - 4, centerY - 4, 8, 8);
                g2d.setColor(Color.GREEN);
                g2d.fillOval(centerX + (int)(semiMajorAxis * (1 - eccentricity)) - 4, centerY - 4, 8, 8);
                
                // Restore original transform
                g2d.setTransform(oldTransform);
            }
            
            private void drawHistoryTrail(Graphics2D g2d) {
                if (orbitHistory.size() < 2) return;
                
                // Draw trail to show orbital path history
                g2d.setColor(new Color(0, 200, 255, 150));
                for (int i = 1; i < orbitHistory.size(); i++) {
                    Point2D.Double p1 = orbitHistory.get(i-1);
                    Point2D.Double p2 = orbitHistory.get(i);
                    g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                }
            }
            
            private void drawThirdBody(Graphics2D g2d, int centerX, int centerY) {
                // Simulate third body at a fixed distance with independent orbit
                double thirdBodyDist = 250;
                double thirdBodyAngle = angle * 0.3; // Slower rotation for third body
                
                int thirdBodyX = centerX + (int)(thirdBodyDist * Math.cos(thirdBodyAngle));
                int thirdBodyY = centerY + (int)(thirdBodyDist * Math.sin(thirdBodyAngle));
                
                // Draw third body
                g2d.setColor(new Color(255, 150, 0));
                g2d.fillOval(thirdBodyX - 8, thirdBodyY - 8, 16, 16);
                
                // Draw gravitational influence line to orbiting body
                g2d.setColor(new Color(255, 150, 0, 100));
                Point2D.Double orbitPos = calculateOrbitPosition();
                g2d.drawLine(thirdBodyX, thirdBodyY, (int)orbitPos.x, (int)orbitPos.y);
            }
            
            private void drawDisplayElements(Graphics2D g2d) {
                // Draw border with inset effect
                g2d.setColor(new Color(60, 60, 80));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2d.setColor(new Color(40, 40, 60));
                g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                
                // Draw parameter readouts
                g2d.setColor(new Color(0, 200, 255));
                g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
                g2d.drawString(String.format("Eccentricity: %.2f", eccentricity), 20, 60);
                g2d.drawString(String.format("Total Precession: %.2f°/orbit", 
                    precessionRate + (showRelativity ? relativisticFactor : 0) + 
                    (showOblateness ? oblatenessFactor : 0) + 
                    (showThirdBody ? thirdBodyInfluence : 0)), 20, 80);
                g2d.drawString(String.format("Current Angle: %.1f°", Math.toDegrees(precessionAngle)), 20, 100);
                g2d.drawString(String.format("Relativity Effect: %.2f", showRelativity ? relativisticFactor : 0), 20, 120);
                g2d.drawString(String.format("Oblateness Effect: %.2f", showOblateness ? oblatenessFactor : 0), 20, 140);
                g2d.drawString(String.format("Third Body Effect: %.2f", showThirdBody ? thirdBodyInfluence : 0), 20, 160);
                
                // Draw status indicator
                g2d.setColor(isRunning ? new Color(0, 255, 0) : new Color(255, 0, 0));
                g2d.fillOval(getWidth() - 30, 20, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawString(isRunning ? "RUNNING" : "STOPPED", getWidth() - 100, 30);
                
                // Draw legend
                g2d.setColor(Color.RED);
                g2d.drawString("Periapsis", getWidth() - 120, getHeight() - 60);
                g2d.setColor(Color.GREEN);
                g2d.drawString("Apoapsis", getWidth() - 120, getHeight() - 40);
                g2d.setColor(new Color(255, 150, 0));
                g2d.drawString("Third Body", getWidth() - 120, getHeight() - 20);
            }
        };
        displayPanel.setPreferredSize(new Dimension(WIDTH - 40, HEIGHT - 200));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBackground(new Color(40, 40, 50));
        controlPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 100), 2),
            "SIMULATION CONTROLS",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Bahnschrift", Font.BOLD, 12),
            new Color(200, 200, 255)
        ));
        
        // Upper control panel for sliders
        JPanel upperControlPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        upperControlPanel.setBackground(new Color(40, 40, 50));
        
        // Lower control panel for checkboxes and buttons
        JPanel lowerControlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        lowerControlPanel.setBackground(new Color(40, 40, 50));
        
        // Eccentricity slider (controls orbit shape)
        eccentricitySlider = createStyledSlider(0, 90, (int)(eccentricity * 100));
        eccentricitySlider.addChangeListener(e -> {
            eccentricity = eccentricitySlider.getValue() / 100.0;
            displayPanel.repaint();
        });
        
        // Precession rate slider (base apsidal precession)
        precessionSlider = createStyledSlider(0, 100, (int)(precessionRate * 10));
        precessionSlider.addChangeListener(e -> {
            precessionRate = precessionSlider.getValue() / 10.0;
            displayPanel.repaint();
        });
        
        // Relativity slider (general relativistic precession)
        relativitySlider = createStyledSlider(0, 100, (int)(relativisticFactor * 10));
        relativitySlider.addChangeListener(e -> {
            relativisticFactor = relativitySlider.getValue() / 10.0;
            displayPanel.repaint();
        });
        
        // Oblateness slider (J2 perturbation effect)
        oblatenessSlider = createStyledSlider(0, 100, (int)(oblatenessFactor * 10));
        oblatenessSlider.addChangeListener(e -> {
            oblatenessFactor = oblatenessSlider.getValue() / 10.0;
            displayPanel.repaint();
        });
        
        // Third body influence slider (external gravitational perturbation)
        thirdBodySlider = createStyledSlider(0, 100, (int)(thirdBodyInfluence * 10));
        thirdBodySlider.addChangeListener(e -> {
            thirdBodyInfluence = thirdBodySlider.getValue() / 10.0;
            displayPanel.repaint();
        });
        
        // Checkboxes for toggling physical effects
        relativityCheckbox = createStyledCheckbox("Relativity", showRelativity);
        relativityCheckbox.addActionListener(e -> {
            showRelativity = relativityCheckbox.isSelected();
            displayPanel.repaint();
        });
        
        oblatenessCheckbox = createStyledCheckbox("Oblateness", showOblateness);
        oblatenessCheckbox.addActionListener(e -> {
            showOblateness = oblatenessCheckbox.isSelected();
            displayPanel.repaint();
        });
        
        thirdBodyCheckbox = createStyledCheckbox("Third Body", showThirdBody);
        thirdBodyCheckbox.addActionListener(e -> {
            showThirdBody = thirdBodyCheckbox.isSelected();
            displayPanel.repaint();
        });
        
        // Body selector with preset orbital parameters
        String[] bodies = {"Mercury", "Earth", "Binary Star", "Exoplanet"};
        bodySelector = new JComboBox<>(bodies);
        bodySelector.setBackground(new Color(60, 60, 80));
        bodySelector.setForeground(Color.WHITE);
        bodySelector.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        bodySelector.addActionListener(e -> {
            String selected = (String)bodySelector.getSelectedItem();
            switch(selected) {
                case "Mercury":
                    // Mercury's orbit: high eccentricity, significant relativistic precession
                    eccentricity = 0.2056;
                    precessionRate = 5.74; // arcseconds per century converted
                    relativisticFactor = 4.3;
                    oblatenessFactor = 0.0;
                    break;
                case "Earth":
                    // Earth's orbit: low eccentricity, small relativistic effect
                    eccentricity = 0.0167;
                    precessionRate = 1.72;
                    relativisticFactor = 0.1;
                    oblatenessFactor = 0.5;
                    break;
                case "Binary Star":
                    // Binary star: high eccentricity, strong relativistic effects
                    eccentricity = 0.8;
                    precessionRate = 15.0;
                    relativisticFactor = 8.0;
                    oblatenessFactor = 2.0;
                    break;
                case "Exoplanet":
                    // Exoplanet: arbitrary parameters for demonstration
                    eccentricity = 0.5;
                    precessionRate = 10.0;
                    relativisticFactor = 3.0;
                    oblatenessFactor = 1.0;
                    break;
            }
            updateSliders();
            displayPanel.repaint();
        });
        
        // Start/Stop button for animation control
        startStopButton = new JButton("START/STOP");
        startStopButton.setBackground(new Color(60, 60, 80));
        startStopButton.setForeground(Color.WHITE);
        startStopButton.setFont(new Font("Bahnschrift", Font.BOLD, 14));
        startStopButton.setFocusPainted(false);
        startStopButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        startStopButton.addActionListener(e -> {
            isRunning = !isRunning;
            displayPanel.repaint();
        });
        
        // Reset button to clear orbit history
        JButton resetButton = new JButton("RESET");
        resetButton.setBackground(new Color(60, 60, 80));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Bahnschrift", Font.BOLD, 14));
        resetButton.setFocusPainted(false);
        resetButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        resetButton.addActionListener(e -> {
            angle = 0;
            precessionAngle = 0;
            orbitHistory.clear();
            displayPanel.repaint();
        });
        
        // Add components to control panels
        upperControlPanel.add(createSliderPanel("ECCENTRICITY", eccentricitySlider));
        upperControlPanel.add(createSliderPanel("BASE PRECESSION", precessionSlider));
        upperControlPanel.add(createSliderPanel("RELATIVITY", relativitySlider));
        upperControlPanel.add(createSliderPanel("OBLATENESS", oblatenessSlider));
        upperControlPanel.add(createSliderPanel("THIRD BODY", thirdBodySlider));
        
        lowerControlPanel.add(relativityCheckbox);
        lowerControlPanel.add(oblatenessCheckbox);
        lowerControlPanel.add(thirdBodyCheckbox);
        lowerControlPanel.add(createControlPanel("PRESETS", bodySelector));
        lowerControlPanel.add(startStopButton);
        lowerControlPanel.add(resetButton);
        
        // Add to main control panel
        controlPanel.add(upperControlPanel);
        controlPanel.add(lowerControlPanel);
        
        // Add to main panel
        mainPanel.add(displayPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JSlider createStyledSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(new Color(40, 40, 50));
        slider.setForeground(new Color(0, 200, 255));
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing((max - min) / 5);
        slider.setPaintLabels(true);
        slider.setFont(new Font("Bahnschrift", Font.PLAIN, 10));
        return slider;
    }
    
    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox checkbox = new JCheckBox(text, selected);
        checkbox.setBackground(new Color(40, 40, 50));
        checkbox.setForeground(new Color(200, 200, 255));
        checkbox.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        checkbox.setFocusPainted(false);
        return checkbox;
    }
    
    private JPanel createSliderPanel(String title, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 50));
        
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(new Color(200, 200, 255));
        label.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlPanel(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 50));
        
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(new Color(200, 200, 255));
        label.setFont(new Font("Bahnschrift", Font.BOLD, 12));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateSliders() {
        eccentricitySlider.setValue((int)(eccentricity * 100));
        precessionSlider.setValue((int)(precessionRate * 10));
        relativitySlider.setValue((int)(relativisticFactor * 10));
        oblatenessSlider.setValue((int)(oblatenessFactor * 10));
        thirdBodySlider.setValue((int)(thirdBodyInfluence * 10));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationOfApsidalPrecession simulator = new SimulationOfApsidalPrecession();
            simulator.setVisible(true);
        });
    }
}