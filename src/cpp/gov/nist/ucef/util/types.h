#pragma once

/**
 * @file types.h
 * Defines all types used by UCEFFederate
 */
#include <codecvt>
#include <list>
#include <locale>
#include <memory>
#include <string>
#include <unordered_map>

namespace rti1516e
{
	class ObjectInstanceHandle;
}

namespace ucef
{
	namespace util
	{
		//----------------------------------------
		//            Enum declaration
		//-----------------------------------------

		/**
		 *  Represents the logging level of the logger
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

		/**
		 *  Represents the synchronization points of this federate
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


		//----------------------------------------
		//            Struct declaration
		//-----------------------------------------

		/**
		 *  Represents an attribute of an object class given a Simulation Object Model
		 *
		 *  @see ObjectClass
		 */
		struct ObjectAttribute
		{
			ObjectAttribute() : name( "" ),
			                    publish( false ),
			                    subscribe( false )
			{
			}

			std::string name;
			bool publish;
			bool subscribe;
			};

		/**
		 *  Represents an object class given a Simulation Object Model
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
			                objectAttributes{ }
			{
			}
			std::string name; // fully qualified object class name
			bool publish;
			bool subscribe;
			ObjectAttributes objectAttributes;
		};

		/**
		 *  Represents a parameter of an interaction class a Simulation Object Model
		 *
		 *  @see InteractionClass
		 */
		struct InteractionParameter
		{
			InteractionParameter() : name( "" )
			{

			}
			std::string name;
		};

		/**
		 *  Represents an interaction class given a Simulation Object Model
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
			                     parameters{ }
			{

			}
			std::string name; // fully qualified interaction class name
			bool publish;
			bool subscribe;
			InteractionParameters parameters;
		};

		/**
		 * Type neutral representation of attrbiute and parameter values
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

		// to resolve object class data from a object class name of an incoming object
		typedef std::unordered_map<std::string, std::shared_ptr<ObjectClass>> ObjectDataStoreByName;
		// to resolve object class data from a object class handle's hash of an incoming object
		typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByHash;
		// to resolve object class data from a object instance handle's hash of an incoming object
		typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByInstance;
		// to resolve object instance handle from the instance hash of an outgoing object
		typedef std::unordered_map<long, std::shared_ptr<rti1516e::ObjectInstanceHandle>> ObjectInstanceStoreByHash;

		// to resolve interaction class data from an interaction class name of an incoming object
		typedef std::unordered_map<std::string, std::shared_ptr<InteractionClass>> InteractionDataStoreByName;
		// to resolve interaction class data from an interaction class handle's hash of an incoming object
		typedef std::unordered_map<long, std::shared_ptr<InteractionClass>> InteractionDataStoreByHash;

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
				 * @param str the msg that required wstring representation
				 * @return the wstring representation of a string
				 */
				static std::wstring s2ws( const std::string& str )
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.from_bytes(str);
				}

				/**
				 * Converts a wstring to a string
				 * 
				 * @param wstr the msg that required string representation
				 * @return the string representation of a wstring
				 */
				static std::string ws2s(const std::wstring& wstr)
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.to_bytes(wstr);
				}
		};
	}
}
