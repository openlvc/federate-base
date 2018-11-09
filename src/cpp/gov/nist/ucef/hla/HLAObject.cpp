#include "HLAObject.h"

#include "RTI/Handle.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{
	HLAObject::HLAObject( const string& className,
	                      shared_ptr<ObjectInstanceHandle>& instanceHandle ) : m_className( className ),
	                                                                           m_instanceHandle( instanceHandle )
	{
	}

	HLAObject::~HLAObject()
	{
	}

	void HLAObject::setAttributeValue( string& attributeName, bool val )
	{
		
	}

	void HLAObject::setAttributeValue( string& attributeName, char val )
	{
		
	}

	void HLAObject::setAttributeValue( string& attributeName, short val )
	{
	
	}

	void HLAObject::setAttributeValue( string& attributeName, int val )
	{
		
	}

	void HLAObject::setAttributeValue( string& attributeName, long val )
	{
		
	}

	void HLAObject::setAttributeValue( string& attributeName, float val )
	{
	
	}

	void HLAObject::setAttributeValue( string& attributeName, double val )
	{
		
	}

	void HLAObject::setAttributeValue( string& attributeName, string& val )
	{
		pushToMap( attributeName, val );
	}

	std::string HLAObject::getClassName()
	{
		return m_className;
	}

	HLAObjectAttributeMap* HLAObject::getAttributeMap()
	{
		return &attributeData;
	}

	shared_ptr<ObjectInstanceHandle> HLAObject::getInstanceHandle()
	{
		return m_instanceHandle;
	}

	void HLAObject::pushToMap( string& attributeName, string& data )
	{
		attributeData.insert( pair<string, string>( m_className + "." + attributeName, data ) );
	}
}
