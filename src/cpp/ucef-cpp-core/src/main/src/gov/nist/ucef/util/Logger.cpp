#include <iostream>
#if defined(_WIN32)
#include <direct.h>
#endif
#include "gov/nist/ucef/util/Logger.h"
#include "spdlog/spdlog.h"

#include <sys/stat.h>
using namespace spdlog;
using namespace std;

namespace base
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
#if defined(_WIN32)
				_mkdir("logs");
#else
				mkdir("logs", S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
#endif
				logger = basic_logger_mt( "ucef_federate", "logs/uceffederate.log", true );
				logger->set_level( level::level_enum::info );
			}
			catch( const spdlog::spdlog_ex &e )
			{
				cout << "Failed to create the logger. Error : " << e.what();
				exit( 1 );
			}
		}

		void Logger::setLogLevel( LogLevel level )
		{
			// We are using the same numeric values as in spdlog, so the cast is safe.
			logger->set_level( level::level_enum(level) );
			logger->info( "Log level changed to " + string(level::to_str((level::level_enum)level)) );
		}

		void Logger::log( const std::string& message, LogLevel level )
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
			logger->flush();
		}
	}
}
