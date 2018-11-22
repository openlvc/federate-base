#include "HLAInteraction.h"

#include <string>

#ifndef _WIN32
#include <cstring>
#endif

#include "gov/nist/ucef/util/Logger.h"
#include "RTI/Handle.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{
	HLAInteraction::HLAInteraction
	                    ( const string& interactionClassName ) : m_interactionClassName( interactionClassName )
	{
		m_parameterDataStore = make_shared<HLAInteractionParameters>();
	}

	HLAInteraction::~HLAInteraction()
	{
	}

	bool HLAInteraction::isParameter( const string& attributeName ) const
	{
		bool flag = m_parameterDataStore->find( attributeName ) == m_parameterDataStore->end() ? false : true;
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
		setValue( parameterName, make_shared<short>(val), sizeof(short));
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
		if( m_parameterDataStore->find( parameterName ) != m_parameterDataStore->end() )
		{
			(*m_parameterDataStore)[parameterName].data = data;
			(*m_parameterDataStore)[parameterName].size = size;
		}
		else
		{
			VariableData variableData;
			variableData.data = data;
			variableData.size = size;
			m_parameterDataStore->insert( pair<string, VariableData>( parameterName, variableData) );
		}
	}

	bool HLAInteraction::getAsBool( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (bool *)data.data.get() );
		return false;
	}

	char HLAInteraction::getAsChar( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (char *)data.data.get() );
		return (char) 0;
	}

	short HLAInteraction::getAsShort( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAInteraction::getAsInt( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (int *)data.data.get() );
		return 0;
	}

	long HLAInteraction::getAsLong( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (long *)data.data.get() );
		return 0;
	}

	float HLAInteraction::getAsFloat( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (float *)data.data.get() );
		return 0.0f;
	}

	double HLAInteraction::getAsDouble( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return *( (double *)data.data.get() );
		return 0.0;
	}

	string HLAInteraction::getAsString( const string& parameterName ) const
	{
		VariableData data = getRawValue( parameterName );
		if(data.data)
			return string( (char *)data.data.get() );
		return "";
	}

	VariableData HLAInteraction::getRawValue( const string& parameterName ) const
	{
		VariableData data;
		data.data = nullptr;
		data.size = 0;
		auto it = m_parameterDataStore->find( parameterName );
		if( it != m_parameterDataStore->end() )
		{
			data = it->second;
		}
		return data;
	}

	vector<string> HLAInteraction::getParameterNames() const
	{
		vector<string> paramNameList;
		HLAInteractionParameters &store = *m_parameterDataStore;
		for( auto kv : store)
		{
			paramNameList.emplace_back(kv.first);
		}
		return paramNameList;
	}

	std::string HLAInteraction::getInteractionClassName() const
	{
		return m_interactionClassName;
	}

	void HLAInteraction::clear()
	{
		m_parameterDataStore->clear();
	}
}
