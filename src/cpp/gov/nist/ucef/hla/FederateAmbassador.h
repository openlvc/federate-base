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
	/**
	 * The {@link FederateAmbassador} class implements an interface that allows RTI
	 * infrastructure to communicate with the Federate.
	 *
	 * @see IFederateBase
	 */
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
			//             RTI Callbacks - Time management
			//----------------------------------------------------------
			virtual void announceSynchronizationPoint( const std::wstring& label,
			                                           const rti1516e::VariableLengthData& tag )
			                                                throw(rti1516e::FederateInternalError) override;
			virtual void federationSynchronized( const std::wstring& label,
			                                     const rti1516e::FederateHandleSet& failedSet )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void timeRegulationEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void timeConstrainedEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void timeAdvanceGrant( const rti1516e::LogicalTime& theFederateTime )
			                                                throw(rti1516e::FederateInternalError) override;

			//----------------------------------------------------------
			//             RTI Callbacks - Object Management
			//----------------------------------------------------------
			virtual void discoverObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::ObjectClassHandle theObjectClass,
			                                     const std::wstring& theObjectName )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void discoverObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::ObjectClassHandle theObjectClass,
			                                     const std::wstring& theObjectName,
			                                     rti1516e::FederateHandle producingFederate )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const& theAttributeValues,
			                                     rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const& theAttributeValues,
			                                     rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::LogicalTime const& theTime,
			                                     rti1516e::OrderType receivedOrder,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void reflectAttributeValues( rti1516e::ObjectInstanceHandle theObject,
			                                     rti1516e::AttributeHandleValueMap const& theAttributeValues,
			                                     rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                     rti1516e::OrderType sentOrder,
			                                     rti1516e::TransportationType theType,
			                                     rti1516e::LogicalTime const& theTime,
			                                     rti1516e::OrderType receivedOrder,
			                                     rti1516e::MessageRetractionHandle theHandle,
			                                     rti1516e::SupplementalReflectInfo theReflectInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::LogicalTime const& theTime,
			                                   rti1516e::OrderType receivedOrder,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void removeObjectInstance( rti1516e::ObjectInstanceHandle theObject,
			                                   rti1516e::VariableLengthData const& theUserSuppliedTag,
			                                   rti1516e::OrderType sentOrder,
			                                   rti1516e::LogicalTime const& theTime,
			                                   rti1516e::OrderType receivedOrder,
			                                   rti1516e::MessageRetractionHandle theHandle,
			                                   rti1516e::SupplementalRemoveInfo theRemoveInfo)
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 const rti1516e::LogicalTime& theTime,
			                                 rti1516e::OrderType receivedOrder,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			virtual void receiveInteraction( rti1516e::InteractionClassHandle theInteraction,
			                                 const rti1516e::ParameterHandleValueMap& theParameters,
			                                 const rti1516e::VariableLengthData& tag,
			                                 rti1516e::OrderType sentOrder,
			                                 rti1516e::TransportationType theType,
			                                 const rti1516e::LogicalTime& theTime,
			                                 rti1516e::OrderType receivedOrder,
			                                 rti1516e::MessageRetractionHandle theHandle,
			                                 rti1516e::SupplementalReceiveInfo theReceiveInfo )
			                                                throw(rti1516e::FederateInternalError) override;

			//----------------------------------------------------------
			//             Access Methods
			//----------------------------------------------------------

			/**
			 * Checks if a synchronization point has been announced to the federation
			 * 
			 * @param label the identifier of the synchronization point
			 * @return true if the synchronization point has been announced to the federation,
			 *         false otherwise
			 */
			bool isAnnounced( std::string& label );

			/**
			 * Checks if a given synchronization point has been achieved by the federation
			 * 
			 * @param label the identifier of the synchronization point
			 * @return true if the synchronization point has been achieved by the federation,
			 *         false otherwise
			 */
			bool isAchieved( std::string& achievedPoint );

			/**
			 * Checks if this federation is time regulated
			 * 
			 * @return true if this federation is time regulated, false otherwise
			 */
			bool isTimeRegulated();

			/**
			 * resets time regulating indicator flag
			 */
			void resetTimeRegulated();

			/**
			 * Checks if this federation is time constrained
			 * 
			 * @return true if this federation is time constrained, false otherwise
			 */
			bool isTimeConstrained();

			/**
			 * resets time constrained indicator flag
			 */
			void resetTimeConstrained();

			/**
			 * Returns the current federate time as a double
			 * 
			 * @return the current federate time as a double
			 */
			double getFederateTime();

			//----------------------------------------------------------
			//             Business Logic
			//----------------------------------------------------------

			/**
			 * Converts a logical time instance to a double
			 *
			 * @param time the logical time
			 * @return the double equivalent of the logical time
			 */
			double logicalTimeAsDouble( const rti1516e::LogicalTime& time );

		private:
			std::set<std::string> announcedSynchPoints;
			std::set<std::string> achievedSynchPoints;
			bool m_regulated;
			bool m_constrained;
			double m_federateTime;
			FederateBase* m_federateBase;
			std::mutex m_threadSafeLock;
	};
}
