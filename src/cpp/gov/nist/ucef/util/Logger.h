#pragma once

#include <memory>
#include <string>

#include "gov/nist/ucef/config.h"

#include "types.h"

namespace spdlog
{
	class logger;
}

namespace ucef
{
	namespace util
	{
		/**
		 * The {@link Logger} provides a simple logging framework for the system.
		 * 
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
