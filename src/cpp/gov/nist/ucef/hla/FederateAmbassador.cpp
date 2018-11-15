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
	                                                                       m_advanced( false ),
	                                                                       m_federateTime( 0.0 ),
	                                                                       m_federateBase( federateBase )
	{
	}

	FederateAmbassador::~FederateAmbassador() throw()
	{

	}

	//----------------------------------------------------------
	//            synchronization related methods
	//----------------------------------------------------------
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

	//----------------------------------------------------------
	//            Time related methods
	//----------------------------------------------------------
	void FederateAmbassador::timeRegulationEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( m_threadSafeLock );
			this->m_regulated = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	void FederateAmbassador::timeConstrainedEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( m_threadSafeLock );
			this->m_constrained = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	void FederateAmbassador::timeAdvanceGrant( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( m_threadSafeLock );
			this->m_advanced = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	void FederateAmbassador::discoverObjectInstance( ObjectInstanceHandle theObject,
	                                                 ObjectClassHandle theObjectClass,
	                                                 const wstring& theObjectName )
	                                                              throw(FederateInternalError)
	{
		Logger& logger = Logger::getInstance();
		shared_ptr<ObjectInstanceHandle> instanceHandle = make_shared<ObjectInstanceHandle>( theObject );
		shared_ptr<ObjectClass> objectClass = m_federateBase->getObjectClass( theObjectClass.hash() );
		if( objectClass )
		{
			shared_ptr<HLAObject> object =
				make_shared<HLAObject>( ConversionHelper::ws2s( objectClass->name ), instanceHandle );
			m_federateBase->receiveObjectRegistration( object, m_federateTime );
		}
		else
		{
			logger.log( "Discovered an unknown object with name " +
			             ConversionHelper::ws2s( theObjectName ), LevelError );
		}
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
		Logger& logger = Logger::getInstance();
		shared_ptr<HLAObject> object = m_federateBase->findIncomingObject( theObject.hash() );
		if( object )
		{
			shared_ptr<ObjectClass> objectClass = m_federateBase->getObjectClass( object->getClassName() );

			for( auto& incomingAttributeValue : theAttributeValues )
			{
				ObjectAttributes& attributes = objectClass->objectAttributes;
				for( auto& attribute : attributes )
				{
					if( attribute.second->handle->hash() == incomingAttributeValue.first.hash())
					{
						size_t size = incomingAttributeValue.second.size();
						const void* data = incomingAttributeValue.second.data();
						shared_ptr<void> arr(new char[size](), [](char *p) { delete [] p; });
						memcpy_s(arr.get(), size, data, size);
						object->setAttributeValue
						            ( ConversionHelper::ws2s(attribute.second->name), arr, size );
					}
				}
			}
			m_federateBase->objectUpdate( std::const_pointer_cast<const HLAObject>(object), m_federateTime );
		}
		else
		{
			logger.log( string("Received attribute update of an unknown object."), LevelError );
		}
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
			shared_ptr<ObjectInstanceHandle> instanceHandle = make_shared<ObjectInstanceHandle>( theObject );

			shared_ptr<HLAObject> object = make_shared<HLAObject>( "", instanceHandle );
			m_federateBase->objectDelete( object );
		}
		else
		{
			Logger::getInstance().log( string("Received object delete notification with an invalid handler."),
			                           LevelError );
		}
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               LogicalTime const & theTime,
	                                               OrderType receivedOrder,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw(FederateInternalError)
	{
		removeObjectInstance( theObject, theUserSuppliedTag, sentOrder, theRemoveInfo );
	}

	void FederateAmbassador::removeObjectInstance( ObjectInstanceHandle theObject,
	                                               VariableLengthData const& theUserSuppliedTag,
	                                               OrderType sentOrder,
	                                               LogicalTime const & theTime,
	                                               OrderType receivedOrder,
	                                               MessageRetractionHandle theHandle,
	                                               SupplementalRemoveInfo theRemoveInfo )
	                                                              throw(FederateInternalError)
	{
		removeObjectInstance( theObject, theUserSuppliedTag, sentOrder, theRemoveInfo );
	}
	//----------------------------------------------------------
	//             Federate Access Methods
	//----------------------------------------------------------
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

	bool FederateAmbassador::isTimeAdvanced()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_advanced;
	}

	void FederateAmbassador::resetTimeAdvanced()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		m_advanced = false;
	}

	double FederateAmbassador::getFederateTime()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		return m_federateTime;
	}
	
	//----------------------------------------------------------
	//             Instance Methods
	//----------------------------------------------------------
	double FederateAmbassador::convertTime( const LogicalTime& theTime )
	{
		const HLAfloat64Time& castTime = dynamic_cast<const HLAfloat64Time&>(theTime);
		return castTime.getTime();
	}
}

