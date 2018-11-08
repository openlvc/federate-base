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
	class ObjectClassHandle;
	class AttributeHandle;
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
		 *  Represents the publish and subscribe state of an object, interaction,
		 *  parameter or attribute
		 *
		 *  @see ObjectAttribute
		 *  @see ObjectClass
		 *  @see InteractionParameter
		 *  @see InteractionClass
		 */
		enum SharingState
		{
			StatePublish,   // publishing
			StateSubscribe, // subscribe to
			StatePubSub,    // both publishing and subscribe to
			StateNone,      // none
		};

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
			ObjectAttribute() : name( L"" ),
			                    sharingState( StateNone )
			{

			}

			std::wstring name;
			SharingState sharingState;
			std::shared_ptr<rti1516e::AttributeHandle> handle;
		};

		/**
		 *  Represents an object class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getObjectClasses(string&)
		 */
		typedef std::unordered_map<std::string, std::shared_ptr<ObjectAttribute>> ObjectAttributes;
		struct ObjectClass
		{
			ObjectClass() : name( L"" ),
			                sharingState( StateNone ),
			                classHandle( nullptr ),
			                instanceHandle( nullptr ),
			                objectAttributes{ },
			                hasAttrToPubOrSub( false )
			{

			}
			std::wstring name; // fully qualified object class name
			SharingState sharingState;
			std::shared_ptr<rti1516e::ObjectClassHandle> classHandle;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> instanceHandle;
			ObjectAttributes objectAttributes;
			bool hasAttrToPubOrSub;
		};

		/**
		 *  Represents a parameter of an interaction class in a given
		 *  Simulation Object Model
		 *
		 *  @see InteractionClass
		 */
		struct InteractionParameter
		{
			InteractionParameter() : name( L"" )
			{

			}
			std::wstring name;
		};

		/**
		 *  Represents an interaction class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getInteractionClasses(string&)
		 */
		typedef std::unordered_map<std::string, std::shared_ptr<InteractionParameter>> InteractionParameters;
		struct InteractionClass
		{
			InteractionClass() : name( L"" ),
			                     sharingState( StateNone ),
			                     parameters{ }
			{

			}
			std::wstring name; // fully qualified interaction class name
			SharingState sharingState;
			InteractionParameters parameters;
		};

		//----------------------------------------
		//           Typedefs
		//-----------------------------------------

		// unordered_map because we do not need any ordering,
		// what we need is a faster way to get the object class
		typedef std::unordered_map<std::string, std::shared_ptr<ObjectClass>> ObjectClassMap;

		//----------------------------------------
		//           Conversion helpers
		//-----------------------------------------
		class ConversionHelper
		{
			public:

				/**
				 *  Convert a given string to a SharingState
				 *
				 *  @see SharingState
				 */
				static SharingState toSharingState( const std::string& sharingStateString )
				{
					SharingState sharingState = StateNone;
					if( sharingStateString == "Publish" )
					{
						sharingState = StatePublish;
					}
					else if( sharingStateString == "Subscribe" )
					{
						sharingState = StateSubscribe;
					}
					else if( sharingStateString == "PublishSubscribe" )
					{
						sharingState = StatePubSub;
					}

					return sharingState;
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

				static SynchPoint StringToSynchPoint( std::string synchPointStr )
				{
					SynchPoint synchPointEnum = SynchPoint::PointUnknown;
					if( synchPointStr == "ReadyToPopulate" )
					{
						synchPointEnum =  SynchPoint::PointReadyToPopulate;
					}
					else if( synchPointStr == "ReadyToRun" )
					{
						synchPointEnum =  SynchPoint::PointReadyToRun;
					}
					else if( synchPointStr == "ReadyToResign" )
					{
						synchPointEnum =  SynchPoint::PointReadyToResign;
					}
					return synchPointEnum;
				}

				static  std::wstring s2ws( const std::string& str )
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.from_bytes(str);
				}

				static  std::string ws2s(const std::wstring& wstr)
				{
					using convert_typeX = std::codecvt_utf8<wchar_t>;
					std::wstring_convert<convert_typeX, wchar_t> converterX;

					return converterX.to_bytes(wstr);
				}
		};
	}
}
