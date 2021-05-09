import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Mimic {
    int h = 900;					//frame height
    int w = 1400;
    int vvh = 400;					//valve height
    int vvw = 600;
    int vvx = 450;
    int vvy = 50;
    int wall = 50;
    int ccw = vvw - wall - 150;		//controll channel width
    int cch = 70;
    int pcly = vvy + 2*wall + cch + (vvh - 3*wall - cch)/2;		//piston center line
    int ph = vvh - 3*wall - cch;								//piston height
    int pw = vvw - 3*wall - 100;
    int pZeroPos = vvx + 2*wall + 100;
    int pOffset = 0;
    int pIdle = 40;
    int nZeroPos = vvx + wall + ccw;		//needle zero
    int ncly = vvy + wall + cch/2;			//needle center line
    int nOffset = 0;
    int legendX = vvx + vvw + 30;
    float ps = 0.0f;		//system press
    float pc = 0.0f;		//controll press
    float vp = 0.0f;		//piston velocity
    float vn = 0.0f;		//needle velocity
    float qp = 0.0f;		//flow through piston
    float qn = 0.0f;		//flow through needle
    float qb = 0.0f;		//flow through bypass
    JTextField nvym = new JTextField(6); //needle valve spring Young modulus
    JTextField nvit = new JTextField(6); //spring initial tension
    JTextField nvm = new JTextField(6);	//needle mass
    JTextField pym = new JTextField(6); //piston Young modulus
    JTextField pit = new JTextField(6);	//initial tension
    JTextField pm = new JTextField(6);	//piston mass
    JTextField pod = new JTextField(6); //orifice diameter
    JTextField pis = new JTextField(6); //idle stroke
    JTextField spc = new JTextField(6); //pump capacity
    JTextField se = new JTextField(6); //system expansion
    JTextField ctb = new JTextField(6);	//calculation time base
    JTextField cd = new JTextField(6);	//simulation duration
    JTextField cs = new JTextField(6);	//playback speed
    JSlider slider;
    JButton solve;
    Timer timer;
    JFrame frame;
    int val = 0;
    ArrayList<DataPoint> points = new ArrayList<DataPoint>();
    Chart chart;
    Main main;

    public Mimic(Main main) {
        this.main = main;
        JToggleButton play = new JToggleButton() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                int w = this.getWidth();
                int h = this.getHeight();
                int size = 30;
                g2d.setColor(Color.BLACK);
                g2d.fillPolygon(new int[] {(w/2) - (size/2), (w/2) - (size/2), (w/2) + (size/2)}, new int[] {(h/2) + (size/2), (h/2) - (size/2), h/2}, 3);
            }
        };
        frame = new JFrame();
        ActionListener timerListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                val += 1;
                if(val < points.size()) {
                    points.get(val).drawPoint();
                }
                else {
                    timer.stop();
                    play.setSelected(false);
                }
                slider.setValue((100*val)/points.size());
                chart.setVerticalMarker((float)val/points.size());
            }
        };
        timer = new Timer(20, timerListener);
        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                Paint paint = new GradientPaint(0, 0, Color.GRAY, 5, 5, Color.LIGHT_GRAY, true);
                g2d.setPaint(paint);
                g2d.fillRect(vvx, vvy, vvw, vvh);	//valve body
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(vvx + wall, vvy + wall, 100, vvh - wall);		//outlet
                g2d.fillRect(vvx + wall, vvy + wall, ccw, cch);				//controll channel
                g2d.fillRect(pZeroPos, vvy + wall + cch/2 - 15, pw, 30);	//passage
                g2d.fillRect(pZeroPos + pw - 30, vvy + wall + cch/2 + 15, 30, 2*wall);	//passage
                g2d.fillRect(vvx + wall + 100, pcly - 25, vvw - wall - 100, 50);	//piston guide
                g2d.fillRect(pZeroPos, pcly - ph/2, pw, ph);		//piston chamber
                g2d.fillRect(pZeroPos, pcly + ph/2, 100, wall);		//inlet
                paint = new GradientPaint(0, pcly - 23, Color.DARK_GRAY, 0, pcly, Color.WHITE, true);	//piston guide
                g2d.setPaint(paint);
                g2d.fillRect(pZeroPos + pOffset - pIdle, pcly - 23, pw + pIdle + 40, 46);
                paint = new GradientPaint(0, pcly - ph/2, Color.DARK_GRAY, 0, pcly, Color.WHITE, true);		//piston
                g2d.setPaint(paint);
                g2d.fillRect(pZeroPos + pOffset + pw/2 - 20, pcly - ph/2 + 2, 40, ph - 4);
                paint = new GradientPaint(0, ncly - 40, Color.DARK_GRAY, 0, ncly, Color.WHITE, true);		//needle
                g2d.setPaint(paint);
                int[] x = {nZeroPos - 50 - nOffset, nZeroPos + 50 - nOffset, nZeroPos - 50 - nOffset};
                int[] y = {ncly - 30, ncly, ncly + 30};
                g2d.fillPolygon(x, y, 3);
                g2d.setColor(Color.BLUE);													//legend
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.drawLine(nZeroPos - nOffset, ncly, legendX, ncly - 30);					//needle
                g2d.drawLine(legendX, ncly - 30, legendX + 50, ncly - 30);
                g2d.fillOval(nZeroPos - 4 - nOffset, ncly - 4, 8, 8);
                g2d.drawLine(pZeroPos + pOffset + pw/2, pcly - ph/4, legendX, ncly + 80);	//piston
                g2d.drawLine(legendX, ncly + 80, legendX + 50, ncly + 80);
                g2d.fillOval(pZeroPos + pOffset + pw/2 - 4, pcly - ph/4 - 4, 8, 8);
                g2d.setColor(Color.RED);
                g2d.drawLine(pZeroPos + pw - 20 , pcly + ph/4, legendX, ncly + 200);		//ctrl press
                g2d.fillOval(pZeroPos + pw - 24, pcly + ph/4 - 4, 8, 8);
                g2d.drawLine(legendX, ncly + 200, legendX + 50, ncly + 200);
                g2d.setColor(Color.GREEN);
                g2d.drawLine(pZeroPos + 50, pcly + ph/4, legendX, ncly + 260);				//system press
                g2d.fillOval(pZeroPos + 46, pcly + ph/4 - 4, 8, 8);
                g2d.drawLine(legendX, ncly + 260, legendX + 50, ncly + 260);
                g2d.setColor(Color.BLUE);
                g2d.drawLine(pZeroPos - wall/2, pcly, pZeroPos + 100, vvy + vvh + 20);					//bypass flow
                g2d.fillOval(pZeroPos - wall/2 - 4, pcly - 4, 8, 8);
                g2d.drawLine(pZeroPos + 100, vvy + vvh + 20, legendX + 50, vvy + vvh + 20);
                String txt = "Vn " + vn + " m/s";
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font(g2d.getFont().getFontName(), Font.BOLD, 30));
                g2d.drawString(txt, legendX + 60, ncly - 40);
                txt = "Qn " + qn + " L/min";
                g2d.drawString(txt, legendX + 60, ncly);
                txt = "Vp " + vp + " m/s";
                g2d.drawString(txt, legendX + 60, ncly + 70);
                txt = "Qp " + qp + " L/min";
                g2d.drawString(txt, legendX + 60, ncly + 110);
                txt = "pc " + pc + " bar";
                g2d.drawString(txt, legendX + 60, ncly + 210);
                txt = "ps " + ps + " bar";
                g2d.drawString(txt, legendX + 60, ncly + 270);
                txt = "Qb " + qb + " L/min";
                g2d.drawString(txt, legendX + 60, vvy + vvh + 30);
                g2d.setStroke(new BasicStroke(1.0f));
            }
        };
        panel.setLayout(null);
        JPanel params = new JPanel();
        params.setBorder(BorderFactory.createRaisedBevelBorder());

        JPanel needleParams = new JPanel();
        needleParams.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Needle valve", TitledBorder.CENTER, TitledBorder.TOP, new Font(panel.getFont().getName(), Font.PLAIN, 15)));
        needleParams.setLayout(null);
        JTextField[] fields = {nvym, nvit, nvm};
        String[] labels = {"Young modulus", "Initial tension", "Mass"};
        String[] units = {"N/mm", "N", "g"};
        int[] spacingX = {15, 180, 80, 10, 80};
        int[] spacingY = {30, 30, 35};
        Font font = new Font(panel.getFont().getName(), Font.PLAIN, 20);
        for(int i = 0; i < fields.length; i++) {
            JLabel l = new JLabel(labels[i]);
            needleParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[1], spacingY[1]);
            l.setLocation(spacingX[0], i*spacingY[2] + spacingY[0]);
            needleParams.add(fields[i]);
            fields[i].setSize(spacingX[2], spacingY[1]);
            fields[i].setLocation(spacingX[0] + spacingX[1], i*spacingY[2] + spacingY[0]);
            fields[i].setFont(font);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            l = new JLabel(units[i]);
            needleParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[4], spacingY[1]);
            l.setLocation(spacingX[0] + spacingX[1] + spacingX[2] + spacingX[3], i*spacingY[2] + spacingY[0]);
        }
        needleParams.setPreferredSize(new Dimension(380, fields.length*spacingY[2] + spacingY[1] + 5));
        nvym.setText("10");
        nvit.setText("40");
        nvm.setText("4");
        params.add(needleParams);

        JPanel pistonParams = new JPanel();
        pistonParams.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Plunger", TitledBorder.CENTER, TitledBorder.TOP, new Font(panel.getFont().getName(), Font.PLAIN, 15)));
        params.add(pistonParams);
        pistonParams.setLayout(null);
        FocusListener tfcl = new FocusListener() {
            public void focusLost(FocusEvent e) {
                pIdle = (int)(Float.parseFloat(pis.getText())*10);
                frame.repaint();
            }
            public void focusGained(FocusEvent e) {}
        };
        KeyListener kl = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == 10) {
                    pIdle = (int)(Float.parseFloat(pis.getText())*10);
                    frame.repaint();
                }
            }
        };
        pis.addKeyListener(kl);
        pis.addFocusListener(tfcl);
        fields = new JTextField[] {pym, pit, pm, pod, pis};
        labels = new String[] {"Young modulus", "Initial tension", "Mass", "Orifice diameter", "Idle stroke"};
        units = new String[] {"N/mm", "N", "g", "mm", "mm"};
        for(int i = 0; i < fields.length; i++) {
            JLabel l = new JLabel(labels[i]);
            pistonParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[1], spacingY[1]);
            l.setLocation(spacingX[0], i*spacingY[2] + spacingY[0]);
            pistonParams.add(fields[i]);
            fields[i].setSize(spacingX[2], spacingY[1]);
            fields[i].setLocation(spacingX[0] + spacingX[1], i*spacingY[2] + spacingY[0]);
            fields[i].setFont(font);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            l = new JLabel(units[i]);
            pistonParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[4], spacingY[1]);
            l.setLocation(spacingX[0] + spacingX[1] + spacingX[2] + spacingX[3], i*spacingY[2] + spacingY[0]);
        }
        pistonParams.setPreferredSize(new Dimension(380, fields.length*spacingY[2] + spacingY[1] + 5));
        pym.setText("1");
        pit.setText("10");
        pm.setText("40");
        pod.setText("1");
        pis.setText("4");

        JPanel systemParams = new JPanel();
        systemParams.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "System", TitledBorder.CENTER, TitledBorder.TOP, new Font(panel.getFont().getName(), Font.PLAIN, 15)));
        params.add(systemParams);
        systemParams.setLayout(null);
        fields = new JTextField[] {spc, se};
        labels = new String[] {"Pump capacity", "Accumulation"};
        units = new String[] {"L/min", "ml/bar"};
        for(int i = 0; i < fields.length; i++) {
            JLabel l = new JLabel(labels[i]);
            systemParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[1], spacingY[1]);
            l.setLocation(spacingX[0], i*spacingY[2] + spacingY[0]);
            systemParams.add(fields[i]);
            fields[i].setSize(spacingX[2], spacingY[1]);
            fields[i].setLocation(spacingX[0] + spacingX[1], i*spacingY[2] + spacingY[0]);
            fields[i].setFont(font);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            l = new JLabel(units[i]);
            systemParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[4], spacingY[1]);
            l.setLocation(spacingX[0] + spacingX[1] + spacingX[2] + spacingX[3], i*spacingY[2] + spacingY[0]);
        }
        systemParams.setPreferredSize(new Dimension(380, fields.length*spacingY[2] + spacingY[1] + 5));
        spc.setText("18");
        se.setText("0.05");

        JPanel simulationParams = new JPanel();
        simulationParams.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Simulation", TitledBorder.CENTER, TitledBorder.TOP, new Font(panel.getFont().getName(), Font.PLAIN, 15)));
        params.add(simulationParams);
        simulationParams.setLayout(null);
        fields = new JTextField[] {ctb, cd, cs};
        labels = new String[] {"Time base", "Duration", "Playback speed"};
        units = new String[] {"us", "s", "-"};
        for(int i = 0; i < fields.length; i++) {
            JLabel l = new JLabel(labels[i]);
            simulationParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[1], spacingY[1]);
            l.setLocation(spacingX[0], i*spacingY[2] + spacingY[0]);
            simulationParams.add(fields[i]);
            fields[i].setSize(spacingX[2], spacingY[1]);
            fields[i].setLocation(spacingX[0] + spacingX[1], i*spacingY[2] + spacingY[0]);
            fields[i].setFont(font);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            l = new JLabel(units[i]);
            simulationParams.add(l);
            l.setFont(font);
            l.setSize(spacingX[4], spacingY[1]);
            l.setLocation(spacingX[0] + spacingX[1] + spacingX[2] + spacingX[3], i*spacingY[2] + spacingY[0]);
        }
        simulationParams.setPreferredSize(new Dimension(380, fields.length*spacingY[2] + spacingY[1] + 5));
        ctb.setText("0.3");
        cd.setText("0.3");
        cs.setText("0.005");

        panel.add(params);
        params.setSize(400, needleParams.getPreferredSize().height + pistonParams.getPreferredSize().height + systemParams.getPreferredSize().height + simulationParams.getPreferredSize().height + 30);
        params.setLocation(20, 20);

        JPanel controlls = new JPanel();
        controlls.setBorder(BorderFactory.createRaisedBevelBorder());
        controlls.setSize(400, 180);
        controlls.setLocation(20, params.getHeight() + 30);
        controlls.setLayout(null);

        ActionListener pl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(timer.isRunning()) timer.stop();
                else timer.start();
            }
        };
        play.addActionListener(pl);
        controlls.add(play);
        play.setSize((controlls.getWidth() - 60)/2, 50);
        play.setLocation(20, 10);
        JButton stop = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                int w = this.getWidth();
                int h = this.getHeight();
                int size = 30;
                g2d.setColor(Color.BLACK);
                g2d.fillRect(w/2 - size/2, h/2 - size/2, size, size);
            }
        };
        ActionListener sl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                val = 0;
                slider.setValue(val);
                chart.setVerticalMarker(0);
                play.setSelected(false);
                points.get(0).drawPoint();
            }
        };
        stop.addActionListener(sl);
        controlls.add(stop);
        stop.setSize((controlls.getWidth() - 60)/2, 50);
        stop.setLocation(controlls.getWidth() - 20 - stop.getWidth(), 10);
        slider = new JSlider();
        ChangeListener scl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                val = ((points.size() - 1)*slider.getValue())/100;
                if(points.size() != 0) points.get(val).drawPoint();
                chart.setVerticalMarker((float)val/points.size());
            }
        };
        MouseListener sliderListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                slider.addChangeListener(scl);
            }
            public void mouseReleased(MouseEvent e) {
                slider.removeChangeListener(scl);
            }
        };
        slider.addMouseListener(sliderListener);
        controlls.add(slider);
        slider.setSize(controlls.getWidth() - 40, 40);
        slider.setLocation(20, 70);
        slider.setValue(0);
        solve = new JButton("Solve");
        ActionListener sol = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                play.setSelected(false);
                val = 0;
                chart.setVerticalMarker(0);
                slider.setValue(0);
                main.simulation();
                dataToChart();
                points.get(0).drawPoint();
            }
        };
        solve.addActionListener(sol);
        controlls.add(solve);
        solve.setSize(controlls.getWidth() - 40, 50);
        solve.setLocation(20, 120);
        solve.setFont(font);
        panel.add(controlls);

        chart = new Chart(0, 1, 0, 100);
        panel.add(chart);
        chart.setLocation(vvx + 20, vvy + vvh + 70);
        chart.setSize(w - vvx - 90, h - (vvy + vvh + 70) - 90);
        MouseListener cml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                chart.verticalMarker = e.getX();
                val = (int)chart.locationToValue(e.getX(), e.getY())[0];
                slider.setValue((100*val)/points.size());
                points.get(val).drawPoint();
            }
        };
        chart.addMouseListener(cml);

        frame.add(panel);
        frame.setSize(w, h);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private class DataPoint{
        //String[] paramName = {"nx", "px", "Vn", "Qn", "Vp", "Qp", "pc", "ps", "Qb"};
        int xF = 10; //1mm = 10pix
        int[] loc = new int[2];
        float[] par = new float[7];

        DataPoint(double[] loc, double[] par){
            for(int i = 0; i < 2; i++) {
                this.loc[i] = (int)(loc[i]*xF);
            }
            for(int i = 0; i < 7; i++) {
                this.par[i] = (int)(100*par[i])/100.0f;
            }
        }

        void drawPoint() {
            nOffset = loc[0];
            pOffset = loc[1];
            vn = par[0];
            qn = par[1];
            vp = par[2];
            qp = par[3];
            pc = par[4];
            ps = par[5];
            qb = par[6];
            frame.repaint();
        }
    }

    void setDataPoint(double[] locations, double[] parameters) {
        points.add(new DataPoint(locations, parameters));
    }

    void clearDataPoints() {
        points = new ArrayList<DataPoint>();
    }

    void dataToChart() {
        float minVal = 0;
        float maxVal = 0;
        float[][] psVals = new float[points.size()][2];
        float[][] pcVals = new float[points.size()][2];
        for(int i = 0; i < points.size(); i++) {
            psVals[i][0] = i;
            pcVals[i][0] = i;
            psVals[i][1] = points.get(i).par[5];
            pcVals[i][1] = points.get(i).par[4];
            if(psVals[i][1] > maxVal) maxVal = psVals[i][1];
            if(psVals[i][1] < minVal) minVal = psVals[i][1];
            if(pcVals[i][1] > maxVal) maxVal = pcVals[i][1];
            if(pcVals[i][1] < minVal) minVal = pcVals[i][1];
        }
        chart.setContent(0, psVals);
        chart.setContent(2, pcVals);
        chart.setXAxisDim(0, points.size() - 1);
        chart.setYAxisDim(minVal, maxVal + 0.05f*(maxVal - minVal));
        chart.setChannelActive(0, true);
        chart.setChannelActive(2, true);
        chart.repaint();
    }
}
