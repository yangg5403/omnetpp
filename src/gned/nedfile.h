//==========================================================================
//   NEDFILE.H -
//            part of GNED, the graphical network editor of
//                             OMNeT++
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2001 Andras Varga
  Technical University of Budapest, Dept. of Telecommunications,
  Stoczek u.2, H-1111 Budapest, Hungary.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `terms' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __NEDFILE_H
#define __NEDFILE_H

#include "parsened.h"   // for YYLTYPE

enum {COMMENT_LINE, BLANK_LINE, CODE_LINE};

class NEDFile
{
  private:
    char *wholeFile;

    int numLines;
    char **lineBeg;

    char *end;
    char savedChar;

    char *commentBuf;
    int commentBufLen;

  private:
    int lineType(char *s);
    int lineContainsCode(char *s);
    int getIndent(char *s);
    int lastColumn(char *s);
    char *getPosition(int line, int column);

    bool indexLines();
    int topLineOfBannerComment(int li);
    char *stripComment(char *s, int numlines);

  public:
    NEDFile();
    ~NEDFile();

    bool readFile(const char *filename);
    bool setData(const char *data);

    char *get(YYLTYPE pos);

    char *getFileComment();
    char *getBannerComment(YYLTYPE pos);
    char *getTrailingComment(YYLTYPE pos);
};

#endif

