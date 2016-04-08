import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;

class Ducksplay extends JPanel {

    ModelState state;

    public Ducksplay() {
	super();
	state = new ModelState(10);
    }

    public void paintComponent(Graphics g_old) {
	clear(g_old);
	Graphics2D g = (Graphics2D)g_old;
	int size = getWidth()<getHeight() ? getWidth() : getHeight();
	int centre = size/2;
	int radius = centre;
	
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			   RenderingHints.VALUE_ANTIALIAS_ON);
	g.setColor(Color.black);
	g.setStroke(new BasicStroke(4));
	g.draw(new Ellipse2D.Float(2,2,size-4,size-4));

	for (int i=0; i<state.ducks(); i++)
	    drawDuck(size, g, state.duck(i));
	for (int i=0; i<state.dogs(); i++)
	    drawDog(size, g, state.dog(i));

	// Image dogpic = getToolkit().createImage("dog.gif");
	// AffineTransform picpos =
	// AffineTransform.getTranslateInstance(centre + state.dog.x * radius,
	//					 centre+state.dog.y *radius);
	// picpos.scale(((double) size)/500,((double) size)/500);
	// g.drawImage(dogpic, picpos, this);
	
    }

    private void drawDog(int size, Graphics2D g, Pos p) {
	final float r = size / 50;
	final float x = (size/2) + p.p.x * (size/2) / state.arenaSize();
	final float y = (size/2) + p.p.y * (size/2) / state.arenaSize();

	g.setColor(Color.black);
	g.setStroke(new BasicStroke(4));
	g.draw(new Ellipse2D.Float(x-r, y-r, 2*r, 2*r));
	g.setColor(Color.red);
	g.setStroke(new BasicStroke(2));
	g.draw(new Line2D.Float(x, y,
				x+ r*p.d.x*30/state.arenaSize(),
				y + r*p.d.y*30/state.arenaSize()));
    }

    private void drawDuck(int size, Graphics2D g, Pos p) {
	final float r = size / 50;
	final float x = (size/2) + p.p.x * (size/2) / state.arenaSize();
	final float y = (size/2) + p.p.y * (size/2) / state.arenaSize();

	g.setColor(Color.blue);
	g.setStroke(new BasicStroke(2));
	g.draw(new Ellipse2D.Float(x-r, y-r, 2*r, 2*r));
	g.setColor(Color.red);
	g.setStroke(new BasicStroke(1));
	g.draw(new Line2D.Float(x, y,
				x+ r*p.d.x*5,
				y + r*p.d.y*5));
    }

    // super.paintComponent clears offscreen pixmap,
    // since we're using double buffering by default.

    protected void clear(Graphics g) {
	super.paintComponent(g);
    }

}

class RunSim extends Thread {

    boolean running;
    Simulation sim;
    Ducksplay canvas;
    int interval;

    public RunSim(Simulation sim, Ducksplay canvas, int interval) {
	setDaemon(true);
	this.sim = sim;
	this.canvas = canvas;
	this.interval = interval;
	running = false;
    }

    public void run() {
	running = true;
	while (running) {
	    sim.update();
	    canvas.repaint();
	    try { Thread.sleep(interval); }
	    catch (InterruptedException e) {}
	}
    }

    public void pleaseStop() { running = false; }
}

class Simulation {
    ModelState state;
    private DuckModel duckModel;
    private DogModel dogModel;
    int time;
    
    Simulation(ModelState state, DuckModel duckModel, DogModel dogModel) {
	this.state = state;
	this.duckModel = duckModel;
	this.dogModel = dogModel;
	this.time = 0;
    }

    public void update() {
	time++;
	duckModel.update();
	dogModel.update();
	state.advance();
	duckModel.postUpdate();
	dogModel.postUpdate();
    }

}

public class DuckSim {

  private static JFrame
  init_ui(final Ducksplay canvas, final Simulation ducksim) {

    try {
      UIManager.setLookAndFeel
	(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) { }

    // Display frame

    JFrame dframe = new JFrame("Duck Display");
    dframe.getContentPane().setLayout(new BorderLayout());
    dframe.getContentPane().add(canvas, "Center");
    dframe.setBackground(Color.green);
    canvas.setBackground(Color.white);
    canvas.setSize(256, 256);
    canvas.setPreferredSize(new Dimension(256, 256));
    dframe.pack();
    dframe.setVisible(true);

    // Control frame

    JFrame cframe = new JFrame("Duck control");
    cframe.getContentPane().setLayout(new GridLayout(3,1));

    JButton stepbut = new JButton("Step");
    cframe.getContentPane().add(stepbut);
    stepbut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  ducksim.update();
	  canvas.repaint();
	}
      });

    JButton runbut = new JButton(" Run ");
    cframe.getContentPane().add(runbut);
    runbut.addActionListener(new ActionListener() {
	private boolean running = false;
	private RunSim runsim;
	public void actionPerformed(ActionEvent e) {
	  if (running = !running) {
	    runsim  = new RunSim(ducksim, canvas, 10);
	    runsim.start();
	    ((AbstractButton) (e.getSource())).setText("Stop");
	  } else {
	    runsim.pleaseStop();
	    ((AbstractButton) (e.getSource())).setText("Run");
	  }
	}
      });

    JButton resetbut = new JButton("Reset");
    cframe.getContentPane().add(resetbut);
    resetbut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  ducksim.state.resetRandom(true);
	  canvas.repaint();
	}
      });

    cframe.pack();
    cframe.setVisible(true);

    // Set windows to kill app on closure / Q keypress

    WindowAdapter killOnClose = new WindowAdapter() {
	public void windowClosing(WindowEvent e)
	{ System.exit(0); }
      };
    dframe.addWindowListener(killOnClose);
    cframe.addWindowListener(killOnClose);

    KeyListener killOnQ = new KeyAdapter() {
	public void keyTyped(KeyEvent e) {
	  if (e.getKeyChar() == 'q' ||
	      e.getKeyChar() == 'Q' ) System.exit(0);
	}
      };
    dframe.addKeyListener(killOnQ);
    cframe.addKeyListener(killOnQ);

    return dframe;
  }

  public static void main(String[] args) {

    class DeadDog extends DogModel {
      void update() {}
      void postUpdate() {}
    }

    class MadDog extends DogModel {
      private java.util.Random rand = new java.util.Random();
      DogControl state;

      MadDog(DogControl state)
      { this.state = state; }

      void update() {
	for (int i=0; i<state.dogs(); i++) {
	  Pos p = state.dog(i);
	  p.d.x += rand.nextFloat() * 2 - 1 - 
	    p.p.x/state.arenaSize();
	  p.d.y += rand.nextFloat() * 2 - 1 -
	    p.p.y/state.arenaSize();
	  if (p.d.abs()>20) p.d = p.d.ti(0.6f);
	  state.setDog(i,p.d);
	}
      }

      void postUpdate() {}
    }

    class DaffyDucks extends DuckModel {
      DuckControl state;
      DaffyDucks(DuckControl state)
      { this.state = state; }
      void update() {}
      void postUpdate() {}
    }

    final Ducksplay canvas = new Ducksplay();
    final Simulation ducksim =
      new Simulation(canvas.state,
		     // new ChengDucks(canvas.state),
		     new DaffyDucks(canvas.state),
		     // new MadDog(canvas.state));
		     new DogBreeder(canvas.state, canvas.state));
    /*
      new OrigDog(canvas.state,
      new Vec(canvas.state.arenaSize()/2,
      canvas.state.arenaSize()/10)));
    */
    JFrame frame = init_ui(canvas, ducksim);

  }
}
