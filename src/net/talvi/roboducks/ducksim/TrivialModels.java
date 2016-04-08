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

// TrivialModels.java: some really trivial duck and dog controllers,
// for testing purposes

package net.talvi.roboducks.ducksim;

class TrivialModels {

  static class DeadDog extends AnimalModel {
    void update() {}
    void postUpdate() {}
  }
  
  static class MadDog extends AnimalModel {
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
  
  static class DaffyDucks extends AnimalModel {
    void update() {}
    void postUpdate() {}
  }
}
