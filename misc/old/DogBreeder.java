import net.talvi.roboducks.neurotic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class DogBreeder extends DogModel {

  // final static int numEnvs = 5;	// #environments to train on
  final static int numSteps = 32;	// how long to train for
  DogControl dogCont;
  TrainerControl trainCont;
  Net net;
  Population pop;
  DbPanel controlPanel;
  DogFitness fitness;
  int hiddens, popn, mean, range, muteRate, envs;

  public DogBreeder(DogControl dogCont,
		    TrainerControl trainCont) {
    fitness = new DogFitness();
    reInit(4, 48, 0, 4, 4, 20);
    controlPanel = new DbPanel(this);
    this.dogCont = dogCont;
    this.trainCont = trainCont;
    useCurrentBest();
  }

  void reInit(int hiddens, int popn, int mean, int range, int muteRate,
	      int envs) {
    this.hiddens = hiddens; this.popn = popn;
    this.mean = mean; this.range = range;
    this.muteRate = muteRate; this.envs = envs;
    net = new Net(5, hiddens, 2, true);
    pop = new Population(net, fitness, popn, mean, range, muteRate, envs);
  }

  void evolve() { pop.evolve(); }
  void evolve(int n) { pop.evolve(n); }
  int getCurrGen() { return pop.getCurrGen(); }
  int getPopn() { return pop.popSize(); }

  void update() { update(net, dogCont); }

  void update(Net network, DogControl control) {
    Pos dogPos = control.dog(0);
    Vec wall = wallPoint(dogPos.p);
    net.setIn(0, wall.abs() / control.arenaSize());
    // scaled distance to wall
    float theta = dogPos.d.angle() - wall.angle();
    // angle to wall, rel. to current facing
    if (theta > Math.PI) theta -= (float) ( 2 * Math.PI);
    if (theta < - Math.PI) theta += (float) (2 * Math.PI);
    // now have theta in [-pi,+pi], need to scale to [0,1]
    theta = (float) (theta / (2*Math.PI)) + 1;
    net.setIn(1, theta);
    net.think();
    theta = dogPos.d.angle();
    float r = dogPos.d.abs();
    r += (net.getOut(0) - 0.5f);
    theta += (net.getOut(1) - 0.5f);
    dogPos.d.x = (float) (r * Math.sin(theta));
    dogPos.d.y = (float) (r * Math.cos(theta));
    control.setDog(0, dogPos.d);
  }

  void postUpdate() {

  }

  class DogFitness implements Fitness {
    class Enviro {
	Pos[] dogs; Pos[] ducks;
	// NB: vars not init'ed, since we'll just clone into them
    }
    Enviro[] envs;		// environments to train

    // better as a constructor? But then it can't go in the interface...
    public void initEnviros(int n) {
      envs = new Enviro[n];
      for (int i=0; i<n; i++) {
	trainCont.resetRandom(true);
	envs[i] = new Enviro();
	envs[i].dogs = trainCont.cloneAllDogs();
	envs[i].ducks = trainCont.cloneAllDucks();
      }
    }

    public float score(Net net, int env) {
      trainCont.setAllDogs(envs[env].dogs);
      trainCont.setAllDucks(envs[env].ducks);
      return testScore(net, env);
    }

    float testScore(Net net, int env) {
      float score = 0;
      Pos oldPos = trainCont.dog(0), newPos;

      for (int i=0; i<numSteps; i++) {
	update(net, trainCont);
	trainCont.advance();
	newPos = trainCont.dog(0);
	float r = trainCont.arenaSize();
	if (newPos.p.abs() > r) // hit a wall
	  score -= 1000;
	float a = newPos.d.abs();
	if (a < 1.5) // moving at a reasonable rate
	  score += a * 20;
	/*
	a = newPos.d.angle() - oldPos.d.angle();
	if (a > Math.PI * 2)  a -= Math.PI * 2;
	if (a < -Math.PI * 2) a += Math.PI * 2;
	score -= 50 * Math.abs(a);
	*/

	oldPos = newPos;
      }
      return score;
    }

  }

  // return vector pointing to nearest point on wall
  // maybe this would be better off in the putative Arena class
  Vec wallPoint(Vec v) {
    // this is pretty easy if the arena's circular
    return v.unit().ti(trainCont.arenaSize()).mi(v);
  }

  void useCurrentBest() {
    net.setAllWeights(pop.fittest());
  }

}

class DbPanel extends JFrame {

  JLabel popL, maxL, minL, genL, stepL;
  JTextField popF, maxF, minF, genF, stepF;
  DogBreeder db;
  JToolBar toolbar;
  java.util.Timer timer;

  DbPanel(DogBreeder db) {
    super("Dog breeder control");
    this.db = db;
    popL = new JLabel("Population");
    maxL = new JLabel("Max. fitness");
    minL = new JLabel("Min. fitness");
    genL = new JLabel("Generation");
    stepL = new JLabel("Gen. step");
    popF = new JTextField();
    maxF = new JTextField();
    minF = new JTextField();
    genF = new JTextField();
    stepF = new JTextField("256",5);
    updateFields();

    JPanel p = new JPanel();
    p.setLayout(new GridLayout(5,2,4,4));
    p.add(popL); noEdit(popF); p.add(popF);
    p.add(maxL); noEdit(maxF); p.add(maxF);
    p.add(minL); noEdit(minF); p.add(minF);
    p.add(genL); noEdit(genF); p.add(genF);
    p.add(stepL); p.add(stepF);
    getContentPane().add(p, BorderLayout.NORTH);

    toolbar = new JToolBar();
    toolbar.setBorder(new EtchedBorder());
    toolbar.add(new EvolveStepAction("Evolve"));
    toolbar.add(new StartStopAction("Start"));
    toolbar.add(new ReInitAction("Reinit"));
    getContentPane().add(toolbar, BorderLayout.SOUTH);

    pack(); setVisible(true);
    timer = new java.util.Timer(true);	// kill timer when we exit
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void noEdit(JTextField t) {t.setEditable(false);}

  void updateFields() {
    popF.setText(String.valueOf(db.getPopn()));
    maxF.setText("FIX ME");
    minF.setText("FIX ME");
    genF.setText(String.valueOf(db.getCurrGen()));
  }

  class EvolveStepAction extends AbstractAction {
    public EvolveStepAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      int step;
      try { step = Integer.parseInt(stepF.getText()); }
      catch (NumberFormatException ex)
	{ stepF.setText(String.valueOf(step = 1)); }
      db.evolve(Integer.parseInt(stepF.getText()));
      genF.setText(String.valueOf(db.getCurrGen()));
      db.useCurrentBest();
    }
  }

  class StartStopAction extends AbstractAction {
    // Evolve evolve;
    Evolve evolution;
    UpdateGen updateGen;
    public StartStopAction(String t) {
      super(t);
      // putValue("NAME","Start");
    }
    public void actionPerformed(ActionEvent e) {
      if (((String) getValue(NAME)).equalsIgnoreCase("Start")) {
	// timer.schedule(evolve = new Evolve(), 0, 2);
	evolution = new Evolve();
	timer.schedule(updateGen = new UpdateGen(), 0, 1000);
	putValue(NAME,"Stop");
      } else {
	updateGen.cancel();
	evolution.pleaseStop();
	putValue(NAME,"Start");
      }
    }
  }
 
  class ReInitAction extends AbstractAction {
    public ReInitAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      SetupFrame setupFrame = new SetupFrame();
      setupFrame.show();
    }
  }

  class Evolve extends Thread {
    boolean running;
    public Evolve() {
      setDaemon(true);
      running = true;
      start();
    }
    public void run() {
      while (running) db.evolve();
      genF.setText(String.valueOf(db.getCurrGen()));
      db.useCurrentBest();
    }
    public void pleaseStop() { running=false; }
  }

  /*
  class Evolve extends java.util.TimerTask {
    public void run() {
      db.evolve();
    }
  }
  */

  class UpdateGen extends java.util.TimerTask {
    public void run()
    { genF.setText(String.valueOf(db.getCurrGen())); }
  }

  class SetupFrame extends JFrame {
    JToolBar stoolbar;
    JTextField hidF, popF, meanF, rangeF, muteF, envsF;

    //      reInit(4, 48, 0, 4, 4, 20);
    //  void reInit(int hiddens, int popn, int mean, int range, int muteRate,
    //      int envs) {

    SetupFrame() {
      super("Dog Breeder Setup");

      hidF = new JTextField(String.valueOf(db.hiddens));
      popF = new JTextField(String.valueOf(db.popn));
      meanF = new JTextField(String.valueOf(db.mean));
      rangeF = new JTextField(String.valueOf(db.range));
      muteF = new JTextField(String.valueOf(db.muteRate));
      envsF = new JTextField(String.valueOf(db.envs));

      JPanel p = new JPanel();
      p.setLayout(new GridLayout(6,2,4,4));
      p.add(new JLabel("Hidden nodes")); p.add(hidF);
      p.add(new JLabel("Population")); p.add(popF);
      p.add(new JLabel("Weight mean")); p.add(meanF);
      p.add(new JLabel("Weight range")); p.add(rangeF);
      p.add(new JLabel("Mut'n rate")); p.add(muteF);
      p.add(new JLabel("Evironments")); p.add(envsF);
      getContentPane().add(p, BorderLayout.NORTH);

      toolbar = new JToolBar();
      toolbar.setBorder(new EtchedBorder());
      toolbar.add(new CancelAction("Cancel"));
      toolbar.add(new OkAction("ReInit"));
      getContentPane().add(toolbar, BorderLayout.SOUTH);
      pack();
    }

    class CancelAction extends AbstractAction {
      public CancelAction(String t) { super(t); }
      public void actionPerformed(ActionEvent e) {
	dispose();
      }
    }

    class OkAction extends AbstractAction {
      public OkAction(String t) { super(t); }
      public void actionPerformed(ActionEvent e) {
	db.reInit(getInt(hidF, 4), getInt(popF, 48),
		  getInt(meanF, 0), getInt(rangeF, 4),
		  getInt(muteF, 4), getInt(envsF, 20));
	updateFields();
	dispose();
      }
      private int getInt(JTextField t, int d) {
	return Integer.parseInt(t.getText());
      }
    }

  }

}
