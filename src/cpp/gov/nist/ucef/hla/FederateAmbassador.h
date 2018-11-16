#pragma once

#include <set>
#include <string>
#include <mutex>

#include "RTI/Exception.h"
#include "RTI/NullFederateAmbassador.h"
#include "RTI/Typedefs.h"
#include "RTI/VariableLengthData.h"
#include "RTI/Exception.h"

namespace ucef
{
	class FederateBase;
	class FederateAmbassador : public rti1516e::NullFederateAmbassador
	{

		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateAmbassador( FederateBase* federateBase );
			virtual ~FederateAmbassador() throw();
			FederateAmbassador(const FederateAmbassador&) = delete;

			//----------------------------------------------------------
			//            Federate Ambassador Callbacks
			//----------------------------------------------------------
			virtual void announceSynchronizationPoint( const std::wstring& label,
			                                           const rti1516e::VariableLengthData& tag )
			                                                            throw( rti1516e::FederateInternalError );
			virtual void federationSynchronized( const std::wstring& label,
			                                     const rti1516e::FederateHandleSet& failedSet )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeRegulationEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeConstrainedEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeAdvanceGrant( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			//----------------------------------------------------------
			//             Object Management Services
			//----------------------------------------------------------
			virtual void discoverObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::ObjectClassHandle theObjectClass,
			                                     const std::wstring& theObjectName )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void discoverObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::ObjectClassHandle theObjectClass,
			                                     const std::wstring& theObjectName,
			                                     rti1516e::FederateHandle producingFederate )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const & theAttributeValues,
			                                     rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const & theAttributeValues,
			                                     rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::LogicalTime const & theTime,
			                                     rti1516e::OrderType receivedOrder,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const & theAttributeValues,
			                                     rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::LogicalTime const & theTime,
			                                     rti1516e::OrderType receivedOrder,
			                                     rti1516e::MessageRetractionHandle theHandle,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                            throw (rti1516e:: FederateInternalError);

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::LogicalTime const & theTime,
			                                   rti1516e::OrderType receivedOrder,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                            throw (rti1516e:: FederateInternalError);

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const & theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::LogicalTime const & theTime,
			                                   rti1516e::OrderType receivedOrder,
			                                   rti1516e::MessageRetractionHandle theHandle,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                            throw (rti1516e:: FederateInternalError);

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 const rti1516e::LogicalTime& theTime,
			                                 rti1516e::OrderType receivedOrder,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 const rti1516e::LogicalTime& theTime,
			                                 rti1516e::OrderType receivedOrder,
			                                 rti1516e::MessageRetractionHandle theHandle,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                            throw( rti1516e::FederateInternalError );

			//----------------------------------------------------------
			//             Federate Access Methods
			//----------------------------------------------------------
			bool isAnnounced( std::wstring& announcedPoint );
			bool isAchieved( std::wstring& achievedPoint );
			bool isRegulated();
			bool isConstrained();
			bool isTimeAdvanced();
			void resetTimeAdvanced();
			double getFederateTime();

			//----------------------------------------------------------
			//             Instance Methods
			//----------------------------------------------------------
			double convertTime( const rti1516e::LogicalTime& theTime );
		private:
			FederateBase* m_federateBase;
			std::set<std::wstring> announcedSynchPoints;
			std::set<std::wstring> achievedSynchPoints;
			bool m_regulated;
			bool m_constrained;
			bool m_advanced;
			double m_federateTime;
			std::mutex m_threadSafeLock;
	};
}
