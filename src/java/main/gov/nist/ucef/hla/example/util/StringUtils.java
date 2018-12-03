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
package gov.nist.ucef.hla.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides a few static methods which provide some commonly used 
 * String processing functions
 */
public class StringUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static String NEWLINE = "\n";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Utility method to repeat a string a given number of times.
	 * 
	 * @param str the string to repeat
	 * @param count the number of repetitions
	 * @return the repeated string
	 */
	public static String repeat( String str, int count )
	{
		return IntStream.range( 0, count ).mapToObj( i -> str ).collect( Collectors.joining( "" ) );
	}
	
	/**
	 * Utility class to center a string in a given width
	 * 
	 * @param str the string to center
	 * @param width the width to center the string in
	 * @param padding the padding character to use to the left and right of the string
	 * @return the centered string
	 */
	public static String center( String str, int width, char padding )
	{
		int count = width - str.length();
		if( count <= 0 )
			return str;

		String padStr = Character.toString( padding );
		String leftPad = repeat( padStr, count / 2 );
		if( count % 2 == 0 )
			return leftPad + str + leftPad;

		return leftPad + str + leftPad.substring( 0, count + 1 );
	}

	/**
	 * Simple utility method to create an ASCII-art style table from a 2D list of data.
	 * 
	 * Assumes that the first row is a header row.
	 * 
	 * @param tableData the data to be shown in the table
	 * @return an ASCII-art style table
	 */
	public static String makeTable( List<List<Object>> tableData )
	{
		// this has three phases
		//   1 - collect statistics about the table rows and columns so that
		//       we know how wide each column has to be
		//   2 - pad any rows that don't have enough columns (so all rows are
		//       the same number of columns "wide")
		//   3 - Construct the table as ASCII-art

		// collect statistics about the table rows/columns
		int numRows = tableData.size();
		int numColumns = 0;
		List<Integer> colWidths = new ArrayList<>();
		List<List<String>> tableContent = new ArrayList<>();
		for( int rowIdx = 0; rowIdx < numRows; rowIdx++ )
		{
			List<Object> rowData = tableData.get( rowIdx );
			List<String> rowContent = new ArrayList<>();
			tableContent.add( rowContent );
			numColumns = rowData.size() > numColumns ? rowData.size() : numColumns;
			for( int colIdx = 0; colIdx < rowData.size(); colIdx++ )
			{
				Object cellData = rowData.get( colIdx );
				String cellText = cellData == null ? "" : cellData.toString();
				rowContent.add( cellText );
				int cellTextLength = cellText.length();
				if( colIdx < colWidths.size() )
				{
					int colWidth = colWidths.get( colIdx );
					if( colWidth < cellTextLength )
						colWidths.set( colIdx, cellTextLength );
				}
				else
				{
					colWidths.add( cellTextLength );
				}
			}
		}

		// pad any rows which don't have enough columns
		for( List<String> rowContent : tableContent )
			while( rowContent.size() < numColumns )
				rowContent.add( "" );

		// build the various table borders
		StringBuilder topBorderBuilder = new StringBuilder();
		StringBuilder rowBorderBuilder = new StringBuilder();
		StringBuilder botBorderBuilder = new StringBuilder();
		for( int colIdx = 0; colIdx < colWidths.size(); colIdx++ )
		{
			String colSpacer = repeat( "─", colWidths.get( colIdx ) + 2 );
			topBorderBuilder.append( colIdx == 0 ? '┌' : '┬' ).append( colSpacer );
			rowBorderBuilder.append( colIdx == 0 ? '├' : '┼' ).append( colSpacer );
			botBorderBuilder.append( colIdx == 0 ? '└' : '┴' ).append( colSpacer );
		}
		String topBorder = topBorderBuilder.append( '┐' ).append( NEWLINE ).toString();
		String rowBorder = rowBorderBuilder.append( '┤' ).append( NEWLINE ).toString();
		String botBorder = botBorderBuilder.append( '┘' ).append( NEWLINE ).toString();

		// build the table
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append( topBorder );
		for( int rowIdx = 0; rowIdx < numRows; rowIdx++ )
		{
			List<String> rowContent = tableContent.get( rowIdx );
			for( int colIdx = 0; colIdx < numColumns; colIdx++ )
			{
				String cellContent = rowContent.get( colIdx );
				int colWidth = colWidths.get( colIdx );
				int padding = colWidth - cellContent.length();
				if( colIdx > 0 )
					tableBuilder.append( " " );
				tableBuilder.append( "│ " );
				tableBuilder.append( cellContent );
				tableBuilder.append( repeat( " ", padding ) );
				if( colIdx > (numColumns - 2) )
					tableBuilder.append( " │\n" );
			}

			if( rowIdx == 0 && numRows > 1 )
				tableBuilder.append( rowBorder );
		}
		tableBuilder.append( botBorder );

		// return the table
		return tableBuilder.toString();
	}
}
