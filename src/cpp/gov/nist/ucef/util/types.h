#pragma once

/**
 * @file types.h
 * Defines all POD types used bye UCEFFederate
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
		 *  parameter or attribute
		 *
		 *  @see Logger::setLogLevel( LogLevel level )
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

		enum SynchPoint
		{
			PointReadyToPopulate = 0,
			PointReadyToRun = 1,
			PointReadyToResign = 2,
			PointUnknown = 3
		};


		//----------------------------------------
		//            Struct declaration
		//-----------------------------------------

		/**
		 *  Represents an attribute of an object class in a given Simulation Object Model
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
		 *  Represents an object class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getObjectClasses(string&)
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
		 *  Represents a parameter of an interaction class in a given
		 *  Simulation Object Model
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
		 *  Represents an interaction class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getInteractionClasses(string&)
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
		 *  Represents attribute and interaction data passed from/to user
		 *
		 *  @see HLAObject
		 */
		struct VariableData
		{
			std::shared_ptr<void> data;
			size_t size;
		};

		//----------------------------------------
		//           Typedefs
		//-----------------------------------------

		// unordered_map because we do not need any ordering,
		// what we need is a faster way to get the object class
		// to resolve object class data from the object class name for incoming objects
		typedef std::unordered_map<std::string, std::shared_ptr<ObjectClass>> ObjectDataStoreByName;
		// to resolve object class data from the object class hash for incoming objects
		typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByHash;
		// to resolve object class data from the object instance hash for incoming objects
		typedef std::unordered_map<long, std::shared_ptr<ObjectClass>> ObjectDataStoreByInstance;
		// to resolve object instance handle from the instance hash of the outgoing objects
		typedef std::unordered_map<long, std::shared_ptr<rti1516e::ObjectInstanceHandle>> ObjectInstanceStoreByHash;

		typedef std::unordered_map<std::string, std::shared_ptr<InteractionClass>> InteractionDataStoreByName;
		typedef std::unordered_map<long, std::shared_ptr<InteractionClass>> InteractionDataStoreByHash;
		//----------------------------------------
		//           Conversion helpers
		//-----------------------------------------
		class ConversionHelper
		{
			public:
				static bool isPublish( const std::string& sharingStateString )
				{
					bool publish = false;
					if( sharingStateString == "Publish" || sharingStateString == "PublishSubscribe")
					{
						publish = true;
					}
					return publish;
				}

				static bool isSubscribe( const std::string& sharingStateString )
				{
					bool subscribe = false;
					if( sharingStateString == "Subscribe" || sharingStateString == "PublishSubscribe")
					{
						subscribe = true;
					}
					return subscribe;
				}

				static std::string SynchPointToString( SynchPoint point )
				{
					std::string synchPointStr = "";
					if( point == SynchPoint::PointReadyToPopulate )
					{
						synchPointStr = "ReadyToPopulate";
					}
					else if( point == SynchPoint::PointReadyToRun )
					{
						synchPointStr = "ReadyToRun";
					}
					else if( point == SynchPoint::PointReadyToResign )
					{
						synchPointStr = "ReadyToResign";
					}
					return synchPointStr;
				}

				static std::wstring SynchPointToWstring( SynchPoint point )
				{
					std::wstring synchPointStr = L"";
					if( point == SynchPoint::PointReadyToPopulate )
					{
						synchPointStr = L"ReadyToPopulate";
					}
					else if( point == SynchPoint::PointReadyToRun )
					{
						synchPointStr = L"ReadyToRun";
					}
					else if( point == SynchPoint::PointReadyToResign )
					{
						synchPointStr = L"ReadyToResign";
					}
					return synchPointStr;
				}

				static SynchPoint StringToSynchPoint( std::wstring synchPointStr )
				{
					SynchPoint synchPointEnum = SynchPoint::PointUnknown;
					if( synchPointStr == L"ReadyToPopulate" )
					{
						synchPointEnum =  SynchPoint::PointReadyToPopulate;
					}
					else if( synchPointStr == L"ReadyToRun" )
					{
						synchPointEnum =  SynchPoint::PointReadyToRun;
					}
					else if( synchPointStr == L"ReadyToResign" )
					{
						synchPointEnum =  SynchPoint::PointReadyToResign;
					}
					return synchPointEnum;
				}

				static std::wstring s2ws( const std::string& str )
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.from_bytes(str);
				}

				static std::string ws2s(const std::wstring& wstr)
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.to_bytes(wstr);
				}
		};
	}
}
