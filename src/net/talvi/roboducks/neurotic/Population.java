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

import java.util.*;
import java.io.*;
import java.text.*;

public class Population {

  Net net;
  Fitness fitfunc;	// the fitness function
  public float[][] weights;	// weights of networks
  public float[] fitness;	// fitnesses of individuals
  Random rnd;
  int envs;		// number of environments for determining fitness
  float muteRate;	// mutation rate
  int currGen;		// current generation
  public int fittest, leastFit;	// most and least fit individuals

  public Population(Net net, Fitness fitfunc, int size,
		    float mean, float range, float muteRate,
		    int envs) {
    this.net = net;
    this.fitfunc = fitfunc;
    this.muteRate = muteRate;
    rnd = new Random();
    this.envs = envs;
    weights = new float[size][];
    this.fitness = new float[size];
    randomizePopulation(size, mean, range);
  }

  // load weights from input stream instead of initialising randomly
  // file format: size muteRate currGen net.numWeights weights
  public Population(Net net, Fitness fitfunc, int envs,
		    Reader r) {
    this.net = net;
    this.fitfunc = fitfunc;
    rnd = new Random();
    this.envs = envs;
    try {
      StreamTokenizer in = new StreamTokenizer(r);
      in.nextToken(); int size = (int) in.nval;
      this.fitness = new float[size];
      in.nextToken(); muteRate = (float) in.nval;
      in.nextToken(); currGen = (int) in.nval;
      in.nextToken(); int numWts = (int) in.nval;
      if (numWts != net.numWeights()) 
	throw new Error("Population: error reading input file: "+
			"wrong number of weights for this network.");
      weights = new float[size][];
      for (int i=0; i<size; i++) {
	weights[i] = new float[numWts];
	for (int j=0; j<numWts; j++) {
	  in.nextToken();
	  weights[i][j] = (float) in.nval;
	}
      }
    }
    catch (IOException e) { throw new Error(e.getMessage()); }

  }

  // save weights to output stream
  public void save(Writer w) {
    DecimalFormat d = new DecimalFormat("0.##########");
    try {
      w.write(weights.length + " " + muteRate + " " +
	      currGen + " " + net.numWeights() + "\n");
      for (int i=0; i<weights.length; i++) {
	for (int j=0; j<net.numWeights(); j++) {
	  w.write(d.format(weights[i][j]) + " ");
	}
	w.write("\n");
      }
    }
    catch (IOException e) { throw new Error(e.getMessage()); }
  }

  public int popSize() { return weights.length; }
  public int getCurrGen() { return currGen; }

  public void randomizePopulation(int size, float mean, float range) {
    for (int i=0; i<size; i++) {
      net.randomizeWeights(mean, range);
      weights[i] = net.getAllWeights();
    }
    currGen = 0;
  }

  float measureFitness(int i) {
    float total = 0;
    // System.err.println(i);
    net.setAllWeights(weights[i]);
    net.flushContext(0.5f);
    for (int j=0; j<envs; j++) {
      total += fitfunc.score(net, j);
    }
    return total;
  }

  // randomly select two candidates, let them compete
  // in the prediefined environments, and replace the
  // loser by a mutation of the winner
  void tournamentSelect() {
    int a = rnd.nextInt(popSize());
    int b = rnd.nextInt(popSize()-1); if (b>=a) b++;
    int winner = fitness[a] > fitness[b] ? a : b;
    int loser = (a==winner) ? b : a;
    mutate(weights[winner],weights[loser]);
    fitness[loser] = measureFitness(loser);
  }

  // find individuals with least and greatest fitness
  public void recalcStats() {
    fittest = leastFit = -1;
    float max = Float.NEGATIVE_INFINITY;
    float min = Float.POSITIVE_INFINITY;
    for (int i=0; i<popSize(); i++) {
      System.err.println("> " + i + " : " + fitness[i]);
      if (fitness[i]>max) {fittest=i; max=fitness[i];}
      if (fitness[i]<min) {leastFit=i; min=fitness[i];}
    }
    if (fittest==-1) throw new Error
		       ("All "+popSize()+" fitnesses "+
			"are Float.NEGATIVE_INFINITY.");
    if (leastFit==-1) throw new Error
		       ("All "+popSize()+" fitnesses "+
			"are Float.POSITIVE_INFINITY.");    
  }

  public float[] fittest() {
    return weights[fittest];
  }

  // progress evolution by n generations
  public void evolve(int n) {
    for (int i=0; i<n; i++) {
      fitfunc.initEnviros(envs);
      for (int j=0; j<popSize(); j++) fitness[j] = measureFitness(j);
      for (int j=0; j<popSize(); j++) tournamentSelect();
    }
    currGen += n;
    recalcStats();
  }

  public void evolve() { evolve(1); }

  // returns a mutation of the given float array
  float[] mutate(float[] in, float range) {
    float[] out = (float[]) in.clone();
    for (int i=0; i<out.length; i++)
      out[i] += (rnd.nextFloat() - 0.5f) * range;
    return out;
  }

  // overwrites `out' with a mutation of `in'
  void mutate(float[] in, float[] out) {
    for (int i=0; i<out.length; i++)
      out[i] = in[i] + (rnd.nextFloat() - 0.5f) * muteRate;
  }

}
