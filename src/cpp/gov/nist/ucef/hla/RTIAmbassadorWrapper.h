#pragma once

#include <memory>
#include <set>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace rti1516e
{
	class AttributeHandle;
	class InteractionClassHandle;
	class ObjectClassHandle;
	class ParameterHandle;
	class RTIambassador;
}

namespace ucef
{
	class FederateAmbassador;
	namespace util
	{
		class FederateConfiguration;
	}

	class RTIAmbassadorWrapper
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			RTIAmbassadorWrapper();
			virtual ~RTIAmbassadorWrapper();
			RTIAmbassadorWrapper( const RTIAmbassadorWrapper& ) = delete;

		public:
			//----------------------------------------------------------
			//             Instance methods
			//----------------------------------------------------------
			void connect( std::shared_ptr<FederateAmbassador>& federateAmbassador,
			              const std::shared_ptr<util::FederateConfiguration>& config );
			void createFederation( const std::shared_ptr<util::FederateConfiguration>& config );
			void joinFederation( const std::shared_ptr<util::FederateConfiguration>& config );
			void enableTimeRegulated( const std::shared_ptr<util::FederateConfiguration>& config );
			void enableTimeConstrained( const std::shared_ptr<util::FederateConfiguration>& config );
			rti1516e::ObjectClassHandle getClassHandle( std::wstring& name );
			rti1516e::AttributeHandle getAttributeHandle( rti1516e::ObjectClassHandle& classHandle,
			                                              std::wstring& name );
			void publishSubscribeObjectClassAttributes( rti1516e::ObjectClassHandle& classHandle,
			                                            std::set<rti1516e::AttributeHandle>& pubAttributes,
			                                            std::set<rti1516e::AttributeHandle>& subAttributes );
			rti1516e::ObjectInstanceHandle getInstanceHandle( rti1516e::ObjectClassHandle& classHandle);
			rti1516e::InteractionClassHandle getInteractionHandle( std::wstring& name );
			rti1516e::ParameterHandle getParameterHandle( rti1516e::InteractionClassHandle& interactionHandle,
			                                              std::wstring& name );
			void publishSubscribeInteractionClass( rti1516e::InteractionClassHandle& interactionHandle,
			                                       bool toPublish,
			                                       bool toSubscribe );

			void announceSynchronizationPoint( std::wstring& synchPoint );
			void achieveSynchronizationPoint( std::wstring& synchPoint );
			void advanceLogicalTime( double requestedTime );
			void deleteObjectInstances( rti1516e::ObjectInstanceHandle& instanceHandle );
			void resign();
			void tickForCallBacks( double min, double max );

	private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
	};
}

