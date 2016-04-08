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

// ArenaView: view class for Arena.

package net.talvi.roboducks.ducksim;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.font.*;

class ArenaView extends JFrame {

  ArenaPanel p;			// the actual arena display
  private boolean active;	// whether we should be updating
  static boolean trace = true;

  public ArenaView(Arena a) {
    super("Duck Display");
    active = true;
    p = new ArenaPanel(a);
    setContentPane(p);
    p.reset();
    pack();
    setVisible(true);
  }

  public void setActive(boolean a) { active = a; repaint(); }
  public boolean isActive() { return active; }
  public void reset() { p.reset(); }

  class ArenaPanel extends JPanel {

    // whether to produce a trace of the dog's motion
    GeneralPath tracePath;
    Arena arena;
    Font f = new Font("SansSerif",Font.PLAIN,14);
    int nextPathUpdate;

    ArenaPanel(Arena a) {
      super();
      arena = a;
      setSize(256, 256);
      setPreferredSize(new Dimension(256, 256));
      tracePath = trace ? new GeneralPath(0, 1000) : null;
    }

    public void reset() {
      tracePath.reset();
      nextPathUpdate = 0;
    }

    public void paintComponent(Graphics oldGraphics) {
      Graphics2D g = (Graphics2D) oldGraphics;
      int size = getWidth()<getHeight() ? getWidth() : getHeight();
      int centre = size/2;
      int radius = centre;

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			 RenderingHints.VALUE_ANTIALIAS_ON);
      clear(g);

      g.setColor(Color.black);
      g.setFont(f);
      g.setStroke(new BasicStroke(4));
      g.draw(new Ellipse2D.Float(2,2,size-4,size-4));

      if (active) {
	if (trace) {
	  final float x = arena.dogPos(0).x;
	  final float y = arena.dogPos(0).y;
	  g.setColor(Color.darkGray);
	  g.setStroke(new BasicStroke(1));
	  if (arena.time()>=nextPathUpdate) {
	    if (nextPathUpdate==0) tracePath.moveTo(x,y);
	    else tracePath.lineTo(x,y);
	    nextPathUpdate += 10;
	  }
	  AffineTransform a = new AffineTransform();
	  a.translate(centre, centre);
	  a.scale(radius/arena.arenaSize(), radius/arena.arenaSize());
	  g.draw(tracePath.createTransformedShape(a));
	}
	for (int i=0; i<arena.ducks(); i++)
	  drawDuck(size, g, arena.duck(i));
	for (int i=0; i<arena.dogs(); i++)
	  drawDog(size, g, arena.dog(i));
	g.setColor(Color.black);
	g.drawString(String.valueOf(arena.time()), 4, 16);
      } else {
	drawText(g, "Display inactive");
      }

    }

    private void drawText(Graphics2D g, String text) {
      Rectangle2D box = getBounds();
      FontRenderContext frc = g.getFontRenderContext();
      Rectangle2D bounds = f.getStringBounds(text,frc);
      LineMetrics metrics = f.getLineMetrics(text,frc);
      float width = (float) bounds.getWidth();
      float lineheight = metrics.getHeight();
      float ascent = metrics.getAscent();
      float x0 = (float) (box.getX() + (box.getWidth() - width)/2);
      float y0 = (float) (box.getY() + (box.getHeight() - lineheight)/2
			  + ascent);
      g.drawString(text, x0, y0);
    }

    private void drawDog(int size, Graphics2D g, Pos p) {
      final float r = size / 50;
      final float x = (size/2) + p.p.x * (size/2) / arena.arenaSize();
      final float y = (size/2) + p.p.y * (size/2) / arena.arenaSize();

      g.setColor(Color.black);
      g.setStroke(new BasicStroke(4));
      g.draw(new Ellipse2D.Float(x-r, y-r, 2*r, 2*r));
      g.setColor(Color.red);
      g.setStroke(new BasicStroke(1));
      g.draw(new Line2D.Float(x, y,
			      x+ r*p.d.x * 2f,
			      y + r*p.d.y * 2f ));
      
    }

    private void drawDuck(int size, Graphics2D g, Pos p) {
      final float r = size / 60;
      final float x = (size/2) + p.p.x * (size/2) / arena.arenaSize();
      final float y = (size/2) + p.p.y * (size/2) / arena.arenaSize();

      g.setColor(Color.blue);
      g.setStroke(new BasicStroke(2));
      g.draw(new Ellipse2D.Float(x-r, y-r, 2*r, 2*r));
      g.setColor(Color.red);
      g.setStroke(new BasicStroke(1));
      g.draw(new Line2D.Float(x, y,
			      x+ r*p.d.x*2,
			      y + r*p.d.y*2));
    }

    // super.paintComponent clears offscreen pixmap,
    // since we're using double buffering by default.
    protected void clear(Graphics g) {
      super.paintComponent(g);
    }

  }

}
