
public class Main {
    Mimic gui;
    double timeBase;			//[s]
    double duration;			//[s]
    double step;				//[s]
    double pumpCapacity;		//[ml/s]
    double systemExpansion;		//[ml/bar]
    double needleMass;			//[kg]
    double pistonMass;			//[kg]
    double needleInitTens;		//[N]
    double pistonInitTens;		//[N]
    double needleYoung;			//[N/m]
    double pistonYoung;			//[N/m]
    double pistonIdleStroke;	//[m]
    double orificeSurface;		//[mm^2]
    double needleSurface = Math.PI*Math.pow(3, 2)/4;					//[mm^2]
    double systemSurface = Math.PI*(Math.pow(25, 2)-Math.pow(10, 2))/4;	//[mm^2]
    double controllSurface = Math.PI*(Math.pow(25, 2)-Math.pow(8, 2))/4;//[mm^2]
    double bypassSurface = Math.PI*(Math.pow(10, 2)-Math.pow(4.5, 2))/4;//[mm^2]
    double systemPress = 0;		//[bar]
    double controllPress = 0;	//[bar]
    double needleFlow = 0;		//[ml/s]
    double orificeFlow = 0;		//[ml/s]
    double bypassFlow = 0;		//[ml/s]
    double aNeedle = 0;			//[m/s^2]
    double vNeedle = 0;			//[m/s]
    double xNeedle = 0;			//[m]
    double aPiston = 0;			//[m/s^2]
    double vPiston = 0;			//[m/s]
    double xPiston = 0;			//[m]
    double ro = 870;			//[kg/m^3]
    double dfc;					//flow in control chamber due to volume alteration [ml/s]
    double needleActiveSurface;	//[mm^2]

    public Main() {
        gui = new Mimic(this);
        simulation();
        gui.dataToChart();
    }

    void simulation() {
        timeBase = Double.parseDouble(gui.ctb.getText())/1000000;	//[s]
        duration = Double.parseDouble(gui.cd.getText());				//[s]
        step = 0.02*Double.parseDouble(gui.cs.getText());			//[s]
        pumpCapacity = 100*Double.parseDouble(gui.spc.getText())/6;	//[ml/s]
        systemExpansion = Double.parseDouble(gui.se.getText());		//[ml/bar]
        needleMass = Double.parseDouble(gui.nvm.getText())/1000;		//[kg]
        pistonMass = Double.parseDouble(gui.pm.getText())/1000;		//[kg]
        needleInitTens = Double.parseDouble(gui.nvit.getText());		//[N]
        pistonInitTens = Double.parseDouble(gui.pit.getText());		//[N]
        needleYoung = 1000*Double.parseDouble(gui.nvym.getText());	//[N/m]
        pistonYoung = 1000*Double.parseDouble(gui.pym.getText());	//[N/m]
        pistonIdleStroke = Double.parseDouble(gui.pis.getText())/1000;	//[m]
        orificeSurface = Math.PI*Math.pow(Double.parseDouble(gui.pod.getText()), 2)/4;	//[mm^2]
        systemPress = 0;		//[bar]
        controllPress = 0;	//[bar]
        needleFlow = 0;		//[ml/s]
        orificeFlow = 0;		//[ml/s]
        bypassFlow = 0;		//[ml/s]
        aNeedle = 0;			//[m/s^2]
        vNeedle = 0;			//[m/s]
        xNeedle = 0;			//[m]
        aPiston = 0;			//[m/s^2]
        vPiston = 0;			//[m/s]
        xPiston = 0;			//[m]

        gui.clearDataPoints();
        sendDataPoint();

        double timeLapsed = 0;
        double lastStep = 0; //timestamp for last frame
        double needleActiveSurface = 0;
        double criticalPress = 0;		//controll pressure for zero force on piston
        double bypassActiveSurface = 0;

        while(timeLapsed < duration) {
            if(xPiston > pistonIdleStroke) {
                bypassActiveSurface = Math.PI*10*(xPiston - pistonIdleStroke)*1000; //[mm^3]
                if(bypassActiveSurface > bypassSurface) bypassActiveSurface = bypassSurface;
                bypassFlow = bypassActiveSurface*Math.sqrt(systemPress*100000/ro);
            }
            else bypassFlow = 0;
            systemPress += (pumpCapacity - bypassFlow - orificeFlow - vPiston*systemSurface)*timeBase/systemExpansion;
            needleActiveSurface = xNeedle*needleSurface/0.005;
            if(needleActiveSurface > needleSurface) needleActiveSurface = needleSurface;
            criticalPress = (systemPress*systemSurface - pistonInitTens - xPiston*pistonYoung)/controllSurface;
            if((needleActiveSurface == 0) && (vPiston == 0)) {
                controllPress = systemPress;
            }
            else if(vPiston == 0) {
                controllPress = systemPress/(Math.pow(needleActiveSurface/orificeSurface, 2) + 1);
            }
            if((xPiston > 0) || (controllPress < criticalPress)) {
                controllPress = criticalPress;
                orificeFlow = orificeSurface*Math.sqrt((systemPress - controllPress)*100000/ro);
                needleFlow = needleActiveSurface*Math.sqrt(controllPress*100000/ro);
                double pistonFlow = vPiston*controllSurface;	//calculate flow in due to piston movement
                pistonFlow = -orificeFlow + needleFlow - pistonFlow;	//calculate additional flow required to equalize forces
                double dpx = (pistonFlow*timeBase)/controllSurface;	//how much more piston need to move additionally to generate flow
                double Fp = (2*dpx/Math.pow(timeBase, 2))*pistonMass;	//force required to generate additional movement
                controllPress = (systemPress*systemSurface - pistonInitTens - xPiston*pistonYoung - Fp)/controllSurface;
                xPiston += dpx + vPiston*timeBase;
                vPiston += 2*dpx/timeBase;
            }
            orificeFlow = orificeSurface*Math.sqrt((systemPress - controllPress)*100000/ro);
            needleFlow = needleActiveSurface*Math.sqrt(controllPress*100000/ro);
            double vd = vNeedle*0.1;	//viscotic damping
            double fn = controllPress*needleSurface/10 - needleInitTens - xNeedle*needleYoung - vd;
            xNeedle += vNeedle*timeBase + (fn/needleMass)*timeBase*timeBase/2;
            vNeedle += (fn/needleMass)*timeBase;
            if(xNeedle < 0) {
                xNeedle = 0;
                vNeedle = 0;
            }
            if(xPiston < 0) {
                xPiston = 0;
                vPiston = 0;
            }
            timeLapsed += timeBase;
            if(timeLapsed >= (lastStep + step)) {
                lastStep = timeLapsed;
                sendDataPoint();
                if(gui.points.size() > 20000000) break;
            }
        }

    }

    void sendDataPoint() {
        gui.setDataPoint(new double[] {xNeedle*1000, xPiston*1000}, new double[] {vNeedle, needleFlow*0.06, vPiston, orificeFlow*0.06, controllPress, systemPress, bypassFlow*0.06});
    }

    public static void main(String[] args) {
        new Main();
    }

}
