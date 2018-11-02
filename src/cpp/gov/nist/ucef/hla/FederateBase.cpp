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

	FederateBase::FederateBase() : m_rtiAmbassadorWrapper( new RTIAmbassadorWrapper() )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
		// initialise rti ambassador
		createRtiAmbassador();

		// before create federation hook
		beforeFederationCreate();
		// create federation
		createFederation();

		// before federation join hook
		beforeFederationJoin();
		// federation join
		joinFederation();

		// cache object, attribute, interaction, and parameter handles
		initialiseHandles();

		// enables time management policy for this federate
		enableTimePolicy();

		// now we are ready to run the federate
		synchronize( PointReadyToRun );
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

	void FederateBase::initialiseHandles()
	{
		m_rtiAmbassadorWrapper->initialiseHandles();
	}

	void FederateBase::synchronize( SynchPoint point )
	{
		m_rtiAmbassadorWrapper->synchronize( point );
	}

	inline void FederateBase::enableTimePolicy()
	{
		m_rtiAmbassadorWrapper->enableTimePolicy();
	}
}