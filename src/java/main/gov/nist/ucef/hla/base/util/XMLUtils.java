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
package gov.nist.ucef.hla.base.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import old.util.StringUtils;

public class XMLUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

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
	 * Given an XML element and a dot delimited path, search for the tag and the matching node
	 * 
	 * For example, given XML...
	 * 
	 *     <restaurant>
	 *         <consumables>
	 *             <food>
	 *                 <name>Tostada</name>
	 *             </food>
	 *             <drink>
	 *                 <name>Mojito</name>
	 *             </drink>
	 *        </consumables>
	 *    </restaurant>
	 * 
	 * ...if the element points to the <restaurant> node and path is "consumables.drink.name", the 
	 * <name>Mojito</name> element will be returned.
	 * 
	 * NOTE: in the case that there are multiple matches on any item in the path, only the first
	 * matching item will be traversed  
	 * 
	 * @param element the XML element
	 * @param path the dot delimited path to the target XML element
	 * @return the element, or null if no such element could be found matching the path
	 */
	public static Element getElementByPath(Node root, String path)
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
	
	/**
	 * Given an XML element and a tag name, obtain all child elements with that tag
	 * 
	 * For example, given XML...
	 * 
	 *     <consumables>
	 *         <drink>
	 *             <name>Mojito</name>
	 *         </drink>
	 *         <drink>
	 *             <name>Manhattan</name>
	 *         </drink>
	 *         <drink>
	 *             <name>Martini</name>
	 *         </drink>
	 *     </consumables>
	 * 
	 * ...if the element points to the <consumables> node and tagName is "drink", the three <drink>
	 * elements will be returned
	 * 
	 * NOTE: the tag name search is not recursive; only the immediate children of the provided element
	 * will be considered.
	 * 
	 * @param element the XML element
	 * @param elementName the element name to match on
	 * @return the matching elements, if any (may be an empty list)
	 */
	public static List<Element> getChildElementsByName( Node root, String elementName )
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
	
	/**
	 * Given an XML element and a tag name, search for the tag and get its text content
	 * 
	 * For example, given XML...
	 * 
	 *     <drink>
	 *         <name>Mojito</name>
	 *     </drink>
	 * 
	 * ...if the element points to the <drink> node and tagName is "name", "Mojito" will be
	 * returned
	 * 
	 * @param element the XML element
	 * @param tag the tag to extract text from within the XML element
	 * @return the text value, or null if no such tag could be found
	 */
	public static String getTextValue( Element element, String tag )
	{
		NodeList nl = element.getElementsByTagName( tag );
		if( nl != null && nl.getLength() > 0 )
		{
			Element el = (Element)nl.item( 0 );
			return el.getFirstChild().getNodeValue();
		}
		return null;
	}
}