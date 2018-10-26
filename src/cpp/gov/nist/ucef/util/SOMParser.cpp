#include "SOMParser.h"

using namespace std;
using namespace tinyxml2;

namespace ucef
{
	namespace util
	{
		vector<shared_ptr<ObjectClass>> SOMParser::getObjectClasses( const string& SomFilePath,
		                                                           const string& SomFileName )
		{
			vector<shared_ptr<ObjectClass>> SomObjects;
			vector<shared_ptr<ObjectAttribute>> SomA;
			string somPath = SomFilePath + SomFileName;
			XMLDocument doc;
			XMLError xmlError = doc.LoadFile( somPath.c_str() );
			if( xmlError == XML_SUCCESS )
			{
				XMLElement* root = doc.FirstChildElement( "objectModel" );
				if( root )
				{
					XMLElement* objectsElement = root->FirstChildElement( "objects" );
					SOMParser::traverseObjectClasses( L"", SomA, objectsElement, SomObjects );
				}
				else
				{
					// TO-DO Log an error
				}
			}
			else
			{
				// TO-DO Log an error
			}
			return SomObjects;
		}

		vector<shared_ptr<InteractionClass>> SOMParser::getInteractionClasses( const string& SomFilePath,
		                                                                       const string& SomFileName )
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

		void SOMParser::traverseObjectClasses( wstring objectClassName,
		                                       vector<shared_ptr<ObjectAttribute>> attributes,
											   XMLElement* parentElement,
											   vector<shared_ptr<ObjectClass>> &objectClasses )
		{
			if( parentElement->FirstChildElement( "objectClass" ) == nullptr )
			{
				XMLElement* objectNameElement = parentElement->FirstChildElement( "name" );
				if( objectNameElement )
				{
					shared_ptr<ObjectClass> objectClass = make_shared<ObjectClass>();
					// fully qualified object class name
					objectClass->name = objectClassName + ConversionHelper::s2ws(objectNameElement->GetText());
					XMLElement* objectSharingElement = parentElement->FirstChildElement( "sharing" );
					if( objectSharingElement )
					{
						// set the sharing state of the object (not the attribute)
						objectClass->sharingState = ConversionHelper::toSharingState(objectSharingElement->GetText());
					}
					// collect attributes in this object class (leaf node)
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
							objectClass->attributes.push_back( attribute );
						}
						objectClasses.push_back(objectClass);
					}
					else
					{
						// TO-DO Log an data
					}

				}
			}
			else
			{
				// get the name element of this parentElement that represents an objectClass
				XMLElement* classNameElement = parentElement->FirstChildElement( "name" );
				if( classNameElement )
				{
					// build up the fully qualified class name
					objectClassName +=
							ConversionHelper::s2ws(classNameElement->FirstChildElement( "name" )->GetText());

					// collect attributes in this object class
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
				vector<XMLElement*> childElements = getObjectClassChildElements(parentElement);
				// start processing child nodes
				for(XMLElement* childElement : childElements)
				{
					traverseObjectClasses(objectClassName, attributes, childElement, objectClasses);
				}
			}
		}

	}
}

