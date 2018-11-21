#include "HLAObject.h"

#include <string>
#include "gov/nist/ucef/util/Logger.h"
#include "RTI/Handle.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{
	HLAObject::HLAObject( const string& objectClassName,
	                      long instanceId ) : m_className( objectClassName ),
	                                              m_instanceId( instanceId )
	{
		m_attributeDataStore = make_shared<HLAObjectAttributes>();
	}

	HLAObject::~HLAObject()
	{
	}

	void HLAObject::setAttributeValue( const string& attributeName, bool val )
	{
		setAttributeValue( attributeName, make_shared<bool>(val), sizeof(bool) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, const char val )
	{
		setAttributeValue( attributeName, make_shared<char>(val), sizeof(char) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, short val )
	{
		setAttributeValue( attributeName, make_shared<short>(val), sizeof(short));
	}

	void HLAObject::setAttributeValue( const string& attributeName, int val )
	{
		setAttributeValue( attributeName, make_shared<int>(val), sizeof(int) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, long val )
	{
		setAttributeValue( attributeName, make_shared<long>(val), sizeof(long) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, float val )
	{
		setAttributeValue( attributeName, make_shared<float>(val), sizeof(float) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, double val )
	{
		setAttributeValue( attributeName, make_shared<double>(val), sizeof(double) );
	}

	void HLAObject::setAttributeValue( const string& attributeName, const string& val )
	{
		shared_ptr<char> arr(new char[val.length() + 1](), [](char *p) { delete [] p; });
		strcpy_s(arr.get(), val.length() + 1, val.c_str());
		setAttributeValue( attributeName, arr, val.length() + 1 );
	}

	void HLAObject::setAttributeValue( const string& attributeName,
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

	bool HLAObject::getAttributeValueAsBool( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (bool *)data.data.get() );
		return false;
	}

	char HLAObject::getAttributeValueAsChar( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (char *)data.data.get() );
		return (char) 0;
	}

	short HLAObject::getAttributeValueAsShort( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAObject::getAttributeValueAsInt( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (int *)data.data.get() );
		return 0;
	}

	long HLAObject::getAttributeValueAsLong( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (long *)data.data.get() );
		return 0;
	}

	float HLAObject::getAttributeValueAsFloat( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (float *)data.data.get() );
		return 0.0f;
	}

	double HLAObject::getAttributeValueAsDouble( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (double *)data.data.get() );
		return 0.0;
	}

	string HLAObject::getAttributeValueAsString( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return string( (char *)data.data.get() );
		return "";
	}

	VariableData HLAObject::getAttributeValue( const string& attributeName ) const
	{
		VariableData data;
		data.data = nullptr;
		data.size = 0;
		auto it = m_attributeDataStore->find( attributeName );
		if( it != m_attributeDataStore->end() )
		{
			data = it->second;
		}
		return data;
	}

	vector<string> HLAObject::getAttributeNames() const
	{
		vector<string> attributeNameList;
		HLAObjectAttributes &store = *m_attributeDataStore;
		for( auto kv : store)
		{
			attributeNameList.emplace_back(kv.first);
		}
		return attributeNameList;
	}

	void HLAObject::clearAttributeDataStore()
	{
		m_attributeDataStore->clear();
	}

	std::string HLAObject::getClassName() const
	{
		return m_className;
	}

	long HLAObject::getInstanceId()
	{
		return m_instanceId;
	}
}
