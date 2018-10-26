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

namespace ucef
{
	namespace util
	{
		//----------------------------------------
		//            Enum Declaration
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

		//----------------------------------------
		//            Struct Declaration
		//-----------------------------------------

		/**
		 *  Represents an attribute of an object class in a given Simulation Object Model
		 *
		 *  @see ObjectClass
		 */
		struct ObjectAttribute
		{
			std::wstring name;
			SharingState sharingState;
		};

		/**
		 *  Represents an object class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getObjectClasses(string&)
		 */
		struct ObjectClass
		{
			std::wstring name; // fully qualified object class name
			SharingState sharingState;
			std::list<std::shared_ptr<ObjectAttribute>> attributes;
		};

		/**
		 *  Represents a parameter of an interaction class in a given
		 *  Simulation Object Model
		 *
		 *  @see InteractionClass
		 */
		struct InteractionParameter
		{
			std::wstring name;
			SharingState sharingState;
		};

		/**
		 *  Represents an interaction class in a given Simulation Object Model
		 *
		 *  @see SOMParser#getInteractionClasses(string&)
		 */
		struct InteractionClass
		{
			std::wstring name; // fully qualified interaction class name
			SharingState sharingState;
			std::list<std::shared_ptr<InteractionParameter>> parameters;
		};

		//----------------------------------------
		//           Conversion support class
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
