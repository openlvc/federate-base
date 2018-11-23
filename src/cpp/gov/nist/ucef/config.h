#pragma once

#ifdef _WIN32
	#ifdef UCEF_EXPORTS
		#define UCEF_API __declspec(dllexport)
	#else
		#define UCEF_API __declspec(dllimport)
	#endif
#else
	#define UCEF_API
#endif
