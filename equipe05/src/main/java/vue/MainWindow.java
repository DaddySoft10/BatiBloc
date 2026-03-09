package vue;

import domaine.Controller;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {
    private final Controller controller;
    private final DrawingPanel drawingPanel;

    // Attributs pour les proprietes
    private JTextField txtForme;
    private JTextField txtLargeur;
    private JTextField txtHauteur;
    private JTextField txtPosX;
    private JTextField txtPosY;
    private JTextField txtNomNouvelleVue;

    // Attribut pour boutons radio
    private ButtonGroup typeGroup;
    private DefaultListModel<String> listeVuesModel;
    private JList<String> listeVues;
    private JLabel lblVueCourante;

    public MainWindow() {
        this.controller = new Controller();

        this.setTitle("BatiBloc - Equipe 05");
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
        return "Classique";
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
        this.txtNomNouvelleVue = new JTextField("Vue rognee");

        this.listeVuesModel = new DefaultListModel<>();
        this.listeVues = new JList<>(this.listeVuesModel);
        this.listeVues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listeVues.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int indexSelectionne = listeVues.getSelectedIndex();
                if (indexSelectionne >= 0) {
                    controller.selectionnerVue(indexSelectionne);
                    mettreAJourVueCourante();
                    if (drawingPanel != null) {
                        drawingPanel.repaint();
                    }
                }
            }
        });

        this.lblVueCourante = new JLabel("Vue courante: Aucune");
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

        JMenuItem itemImporter = new JMenuItem("Importer un plan PDF...");
        itemImporter.addActionListener(e -> this.importerPlanPdf());

        JMenuItem itemSauvegarder = new JMenuItem("Sauvegarder le projet");
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> this.dispose());

        menuFichier.add(itemImporter);
        menuFichier.add(itemSauvegarder);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);

        menuBar.add(menuFichier);
        this.setJMenuBar(menuBar);
    }

    private void initTopToolBar() {
        JToolBar topToolBar = new JToolBar();
        topToolBar.setFloatable(false);

        JButton btnImporter = new JButton("Importer un plan PDF");
        btnImporter.addActionListener(e -> this.importerPlanPdf());

        topToolBar.add(btnImporter);
        topToolBar.addSeparator();
        topToolBar.add(new JButton("Sauvegarder"));
        topToolBar.addSeparator();

        JButton btnCalculer = new JButton("Calculer l'estimation");
        btnCalculer.addActionListener(e -> this.afficherEstimation());
        JButton btnZoomPlus = new JButton("Zoom +");
        btnZoomPlus.addActionListener(e -> this.drawingPanel.zoomerDepuisCentre(1.15));
        JButton btnZoomMoins = new JButton("Zoom -");
        btnZoomMoins.addActionListener(e -> this.drawingPanel.zoomerDepuisCentre(1.0 / 1.15));
        JButton btnRecentrer = new JButton("Recentrer");
        btnRecentrer.addActionListener(e -> this.drawingPanel.reinitialiserVue());

        topToolBar.add(btnCalculer);
        topToolBar.addSeparator();
        topToolBar.add(btnZoomPlus);
        topToolBar.add(btnZoomMoins);
        topToolBar.add(btnRecentrer);
        this.add(topToolBar, BorderLayout.NORTH);
    }

    private void importerPlanPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selectionner un plan PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf"));

        int resultat = fileChooser.showOpenDialog(this);
        if (resultat != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fichierSelectionne = fileChooser.getSelectedFile();

        try {
            int nombrePages = this.controller.importerPlanPdf(fichierSelectionne.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Importation reussie: " + nombrePages + " page(s) detectee(s).",
                    "Import PDF",
                    JOptionPane.INFORMATION_MESSAGE);
            this.rafraichirVuesDuPlan();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Echec de l'importation PDF: " + ex.getMessage(),
                    "Erreur import PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherEstimation() {
        double largeur = this.getLargeurSaisie();
        double hauteur = this.getHauteurSaisie();

        // Validation de base pour eviter les calculs non necessaires
        if (largeur <= 0 || hauteur <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir des dimensions valides (strictement superieures a 0) dans le panneau de droite.",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Appel au controleur pour executer la logique d'affaires
        String resultat = this.controller.simulerPlacement(largeur, hauteur);

        // Affichage du bilan final
        JOptionPane.showMessageDialog(this,
                resultat,
                "Estimation des couts - BatiBloc",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildLeftSideBar() {
        JPanel leftSideBar = new JPanel();
        leftSideBar.setLayout(new BoxLayout(leftSideBar, BoxLayout.Y_AXIS));
        leftSideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leftSideBar.add(new JLabel("Selection de forme :"));
        leftSideBar.add(Box.createVerticalStrut(10));

        Dimension btnSize = new Dimension(260, 35);
        JButton btnRect = new JButton("Rectangle");
        btnRect.setMaximumSize(btnSize);
        JButton btnTri = new JButton("Triangle");
        btnTri.setMaximumSize(btnSize);
        JButton btnTriTronq = new JButton("Triangle tronque");
        btnTriTronq.setMaximumSize(btnSize);

        btnRect.addActionListener(e -> this.txtForme.setText("Rectangle"));
        btnTri.addActionListener(e -> this.txtForme.setText("Triangle"));
        btnTriTronq.addActionListener(e -> this.txtForme.setText("Triangle tronque"));

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

        JRadioButton radOuverture = new JRadioButton("Ouverture (porte/fenetre)");
        radOuverture.setActionCommand("Ouverture");

        this.typeGroup = new ButtonGroup();
        this.typeGroup.add(radClassique);
        this.typeGroup.add(radBlocs);
        this.typeGroup.add(radOuverture);

        leftSideBar.add(radClassique);
        leftSideBar.add(radBlocs);
        leftSideBar.add(radOuverture);
        leftSideBar.add(Box.createVerticalStrut(20));
        leftSideBar.add(new JLabel("Vues importees :"));
        leftSideBar.add(Box.createVerticalStrut(10));

        JScrollPane scrollVues = new JScrollPane(this.listeVues);
        scrollVues.setPreferredSize(new Dimension(260, 170));
        scrollVues.setMaximumSize(new Dimension(260, 170));
        leftSideBar.add(scrollVues);
        leftSideBar.add(Box.createVerticalStrut(8));
        JButton btnSupprimerVue = new JButton("Supprimer la vue");
        btnSupprimerVue.setMaximumSize(new Dimension(260, 35));
        btnSupprimerVue.addActionListener(e -> this.supprimerVueSelectionnee());
        leftSideBar.add(btnSupprimerVue);

        leftSideBar.add(Box.createVerticalStrut(10));
        leftSideBar.add(new JLabel("Rognage :"));
        leftSideBar.add(Box.createVerticalStrut(6));
        JToggleButton btnModeRognage = new JToggleButton("Mode rognage");
        btnModeRognage.setMaximumSize(new Dimension(260, 35));
        btnModeRognage.addActionListener(e -> this.drawingPanel.setModeRognageActif(btnModeRognage.isSelected()));
        leftSideBar.add(btnModeRognage);
        leftSideBar.add(Box.createVerticalStrut(6));
        JButton btnRognerVue = new JButton("Rogner vue courante");
        btnRognerVue.setMaximumSize(new Dimension(260, 35));
        btnRognerVue.addActionListener(e -> this.rognerVueCouranteDepuisSelection());
        leftSideBar.add(btnRognerVue);
        leftSideBar.add(Box.createVerticalStrut(6));
        this.txtNomNouvelleVue.setMaximumSize(new Dimension(260, 30));
        leftSideBar.add(this.txtNomNouvelleVue);
        leftSideBar.add(Box.createVerticalStrut(6));
        JButton btnCreerVueRognee = new JButton("Creer nouvelle vue rognee");
        btnCreerVueRognee.setMaximumSize(new Dimension(260, 35));
        btnCreerVueRognee.addActionListener(e -> this.creerNouvelleVueRogneeDepuisSelection());
        leftSideBar.add(btnCreerVueRognee);
        leftSideBar.add(Box.createVerticalStrut(8));
        leftSideBar.add(this.lblVueCourante);
        leftSideBar.add(Box.createVerticalGlue());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Outils de creation"));
        wrapper.setMinimumSize(new Dimension(320, 0));
        wrapper.setPreferredSize(new Dimension(340, 0));
        wrapper.add(leftSideBar, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildRightSideBar() {
        JPanel rightSideBar = new JPanel(new GridBagLayout());
        rightSideBar.setBorder(BorderFactory.createTitledBorder("Proprietes de la zone"));
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

    private void rafraichirVuesDuPlan() {
        this.listeVuesModel.clear();
        for (String vue : this.controller.getVuesDuPlan()) {
            this.listeVuesModel.addElement(vue);
        }

        int indexCourant = this.controller.getIndexVueCourante();
        if (indexCourant >= 0 && indexCourant < this.listeVuesModel.size()) {
            this.listeVues.setSelectedIndex(indexCourant);
        }

        this.mettreAJourVueCourante();
    }

    private void mettreAJourVueCourante() {
        String nomVue = this.controller.getNomVueCourante();
        if (nomVue == null || nomVue.isBlank()) {
            this.lblVueCourante.setText("Vue courante: Aucune");
            return;
        }
        this.lblVueCourante.setText("Vue courante: " + nomVue);
    }

    private void supprimerVueSelectionnee() {
        int indexSelectionne = this.listeVues.getSelectedIndex();
        if (indexSelectionne < 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez selectionner une vue a supprimer.",
                    "Suppression de vue",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomVue = this.listeVuesModel.getElementAt(indexSelectionne);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Supprimer " + nomVue + " ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            this.controller.supprimerVue(indexSelectionne);
            this.rafraichirVuesDuPlan();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Suppression impossible: " + ex.getMessage(),
                    "Erreur suppression",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rognerVueCouranteDepuisSelection() {
        Rectangle zoneSelectionnee = this.drawingPanel.getSelectionRognageImage();
        if (zoneSelectionnee == null) {
            JOptionPane.showMessageDialog(this,
                    "Activez le mode rognage et selectionnez une zone.",
                    "Rognage",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            this.controller.rognerVueCourante(
                    zoneSelectionnee.x,
                    zoneSelectionnee.y,
                    zoneSelectionnee.width,
                    zoneSelectionnee.height
            );
            this.drawingPanel.effacerSelectionRognage();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    "Rognage impossible: " + ex.getMessage(),
                    "Erreur rognage",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void creerNouvelleVueRogneeDepuisSelection() {
        Rectangle zoneSelectionnee = this.drawingPanel.getSelectionRognageImage();
        if (zoneSelectionnee == null) {
            JOptionPane.showMessageDialog(this,
                    "Activez le mode rognage et selectionnez une zone.",
                    "Creation vue rognee",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomVue = this.txtNomNouvelleVue.getText();
        if (nomVue == null || nomVue.isBlank()) {
            nomVue = "Vue " + (this.controller.getNombreVues() + 1);
        }

        try {
            this.controller.ajouterVueRognee(
                    zoneSelectionnee.x,
                    zoneSelectionnee.y,
                    zoneSelectionnee.width,
                    zoneSelectionnee.height,
                    nomVue
            );
            this.rafraichirVuesDuPlan();
            this.drawingPanel.effacerSelectionRognage();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    "Creation impossible: " + ex.getMessage(),
                    "Erreur creation vue",
                    JOptionPane.ERROR_MESSAGE);
        }
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
