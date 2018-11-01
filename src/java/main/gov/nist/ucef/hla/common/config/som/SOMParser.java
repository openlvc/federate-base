
/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package gov.nist.ucef.hla.common.config.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.nist.ucef.hla.common.UCEFException;
import gov.nist.ucef.hla.common.config.FederateConfiguration;
import gov.nist.ucef.hla.util.StringUtils;

public class SOMParser
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String NAME = "name";
	private static final String SHARING = "sharing";
	private static final String OBJECTS = "objects";
	private static final String OBJECTCLASS = "objectClass";
	private static final String ATTRIBUTE = "attribute";
	private static final String INTERACTIONS = "interactions";
	private static final String INTERACTIONCLASS = "interactionClass";
	private static final String PARAMETER = "parameter";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void somToFederationConfig( String somPath, FederateConfiguration config )
	{
		// String somPathRoot = "resources/soms/RestaurantSOMmodule.xml";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse( somPath );

			// get the root element, which is <objectModel>
			Element objectModelNode = dom.getDocumentElement();

			// get the <objects> element, which is directly under <objectModel>
			Element objectsRoot = getElementByPath( objectModelNode, OBJECTS );
			parseObjectNode( objectsRoot, config );

			// get the <interactions> element, which is directly under <objectModel>
			Element interactionsRoot = getElementByPath( objectModelNode, INTERACTIONS );
			parseInteractionsNode( interactionsRoot, config );
		}
		catch( Exception e )
		{
			throw new UCEFException( "Unable to parse SOM to federate configuration.", e );
		}
	}
	
	/**
	 * Given an XML element and a tag name, search for the tag and get its text content
	 * 
	 * For example, given XML...
	 * 
	 *     <drink><name>Mojito</name></drink>
	 * 
	 * ...if the element points to the <drink> node and tagName is "name", "Mojito" will be
	 * returned
	 * 
	 * @param element the XML element
	 * @param tag the tag to extract text from within the XML element
	 * @return the text value, or null if no such tag could be found
	 */
	private static String getTextValue( Element element, String tag )
	{
		NodeList nl = element.getElementsByTagName( tag );
		if( nl != null && nl.getLength() > 0 )
		{
			Element el = (Element)nl.item( 0 );
			return el.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private static void parseObjectNode(Element root, FederateConfiguration config)
	{
		for(Element elm : getChildElementsByName( root, OBJECTCLASS ))
		{
			parseObjectClass( elm, "", config );
		}
	}
	
	private static void parseObjectClass(Element root, String namespace, FederateConfiguration config)
	{
		String className = getTextValue( (Element)root, "name" );
		for(Element elm : getChildElementsByName( root, OBJECTCLASS ))
		{
			parseObjectClass( elm, (namespace + className + "."), config );
		}
		
		Sharing sharing = Sharing.fromID( getTextValue( (Element)root, "sharing" ) );
		if(sharing != null && sharing.isNot( Sharing.NEITHER ))
		{
    		for(Element elm : getChildElementsByName( root, ATTRIBUTE ))
    		{
    			parseObjectClassAttributeNode( elm, namespace + className, sharing, config );
    		}
		}
	}
	
	private static void parseObjectClassAttributeNode(Element root, String objectClassName,
	                                                  Sharing sharing, FederateConfiguration config)
	{
		String attributeName = getTextValue( (Element)root, "name" );
		String datatype = getTextValue( (Element)root, "dataType" );
		switch(sharing)
		{
			case PUBLISH_SUBSCRIBE:
			{
				config.addPublishedAtribute( objectClassName, attributeName );
				config.addSubscribedAtribute( objectClassName, attributeName );
				break;
			}
			case PUBLISH:
			{
				config.addPublishedAtribute( objectClassName, attributeName );
				break;
			}
			case SUBSCRIBE:
			{
				config.addSubscribedAtribute( objectClassName, attributeName );
				break;
			}
			default:
				// NEITHER
				break;
		}
	}
	
	private static void parseInteractionsNode(Element root, FederateConfiguration config)
	{
		for(Element elm : getChildElementsByName( root, INTERACTIONCLASS ))
		{
			parseInteractionClassNode( elm, "", config );
		}
	}
	
	private static void parseInteractionClassNode(Element root, String namespace, FederateConfiguration config)
	{
		String className = getTextValue( root, "name" );
		Sharing sharing = Sharing.fromID( getTextValue( root, "sharing" ) );
		for(Element elm : getChildElementsByName( root, INTERACTIONCLASS ))
		{
			parseInteractionClassNode( elm, (namespace + className + "."), config );
		}
		if(sharing != null && sharing.isNot( Sharing.NEITHER ))
		{
			String interactionClassName = namespace + className;
			switch(sharing)
			{
				case PUBLISH_SUBSCRIBE:
				{
					config.addPublishedInteraction( interactionClassName );
					config.addSubscribedInteraction( interactionClassName );
					break;
				}
				case PUBLISH:
				{
					config.addPublishedInteraction( interactionClassName );
					break;
				}
				case SUBSCRIBE:
				{
					config.addSubscribedInteraction( interactionClassName );
					break;
				}
				default:
					// NEITHER
					break;
			}
		}
	}
	
	private static Element getElementByPath(Node root, String path)
	{
		if( root == null )
			return null;

		if( StringUtils.isNullOrEmpty( path ) )
			return null;
		
		String[] parts = path.split("\\.", 2);
		List<Element> elements = getChildElementsByName( root, parts[0] );
		if(elements.size() > 0)
		{
			Element current = elements.get(0);
			if(elements.size() == 1)
				return current;
			else if (parts.length > 0)
				return getElementByPath(current, parts[1]);
		}
		return null;
	}
	
	private static List<Element> getChildElementsByName( Node root, String elementName )
	{
		List<Element> elements = new ArrayList<>();
		
		if(root == null || StringUtils.isNullOrEmpty( elementName ))
			return elements;
			
		NodeList objectsList = root.getChildNodes();
		if( objectsList != null )
		{
			for( int i = 0; i < objectsList.getLength(); i++ )
			{
				Node node = objectsList.item( i );
				if( node.getNodeType() == Node.ELEMENT_NODE )
				{
					if( elementName.equals( node.getNodeName() ) )
					{
						elements.add( (Element)node );
					}
				}
			}
		}
		
		return elements;
	}
}
