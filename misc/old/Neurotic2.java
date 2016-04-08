/* Neurotic:
   A class for creating, training and using simply recurrent
   3-layer neural networks.
*/

class Neurotic {

  // An Axon is either a neuron or an input node: anything
  // that can provide an input to a neuron. All we care is
  // that the neuron at the other end of the forward connection
  // can get at its output.
  interface Axon {
    float newvalue();
    float oldvalue();
  }

  class InputNode implements Axon {
    float value;
    public InputNode(float value)
    { this.value = value; }
    public set(float value)
    { this.value = value; }
    public float newvalue()
    { return value; }
    public float oldvalue()
    { return value; }
  }

  /* A synapse is one of the inputs to a neuron. It has an input
     Axon, a weight, and a `recurrent' flag (true if the 
     connection is not strictly forwards).
     TODO: probably simpler to subsume this into the Neurons. */
  class Synapse {
    private Axon input;
    float weight;
    boolean recurrent;
    public Synapse(Axon input, float weight, boolean rec) {
      this.input = input;
      this.weight = weight;
      this.recurrent = rec;
    }
    public float oldvalue() {
      return input.oldvalue() * weight;
    public float newvalue() {
      return input.newvalue() * weight;
    }
  }

  // We'll emulate thresholds with bias connections
  class Neuron implements Axon {
    Synapse[] synapse;
    public Neuron(Synapse[] synapse) {
      this.synapse = synapse;
    }

    // This makes it easier to create self-referential neurons
    // (only intended for use during initialization)
    public void addSynapses(Synapse[] s) {
      Synapse[] t = new Synapse[synapse.length + s.length];
      for (int i=0; i<t.length; i++)
	t[i] = (i<synapse.length) ? synapse[i] : s[i-synapse.length];
      synapse = s;
    }

    /* We'll seldom be interested in the return value of this function.
       The important thing is the cached newvalue, which becomes the
       oldvalue when tick() is called. This allows new values to be
       computed for all neurons in a layer before they're updated,
       avoiding the cycles that would otherwise crop up in recurrent
       layers. */
    public float newvalue() {
      float sum = 0;
      for (int i=0; i<synapse.length; i++)
	sum += synapse[i].oldvalue();      
      if (sum>=threshold) newvalue=1;
      else newvalue=0;
      // TODO: allow for an externally-supplied activation function
      return newvalue;
    }

    /* Effectively says "I've finished with your layer, so update
       yourself so the next layer can use your new values".
       (TODO: would things be simpler if neurons knew which layer
       they were in?) */
    void tick() { oldvalue=newvalue;}

    float oldvalue = 0;	// should maybe initialise in constructor...
    float newvalue;

  }

  public class Net { // 3-layer neural nets

    InputNode[] ilyr; // input layer
    Neuron[] hlyr; // hidden layer
    Neuron[] olyr; // output layer

    /* Network layout is like Meeden '96, which is essentially same
       as Ellman '90. */
    public Net(int inputs, int hiddens, int outputs, boolean recurrent) {
      ilyr = new InputNode[inputs];
      hlyr = new Neuron[hiddens];
      olyr = new Neuron[outputs];

      // first, create the input layer
      for (int i=0; i<inputs; i++)
	ilyr[i] = new InputNode(null);

      // now create the hidden layer and connections from the input
      for (int i=0; i<hiddens; i++) {
	Synapse[] ips = new Synapse[inputs];
	for (int j=0; j<inputs; j++)
	  ips[j] = new Synapse(ilyr[j], myRand(), false);
	hlyr[i] = new Neuron(ips);
      }

      // recurrent connections from the hidden layer to itself
      if (recurrent) {
	for (int i=0; i<hiddens; i++) {
	  Synapse[] rec = new Synapse[hiddens];
	  for (int j=0; j<hiddens; j++)
	    rec[j] = new Synapse(hlyr[j], myRand(), true);
	  hlyr[i].addSynapses(rec);
	}
      }

      // connections from the hidden layer to the output layer
      for (int i=0; i<outputs; i++) {
	Synapse[] ips = new Synapse[hiddens];
	for (int j=0; j<hiddens; j++)
	  ips[j] = new Synapse(ilyr[j], myRand(), false);
	olyr[i] = new Neuron(ips);
      }

    }

    // functions for accessing input/output values
    public void setIn(int node, float value)
    { ilyr[node].value = value; }
    public float getIn(int node) { return ilyr[node].value; }
    public float getOut(int node) { return olyr[node].value; }
    
    /* run a forward-propagation. It's assumed that the appropriate
       input values have already been set and the output values
       will be extracted afterwards. */
    void think() {
      /* Since the hidden nodes are connected to each other, we
	 need to calculate all the new values (using the old values)
	 before actually setting them. */
      for (int i=0; i<hlyr.length; i++) hlyr.newvalue();
      for (int i=0; i<hlyr.length; i++) hlyr.tick();
      /* This trick isn't necessary for the non-recurrent output
	 layer, but a separate OutputNeuron class would be more
	 hassle (probably) */
      for (int i=0; i<olyr.length; i++) olyr.newvalue();
      for (int i=0; i<olyr.length; i++) olyr.tick();
    }

    private myRand() {
      static java.util.Random rand = new java.util.Random();
      return rand.nextFloat() * 2 - 1;
    }

  }

}
