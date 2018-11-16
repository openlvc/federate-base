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
			rti1516e::ObjectClassHandle getClassHandle( const std::wstring& name );
			rti1516e::AttributeHandle getAttributeHandle( rti1516e::ObjectClassHandle& classHandle,
			                                              const std::wstring& name );
			void publishSubscribeObjectClassAttributes( rti1516e::ObjectClassHandle& classHandle,
			                                            std::set<rti1516e::AttributeHandle>& pubAttributes,
			                                            std::set<rti1516e::AttributeHandle>& subAttributes );
			void publishSubscribeInteractionClassParams( rti1516e::InteractionClassHandle& interactionHandle,
			                                             bool publish,
			                                             bool subScribe);
			std::shared_ptr<HLAObject> registerObject( const std::string& className,
			                                           rti1516e::ObjectClassHandle& classHandle);
			rti1516e::InteractionClassHandle getInteractionHandle( const std::wstring& name );
			rti1516e::ParameterHandle getParameterHandle( rti1516e::InteractionClassHandle& interactionHandle,
			                                              const std::wstring& name );
			void publishSubscribeInteractionClass( rti1516e::InteractionClassHandle& interactionHandle,
			                                       const bool publish,
			                                       const bool subscribe );

			void announceSynchronizationPoint( const std::wstring& synchPoint );
			void achieveSynchronizationPoint( const std::wstring& synchPoint );
			void advanceLogicalTime( const double requestedTime );
			void updateObjectInstances( std::shared_ptr<HLAObject>& hlaObject,
			                            const util::ObjectCacheStoreByName& cacheStore );
			void sendInteraction( std::shared_ptr<HLAInteraction>& hlaInteraction,
			                      const util::InteractionCacheStoreByName& cacheStore );
			void deleteObjectInstances( std::shared_ptr<HLAObject>& hlaObject );
			void resign();
			void tickForCallBacks( double min, double max );

	private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
	};
}

