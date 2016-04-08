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

package net.talvi.roboducks.neurotic;

import java.io.*;
import java.util.*;

/* Simple recurrent neural net package, second attempt. */

class Debug {
    public static boolean on = false;
    public static boolean lots = false;
}

public class Net {
  InputNode[] ilyr;	// input layer
  ContextNode[] clyr;	// context layer
  Neuron[] hlyr;	// hidden layer
  Neuron[] olyr;	// output layer
  public float rate;	// learning rate
  public float momentum;
  float sumSqError;	// sum of squared errors
  Random rnd;		// our random generator (for weights)

    /* Network architecture is normal feedforward, or SRN
       like Elman '90, depending on `recurrent' flag */
  public Net(int inputs, int hiddens, int outputs, boolean recurrent) {

    rnd = new Random();
    int contexts = recurrent ? hiddens : 0;	// # context nodes
    ilyr = new InputNode[inputs+1]; // extra one for threshold bias
    clyr = new ContextNode[contexts];
    hlyr = new Neuron[hiddens];
    olyr = new Neuron[outputs];

    rate = 0.1f;			 // learning rate
    momentum = 0.9f;
    float w_centre = 0f, w_range = 0.5f; // initial weights

    // 1. create the input layer
    for (int i=0; i<inputs; i++)
      ilyr[i] = new InputNode(0, "I"+i);
    ilyr[inputs] = new InputNode(1, "Bi"); // bias

    // 2. create the context layer (will add inputs later)
    for (int i=0; i<contexts; i++)
      clyr[i] = new ContextNode(this, 0.5f, "C"+i);

    // 3. create hidden layer & links from input/context
    for (int i=0; i<hiddens; i++) {
      Link[] inLinks = new Link[inputs+contexts+1];
      for (int j=0; j<inputs; j++)
	inLinks[j] = new Link(ilyr[j], true, w_centre, w_range, rnd);
      for (int j=0; j<contexts; j++)
	inLinks[inputs+j] = new Link(clyr[j], true, w_centre, w_range, rnd);
      inLinks[inputs+contexts] =
	new Link(ilyr[inputs], true, w_centre, w_range, rnd); // bias
      hlyr[i] = new Neuron(inLinks, this, 2, 0.5f, "H"+i);
    }

    // 4. add backlinks from hidden to context
    for (int i=0; i<contexts; i++)
      clyr[i].setInput( new Link(hlyr[i], false, 5f, 0f) );

    // 5. create output layer
    for (int i=0; i<outputs; i++) {
      Link[] inLinks = new Link[hiddens+1];
      for (int j=0; j<hiddens; j++)
	inLinks[j] = new Link(hlyr[j], true, w_centre, w_range, rnd);
      inLinks[hiddens] =
	new Link(ilyr[inputs], true, w_centre, w_range, rnd); // bias
      olyr[i] = new Neuron(inLinks, this, 3, 0.5f, "O"+i);
    }
  }

  // Randomize weights of all links
  public void randomizeWeights(float mean, float range) {
    LinkEnum e = new LinkEnum();
    while (e.hasMoreElements())
      ((Link) e.nextElement()).randomizeWeight(mean, range, rnd);
  }

  // functions for accessing input/output values
  public void setIn(int node, float value) { ilyr[node].set(value); }
  public float getIn(int node) { return ilyr[node].act(); }
  public float getOut(int node) { return olyr[node].act(); }

  /* A class to represent the activation function -- really
     just a wrapper around two float->float methods.
     (XXX should this be an interface? ) */
  public abstract class Activation {
    abstract float f(float x); // the activation function
    abstract float dfdx(float x); // its first derivative
    // XXX change the name of dfdx to something that's not a lie
  }

  /* Our standard (logistic) activation function */
  private class Sigmoid extends Activation {
    float f(float x) {
      float fx;
      fx = (float) (1/(1 + java.lang.Math.exp(-x)));
      if (Float.isNaN(fx)) throw new Error("Aaagh! NaN! "+x+"\n");
      return fx;
    }
    /* NB: returns slope at given f(x) (y value). So it's not really
       df/dx. So sue me. */
    float dfdx(float x)
    { return x*(1-x); }
  }

  Activation act = new Sigmoid();

  // Forward-propagate the current input values
  public void think() {
    // order of activation will be: hidden, context, output
    // (so *initial* activations only matter for context nodes)
    for (int i=0; i<hlyr.length; i++) hlyr[i].recalc();
    for (int i=0; i<clyr.length; i++) clyr[i].recalc();
    for (int i=0; i<olyr.length; i++) olyr[i].recalc();
  }

  /* Run a backprop against given (correct) output values.
     Again, it's assumed the inputs have already been set. */
  public void train(float[] desired) {

    think();

    // compute sumSqError

    sumSqError = 0;
    for (int i=0; i<olyr.length; i++) {
      float e = (desired[i] - olyr[i].act());
      sumSqError += e*e;
    }

    // calculate deltas (deltata?)

    for (int i=0; i<olyr.length; i++) {
      olyr[i].delta = (desired[i] - olyr[i].act()) *
	act.dfdx(olyr[i].act());
    }

    for (int i=0; i<hlyr.length; i++) {
      hlyr[i].delta = 0;
      for (int j=0; j<hlyr[i].outputs.length; j++)
	if (hlyr[i].outputs[j].trainable)
	  hlyr[i].delta +=
	    hlyr[i].outputs[j].weight *
	    hlyr[i].outputs[j].output.delta();
      hlyr[i].delta *= act.dfdx(hlyr[i].act());
    }

    calcInputWeights(olyr);
    calcInputWeights(hlyr);

  }

  /* Apply (possibly accumulated) weight changes; resetDeltas()
     should be called after this method. */
  public void changeWeights() {
    LinkEnum e = new LinkEnum(true);
	// shouldn't make any difference which way we go actually
    while (e.hasMoreElements()) {
      Link l = (Link) e.nextElement();
      l.dw = l.wacc + l.dw * momentum;
      l.weight += rate * l.dw;
    }
  }

  // Reset accumulated weight changes to zero
  public void resetDeltas() {
    for (int i=0; i<olyr.length; i++)
      for (int j=0; j<olyr[i].inputs.length; j++)
	olyr[i].inputs[j].wacc = 0;
    for (int i=0; i<hlyr.length; i++)
      for (int j=0; j<hlyr[i].inputs.length; j++)
	hlyr[i].inputs[j].wacc = 0;
  }

  /* calculate changes in input weights & add them to the
     wacc (weight-accumulator) fields. */
  private void calcInputWeights(Neuron[] ns) {
    for (int i=0; i<ns.length; i++)
      for (int j=0; j<ns[i].inputs.length; j++) {
	Link ln = ns[i].inputs[j];
	ln.wacc += ln.output.delta() * ln.input.act();
      }
  }

  // overwrite the cached activations of the context nodes with
  // the given value. Handy for starting new training patterns
  public void flushContext(float x)
  { for (int i=0; i<clyr.length; i++) clyr[i].act = x; }

  // return sum of squared errors computed during backprop
  public float sumSqError() { return sumSqError; }

  // read weights in from a file (does not check for bad input)
  public void loadWeights(File f) {
    try {
      StreamTokenizer input = new StreamTokenizer
	(new BufferedReader (new InputStreamReader
	  (new FileInputStream(f))));

      LinkEnum linkEnum = new LinkEnum();
      while (linkEnum.hasMoreElements()) {
	while (input.nextToken() != input.TT_NUMBER);
	((Link) linkEnum.nextElement()).weight = (float) input.nval;
      }
    }
    catch (IOException e) { throw new Error(e.getMessage()); }
  }

  /* Return number of trainable weights in the network. Handy
     if creating an array for getAllWeights(). */
  public int numWeights() {
    return (ilyr.length+clyr.length)*hlyr.length // ->hidden
      +    (1 + hlyr.length) * olyr.length ;	 // ->output
  }

  /* Output all weights as a single array of floats
     order: hidden, output (input wts of each) */
  public float[] getAllWeights() {
    float[] w = new float[numWeights()];
    int i=0;
    LinkEnum e = new LinkEnum();
    while (e.hasMoreElements())
      w[i++] = ((Link) e.nextElement()).weight;
    return w;
  }

  /* Set all weights from the supplied array of floats.
     Order same as getAllWeights(). */
  public void setAllWeights(float w[]) {
    if (w.length != numWeights())
      throw new Error("Wrong number of weights.");
    int i=0;
    LinkEnum e = new LinkEnum();
    while (e.hasMoreElements())
      ((Link) e.nextElement()).weight = w[i++];
  }

  /* Enumerate over all (trainable) links in network. It can
     be relied upon always to enumerate in the same order. */
  public class LinkEnum implements Enumeration {
    private Neuron[] layer;
    private int i,j;
    private Link next;
    private boolean nextIsValid;
    private boolean backwards;	// i.e. output then hidden

    public LinkEnum() { this(false); }

    public LinkEnum(boolean backwards) {
      this.backwards = backwards;
      layer = backwards ? olyr : hlyr;
      i = j = 0;
      nextIsValid = true;
      cacheNext();
    }

    private void cacheNext() {
      if (!nextIsValid) throw new Error
	("LinkEnum fell off the end. This really shouldn't happen.");
      if (j>=layer[i].inputs.length) {j=0; i++;}
      if (i>=layer.length) {
	i = 0;
	if (backwards) layer = (layer==olyr) ? hlyr : null;
	else           layer = (layer==hlyr) ? olyr : null;
      }
      if (layer==null)
	nextIsValid = false;
      else {
	next = layer[i].inputs[j];
	nextIsValid = true;
      }
      j++;
    }

    public boolean hasMoreElements() { return nextIsValid; }
    public Object nextElement() {
      if (!nextIsValid) throw new Error
	("nextElement() called on empty link enumerator.");
      Link r = next;
      cacheNext();
      return r;
    }
  }

  // ============= Debugging routines below here

  // Dump a line describing weights to given o/p stream
  // (same order as dumpWeights, naturellement)
  public void describeWeights(java.io.PrintStream out) {
    for (int i=0; i<hlyr.length; i++)
      for (int j=0; j<hlyr[i].inputs.length; j++)
	out.print(hlyr[i].inputs[j].input.label()+hlyr[i].label()+" ");
    for (int i=0; i<olyr.length; i++)
      for (int j=0; j<olyr[i].inputs.length; j++)
	out.print(olyr[i].inputs[j].input.label()+olyr[i].label()+" ");
    out.println();
  }

  // Dump the weightings to the given output stream
  public void dumpWeights(java.io.PrintStream out) {
    dumpWeights(out, hlyr);
    dumpWeights(out, olyr);
    out.println();
  }

  public void dumpWeights(java.io.PrintStream out,
			  Neuron[] nodes) {
    for (int i=0; i<nodes.length; i++)
      for (int j=0; j<nodes[i].inputs.length; j++)
	out.print(nodes[i].inputs[j].weight+" ");
  }


  // Dump the activations to the given output stream
  public void dumpActs(java.io.PrintStream out) {
    dumpActs(out, olyr, "Outputs: ");
    dumpActs(out, hlyr, "Hiddens: ");
    dumpActs(out, clyr, "Context: ");
    dumpActs(out, ilyr, "Inputs : ");
  }

  public void dumpActs(java.io.PrintStream out, Axon[] ns,
		       String label) {
    out.print(label);
    for (int i=0; i<ns.length; i++) out.print(ns[i].act()+" ");
    out.println();
  }


}
