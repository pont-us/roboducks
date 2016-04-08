// FUCK THIS, I'm writing a new one.

package net.talvi.roboducks.neurotic;

class Debug {
  public static boolean on = false;
  public static boolean trace = false;
  public static int t = 0;
}

/* Neurotic:
   A package for creating, training and using simply recurrent
   3-layer neural networks.
*/

/* An Axon is either a neuron or an input node: anything
   that can provide an input to a neuron. All we care is
   that the neuron at the other end of the forward connection
   can get at its output. */
interface Axon {
  float newvalue();
  float oldvalue();
  float delta();
  void addOutput(Neuron x, int index);
  public float ancientvalue();
}

class InputNode implements Axon {
  float value;
  Neuron[] outputs;
  int[] windex;
  float delta;

  public float delta() { return delta; }
  public InputNode(float value) {
    this.value = value;
    outputs = new Neuron[0];
    windex = new int[0];
  }
  public void set(float value)
  { this.value = value; }
  public float newvalue()
  { return value; }
  public float oldvalue()
  { return value; }
  public float ancientvalue()
  { return value; }
  public void addOutput(Neuron x, int index) {
    Neuron[] os = new Neuron[outputs.length+1];
    int[] ws = new int[windex.length+1];
    System.arraycopy(outputs, 0, os, 0, outputs.length);
    System.arraycopy(windex, 0, ws, 0, windex.length);
    os[outputs.length] = x;
    ws[windex.length] = index;
    outputs = os; windex = ws;
    // Yecch. Could use a Vector and make it neater, but would
    // that slow things down on the time-critical propagation bits?
  }
}

class Neuron implements Axon {
  Axon[] inputs;
  float[] weights;
  Neuron[] outputs;
  int[] windex;
  Net net; // parent network, to determine activation fn & suchlike
    
  public Neuron(Axon[] inputs, Net net) {
    this.net = net;
    this.inputs = inputs;
    this.weights = new float[inputs.length];
    outputs = new Neuron[0];
    windex = new int[0];
    for (int i=0; i<inputs.length; i++) {
      weights[i] = myRand();
      inputs[i].addOutput(this,i);
    }
    oldvalue = 0.5f;	// should maybe init. to a constructor arg?
  }

  // This makes it easier to create self-referential neurons
  // (only intended for use during initialization)
  public void addInputs(Axon[] xs) {
    Axon[] t = new Axon[inputs.length + xs.length];
    float[] f = new float[weights.length + xs.length];
    System.arraycopy(inputs, 0, t, 0, inputs.length);
    System.arraycopy(xs, 0, t, inputs.length, xs.length);
    System.arraycopy(weights, 0, f, 0, weights.length);
    for (int i=0; i<xs.length; i++) {
      f[i+weights.length] = myRand();
      t[i+weights.length].addOutput(this,i+weights.length);
    }
    inputs = t; weights = f;
  }

  public void addOutput(Neuron x, int index) {
    Neuron[] os = new Neuron[outputs.length+1];
    int[] ws = new int[windex.length+1];
    System.arraycopy(outputs, 0, os, 0, outputs.length);
    System.arraycopy(windex, 0, ws, 0, windex.length);
    os[outputs.length] = x;
    ws[windex.length] = index;
    outputs = os; windex = ws;
    // Yecch. Could use a Vector and make it neater, but would
    // that slow things down on the time-critical propagation bits?
  }

  /* We'll seldom be interested in the return value of this function.
     The important thing is the cached newvalue, which becomes the
     oldvalue when tick() is called. This allows new values to be
     computed for all neurons in a layer before they're updated,
     avoiding the cycles that would otherwise crop up in recurrent
     layers. */
  public float newvalue() {
    float sum = 0;
    for (int i=0; i<inputs.length; i++)
      sum += weights[i] * inputs[i].oldvalue();
    if (Debug.on) {
      for (int i=0; i<inputs.length; i++)
	System.err.println(weights[i]+" * "+inputs[i].oldvalue());
      System.err.println("------------------------");
    }
    if (Float.isNaN(sum)) {
      for (int i=0; i<inputs.length; i++)
	System.err.println(weights[i]+" * "+inputs[i].oldvalue());
      throw new Error("Oh shit...");
    }
    newsum = sum;
    newvalue = net.act.f(sum);
    return newvalue;
  }

  /* Effectively says "I've finished with your layer, so update
     yourself so the next layer can use your new values".
     (TODO: would things be simpler if neurons knew which layer
     they were in?) */
  void tick() {
    ancientvalue=oldvalue;
    /* this is getting incredibly confusing. Good thing I planned
       to throw one away anyway. */
    oldvalue=newvalue;
  }

  public float oldvalue() { return oldvalue; }
  public float ancientvalue() { return ancientvalue; }

  float oldvalue;	// initialized in constructor
  float newvalue;
  float ancientvalue; // yes, it's getting messy. Needs a redesign

  float newsum; // these two needed for backprop
  float delta = 0; 

  public float delta() { return delta; }

  java.util.Random rand = new java.util.Random();
  float myRand() {
    return rand.nextFloat() * 0.2f - 0.1f; // range -0.1 to 0.1
  }

}

public class Net { // 3-layer neural nets
  InputNode[] ilyr; // input layer
  Neuron[] hlyr; // hidden layer
  Neuron[] olyr; // output layer

  /* Network layout is like Meeden '96, which is essentially same
     as Elman '90. */
  public Net(int inputs, int hiddens, int outputs, boolean recurrent) {
    ilyr = new InputNode[inputs+1]; // extra one for threshold bias
    hlyr = new Neuron[hiddens];
    olyr = new Neuron[outputs];

    // first, create the input layer
    for (int i=0; i<inputs; i++)
      ilyr[i] = new InputNode(0);
    ilyr[inputs] = new InputNode(1); // bias

    // now create the hidden layer and connections from the input
    for (int i=0; i<hiddens; i++) {
      hlyr[i] = new Neuron(ilyr, this);
    }

    // recurrent connections from the hidden layer to itself
    if (recurrent) {
      for (int i=0; i<hiddens; i++) {
	hlyr[i].addInputs(hlyr);
      }
    }

    // connections from the hidden layer to the output layer
    for (int i=0; i<outputs; i++) {
      olyr[i] = new Neuron(hlyr, this);
      olyr[i].addInputs(new InputNode[] { ilyr[inputs] }); // bias
    }

    if (Debug.trace) {
      System.err.print("#T ");
      for (int i=0; i<hlyr.length; i++) {
	for (int j=0; j<hlyr[i].inputs.length; j++) {
	  Axon a = hlyr[i].inputs[j];
	  boolean found=false;
	  int n = 0;
	  for (int k=0; k<ilyr.length-1; k++)
	    if (ilyr[k]==a) {found=true; n=k;}
	  if (found==true) System.err.print("I"+n);
	  else {
	    for (int k=0; k<hlyr.length; k++)
	      if (hlyr[k]==a) {found=true; n=k;}
	    if (found==true) System.err.print("H"+n);
	    else System.err.print("Bi");
	  }
	  System.err.print("H"+i+" ");
	}
      }
      for (int i=0; i<olyr.length; i++) {
	for (int j=0; j<olyr[i].inputs.length; j++) {
	  Axon a = olyr[i].inputs[j];
	  boolean found=false;
	  int n = 0;
	  for (int k=0; k<hlyr.length; k++)
	    if (hlyr[k]==a) {found=true; n=k;}
	  if (found==true) System.err.print("H"+n);
	  else System.err.print("Bi");
	  System.err.print("O"+i+" ");
	}
      }
    }
    System.err.print("\n");
  }

  // functions for accessing input/output values
  public void setIn(int node, float value) { ilyr[node].value = value; }
  public float getIn(int node) { return ilyr[node].value; }
  public float getOut(int node) { return olyr[node].oldvalue(); }
    
  /* run a forward-propagation. It's assumed that the appropriate
     input values have already been set and the output values
     will be extracted afterwards. */
  public void think() {
    /* Since the hidden nodes are connected to each other, we
       need to calculate all the new values (using the old values)
       before actually setting them. */

    if (Debug.on) System.err.println("Calculating hidden layer ");
    for (int i=0; i<hlyr.length; i++) {
	if (Debug.on) System.err.println("Hidden node "+i);
	hlyr[i].newvalue();
    }
    for (int i=0; i<hlyr.length; i++) hlyr[i].tick();

    /* This trick isn't necessary for the non-recurrent output
       layer, but a separate OutputNeuron class would be more
       hassle (probably) */
    if (Debug.on) System.err.println("Calculating output layer");
    for (int i=0; i<olyr.length; i++) {
      if (Debug.on) System.err.println("Output node "+i);
      olyr[i].newvalue();
    }
    for (int i=0; i<olyr.length; i++) olyr[i].tick();
  }

  /* Run a backprop against given (correct) output values.
     Again, it's assumed the inputs have already been set. */
  public void train(float[] desired) {

    if (Debug.trace) {	// Dump weights to stderr
      System.err.print((Debug.t++)+" ");
      for (int i=0; i<hlyr.length; i++)
	for (int j=0; j<hlyr[i].inputs.length; j++)
	  System.err.print(hlyr[i].weights[j]+" ");
      for (int i=0; i<olyr.length; i++)
	for (int j=0; j<olyr[i].inputs.length; j++)
	  System.err.print(olyr[i].weights[j]+" ");
      System.err.print("\n");
    }

    // 1. Run forward propagation
    if (Debug.on) System.err.println("Performing forward propagation");      
    think();

    // 2. Compute weight changes (the tricky bit)

    if (Debug.on) System.err.println("Computing weight changes...");

    if (Debug.on) System.err.println("Output layer...");

    for (int i=0; i<olyr.length; i++)
	olyr[i].delta =
	    (desired[i] - olyr[i].newvalue) * act.dfdx(olyr[i].oldvalue());

    if (Debug.on) System.err.println("Hidden layer...");

    float[] newdelta = new float[hlyr.length];
    // maybe this will do the trick -- 30.1.01
    for (int i=0; i<hlyr.length; i++) {
	// hlyr[i].delta = 0;
	newdelta[i] = 0;
      for (int j=0; j<hlyr[i].outputs.length; j++)
	  //hlyr[i].delta += hlyr[i].outputs[j].delta *
	  //  hlyr[i].outputs[j].weights[hlyr[i].windex[j]]; // yecch
	  newdelta[i] += hlyr[i].outputs[j].delta *
	    hlyr[i].outputs[j].weights[hlyr[i].windex[j]]; // yecch
      // hlyr[i].delta *= act.dfdx(hlyr[i].newsum);
      newdelta[i] *= act.dfdx(hlyr[i].ancientvalue);
    }

    for (int i=0; i<hlyr.length; i++) hlyr[i].delta = newdelta[i];

    /*
    for (int i=0; i<ilyr.length; i++) {
      hlyr[i].delta = 0;
      for (int j=0; j<ilyr[i].outputs.length; j++)
	ilyr[i].delta += ilyr[i].outputs[j].delta *
	  ilyr[i].outputs[j].weights[ilyr[i].windex[j]];
      ilyr[i].delta *= act.dfdx(ilyr[i].newsum);
    }
    */

    if (Debug.on) {
      System.err.println("Deltas:");
      System.err.print("O ");
      for (int i=0; i<olyr.length; i++) System.err.print(olyr[i].delta()+" ");
      System.err.print("\nH ");
      for (int i=0; i<hlyr.length; i++) System.err.print(hlyr[i].delta()+" ");
      System.err.print("\n");
    }

    // 3. Apply weight changes
    if (Debug.on) System.err.print("Applying weight changes... ");
    for (int i=0; i<hlyr.length; i++) {
      for (int j=0; j<hlyr[i].weights.length; j++)
	hlyr[i].weights[j] += rate * hlyr[i].delta() *
	  hlyr[i].inputs[j].oldvalue();
    }

    for (int i=0; i<olyr.length; i++)
      for (int j=0; j<olyr[i].weights.length; j++)
	olyr[i].weights[j] += rate * olyr[i].delta() *
	  olyr[i].inputs[j].oldvalue();

    if (Debug.on) System.err.println("done");

  }

  /* A little class to let us plug in different activation
     functions. */
  public abstract class Activation {
    abstract float f(float x); // the activation function
    abstract float dfdx(float x); // its first derivative
  }

  /* Our standard activation function */
  private class Sigmoid extends Activation {
    float f(float x) {
      float fx;
      fx = (float) (1/(1 + java.lang.Math.exp(-x)));
      if (Float.isNaN(fx)) throw new Error("Oh shit... "+x+"\n");
      return fx;
    }
    float dfdx(float x)
    { return x*(1-x); }
  }

  Activation act = new Sigmoid();

  public float rate = 1f; // learning rate parameter

}
