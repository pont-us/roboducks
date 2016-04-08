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

/* An Axon is either a neuron or an input node: anything
   that can provide an input to a neuron. All we care is
   that the neuron at the other end of the forward connection
   can get at its output. */
abstract class Axon {
  float act;
  String label;
  Link[] outputs;

  // activation value (just returns cached value, *doesn't* recalculate)
  float act() { return act; }
  // arbitrary name, mainly for debugging
  String label() { return label; } 

  float delta() {
    throw new Error("Unimplemented delta function called on Axon.");
    // Yes, it is a bit of a fudge, but it keeps a lot of other
    // things neat.
  }

  abstract int layer();		// 0=input, 1=context, 2=hidden, 3=output

  /* Creating two interlinked Axons simultaneously is tricky,
     so we create the input one first, and use this method to tell
     it who it's outputting to. (This is the responsibility of the
     output Neuron.) */
  void addOutput(Link x) {
    Link[] os = new Link[outputs.length+1];
    System.arraycopy(outputs, 0, os, 0, outputs.length);
    os[outputs.length] = x;
    outputs = os;
    // Vector would be neater, but might slow propagation bits down
  }

}

/* Input node: just stores one value and always returns it as its
   activation. The activation can be changed by the set() method. */
class InputNode extends Axon {
  InputNode(float act, String label) {
    this.act = act;
    this.label = label;
    outputs = new Link[0];
  }
  int layer() { return 0; }
  void set(float act) { this.act = act; }
}

// Class for hidden and output nodes.
class Neuron extends Axon {
  Link[] inputs;
  float delta;	// for backprop calculation
  Net net;	// parent network, to determine activation fn & suchlike
  int layer;

  // Initial activation is irrelevant for neuron, so I should
  // probably take it out of the constructor
  public Neuron(Link[] inputs, Net net, int layer, float act,
		String label) {
    this.inputs = inputs;
    this.outputs = new Link[0];
    this.net = net;
    this.layer = layer;
    this.act = act;
    this.label = label;
    // It's our responsibility to set `output' on the links
    for (int i=0; i<inputs.length; i++) {
      inputs[i].output = this;
      inputs[i].input.addOutput(inputs[i]);
    }
  }

  public float delta()	{ return delta; }
  public int layer()	{ return layer; }

  /* Now that I've moved context nodes into their own class,
     this is redundant, at least if I'm sticking to SRNs. */
  public void addInputs(Link[] xs) {
    // first, set the outputs for the links
    for (int i=0; i<xs.length; i++) {
      xs[i].output = this;
      xs[i].input.addOutput(xs[i]);
    }
    Link[] newI = new Link[inputs.length + xs.length];
    System.arraycopy(inputs, 0, newI, 0, inputs.length);
    System.arraycopy(xs, 0, newI, inputs.length, xs.length);
    // weights etc. already initialized by Link constructor
    inputs = newI;
  }

  /* Calculate our activation and cache it in `act'.
     Does NOT force recalculation of any other activations.
  */
  public void recalc() {
    float sum = 0;
    for (int i=0; i<inputs.length; i++)
      sum += inputs[i].weight * inputs[i].input.act();
    // XXX this is a bit of a mess
    if (false) {
    if (Debug.lots) {
      for (int i=0; i<inputs.length; i++)
	System.err.println(inputs[i].weight+" * "+inputs[i].input.act());
      System.err.println("------------------------");
    }
    if (Float.isNaN(sum)) {
      for (int i=0; i<inputs.length; i++)
	System.err.println(inputs[i].weight+" * "+inputs[i].input.act());
      throw new Error("Looks like we've hit NaN...");
    }
    }
    act = net.act.f(sum);	// activation function
  }

}

class ContextNode extends Axon {
  Link input;	// We shouldn't have more than one input
  Net net;	// parent network, to determine activation fn & suchlike

  public ContextNode(Net net, float act, String label) {
    outputs = new Link[0];
    this.net = net;
    act = act;
    this.label = label;
  }

  public int layer() { return 1; }

  /* A context node just copies the output of the neuron it's
     connected to. */
  void recalc() { act = input.input.act(); }

  /* WARNING: If this is called more than once, an output of
     the node at the other end of the old link will still be
     pointing hither, and all hell may well break loose. So
     DON'T DO IT KIDS. */
  void setInput(Link l) {
    l.output =  this;
    l.input.addOutput(l);
    input = l;
  }
}
