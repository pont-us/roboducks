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

// Parrot: try to learn to repeat previous input

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;
import java.io.*;

public class Parrot {

  static java.util.Random rnd = new java.util.Random();

  public static void main(String[] argv) {
    Net net = new Net(1,2,1,true);
    net.loadWeights(new File("test-wts"));
    boolean curr, prev = false;

    net.rate = 0.1f;
    net.momentum = 0.0f;

    net.describeWeights(System.err);
    net.dumpWeights(System.err);

    if (true) {
    net.resetDeltas();
    for (int i=1; i<100000; i++) {

	curr = rnd.nextBoolean();
	net.setIn(0, curr? 1 : 0);

	net.train(new float[] { prev ? 1 : 0});
	prev = curr;

	net.changeWeights(); net.resetDeltas();

    }
    }
    net.changeWeights();
    net.flushContext(0);
    net.dumpActs(System.out);
    net.setIn(0,0);
    net.think();
    net.dumpActs(System.out);

    if (true) {
    prev = false;
    for (int i=0; i<16; i++) {
	curr = rnd.nextBoolean();
	net.setIn(0, curr ? 1 : 0);
	net.think();
	System.out.println( (prev ? "X " : ". ") +
			    (curr ? "X " : ". ") +
			    (net.getOut(0) ));
	prev = curr;
    }

    net.dumpWeights(System.err);
    }
  }

}
