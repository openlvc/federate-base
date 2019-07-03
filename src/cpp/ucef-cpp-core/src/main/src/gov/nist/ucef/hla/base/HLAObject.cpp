#include "gov/nist/ucef/hla/base/HLAObject.h"

#include <cstring>
#include <string>

#include "gov/nist/ucef/util/HLACodecUtils.h"
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
		this->instanceId = INVALID_LONG;
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
			setValue( item.first, item.second );
		}
	}

	
	bool HLAObject::isPresent( const string& attributeName ) const
	{
		bool flag = attributeDataStore->find( attributeName ) == attributeDataStore->end() ? false : true;
		return flag;
	}

	void HLAObject::setValue( const string& attributeName, bool val )
	{
		VariableData data = HLACodecUtils::setAsBool( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, const char val )
	{
		VariableData data = HLACodecUtils::setAsChar( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, const wchar_t val )
	{
		VariableData data = HLACodecUtils::setAsWChar( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValueAsByte( const string& attributeName, const char val )
	{
		VariableData data = HLACodecUtils::setAsByte( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, short val )
	{
		VariableData data = HLACodecUtils::setAsShort( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, int val )
	{
		VariableData data = HLACodecUtils::setAsInt( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, long val )
	{
		VariableData data = HLACodecUtils::setAsLong( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, float val )
	{
		VariableData data = HLACodecUtils::setAsFloat( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, double val )
	{
		VariableData data = HLACodecUtils::setAsDouble( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, const string& val )
	{
		VariableData data = HLACodecUtils::setAsString( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, const wstring& val )
	{
		VariableData data = HLACodecUtils::setAsWString( val );
		setValue( attributeName, data );
	}

	void HLAObject::setValue( const string& attributeName, VariableData& data  )
	{
		auto it = attributeDataStore->find( attributeName );
		if( it != attributeDataStore->end() )
		{
			it->second.data = data.data;
			it->second.size = data.size;
		}
		else
		{ 
			attributeDataStore->insert( pair<string, VariableData>( attributeName, data) );
		}
	}

	void HLAObject::setInstanceId( long hash )
	{
		this->instanceId = hash;
	}

	bool HLAObject::getAsBool( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsBool( data );
	}

	char HLAObject::getAsByte( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsByte( data );
	}

	char HLAObject::getAsChar( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsChar( data );
	}

	wchar_t HLAObject::getAsWChar( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsWChar( data );
	}

	short HLAObject::getAsShort( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsShort( data );
	}

	int HLAObject::getAsInt( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsInt( data );
	}

	long HLAObject::getAsLong( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsLong( data );
	}

	float HLAObject::getAsFloat( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsFloat( data );
	}

	double HLAObject::getAsDouble( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsDouble( data );
	}

	string HLAObject::getAsString( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsString( data );
	}

	wstring HLAObject::getAsWString( const string& attributeName ) const
	{
		VariableData data = getRawValue( attributeName );
		return HLACodecUtils::getAsWString( data );
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
