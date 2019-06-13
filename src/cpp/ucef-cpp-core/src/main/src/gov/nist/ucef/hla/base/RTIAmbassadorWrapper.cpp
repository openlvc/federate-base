#include "gov/nist/ucef/hla/base/RTIAmbassadorWrapper.h"

#include <thread>

#include "gov/nist/ucef/hla/base/FederateAmbassador.h"
#include "gov/nist/ucef/hla/base/FederateConfiguration.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

#include "RTI/Handle.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64Time.h"
#include "RTI/Typedefs.h"

using namespace rti1516e;
using namespace std;
using namespace base::util;

namespace base
{
	RTIAmbassadorWrapper::RTIAmbassadorWrapper()
	{
		RTIambassador* tmpAmbassador = RTIambassadorFactory().createRTIambassador().release();
		rtiAmbassador.reset(tmpAmbassador);
	}

	RTIAmbassadorWrapper::~RTIAmbassadorWrapper()
	{

	}

	void RTIAmbassadorWrapper::connect( const shared_ptr<FederateAmbassador>& federateAmbassador,
	                                    bool isImmediate )
	{

		//----------------------------------------
		//            Connect to the RTI
		//-----------------------------------------
		try
		{
			CallbackModel callBackModel = isImmediate ? HLA_IMMEDIATE : HLA_EVOKED;
			rtiAmbassador->connect( *federateAmbassador, callBackModel );
		}
		catch( AlreadyConnected& )
		{
			Logger::getInstance().log( string("Federate is already connected to the federation."), LevelWarn );
		}
		catch( Exception& e)
		{
			throw UCEFException( "Failed to connect due to " + ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::createFederation( const string& federationName, const vector<string>& fomPaths )
	{
		Logger& logger = Logger::getInstance();
		try
		{
			vector<wstring> wFomPaths;
			for( string path : fomPaths )
			{
				wFomPaths.push_back( ConversionHelper::s2ws( path ) );
			}

			rtiAmbassador->createFederationExecution( ConversionHelper::s2ws(federationName),
			                                            wFomPaths );

		}
		catch( FederationExecutionAlreadyExists& )
		{
			logger.log( string("Federation creation failed, federation "
			            + federationName + " already exist."), LevelWarn );
		}
		catch( Exception& e)
		{
			throw UCEFException( "Failed to create federation due to :" + ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::joinFederation( const string& federateName,
	                                           const string& federateType,
	                                           const string& federationName )
	{
		try
		{
			rtiAmbassador->joinFederationExecution( ConversionHelper::s2ws(federateName),
			                                          ConversionHelper::s2ws(federateType),
			                                          ConversionHelper::s2ws(federationName) );
		}
		catch( Exception& e)
		{
			throw UCEFException( federateName + " failed to join the federation '" + federationName +
			                     "' due to " + ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::enableTimeRegulation( const float lookAhead )
	{
		HLAfloat64Interval lookAheadInterval( lookAhead );
		try
		{
			rtiAmbassador->enableTimeRegulation( lookAheadInterval );
		}
		catch( TimeRegulationAlreadyEnabled& )
		{
			// we do not need to throw an exception for this
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::disableTimeRegulation()
	{
		try
		{
			rtiAmbassador->disableTimeRegulation();
		}
		catch( TimeRegulationIsNotEnabled& )
		{
			// we do not need to throw an exception for this
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::enableTimeConstrained()
	{
		try
		{
			rtiAmbassador->enableTimeConstrained();
		}
		catch( TimeConstrainedAlreadyEnabled& )
		{
			// we do not need to throw an exception for this
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::disableTimeConstrained()
	{
		try
		{
			rtiAmbassador->disableTimeConstrained();
		}
		catch( TimeConstrainedIsNotEnabled& )
		{
			// we do not need to throw an exception for this
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::publishObjectClassAttributes( ObjectClassHandle& classHandle,
	                                                         set<AttributeHandle>& pubAttributes)
	{
		if( pubAttributes.size() )
		{
			try
			{
				rtiAmbassador->publishObjectClassAttributes( classHandle, pubAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	void RTIAmbassadorWrapper::subscribeObjectClassAttributes( ObjectClassHandle& classHandle,
	                                                           set<AttributeHandle>& subAttributes)
	{
		if( subAttributes.size() )
		{
			try
			{
				rtiAmbassador->subscribeObjectClassAttributes( classHandle, subAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	void RTIAmbassadorWrapper::publishInteractionClass( InteractionClassHandle& interactionHandle )
	{
		try
		{
			rtiAmbassador->publishInteractionClass( interactionHandle );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::subscribeInteractionClasses( InteractionClassHandle& interactionHandle )
	{
		try
		{
			rtiAmbassador->subscribeInteractionClass( interactionHandle );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::registerFederationSynchronizationPoint( const string& synchPoint )
	{
		try
		{
			const VariableLengthData tag( (void*)"", 1 );
			rtiAmbassador->registerFederationSynchronizationPoint( ConversionHelper::s2ws(synchPoint), tag );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::synchronizationPointAchieved( const string& synchPoint )
	{
		try
		{
			rtiAmbassador->synchronizationPointAchieved( ConversionHelper::s2ws(synchPoint) );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::timeAdvanceRequest( const double requestedTime )
	{
		unique_ptr<HLAfloat64Time> newTime( new HLAfloat64Time(requestedTime) );
		try
		{
			rtiAmbassador->timeAdvanceRequest( *newTime );
		}
		catch( InTimeAdvancingState& )
		{
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::registerObjectInstance( shared_ptr<HLAObject> hlaObject )
	{
		Logger& logger = Logger::getInstance();
		string className = hlaObject->getClassName();

		if( className.empty() ) return;

		try
		{
			ObjectClassHandle objectHandle =
				rtiAmbassador->getObjectClassHandle( ConversionHelper::s2ws(className) );
			try
			{
				ObjectInstanceHandle instanceHandle = rtiAmbassador->registerObjectInstance( objectHandle );
				hlaObject->setInstanceId( instanceHandle.hash() );

				instanceStoreByHash[instanceHandle.hash()] = make_shared<ObjectInstanceHandle>( instanceHandle );
			}
			catch( Exception& )
			{
				logger.log( "Could not register an object instance for " + hlaObject->getClassName(), LevelError );
			}
		}
		catch( Exception& )
		{
			logger.log( "Can't find object class handle for" + hlaObject->getClassName() +
			            ". Ignoring this update request.", LevelError );
		}
	}

	shared_ptr<HLAObject> RTIAmbassadorWrapper::registerObjectInstance( const string& className )
	{
		Logger& logger = Logger::getInstance();

		shared_ptr<HLAObject> hlaObject = nullptr;
		try
		{
			ObjectClassHandle objectHandle =
				rtiAmbassador->getObjectClassHandle( ConversionHelper::s2ws( className ) );
			try
			{
				ObjectInstanceHandle instanceHandle = rtiAmbassador->registerObjectInstance( objectHandle );
				hlaObject = make_shared<HLAObject>( className, instanceHandle.hash() );

				instanceStoreByHash[instanceHandle.hash()] = make_shared<ObjectInstanceHandle>(instanceHandle);
			}
			catch( Exception& )
			{
				logger.log( "Could not register an object instance for " + hlaObject->getClassName(), LevelError );
			}
		}
		catch( Exception& )
		{
			logger.log( "Can't find object class handle for" + hlaObject->getClassName() +
			            ". Ignoring this update request.", LevelError );
		}
		return hlaObject;
	}

	void RTIAmbassadorWrapper::updateAttributeValues( shared_ptr<HLAObject>& hlaObject )
	{
		Logger& logger = Logger::getInstance();

		ObjectClassHandle objectHandle = getClassHandle( hlaObject->getClassName() );

		vector<string> attributeList = hlaObject->getAttributeNames();
		AttributeHandleValueMap rtiAttributeMap;
		for( string& attrbuteName : attributeList )
		{
			AttributeHandle handle = getAttributeHandle( objectHandle, attrbuteName );
			VariableData val = hlaObject->getRawValue( attrbuteName );
			if( val.data != nullptr )
			{
				VariableLengthData data( val.data.get(), val.size );
				rtiAttributeMap[handle] = data;
				logger.log( "The attribute value of " + attrbuteName + " in " + hlaObject->getClassName() + 
				            " is ready to publish.", LevelDebug );
			}
		}

		if( rtiAttributeMap.size() )
		{
			VariableLengthData tag( (void*)"", 1 );
			try
			{
				ObjectInstanceStoreByHash::iterator it = instanceStoreByHash.find( hlaObject->getInstanceId() );
				if( it != instanceStoreByHash.end() )
				{
					auto handle = it->second;
					rtiAmbassador->updateAttributeValues( *handle, rtiAttributeMap, tag );
					logger.log( "Successfully published the updated attributes of " + hlaObject->getClassName() +
					            ".", LevelDebug );
				}
				else
				{
					logger.log( "Cannot publish attributes of " + hlaObject->getClassName() + ". Instance id : " +
					            to_string( hlaObject->getInstanceId() ) + " not found.", LevelWarn );
				}
			}
			catch( Exception& e )
			{
				logger.log( "Failed to  publish attributes of " + hlaObject->getClassName() +
				            ConversionHelper::ws2s( e.what() ), LevelError );
			}
		}
		else
		{
			logger.log( "Can't find any attributes to publish in " + hlaObject->getClassName() + ".",
			            LevelError );
		}
	}

	void RTIAmbassadorWrapper::sendInteraction( shared_ptr<HLAInteraction>& hlaInteraction )
	{
		Logger& logger = Logger::getInstance();

		InteractionClassHandle interactionHandle = 
			getInteractionHandle( hlaInteraction->getInteractionClassName() );
		vector<string> paramList = hlaInteraction->getParameterNames();
		ParameterHandleValueMap rtiParameterMap;
		for( string& param : paramList )
		{
			ParameterHandle handle = getParameterHandle( interactionHandle, param );
			VariableData val = hlaInteraction->getRawValue( param );
			if( val.data != nullptr )
			{
				VariableLengthData data( val.data.get(), val.size );
				rtiParameterMap[handle] = data;
				logger.log( "The parameter value of " + param + " in " + hlaInteraction->getInteractionClassName() +
				            " is ready to publish.", LevelDebug );
			}
		}

		VariableLengthData tag( (void*)"", 4 );
		try
		{
			rtiAmbassador->sendInteraction( interactionHandle, rtiParameterMap, tag );
			logger.log( "Successfully published an interaction named " +
			            hlaInteraction->getInteractionClassName() + ".", LevelDebug );
		}
		catch( Exception& e )
		{
			logger.log( "Send interaction failed : " + ConversionHelper::ws2s(e.what()), LevelError );
		}
	}

	void RTIAmbassadorWrapper::deleteObjectInstance( shared_ptr<HLAObject>& hlaObject )
	{

		Logger& logger = Logger::getInstance();
		ObjectInstanceStoreByHash::iterator it = instanceStoreByHash.find( hlaObject->getInstanceId() );
		if( it != instanceStoreByHash.end() )
		{
			VariableLengthData tag( (void*)"", 1 );
			auto handle = it->second;
			instanceStoreByHash.erase( hlaObject->getInstanceId() );
			try
			{
				rtiAmbassador->deleteObjectInstance( *handle, tag );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
		else
		{
			logger.log( "Cannot delete the given instance of " + hlaObject->getClassName() + ". Instance id : " +
				        to_string(hlaObject->getInstanceId()) + " not found.", LevelWarn );
		}

	}

	void RTIAmbassadorWrapper::resign()
	{
		try
		{
			rtiAmbassador->resignFederationExecution( DELETE_OBJECTS_THEN_DIVEST );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::evokeMultipleCallbacks( double min, double max )
	{
		rtiAmbassador->evokeMultipleCallbacks( min, max );
	}

	ObjectClassHandle RTIAmbassadorWrapper::getClassHandle( const string& name )
	{
		Logger& logger = Logger::getInstance();
		ObjectClassHandle classHandle = {};
		try
		{
			classHandle =  rtiAmbassador->getObjectClassHandle( ConversionHelper::s2ws(name) );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid class handle for " + name, LevelError );
		}
		return classHandle;
	}

	AttributeHandle RTIAmbassadorWrapper::getAttributeHandle( const ObjectClassHandle& classHandle,
	                                                          const string& name )
	{
		Logger& logger = Logger::getInstance();
		AttributeHandle attHandle = {};
		try
		{
			attHandle = rtiAmbassador->getAttributeHandle( classHandle, ConversionHelper::s2ws(name) );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid attribute handle for " + name, LevelError );
		}
		return attHandle;
	}

	string RTIAmbassadorWrapper::getAttributeName( const ObjectClassHandle& classHandle,
	                                               const AttributeHandle& attributeHandle )
	{
		Logger& logger = Logger::getInstance();
		wstring attName = L"";
		try
		{
			attName = rtiAmbassador->getAttributeName( classHandle, attributeHandle );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid name for the given attribute handle with id " +
			            to_string(attributeHandle.hash()), LevelError );
		}
		return ConversionHelper::ws2s(attName);
	}


	InteractionClassHandle RTIAmbassadorWrapper::getInteractionHandle( const string& name )
	{
		Logger& logger = Logger::getInstance();
		InteractionClassHandle interactionhandle = {};
		try
		{
			interactionhandle =  rtiAmbassador->getInteractionClassHandle(  ConversionHelper::s2ws(name) );
		}
		catch( Exception& )
		{
			
			logger.log( "Could not find a valid interaction class handle for " + name, LevelError );
		}
		return interactionhandle;
	}

	ParameterHandle RTIAmbassadorWrapper::getParameterHandle( const InteractionClassHandle& interactionHandle,
	                                                          const string& name )
	{
		Logger& logger = Logger::getInstance();
		ParameterHandle paramHandle = {};
		try
		{
			paramHandle = rtiAmbassador->getParameterHandle( interactionHandle, ConversionHelper::s2ws(name) );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid parameter handle for parameter " + name, LevelWarn );
		}
		return paramHandle;
	}

	string RTIAmbassadorWrapper::getParameterName( const InteractionClassHandle& interactionHandle,
	                                                const ParameterHandle& parameterHandle )
	{
		Logger& logger = Logger::getInstance();
		wstring paramName = L"";
		try
		{
			paramName = rtiAmbassador->getParameterName( interactionHandle, parameterHandle );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid name for the given parameter handle with id " +
			            to_string(parameterHandle.hash()), LevelError );
		}
		return ConversionHelper::ws2s(paramName);
	}
}
