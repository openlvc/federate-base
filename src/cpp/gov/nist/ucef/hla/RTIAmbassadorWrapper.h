#pragma once

#include <memory>
#include <set>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "gov/nist/ucef/hla/HLAInteraction.h"
#include "gov/nist/ucef/util/types.h"

namespace rti1516e
{
	class AttributeHandle;
	class InteractionClassHandle;
	class ObjectClassHandle;
	class ParameterHandle;
	class RTIambassador;
	class VariableLengthData;
}

namespace ucef
{
	class FederateAmbassador;
	namespace util
	{
		class FederateConfiguration;
	}

	class UCEF_API RTIAmbassadorWrapper
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
			void connect( const std::shared_ptr<FederateAmbassador>& federateAmbassador,
			              const bool isImmediate );
			void createFederation( const std::string& federationName, const std::vector<std::wstring>& fomPaths );
			void joinFederation( const std::string& federateName,
			                     const std::string& federateType,
			                     const std::string& federationName );
			void enableTimeRegulated( const float lookAhead );
			void enableTimeConstrained();

			void publishSubscribeObjectClassAttributes( rti1516e::ObjectClassHandle& classHandle,
			                                            std::set<rti1516e::AttributeHandle>& pubAttributes,
			                                            std::set<rti1516e::AttributeHandle>& subAttributes );
			void publishSubscribeInteractionClass( rti1516e::InteractionClassHandle& interactionHandle,
			                                       const bool publish,
			                                       const bool subscribe );
			std::shared_ptr<HLAObject> registerObjectInstance( const std::string& className );

			void announceSynchronizationPoint( const std::wstring& synchPoint );
			void achieveSynchronizationPoint( const std::wstring& synchPoint );
			void advanceLogicalTime( const double requestedTime );
			void updateObjectInstances( std::shared_ptr<HLAObject>& hlaObject );
			void sendInteraction( std::shared_ptr<HLAInteraction>& hlaInteraction );
			void deleteObjectInstances( std::shared_ptr<HLAObject>& hlaObject );
			void resign();
			void tickForCallBacks( double min, double max );

			rti1516e::ObjectClassHandle getClassHandle( const std::wstring& name );
			rti1516e::AttributeHandle getAttributeHandle( const rti1516e::ObjectClassHandle& classHandle,
			                                              const std::wstring& name );
			std::wstring RTIAmbassadorWrapper::getAttributeName( const rti1516e::ObjectClassHandle& classHandle,
			                                                     const rti1516e::AttributeHandle& attributeHandle );
			rti1516e::InteractionClassHandle getInteractionHandle( const std::wstring& name );
			rti1516e::ParameterHandle getParameterHandle( const rti1516e::InteractionClassHandle& interactionHandle,
			                                              const std::wstring& name );
			std::wstring getParameterName( const rti1516e::InteractionClassHandle& interactionHandle,
			                               const rti1516e::ParameterHandle& parameterHandle );
	private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
			util::OutgoingStore m_outgoingStore;
	};
}

