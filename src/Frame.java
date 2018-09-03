import javax.swing.*;

class Frame extends JFrame {
        Frame(String name) {
            this.setName(name);
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        Frame(String name, int a) {
            this.setName(name);
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

    }

