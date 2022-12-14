/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <ctype.h>
#include <errno.h>
#include <libgen.h>
#include <signal.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <net/if.h>
#include <sys/epoll.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include <math.h>

#include <sys/syscall.h>
/* Header for class com_back_isobus_CanBUS_socket */

#ifndef _Included_com_back_isobus_CanBUS_socket
#define _Included_com_back_isobus_CanBUS_socket
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_back_isobus_CanBUS_socket
 * Method:    __canOpenRaw
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canOpenRaw
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_back_isobus_CanBUS_socket
 * Method:    __canOpenBCM
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canOpenBCM
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_back_isobus_CanBUS_socket
 * Method:    __canClose
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canClose
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_back_isobus_CanBUS_socket
 * Method:    __canSendFrame
 * Signature: (IJLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canSendFrame
  (JNIEnv *, jobject, jint, jlong, jstring, jint);

/*
 * Class:     com_back_isobus_CanBUS_socket
 * Method:    __canReceiveFrame
 * Signature: (ILcom/back/isobus/CANframe;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canReceiveFrame
  (JNIEnv *, jobject, jint, jobject);

#ifdef __cplusplus
}
#endif
#endif
