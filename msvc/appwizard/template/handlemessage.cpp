//-------------------------------------------------------------
// file: $$MODULE_NAME$$.cpp
//-------------------------------------------------------------

#include <omnetpp.h>


// $$MODULE_NAME$$ simple module class
//
// $$DESCRIPTION$$
//
class $$MODULE_NAME$$ : public cSimpleModule
{
    // retrieved module parameters and pre-calculated data 
    const char *first_param;
    double second_param;

    // module state variables
    enum {IDLE,BUSY} module_state;  // just an example

    // summary result data to be stored from finish()
    int total_msgs;  // just an example

    // member functions
    Module_Class_Members($$MODULE_NAME$$,cSimpleModule,0)
    virtual void initialize();
    virtual void handleMessage();
    virtual void finish();
};

Define_Module( $$MODULE_NAME$$ );

void $$MODULE_NAME$$::initialize()
{
    // initialize class data members
    first_param = par("first_param");
    second_param = par("second_param");    
    module_state = IDLE;
    total_msgs = 0;

    // schedule first message (if needed)
    // ...
}

void $$MODULE_NAME$$::handleMessage(cMessage *msg)
{
    // handle the message according to the simple module algorithm
    // ...
}

void $$MODULE_NAME$$::finish()
{
    // save summary results (if any)
    writeScalar("total_msgs", total_msgs);
}
