/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package gov.nist.ucef.hla.example.util.cmdargs;

import java.util.Comparator;

/**
 * Utility class which simpy implements the {@link Comparator} interface to allow comparison of
 * {@link CmdLineArgument} instances for the purposes of sorting
 */
class CmdLineArgumentComparator implements Comparator<CmdLineArgument>
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
	public int compare( CmdLineArgument cla1, CmdLineArgument cla2 )
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
		int diff = ord2 - ord1;
		if( diff != 0 )
			return diff;

		// alphabetical sort by short/long form of argument
		String o1Str = cla1.hasShortForm() ? cla1.shortForm.toString() : cla1.longForm;
		String o2Str = cla2.hasShortForm() ? cla2.shortForm.toString() : cla2.longForm;
		return o1Str.compareTo( o2Str );
	}
}
