#include "gov/nist/ucef/hla/base/HLAInteraction.h"

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
	HLAInteraction::HLAInteraction( const string& className ) : interactionClassName( className )
	{
		this->parameterDataStore = make_shared<HLAInteractionParameters>();
	}

	HLAInteraction::~HLAInteraction()
	{
	}

	HLAInteraction::HLAInteraction( const HLAInteraction& hlaInteraction )
	{
		this->interactionClassName = hlaInteraction.interactionClassName;
		this->parameterDataStore = make_shared<HLAInteractionParameters>();
		// if there is any attributes copy across
		auto &parameterStoreFrom = *hlaInteraction.parameterDataStore;
		for( auto item : parameterStoreFrom )
		{
			setValue( item.first, item.second );
		}
	}

	bool HLAInteraction::isPresent( const string& parameterName ) const
	{
		bool flag = parameterDataStore->find( parameterName ) == parameterDataStore->end() ? false : true;
		return flag;
	}

	void HLAInteraction::setValue( const string& parameterName, bool val )
	{
		VariableData data = HLACodecUtils::setAsBool( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, const char val )
	{
		VariableData data = HLACodecUtils::setAsChar( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, const wchar_t val )
	{
		VariableData data = HLACodecUtils::setAsWChar( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValueAsByte( const string& parameterName, const char val )
	{
		VariableData data = HLACodecUtils::setAsByte( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, short val )
	{
		VariableData data = HLACodecUtils::setAsShort( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, int val )
	{
		VariableData data = HLACodecUtils::setAsInt( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, long val )
	{
		VariableData data = HLACodecUtils::setAsLong( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, float val )
	{
		VariableData data = HLACodecUtils::setAsFloat( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, double val )
	{
		VariableData data = HLACodecUtils::setAsDouble( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, const string& val )
	{
		VariableData data = HLACodecUtils::setAsString( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, const wstring& val )
	{
		VariableData data = HLACodecUtils::setAsWString( val );
		setValue( parameterName, data );
	}

	void HLAInteraction::setValue( const string& parameterName, VariableData& data )
	{
		if( parameterDataStore->find( parameterName ) != parameterDataStore->end() )
		{
			(*parameterDataStore)[parameterName].data = data.data;
			(*parameterDataStore)[parameterName].size = data.size;
		}
		else
		{
			parameterDataStore->insert( pair<string, VariableData>( parameterName, data) );
		}
	}

	bool HLAInteraction::getAsBool( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsBool( data );
	}

	char HLAInteraction::getAsChar( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsChar( data );
	}

	wchar_t HLAInteraction::getAsWChar( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsWChar( data );
	}

	char HLAInteraction::getAsByte( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsByte( data );
	}

	short HLAInteraction::getAsShort( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsShort( data );
	}

	int HLAInteraction::getAsInt( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsInt( data );
	}

	long HLAInteraction::getAsLong( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsLong( data );
	}

	float HLAInteraction::getAsFloat( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsFloat( data );
	}

	double HLAInteraction::getAsDouble( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsDouble( data );
	}

	string HLAInteraction::getAsString( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsString( data );
	}

	wstring HLAInteraction::getAsWString( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		return HLACodecUtils::getAsWString( data );
	}

	VariableData HLAInteraction::getRawValue( const string& parameterName ) const
	{
		VariableData data;
		data.data = nullptr;
		data.size = 0;
		auto it = parameterDataStore->find( parameterName );
		if( it != parameterDataStore->end() )
		{
			data = it->second;
		}
		return data;
	}

	vector<string> HLAInteraction::getParameterNames() const
	{
		vector<string> paramNameList;
		HLAInteractionParameters &store = *parameterDataStore;
		for( auto kv : store)
		{
			paramNameList.emplace_back(kv.first);
		}
		return paramNameList;
	}

	std::string HLAInteraction::getInteractionClassName() const
	{
		return interactionClassName;
	}

	void HLAInteraction::clear()
	{
		parameterDataStore->clear();
	}
}
