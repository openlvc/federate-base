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

#include <memory>
#include <string>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/types.h"

namespace spdlog
{
	class logger;
}

namespace base
{
	namespace util
	{
		/**
		 * The {@link Logger} provides a simple logging framework for the system.
		 */
		class Logger
		{
			//----------------------------------------------------------
			//                    Static methods
			//----------------------------------------------------------
		public:
			/*
			 * Returns a singelton instance of the {@link Logger}.
			 * <p/>
			 * By default the logging level of the Logger is set to
			 * LogLevel::LevelInfo.
			 *
			 * @see LogLevel
			 */
			static Logger& getInstance();

			//----------------------------------------------------------
			//                    Private methods
			//----------------------------------------------------------
		private:
			Logger();

			//----------------------------------------------------------
			//            Deleted copy and assignment constructors 
			//----------------------------------------------------------
		public:
			Logger( Logger const& ) = delete;
			void operator=( Logger const& ) = delete;

			//----------------------------------------------------------
			//                    Instance methods
			//----------------------------------------------------------

			/*
			 * Updates the logging level of the {@link Logger}.
			 *
			 * @param level the logging level to be set
			 *
			 * @see LogLevel
			 */
			void setLogLevel( LogLevel level );

			/*
			 * Logs a message to a log file
			 *
			 * @param message the message to be logged
			 * @param level logging level of this message
			 *
			 * @see LogLevel
			 */
			void log( const std::string& message, LogLevel level );

		private:
			std::shared_ptr<spdlog::logger> logger;
		};
	}
}
