#pragma once

#include <iostream>

#include "Logger.h"
#include "spdlog/sinks/basic_file_sink.h"

using namespace spdlog;
using namespace std;

namespace ucef
{
	namespace util
	{
		Logger& Logger::getInstance()
		{
			static Logger instance;
			return instance;
		}

		Logger::Logger()
		{ 
			try
			{
				logger = basic_logger_mt( "ucef_federate", "logs/uceffederate.log", true );
				// we are using same numeric values, so the cast is safe
				logger->set_level( level::level_enum::debug );
			}
			catch( const spdlog::spdlog_ex &e )
			{
				cout << "Failed to create the logger. Error : " << e.what();
				exit( 1 );
			}
		}

		void Logger::setLogLevel( LogLevel level )
		{
			logger->set_level( level::level_enum(level) );
		}

		void Logger::log( std::string& message, LogLevel level )
		{
			if( level == LogLevel::LevelInfo )
				logger->info( message );
			else if( level == LogLevel::LevelWarn )
				logger->warn( message );
			else if( level == LogLevel::LevelDebug )
				logger->debug( message );
			else if( level == LogLevel::LevelError )
				logger->error( message );
			else if( level == LogLevel::LevelInfo )
				logger->info( message );
			else if( level == LogLevel::LevelCritical )
				logger->critical( message );
		}
	}
}