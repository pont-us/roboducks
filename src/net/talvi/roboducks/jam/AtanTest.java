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

package net.talvi.roboducks.jam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

public class AtanTest extends JFrame {

  JLabel angleLabel;
  JTextField angleView;
  Line2D.Float line;

  public AtanTest() {

    super("Angle test");
    setSize(256,256);
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  Window w = e.getWindow();
	  w.setVisible(false);
	  w.dispose();
	  System.exit(0);
	}});

    angleView = new JTextField("66.6");
    angleView.setEditable(false);
    angleLabel = new JLabel("Angle: ");
    angleLabel.setLabelFor(angleView);

    Container pane = getContentPane();
    pane.setLayout(new FlowLayout(FlowLayout.LEFT));
    pane.add(angleLabel);
    pane.add(angleView);
    pane.addMouseListener(new mouseListener());
    keyListener k = new keyListener();
    addKeyListener(k);
    angleView.addKeyListener(k);
    /*
    JRootPane root = getRootPane();
    root.registerKeyboardAction(new QuitAction(),
				KeyStroke.getKeyStroke('q'),
				root.WHEN_FOCUSED);
    */
    line = new Line2D.Float(127,127,127,127);
  }

  public class QuitAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      /*
      Window w = AtanTest.this;
      w.setVisible(false);
      w.dispose();
      System.exit(0);
      */
      System.out.println("q pressed\n");
    }
  }

  public class mouseListener extends MouseInputAdapter {
    public void mouseClicked(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      angleView.setText(Math.atan2(x-127,y-127) + "");
      angleLabel.setText(y + " : ");
      line.x2 = x;
      line.y2 = y;
      repaint();
      // System.out.println(x + ", " + y + "\n");
    }
  }

  public class keyListener extends KeyAdapter {
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == 'Q' || e.getKeyChar() == 'q') {
	  Window w = AtanTest.this;
	  w.setVisible(false);
	  w.dispose();
	  System.exit(0);
      }
    }
  }

  public void paint(Graphics g0) {
    super.paint(g0);
    Graphics2D g = (Graphics2D) getContentPane().getGraphics();
    g.drawOval(0,0,255,255);
    g.drawLine(0,127,255,127);
    g.drawLine(127,0,127,255);

    float r, t;
    final Line2D.Float l = line;
    r = (float) Math.sqrt( (l.x2-l.x1)*(l.x2-l.x1) +
			   (l.y2-l.y1)*(l.y2-l.y1) );
    t = (float) Math.atan2(l.x2-l.x1, l.y2-l.y1);
    g.draw(new Line2D.Float
	   ((float) (l.x1 + r * Math.sin(t) * 0.9),
	    (float) (l.y1 + r * Math.cos(t) * 0.9),
	    (float) (l.x1 + r * Math.sin(t) * 1.1),
	    (float) (l.y1 + r * Math.cos(t) * 1.1) ));

    g.draw(new Line2D.Float
	   ((float) (l.x1 + r * Math.sin(t-0.1)),
	    (float) (l.y1 + r * Math.cos(t-0.1)),
	    (float) (l.x1 + r * Math.sin(t+0.1)),
	    (float) (l.y1 + r * Math.cos(t+0.1)) ));
    
    // g.draw(line);
  }

  public static void main(String[] argv) {
    AtanTest a = new AtanTest();
    a.setVisible(true);
    a.requestFocus();
  }

}
