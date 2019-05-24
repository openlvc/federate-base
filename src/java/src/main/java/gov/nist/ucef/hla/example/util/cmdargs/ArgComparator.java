/*
 * This software is contributed as a public service by The National Institute of Standards 
 * and Technology (NIST) and is not subject to U.S. Copyright
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */
package gov.nist.ucef.hla.example.util.cmdargs;

import java.util.Comparator;

/**
 * Utility class which simpy implements the {@link Comparator} interface to allow comparison of
 * {@link Arg} instances for the purposes of sorting
 */
public class ArgComparator implements Comparator<Arg>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public int compare( Arg cla1, Arg cla2 )
	{
		// return...
		//   *  -ve if the first value is less than the second 
		//   *  0 if the first value is equal to second 
		//   *  +ve if the first value is more than the second

		// basically we are trying to sort so that optional arguments come
		// after required arguments, and otherwise sort alphabetically by
		// the short form or the argument if it has one, and the long form
		// if not

		// required arguments before optional arguments
		if( cla1.isRequired() && !cla2.isRequired() )
		{
			return -1;
		}
		else if( !cla1.isRequired() && cla2.isRequired() )
		{
			return 1;
		}

		// sort by type - SWITCH before VALUE before LIST
		int ord1 = cla1.argKind().ordinal();
		int ord2 = cla2.argKind().ordinal();
		int diff = ord1 - ord2;
		if( diff != 0 )
			return diff;

		// alphabetical sort by short/long form of argument
		String o1Str = cla1.hasShortForm() ? cla1.shortForm.toString() : cla1.longForm;
		String o2Str = cla2.hasShortForm() ? cla2.shortForm.toString() : cla2.longForm;
		return o1Str.compareTo( o2Str );
	}
}
