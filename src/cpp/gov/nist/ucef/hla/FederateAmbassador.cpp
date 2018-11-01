#include "FederateAmbassador.h"

#include <mutex>

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

	void FederateAmbassador::announceSynchronizationPoint( const wstring& label,
	                                                       const VariableLengthData& tag )
	                                                            throw(FederateInternalError)
	{
		lock_guard<mutex> lck (synchAnnounceLock);
		announcedSynchPoint = label;
	}


	void FederateAmbassador::federationSynchronized( const std::wstring& label,
	                                                 const FederateHandleSet& failedSet )
	                                                             throw( FederateInternalError )
	{
		lock_guard<mutex> lck ( synchAchievedLock );
		synchAchievedPoint = label;
	}

	wstring FederateAmbassador::getAnnouncedSynchPoint()
	{
		lock_guard<mutex> lck (synchAnnounceLock);
		return announcedSynchPoint;
	}

	wstring FederateAmbassador::getAchievedSynchPoint()
	{
		lock_guard<mutex> lck ( synchAchievedLock );
		return synchAchievedPoint;
	}

}

