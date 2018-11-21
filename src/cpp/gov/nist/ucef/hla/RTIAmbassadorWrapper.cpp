#include "RTIAmbassadorWrapper.h"

#include <thread>

#include "gov/nist/ucef/hla/FederateAmbassador.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/UCEFException.h"

#include "RTI/Handle.h"
#include "RTI/Handle.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64Time.h"
#include "RTI/Typedefs.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	RTIAmbassadorWrapper::RTIAmbassadorWrapper()
	{
		RTIambassador* tmpAmbassador = RTIambassadorFactory().createRTIambassador().release();
		m_rtiAmbassador.reset(tmpAmbassador);
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
			m_rtiAmbassador->connect( *federateAmbassador, callBackModel );
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

	void RTIAmbassadorWrapper::createFederation( const string& federationName, const vector<wstring>& fomPaths )
	{
		Logger& logger = Logger::getInstance();
		try
		{
			m_rtiAmbassador->createFederationExecution( ConversionHelper::s2ws(federationName),
			                                            fomPaths );

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
			m_rtiAmbassador->joinFederationExecution( ConversionHelper::s2ws(federateName),
			                                          ConversionHelper::s2ws(federateType),
			                                          ConversionHelper::s2ws(federationName) );
		}
		catch( Exception& e)
		{
			throw UCEFException( federateName + " failed to join the federation : " + federationName +
			                     "due to :" + ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::enableTimeRegulated( const float lookAhead )
	{
		HLAfloat64Interval lookAheadInterval( lookAhead );
		try
		{
			m_rtiAmbassador->enableTimeRegulation( lookAheadInterval );
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
			m_rtiAmbassador->enableTimeConstrained();
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}	

	void RTIAmbassadorWrapper::publishSubscribeObjectClassAttributes( ObjectClassHandle& classHandle,
	                                                                  set<AttributeHandle>& pubAttributes,
	                                                                  set<AttributeHandle>& subAttributes )
	{
		if( pubAttributes.size() )
		{
			try
			{
				m_rtiAmbassador->publishObjectClassAttributes( classHandle, pubAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
		if( subAttributes.size() )
		{
			try
			{
				m_rtiAmbassador->subscribeObjectClassAttributes( classHandle, subAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	std::shared_ptr<HLAObject> RTIAmbassadorWrapper::registerObjectInstance( const string& className )
	{
		Logger& logger = Logger::getInstance();

		shared_ptr<HLAObject> hlaObject = nullptr;
		try
		{
			ObjectClassHandle objectHandle =
				m_rtiAmbassador->getObjectClassHandle( ConversionHelper::s2ws( className ) );
			try
			{
				ObjectInstanceHandle instanceHandle = m_rtiAmbassador->registerObjectInstance( objectHandle );
				hlaObject = make_shared<HLAObject>( className, instanceHandle.hash() );
				m_instanceStoreByHash[instanceHandle.hash()] = instanceHandle;
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

	void RTIAmbassadorWrapper::publishSubscribeInteractionClass( InteractionClassHandle& interactionHandle,
	                                                             const bool publish,
	                                                             const bool subscribe )
	{
		if( publish )
		{
			try
			{
				m_rtiAmbassador->publishInteractionClass( interactionHandle );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
		if( subscribe )
		{
			try
			{
				m_rtiAmbassador->subscribeInteractionClass( interactionHandle );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	void RTIAmbassadorWrapper::announceSynchronizationPoint( const wstring& synchPoint )
	{
		try
		{
			const VariableLengthData tag( (void*)"", 1 );
			m_rtiAmbassador->registerFederationSynchronizationPoint( synchPoint, tag );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::achieveSynchronizationPoint( const wstring& synchPoint )
	{
		try
		{
			m_rtiAmbassador->synchronizationPointAchieved( synchPoint );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::advanceLogicalTime( const double requestedTime )
	{
		unique_ptr<HLAfloat64Time> newTime( new HLAfloat64Time(requestedTime) );
		try
		{
			m_rtiAmbassador->timeAdvanceRequest( *newTime );
		}
		catch( InTimeAdvancingState& )
		{
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::updateObjectInstance( shared_ptr<HLAObject>& hlaObject )
	{
		Logger& logger = Logger::getInstance();

		ObjectClassHandle objectHandle = getClassHandle( ConversionHelper::s2ws( hlaObject->getClassName() ) );

		vector<string> attributeList = hlaObject->getAttributeNames();
		AttributeHandleValueMap rtiAttributeMap;
		for( string& attrbuteName : attributeList )
		{
			AttributeHandle handle = getAttributeHandle( objectHandle, ConversionHelper::s2ws( attrbuteName ) );
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
				ObjectInstanceStoreByHash::iterator it = m_instanceStoreByHash.find( hlaObject->getInstanceId() );
				if( it != m_instanceStoreByHash.end() )
				{
					ObjectInstanceHandle handle = it->second;
					m_rtiAmbassador->updateAttributeValues( handle, rtiAttributeMap, tag );
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
			getInteractionHandle( ConversionHelper::s2ws(hlaInteraction->getInteractionClassName()) );
		vector<string> paramList = hlaInteraction->getParameterNames();
		ParameterHandleValueMap rtiParameterMap;
		for( string& param : paramList )
		{
			ParameterHandle handle =
				getParameterHandle( interactionHandle, ConversionHelper::s2ws( param ) );
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
			m_rtiAmbassador->sendInteraction( interactionHandle, rtiParameterMap, tag );
			logger.log( "Successfully published an interaction named " +
			            hlaInteraction->getInteractionClassName() + ".", LevelDebug );
		}
		catch( Exception& e )
		{
			logger.log( "Send interaction failed : " + ConversionHelper::ws2s(e.what()), LevelError );
		}
	}

	void RTIAmbassadorWrapper::deleteObjectInstances( std::shared_ptr<HLAObject>& hlaObject )
	{

		Logger& logger = Logger::getInstance();
		ObjectInstanceStoreByHash::iterator it = m_instanceStoreByHash.find( hlaObject->getInstanceId() );
		if( it != m_instanceStoreByHash.end() )
		{
			VariableLengthData tag( (void*)"", 1 );
			ObjectInstanceHandle handle = it->second;
			m_instanceStoreByHash.erase( hlaObject->getInstanceId() );
			try
			{
				m_rtiAmbassador->deleteObjectInstance( handle, tag );
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
			m_rtiAmbassador->resignFederationExecution( DELETE_OBJECTS_THEN_DIVEST );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::tickForCallBacks( double min, double max )
	{
		m_rtiAmbassador->evokeMultipleCallbacks( min, max );
	}

	ObjectClassHandle RTIAmbassadorWrapper::getClassHandle( const wstring& name )
	{
		Logger& logger = Logger::getInstance();
		ObjectClassHandle classHandle = {};
		try
		{
			classHandle =  m_rtiAmbassador->getObjectClassHandle( name );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid class handle for " +
			            ConversionHelper::ws2s(name), LevelError );
		}
		return classHandle;
	}

	AttributeHandle RTIAmbassadorWrapper::getAttributeHandle( const ObjectClassHandle& classHandle,
	                                                          const wstring& name )
	{
		Logger& logger = Logger::getInstance();
		AttributeHandle attHandle = {};
		try
		{
			attHandle = m_rtiAmbassador->getAttributeHandle( classHandle, name );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid attribute handle for " +
			            ConversionHelper::ws2s(name), LevelError );
		}
		return attHandle;
	}

	wstring RTIAmbassadorWrapper::getAttributeName( const ObjectClassHandle& classHandle,
	                                                const AttributeHandle& attributeHandle )
	{
		Logger& logger = Logger::getInstance();
		wstring attName = L"";
		try
		{
			attName = m_rtiAmbassador->getAttributeName( classHandle, attributeHandle );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid name for the given attribute handle with id " +
			            to_string(attributeHandle.hash()), LevelError );
		}
		return attName;
	}


	InteractionClassHandle RTIAmbassadorWrapper::getInteractionHandle( const wstring& name )
	{
		Logger& logger = Logger::getInstance();
		InteractionClassHandle interactionhandle = {};
		try
		{
			interactionhandle =  m_rtiAmbassador->getInteractionClassHandle( name );
		}
		catch( Exception& )
		{
			
			logger.log( "Could not find a valid interaction class handle for " +
			             ConversionHelper::ws2s(name), LevelError );
		}
		return interactionhandle;
	}

	ParameterHandle RTIAmbassadorWrapper::getParameterHandle( const InteractionClassHandle& interactionHandle,
	                                                          const wstring& name )
	{
		Logger& logger = Logger::getInstance();
		ParameterHandle paramHandle = {};
		try
		{
			paramHandle = m_rtiAmbassador->getParameterHandle( interactionHandle, name );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid parameter handle for parameter " +
			            ConversionHelper::ws2s(name), LevelWarn );
		}
		return paramHandle;
	}

	wstring RTIAmbassadorWrapper::getParameterName( const InteractionClassHandle& interactionHandle,
	                                                const ParameterHandle& parameterHandle )
	{
		Logger& logger = Logger::getInstance();
		wstring paramName = L"";
		try
		{
			paramName = m_rtiAmbassador->getParameterName( interactionHandle, parameterHandle );
		}
		catch( Exception& )
		{
			logger.log( "Could not find a valid name for the given parameter handle with id " +
			            to_string(parameterHandle.hash()), LevelError );
		}
		return paramName;
	}
}