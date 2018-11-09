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
			void setAttributeValue( std::string& attributeName, bool val );
			void setAttributeValue( std::string& attributeName, char val );
			void setAttributeValue( std::string& attributeName, short val );
			void setAttributeValue( std::string& attributeName, int val );
			void setAttributeValue( std::string& attributeName, long val );
			void setAttributeValue( std::string& attributeName, float val );
			void setAttributeValue( std::string& attributeName, double val );
			void setAttributeValue( std::string& attributeName, std::string& val );

			std::string getClassName();
			HLAObjectAttributeMap* getAttributeMap();
			std::shared_ptr<rti1516e::ObjectInstanceHandle> getInstanceHandle();
		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			void pushToMap( std::string& attributeName, std::string& data );
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			HLAObjectAttributeMap attributeData;
			std::string m_className;
			std::shared_ptr<rti1516e::ObjectInstanceHandle> m_instanceHandle;
	};
}

