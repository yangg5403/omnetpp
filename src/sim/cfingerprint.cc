//==========================================================================
//   CFINGERPRINT.CC  - part of
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//  Author: Andras Varga
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2005 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include "cfingerprint.h"


void cFingerprint::add(const char *p, size_t length)
{
    // add the bulk in 4-byte chunks
    size_t lengthmod4 = length&~3U;
    size_t i;
    for (i=0; i<lengthmod4; i+=4)
        merge((uint32)(p[i] | (p[i+1]<<8) | (p[i+2]<<16) | (p[i+3]<<24)));

    // add the 1, 2 or 3 bytes left
    switch (length-i)
    {
        case 0: break;
        case 1: merge((uint32)(p[i])); break;
        case 2: merge((uint32)(p[i] | (p[i+1]<<8))); break;
        case 3: merge((uint32)(p[i] | (p[i+1]<<8) | (p[i+2]<<16))); break;
        default: ASSERT(false);
    }
}

/* XXX to test case

int main(int argc, char **argv)
{
#define PRINT printf("%x\n", h.getHash())
    cFingerprint h;
    h.add(1); PRINT;
    h.add(4); PRINT;
    h.add(1); PRINT;
    h.add(0xffffffff); PRINT;
    h.add(0); h.add(0); h.add(0); h.add(0); PRINT;

    h.reset();
    h.add("alma"); PRINT;

    h.reset();
    h.add("almaalma"); PRINT;

    h.reset();
    h.add("almaalmaalma"); PRINT;
    return 0;
}
*/

