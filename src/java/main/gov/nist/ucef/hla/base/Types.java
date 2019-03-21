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

import java.util.HashMap;
import java.util.Map;

public class Types
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
	 * Represents an attribute in an object class in a Simulation Object Model
	 *
	 * @see ObjectClass
	 */
	protected static class ObjectAttribute
	{
		public String name;
		public boolean publish;
		public boolean subscribe;

		public ObjectAttribute( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
		}
	}

	/**
	 * Represents an object class in a Simulation Object Model
	 *
	 * @see SOMParser#getObjectClasses(string&)
	 * @see ObjectAttribute
	 */
	protected static class ObjectClass
	{
		public String name; // fully qualified object class name
		public boolean publish;
		public boolean subscribe;
		Map<String,ObjectAttribute> attributes;

		public ObjectClass( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
			this.attributes = new HashMap<>();
		}
	}

	/**
	 * Represents a parameter in an interaction class in a Simulation Object Model
	 *
	 * @see InteractionClass
	 */
	protected static class InteractionParameter
	{
		public String name;

		public InteractionParameter( String name )
		{
			this.name = name;
		}
	}

	/**
	 * Represents an interaction class in a Simulation Object Model
	 *
	 * @see SOMParser#getInteractionClasses(string&)
	 * @see InteractionParameter
	 */
	protected static class InteractionClass
	{
		public String name; // fully qualified interaction class name
		public boolean publish;
		public boolean subscribe;
		public Map<String,InteractionParameter> parameters;

		public InteractionClass( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
			this.parameters = new HashMap<>();
		}
	}
}
