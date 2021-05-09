import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Chart extends JPanel{

    private class Channel{
        float[][] content = null;
        Color col;
        String name;
        boolean active = false;

        public Channel(String name, Color col) {
            this.name = name;
            this.col = col;
        }
    }

    float[] xDim = new float[2];
    float[] yDim = new float[2];
    float xF;
    float yF;
    Color meshColor = Color.GRAY;
    int meshHor = 6;
    int meshVer = 4;
    ArrayList<Channel> ch = new ArrayList<Channel>();
    int verticalMarker = 0;

    public Chart(int xLow, int xHigh, int yLow, int yHigh) {
        ch.add(new Channel("A0", Color.GREEN));
        ch.add(new Channel("A1", Color.BLUE));
        ch.add(new Channel("A2", Color.RED));
        ch.add(new Channel("A3", Color.YELLOW));
        ch.add(new Channel("A4", Color.MAGENTA));
        this.setBackground(Color.BLACK);
        xDim[0] = xLow;
        xDim[1] = xHigh;
        yDim[0] = yLow;
        yDim[1] = yHigh;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        xF = this.getWidth()/(xDim[1] - xDim[0]);
        yF = this.getHeight()/(yDim[1] - yDim[0]);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(meshColor);
        int f = this.getHeight()/(meshHor + 1);
        for(int i = 1; i <= meshHor; i++) {
            g2d.drawLine(0, i*f, this.getWidth(), i*f);
        }
        f = this.getWidth()/(meshVer + 1);
        for(int i = 1; i <= meshVer; i++) {
            g2d.drawLine(i*f, 0, i*f, this.getHeight());
        }
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.drawLine(verticalMarker, 0, verticalMarker, this.getHeight());
        for(int i = 0; i < ch.size(); i++) {
            Channel c = ch.get(i);
            if((c.content != null) && c.active) {
                g2d.setColor(c.col);
                for(int j = 0; j < (c.content.length - 1); j++) {
                    g2d.drawLine((int)(c.content[j][0]*xF - xDim[0]*xF), this.getHeight() - (int)(c.content[j][1]*yF - yDim[0]*yF), (int)(c.content[j+1][0]*xF- xDim[0]*xF), this.getHeight() - (int)(c.content[j+1][1]*yF - yDim[0]*yF));
                }
            }
        }
    }

    public void setContent(int chNum, float[][] cont) {
        if(chNum < ch.size() && chNum > -1) {
            ch.get(chNum).content = cont;
        }
    }

    public void setXAxisDim(float low, float high) {
        xDim[0] = low;
        xDim[1] = high;
    }

    public void setYAxisDim(float low, float high) {
        yDim[0] = low;
        yDim[1] = high;
    }

    public void setMesh(int horizontal, int vertical) {
        meshHor = horizontal;
        meshVer = vertical;
    }

    public String getChannelName(int chNum) {
        if(chNum < ch.size() && chNum > -1) return ch.get(chNum).name;
        else return null;
    }

    public Color getChannelColor(int chNum) {
        if(chNum < ch.size() && chNum > -1) return ch.get(chNum).col;
        else return null;
    }

    public void setChannelActive(int chNum, boolean isActive) {
        ch.get(chNum).active = isActive;
    }

    public boolean isChannelActive(int chNum) {
        return ch.get(chNum).active;
    }

    public float[] locationToValue(int x, int y) {
        float[] res = new float[2];
        res[0] = xDim[0] + x/xF;
        res[1] = yDim[1] - y/yF;
        return res;
    }

    public void setVerticalMarker(float pos) {
        verticalMarker = (int)(this.getWidth()*pos);
    }
}
