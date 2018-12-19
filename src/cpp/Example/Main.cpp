#include "gov/nist/ucef/hla/FederateBase.h"
#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/UCEFException.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "gov/nist/ucef/hla/HLAInteraction.h"
#include <iostream>

using namespace std;
using namespace ucef;
using namespace ucef::util;

class ExampleFederate : public FederateBase
{
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		ExampleFederate() = default;
		virtual ~ExampleFederate() = default;

		//----------------------------------------------------------
		//          Lifecycle hooks implementation
		//----------------------------------------------------------
		void beforeReadyToPopulate() override
		{
			cout << "\'Ready to populate\' hook" << endl;
			pressEnterToContinue();
		}

		void beforeReadyToRun() override
		{
			cout << "\'Ready to run\' hook" << endl;
			cout << "Creating object instances for publishing" << endl;

			vector<string> publishClassNames =  getFederateConfiguration()->getClassNamesPublished();
			try
			{
				for(auto className : publishClassNames )
				{
					shared_ptr<HLAObject> obj = m_rtiAmbassadorWrapper->registerObjectInstance( className );
					if( obj )
					{
						federateObjects.push_back( obj );
					}
				}
			}
			catch( UCEFException& e )
			{
				cout << e.what() << endl;
			}
			cout << "Object instances creation completed, moving on." << endl;
		}

		void beforeFirstStep() override
		{
			cout << "\'Before first step\' hook" << endl;
			pressEnterToContinue();
		}

		void beforeReadyToResign() override
		{
			cout << "\'Before ready to resign\' hook" << endl;
			pressEnterToContinue();
		}

		virtual void beforeExit() override 
		{
			cout << "\'Before exit\' hook" << endl;
			pressEnterToContinue();
			cout << "Request to delete federate objects" << endl;

			for( auto object : federateObjects )
			{
				m_rtiAmbassadorWrapper->deleteObjectInstance( object );
			}
		}

		virtual bool step( double federateTime ) override
		{
			static int i = 0;
			i++;
			if( i > 100 ) return false;
			else
			{
				if( i % 2 == 0 )
				{
					// send object interactions
					vector<string> publishInteractionNames = getFederateConfiguration()->getInteractionNamesPublished();
					for( auto interactionName : publishInteractionNames )
					{
						shared_ptr<HLAInteraction> interaction = make_shared<HLAInteraction>( interactionName );

						vector<string> parameterNames = getFederateConfiguration()->getParameterNames( interactionName );
						for( auto name : parameterNames )
						{
							interaction->setValue( name, "parameter " + name + " : " + to_string( i ) );
						}
						cout << "Sending an interaction " + interactionName << endl;
						m_rtiAmbassadorWrapper->sendInteraction( interaction );
					}
				}
				else
				{
					// send object class updates
					for( auto val : federateObjects )
					{
						// clear current attribute data
						val->clear();
						vector<string> attributeNames = getFederateConfiguration()->getAttributeNamesPublished( val->getClassName() );
						// set new attribute values
						for( auto attributeName : attributeNames )
						{
							val->setValue( attributeName, "attribute " + attributeName + " : " + to_string( i ) );
						}
						cout << "Sending an object update " + val->getClassName() << endl;
						m_rtiAmbassadorWrapper->updateAttributeValues( val );
					}
				}
			}
			return true;
		}

		//----------------------------------------------------------
		//         Implement Callback Methods
		//----------------------------------------------------------
		virtual void receivedObjectRegistration( shared_ptr<const HLAObject> hlaObject,
		                                         double federateTime ) override
		{
			cout << "Received an object registration callback " << hlaObject->getClassName() << endl;
		}

		virtual void receivedAttributeReflection( std::shared_ptr<const HLAObject> hlaObject,
		                                          double federateTime ) override 
		{
			cout << "Received an object update " + hlaObject->getClassName();
			cout << " at " << to_string( federateTime ) << endl;
			cout << "Received attribute values are : " << endl;
			std::vector<std::string> attributes = hlaObject->getAttributeNames();
			for( string attribute : attributes )
			{
				cout << hlaObject->getAsString( attribute ) << endl;
			}
		}

		virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInt,
		                                  double federateTime ) override
		{
			cout << "Received an object interaction callback " << hlaInt->getInteractionClassName();
			cout <<	" at " << to_string( federateTime ) << endl;
			cout << "Received parameter values are :" << endl;
			std::vector<std::string> parameters = hlaInt->getParameterNames();
			for( string parameter : parameters )
			{
				cout << hlaInt->getAsString( parameter ) << endl;
			}
		}

		virtual void receivedObjectDeletion( std::shared_ptr<const HLAObject> hlaObject ) override
		{
			cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
		}

	private:
		void pressEnterToContinue()
		{
			do 
			{
				cout << '\n' << "Press ENTER to continue...";
			} while (cin.get() != '\n');
		}
	private:
		std::vector<std::shared_ptr<HLAObject>> federateObjects;
};

int main()
{
	IFederateBase *x = new ExampleFederate();
	std::shared_ptr<util::FederateConfiguration> federateConfig = x->getFederateConfiguration();
	federateConfig->setFederationName( string("MyTestFeeration") );
	federateConfig->setFederateName( string("MyTestFederate") + to_string(rand()) );
	federateConfig->addFomPath( string("restaurant/RestaurantFood.xml") );
	federateConfig->addFomPath( string("restaurant/RestaurantDrinks.xml") );
	federateConfig->addFomPath( string("restaurant/RestaurantProcesses.xml") );
	federateConfig->addSomPath( string("restaurant/RestaurantProcesses.xml") );
	try
	{
		x->runFederate();
	}
	catch( UCEFException& e )
	{
		cout << e.what() << endl;
	}
	delete x;
}