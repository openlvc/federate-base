#pragma once

/**
 * @file types.h
 * Defines all POD types used bye UCEFFederate
 */

#include <list>
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
			StateNothing,   // none
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
			std::wstring name; // fully qulified object class name
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
			std::wstring name; // fully qulified interaction class name
			SharingState sharingState;
			std::list<std::shared_ptr<InteractionParameter>> parameters;
		};

	}
}
