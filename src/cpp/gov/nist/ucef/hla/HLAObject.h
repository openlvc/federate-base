#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include <map>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	typedef std::map<std::string, util::VariableData> HLAObjectAttributes;
	/**
	 * @file HLAObject.h
	 *
	 */
	class UCEF_API HLAObject
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			HLAObject( const std::string& className, std::shared_ptr<rti1516e::ObjectInstanceHandle>& instanceHandle );
			virtual ~HLAObject();
			HLAObject( const HLAObject& ) = delete;
			HLAObject& operator=( const HLAObject& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------
			void setAttributeValue( const std::string& attributeName, bool val );
			void setAttributeValue( const std::string& attributeName, char val );
			void setAttributeValue( const std::string& attributeName, short val );
			void setAttributeValue( const std::string& attributeName, int val );
			void setAttributeValue( const std::string& attributeName, long val );
			void setAttributeValue( const std::string& attributeName, float val );
			void setAttributeValue( const std::string& attributeName, double val );
			void setAttributeValue( const std::string& attributeName, const std::string& val );

			bool getAttributeAsBool( const std::string& attributeName );
			char getAttributeAsChar( const std::string& attributeName );
			short getAttributeAsShort( const std::string& attributeName );
			int getAttributeAsInt( const std::string& attributeName );
			long getAttributeAsLong( const std::string& attributeName );
			float getAttributeAsFloat( const std::string& attributeName );
			double getAttributeAsDouble( const std::string& attributeName );
			std::string getAttributeAsString( const std::string& attributeName );

			std::shared_ptr<HLAObjectAttributes> getAttributeDataStore();
			void clearAttributeDataStore();
			std::string getClassName();
			std::shared_ptr<rti1516e::ObjectInstanceHandle> getInstanceHandle();
		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			void pushToAttributeStore( const std::string& attributeName,
			                           std::shared_ptr<void> data,
			                           const size_t size );
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<HLAObjectAttributes> m_attributeDataStore;
			std::string m_className;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> m_instanceHandle;
	};
}

