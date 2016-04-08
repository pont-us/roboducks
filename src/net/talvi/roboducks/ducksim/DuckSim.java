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

package net.talvi.roboducks.ducksim;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;

class RunSim extends Thread {
  boolean running;
  Arena arena;
  int interval;
  public RunSim(Arena arena, int interval) {
    setDaemon(true);
    this.arena = arena;
    this.interval = interval;
    running = false;
  }
  public void run() {
    running = true;
    while (running) {
      arena.update();
      try { Thread.sleep(interval); }
      catch (InterruptedException e) {}
    }
  }
  public void pleaseStop() { running = false; }
}

public class DuckSim {

  Arena arena;

  class StepAction extends AbstractAction {
    public StepAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      arena.update();
    }
  }

  class StartStopAction extends AbstractAction {
    RunSim runsim;
    public StartStopAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      if (((String) getValue(NAME)).equalsIgnoreCase("Start")) {
	runsim  = new RunSim(arena, 10);
	runsim.start();
	putValue(NAME,"Stop");
      } else {
	runsim.pleaseStop();
	putValue(NAME,"Start");
      }
    }
  }

  class ResetAction extends AbstractAction {
    public ResetAction(String t) { super(t); }
    public void actionPerformed(ActionEvent e) {
      arena.resetRandom(true);
      arena.repaint();
    }
  }

  public static void main(String[] args)
  { DuckSim ducksim = new DuckSim(); }

  public DuckSim() {
    arena = new Arena(10);
    AnimalModel duckModel = new ChengDucks(arena);
    arena.setDuckModel(duckModel);
    AnimalModel dogModel = new DogBreeder(arena, arena);
    arena.setDogModel(dogModel);

    try {
      UIManager.setLookAndFeel
	(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) { }

    // Control frame
    
    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    JFrame cframe = new JFrame("Duck control");
    cframe.getContentPane().add(toolbar);

    toolbar.setLayout(new GridLayout(3,1));
    toolbar.add(new StepAction("Step"));
    toolbar.add(new StartStopAction("Start"));
    toolbar.add(new ResetAction("Reset"));

    cframe.pack();
    cframe.setVisible(true);

    // Set windows to kill app on closure / Q keypress

    WindowAdapter killOnClose = new WindowAdapter() {
	public void windowClosing(WindowEvent e)
	{ System.exit(0); }
      };
    cframe.addWindowListener(killOnClose);

    KeyListener killOnQ = new KeyAdapter() {
	public void keyTyped(KeyEvent e) {
	  if (e.getKeyChar() == 'q' ||
	      e.getKeyChar() == 'Q' ) System.exit(0);
	}
      };
    cframe.addKeyListener(killOnQ);
  }
}
