// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include "org_joy_JoyInterface.h"
#include "linux/joystick.h"


#define AXIS_MIN -32767
#define AXIS_MAX 32767
#define DEVICES_MAX 16
#define EVENT_READ_MAX 64

#define ACCEPT_MIN_AXES 0		// Maybe only POV as buttons?
#define ACCEPT_MAX_AXES 8
#define ACCEPT_MIN_BUTTONS 0	// Maybe only analog triggers?
#define ACCEPT_MAX_BUTTONS 24


typedef struct {
	char buttons = -1;
	char axes = -1;
	char name[64];
	int	fd = -1;
} Info;

typedef struct {
	int buttons = 0;
	int axes[6];
} State;


Info infos[DEVICES_MAX];
State states[DEVICES_MAX];


int connectDeviceAndUpdateInfo(int id) {

	if (id < 0 || id > DEVICES_MAX) return false;

	Info *info = &infos[id];
	if (info->fd >= 0) return true;		// Already connected

	char dev[32];
	sprintf(dev, "/dev/input/js%d", id);

	int fd = open(dev, O_RDONLY);
	if (fd < 0) return false;		// cannot open device, unacceptable

	char axes = -1;
	int io = ioctl(fd, JSIOCGAXES, &axes);
	if (io < 0) return false;		// cannot get number of axes, unacceptable

	char buttons = -1;
	io = ioctl(fd, JSIOCGBUTTONS, &buttons);
	if (io < 0) return false;		// cannot get number of buttons, unacceptable

	char name[sizeof(info->name)];
	io = ioctl(fd, JSIOCGNAME(sizeof(name)), name);
	if (io < 0) sprintf(name, "Unknown");		// cannot get name, ok...
	else name[sizeof(name) - 1] = 0;			// terminate name string just to be sure

	// Check for acceptabe vaues
	if (axes < ACCEPT_MIN_AXES || axes > ACCEPT_MAX_AXES) return false;
	if (buttons < ACCEPT_MIN_BUTTONS || buttons > ACCEPT_MAX_BUTTONS) return false;

	// Everything ok, lets store the info permanently
	info->axes = axes;
	info->buttons = buttons;
	sprintf(info->name, "%s", name);
	info->fd = fd;

	return true;

}


int updateState(int id) {

	if (id < 0 || id > DEVICES_MAX) return false;

	if (infos[id].fd < 0) return false;		// Not Connected

	State *state = &states[id];

    struct js_event event;
	int fd = infos[id].fd;

    int sel;
    fd_set rfds;
    struct timeval timeout;
    FD_ZERO(&rfds);
    FD_SET(fd, &rfds);

    int c = 0;
    for(; c < EVENT_READ_MAX; c++) {

    	timeout.tv_sec = timeout.tv_usec = 0;	// will return immediately
        sel = select(fd + 1, &rfds, NULL, NULL, &timeout);
        if (sel != 1) return errno >= 0;

        TEMP_FAILURE_RETRY(read (fd, &event, sizeof(struct js_event)));
        if (errno < 0) return false;

        if ((event.type & JS_EVENT_BUTTON) != 0) {
            if (event.value == 1) state->buttons |= (1 << event.number);
            else state->buttons &= ~(1 << event.number);
        }
        else if ((event.type & JS_EVENT_AXIS) != 0)
        	state->axes[event.number] = event.value;
    }

    return true;

}


JNIEXPORT jint JNICALL Java_org_joy_JoyInterface_getMaxDevices (JNIEnv *, jclass) {
	return (jint) DEVICES_MAX;
}


JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_isConnected (JNIEnv *, jclass, jint id) {

	if (!connectDeviceAndUpdateInfo(id)) return JNI_FALSE;
	if (!updateState(id)) return JNI_FALSE;
	return JNI_TRUE;

}


JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_updateInfo (JNIEnv *jEnv, jclass, jint id, jobject jInfo) {

	if (!connectDeviceAndUpdateInfo(id)) return JNI_FALSE;

	Info *info = &infos[id];

	jclass jInfoClass = jEnv->GetObjectClass(jInfo);

	jfieldID fIDname = jEnv->GetFieldID(jInfoClass, "name", "Ljava/lang/String;");
	jstring jName = jEnv->NewStringUTF(info->name);
	jEnv->SetObjectField(jInfo, fIDname, jName);

	jfieldID fIDpov = jEnv->GetFieldID(jInfoClass, "pov", "Z");
	jEnv->SetBooleanField(jInfo, fIDpov, 0);		// no POV concept in Linux driver

	jfieldID fIDbuttons = jEnv->GetFieldID(jInfoClass, "buttons", "I");
	jEnv->SetIntField(jInfo, fIDbuttons, info->buttons);

	jfieldID fIDaxes = jEnv->GetFieldID(jInfoClass, "axes", "I");
	jEnv->SetIntField(jInfo, fIDaxes, info->axes);

	jboolean isCopy;

	jfieldID fIDaxesMaxValues = jEnv->GetFieldID(jInfoClass, "axesMaxValues", "[I");
	jintArray maxsArray = (jintArray) jEnv->GetObjectField(jInfo, fIDaxesMaxValues);
	jint* maxsValues = jEnv->GetIntArrayElements(maxsArray, &isCopy);
	maxsValues[0] = maxsValues[1] = maxsValues[2] = maxsValues[3] = maxsValues[4] = maxsValues[5] = AXIS_MAX;
	jEnv->ReleaseIntArrayElements(maxsArray, maxsValues, isCopy ? 0 : JNI_ABORT);

	jfieldID fIDaxesMinValues = jEnv->GetFieldID(jInfoClass, "axesMinValues", "[I");
	jintArray minsArray = (jintArray) jEnv->GetObjectField(jInfo, fIDaxesMinValues);
	jint* minsValues = jEnv->GetIntArrayElements(minsArray, &isCopy);
	minsValues[0] = minsValues[1] = minsValues[2] = minsValues[3] = minsValues[4] = minsValues[5] = AXIS_MIN;
	jEnv->ReleaseIntArrayElements(minsArray, minsValues, isCopy ? 0 : JNI_ABORT);

	return JNI_TRUE;
}


// variables to store java classes and field ids that should never change for the same VM
jclass jStateClass;
jfieldID fIDdata;
int idsStored = false;

JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_updateState (JNIEnv *jEnv, jclass, jint id, jobject jState) {

	if (!updateState(id)) return JNI_FALSE;		// not connected or error updating state

	// get classes and fields ids only if necessary
	if (!idsStored) {
		jStateClass = jEnv->GetObjectClass(jState);
		fIDdata = jEnv->GetFieldID(jStateClass, "data", "[I");
		idsStored = true;
	}

	jboolean isCopy;
	jintArray dataArray = (jintArray) jEnv->GetObjectField(jState, fIDdata);
	jint* dataValues = jEnv->GetIntArrayElements(dataArray, &isCopy);

	State *state = &states[id];

	dataValues[0] = 0;		// no POV concept in Linux driver
	dataValues[1] = state->buttons;
	dataValues[2] = state->axes[0]; dataValues[3] = state->axes[1]; dataValues[4] = state->axes[2];
	dataValues[5] = state->axes[3]; dataValues[6] = state->axes[4]; dataValues[7] = state->axes[5];

	jEnv->ReleaseIntArrayElements(dataArray, dataValues, isCopy ? 0 : JNI_ABORT);

	return JNI_TRUE;
}
