// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

#define WIN32_LEAN_AND_MEAN

#include "org_joy_JoyInterface.h"
#include "windows.h"
#include "mmsystem.h"

#define DEVICES_MAX 16


JNIEXPORT jint JNICALL Java_org_joy_JoyInterface_getMaxDevices (JNIEnv *, jclass) {

	unsigned int devs = joyGetNumDevs();
	return devs > DEVICES_MAX ? DEVICES_MAX : devs;

}


JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_isConnected (JNIEnv *, jclass, jint id) {

	JOYINFO info;
    return joyGetPos(id, &info) == JOYERR_NOERROR ? JNI_TRUE : JNI_FALSE;

}


JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_updateInfo (JNIEnv *jEnv, jclass, jint id, jobject jInfo) {

	JOYCAPSA caps;
	MMRESULT res = joyGetDevCapsA(id, &caps, sizeof(JOYCAPSA));

	if (res != JOYERR_NOERROR) return JNI_FALSE;

	jclass jInfoClass = jEnv->GetObjectClass(jInfo);

	jfieldID fIDname = jEnv->GetFieldID(jInfoClass, "name", "Ljava/lang/String;");		// TODO Get real device name
	jstring jName = jEnv->NewStringUTF(caps.szPname);
	jEnv->SetObjectField(jInfo, fIDname, jName);

	jfieldID fIDpov = jEnv->GetFieldID(jInfoClass, "pov", "Z");
	jEnv->SetBooleanField(jInfo, fIDpov, (caps.wCaps & JOYCAPS_HASPOV) != 0);

	jfieldID fIDbuttons = jEnv->GetFieldID(jInfoClass, "buttons", "I");
	jEnv->SetIntField(jInfo, fIDbuttons, caps.wNumButtons);

	jfieldID fIDaxes = jEnv->GetFieldID(jInfoClass, "axes", "I");
	jEnv->SetIntField(jInfo, fIDaxes, caps.wNumAxes);

	jboolean isCopy;

	jfieldID fIDaxesMaxValues = jEnv->GetFieldID(jInfoClass, "axesMaxValues", "[I");
	jintArray maxsArray = (jintArray) jEnv->GetObjectField(jInfo, fIDaxesMaxValues);
	jint* maxsValues = jEnv->GetIntArrayElements(maxsArray, &isCopy);
	maxsValues[0] = caps.wXmax; maxsValues[1] = caps.wYmax; maxsValues[2] = caps.wZmax;
	maxsValues[3] = caps.wRmax; maxsValues[4] = caps.wUmax; maxsValues[5] = caps.wVmax;
	jEnv->ReleaseIntArrayElements(maxsArray, maxsValues, isCopy ? 0 : JNI_ABORT);

	jfieldID fIDaxesMinValues = jEnv->GetFieldID(jInfoClass, "axesMinValues", "[I");
	jintArray minsArray = (jintArray) jEnv->GetObjectField(jInfo, fIDaxesMinValues);
	jint* minsValues = jEnv->GetIntArrayElements(minsArray, &isCopy);
	minsValues[0] = caps.wXmin; minsValues[1] = caps.wYmin; minsValues[2] = caps.wZmin;
	minsValues[3] = caps.wRmin; minsValues[4] = caps.wUmin; minsValues[5] = caps.wVmin;
	jEnv->ReleaseIntArrayElements(minsArray, minsValues, isCopy ? 0 : JNI_ABORT);

	return JNI_TRUE;

}


// variables to store java classes and field ids that should never change for the same VM
jclass jStateClass;
jfieldID fIDdata;
int idsStored = false;

JNIEXPORT jboolean JNICALL Java_org_joy_JoyInterface_updateState (JNIEnv *jEnv, jclass, jint id, jobject jState) {

	JOYINFOEX pos;
	pos.dwFlags = JOY_RETURNALL;
	pos.dwSize = sizeof(JOYINFOEX);
	MMRESULT res = joyGetPosEx(id, &pos);

	if (res != JOYERR_NOERROR) return JNI_FALSE;

	// get classes and fields ids only if necessary
	if (!idsStored) {
		jStateClass = jEnv->GetObjectClass(jState);
		fIDdata = jEnv->GetFieldID(jStateClass, "data", "[I");
		idsStored = true;
	}

	jboolean isCopy;
	jintArray dataArray = (jintArray) jEnv->GetObjectField(jState, fIDdata);
	jint* dataValues = jEnv->GetIntArrayElements(dataArray, &isCopy);

	dataValues[0] = pos.dwPOV; dataValues[1] = pos.dwButtons;
	dataValues[2] = pos.dwXpos; dataValues[3] = pos.dwYpos; dataValues[4] = pos.dwZpos;
	dataValues[5] = pos.dwRpos; dataValues[6] = pos.dwUpos; dataValues[7] = pos.dwVpos;

	jEnv->ReleaseIntArrayElements(dataArray, dataValues, isCopy ? 0 : JNI_ABORT);

	return JNI_TRUE;

}

