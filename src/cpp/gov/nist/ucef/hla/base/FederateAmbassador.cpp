#include "FederateAmbassador.h"
#include <string>
#include <mutex>

#include "FederateBase.h"

#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/hla/types.h"
#include "RTI/time/HLAfloat64Time.h"

using namespace rti1516e;
using namespace std;
using namespace base::util;

namespace base
{
	FederateAmbassador::FederateAmbassador( FederateBase* federateBase ) : m_regulated( false ),
	                                                                       m_constrained( false ),
	                                                                       m_federateTime( 0.0 ),
	                                                                       m_federateBase( federateBase )
	{
	}

	FederateAmbassador::~FederateAmbassador() throw()
	{

	}

	void FederateAmbassador::announceSynchronizationPoint( const wstring& label,
	                                                       const VariableLengthData& tag )
	                                                            throw( FederateInternalError )
	{
		SynchPoint synchPoint = ConversionHelper::StringToSynchPoint( label );
		if( synchPoint == POINT_UNKNOWN )
		{
			// may be we can achieve this immediately
			return;
		}
		lock_guard<mutex> lock( m_threadSafeLock );

		string sLabel = ConversionHelper::ws2s( label );
		if( announcedSynchPoints.find(sLabel) == announcedSynchPoints.end() )
			announcedSynchPoints.insert( sLabel );
	}


	void FederateAmbassador::federationSynchronized( const wstring& label,
	                                                 const FederateHandleSet& failedSet )
	                                                             throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		string sLabel = ConversionHelper::ws2s( label );
		if( achievedSynchPoints.find(sLabel) == achievedSynchPoints.end() )
			achievedSynchPoints.insert( sLabel );
	}

	void FederateAmbassador::timeRegulationEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		setTimeRegulatedFlag( true );
		this->m_federateTime = logicalTimeAsDouble( theFederateTime );
	}

	void FederateAmbassador::timeConstrainedEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		setTimeConstrainedFlag( true );
		this->m_federateTime = logicalTimeAsDouble( theFederateTime );
	}

	void FederateAmbassador::timeAdvanceGrant( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		this->m_federateTime = logicalTimeAsDouble( theFederateTime );
	}

	void FederateAmbassador::discoverObjectInstance( ObjectInstanceHandle theObject,
	                                                 ObjectClassHandle theObjectClass,
	                                                 const wstring& theObjectName )
	                                                              throw( FederateInternalError )
	{
		m_federateBase->incomingObjectRegistration( theObject.hash(), theObjectClass.hash() );
	}

	void FederateAmbassador::discoverObjectInstance( ObjectInstanceHandle theObject,
	                                                 ObjectClassHandle theObjectClass,
	                                                 const wstring& theObjectName,
	                                                 FederateHandle producingFederate )
	                                                              throw( FederateInternalError )
	{
		discoverObjectInstance( theObject, theObjectClass, theObjectName );
	}

	void FederateAmbassador::reflectAttributeValues( ObjectInstanceHandle theObject,
	                                                 AttributeHandleValueMap const& theAttributeValues,
	                                                 VariableLengthData const& theUserSuppliedTag,
	                                                 OrderType sentOrder,
	                                                 TransportationType theType,
	                                                 SupplementalReflectInfo theReflectInfo )
	                                                              throw( FederateInternalError )
	{
		m_federateBase->incomingAttributeReflection( theObject.hash(), theAttributeValues );
	}

	void FederateAmbassador::reflectAttributeValues( ObjectInstanceHandle theObject,
	                                                 AttributeHandleValueMap const& theAttributeValues,
	                                                 VariableLengthData const& theUserSuppliedTag,
	                                                 OrderType sentOrder,
	                                                 TransportationType theType,
	                                                 LogicalTime const& theTime,
	                                                 OrderType receivedOrder,
	                                                 SupplementalReflectInfo theReflectInfo )
	                                                              throw( FederateInternalError )
	{
		reflectAttributeValues( theObject, theAttributeValues, theUserSuppliedTag, sentOrder, theType, theReflectInfo );
	}

	void FederateAmbassador::reflectAttributeValues( ObjectInstanceHandle theObject,
	                                                 AttributeHandleValueMap const& theAttributeValues,
	                                                 VariableLengthData const& theUserSuppliedTag,
	                                                 OrderType sentOrder,
	                                                 TransportationType theType,
	                                                 LogicalTime const & theTime,
	                                                 OrderType receivedOrder,
	                                                 MessageRetractionHandle theHandle,
	                                                 SupplementalReflectInfo theReflectInfo )
	                                                              throw( FederateInternalError )
	{
		reflectAttributeValues( theObject, theAttributeValues, theUserSuppliedTag, sentOrder, theType, theReflectInfo );
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw( FederateInternalError )
	{
		if( theObject.isValid() )
		{
			m_federateBase->incomingObjectDeletion( theObject.hash() );
		}
		else
		{
			string msg = "Received object delete notification with an invalid handler.";
			Logger::getInstance().log( msg, LevelError );
		}
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               LogicalTime const& theTime,
	                                               OrderType receivedOrder,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw( FederateInternalError )
	{
		removeObjectInstance( theObject, theUserSuppliedTag, sentOrder, theRemoveInfo );
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               LogicalTime const& theTime,
	                                               OrderType receivedOrder,
	                                               MessageRetractionHandle theHandle,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw( FederateInternalError )
	{
		removeObjectInstance( theObject, theUserSuppliedTag, sentOrder, theRemoveInfo );
	}

	void FederateAmbassador::receiveInteraction( InteractionClassHandle theInteraction,
	                                             const ParameterHandleValueMap& theParameters,
	                                             const VariableLengthData& tag,
	                                             OrderType sentOrder,
	                                             TransportationType theType,
	                                             SupplementalReceiveInfo theReceiveInfo )
	                                                               throw( FederateInternalError )
	{
		m_federateBase->incomingInteraction( theInteraction.hash(), theParameters );
	}

	void FederateAmbassador::receiveInteraction( InteractionClassHandle theInteraction,
	                                             const ParameterHandleValueMap& theParameters,
	                                             const VariableLengthData& tag,
	                                             OrderType sentOrder,
	                                             TransportationType theType,
	                                             const LogicalTime& theTime,
	                                             OrderType receivedOrder,
	                                             SupplementalReceiveInfo theReceiveInfo )
	                                                              throw( FederateInternalError )
	{
		receiveInteraction( theInteraction, theParameters, tag, sentOrder, theType, theReceiveInfo );
	}

	void FederateAmbassador::receiveInteraction( InteractionClassHandle theInteraction,
	                                             const ParameterHandleValueMap& theParameters,
	                                             const VariableLengthData& tag,
	                                             OrderType sentOrder,
	                                             TransportationType theType,
	                                             const LogicalTime& theTime,
	                                             OrderType receivedOrder,
	                                             MessageRetractionHandle theHandle,
	                                             SupplementalReceiveInfo theReceiveInfo )
	                                                              throw( FederateInternalError )
	{
		receiveInteraction( theInteraction, theParameters, tag, sentOrder, theType, theReceiveInfo );
	}

	bool FederateAmbassador::isAnnounced( string& announcedPoint )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		bool announced = announcedSynchPoints.find( announcedPoint ) == announcedSynchPoints.end() ? false : true;
		return announced;
	}

	bool FederateAmbassador::isAchieved( string& achievedPoint )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		bool achieved = achievedSynchPoints.find( achievedPoint ) == achievedSynchPoints.end() ? false : true;
		return achieved;
	}

	bool FederateAmbassador::isTimeRegulated()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_regulated;
	}

	void FederateAmbassador::setTimeRegulatedFlag( bool flag )
	{
		m_regulated = flag;
	}

	bool FederateAmbassador::isTimeConstrained()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_constrained;
	}

	void FederateAmbassador::setTimeConstrainedFlag( bool flag )
	{
		m_constrained = flag;
	}

	double FederateAmbassador::getFederateTime()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_federateTime;
	}
	
	double FederateAmbassador::logicalTimeAsDouble( const LogicalTime& time )
	{
		const HLAfloat64Time& hlaTime = dynamic_cast<const HLAfloat64Time&>( time );
		return hlaTime.getTime();
	}
}