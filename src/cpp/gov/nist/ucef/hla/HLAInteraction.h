#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include <map>
#include <mutex>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	typedef std::map<std::string, util::VariableData> HLAInteractionParameters;
	/**
	 * @file HLAInteraction.h
	 *
	 */
	class UCEF_API HLAInteraction
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			HLAInteraction( const std::string& interactionClassName );
			virtual ~HLAInteraction();
			HLAInteraction( const HLAInteraction& ) = delete;
			HLAInteraction& operator=( const HLAInteraction& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------
			void setParameterValue( const std::string& parameterName, bool val );
			void setParameterValue( const std::string& parameterName, const char val );
			void setParameterValue( const std::string& parameterName, short val );
			void setParameterValue( const std::string& parameterName, int val );
			void setParameterValue( const std::string& parameterName, long val );
			void setParameterValue( const std::string& parameterName, float val );
			void setParameterValue( const std::string& parameterName, double val );
			void setParameterValue( const std::string& parameterName, const std::string& val );
			void setParameterValue( const std::string& parameterName,
			                        std::shared_ptr<void> data,
			                        const size_t size );

			bool getParameterValueAsBool( const std::string& parameterName ) const;
			char getParameterValueAsChar( const std::string& parameterName ) const;
			short getParameterValueAsShort( const std::string& parameterName ) const;
			int getParameterValueAsInt( const std::string& parameterName ) const;
			long getParameterValueAsLong( const std::string& parameterName ) const;
			float getParameterValueAsFloat( const std::string& parameterName ) const;
			double getParameterValueAsDouble( const std::string& parameterName ) const;
			std::string getParameterValueAsString( const std::string& parameterName ) const;
			util::VariableData getParameterValue( const std::string& parameterName ) const;

			std::vector<std::string> getParameterNames() const;
			void clearParameterDataStore();
			std::string getInteractionClassName() const;
		private:

			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<HLAInteractionParameters> m_parameterDataStore;
			std::string m_interactionClassName;
	};
}

