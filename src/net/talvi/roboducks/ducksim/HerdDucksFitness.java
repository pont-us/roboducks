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

// HerdDucksFitness

// Fitness function which assigns good scores for decreasing the flock
// size.

package net.talvi.roboducks.ducksim;

import net.talvi.roboducks.neurotic.*;

class HerdDucksFitness implements Fitness {
  class Enviro {
    Pos[] dogs; Pos[] ducks;
    // NB: vars not init'ed, since we'll just clone into them
  }
  Enviro[] envs;		// environments to train
  TrainerControl trainCont;	// i/face to arena
  int tSteps;			// #steps per training enviro.
  DogBreeder db;
  Pos[] duck;
  int numDucks;
  AnimalModel duckModel;
  Vec goal;

  public HerdDucksFitness(TrainerControl t, int s, DogBreeder d,
		       Vec g) {
    trainCont = t;
    tSteps = s;
    db = d;
    goal = g;
    duck = trainCont.getAllDucks();
    numDucks = duck.length;
    duckModel = trainCont.duckModel();
  }

  // Initialise environments
  public void initEnviros(int n) {
    envs = new Enviro[n];

    for (int i=0; i<n; i++) {
      trainCont.resetRandom(true);
      envs[i] = new Enviro();
      envs[i].dogs = trainCont.cloneAllDogs();
      trainCont.setDog(0, new Vec(0,0));
      /* Ducks tend to group together naturally, even without
	 being herded. Therefore I run the simulation for a bit
	 without moving the dog, in order to let them settle a bit
	 and get a more neutral starting state. */
      for (int j=0; j<128; j++) {
	duckModel.update();
	trainCont.advance();
	duckModel.postUpdate();
      }
      envs[i].ducks = trainCont.cloneAllDucks();
    }
  }

  public float score(Net net, int env) {
    trainCont.setAllDogs(envs[env].dogs);
    trainCont.setAllDucks(envs[env].ducks);

    int i;
    float score = 50000;
    Pos oldPos = trainCont.dog(0), newPos;
    Pos origPos = (Pos) oldPos.clone();
    float improvement = 0;
    final float r = trainCont.arenaSize() - 30;

    // Measure initial total distance from goal (i.e. centre)
    for (i=0; i<numDucks; i++)
      improvement += duck[i].p.abs();

    for (i=0; i<tSteps; i++) {
      duckModel.update();
      db.update(net, trainCont);
      trainCont.advance();
      duckModel.postUpdate();
      newPos = trainCont.dog(0);
      
      if (newPos.p.abs() > r - 50) score -= 20000;
      // impose a heavy penalty for hitting a wall
      float a = newPos.d.abs();
      if (a > 6.0) score -= a * 20;
      // penalise dog for moving too fast
      oldPos = newPos;
    }

    // Subtract new total distance from goal,
    // and calculate score accordingly
    for (i=0; i<numDucks; i++)
      improvement -= duck[i].p.abs();
    score += improvement * 10000;

    // Reward the dog for moving, to avoid falling into the
    // degenerate "do nothing" solution
    score += (origPos.p.distTo(oldPos.p)) * 2;

    return score;
  }

}
