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
			HLAObject( const std::string& objectClassName,
			           std::shared_ptr<rti1516e::ObjectInstanceHandle>& instanceHandle );
			virtual ~HLAObject();
			HLAObject( const HLAObject& ) = delete;
			HLAObject& operator=( const HLAObject& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------
			void setAttributeValue( const std::string& attributeName, bool val );
			void setAttributeValue( const std::string& attributeName, const char val );
			void setAttributeValue( const std::string& attributeName, short val );
			void setAttributeValue( const std::string& attributeName, int val );
			void setAttributeValue( const std::string& attributeName, long val );
			void setAttributeValue( const std::string& attributeName, float val );
			void setAttributeValue( const std::string& attributeName, double val );
			void setAttributeValue( const std::string& attributeName, const std::string& val );
			void setAttributeValue( const std::string& attributeName,
			                        std::shared_ptr<void> data,
			                        const size_t size );

			bool getAttributeValueAsBool( const std::string& attributeName ) const;
			char getAttributeValueAsChar( const std::string& attributeName ) const;
			short getAttributeValueAsShort( const std::string& attributeName ) const;
			int getAttributeValueAsInt( const std::string& attributeName ) const;
			long getAttributeValueAsLong( const std::string& attributeName ) const;
			float getAttributeValueAsFloat( const std::string& attributeName ) const;
			double getAttributeValueAsDouble( const std::string& attributeName ) const;
			std::string getAttributeValueAsString( const std::string& attributeName ) const;
			std::vector<std::string> getAttributeNameList() const;

			util::VariableData getAttributeValue( const std::string& attributeName ) const;
			void clearAttributeDataStore();
			std::string getClassName() const;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> getInstanceHandle();
		private:

			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<HLAObjectAttributes> m_attributeDataStore;
			std::string m_className;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> m_instanceHandle;
			mutable std::mutex m_threadSafeLock;
	};
}

