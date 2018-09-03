import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class StartupScreen extends JPanel{
    private static ArrayList<Integer> horizontalLines = new ArrayList<>();
    private static ArrayList<Integer> verticalLines = new ArrayList<>();
    private JRadioButton classicRadio, normalRadio, HDRadio;

    public StartupScreen(){
        selector();
    }
    private void selector() {
        Frame frame = new Frame("Chain Reaction", 0);
        JPanel content = new JPanel();
        frame.setContentPane(content);
        content.setLayout(null);
        ButtonGroup selectorGroup = new ButtonGroup();
        classicRadio = new JRadioButton("Classic(5x8)");
        normalRadio = new JRadioButton("Normal(11x11)");
        HDRadio = new JRadioButton("HD Grid(15x10)");
        selectorGroup.add(classicRadio);
        selectorGroup.add(normalRadio);
        selectorGroup.add(HDRadio);
        content.add(classicRadio);
        content.add(normalRadio);
        content.add(HDRadio);
        classicRadio.setBounds(0, 0, 100, 25);
        normalRadio.setBounds(100, 0, 100, 25);
        HDRadio.setBounds(200, 0, 100, 25);
        content.setPreferredSize(new Dimension(300, 50));
        content.setBackground(Color.BLACK);
        JButton button = new JButton("OK");
        content.add(button);
        ButtonHandler b = new ButtonHandler();
        button.addActionListener(b);
        button.setBounds(100, 25, 100, 25);
        frame.pack();
    }
    private void startUp(double rowSpacing, double columnSpacing, int down, int rowNumber, int columnNumber) {
        int width = 625;
        int length = (int) (width * rowSpacing);
        for (int x = 0; x < rowNumber; x++) {
            verticalLines.add(length);
            length = length + (int) (width * rowSpacing);
        }
        for (int x = 0; x < columnNumber; x++) {
            horizontalLines.add(down);
            down = down + (int) (width * columnSpacing);
        }
        verticalLines.add(length);
    }

    private void setValues() {
        if (classicRadio.isSelected()) {
            ChainReactionClient.setHandshake("classic");
        } else if (normalRadio.isSelected()) {
            ChainReactionClient.setHandshake("normal");
            ChainReactionClient.setRowNumber(11);
            ChainReactionClient.setColumnNumber(11);
            startUp(.08, .08, 65, 11, 11);
        } else if (HDRadio.isSelected()) {
            ChainReactionClient.setHandshake("HD");
            ChainReactionClient.setRowNumber(10);
            ChainReactionClient.setColumnNumber(15);
            startUp(.09, .07, 35, 10 ,15);
        } else {
            ChainReactionClient.setIsBroken(true);
        }
    }

    public ArrayList<Integer> getVerticalLines() {
        return verticalLines;
    }

    public ArrayList<Integer> getHorizontalLines() {
        return horizontalLines;
    }

    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            setValues();
            synchronized (ChainReactionClient.monitor){
                ChainReactionClient.monitor.notify();
            }
        }
    }


}
