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

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;

public class GeneTest {

  public static void main(String[] argv) {
    Net net = new Net(2,2,1,false);
      
    Population pop =
      new Population(net, new XorFitness(), 400, 0, 4, 6, 4);
    pop.evolve(50);
    net.setAllWeights(pop.fittest());
    testNet(net);
    net.describeWeights(System.err);
    net.dumpWeights(System.err);

  }

  static void testNet(Net net) {
    for (int i=0; i<4; i++) {
      int a = i / 2;
      int b = i % 2;
      net.setIn(0, (float) a);
      net.setIn(1, (float) b);
      net.think();
      System.out.println(a+" "+b+" "+(a^b)+" "+net.getOut(0));
    }
  }

}

class XorFitness implements Fitness {

  // we'll always have four environments
  public void initEnviros(int n) {};

  public float score(Net net, int env) {
    int a = env / 2;
    int b = env % 2;
    net.setIn(0, (float) a);
    net.setIn(1, (float) b);
    net.think();
    float actual = net.getOut(0);
    float ideal = (float) (a^b);
    float score =
	(float) Math.pow(10*(1-Math.abs(ideal-actual)),1);
    if (env==3 && actual>0.6) score -= 5;
    return score;
  }

}
