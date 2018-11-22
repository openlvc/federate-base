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
		 * @file Logger.h
		 *
		 * A singelton class that provides logging methods
		 */
		class UCEF_API Logger
		{
			//----------------------------------------------------------
			//                    Static methods
			//----------------------------------------------------------
		public:
			/*
			 * Returns a logger to provide logging facility.
			 *
			 * By default the logging level is set to LogLevel::LevelDebug.
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
			 * Update the logging level of the logger
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
			 * @param level log level of this message
			 *
			 * @see LogLevel
			 */
			void log( const std::string& message, LogLevel level );

		private:
			std::shared_ptr<spdlog::logger> logger;
		};
	}
}
