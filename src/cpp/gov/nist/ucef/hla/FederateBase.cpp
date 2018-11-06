#include "FederateBase.h"

#include <thread>
#include <chrono>

#include "gov/nist/ucef/hla/FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "gov/nist/ucef/util/UCEFException.h"

#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase() : m_rtiAmbassadorWrapper( new RTIAmbassadorWrapper() ),
	                               m_federateAmbassador( make_shared<FederateAmbassador>() ),
	                               m_ucefConfig( make_shared<FederateConfiguration>() )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
		// initialise rti ambassador
		connectToRti();

		// before creating the federation hook
		beforeFederationCreate();
		// create federation
		createFederation();

		// before joining the federation hook
		beforeFederateJoin();
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
			if( step() == false )
				break;
			advanceLogicalTime();
		}

		// before resigning the federation hook
		beforeReadyToResign();
		// now we are ready to resign from this federation
		synchronize( PointReadyToResign );
		// resign from this federation
		resign();
	}

	void FederateBase::connectToRti()
	{
		try
		{
			m_rtiAmbassadorWrapper->connect( m_federateAmbassador, m_ucefConfig);
			Logger::getInstance().log( m_ucefConfig->getFederateName() + " connected to RTI.", LevelInfo );
		} 
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::createFederation()
	{
		try
		{
			m_rtiAmbassadorWrapper->createFederation( m_ucefConfig );
			Logger::getInstance().log( "Federation " + m_ucefConfig->getFederationName() + " created.", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::joinFederation()
	{
		try
		{
			m_rtiAmbassadorWrapper->joinFederation( m_ucefConfig );
			Logger::getInstance().log( m_ucefConfig->getFederateName() + " joined the federation " +
			                           m_ucefConfig->getFederationName() + ".", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::enableTimePolicy()
	{
		Logger& logger = Logger::getInstance();

		if( m_ucefConfig->isTimeRegulated() )
		{
			logger.log( string("Inform time policy - regulated to RTI."), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->enableTimeRegulated( m_ucefConfig );
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !m_federateAmbassador->isRegulated() )
			{
				tick();
			}
			logger.log( string("RTI acknowledged time policy - regulated"), LevelInfo );
		}
		else if( m_ucefConfig->isTimeConstrained() )
		{
			logger.log( string("Inform time policy - constrain to RTI."), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->enableTimeConstrained( m_ucefConfig );
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !m_federateAmbassador->isConstrained() )
			{
				tick();
			}
			logger.log( string("RTI acknowledged time policy - constrain"), LevelInfo );
		}
	}

	void FederateBase::publishAndSubscribe()
	{
		m_rtiAmbassadorWrapper->publishAndSubscribe();
	}

	void FederateBase::setResign( bool resign )
	{
		m_resign = resign;
	}







	void FederateBase::synchronize( SynchPoint point )
	{
		m_rtiAmbassadorWrapper->synchronize( point );
	}


	void FederateBase::resign()
	{
		m_rtiAmbassadorWrapper->resign();
	}
	void FederateBase::advanceLogicalTime()
	{
		m_rtiAmbassadorWrapper->advanceLogicalTime();
	}

	void FederateBase::tick()
	{
		if( m_ucefConfig->isImmediate() )
		{
			this_thread::sleep_for( chrono::microseconds( 10 ) );
		}
		else
		{
			m_rtiAmbassadorWrapper->tickForCallBacks( 0.1, 1.0 );
		}
	}
}