#include "RTIAmbassadorWrapper.h"

#include <thread>
#include <chrono>

#include "gov/nist/ucef/hla/FederateAmbassador.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/UCEFException.h"

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

	ObjectClassHandle RTIAmbassadorWrapper::getClassHandle( const wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getObjectClassHandle( name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	AttributeHandle RTIAmbassadorWrapper::getAttributeHandle( ObjectClassHandle& classHandle, const wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getAttributeHandle( classHandle, name );
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

	std::shared_ptr<HLAObject> RTIAmbassadorWrapper::registerObject( const string& className,
	                                                                 ObjectClassHandle& classHandle)
	{
		shared_ptr<HLAObject> hlaObject = nullptr;
		try
		{
			ObjectInstanceHandle instanceHandle = m_rtiAmbassador->registerObjectInstance( classHandle );
			shared_ptr<ObjectInstanceHandle> instance;
			instance.reset( new ObjectInstanceHandle(instanceHandle) );
			hlaObject = make_shared<HLAObject>( className, instance );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
		return hlaObject;
	}

	InteractionClassHandle RTIAmbassadorWrapper::getInteractionHandle( const wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getInteractionClassHandle( name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	ParameterHandle RTIAmbassadorWrapper::getParameterHandle( InteractionClassHandle& interactionHandle,
	                                                          const wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getParameterHandle( interactionHandle, name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
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

	void RTIAmbassadorWrapper::updateObjectInstances( shared_ptr<HLAObject>& hlaObject,
	                                                  const ObjectCacheStoreByName& cacheStore )
	{
		Logger& logger = Logger::getInstance();

		auto classIt = cacheStore.find( hlaObject->getClassName() );
		if( classIt != cacheStore.end() )
		{
			AttributeHandleValueMap rtiAttributeMap;

			ObjectAttributes cachedAttributes = classIt->second->objectAttributes;

			shared_ptr<HLAObjectAttributes> atributeData = hlaObject->getAttributeDataStore();
			for( auto attribute : (*atributeData) )
			{
				auto it = cachedAttributes.find( attribute.first );
				if( it != cachedAttributes.end() )
				{
					shared_ptr<ObjectAttribute> cacheAttribute = it->second;
					if( cacheAttribute->publish )
					{
						if( cacheAttribute->handle->isValid() )
						{
							VariableData &val = attribute.second;
							VariableLengthData data( val.data.get(), val.size );
							rtiAttributeMap[*(cacheAttribute->handle)] = data;
							logger.log( "The new value of " + attribute.first + " in " + hlaObject->getClassName()
							            + " is ready to publish.", LevelDebug );
						}
						else
						{
							logger.log( "Attribute handler is not valid for " + attribute.first, LevelError );
						}
					}
					else
					{
						logger.log( attribute.first + " is not mentioned as a publishable item in SOM."
						            + ". hence ignore this attribute update request.", LevelError );
					}
				}
				else
				{
					logger.log( "Can't find " + attribute.first + " in " + hlaObject->getClassName()
					            + ". hence ignore this attribute update request.", LevelError );
				}
			}
			
			if( rtiAttributeMap.size() )
			{
				VariableLengthData tag( (void*)"", 4 );
				try
				{
					m_rtiAmbassador->updateAttributeValues( *(hlaObject->getInstanceHandle()), rtiAttributeMap, tag );
					logger.log( "Suucesfully published the updated attributes of " + hlaObject->getClassName()
					            + ".", LevelDebug );
				}
				catch( Exception& e )
				{
					throw UCEFException( ConversionHelper::ws2s(e.what()) );
				}
			}
			else
			{
				logger.log( "Can't find any attributes to publish in " + hlaObject->getClassName() + ".", LevelError );
			}
		}
		else
		{
			logger.log( "Can't find " + hlaObject->getClassName() + " in system's cache."
			            + ". Ignore this update request.", LevelError );
		}
	}

	void RTIAmbassadorWrapper::deleteObjectInstances( std::shared_ptr<HLAObject>& hlaObject )
	{
		try
		{
			VariableLengthData tag( (void*)"", 1 );
			m_rtiAmbassador->deleteObjectInstance( *(hlaObject->getInstanceHandle()), tag );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::resign()
	{
		try
		{
			m_rtiAmbassador->resignFederationExecution( NO_ACTION );
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
}