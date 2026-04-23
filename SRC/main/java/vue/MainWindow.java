package vue;

import domaine.Controller;
import domaine.ImperialParser;
import dto.ZoneDTO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
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
    private JTextField txtEchelle;
    private JTextField txtNombreBlocs;
    private JTextField txtPrixParBloc;
    private JLabel lblNombreBlocs;
    private JTextArea txtResultatEstimation;

    // Attribut pour boutons radio
    private ButtonGroup typeGroup;
    private DefaultListModel<String> listeVuesModel;
    private JList<String> listeVues;
    private JLabel lblVueCourante;
    private JButton btnSupprimerZone;
    private StatusBar statusBar;

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
        this.drawingPanel.setModeActuel(ModeInteraction.SELECTION);
        this.installerRaccourciSuppressionZone();

        JPanel leftPanel = this.buildLeftSideBar();
        JPanel rightPanel = this.buildRightSideBar();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.drawingPanel, rightPanel);
        rightSplit.setResizeWeight(1.0);
        rightSplit.setContinuousLayout(true);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplit);
        mainSplit.setResizeWeight(0.0);
        mainSplit.setContinuousLayout(true);

        this.add(mainSplit, BorderLayout.CENTER);

        this.statusBar = new StatusBar();
        this.add(this.statusBar, BorderLayout.SOUTH);

        this.setupUndoRedoKeybinds();
    }

    private void setupUndoRedoKeybinds() {
        KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(undoKey, "undo");
        this.getRootPane().getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.annulerAction();
                drawingPanel.repaint();
                rafraichirPanneauDroit();
                statusBar.setMessage("Action annulee.");
            }
        });

        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(redoKey, "redo");
        this.getRootPane().getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.refaireAction();
                drawingPanel.repaint();
                rafraichirPanneauDroit();
                statusBar.setMessage("Action refaite.");
            }
        });
    }

    public Controller getController() {
        return this.controller;
    }

    public String getFormeSaisie() {
        return this.txtForme.getText();
    }

    public double getLargeurSaisie() {
        try {
            return ImperialParser.parsePouces(this.txtLargeur.getText());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getHauteurSaisie() {
        try {
            return ImperialParser.parsePouces(this.txtHauteur.getText());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getTypeZoneSelectionne() {
        if (this.typeGroup != null && this.typeGroup.getSelection() != null) {
            return this.typeGroup.getSelection().getActionCommand();
        }
        return "Classique";
    }

    public void rafraichirPanneauDroit() {
        ZoneDTO zoneSelectionnee = this.controller.getZoneSelectionnee();
        this.mettreAJourChampEchelle();
        if (zoneSelectionnee == null) {
            this.txtForme.setText("");
            this.txtLargeur.setText("0'0.0\"");
            this.txtHauteur.setText("0'0.0\"");
            this.txtPosX.setText("0'0.0\"");
            this.txtPosY.setText("0'0.0\"");
            this.lblNombreBlocs.setText(
                    "Nombre de Blocs Total : " + this.controller.getNombreTotalBlocs());

            if (this.btnSupprimerZone != null) {
                this.btnSupprimerZone.setEnabled(false);
            }
            return;
        }

        this.txtForme.setText(formaterTypeForme(zoneSelectionnee.getTypeForme()));
        this.txtLargeur.setText(ImperialParser.formatterImperialCourt(zoneSelectionnee.getLargeur()));
        this.txtHauteur.setText(ImperialParser.formatterImperialCourt(zoneSelectionnee.getHauteur()));
        this.txtPosX.setText(ImperialParser.formatterImperialCourt(zoneSelectionnee.getX()));
        this.txtPosY.setText(ImperialParser.formatterImperialCourt(zoneSelectionnee.getY()));
        if (this.btnSupprimerZone != null) {
            this.btnSupprimerZone.setEnabled(true);
        }

        String msg = this.controller.getDernierMessage();
        if (msg != null && !msg.isEmpty() && this.statusBar != null) {
            this.statusBar.setMessage(msg);
        }
    }

    public void mettreAJourCoordonnees(double x, double y) {
        if (this.statusBar != null) {
            this.statusBar.setCoordonnees(String.format("x: %s  y: %s",
                    ImperialParser.formatterImperialCourt(x),
                    ImperialParser.formatterImperialCourt(y)));
        }
    }

    private void initComponents() {
        this.txtForme = new JTextField("Rectangle");
        this.txtForme.setEditable(false);

        this.txtLargeur = this.createImperialField();
        this.txtLargeur.setEditable(true);

        this.txtHauteur = this.createImperialField();
        this.txtHauteur.setEditable(true);

        this.txtPosX = this.createImperialField();
        this.txtPosX.setEditable(true);

        this.txtPosY = this.createImperialField();
        this.txtPosY.setEditable(true);

        this.txtEchelle = this.createNumberField();
        this.txtEchelle.setEditable(true);

        double poucesParPixel = this.controller.getEchellePoucesParPixel();
        this.txtEchelle.setText(String.format(java.util.Locale.US, "%.6f", poucesParPixel));

        this.txtNombreBlocs = new JTextField();
        this.txtNombreBlocs.setEditable(false);
        this.txtNombreBlocs.setText("0");

        this.txtPrixParBloc = new JTextField("20.0");
        this.txtPrixParBloc.setEditable(true);
        this.txtPrixParBloc.addActionListener(e -> this.mettreAJourNombreTotalBlocs());
        this.txtPrixParBloc.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                mettreAJourNombreTotalBlocs();
            }
        });

        this.lblNombreBlocs = new JLabel("Nombre de Blocs Total : 0");

        this.txtResultatEstimation = new JTextArea(10, 20);
        this.txtResultatEstimation.setEditable(false);
        this.txtResultatEstimation.setFont(new Font("Monospaced", Font.PLAIN, 11));
        this.txtResultatEstimation.setLineWrap(true);
        this.txtResultatEstimation.setWrapStyleWord(true);

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

    private JTextField createImperialField() {
        JTextField field = new JTextField("0'0.0\"");

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != '\'' && c != '"' && c != ' ' && c != '/'
                        && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                    field.setText("0'0.0\"");
                    return;
                }
                try {
                    double val = ImperialParser.parsePouces(text);
                    field.setText(ImperialParser.formatterImperialCourt(val));
                } catch (Exception ex) {
                    field.setText("0'0.0\"");
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

        JMenuItem itemOuvrir = new JMenuItem("Ouvrir un projet...");
        itemOuvrir.addActionListener(e -> this.ouvrirProjet());

        JMenuItem itemSauvegarder = new JMenuItem("Sauvegarder le projet");
        itemSauvegarder.addActionListener(e -> this.sauvegarderProjet());

        JMenuItem itemExporterVue = new JMenuItem("Exporter la vue courante (PNG)...");
        itemExporterVue.addActionListener(e -> this.exporterVueCourantePNG());

        JMenuItem itemExporterToutes = new JMenuItem("Exporter toutes les vues (PNG)...");
        itemExporterToutes.addActionListener(e -> this.exporterToutesLesVuesPNG());

        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> this.dispose());

        menuFichier.add(itemImporter);
        menuFichier.add(itemOuvrir);
        menuFichier.add(itemSauvegarder);
        menuFichier.addSeparator();
        menuFichier.add(itemExporterVue);
        menuFichier.add(itemExporterToutes);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);

        menuBar.add(menuFichier);
        this.setJMenuBar(menuBar);
    }

    private void initTopToolBar() {
        JToolBar topToolBar = new JToolBar();
        topToolBar.setFloatable(false);

        JToggleButton btnTronquage = new JToggleButton("Tronquer triangle");
        btnTronquage.addActionListener(e -> this.drawingPanel.setModeActuel(ModeInteraction.TRONQUAGE));

        JButton btnZoomPlus = new JButton("Zoom +");
        btnZoomPlus.addActionListener(e -> {
            this.drawingPanel.zoomerDepuisCentre(1.15);
            this.mettreAJourChampEchelle();
        });

        JButton btnZoomMoins = new JButton("Zoom -");
        btnZoomMoins.addActionListener(e -> {
            this.drawingPanel.zoomerDepuisCentre(1.0 / 1.15);
            this.mettreAJourChampEchelle();
        });

        JButton btnRecentrer = new JButton("Recentrer");
        btnRecentrer.addActionListener(e -> {
            this.drawingPanel.reinitialiserVue();
            this.mettreAJourChampEchelle();
        });

        JButton btnUndo = new JButton("Undo (Ctrl+Z)");
        btnUndo.addActionListener(e -> {
            this.controller.annulerAction();
            this.rafraichirPanneauDroit();
            this.drawingPanel.repaint();
            this.mettreAJourNombreTotalBlocs();
        });

        JButton btnRedo = new JButton("Redo (Ctrl+Y)");
        btnRedo.addActionListener(e -> {
            this.controller.refaireAction();
            this.rafraichirPanneauDroit();
            this.drawingPanel.repaint();
            this.mettreAJourNombreTotalBlocs();
        });

        JComboBox<String> comboTheme = new JComboBox<>(new String[] { "Automatique", "Clair", "Sombre" });
        comboTheme.setMaximumSize(new Dimension(120, 28));
        comboTheme.setPreferredSize(new Dimension(120, 28));
        comboTheme.setToolTipText("Changer le theme de l'interface");
        comboTheme.addActionListener(e -> this.appliquerTheme((String) comboTheme.getSelectedItem()));

        topToolBar.add(btnUndo);
        topToolBar.add(btnRedo);
        topToolBar.addSeparator();
        topToolBar.add(btnTronquage);
        topToolBar.addSeparator();
        topToolBar.add(btnZoomPlus);
        topToolBar.add(btnZoomMoins);
        topToolBar.add(btnRecentrer);
        topToolBar.addSeparator();
        topToolBar.add(new JLabel("Theme: "));
        topToolBar.add(comboTheme);
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
            this.statusBar.setMessage("Importation reussie: " + nombrePages + " page(s) detectee(s).");
            this.rafraichirVuesDuPlan();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IOException ex) {
            this.statusBar.setMessage("Echec de l'importation PDF: " + ex.getMessage());
        }
    }

    private void afficherEstimation() {
        double largeur = this.getLargeurSaisie();
        double hauteur = this.getHauteurSaisie();

        // Validation de base pour eviter les calculs non necessaires
        if (largeur <= 0 || hauteur <= 0) {
            this.statusBar.setMessage("Veuillez saisir des dimensions valides (strictement superieures a 0).");
            return;
        }

        // Appel au controleur pour executer la logique d'affaires
        String resultat = this.controller.simulerPlacement(largeur, hauteur);

        // Affichage du bilan dans le panneau lateral
        this.txtResultatEstimation.setText(resultat);
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

        btnRect.addActionListener(e -> {
            this.txtForme.setText("Rectangle");
            this.drawingPanel.setModeActuel(ModeInteraction.CREATION);
            this.statusBar.setMessage("Mode Creation: Rectangle. Tracez la zone sur le plan.");
        });
        btnTri.addActionListener(e -> {
            this.txtForme.setText("Triangle");
            this.drawingPanel.setModeActuel(ModeInteraction.CREATION);
            this.statusBar.setMessage("Mode Creation: Triangle. Tracez la zone sur le plan.");
        });
        btnTriTronq.addActionListener(e -> {
            this.txtForme.setText("Triangle tronque");
            this.drawingPanel.setModeActuel(ModeInteraction.CREATION);
            this.statusBar.setMessage("Mode Creation: Triangle tronque. Tracez la zone sur le plan.");
        });

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
        JButton btnRognerVue = new JButton("Rogner vue courante");
        btnRognerVue.setMaximumSize(new Dimension(260, 35));
        btnRognerVue.addActionListener(e -> {
            this.drawingPanel.activerRognageRognerCourante();
            this.statusBar.setMessage("Mode Rognage: Tracez la zone a rogner.");
        });
        leftSideBar.add(btnRognerVue);
        leftSideBar.add(Box.createVerticalStrut(6));
        this.txtNomNouvelleVue.setMaximumSize(new Dimension(260, 30));
        leftSideBar.add(this.txtNomNouvelleVue);
        leftSideBar.add(Box.createVerticalStrut(6));
        JButton btnCreerVueRognee = new JButton("Creer nouvelle vue rognee");
        btnCreerVueRognee.setMaximumSize(new Dimension(260, 35));
        btnCreerVueRognee.addActionListener(e -> {
            this.drawingPanel.activerRognageCreerVue();
            this.statusBar.setMessage("Mode Rognage: Tracez la zone — la nouvelle vue sera creee automatiquement.");
        });
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
        this.addFormField(rightSideBar, gbc, 1, "Largeur (ex: 3' 6\" ou 42) :", this.txtLargeur);
        this.addFormField(rightSideBar, gbc, 2, "Hauteur (ex: 2' 0\" ou 24) :", this.txtHauteur);
        this.addFormField(rightSideBar, gbc, 3, "Position X (ex: 1' 6\" ou 18) :", this.txtPosX);
        this.addFormField(rightSideBar, gbc, 4, "Position Y (ex: 0' 6\" ou 6) :", this.txtPosY);
        this.addFormField(rightSideBar, gbc, 5, "Echelle (pouces / pixel) :", this.txtEchelle);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);

        JButton btnAppliquerEchelle = new JButton("Appliquer l'echelle");
        btnAppliquerEchelle.addActionListener(e -> this.appliquerEchelleIndependante());
        rightSideBar.add(btnAppliquerEchelle, gbc);

        gbc.gridy = 7;
        JButton btnCreerZone = new JButton("Creer zone");
        btnCreerZone.addActionListener(e -> this.creerZoneDepuisPanneau());
        rightSideBar.add(btnCreerZone, gbc);

        gbc.gridy = 8;
        JButton btnAppliquerModification = new JButton("Appliquer les modifications");
        btnAppliquerModification.addActionListener(e -> this.appliquerModificationZone());
        rightSideBar.add(btnAppliquerModification, gbc);

        gbc.gridy = 9;
        this.btnSupprimerZone = new JButton("Supprimer la zone");
        this.btnSupprimerZone.setBackground(new Color(220, 53, 69));
        this.btnSupprimerZone.setForeground(Color.RED);
        this.btnSupprimerZone.setEnabled(false);
        this.btnSupprimerZone.addActionListener(e -> this.supprimerZoneSelectionnee());
        rightSideBar.add(this.btnSupprimerZone, gbc);

        gbc.gridy = 10;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(20, 5, 5, 5);
        rightSideBar.add(new JLabel("Prix par bloc ($) :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel prixPanel = new JPanel(new BorderLayout(4, 0));
        prixPanel.add(this.txtPrixParBloc, BorderLayout.CENTER);
        JButton btnValiderPrix = new JButton("Valider");
        btnValiderPrix.addActionListener(e -> this.mettreAJourNombreTotalBlocs());
        prixPanel.add(btnValiderPrix, BorderLayout.EAST);
        rightSideBar.add(prixPanel, gbc);

        gbc.gridy = 11;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);

        this.lblNombreBlocs.setFont(new Font("Arial", Font.BOLD, 12));
        rightSideBar.add(this.lblNombreBlocs, gbc);

        gbc.gridy = 12;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(10, 5, 2, 5);
        JButton btnSimulation = new JButton("Lancer la simulation");
        btnSimulation.addActionListener(e -> this.afficherEstimation());
        rightSideBar.add(btnSimulation, gbc);

        gbc.gridy = 13;
        gbc.insets = new Insets(2, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane scrollResultat = new JScrollPane(
                this.txtResultatEstimation,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollResultat.setBorder(BorderFactory.createTitledBorder("Resultat estimation"));
        scrollResultat.setMinimumSize(new Dimension(100, 160));
        rightSideBar.add(scrollResultat, gbc);

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

    private void installerRaccourciSuppressionZone() {
        JRootPane rootPane = this.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "supprimer-zone-selectionnee");
        actionMap.put("supprimer-zone-selectionnee", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerZoneSelectionnee();
            }
        });
    }

    private String formaterTypeForme(String typeForme) {
        if (typeForme == null || typeForme.isBlank()) {
            return "";
        }

        return switch (typeForme) {
            case "RECTANGULAIRE" -> "Rectangle";
            case "TRIANGULAIRE" -> "Triangle";
            case "TRIANGULAIRE_TRONQUEE" -> "Triangle tronque";
            default -> typeForme;
        };
    }

    private void supprimerZoneSelectionnee() {
        int indexSelectionne = this.controller.getIndexZoneSelectionnee();
        if (indexSelectionne < 0) {
            return;
        }

        this.controller.supprimerZoneSelectionnee();
        this.rafraichirPanneauDroit();
        this.mettreAJourNombreTotalBlocs();
        this.drawingPanel.repaint();
    }

    private void supprimerVueSelectionnee() {
        int indexSelectionne = this.listeVues.getSelectedIndex();
        if (indexSelectionne < 0) {
            this.statusBar.setMessage("Veuillez selectionner une vue a supprimer.");
            return;
        }

        try {
            this.controller.supprimerVue(indexSelectionne);
            this.rafraichirVuesDuPlan();
            this.mettreAJourNombreTotalBlocs();
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException ex) {
            this.statusBar.setMessage("Suppression impossible: " + ex.getMessage());
        }
    }

    void rognerVueCouranteDepuisSelection() {
        Rectangle zoneSelectionnee = this.drawingPanel.getSelectionRognageImage();
        if (zoneSelectionnee == null) {
            this.statusBar.setMessage("Activez le mode rognage et selectionnez une zone.");
            return;
        }

        try {
            this.controller.rognerVueCourante(
                    zoneSelectionnee.x,
                    zoneSelectionnee.y,
                    zoneSelectionnee.width,
                    zoneSelectionnee.height);
            this.drawingPanel.effacerSelectionRognage();
            this.drawingPanel.setModeActuel(ModeInteraction.SELECTION);
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            this.statusBar.setMessage("Rognage impossible: " + ex.getMessage());
        }
    }

    void creerNouvelleVueRogneeDepuisSelection() {
        Rectangle zoneSelectionnee = this.drawingPanel.getSelectionRognageImage();
        if (zoneSelectionnee == null) {
            this.statusBar.setMessage("Activez le mode rognage et selectionnez une zone.");
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
                    nomVue);
            this.rafraichirVuesDuPlan();
            this.drawingPanel.effacerSelectionRognage();
            this.drawingPanel.setModeActuel(ModeInteraction.SELECTION);
            this.drawingPanel.repaint();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            this.statusBar.setMessage("Creation impossible: " + ex.getMessage());
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

    public void chargerZoneSelectionneeDansPanneau() {
        dto.ZoneDTO zone = this.controller.getZoneSelectionnee();

        if (zone == null) {
            this.txtForme.setText("Rectangle");
            this.txtLargeur.setText("0'0.0\"");
            this.txtHauteur.setText("0'0.0\"");
            this.txtPosX.setText("0'0.0\"");
            this.txtPosY.setText("0'0.0\"");
            return;
        }

        this.txtPosX.setText(ImperialParser.formatterImperialCourt(zone.getX()));
        this.txtPosY.setText(ImperialParser.formatterImperialCourt(zone.getY()));
        this.txtLargeur.setText(ImperialParser.formatterImperialCourt(zone.getLargeur()));
        this.txtHauteur.setText(ImperialParser.formatterImperialCourt(zone.getHauteur()));

        switch (zone.getTypeForme()) {
            case "RECTANGULAIRE" -> this.txtForme.setText("Rectangle");
            case "TRIANGULAIRE" -> this.txtForme.setText("Triangle");
            case "TRIANGULAIRE_TRONQUEE" -> this.txtForme.setText("Triangle tronque");
            default -> this.txtForme.setText("Rectangle");
        }

        String typeZone = zone.getTypeZone();
        java.util.Enumeration<AbstractButton> boutons = this.typeGroup.getElements();
        while (boutons.hasMoreElements()) {
            AbstractButton bouton = boutons.nextElement();
            String action = bouton.getActionCommand();

            if ("BLOC".equals(typeZone) && "Blocs".equals(action)) {
                bouton.setSelected(true);
                break;
            }
            if ("CLASSIQUE".equals(typeZone) && "Classique".equals(action)) {
                bouton.setSelected(true);
                break;
            }
            if ("OUVERTURE".equals(typeZone) && "Ouverture".equals(action)) {
                bouton.setSelected(true);
                break;
            }
        }
    }

    private void creerZoneDepuisPanneau() {
        try {
            this.appliquerEchelleDepuisChamp();

            double x = ImperialParser.parsePouces(this.txtPosX.getText());
            double y = ImperialParser.parsePouces(this.txtPosY.getText());
            double largeur = ImperialParser.parsePouces(this.txtLargeur.getText());
            double hauteur = ImperialParser.parsePouces(this.txtHauteur.getText());

            if (largeur <= 0 || hauteur <= 0) {
                throw new IllegalArgumentException("La largeur et la hauteur doivent etre superieures a 0.");
            }

            String forme = this.getFormeSaisie();
            String typeZone = this.getTypeZoneSelectionne();

            this.controller.ajouterZoneDepuisPanneau(x, y, largeur, hauteur, forme, typeZone, 0.0);

            this.chargerZoneSelectionneeDansPanneau();
            this.mettreAJourChampEchelle();
            this.mettreAJourNombreTotalBlocs();
            this.drawingPanel.repaint();

        } catch (NumberFormatException ex) {
            this.statusBar.setMessage("Valeurs invalides dans le panneau.");
        } catch (IllegalArgumentException ex) {
            this.statusBar.setMessage(ex.getMessage());
        }
    }

    private void appliquerEchelleDepuisChamp() {
        double echelle = ImperialParser.parsePouces(this.txtEchelle.getText());

        if (echelle <= 0.0) {
            throw new IllegalArgumentException("L'echelle doit etre superieure a 0.");
        }

        this.controller.definirEchellePoucesParPixel(echelle);
    }

    private void mettreAJourChampEchelle() {
        double echelleAffichee = this.controller.getEchellePoucesParPixel() / this.drawingPanel.getZoomFactor();
        this.txtEchelle.setText(String.format(java.util.Locale.US, "%.6f", echelleAffichee));
    }

    private void appliquerEchelleIndependante() {
        try {
            double poucesParPixel = ImperialParser.parsePouces(this.txtEchelle.getText());

            if (poucesParPixel <= 0.0) {
                throw new IllegalArgumentException("L'echelle doit etre superieure a 0.");
            }

            double echelleBase = this.controller.getEchellePoucesParPixel();
            double nouveauZoom = echelleBase / poucesParPixel;

            this.drawingPanel.definirZoomFactor(nouveauZoom);
            this.mettreAJourChampEchelleSelonZoom();

        } catch (NumberFormatException ex) {
            this.statusBar.setMessage("Valeur d'echelle invalide.");
        } catch (IllegalArgumentException ex) {
            this.statusBar.setMessage(ex.getMessage());
        }
    }

    private void mettreAJourChampEchelleSelonZoom() {
        double echelleAffichee = this.controller.getEchellePoucesParPixel() / this.drawingPanel.getZoomFactor();
        this.txtEchelle.setText(String.format(java.util.Locale.US, "%.6f", echelleAffichee));
    }

    private void appliquerModificationZone() {
        try {
            int index = this.controller.getIndexZoneSelectionnee();
            if (index < 0) {
                return;
            }

            double x = ImperialParser.parsePouces(this.txtPosX.getText());
            double y = ImperialParser.parsePouces(this.txtPosY.getText());
            double largeur = ImperialParser.parsePouces(this.txtLargeur.getText());
            double hauteur = ImperialParser.parsePouces(this.txtHauteur.getText());

            String forme = this.getFormeSaisie();
            String typeZone = this.getTypeZoneSelectionne();

            ZoneDTO zone = this.controller.getZoneSelectionnee();
            double ratioCoupe = zone != null ? zone.getRatioCoupe() : 0.0;

            this.controller.modifierZone(
                    index, x, y, largeur, hauteur,
                    forme, typeZone, ratioCoupe);

            this.chargerZoneSelectionneeDansPanneau();
            this.mettreAJourNombreTotalBlocs();
            this.drawingPanel.repaint();

        } catch (Exception ex) {
            this.statusBar.setMessage("Erreur modification: " + ex.getMessage());
        }
    }

    public void mettreAJourNombreTotalBlocs() {
        try {
            double prix = Double.parseDouble(this.txtPrixParBloc.getText().replace(',', '.'));
            if (prix >= 0.0) {
                this.controller.setPrixParBloc(prix);
            }
        } catch (NumberFormatException ignored) {
        }
        this.controller.lancerSimulationToutesLesZones();
        int totalBlocs = this.controller.getNombreTotalBlocs();
        double cout = totalBlocs * this.controller.getPrixParBloc();
        this.lblNombreBlocs.setText(String.format(
                java.util.Locale.US, "Blocs : %d  |  Cout estimé : %.2f $", totalBlocs, cout));
    }

    private void appliquerTheme(String theme) {
        try {
            switch (theme) {
                case "Automatique" -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                case "Clair" -> {
                    UIManager.put("control", null);
                    UIManager.put("info", null);
                    UIManager.put("nimbusBase", null);
                    UIManager.put("nimbusLightBackground", null);
                    UIManager.put("text", null);
                    UIManager.put("nimbusSelectedText", null);
                    UIManager.put("nimbusSelectionBackground", null);
                    UIManager.put("nimbusFocus", null);
                    UIManager.put("nimbusBlueGrey", null);
                    UIManager.put("nimbusDisabledText", null);
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                }
                case "Sombre" -> {
                    UIManager.put("control", new Color(50, 50, 50));
                    UIManager.put("info", new Color(50, 50, 50));
                    UIManager.put("nimbusBase", new Color(18, 30, 49));
                    UIManager.put("nimbusLightBackground", new Color(60, 60, 60));
                    UIManager.put("text", new Color(220, 220, 220));
                    UIManager.put("nimbusSelectedText", Color.WHITE);
                    UIManager.put("nimbusSelectionBackground", new Color(80, 100, 160));
                    UIManager.put("nimbusFocus", new Color(100, 140, 210));
                    UIManager.put("nimbusBlueGrey", new Color(60, 60, 70));
                    UIManager.put("nimbusDisabledText", new Color(130, 130, 130));
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
            this.revalidate();
            this.repaint();
        } catch (Exception ex) {
            // ignore
        }
        // Nimbus resets editable state on JTextFields — restore manually
        SwingUtilities.invokeLater(() -> {
            if (this.txtPrixParBloc != null)   this.txtPrixParBloc.setEditable(true);
            if (this.txtLargeur != null)        this.txtLargeur.setEditable(true);
            if (this.txtHauteur != null)        this.txtHauteur.setEditable(true);
            if (this.txtPosX != null)           this.txtPosX.setEditable(true);
            if (this.txtPosY != null)           this.txtPosY.setEditable(true);
            if (this.txtEchelle != null)        this.txtEchelle.setEditable(true);
            if (this.txtNomNouvelleVue != null) this.txtNomNouvelleVue.setEditable(true);
            if (this.txtForme != null)          this.txtForme.setEditable(false);
            if (this.txtNombreBlocs != null)    this.txtNombreBlocs.setEditable(false);
            if (this.txtResultatEstimation != null) this.txtResultatEstimation.setEditable(false);
        });
    }

    public void activerModeSelection() {
        if (this.drawingPanel != null) {
            this.drawingPanel.setModeActuel(ModeInteraction.SELECTION);
            this.statusBar.setMessage("Mode Selection actif.");
        }
    }

    private void sauvegarderProjet() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Sauvegarder le projet");
        fc.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Projet BatiBloc (*.batibloc)", "batibloc"));
        int result = fc.showSaveDialog(this);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            String chemin = fc.getSelectedFile().getAbsolutePath();
            if (!chemin.endsWith(".batibloc")) {
                chemin += ".batibloc";
            }
            try {
                this.controller.sauvegarderProjet(chemin);
                this.statusBar.setMessage("Projet sauvegarde avec succes : " + chemin);
            } catch (Exception ex) {
                this.statusBar.setMessage("Erreur sauvegarde : " + ex.getMessage());
            }
        }
    }

    private void ouvrirProjet() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Ouvrir un projet");
        fc.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Projet BatiBloc (*.batibloc)", "batibloc"));
        int result = fc.showOpenDialog(this);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                this.controller.chargerProjet(fc.getSelectedFile().getAbsolutePath());
                this.chargerVuesDuPlan();
                this.drawingPanel.repaint();
                this.mettreAJourNombreTotalBlocs();
                this.statusBar.setMessage("Projet charge avec succes.");
            } catch (Exception ex) {
                this.statusBar.setMessage("Erreur chargement : " + ex.getMessage());
            }
        }
    }

    private void chargerVuesDuPlan() {
        // Recharger le combo des vues apres un chargement de projet
        // Le combo est dans le topToolBar - on le reconstruit
        java.util.List<String> vues = this.controller.getVuesDuPlan();
        if (vues != null && !vues.isEmpty()) {
            // Trouver et mettre a jour le comboBox existant si possible
            java.awt.Component[] components = this.getContentPane().getComponents();
            for (java.awt.Component comp : components) {
                if (comp instanceof javax.swing.JToolBar toolbar) {
                    for (java.awt.Component c : toolbar.getComponents()) {
                        if (c instanceof javax.swing.JComboBox<?> combo) {
                            combo.removeAllItems();
                            @SuppressWarnings("unchecked")
                            javax.swing.JComboBox<String> comboStr = (javax.swing.JComboBox<String>) combo;
                            for (String vue : vues) {
                                comboStr.addItem(vue);
                            }
                            int idx = this.controller.getIndexVueCourante();
                            if (idx >= 0 && idx < vues.size()) {
                                comboStr.setSelectedIndex(idx);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private void exporterVueCourantePNG() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Exporter la vue courante en PNG");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image PNG (*.png)", "png"));
        int result = fc.showSaveDialog(this);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            String chemin = fc.getSelectedFile().getAbsolutePath();
            if (!chemin.toLowerCase().endsWith(".png")) {
                chemin += ".png";
            }
            try {
                java.awt.image.BufferedImage imageVue = this.controller.getImageVueCourante();
                if (imageVue == null) {
                    this.statusBar.setMessage("Aucune vue a exporter.");
                    return;
                }

                // Creer une image de la taille de la vue
                int w = imageVue.getWidth();
                int h = imageVue.getHeight();
                java.awt.image.BufferedImage export = new java.awt.image.BufferedImage(w, h,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = export.createGraphics();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // Dessiner le fond (image du plan)
                g2d.drawImage(imageVue, 0, 0, null);

                // Dessiner les zones et les blocs par dessus
                this.drawingPanel.dessinerZonesEtBlocsSurImage(g2d, imageVue);

                g2d.dispose();
                javax.imageio.ImageIO.write(export, "PNG", new java.io.File(chemin));
                this.statusBar.setMessage("Vue exportee avec succes : " + chemin);
            } catch (Exception ex) {
                this.statusBar.setMessage("Erreur export : " + ex.getMessage());
            }
        }
    }

    private void exporterToutesLesVuesPNG() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Exporter toutes les vues en PNG");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image PNG (*.png)", "png"));
        int result = fc.showSaveDialog(this);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            String chemin = fc.getSelectedFile().getAbsolutePath();
            if (!chemin.toLowerCase().endsWith(".png")) {
                chemin += ".png";
            }
            try {
                java.util.List<String> vues = this.controller.getVuesDuPlan();
                if (vues == null || vues.isEmpty()) {
                    this.statusBar.setMessage("Aucune vue a exporter.");
                    return;
                }

                int indexOriginal = this.controller.getIndexVueCourante();
                int totalHeight = 0;
                int maxWidth = 0;

                // Calculer les dimensions totales
                for (int i = 0; i < vues.size(); i++) {
                    this.controller.selectionnerVue(i);
                    java.awt.image.BufferedImage img = this.controller.getImageVueCourante();
                    if (img != null) {
                        totalHeight += img.getHeight();
                        maxWidth = Math.max(maxWidth, img.getWidth());
                    }
                }

                // Creer l'image combinee
                java.awt.image.BufferedImage combined = new java.awt.image.BufferedImage(maxWidth, totalHeight,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = combined.createGraphics();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                int yOffset = 0;
                for (int i = 0; i < vues.size(); i++) {
                    this.controller.selectionnerVue(i);
                    java.awt.image.BufferedImage img = this.controller.getImageVueCourante();
                    if (img != null) {
                        g2d.drawImage(img, 0, yOffset, null);

                        // Dessiner les zones/blocs de cette vue
                        java.awt.Graphics2D g2dSub = (java.awt.Graphics2D) g2d.create(0, yOffset, img.getWidth(),
                                img.getHeight());
                        this.drawingPanel.dessinerZonesEtBlocsSurImage(g2dSub, img);
                        g2dSub.dispose();

                        yOffset += img.getHeight();
                    }
                }

                g2d.dispose();

                // Restaurer la vue originale
                if (indexOriginal >= 0) {
                    this.controller.selectionnerVue(indexOriginal);
                }

                javax.imageio.ImageIO.write(combined, "PNG", new java.io.File(chemin));
                this.statusBar.setMessage("Toutes les vues exportees : " + chemin);
            } catch (Exception ex) {
                this.statusBar.setMessage("Erreur export : " + ex.getMessage());
            }
        }
    }
}
