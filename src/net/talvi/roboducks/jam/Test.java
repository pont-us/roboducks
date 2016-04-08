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

package net.talvi.roboducks.jam;

public class Test {

  final static float PI = (float) Math.PI;

  public static void main(String[] argv) {

    for (int i=-4; i<5; i++)
      System.out.println("" + i + " : " + normAngle((float) i));

  }

  private static float normAngle(float theta) {
    float out = ( (theta + PI) / (2*PI) ) % 1.0f;
    if (out<0) out += 1.0f;
    return out;
  }


}
