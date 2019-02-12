#include "HLAObject.h"

#include <cstring>
#include <string>

#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/hla/base/UCEFDataTypeException.h"

#include "RTI/Handle.h"

using namespace rti1516e;
using namespace std;
using namespace base::util;

namespace base
{
	HLAObject::HLAObject( const string& objectClassName, long id ) : className( objectClassName ),
	                                                                 instanceId( id )
	{
		this->attributeDataStore = make_shared<HLAObjectAttributes>();
	}

	HLAObject::HLAObject( const std::string & objectClassName )
	{
		this->className = objectClassName;
		this->instanceId = INVLID_LONG;
		this->attributeDataStore = make_shared<HLAObjectAttributes>();
	}

	HLAObject::~HLAObject()
	{
	}

	HLAObject::HLAObject( const HLAObject& hlaObject )
	{
		this->className = hlaObject.className;
		this->instanceId = hlaObject.instanceId;
		this->attributeDataStore = make_shared<HLAObjectAttributes>();
		// if there is any attributes copy across
		auto &attributeStoreFrom = *hlaObject.attributeDataStore;
		for( auto item : attributeStoreFrom )
		{
			setValue( item.first, item.second.data, item.second.size );
		}
	}

	
	bool HLAObject::isPresent( const string& attributeName ) const
	{
		bool flag = attributeDataStore->find( attributeName ) == attributeDataStore->end() ? false : true;
		return flag;
	}

	void HLAObject::setValue( const string& attributeName, bool val )
	{
		setValue( attributeName, make_shared<bool>(val), sizeof(bool) );
	}

	void HLAObject::setValue( const string& attributeName, const char val )
	{
		setValue( attributeName, make_shared<char>(val), sizeof(char) );
	}

	void HLAObject::setValue( const string& attributeName, short val )
	{
		setValue( attributeName, make_shared<short>(val), sizeof(short));
	}

	void HLAObject::setValue( const string& attributeName, int val )
	{
		setValue( attributeName, make_shared<int>(val), sizeof(int) );
	}

	void HLAObject::setValue( const string& attributeName, long val )
	{
		setValue( attributeName, make_shared<long>(val), sizeof(long) );
	}

	void HLAObject::setValue( const string& attributeName, float val )
	{
		setValue( attributeName, make_shared<float>(val), sizeof(float) );
	}

	void HLAObject::setValue( const string& attributeName, double val )
	{
		setValue( attributeName, make_shared<double>(val), sizeof(double) );
	}

	void HLAObject::setValue( const string& attributeName, const string& val )
	{
		shared_ptr<char> arr(new char[val.length() + 1](), [](char *p) { delete [] p; });
		strcpy(arr.get(), val.c_str());
		setValue( attributeName, arr, val.length() + 1 );
	}

	void HLAObject::setValue( const string& attributeName,
	                          shared_ptr<void> data,
	                          const size_t size )
	{
		auto it = attributeDataStore->find( attributeName );
		if( it != attributeDataStore->end() )
		{
			it->second.data = data;
			it->second.size = size;
		}
		else
		{ 
			VariableData variableData;
			variableData.data = data;
			variableData.size = size;
			attributeDataStore->insert( pair<string, VariableData>( attributeName, variableData) );
		}
	}

	void HLAObject::setInstanceId( long hash )
	{
		this->instanceId = hash;
	}

	bool HLAObject::getAsBool( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(bool) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Bool type" );

		if( data.data )
			return *( (bool *)data.data.get() );
		return false;
	}

	char HLAObject::getAsChar( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(char) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Char type" );

		if( data.data )
			return *( (char *)data.data.get() );
		return (char) 0;
	}

	short HLAObject::getAsShort( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(short) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Short type" );

		if( data.data )
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAObject::getAsInt( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(int) > data.size )
			throw UCEFDataTypeException( "Cannot convert to an Int type" );

		if( data.data )
			return *( (int *)data.data.get() );
		return 0;
	}

	long HLAObject::getAsLong( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(long) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Long type" );

		if( data.data )
			return *( (long *)data.data.get() );
		return 0;
	}

	float HLAObject::getAsFloat( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(float) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Float type" );

		if( data.data )
			return *( (float *)data.data.get() );
		return 0.0f;
	}

	double HLAObject::getAsDouble( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( sizeof(double) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Double type" );

		if( data.data )
			return *( (double *)data.data.get() );
		return 0.0;
	}

	string HLAObject::getAsString( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );

		if( data.data )
			return string( (char *)data.data.get() );

		return "";
	}

	VariableData HLAObject::getRawValue( const string& attributeName ) const
	{
		VariableData data;
		data.data = nullptr;
		data.size = 0;
		auto it = attributeDataStore->find( attributeName );
		if( it != attributeDataStore->end() )
		{
			data = it->second;
		}
		return data;
	}

	vector<string> HLAObject::getAttributeNames() const
	{
		vector<string> attributeNameList;
		HLAObjectAttributes &store = *attributeDataStore;
		for( auto kv : store)
		{
			attributeNameList.emplace_back( kv.first );
		}
		return attributeNameList;
	}

	void HLAObject::clear()
	{
		attributeDataStore->clear();
	}

	std::string HLAObject::getClassName() const
	{
		return className;
	}

	long HLAObject::getInstanceId()
	{
		return instanceId;
	}
}
