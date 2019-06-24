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

/**
 * @file types.h
 * Defines all types used by UCEFFederate
 */
#include <codecvt>
#include <list>
#include <locale>
#include <memory>
#include <regex>
#include <string>
#include <unordered_map>

namespace rti1516e
{
	class ObjectInstanceHandle;
}

namespace base
{
	const static long INVALID_LONG = -1;
	/**
	 *  Represents the valid synchronization points of this federate
	 *
	 *  @see FederateBase#synchronize( SynchPoint )
	 */
	enum SynchPoint
	{
		READY_TO_POPULATE = 0,
		READY_TO_RUN = 1,
		READY_TO_RESIGN = 2,
		POINT_UNKNOWN = 3
	};

	/**
	 *  Represents the life cycle state of this federate
	 *
	 *  @see FederateBase#getLifecycleState()
	 */
	enum LifecycleState
	{
		INITIALIZING,
		RUNNING,
		CLEANING_UP,
		EXPIRED,
		LIFE_CYCLE_UNKNOWN
	};

	/**
	 *  Represents data type of an attribute or interaction parameter
	 *
	 *  @see ObjectAttribute
	 *  @see InteractionParameter
	 */
	enum DataType
	{
		DATATYPEBYTE,
		DATATYPECHAR,
		DATATYPESHORT,
		DATATYPEINT,
		DATATYPELONG,
		DATATYPEFLOAT,
		DATATYPEDOUBLE,
		DATATYPEBOOLEAN,
		DATATYPESTRING,
		DATATYPEUNKNOWN
	};

	//----------------------------------------
	//            Struct declaration
	//-----------------------------------------

	/**
	 *  Represents an attribute in an object class in a Simulation Object Model
	 *
	 *  @see ObjectClass
	 */
	struct ObjectAttribute
	{
		ObjectAttribute() : name( "" ),
		                    publish( false ),
		                    subscribe( false ),
		                    type( DATATYPEUNKNOWN )
		{
		}

		std::string name;
		bool publish;
		bool subscribe;
		DataType type;
	};

	/**
	 *  Represents an object class in a Simulation Object Model
	 *
	 *  @see SOMParser#getObjectClasses(string&)
	 *  @see ObjectAttribute
	 */
	typedef std::unordered_map<std::string, std::shared_ptr<ObjectAttribute>> ObjectAttributes;
	struct ObjectClass
	{
		ObjectClass() : name( "" ),
		                publish( false ),
		                subscribe( false ),
		                objectAttributes{}
		{
		}
		std::string name; // fully qualified object class name
		bool publish;
		bool subscribe;
		ObjectAttributes objectAttributes;
	};

	/**
	 *  Represents a parameter in an interaction class in a Simulation Object Model
	 *
	 *  @see InteractionClass
	 */
	struct InteractionParameter
	{
		InteractionParameter() : name( "" ),
		                         type( DATATYPEUNKNOWN )
		{

		}
		std::string name;
		DataType type;
	};

	/**
	 *  Represents an interaction class in a Simulation Object Model
	 *
	 *  @see SOMParser#getInteractionClasses(string&)
	 *  @see InteractionParameter
	 */
	typedef std::unordered_map<std::string, std::shared_ptr<InteractionParameter>> InteractionParameters;
	struct InteractionClass
	{
		InteractionClass() : name( "" ),
		                     publish( false ),
		                     subscribe( false ),
		                     parameters{}
		{

		}
		std::string name; // fully qualified interaction class name
		bool publish;
		bool subscribe;
		InteractionParameters parameters;
	};

	/**
	 * Type neutral representation of attribute and parameter values
	 *
	 *  @see HLAObject
	 *  @see HLAInteraction
	 */
	struct VariableData
	{
		std::shared_ptr<void> data;
		size_t size;
	};

	//----------------------------------------
	//           Typedefs
	//-----------------------------------------

	// unordered_map because we do not need any ordering
	// what we need is a faster way to get the object class

	// To resolve ObjectClass from an object class name of an incoming object
	typedef std::unordered_map<std::string, std::shared_ptr<ObjectClass>> ObjectDataStoreByName;
	// To resolve ObjectClass from the hash of the object class handle of an incoming object
	typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByHash;
	// To resolve ObjectClass from the hash of the object instance handle of an incoming object
	typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByInstance;
	// To resolve ObjectInstanceHandle from the hash of the object instance handle of an outgoing object
	typedef std::unordered_map<long, std::shared_ptr<rti1516e::ObjectInstanceHandle>> ObjectInstanceStoreByHash;

	// To resolve InteractionClass data from an interaction class name of an incoming object
	typedef std::unordered_map<std::string, std::shared_ptr<InteractionClass>> InteractionDataStoreByName;
	// To resolve InteractionClass data from the hash of the interaction class handle of an incoming object
	typedef std::unordered_map<long, std::shared_ptr<InteractionClass>> InteractionDataStoreByHash;

	namespace util
	{
		//----------------------------------------
		//            Enum declaration
		//-----------------------------------------

		/**
		 *  Represents various logging levels of the logger
		 *
		 *  @see Logger#setLogLevel( LogLevel )
		 */
		enum LogLevel
		{
			LevelTrace = 0,
			LevelDebug = 1,
			LevelInfo = 2,
			LevelWarn = 3,
			LevelError = 4,
			LevelCritical = 5,
			LevelOff = 6
		};

		//----------------------------------------
		//           Conversion helpers
		//-----------------------------------------

		/**
		 * The {@link ConversionHelper} contains various static helper methods
		 */
		class ConversionHelper
		{
			public:

				/**
				 * Returns true if a given sharing state string is related
				 * to publishing, false otherwise
				 * 
				 * @param sharingStateString sharing state string as in a SOM
				 * @return true if the given sharing state string is related to publishing,
				 *         false otherwise
				 */
				static bool isPublish( const std::string& sharingStateString )
				{
					bool publish = false;
					if( sharingStateString == "Publish" || sharingStateString == "PublishSubscribe")
					{
						publish = true;
					}
					return publish;
				}

				/**
				 * Returns true if a given sharing state string is related
				 * to subscribing, false otherwise
				 * 
				 * @param sharingStateString sharing state string as in a SOM
				 * @return true if the given sharing state string is related to subscribing,
				 *         false otherwise
				 */
				static bool isSubscribe( const std::string& sharingStateString )
				{
					bool subscribe = false;
					if( sharingStateString == "Subscribe" || sharingStateString == "PublishSubscribe")
					{
						subscribe = true;
					}
					return subscribe;
				}

				/**
				 * Converts a data type string to an equivalent data type enum
				 *
				 * @param dataTypeString string representation of a data type
				 * @return the equivalent enum representation of a data type string
				 */
				static DataType toEnumDataType( const std::string& dataTypeString )
				{
					DataType dataType = DataType::DATATYPEUNKNOWN;
					if( dataTypeString == "byte" )
					{
						dataType = DataType::DATATYPEBYTE;
					}
					else if( dataTypeString == "char" )
					{
						dataType = DataType::DATATYPECHAR;
					}
					else if( dataTypeString == "short" )
					{
						dataType = DataType::DATATYPESHORT;
					}
					else if( dataTypeString == "int" )
					{
						dataType = DataType::DATATYPEINT;
					}
					else if( dataTypeString == "long" )
					{
						dataType = DataType::DATATYPELONG;
					}
					else if( dataTypeString == "float" )
					{
						dataType = DataType::DATATYPEFLOAT;
					}
					else if( dataTypeString == "double" )
					{
						dataType = DataType::DATATYPEDOUBLE;
					}
					else if( dataTypeString == "boolean" )
					{
						dataType = DataType::DATATYPEBOOLEAN;
					}
					else if( dataTypeString == "String" )
					{
						dataType = DataType::DATATYPESTRING;
					}
					return dataType;
				}

				/**
				 * Converts a data type enum to an equivalent data type string representation
				 *
				 * @param dataType enum representation of a data type
				 * @return the equivalent string representation of a data type enum
				 */
				static std::string toStringDataType( const DataType dataType )
				{
					std::string dataTypeStr = "unknown";
					if( dataType == DataType::DATATYPEBYTE )
					{
						dataTypeStr = "byte";
					}
					else if( dataType == DataType::DATATYPECHAR )
					{
						dataTypeStr = "char";
					}
					else if( dataType == DataType::DATATYPESHORT )
					{
						dataTypeStr = "short";
					}
					else if( dataType == DataType::DATATYPEINT )
					{
						dataTypeStr = "int";
					}
					else if( dataType == DataType::DATATYPELONG )
					{
						dataTypeStr = "long";
					}
					else if( dataType == DataType::DATATYPEFLOAT )
					{
						dataTypeStr = "float";
					}
					else if( dataType == DataType::DATATYPEDOUBLE )
					{
						dataTypeStr = "double";
					}
					else if( dataType == DataType::DATATYPEBOOLEAN )
					{
						dataTypeStr = "boolean";
					}
					else if( dataType == DataType::DATATYPESTRING )
					{
						dataTypeStr = "String";
					}
					return dataTypeStr;
				}

				/**
				 * Converts a synchronization point to an equivalent string representation
				 * 
				 * @param point synchronization point
				 * @return the equivalent string representation of a synchronization point
				 */
				static std::string SynchPointToString( SynchPoint point )
				{
					std::string synchPointStr = "";
					if( point == SynchPoint::READY_TO_POPULATE )
					{
						synchPointStr = "ReadyToPopulate";
					}
					else if( point == SynchPoint::READY_TO_RUN )
					{
						synchPointStr = "ReadyToRun";
					}
					else if( point == SynchPoint::READY_TO_RESIGN )
					{
						synchPointStr = "ReadyToResign";
					}
					return synchPointStr;
				}

				/**
				 * Converts a given string to an equivalent synchronization point representation
				 * 
				 * @param synchPointStr wstring representation of a synchronization point
				 * @return the equivalent synchronization point representation of a wstring
				 */
				static SynchPoint StringToSynchPoint( std::wstring synchPointStr )
				{
					SynchPoint synchPointEnum = SynchPoint::POINT_UNKNOWN;
					if( synchPointStr == L"ReadyToPopulate" )
					{
						synchPointEnum =  SynchPoint::READY_TO_POPULATE;
					}
					else if( synchPointStr == L"ReadyToRun" )
					{
						synchPointEnum =  SynchPoint::READY_TO_RUN;
					}
					else if( synchPointStr == L"ReadyToResign" )
					{
						synchPointEnum =  SynchPoint::READY_TO_RESIGN;
					}
					return synchPointEnum;
				}

				/**
				 * Converts a string to a wstring
				 * 
				 * @param str the message that required wstring representation
				 * @return the wstring representation of a string
				 */
				static std::wstring s2ws( const std::string& str )
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.from_bytes( str );
				}

				/**
				 * Converts a wstring to a string
				 * 
				 * @param wstr the msg that required string representation
				 * @return the string representation of a wstring
				 */
				static std::string ws2s( const std::wstring& wstr )
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.to_bytes( wstr );
				}

				static LogLevel toLogLevel( const std::string& str )
				{
					LogLevel level = LevelInfo;
					if( str == "trace" )
					{
						level = LevelTrace;
					}
					else if( str == "debug" )
					{
						level = LevelDebug;
					}
					else if( str == "warning" )
					{
						level = LevelWarn;
					}
					else if( str == "error" )
					{
						level = LevelError;
					}
					else if( str == "critical" )
					{
						level = LevelCritical;
					}
					else if( str == "off" )
					{
						level = LevelOff;
					}

					return level;
				}

				static bool isMatch( const std::string& srcString, const std::string& regexString  )
				{
					bool match = false;

					std::regex e( regexString );
					if ( std::regex_match(srcString, e) )
					{
						match = true;
					}
					return match;
				}

				static bool isMatch( const std::string& srcString, const std::list<std::string>& regexStrings  )
				{
					bool match = false;

					for( std::string regexString : regexStrings )
					{
						std::regex e( regexString );
						if ( std::regex_match(srcString, e) )
						{
							match = true;
							break;
						}
					}
					return match;
				}

				static std::list<std::string> tokenize( std::string& stringVal, char delimiter )
				{
					std::list<std::string> tokense;

					std::stringstream ss(stringVal);
					std::string tmpString;

					while( getline(ss, tmpString, delimiter) )
					{
						tokense.push_back(tmpString);
					}

					return tokense;
				}
		};
	}
}
