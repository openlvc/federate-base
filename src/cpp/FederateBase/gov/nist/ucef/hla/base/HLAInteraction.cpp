#include "HLAInteraction.h"

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
			setValue( item.first, item.second.data, item.second.size );
		}
	}

	bool HLAInteraction::isPresent( const string& attributeName ) const
	{
		bool flag = parameterDataStore->find( attributeName ) == parameterDataStore->end() ? false : true;
		return flag;
	}

	void HLAInteraction::setValue( const string& parameterName, bool val )
	{
		setValue( parameterName, make_shared<bool>(val), sizeof(bool) );
	}

	void HLAInteraction::setValue( const string& parameterName, const char val )
	{
		setValue( parameterName, make_shared<char>(val), sizeof(char) );
	}

	void HLAInteraction::setValue( const string& parameterName, short val )
	{
		setValue( parameterName, make_shared<short>(val), sizeof(short) );
	}

	void HLAInteraction::setValue( const string& parameterName, int val )
	{
		setValue( parameterName, make_shared<int>(val), sizeof(int) );
	}

	void HLAInteraction::setValue( const string& parameterName, long val )
	{
		setValue( parameterName, make_shared<long>(val), sizeof(long) );
	}

	void HLAInteraction::setValue( const string& parameterName, float val )
	{
		setValue( parameterName, make_shared<float>(val), sizeof(float) );
	}

	void HLAInteraction::setValue( const string& parameterName, double val )
	{
		setValue( parameterName, make_shared<double>(val), sizeof(double) );
	}

	void HLAInteraction::setValue( const string& parameterName, const string& val )
	{
		shared_ptr<char> arr(new char[val.length() + 1](), [](char *p) { delete [] p; });
		strcpy(arr.get(), val.c_str());
		setValue( parameterName, arr, val.length() + 1 );
	}

	void HLAInteraction::setValue( const string& parameterName,
	                               shared_ptr<void> data,
	                               const size_t size )
	{
		if( parameterDataStore->find( parameterName ) != parameterDataStore->end() )
		{
			(*parameterDataStore)[parameterName].data = data;
			(*parameterDataStore)[parameterName].size = size;
		}
		else
		{
			VariableData variableData;
			variableData.data = data;
			variableData.size = size;
			parameterDataStore->insert( pair<string, VariableData>( parameterName, variableData) );
		}
	}

	bool HLAInteraction::getAsBool( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(bool) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Bool type" );

		if( data.data )
			return *( (bool *)data.data.get() );
		return false;
	}

	char HLAInteraction::getAsChar( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(char) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Char type" );

		if( data.data )
			return *( (char *)data.data.get() );

		return (char) 0;
	}

	short HLAInteraction::getAsShort( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(short) > data.size )
			throw UCEFDataTypeException("Cannot convert to a Short type");

		if( data.data )
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAInteraction::getAsInt( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(int) > data.size )
			throw UCEFDataTypeException( "Cannot convert to an Int type" );

		if( data.data )
			return *( (int *)data.data.get() );

		return 0;
	}

	long HLAInteraction::getAsLong( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(long) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Long type" );

		if( data.data )
			return *( (long *)data.data.get() );

		return 0;
	}

	float HLAInteraction::getAsFloat( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(float) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Float type" );

		if( data.data )
			return *( (float *)data.data.get() );

		return 0.0f;
	}

	double HLAInteraction::getAsDouble( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );

		if( sizeof(double) > data.size )
			throw UCEFDataTypeException( "Cannot convert to a Double type" );

		if( data.data )
			return *( (double *)data.data.get() );

		return 0.0;
	}

	string HLAInteraction::getAsString( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if( data.data )
			return string( (char *)data.data.get() );
		return "";
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
