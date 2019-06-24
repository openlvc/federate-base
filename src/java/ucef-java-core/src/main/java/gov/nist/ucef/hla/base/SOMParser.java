/*
 * This software is contributed as a public service by The National Institute of Standards
 * and Technology (NIST) and is not subject to U.S. Copyright
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */

package gov.nist.ucef.hla.base;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.InteractionParameter;
import gov.nist.ucef.hla.base.Types.ObjectAttribute;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.Types.Sharing;

public class SOMParser
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( SOMParser.class );

	// SOM XML node identifier constants
	private static final String OBJECT_MODEL = "objectModel";
	private static final String NAME = "name";
	private static final String SHARING = "sharing";
	private static final String OBJECTS = "objects";
	private static final String OBJECTCLASS = "objectClass";
	private static final String ATTRIBUTE = "attribute";
	private static final String INTERACTIONS = "interactions";
	private static final String INTERACTIONCLASS = "interactionClass";
	private static final String PARAMETER = "parameter";
	private static final String DATA_TYPE = "dataType";

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
	/**
	 * Reads a SOM XML definition and pushes the relevant parts into the provided
	 * {@link FederateConiguration} instance
	 *
	 * @param somPath the path to SOM file
	 * @param config the {@link FederateConfiguration} instance which needs to be updated
	 */
	public static void somToFederateConfig( String somPath, FederateConfiguration config )
	{
		Document dom = buildDOM(somPath);
		Element objectModelNode = findObjectModelNode(dom);

		// get the <objects> element, which is directly under <objectModel>
		Element objectsRoot = getElementByPath( objectModelNode, OBJECTS );
		Collection<ObjectClass> reflections = extractObjectClasses( objectsRoot );
		config.cacheObjectClasses(reflections);

		// get the <interactions> element, which is directly under <objectModel>
		Element interactionsRoot = getElementByPath( objectModelNode, INTERACTIONS );
		Collection<InteractionClass> interactions = extractInteractionClasses( interactionsRoot );
		config.cacheInteractionClasses(interactions);
	}

	/**
	 * Reads a SOM XML definition and extracts the details of the object classes
	 *
	 * @param somPath the path to SOM file
	 * @return a collection of {@link ObjectClass} instances which contain details of the names,
	 *         datatypes, attributes, sharing, and so on
	 */
	public static Collection<ObjectClass> getObjectClasses( String somPath )
	{
		Document dom = buildDOM(somPath);
		Element objectModelNode = findObjectModelNode(dom);

		// get the <objects> element, which is directly under <objectModel>
		Element objectsRoot = getElementByPath( objectModelNode, OBJECTS );
		return extractObjectClasses( objectsRoot );
	}

	/**
	 * Reads a SOM XML definition and extracts the details of the interaction classes
	 *
	 * @param somPath the path to SOM file
	 * @return a collection of {@link InteractionClass} instances which contain details of the names,
	 *         datatypes, parameters, sharing, and so on
	 */
	public static Collection<InteractionClass> getInteractionClasses( String somPath )
	{
		Document dom = buildDOM(somPath);
		Element objectModelNode = findObjectModelNode(dom);

		// get the <interactions> element, which is directly under <objectModel>
		Element interactionsRoot = getElementByPath( objectModelNode, INTERACTIONS );

		return extractInteractionClasses( interactionsRoot );
	}

	/**
	 * Attempt to build a DOM from the SOM file at the given path
	 *
	 * @param somPath the path to the SOM XML
	 * @return the DOM from the SOM XML
	 */
	private static Document buildDOM( String somPath )
	{
		// check both file system and resources for the file
		File file = getResource( somPath );
		if( file == null || !file.isFile() )
		{
			throw new UCEFException( "The file '%s' does not exist. " +
			                         "Please check the file path.", file.getAbsolutePath() );
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using builder to get DOM representation of the XML file
			return db.parse( file );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Unable to parse SOM from '%s'.", somPath );
		}
	}

	/**
	 * Attempt to locate the `<objectModel>` node in DOM (should be at the "top" of the DOM)
	 *
	 * @param dom the DOM created from the SOM XML
	 * @return the `<objectModel>` node in DOM, or null if no such node could be found
	 */
	public static Element findObjectModelNode( Document dom )
	{
		// get the root element, which is <objectModel>
		// Element objectModelNode = dom.getDocumentElement();
		Element objectModelNode = getElementByPath(dom, OBJECT_MODEL);
		if(objectModelNode == null)
		{
			// can't find the <objectModel> node, which we need to have to proceed
			throw new UCEFException( "Unable to locate '%s' node in SOM.", OBJECT_MODEL);
		}
		return objectModelNode;
	}

	/**
	 * Extracts the details of the object classes defined in the SOM
	 *
	 * @param root the `<objects>` node which contains the `<objectClass>` definition nodes
	 * @return the extracted object classes
	 */
	private static Collection<ObjectClass> extractObjectClasses( Element root )
	{
		List<ObjectClass> objectClasses = new ArrayList<>();
		List<ObjectAttribute> objectAttributes = new ArrayList<>();

		for( Element elm : getChildElementsByName( root, OBJECTCLASS ) )
		{
			traverseObjectClasses( elm, "", objectClasses, objectAttributes );
		}

		return objectClasses;
	}

	/**
	 * Extracts the details of the interaction classes defined in the SOM
	 *
	 * @param root the `<interactions>` node which contains the `<interactionClass>` definition nodes
	 * @return the extracted interaction classes
	 */
	private static Collection<InteractionClass> extractInteractionClasses( Element root )
	{
		List<InteractionClass> interactionClasses = new ArrayList<>();
		List<InteractionParameter> interactionParameters = new ArrayList<>();

		for( Element elm : getChildElementsByName( root, INTERACTIONCLASS ) )
		{
			traverseInteractionClasses( elm, "", interactionClasses, interactionParameters );
		}

		return interactionClasses;
	}

	/**
	 * Note that is a recursive method and passing copies of objectClassName and attributes
	 * are required for the correct evaluation of values
	 *
	 * @param root the current `<objectClass>` node
	 * @param namespace the current namespace
	 * @param objectClasses the object classes collected so far
	 * @param attributes the object class attributes collected so far
	 */
	private static void traverseObjectClasses( Element root, String namespace,
	                                           Collection<ObjectClass> objectClasses,
	                                           Collection<ObjectAttribute> attributes)
	{
		String className = getTextValue( root, NAME );
		String sharingStr = getTextValue( root, SHARING );

		Sharing sharing = Sharing.fromLabel( sharingStr );
		ObjectClass objectClass = new ObjectClass( namespace + className, sharing );

		for( Element elm : getChildElementsByName( root, ATTRIBUTE ) )
		{
			String attrName = getTextValue( elm, NAME );
			String attrSharingStr = getTextValue( elm, SHARING );
			String attrTypeStr = getTextValue( elm, DATA_TYPE );

			Sharing attrSharing = Sharing.fromLabel( attrSharingStr );
			DataType attrDataType = DataType.fromLabel( attrTypeStr );
			ObjectAttribute attribute = new ObjectAttribute( attrName, attrDataType, attrSharing );

			attributes.add( attribute );
		}

		// if we have attributes in this objectClass then we can publish and
		// subscribe so add it to the vector
		if( attributes.size() > 0 )
		{
			for( ObjectAttribute attribute : attributes )
				objectClass.addAttribute( attribute );

			objectClasses.add( objectClass );
		}
		else
		{
			logger.warn( objectClass.name  + " doesn't have any attributes - ignoring." );
		}

		for(Element elm : getChildElementsByName( root, OBJECTCLASS ))
		{
			// recurse - make a copy of the attributes so we don't "pollute"
			// all the way through the tree using the original collection
			ArrayList<ObjectAttribute> recAttributes = new ArrayList<ObjectAttribute>();
			recAttributes.addAll( attributes );
			traverseObjectClasses( elm, (namespace + className + "."),
			                       objectClasses, recAttributes );
		}
	}

	/**
	 * Note that is a recursive method and passing copies of objectClassName and attributes
	 * are required for the correct evaluation of values
	 *
	 * @param root the current `<interactionClass>` node
	 * @param namespace the current namespace
	 * @param interactionClasses the interaction classes collected so far
	 * @param parameters the interaction class parameters collected so far
	 */
	private static void traverseInteractionClasses( Element root, String namespace,
	                                                Collection<InteractionClass> interactionClasses,
	                                                Collection<InteractionParameter> parameters )
	{
		String className = getTextValue( root, NAME );
		String sharingStr = getTextValue( root, SHARING );

		Sharing sharing   = Sharing.fromLabel( sharingStr );
		InteractionClass interactionClass = new InteractionClass(namespace + className, sharing);

		for(Element elm : getChildElementsByName( root, PARAMETER ))
		{
			String parameterName    = getTextValue( elm, NAME );
			String parameterTypeStr = getTextValue( elm, DATA_TYPE );

			DataType dataType = DataType.fromLabel( parameterTypeStr );
			InteractionParameter parameter = new InteractionParameter( parameterName, dataType );

			parameters.add(parameter);
		}

		// interactions without parameters are just fine
		for( InteractionParameter parameter : parameters )
			interactionClass.addParameter( parameter );

		interactionClasses.add( interactionClass );

		for(Element elm : getChildElementsByName( root, INTERACTIONCLASS ))
		{
			// recurse - make a copy of the parameters so we don't "pollute"
			// all the way through the tree using the original collection
			ArrayList<InteractionParameter> recParameters = new ArrayList<InteractionParameter>();
			recParameters.addAll( parameters );
			traverseInteractionClasses( elm, (namespace + className + "."),
			                            interactionClasses, recParameters );
		}
	}

	/**
	 * Given an XML element and a tag name, search for the tag and get its text content
	 *
	 * For example, given XML...
	 *
	 *     <drink><name>Mojito</name></drink>
	 *
	 * ...if the element points to the <drink> node and tagName is `name`, "Mojito" will be
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

	/**
	 * Attempts to find an XML element given a 'path' to the element from the given root node
	 *
	 * @param root the root node to begin the traversal at
	 * @param path the path to traverse along ('/' delimited)
	 * @return the element located using the path, or null if the path did not resolve to an
	 *         element
	 */
	private static Element getElementByPath(Node root, String path)
	{
		if( root == null )
			return null;

		if( path == null || path.trim().length() == 0)
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

	/**
	 * Obtain all child elements of an XML node by name
	 *
	 * @param root the XML node to obtain the children of
	 * @param elementName the name to identify the children by
	 * @return the matching child elements
	 */
	private static List<Element> getChildElementsByName( Node root, String elementName )
	{
		List<Element> elements = new ArrayList<>();

		if(root == null )
			return elements;

		if( elementName == null || elementName.trim().length() == 0)
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

	/**
	 * Utility method to check both the file system and system resources for a file
	 *
	 * @param path the path to check
	 * @return the file if found, null otherwise
	 */
	private static File getResource( String path )
	{
		// check both file system and resources for the file
		File file = new File( path );
		if( !file.isFile() )
		{
			URL fileUrl = SOMParser.class.getClassLoader().getResource( path );
			if( fileUrl != null )
				file = new File( fileUrl.getFile() );
			else
				file = null;
		}
		return file;
	}
}