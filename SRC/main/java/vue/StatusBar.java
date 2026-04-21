package vue;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel lblCoordonnees;
    private final JLabel lblMessage;
    private Timer messageTimer;

    public StatusBar() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(800, 25));
        this.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        this.lblMessage = new JLabel("");
        this.lblCoordonnees = new JLabel("x: 0' y: 0'");

        this.add(this.lblMessage, BorderLayout.WEST);
        this.add(this.lblCoordonnees, BorderLayout.EAST);
    }

    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        this.lblMessage.setText(message);
        this.lblMessage.setForeground(Color.RED.darker());
        
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }
        
        messageTimer = new Timer(5000, e -> {
            this.lblMessage.setText("");
            this.lblMessage.setForeground(Color.BLACK);
        });
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    public void masquerMessage() {
        this.lblMessage.setText("");
    }

    public void setCoordonnees(String coords) {
        this.lblCoordonnees.setText(coords);
    }
}
