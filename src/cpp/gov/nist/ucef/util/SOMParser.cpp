#include "SOMParser.h"

#include <iostream>

#include "gov/nist/ucef/util/Logger.h"

using namespace std;
using namespace tinyxml2;

namespace ucef
{
	namespace util
	{
		vector<shared_ptr<ObjectClass>> SOMParser::getObjectClasses( const string& somFilePath )
		{
			Logger &logger = Logger::getInstance();

			vector<shared_ptr<ObjectClass>> SomObjects;
			vector<shared_ptr<ObjectAttribute>> SomAttributes;

			logger.log( "Trying to load SOM file to extract objects " + somFilePath, LevelInfo );

			XMLDocument doc;
			XMLError xmlError = doc.LoadFile( somFilePath.c_str() );
			if( xmlError == XML_SUCCESS )
			{
				logger.log( "SOM loaded succefully " + somFilePath, LevelInfo );
	
				XMLElement* root = doc.FirstChildElement( "objectModel" );
				if( root )
				{
					XMLElement* objectsElement = root->FirstChildElement( "objects" );
					SOMParser::traverseObjectClasses( "", SomAttributes, objectsElement, SomObjects );
				}
				else
				{
					logger.log( string("Could not locate objectModel in given SOM file"), LevelError );
				}
			}
			else
			{
				logger.log( "Could not Load SOM file in " + somFilePath, LevelError );
			}
			return SomObjects;
		}

		vector<shared_ptr<InteractionClass>> SOMParser::getInteractionClasses( const string& somFilePath )
		{
			Logger &logger = Logger::getInstance();

			vector<shared_ptr<InteractionClass>> SomInteractions;
			vector<shared_ptr<InteractionParameter>> SomInteractionParams;

			logger.log( "Trying to load SOM file to extract interactions  " + somFilePath, LevelInfo );

			XMLDocument doc;
			XMLError xmlError = doc.LoadFile( somFilePath.c_str() );
			if( xmlError == XML_SUCCESS )
			{
				logger.log( "SOM loaded succefully " + somFilePath, LevelInfo );
	
				XMLElement* root = doc.FirstChildElement( "objectModel" );
				if( root )
				{
					XMLElement* interactionsElement = root->FirstChildElement( "interactions" );
					SOMParser::traverseInteractionClasses
					               ( "", SomInteractionParams, interactionsElement, SomInteractions );
				}
				else
				{
					logger.log( string("Could not locate objectModel in given SOM file"), LevelError );
				}
			}
			else
			{
				logger.log( "Could not Load SOM file in " + somFilePath, LevelError );
			}

			return SomInteractions;
		}

		vector<XMLElement*> SOMParser::getClassChildElements( XMLElement* parentElement,
		                                                      const string& rootText)
		{
			vector<XMLElement*> childElements;

			for( XMLElement* child = parentElement->FirstChildElement( rootText.c_str() );
				 child != NULL; child = child->NextSiblingElement( rootText.c_str() ))
			{
				childElements.emplace_back( child );
			}
			return childElements;
		}

		// this is a recursive method and passing copies of objectClassName and
		// attributes are required for the correct evaluation of values
		void SOMParser::traverseObjectClasses( string objectClassName,
		                                       vector<shared_ptr<ObjectAttribute>> attributes,
		                                       XMLElement* parentElement,
		                                       vector<shared_ptr<ObjectClass>>& objectClasses )
		{
			// this is a leaf object class
			if( parentElement->FirstChildElement( "objectClass" ) == nullptr )
			{
				// get the name of the leaf object class
				XMLElement* objectNameElement = parentElement->FirstChildElement( "name" );
				if( objectNameElement )
				{
					shared_ptr<ObjectClass> objectClass = make_shared<ObjectClass>();

					// fully qualified object class name
					objectClass->name = objectClassName + objectNameElement->GetText();

					// sharing state (pub & sub) of the class
					XMLElement* objectSharingElement = parentElement->FirstChildElement( "sharing" );
					if( objectSharingElement )
					{
						// set the sharing state of the class (not the sharing state of the attributes)
						objectClass->publish = 
							ConversionHelper::isPublish(objectSharingElement->GetText());
						objectClass->subscribe = 
							ConversionHelper::isSubscribe(objectSharingElement->GetText());
					}

					// collect attributes in this object class (traverse only the leaf attributes)
					for( XMLElement* attrElement = parentElement->FirstChildElement( "attribute" );
						 attrElement != NULL; attrElement = attrElement->NextSiblingElement( "attribute" ))
					{
						// try to get the name tag in an attribute
						XMLElement* attributeNameElement = attrElement->FirstChildElement( "name" );
						if( attributeNameElement )
						{
							shared_ptr<ObjectAttribute> objectAttribute = make_shared<ObjectAttribute>();
							// get attribute's name as in SOM
							objectAttribute->name = attributeNameElement->GetText();

							// get the sharing state of attributes
							XMLElement* attributeSharingElement = attrElement->FirstChildElement( "sharing" );
							if( attributeSharingElement )
							{
								objectAttribute->publish = 
										ConversionHelper::isPublish(attributeSharingElement->GetText());
								objectAttribute->subscribe = 
										ConversionHelper::isSubscribe(attributeSharingElement->GetText());
							}
							attributes.push_back( objectAttribute );
						}
					}

					// if we have attributes in this objectClass then we can
					// publish and subscribe so add it to the vector
					if( attributes.size() > 0 )
					{
						for( shared_ptr<ObjectAttribute> attribute : attributes )
						{
							objectClass->objectAttributes.insert( make_pair(attribute->name, attribute) );
						}
						objectClasses.push_back( objectClass );
					}
					else
					{
						string tmpObjectClassName = objectClass->name ;
						Logger::getInstance().log( tmpObjectClassName + " doesn't have any attributes.", LevelWarn );
					}

				}
			}
			else // collect non-leaf attributes or attributes in parent classes
			{
				// get the name element of this parentElement that represents an objectClass
				XMLElement* objectNameElement = parentElement->FirstChildElement( "name" );
				if( objectNameElement )
				{
					// build up the fully qualified class name
					objectClassName += objectNameElement->GetText() + string(".");

					// collect attributes in this object class (traverse non-leaf attributes)
					for( XMLElement* attrElement = parentElement->FirstChildElement( "attribute" );
						 attrElement != NULL; attrElement = attrElement->NextSiblingElement( "attribute" ))
					{
						// try to get the name tag in an attribute
						XMLElement* attributeNameElement = attrElement->FirstChildElement( "name" );
						if( attributeNameElement )
						{
							shared_ptr<ObjectAttribute> objectAttribute = make_shared<ObjectAttribute>();
							// get attribute's name as in SOM
							objectAttribute->name = attributeNameElement->GetText();
							XMLElement* attributeSharingElement = attrElement->FirstChildElement( "sharing" );
							if( attributeSharingElement )
							{
								// set the sharing state of the attribute (not the sharinf state of the class)
								objectAttribute->publish = 
										ConversionHelper::isPublish(attributeSharingElement->GetText());
								objectAttribute->subscribe = 
										ConversionHelper::isSubscribe(attributeSharingElement->GetText());
							}

							attributes.push_back( objectAttribute );
						}
					}
				}

				// seek all the children of this parent element to do depth first search
				vector<XMLElement*> childElements = getClassChildElements( parentElement, "objectClass" );
				// start processing child nodes
				for( XMLElement* childElement : childElements )
				{
					traverseObjectClasses( objectClassName, attributes, childElement, objectClasses );
				}
			}
		}
		void SOMParser::traverseInteractionClasses( string interactionClassName,
		                                            vector<shared_ptr<InteractionParameter>> params,
		                                            tinyxml2::XMLElement * parentElement, 
		                                            vector<shared_ptr<InteractionClass>>& intClasses )
		{
			// this is a leaf interaction class
			if( parentElement->FirstChildElement( "interactionClass" ) == nullptr )
			{
				// get the name of the leaf interaction class
				XMLElement* interactionNameElement = parentElement->FirstChildElement( "name" );
				if( interactionNameElement )
				{
					shared_ptr<InteractionClass> interactionClass = make_shared<InteractionClass>();

					// fully qualified interaction class name
					interactionClass->name = interactionClassName + interactionNameElement->GetText();

					// sharing state (pub & sub) of the interaction class
					XMLElement* objectSharingElement = parentElement->FirstChildElement( "sharing" );
					if( objectSharingElement )
					{
						// set the sharing state of the interaction class (not the sharing state of params)
						interactionClass->publish = 
							ConversionHelper::isPublish( objectSharingElement->GetText() );
						interactionClass->subscribe = 
							ConversionHelper::isSubscribe( objectSharingElement->GetText() );
					}

					// collect params in this interaction class (traverse only the leaf params)
					for( XMLElement* paramElement = parentElement->FirstChildElement( "parameter" );
						 paramElement != NULL; paramElement = paramElement->NextSiblingElement( "parameter" ))
					{
						// try to get the name tag in a param
						XMLElement* attributeNameElement = paramElement->FirstChildElement( "name" );
						if( attributeNameElement )
						{
							shared_ptr<InteractionParameter> interactionParam = make_shared<InteractionParameter>();
							// get param's name as in SOM
							interactionParam->name = attributeNameElement->GetText();
							params.push_back( interactionParam );
						}
					}
	
					for( shared_ptr<InteractionParameter> param : params )
					{
						interactionClass->parameters.insert( make_pair(param->name, param) );
					}
					intClasses.push_back( interactionClass );
				}
			}
			else // collect non-leaf params a.k.a params in parent classes
			{
				// get the name element of this parentElement that represents an interaction class
				XMLElement* interactionNameElement = parentElement->FirstChildElement( "name" );
				if( interactionNameElement )
				{
					// build up the fully qualified interaction class name
					interactionClassName += interactionNameElement->GetText() + string(".");

					// collect params in this interaction class (traverse non-leaf params)
					for( XMLElement* paramElement = parentElement->FirstChildElement( "parameter" );
						 paramElement != NULL; paramElement = paramElement->NextSiblingElement( "parameter" ))
					{
						// try to get the name tag in a param
						XMLElement* paramNameElement = paramElement->FirstChildElement( "name" );
						if( paramNameElement )
						{
							shared_ptr<InteractionParameter> interactionParam = make_shared<InteractionParameter>();
							// get attribute's name as in SOM
							interactionParam->name = paramNameElement->GetText();
							params.push_back( interactionParam );
						}
					}
				}

				// seek all the children of this parent element to do depth first search
				vector<XMLElement*> childElements = getClassChildElements( parentElement, "interactionClass" );
				// start processing child nodes
				for( XMLElement* childElement : childElements )
				{
					traverseInteractionClasses( interactionClassName, params, childElement, intClasses );
				}
			}
		}
	}
}