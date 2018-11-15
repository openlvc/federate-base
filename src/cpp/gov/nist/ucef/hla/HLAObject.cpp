#include "HLAObject.h"

#include <string>
#include "gov/nist/ucef/util/Logger.h"
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
		lock_guard<mutex> lockGuard( m_threadSafeLock );
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

	bool HLAObject::getAttributeValuAsBool( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (bool *)data.data.get() );
		return 0;
	}

	char HLAObject::getAttributeValuAsChar( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (char *)data.data.get() );
		return 0;
	}

	short HLAObject::getAttributeValuAsShort( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAObject::getAttributeValuAsInt( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (int *)data.data.get() );
		return 0;
	}

	long HLAObject::getAttributeValuAsLong( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (long *)data.data.get() );
		return 0;
	}

	float HLAObject::getAttributeValuAsFloat( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (float *)data.data.get() );
		return 0.0f;
	}

	double HLAObject::getAttributeValuAsDouble( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return *( (double *)data.data.get() );
		return 0.0;
	}

	string HLAObject::getAttributeValuAsString( const string& attributeName ) const
	{
		VariableData data = getAttributeValue( attributeName );
		if(data.data)
			return string( (char *)data.data.get() );
		return "";
	}

	VariableData HLAObject::getAttributeValue( const string& attributeName ) const
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		VariableData data;
		auto it = m_attributeDataStore->find( attributeName );
		if( it != m_attributeDataStore->end() )
		{
			data = it->second;
		}
		return data;
	}

	void HLAObject::clearAttributeDataStore()
	{
		lock_guard<mutex> lockGuard( m_threadSafeLock );
		m_attributeDataStore->clear();
	}

	std::string HLAObject::getClassName() const
	{
		return m_className;
	}

	shared_ptr<ObjectInstanceHandle> HLAObject::getInstanceHandle()
	{
		return m_instanceHandle;
	}
}
