/* = = = = >
 * This file is part of roboducks, an animal flocking and herding
 * simulator. Copyright 2001, 2016 Pontus Lurcock.
 *
 * roboducks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * roboducks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with roboducks. If not, see <http://www.gnu.org/licenses/>.
 * < = = = = */

// DogBreeder

/* Framework for genetically developing neural networks for dog
   control. Includes a user interface, DbPanel, as an inner class.
   Servant classes are used for the fitness function and for the
   actual network / dog-control interface. */

package net.talvi.roboducks.ducksim;

import net.talvi.roboducks.neurotic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

public class DogBreeder extends AnimalModel {

  DogControl dogCont;
  TrainerControl trainCont;
  Net net;
  Population pop;
  DbPanel controlPanel;
  Fitness fitness;
  int hiddens, popn, mean, range, muteRate, envs, tSteps;
  final static float PI = (float) Math.PI;
  ControlInterface contInt;;

  public DogBreeder(DogControl dogCont,
		    TrainerControl trainCont) {
    this.dogCont = dogCont;
    this.trainCont = trainCont;
    contInt = new DogControlIface(dogCont, trainCont);
    reInit(contInt.inputs(), 48, 0, 4, 4, 20, 64);
    fitness = new HerdDucksFitness(trainCont, tSteps, this, new Vec());
    // fitness = new AvoidWallsFitness(trainCont, tSteps, this, new Vec());
    createNets();
    controlPanel = new DbPanel(this);
    useCurrentBest();
  }

  void reInit(int hiddens, int popn, int mean, int range,
	      int muteRate, int envs, int tSteps) {
    this.hiddens = hiddens; this.popn = popn;
    this.mean = mean; this.range = range;
    this.muteRate = muteRate; this.envs = envs;
    this.tSteps = tSteps;
  }

  void createNets() {
    net = new Net(contInt.inputs(), hiddens, 2, true);
    pop = new Population(net, fitness, popn, mean,
			 range, muteRate, envs);
  }

  void evolve() { pop.evolve(); }
  void evolve(int n) { pop.evolve(n); }
  int getCurrGen() { return pop.getCurrGen(); }
  int getPopn() { return pop.popSize(); }
  float getMaxFitness() { return pop.fitness[pop.fittest]; }
  float getMinFitness() { return pop.fitness[pop.leastFit]; }

  public interface ControlInterface {
    void update(Net n, DogControl c);
    int inputs();
  }

  void update(Net n, DogControl c) { contInt.update(n, c); }
  void update() { update(net, dogCont); }

  void postUpdate() {  }

  void useCurrentBest() {
    net.setAllWeights(pop.weights[pop.fittest]);
  }

  // Save current parameters and weights to a file
  void save(File f) {
    try {
      Writer w  = new FileWriter(f);
      w.write(hiddens +" "+ popn +" "+ mean +" "+ range +" "+
	      muteRate +" "+ envs +" "+ tSteps + "\n");
      pop.save(w);
    }
    catch (IOException e) { throw new Error(e.getMessage()); }
  }
  
  // Load parameters and weights from a file
  // format: hiddens, popn, mean, range, muteRate, envs, tSteps
  void load(File f) {
    try {
      Reader reader = new FileReader(f);
      StreamTokenizer in = new StreamTokenizer(reader);

      hiddens = readInt(in);
      popn = readInt(in);
      mean = readInt(in);
      range = readInt(in);
      muteRate = readInt(in);
      envs = readInt(in);
      tSteps = readInt(in);
      net = new Net(contInt.inputs(), hiddens, 2, true);
      pop = new Population(net, fitness, envs, reader);
      useCurrentBest();
      controlPanel.updateFields();
    }
    catch (IOException e) { throw new Error(e.getMessage()); }
  }

  private int readInt(StreamTokenizer i)
    throws java.io.IOException
  { i.nextToken(); return (int) i.nval; }

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
    toolbar.add(new LoadSaveAction("Load"));
    toolbar.add(new LoadSaveAction("Save"));

    getContentPane().add(toolbar, BorderLayout.SOUTH);

    pack(); setVisible(true);
    timer = new java.util.Timer(true);	// kill timer when we exit
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void noEdit(JTextField t) {t.setEditable(false);}

  void updateFields() {
    popF.setText(String.valueOf(db.getPopn()));
    maxF.setText(String.valueOf(db.getMaxFitness()));
    minF.setText(String.valueOf(db.getMinFitness()));
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
      db.useCurrentBest();
      updateFields();
    }
  }

  class StartStopAction extends AbstractAction {

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
	db.trainCont.startTraining();
      } else {
	updateGen.cancel();
	evolution.pleaseStop();
	putValue(NAME,"Start");
	db.trainCont.stopTraining();
      }
    }
  }
 
  class ReInitAction extends AbstractAction {
    public ReInitAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      SetupFrame setupFrame = new SetupFrame();
      setupFrame.setVisible(true);
    }
  }

  class LoadSaveAction extends AbstractAction {
    public LoadSaveAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      if (((String) getValue(NAME)).equalsIgnoreCase("Load")) {
	if (chooser.showOpenDialog(DbPanel.this) == 0)
	  db.load(chooser.getSelectedFile());
      } else if (chooser.showSaveDialog(DbPanel.this) == 0)
	db.save(chooser.getSelectedFile());
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
      while (running) { db.evolve(); updateFields(); }
      db.useCurrentBest();
    }
    public void pleaseStop() { running=false; }
  }

  class UpdateGen extends java.util.TimerTask {
    public void run()
    { genF.setText(String.valueOf(db.getCurrGen())); }
  }

  class SetupFrame extends JFrame {
    JToolBar stoolbar;
    JTextField hidF, popF, meanF, rangeF, muteF, envsF, stepsF;

    SetupFrame() {
      super("Dog Breeder Setup");

      hidF = new JTextField(String.valueOf(db.hiddens));
      popF = new JTextField(String.valueOf(db.popn));
      meanF = new JTextField(String.valueOf(db.mean));
      rangeF = new JTextField(String.valueOf(db.range));
      muteF = new JTextField(String.valueOf(db.muteRate));
      envsF = new JTextField(String.valueOf(db.envs));
      stepsF = new JTextField(String.valueOf(db.tSteps));

      JPanel p = new JPanel();
      p.setLayout(new GridLayout(7,2,4,4));
      p.add(new JLabel("Hidden nodes")); p.add(hidF);
      p.add(new JLabel("Population")); p.add(popF);
      p.add(new JLabel("Weight mean")); p.add(meanF);
      p.add(new JLabel("Weight range")); p.add(rangeF);
      p.add(new JLabel("Mut'n rate")); p.add(muteF);
      p.add(new JLabel("Evironments")); p.add(envsF);
      p.add(new JLabel("Sim'n length")); p.add(stepsF);
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
		  getInt(muteF, 4), getInt(envsF, 20),
		  getInt(stepsF, 32));
	db.createNets();
	updateFields();
	dispose();
      }
      private int getInt(JTextField t, int d) {
	return Integer.parseInt(t.getText());
      }
    }

  }

}
