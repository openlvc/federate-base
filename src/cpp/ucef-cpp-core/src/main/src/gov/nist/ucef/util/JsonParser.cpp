#include "gov/nist/ucef/util/JsonParser.h"

#include <fstream>
#include <iostream>

#include "gov/nist/ucef/util/Logger.h"

#include <rapidjson/document.h>
#include <rapidjson/istreamwrapper.h>
#include <rapidjson/prettywriter.h>
#include <rapidjson/stringbuffer.h>

using namespace rapidjson;
using namespace std;

namespace base
{
	namespace util
	{

		bool JsonParser::hasKey( string& json, string& key )
		{
			bool keyPresent = false;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				keyPresent = true;
			return keyPresent;
		}

		bool JsonParser::getValueAsBool( string& json, string& key )
		{
			bool value = false;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetBool();
			return value;

		}

		int JsonParser::getValueAsInt( string& json, string& key )
		{
			int value = 0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetInt();
			return value;
		}

		long JsonParser::getValueAsLong( string& json, string& key )
		{
			long value = 0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetInt64();
			return value;
		}

		float JsonParser::getValueAsFloat( string& json, string& key )
		{
			float value = 0.0f;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetFloat();
			return value;
		}

		double JsonParser::getValueAsDouble( string& json, string& key )
		{
			double value = 0.0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetDouble();
			return value;
		}

		string JsonParser::getValueAsString( string& json, string& key )
		{
			string value = "";

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
				value = document[key.c_str()].GetString();

			ConversionHelper::trim( value );

			return value;
		}

		list<std::string> JsonParser::getValueAsStrList( string& json, string& key )
		{
			list<string> strList;
			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
			{
				auto values = document[key.c_str()].GetArray();
				for( Value::ConstValueIterator itr = values.Begin(); itr != values.End(); ++itr )
				{
					string tmpValue = (*itr).GetString();
					ConversionHelper::trim( tmpValue );
					strList.push_back( tmpValue );
				}
			}
			return strList;
		}

		string JsonParser::getJsonObjectAsString( string& json, string& key )
		{
			string jsonStr = "";

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
			{
				// Convert JSON document to a string
				StringBuffer strbuf;
				PrettyWriter<rapidjson::StringBuffer> writer( strbuf );
				document[key.c_str()].Accept( writer );
				jsonStr = string( strbuf.GetString() );
			}
			return jsonStr;
		}

		string JsonParser::getJsonObjectAsString( string& json, string& key, int arrayIndex )
		{
			string jsonStr = "";

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) && document[key.c_str()].IsArray() )
			{
				// Convert JSON document to a string
				StringBuffer strbuf;
				PrettyWriter<rapidjson::StringBuffer> writer( strbuf );
				document[key.c_str()][arrayIndex].Accept( writer );
				jsonStr = string( strbuf.GetString() );
			}
			return jsonStr;
		}

		int JsonParser::getArrayElementCount( std::string& json, std::string& key )
		{
			int elementCount = 0;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) && document[key.c_str()].IsArray() )
			{
				elementCount = document[key.c_str()].GetArray().Size();
			}
			return elementCount;
		}

		list<map<string, string>> JsonParser::getValuesAsKeyValMapList( string& json, string& key )
		{
			list<map<string, string>> items;

			Document document;
			document.Parse( json.c_str() );

			if( document.HasMember(key.c_str()) )
			{
				auto configArray = document[key.c_str()].GetArray();
				rapidjson::Value::ConstValueIterator itr;
				for ( itr = configArray.Begin(); itr != configArray.End(); ++itr)
				{
					map<string, string> objPropMap;
					const Value& attribute = *itr;
					for( Value::ConstMemberIterator itr2 = attribute.MemberBegin(); itr2 != attribute.MemberEnd(); ++itr2 )
					{
						string key = itr2->name.GetString();
						string val = itr2->value.GetString();
						ConversionHelper::trim( key );
						ConversionHelper::trim( val );
						objPropMap.insert( pair<string,string>( key, val) );
					}
					items.push_back( objPropMap );
				}
			}
			return items;
		}

		string JsonParser::getJsonString( const string& configPath )
		{
			string jsonStr = "";
			ifstream ifs( configPath );
			if ( !ifs.is_open() )
			{
				Logger::getInstance().log( "Could not open the config file for reading, "
				                           "returning an empty list", LevelWarn );
				return jsonStr;
			}

			IStreamWrapper isw { ifs };
			Document doc = {};
			doc.ParseStream( isw );
			if( doc.HasParseError() )
			{
				stringstream ss;
				ss << "Error  : " << doc.GetParseError()  << '\n'
				   << "Offset : " << doc.GetErrorOffset() << '\n';
				Logger::getInstance().log( ss.str(), LevelError );
				return jsonStr;
			}
			// Convert JSON document to a string
			StringBuffer strbuf;
			PrettyWriter<rapidjson::StringBuffer> writer( strbuf );
			doc.Accept( writer );
			jsonStr = string( strbuf.GetString() );

			return jsonStr;
		}
	}
}
