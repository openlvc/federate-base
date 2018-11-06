#pragma once

#ifdef UCEF_EXPORTS
    #define UCEF_API __declspec(dllexport)
#else
    #define UCEF_API __declspec(dllimport)
#endif