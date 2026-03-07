package vue;

import domaine.Controller;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainWindow extends JFrame {
    private final Controller controller;
    private final DrawingPanel drawingPanel;

    // Attributs pour les  propriétés
    private JTextField txtForme;
    private JTextField txtLargeur;
    private JTextField txtHauteur;
    private JTextField txtPosX;
    private JTextField txtPosY;

    // Attribut pour boutons radio
    private ButtonGroup typeGroup;

    public MainWindow() {
        this.controller = new Controller();

        this.setTitle("BâtiBloc - Équipe 05");
        this.setSize(1280, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        this.initComponents();
        this.initMenuBar();
        this.initTopToolBar();

        this.drawingPanel = new DrawingPanel(this);

        JPanel leftPanel = this.buildLeftSideBar();
        JPanel rightPanel = this.buildRightSideBar();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.drawingPanel, rightPanel);
        rightSplit.setResizeWeight(1.0);
        rightSplit.setContinuousLayout(true);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplit);
        mainSplit.setResizeWeight(0.0);
        mainSplit.setContinuousLayout(true);

        this.add(mainSplit, BorderLayout.CENTER);
    }

    public Controller getController() {
        return this.controller;
    }


    public String getFormeSaisie() {
        return this.txtForme.getText();
    }

    public double getLargeurSaisie() {
        try {
            return Double.parseDouble(this.txtLargeur.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getHauteurSaisie() {
        try {
            return Double.parseDouble(this.txtHauteur.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getTypeZoneSelectionne() {
        if (this.typeGroup != null && this.typeGroup.getSelection() != null) {
            return this.typeGroup.getSelection().getActionCommand();
        }
        return "Classique"; // deefaut
    }


    private void initComponents() {
        this.txtForme = new JTextField("Rectangle");
        this.txtForme.setEditable(false);

        this.txtLargeur = this.createNumberField();
        this.txtHauteur = this.createNumberField();

        this.txtPosX = this.createNumberField();
        this.txtPosX.setEditable(false);

        this.txtPosY = this.createNumberField();
        this.txtPosY.setEditable(false);
    }

    private JTextField createNumberField() {
        JTextField field = new JTextField("0.0000");

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    String text = field.getText().replace(',', '.');
                    if (text.isEmpty() || text.equals(".")) {
                        text = "0";
                    }
                    double val = Double.parseDouble(text);
                    field.setText(String.format(java.util.Locale.US, "%.4f", val));
                } catch (NumberFormatException ex) {
                    field.setText("0.0000");
                }
            }
        });

        return field;
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFichier = new JMenu("Fichier");
        menuFichier.add(new JMenuItem("Importer un plan PDF..."));
        menuFichier.add(new JMenuItem("Sauvegarder le projet"));
        menuFichier.addSeparator();
        menuFichier.add(new JMenuItem("Quitter"));
        menuBar.add(menuFichier);
        this.setJMenuBar(menuBar);
    }

    private void initTopToolBar() {
        JToolBar topToolBar = new JToolBar();
        topToolBar.setFloatable(false);
        topToolBar.add(new JButton("Importer un plan PDF"));
        topToolBar.addSeparator();
        topToolBar.add(new JButton("Sauvegarder"));
        topToolBar.addSeparator();

        JButton btnCalculer = new JButton("Calculer l'estimation");
        btnCalculer.addActionListener(e -> this.afficherEstimation());

        topToolBar.add(btnCalculer);
        this.add(topToolBar, BorderLayout.NORTH);
    }

    private void afficherEstimation() {
        double largeur = this.getLargeurSaisie();
        double hauteur = this.getHauteurSaisie();

        // Validation de base pour eviter les calculs non necessaires
        if (largeur <= 0 || hauteur <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir des dimensions valides (strictement supérieures à 0) dans le panneau de droite.",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Appel au controleur pour executer la logique d'affaires
        String resultat = this.controller.simulerPlacement(largeur, hauteur);

        // Affichage du bilan final
        JOptionPane.showMessageDialog(this,
                resultat,
                "Estimation des coûts - BâtiBloc",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildLeftSideBar() {
        JPanel leftSideBar = new JPanel();
        leftSideBar.setLayout(new BoxLayout(leftSideBar, BoxLayout.Y_AXIS));
        leftSideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leftSideBar.add(new JLabel("Sélection de forme :"));
        leftSideBar.add(Box.createVerticalStrut(10));

        Dimension btnSize = new Dimension(180, 35);
        JButton btnRect = new JButton("Rectangle");
        btnRect.setMaximumSize(btnSize);
        JButton btnTri = new JButton("Triangle");
        btnTri.setMaximumSize(btnSize);
        JButton btnTriTronq = new JButton("Triangle tronqué");
        btnTriTronq.setMaximumSize(btnSize);

        btnRect.addActionListener(e -> this.txtForme.setText("Rectangle"));
        btnTri.addActionListener(e -> this.txtForme.setText("Triangle"));
        btnTriTronq.addActionListener(e -> this.txtForme.setText("Triangle tronqué"));

        leftSideBar.add(btnRect);
        leftSideBar.add(Box.createVerticalStrut(5));
        leftSideBar.add(btnTri);
        leftSideBar.add(Box.createVerticalStrut(5));
        leftSideBar.add(btnTriTronq);
        leftSideBar.add(Box.createVerticalStrut(30));

        leftSideBar.add(new JLabel("Type de zone :"));
        leftSideBar.add(Box.createVerticalStrut(10));

        JRadioButton radClassique = new JRadioButton("Armature classique", true);
        radClassique.setActionCommand("Classique");

        JRadioButton radBlocs = new JRadioButton("Armature en blocs");
        radBlocs.setActionCommand("Blocs");

        JRadioButton radOuverture = new JRadioButton("Ouverture (porte/fenêtre)");
        radOuverture.setActionCommand("Ouverture");

        this.typeGroup = new ButtonGroup();
        this.typeGroup.add(radClassique);
        this.typeGroup.add(radBlocs);
        this.typeGroup.add(radOuverture);

        leftSideBar.add(radClassique);
        leftSideBar.add(radBlocs);
        leftSideBar.add(radOuverture);
        leftSideBar.add(Box.createVerticalGlue());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Outils de création"));
        wrapper.setMinimumSize(new Dimension(220, 0));
        wrapper.add(leftSideBar, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildRightSideBar() {
        JPanel rightSideBar = new JPanel(new GridBagLayout());
        rightSideBar.setBorder(BorderFactory.createTitledBorder("Propriétés de la zone"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        this.addFormField(rightSideBar, gbc, 0, "Forme :", this.txtForme);
        this.addFormField(rightSideBar, gbc, 1, "Largeur (m) :", this.txtLargeur);
        this.addFormField(rightSideBar, gbc, 2, "Hauteur (m) :", this.txtHauteur);
        this.addFormField(rightSideBar, gbc, 3, "Position X (m) :", this.txtPosX);
        this.addFormField(rightSideBar, gbc, 4, "Position Y (m) :", this.txtPosY);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton btnSupprimer = new JButton("Supprimer la zone");
        btnSupprimer.setBackground(new Color(220, 53, 69));
        btnSupprimer.setForeground(Color.WHITE);
        rightSideBar.add(btnSupprimer, gbc);

        gbc.gridy = 6;
        gbc.weighty = 1.0;
        rightSideBar.add(Box.createGlue(), gbc);

        rightSideBar.setMinimumSize(new Dimension(260, 0));
        return rightSideBar;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}