#include "HLAInteraction.h"

#include <string>
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

	void HLAInteraction::setParameterValue( const string& parameterName, bool val )
	{
		setParameterValue( parameterName, make_shared<bool>(val), sizeof(bool) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, const char val )
	{
		setParameterValue( parameterName, make_shared<char>(val), sizeof(char) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, short val )
	{
		setParameterValue( parameterName, make_shared<short>(val), sizeof(short));
	}

	void HLAInteraction::setParameterValue( const string& parameterName, int val )
	{
		setParameterValue( parameterName, make_shared<int>(val), sizeof(int) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, long val )
	{
		setParameterValue( parameterName, make_shared<long>(val), sizeof(long) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, float val )
	{
		setParameterValue( parameterName, make_shared<float>(val), sizeof(float) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, double val )
	{
		setParameterValue( parameterName, make_shared<double>(val), sizeof(double) );
	}

	void HLAInteraction::setParameterValue( const string& parameterName, const string& val )
	{
		shared_ptr<char> arr(new char[val.length() + 1](), [](char *p) { delete [] p; });
		strcpy_s(arr.get(), val.length() + 1, val.c_str());
		setParameterValue( parameterName, arr, val.length() + 1 );
	}

	void HLAInteraction::setParameterValue( const string& parameterName,
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

	bool HLAInteraction::getParameterValueAsBool( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (bool *)data.data.get() );
		return false;
	}

	char HLAInteraction::getParameterValueAsChar( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (char *)data.data.get() );
		return (char) 0;
	}

	short HLAInteraction::getParameterValueAsShort( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (short *)data.data.get() );
		return 0;
	}

	int HLAInteraction::getParameterValueAsInt( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (int *)data.data.get() );
		return 0;
	}

	long HLAInteraction::getParameterValueAsLong( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (long *)data.data.get() );
		return 0;
	}

	float HLAInteraction::getParameterValueAsFloat( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (float *)data.data.get() );
		return 0.0f;
	}

	double HLAInteraction::getParameterValueAsDouble( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return *( (double *)data.data.get() );
		return 0.0;
	}

	string HLAInteraction::getParameterValueAsString( const string& parameterName ) const
	{
		VariableData data = getParameterValue( parameterName );
		if(data.data)
			return string( (char *)data.data.get() );
		return "";
	}

	VariableData HLAInteraction::getParameterValue( const string& parameterName ) const
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

	void HLAInteraction::clearParameterDataStore()
	{
		m_parameterDataStore->clear();
	}

	std::string HLAInteraction::getInteractionClassName() const
	{
		return m_interactionClassName;
	}
}
