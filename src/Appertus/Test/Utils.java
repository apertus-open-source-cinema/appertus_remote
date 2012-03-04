/*! Copyright (C) 2012 Apertus, All Rights Reserved
 *! Author : Apertus Team
 *! Description: Class to store helper functions and handy utilities.
 -----------------------------------------------------------------------------**
 *!
 *!  This program is free software: you can redistribute it and/or modify
 *!  it under the terms of the GNU General Public License as published by
 *!  the Free Software Foundation, either version 3 of the License, or
 *!  (at your option) any later version.
 *!
 *!  This program is distributed in the hope that it will be useful,
 *!  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *!  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *!  GNU General Public License for more details.
 *!
 *!  You should have received a copy of the GNU General Public License
 *!  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *!
 -----------------------------------------------------------------------------**/
package Appertus.Test;

public class Utils {
    // round a float value to a certain number of decimals

    public static float Round(float Rval, int decimals) {
	float p = (float) Math.pow(10, decimals);
	Rval = Rval * p;
	float tmp = Math.round(Rval);
	return (float) tmp / p;
    }

    // makes sure the input is within the min/max range
    public static int MinMaxRange(int input, int min, int max) {
	if (input > max) {
	    input = max;
	}
	if (input < min) {
	    input = min;

	}
	return input;
    }
}
