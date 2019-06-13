/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include <memory>
#include <set>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/types.h"

namespace rti1516e
{
	class AttributeHandle;
	class InteractionClassHandle;
	class ObjectClassHandle;
	class ParameterHandle;
	class RTIambassador;
	class VariableLengthData;
}

namespace base
{
	class FederateAmbassador;
	namespace util
	{
		class FederateConfiguration;
	}

	/**
	 * The {@link RTIAmbassadorWrapper} provides a simplified interface to
	 * communicate with the underlying RTI implementation.
	 * <p/>
	 * To publish an object class use the following steps.
	 * <ul>
	 *  <li> Register a class instance using {@link #registerObjectInstance(const std::string& )}
	 *  <li> Populate the returned HLA object instance with attribute data and values {@link HLAObject#setValue()}
	 *  <li> Send data out using {@link #updateAttributeValues( std::shared_ptr<HLAObject>& )}
	 * </ul>
	 * To publish an interaction class use the following steps.
	 * <ul>
	 *  <li> Create an instance of {@link HLAInteraction} class
	 *  <li> Populate the created instance with parameter data {@link HLAInteraction#setValue()}
	 *  <li> Send data out using {@link #sendInteraction( std::shared_ptr<HLAInteraction>& )}
	 * </ul>
	 * To delete an instance from the RTI simply call
	 * <ul>
	 *  <li> {@link #deleteObjectInstance( std::shared_ptr<HLAObject>& )} with a valid instance of a {@link HLAObject}
	 * </ul>
	 */
	class UCEF_API RTIAmbassadorWrapper
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			RTIAmbassadorWrapper();
			virtual ~RTIAmbassadorWrapper();
			RTIAmbassadorWrapper( const RTIAmbassadorWrapper& ) = delete;

		public:
			//----------------------------------------------------------
			//             Instance methods
			//----------------------------------------------------------

			/**
			 * Requests to connect to the underlying RTI.
			 *
			 * @param federateAmbassador FederateAmbassador instance
			 * @param isImmediate true if HLA_IMMEDIATE callback mode required,
			 *        false if HLA_EVOKED callback mode required
			 */
			void connect( const std::shared_ptr<FederateAmbassador>& federateAmbassador,
			              const bool isImmediate );

			/**
			 * Attempts to create a federation.
			 *
			 * @param federationName name of the federation
			 * @param fomPaths file paths to the federation object model
			 */
			void createFederation( const std::string& federationName, const std::vector<std::string>& fomPaths );

			/**
			 * Join an existing federation.
			 *
			 * @param federateName the name of the federate
			 * @param federateType the type of the federate
			 * @param federationName the name of the joining federation
			 */
			void joinFederation( const std::string& federateName,
			                     const std::string& federateType,
			                     const std::string& federationName );

			/**
			 * Informs RTI that this federate is time regulating federate.
			 * <p/>
			 * A federate that declares itself to be "regulating" is capable of
			 * generating time-stamp-ordered (TSO) events.
			 * <p/>
			 * The lookahead value, represents a contract between the regulating federate
			 * and the federation. It establishes the earliest possible TSO event
			 * the federate can generate relative to the current time.
			 *
			 * @param lookAhead lookahead value of this federate
			 */
			void enableTimeRegulation( const float lookAhead );

			/**
			 * Informs RTI that this federate is no longer a time regulating federate.
			 */
			void disableTimeRegulation();

			/**
			 * Informs RTI that this federate is time constrained federate.
			 * <p/>
			 * A federate that declares itself to be "constrained" is capable of
			 * receiving TSO events.
			 */
			void enableTimeConstrained();

			/**
			 * Informs RTI that this federate is no longer a time constrained federate.
			 */
			void disableTimeConstrained();

			/**
			 * Informs RTI about class attributes of a particular object class that is going to be
			 * published by this federate.
			 *
			 * @param classHandle a valid handler to the class who owns the publishing attributes
			 * @param pubAttributes attributes that are going to be published by this federate
			 */
			void publishObjectClassAttributes( rti1516e::ObjectClassHandle& classHandle,
			                                   std::set<rti1516e::AttributeHandle>& pubAttributes);

			/**
			 * Informs RTI about class attributes of a particular object class that is going to be
			 * subscribed by this federate.
			 *
			 * @param classHandle a valid handler to the class who owns the subscribing attributes
			 * @param subAttributes attributes that are going to be subscribed by this federate
			 */
			void subscribeObjectClassAttributes( rti1516e::ObjectClassHandle& classHandle,
			                                     std::set<rti1516e::AttributeHandle>& subAttributes );

			/**
			 * Informs RTI about an interaction class that is going to be published by this federate.
			 *
			 * @param interactionHandle a valid handler to the interaction class that is going
			 *        to be published by this federate
			 */
			void publishInteractionClass( rti1516e::InteractionClassHandle& interactionHandle );

			/**
			 * Informs RTI about an interaction class that is going to be subscribed by this federate.
			 *
			 * @param interactionHandle a valid handler to the class that is going to be subscribed
			 *        by this federate
			 */
			void subscribeInteractionClasses( rti1516e::InteractionClassHandle& interactionHandle );

			/**
			 * Announces a synchronization point to the federation.
			 * <p/>
			 * By announcing a synchronization point a federate requests the federation or the
			 * participating federates to synch into the announced point.
			 *
			 * @param synchPoint the name of the announcing synchronization point
			 */
			void registerFederationSynchronizationPoint( const std::string& synchPoint );

			/**
			 * Announces to the federation that this federate has achived the given synchronization point
			 *
			 * @param synchPoint the name of the synchronization point achieved by this federate
			 */
			void synchronizationPointAchieved( const std::string& synchPoint );

			/**
			 * Requests a time advancement from RTI
			 *
			 * @param requestedTime time requested to advance
			 */
			void timeAdvanceRequest( const double requestedTime );

			/**
			 * Registers an instance of a given class object with RTI.
			 * <p/>
			 * This method must be called before sending updates to the federation using
			 * {@link #updateAttributeValues(std::shared_ptr<HLAObject>&)}. Further, attributes and
			 * their values can be added to this object by calling {@link HLAOBject#setValue}.
			 *
			 * @param hlaObject An HLA object intialised with fully qualified class name
			 *
			 * @see #updateObjectInstance()
			 * @see #deleteObjectInstances()
			 */
			void registerObjectInstance( std::shared_ptr<HLAObject> hlaObject );

			/**
			 * Registers an instance of a given class object with RTI.
			 * <p/>
			 * This method must be called before sending updates to the federation using
			 * {@link #updateAttributeValues(std::shared_ptr<HLAObject>&)}. Further, attributes and
			 * their values can be added to this object by calling {@link HLAOBject#setValue}.
			 *
			 * @param className the fully qualified name of the class that needs to be
			 *        registered with RTI in order to create an instance
			 * @return an abstract representation of the registered RTI object instance
			 *
			 * @see #updateObjectInstance()
			 * @see #deleteObjectInstances()
			 */
			std::shared_ptr<HLAObject> registerObjectInstance( const std::string& className );

			/**
			 * Sends an object class update to the federation
			 *
			 * @param hlaObject object that holds the values of the attributes
			 *        that need to be published by this federate
			 */
			void updateAttributeValues( std::shared_ptr<HLAObject>& hlaObject );

			/**
			 * Sends an interaction to the federation
			 *
			 * @param hlaInteraction object that holds the values of the interaction
			 *        parameters that need to be published by this federate
			 */
			void sendInteraction( std::shared_ptr<HLAInteraction>& hlaInteraction );

			/**
			 * Requests to delete a registered object class instance from RTI
			 * <p/>
			 * Federate can only deletes an object instance only if it owns the given instance.
			 *
			 * @param hlaObject object instance that needs to be deleted from RTI
			 */
			void deleteObjectInstance( std::shared_ptr<HLAObject>& hlaObject );

			/**
			 * Resigns from federation execution
			 */
			void resign();

			/**
			 * Ticks RTI for callbacks
			 * <p/>
			 * Explicit ticking of RTI is required when callback mode is set to
			 * HLA_EVOKED.
			 *
			 *  @see #connect()
			 */
			void evokeMultipleCallbacks( double min, double max );

			/**
			 * Returns rti class handle to the given object class
			 * <p/>
			 * If the class handle could not be found an empty class handle is returned.
			 *
			 * @param name the fully qualified name of an object class
			 * @return rti class handle to the given object class
			 */
			rti1516e::ObjectClassHandle getClassHandle( const std::string& name );

			/**
			 * Returns rti attrbiute handle to the given object class
			 * <p/>
			 * If attrbiute handle could not be found an empty attrbiute handle is returned.
			 *
			 * @param classHandle a valid class handle to an object class
			 * @param name the fully qualified name of an attribute in that class
			 * @return rti attrbiute handle to the given class attribute
			 */
			rti1516e::AttributeHandle getAttributeHandle( const rti1516e::ObjectClassHandle& classHandle,
			                                              const std::string& name );

			/**
			 * Returns the fully qualified name of an attrbiute given a valid class handle and
			 * an attrbiutde handle
			 * <p/>
			 * If the handles are invalid an empty string is returned.
			 *
			 * @param classHandle a valid class handle to an object class
			 * @param attributeHandle a valid attribute handle to an attribute in that object class
			 * @return the fully qualified name of an attrbiute
			 */
			std::string getAttributeName( const rti1516e::ObjectClassHandle& classHandle,
			                              const rti1516e::AttributeHandle& attributeHandle );

			/**
			 * Returns rti interaction handle to the given interaction class
			 * <p/>
			 * If the interaction handle could not be found an empty interaction handle is returned.
			 *
			 * @param name the fully qualified name of an interaction class
			 * @return rti interaction class handle to the given interaction class
			 */
			rti1516e::InteractionClassHandle getInteractionHandle( const std::string& name );

			/**
			 * Returns rti parameter handle to the given interaction class.
			 * <p/>
			 * If parameter handle could not be found an empty parameter handle is returned.
			 *
			 * @param interactionHandle a valid interaction handle to an interaction class
			 * @param name the fully qualified name of a parameter in that class
			 * @return rti parameter handle to the given interaction parameter
			 */
			rti1516e::ParameterHandle getParameterHandle( const rti1516e::InteractionClassHandle& interactionHandle,
			                                              const std::string& name );

			/**
			 * Returns the fully qualified name of a parameter given a valid interaction and
			 * a parameter handle.
			 * <p/>
			 * If the handles are invalid an empty string is returned.
			 *
			 * @param interactionHandle a valid interaction handle to an interaction class
			 * @param parameterHandle a valid parameter handle to a parameter in that interaction class
			 * @return the fully qualified name of a parameter
			 */
			std::string getParameterName( const rti1516e::InteractionClassHandle& interactionHandle,
			                              const rti1516e::ParameterHandle& parameterHandle );
	private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			std::unique_ptr<rti1516e::RTIambassador> rtiAmbassador;
			// to resolve object instance handle from the instance hash of an outgoing object
			base::ObjectInstanceStoreByHash instanceStoreByHash;
	};
}
