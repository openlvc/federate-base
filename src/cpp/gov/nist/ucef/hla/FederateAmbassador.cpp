#include "FederateAmbassador.h"

#include <mutex>
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/types.h"
#include "RTI/time/HLAfloat64Time.h"

using namespace rti1516e;
using namespace std;

namespace ucef
{
	FederateAmbassador::FederateAmbassador()
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
	                                                            throw(FederateInternalError)
	{
		lock_guard<mutex> lock( threadSafeLock );
		if( announcedSynchPoints.find(label) == announcedSynchPoints.end() )
			announcedSynchPoints.insert( label );
	}


	void FederateAmbassador::federationSynchronized( const wstring& label,
	                                                 const FederateHandleSet& failedSet )
	                                                             throw( FederateInternalError )
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		if( achievedSynchPoints.find(label) == achievedSynchPoints.end() )
			achievedSynchPoints.insert(label);
	}

	//----------------------------------------------------------
	//            Time related methods
	//----------------------------------------------------------
	void FederateAmbassador::timeRegulationEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( threadSafeLock );
			this->m_regulating = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	void FederateAmbassador::timeConstrainedEnabled( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( threadSafeLock );
			this->m_constrained = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	void FederateAmbassador::timeAdvanceGrant( const LogicalTime& theFederateTime )
	                                                              throw( FederateInternalError )
	{
			lock_guard<mutex> lockGuard( threadSafeLock );
			this->m_advancing = true;
			this->m_federateTime = convertTime( theFederateTime );
	}

	//----------------------------------------------------------
	//             Federate Access Methods
	//----------------------------------------------------------
	bool FederateAmbassador::isAnnounced( wstring& announcedPoint )
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		bool announced = announcedSynchPoints.find( announcedPoint ) == announcedSynchPoints.end() ? false : true;
		return announced;
	}

	bool FederateAmbassador::isAchieved( wstring& achievedPoint )
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		bool achieved = achievedSynchPoints.find( achievedPoint ) == achievedSynchPoints.end() ? false : true;
		return achieved;
	}

	bool FederateAmbassador::isRegulating()
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		return m_regulating;
	}

	bool FederateAmbassador::isConstrained()
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		return m_constrained;
	}

	bool FederateAmbassador::isAdvancing()
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
		return m_advancing;
	}

	double FederateAmbassador::getFederateTime()
	{
		lock_guard<mutex> lockGuard( threadSafeLock );
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

