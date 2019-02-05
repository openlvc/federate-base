#include <iostream>

#include "gov/nist/ucef/hla/ucef/UCEFNullFederate.h"
#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/HLAInteraction.h"


using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

class ExampleFederate : public UCEFNullFederate
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
					shared_ptr<HLAObject> obj = rtiAmbassadorWrapper->registerObjectInstance( className );
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
				rtiAmbassadorWrapper->deleteObjectInstance( object );
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
						rtiAmbassadorWrapper->sendInteraction( interaction );
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
						rtiAmbassadorWrapper->updateAttributeValues( val );
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

		virtual void receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
		                                          double federateTime ) override 
		{
			cout << "Received an object update " + hlaObject->getClassName();
			cout << " at " << to_string( federateTime ) << endl;
			cout << "Received attribute values are : " << endl;
			vector<string> attributes = hlaObject->getAttributeNames();
			for( string attribute : attributes )
			{
				cout << hlaObject->getAsString( attribute ) << endl;
			}
		}

		virtual void receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
		                                  double federateTime ) override
		{
			cout << "Received an object interaction callback " << hlaInt->getInteractionClassName();
			cout <<	" at " << to_string( federateTime ) << endl;
			cout << "Received parameter values are :" << endl;
			vector<string> parameters = hlaInt->getParameterNames();
			for( string parameter : parameters )
			{
				cout << hlaInt->getAsString( parameter ) << endl;
			}
		}

		virtual void receivedObjectDeletion( shared_ptr<const HLAObject> hlaObject ) override
		{
			cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
		}

		virtual void receivedSimStart( shared_ptr<const SimStart> hlaInt,
		                               double federateTime ) override
		{
			cout << "Received sim start interaction";
		}

		virtual void receivedSimEnd( shared_ptr<const SimEnd> hlaInt,
		                             double federateTime ) override
		{
			cout << "Received sim end interaction";
		}

		virtual void receivedSimPaused( shared_ptr<const SimPause> hlaInt,
		                                double federateTime) override
		{
			cout << "Received sim paused interaction";
		}

		virtual void receivedSimResumed( shared_ptr<const SimResume> hlaInt,
		                                 double federateTime ) override
		{
			cout << "Received sim resumed interaction";
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
		vector<shared_ptr<HLAObject>> federateObjects;
};

int main()
{
	IFederateBase *x = new ExampleFederate();
	shared_ptr<base::FederateConfiguration> federateConfig = x->getFederateConfiguration();
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