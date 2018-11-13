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
		m_attributeDataStore = make_shared<HLAObjectAttributes>();
	}

	HLAObject::~HLAObject()
	{
	}

	void HLAObject::setAttributeValue( const string& attributeName, bool val )
	{
		pushToAttributeStore( attributeName, make_shared<bool>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, char val )
	{
		pushToAttributeStore( attributeName, make_shared<char>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, short val )
	{
		pushToAttributeStore( attributeName, make_shared<short>(val), sizeof(val));
	}

	void HLAObject::setAttributeValue( const string& attributeName, int val )
	{
		pushToAttributeStore( attributeName, make_shared<int>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, long val )
	{
		pushToAttributeStore( attributeName, make_shared<long>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, float val )
	{
		pushToAttributeStore( attributeName, make_shared<float>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, double val )
	{
		pushToAttributeStore( attributeName, make_shared<double>(val), sizeof(val) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, const string& val )
	{
		pushToAttributeStore( attributeName, make_shared<string>(val), sizeof(val) );
	}

	bool HLAObject::getAttributeAsBool( const std::string & attributeName )
	{
		return false;
	}

	char HLAObject::getAttributeAsChar( const std::string & attributeName )
	{
		return 0;
	}

	short HLAObject::getAttributeAsShort( const std::string & attributeName )
	{
		return 0;
	}

	int HLAObject::getAttributeAsInt( const std::string & attributeName )
	{
		return 0;
	}

	long HLAObject::getAttributeAsLong( const std::string & attributeName )
	{
		return 0;
	}

	float HLAObject::getAttributeAsFloat( const std::string & attributeName )
	{
		return 0.0f;
	}

	double HLAObject::getAttributeAsDouble( const std::string & attributeName )
	{
		return 0.0;
	}

	string HLAObject::getAttributeAsString( const std::string & attributeName )
	{
		return string();
	}

	shared_ptr<HLAObjectAttributes> HLAObject::getAttributeDataStore()
	{
		return m_attributeDataStore;
	}

	void HLAObject::clearAttributeDataStore()
	{
		m_attributeDataStore->clear();
	}

	std::string HLAObject::getClassName()
	{
		return m_className;
	}

	shared_ptr<ObjectInstanceHandle> HLAObject::getInstanceHandle()
	{
		return m_instanceHandle;
	}

	void HLAObject::pushToAttributeStore( const string& attributeName,
	                                      shared_ptr<void> data,
	                                      const size_t size )
	{
		if( m_attributeDataStore->find( attributeName ) != m_attributeDataStore->end() )
		{
			(*m_attributeDataStore)[attributeName].data = data;
			(*m_attributeDataStore)[attributeName].size = size;
		}
		else
		{ 
			VariableData variableData;
			variableData.data = data;
			variableData.size = size;
			m_attributeDataStore->insert( pair<string, VariableData>( attributeName, variableData) );
		}
	}
}
