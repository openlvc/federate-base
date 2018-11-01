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

			logger.log( "Trying to load SOM file in " + somFilePath, LevelInfo );

			XMLDocument doc;
			XMLError xmlError = doc.LoadFile( somFilePath.c_str() );
			if( xmlError == XML_SUCCESS )
			{
				logger.log( "SOM loaded succefully " + somFilePath, LevelInfo );
	
				XMLElement* root = doc.FirstChildElement( "objectModel" );
				if( root )
				{
					XMLElement* objectsElement = root->FirstChildElement( "objects" );
					SOMParser::traverseObjectClasses( L"", SomAttributes, objectsElement, SomObjects );
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
			vector<shared_ptr<InteractionClass>> SomInteractions;

			return SomInteractions;
		}

		vector<XMLElement*> SOMParser::getObjectClassChildElements( XMLElement* parentElement )
		{
			vector<XMLElement*> childElements;

			for( XMLElement* child = parentElement->FirstChildElement( "objectClass" );
				 child != NULL; child = child->NextSiblingElement( "objectClass" ))
			{
				childElements.emplace_back( child );
			}
			return childElements;
		}

		// this is a recursive method and passing copies of objectClassName and
		// attributes are required for the correct evaluation of values
		void SOMParser::traverseObjectClasses( wstring objectClassName,
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
					objectClass->name = objectClassName + ConversionHelper::s2ws(objectNameElement->GetText());

					// sharing state (pub & sub) of the object
					XMLElement* objectSharingElement = parentElement->FirstChildElement( "sharing" );
					if( objectSharingElement )
					{
						// set the sharing state of the object (not the attribute)
						objectClass->sharingState = ConversionHelper::toSharingState(objectSharingElement->GetText());
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
							objectAttribute->name = ConversionHelper::s2ws(attributeNameElement->GetText());

							// get the sharing state of this attribute
							XMLElement* attributeSharingElement = attrElement->FirstChildElement( "sharing" );
							if( attributeSharingElement )
							{
								// set the sharing state of the attribute (not the object)
								objectAttribute->sharingState =
									ConversionHelper::toSharingState( attributeSharingElement->GetText() );
							}
							attributes.push_back( objectAttribute );
						}
					}

					// if we have attributes in this objectClass then it is a valid class to
					// register for publishing and subscribing so add it to the vector
					if( attributes.size() > 0 )
					{
						for( shared_ptr<ObjectAttribute> attribute : attributes )
						{
							objectClass->objectAttributes.insert(
								make_pair(ConversionHelper::ws2s(attribute->name), attribute) );
						}
						objectClasses.push_back( objectClass );
					}
					else
					{
						string tmpObjectClassName = ConversionHelper::ws2s( objectClass->name );
						Logger::getInstance().log( tmpObjectClassName + " doesn't have any attributes.", LevelWarn );
					}

				}
			}
			else
			{
				// get the name element of this parentElement that represents an objectClass
				XMLElement* objectNameElement = parentElement->FirstChildElement( "name" );
				if( objectNameElement )
				{
					// build up the fully qualified class name
					objectClassName +=
							ConversionHelper::s2ws( objectNameElement->GetText() ) + L".";

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
							objectAttribute->name = ConversionHelper::s2ws( attributeNameElement->GetText() );
							XMLElement* attributeSharingElement = attrElement->FirstChildElement( "sharing" );
							if( attributeSharingElement )
							{
								// set the sharing state of the attribute (not the object)
								objectAttribute->sharingState =
										ConversionHelper::toSharingState( attributeSharingElement->GetText() );
							}

							attributes.push_back( objectAttribute );
						}
					}
				}

				// seek all the children of this parent element to do depth first search
				vector<XMLElement*> childElements = getObjectClassChildElements( parentElement );
				// start processing child nodes
				for( XMLElement* childElement : childElements )
				{
					traverseObjectClasses( objectClassName, attributes, childElement, objectClasses );
				}
			}
		}
	}
}

