#include "FederateAmbassador.h"
#include <string>
#include <mutex>

#include "FederateBase.h"

#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/types.h"
#include "RTI/time/HLAfloat64Time.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;
namespace ucef
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
		SynchPoint synPoint = ConversionHelper::StringToSynchPoint( label );
		if( synPoint == PointUnknown )
		{
			// may be we can achieve this immediately
			return;
		}
		lock_guard<mutex> lock( m_threadSafeLock );
		if( announcedSynchPoints.find(label) == announcedSynchPoints.end() )
			announcedSynchPoints.insert( label );
	}


	void FederateAmbassador::federationSynchronized( const wstring& label,
	                                                 const FederateHandleSet& failedSet )
	                                                             throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		if( achievedSynchPoints.find(label) == achievedSynchPoints.end() )
			achievedSynchPoints.insert(label);
	}

	void FederateAmbassador::timeRegulationEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		this->m_regulated = true;
		this->m_federateTime = logicalTimeAsDouble( theFederateTime );
	}

	void FederateAmbassador::timeConstrainedEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		this->m_constrained = true;
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
	                                                              throw(FederateInternalError)
	{
		m_federateBase->incomingObjectRegistration( theObject.hash(), theObjectClass.hash() );
	}

	void FederateAmbassador::discoverObjectInstance( ObjectInstanceHandle theObject,
	                                                 ObjectClassHandle theObjectClass,
	                                                 const wstring& theObjectName,
	                                                 FederateHandle producingFederate )
	                                                              throw(FederateInternalError)
	{
		discoverObjectInstance( theObject, theObjectClass, theObjectName );
	}

	void FederateAmbassador::reflectAttributeValues( ObjectInstanceHandle theObject,
	                                                 AttributeHandleValueMap const& theAttributeValues,
	                                                 VariableLengthData const& theUserSuppliedTag,
	                                                 OrderType sentOrder,
	                                                 TransportationType theType,
	                                                 SupplementalReflectInfo theReflectInfo )
	                                                              throw(FederateInternalError)
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
	                                                              throw(FederateInternalError)
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
	                                                              throw(FederateInternalError)
	{
		reflectAttributeValues( theObject, theAttributeValues, theUserSuppliedTag, sentOrder, theType, theReflectInfo );
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw(FederateInternalError)
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

	bool FederateAmbassador::isAnnounced( wstring& announcedPoint )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		bool announced = announcedSynchPoints.find( announcedPoint ) == announcedSynchPoints.end() ? false : true;
		return announced;
	}

	bool FederateAmbassador::isAchieved( wstring& achievedPoint )
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		bool achieved = achievedSynchPoints.find( achievedPoint ) == achievedSynchPoints.end() ? false : true;
		return achieved;
	}

	bool FederateAmbassador::isRegulated()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_regulated;
	}

	bool FederateAmbassador::isConstrained()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_constrained;
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