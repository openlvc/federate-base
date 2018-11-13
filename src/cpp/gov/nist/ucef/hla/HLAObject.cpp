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

	void HLAObject::setAttributeValue( const string& attributeName, bool val )
	{
		
	}

	void HLAObject::setAttributeValue( const string& attributeName, char val )
	{
		
	}

	void HLAObject::setAttributeValue( const string& attributeName, short val )
	{
	
	}

	void HLAObject::setAttributeValue( const string& attributeName, int val )
	{
		
	}

	void HLAObject::setAttributeValue( const string& attributeName, long val )
	{
		
	}

	void HLAObject::setAttributeValue( const string& attributeName, float val )
	{
	
	}

	void HLAObject::setAttributeValue( const string& attributeName, double val )
	{
		
	}

	void HLAObject::setAttributeValue( const string& attributeName, const string& val )
	{
		pushToMap( attributeName, val );
	}

	void HLAObject::resetAttributeValues()
	{
		attributeData.clear();
	}

	std::string HLAObject::getClassName()
	{
		return m_className;
	}

	HLAObjectAttributes HLAObject::getAttributeData()
	{
		return attributeData;
	}

	shared_ptr<ObjectInstanceHandle> HLAObject::getInstanceHandle()
	{
		return m_instanceHandle;
	}

	void HLAObject::pushToMap( const string& attributeName, const string& data )
	{
		if( attributeData.find( attributeName ) != attributeData.end() )
			attributeData[attributeName] = data;
		else
			attributeData.insert( pair<string, string>( attributeName, data ) );
	}
}
