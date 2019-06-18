/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include <string>

namespace base
{
	namespace util
	{

		class JsonParser
		{
			//----------------------------------------------------------
			//                    Static methods
			//----------------------------------------------------------
		public:
			/*
			 * Returns a singleton instance of the {@link JsonParser}.
			 */
			static JsonParser& getInstance();

			//----------------------------------------------------------
			//                    Private methods
			//----------------------------------------------------------
		private:
			JsonParser();

			//----------------------------------------------------------
			//       Deleted copy and assignment constructors
			//----------------------------------------------------------
		public:
			JsonParser( JsonParser const& ) = delete;
			void operator=( JsonParser const& ) = delete;

			//----------------------------------------------------------
			//       Members to parse Json string
			//----------------------------------------------------------
			bool getValueAsBool( std::string& json, std::string& key );
			int getValueAsInt( std::string& json, std::string& key );
			long getValueAsLong( std::string& json, std::string& key );
			float getValueAsFloat( std::string& json, std::string& key );
			double getValueAsDouble( std::string& json, std::string& key );
			std::string getValueAsString( std::string& json, std::string& key );
		};
	}
}