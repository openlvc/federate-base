#include "gov/nist/ucef/util/JsonParser.h"

#include <rapidjson/document.h>
#include <rapidjson/istreamwrapper.h>

using namespace rapidjson;
using namespace std;

namespace base
{
	namespace util
	{

		bool JsonParser::getValueAsBool( std::string& json, std::string& key )
		{
			bool value = false;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetBool();
			return value;

		}

		int JsonParser::getValueAsInt( std::string& json, std::string& key )
		{
			int value = 0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetInt();
			return value;
		}

		long JsonParser::getValueAsLong( std::string& json, std::string& key )
		{
			long value = 0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetInt64();
			return value;
		}

		float JsonParser::getValueAsFloat( std::string& json, std::string& key )
		{
			float value = 0.0f;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetFloat();
			return value;
		}

		double JsonParser::getValueAsDouble( std::string& json, std::string& key )
		{
			double value = 0.0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetDouble();
			return value;
		}

		string JsonParser::getValueAsString( std::string& json, std::string& key )
		{
			string value = "";

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetString();
			return value;
		}
	}
}
