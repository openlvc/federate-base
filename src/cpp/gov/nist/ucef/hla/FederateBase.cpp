#include "FederateBase.h"

#include <thread>
#include <chrono>

#include "FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"

#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase() : m_rtiAmbassadorWrapper( new RTIAmbassadorWrapper() ),
	                               m_resign( false )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
		// initialise rti ambassador
		createRtiAmbassador();

		// before creating the federation hook
		beforeFederationCreate();
		// create federation
		createFederation();

		// before joining the federation hook
		beforeFederationJoin();
		// join the federation
		joinFederation();

		// enables time management policy for this federate
		enableTimePolicy();

		// now we are ready to populate the federation
		synchronize( PointReadyToPopulate );
		// inform RTI about the data we are going publish and subscribe
		publishAndSubscribe();

		// before federate run hook
		beforeReadyToRun();
		// now we are ready to run this federate
		synchronize( PointReadyToRun );

		while( true )
		{
			advanceLogicalTime();
		}

		// before resigning the federation hook
		beforeReadyToResign();
		// now we are ready to resign from this federation
		synchronize( PointReadyToResign );
		// resign from this federation
		resign();
	}

	void FederateBase::setResign( bool resign )
	{
		m_resign = resign;
	}

	void FederateBase::createRtiAmbassador()
	{
		m_rtiAmbassadorWrapper->createRtiAmbassador();
	}

	void FederateBase::createFederation()
	{
		m_rtiAmbassadorWrapper->createFederation();
	}

	void FederateBase::joinFederation()
	{
		m_rtiAmbassadorWrapper->joinFederation();
	}

	void FederateBase::synchronize( SynchPoint point )
	{
		m_rtiAmbassadorWrapper->synchronize( point );
	}

	inline void FederateBase::enableTimePolicy()
	{
		m_rtiAmbassadorWrapper->enableTimePolicy();
	}
	void FederateBase::publishAndSubscribe()
	{
		m_rtiAmbassadorWrapper->publishAndSubscribe();
	}
	void FederateBase::resign()
	{
		m_rtiAmbassadorWrapper->resign();
	}
	void FederateBase::advanceLogicalTime()
	{
		m_rtiAmbassadorWrapper->advanceLogicalTime();
	}
}