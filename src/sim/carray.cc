//=========================================================================
//
//  CARRAY.CC - part of
//                          OMNeT++
//           Discrete System Simulation in C++
//
//   Member functions of
//    cBag   : expandable vector to store small non-class items
//    cArray : vector to store cObject descendants
//
//  Author: Andras Varga
//
//=========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992,99 Andras Varga
  Technical University of Budapest, Dept. of Telecommunications,
  Stoczek u.2, H-1111 Budapest, Hungary.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include <stdio.h>           // sprintf
#include <string.h>          // memcmp, memcpy, memset
#include "macros.h"
#include "errmsg.h"
#include "carray.h"

//=== Registration
Register_Class( cBag )
Register_Class( cArray )

//==========================================================================
//=== cBag - member functions

#define BOOL     sizeof(bool)
#define PTR(m)   ((char *)vect + (m)*(BOOL+elemsize))
#define ELEM(m)  ((void *)((char *)vect + (m)*(BOOL+elemsize) + BOOL))
#define USED(m)  (*(bool *)((char *)vect + (m)*(BOOL+elemsize)))
#define BLK      (BOOL+elemsize)

cBag::cBag(cBag& bag) : cObject()
{
   vect = NULL;
   setName( bag.name() );
   operator=(bag);
}

cBag::cBag(char *namestr, int esiz, int siz, int delt) :
cObject( namestr ), vect(NULL)
{
     setup(esiz, siz, delt);
}

void cBag::info(char *buf)
{
     cObject::info( buf );

     if( lastused == -1 )
        sprintf( buf+strlen(buf), " (empty)" );
     else
        sprintf( buf+strlen(buf), " (n=%d) ", lastused+1);
}

cBag& cBag::operator=(cBag& bag)
{
     cObject::operator=( bag );
     elemsize = bag.elemsize;
     size = bag.size;
     delta = bag.delta;
     lastused = bag.lastused;
     firstfree = bag.firstfree;
     delete vect;
     vect = new char[ size*BLK ];
     if( vect )  memcpy( vect, bag.vect, size*BLK );
     return *this;
}

void cBag::setup(int esiz, int siz, int delt)
{
   clear();
   elemsize = Max(1,esiz);
   delta = Max(1,delt);
   size = Max(siz,0);
   vect = new char[ size*BLK ];
   for (int i=0; i<size; i++) USED(i)=FALSE;
}

void cBag::clear()
{
   delete vect;
   vect = NULL;
   size = firstfree = 0;
   lastused = -1;
}

int cBag::add(void *obj)
{
   int retval;
   if (firstfree < size) {
      USED(firstfree) = TRUE;
      memcpy( ELEM(firstfree), obj, elemsize);
      retval = firstfree;
      lastused = Max(lastused,firstfree);
      do
         firstfree++;
      while (USED(firstfree) && firstfree<size);
   } else {
      char *v = new char[ (size+delta)*BLK ];
      memcpy(v, vect, size*BLK );
      delete vect;
      vect = v;
      for( int i=size; i<size+delta; i++ ) USED(i)=FALSE;
      USED(size) = TRUE;
      memcpy( ELEM(size), obj, elemsize);
      retval = size;
      lastused = size;
      firstfree = size+1;
      size += delta;
      }
   return retval;
}

int cBag::addAt(int m, void *obj) // --LG
{
   if (m < size) {
      USED(m) = TRUE;
      memcpy( ELEM(m), obj, elemsize);
      lastused = Max(lastused,m);
      if (m==firstfree)
         while (USED(firstfree) && firstfree<size)
            firstfree++;
      return m;
   } else {
      int newsize = Max(size+delta,m+1);
      char *v = new char[ newsize*BLK ];
      memcpy(v, vect, size*BLK );
      delete vect;
      vect = v;
      for( int i=size; i<newsize; i++ ) USED(i)=FALSE;
      USED(m) = TRUE;
      memcpy( ELEM(m), obj, elemsize);
      lastused = m;
      if (m==firstfree)
         while (USED(firstfree) && firstfree<size)
            firstfree++;
      size =newsize;
      return m;
   }
}

int cBag::find(void *obj)
{
     int i;
     for (i=0; i<=lastused; i++)
         if( USED(i) && memcmp( ELEM(i), obj, elemsize)==0 ) break;
     if (i<=lastused)
         return i;
     else
         return -1;
}

void *cBag::get(int m)
{
     if( m>=0 && m<=lastused && USED(m) )
     {
          return ELEM(m);
     }
     else
     {
          opp_warning("(%s)%s: slot #%d empty, returning NULL",
                              className(),fullName(),m);
          return NULL;
     }
}

bool cBag::isUsed(int m)
{
     if( m>=0 && m<=lastused )
         return USED(m);
     else
         return FALSE;
}

void *cBag::remove(int m)
{
     if( m<0 || m>lastused || !USED(m) ) {
          return NULL;
     } else {
          USED(m)=FALSE;
          firstfree = Min(firstfree, m);
          if (m==lastused)
              do {
                 lastused--;
              } while ( !USED(lastused) && lastused>=0);
          return ELEM(m);
     }
}

#undef BOOL
#undef PTR
#undef ELEM
#undef USED
#undef BLK

//==========================================================================
//=== cArray - member functions

cArray::cArray(cArray& list) : cObject()
{
     vect=NULL;
     last=-1;
     setName( list.name() );
     operator=(list);
}

cArray::cArray(char *namestr, int siz, int dt) :
cObject( namestr )
{
     delta = Max(1,dt);
     size = Max(siz,0);
     firstfree = 0;
     last = -1;
     vect = new cObject *[size];
     for (int i=0; i<size; i++) vect[i]=NULL;
}

cArray::~cArray()
{
     // no clear()!
     delete vect;
}

cArray& cArray::operator=(cArray& list)
{
     clear();

     cObject::operator=( list );

     size = list.size;
     delta = list.delta;
     firstfree = list.firstfree;
     last = list.last;
     delete vect;
     vect = new cObject *[size];
     if (vect) memcpy( vect, list.vect, size * sizeof(cObject *) );

     for( int i=0; i<=last; i++)
         if (vect[i] && vect[i]->owner()==&list)
             take( vect[i] = vect[i]->dup() );
     return *this;
}

void cArray::info(char *buf)
{
     cObject::info( buf );

     if( last == -1 )
        sprintf( buf+strlen(buf), " (empty)" );
     else
        sprintf( buf+strlen(buf), " (n=%d)", last+1);
}

void cArray::forEach( ForeachFunc do_fn )
{
     if (do_fn(this,TRUE))
         for (int i=0; i<=last; i++)
         {
             cObject *p = vect[i];
             if (p) p->forEach( do_fn );
         }
     do_fn(this,FALSE);
}

void cArray::clear()
{
     for (int i=0; i<=last; i++)
        if (vect[i] && vect[i]->owner()==this)
           free( vect[i] );
     firstfree = 0;
     last = -1;
}

int cArray::add(cObject *obj)
{
     int retval;
     if (takeOwnership())  take( obj );
     if (firstfree < size)
     {
        vect[firstfree] = obj;
        retval = firstfree;
        last = Max(last,firstfree);
        do {
           firstfree++;
        } while (firstfree<size && vect[firstfree]!=NULL);
     }
     else
     {
        cObject **v = new cObject *[size+delta];
        memcpy(v, vect, sizeof(cObject*)*size );
        memset(v+size, 0, sizeof(cObject*)*delta);
        delete vect;
        vect = v;
        vect[size] = obj;
        retval = last = size;
        firstfree = size+1;
        size += delta;
     }
     return retval;
}

int cArray::addAt(int m, cObject *obj)
{
     if (m<size)  // fits in current vector
     {
        if (m<0)
        {
           opp_error("(%s)%s: addAt(): negative position %d",
                            className(),fullPath(),m);
           return -1;
        }
        if (vect[m]!=NULL)
        {
           opp_error("(%s)%s: addAt(): position %d already used",
                            className(),fullPath(),m);
           return -1;
        }
        vect[m] = obj;
        if (takeOwnership()) take(obj);
        last = Max(m,last);
        if (firstfree==m)
           do {
              firstfree++;
           } while (firstfree<size && vect[firstfree]!=NULL);
     }
     else // must allocate bigger vector
     {
        cObject **v = new cObject *[m+delta];
        memcpy(v, vect, sizeof(cObject*)*size);
        memset(v+size, 0, sizeof(cObject*)*(m+delta-size));
        delete vect;
        vect = v;
        vect[m] = obj;
        if (takeOwnership()) take(obj);
        size = m+delta;
        last = m;
        if (firstfree==m)
           firstfree++;
     }
     return m;
}

int cArray::find(cObject *obj)
{
     int i;
     for (i=0; i<=last; i++)
         if (vect[i]==obj)
            break;
     if (i<=last)
         return i;
     else
         return -1;
}

int cArray::find(char *s)
{
     int i;
     for (i=0; i<=last; i++)
        if (vect[i] && vect[i]->isName(s))
            break;
     if (i<=last)
         return i;
     else
         return -1;
}

cObject *cArray::get(int m)
{
     if (m>=0 && m<=last && vect[m])
          return vect[m];
     else
          {opp_warning(eNULLPTR,className(),fullName(),m);return NO(cObject);}
}

cObject *cArray::get(char *s)
{
    int m = find( s );
    if (m!=-1)
        return get(m);
    else
    {
        opp_warning("(%s)%s: get(): no object called `%s'",className(),fullName(),s);
        return NO(cObject);
    }
}

cObject *cArray::remove(char *s)
{
    int m = find( s );
    if (m!=-1)
        return remove(m);
    else
    {
        opp_warning("(%s)%s: remove(): no object called `%s'",className(),fullName(),s);
        return NO(cObject);
    }
}

cObject *cArray::remove(int m)
{
     if (m<0 || m>last || vect[m]==NULL)
          {opp_warning(eNULLPTR,className(),fullName(),m);return NO(cObject);}
     else
     {
          cObject *obj = vect[m]; vect[m] = NULL;
          firstfree = Min(firstfree, m);
          if (m==last)
              do {
                 last--;
              } while (vect[last]==NULL && last>=0);
          if (obj->owner()==this)  drop( obj );
          return obj;
     }
}

