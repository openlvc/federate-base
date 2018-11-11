#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include <map>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	typedef std::map<std::string, std::string> HLAObjectAttributeMap;
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
			void resetAttributeValues();
			std::string getClassName();
			HLAObjectAttributeMap getAttributeMap();
			std::shared_ptr<rti1516e::ObjectInstanceHandle> getInstanceHandle();
		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			void pushToMap( const std::string& attributeName, const std::string& data );
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			HLAObjectAttributeMap attributeData;
			std::string m_className;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> m_instanceHandle;
	};
}

