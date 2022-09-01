#include "com_back_isobus_CanBUS_socket.h"


unsigned char asc2nibble(char c) {
	if ((c >= '0') && (c <= '9'))
		return c - '0';
	if ((c >= 'A') && (c <= 'F'))
		return c - 'A' + 10;
	if ((c >= 'a') && (c <= 'f'))
		return c - 'a' + 10;
	return 16; /* error */
}



JNIEXPORT jint JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canOpenRaw (JNIEnv *env, jobject obj, jstring can_java) {
    
	const char *can_interface = (*env)->GetStringUTFChars(env, can_java, 0); 
	struct ifreq ifr;
	struct sockaddr_can addr;
    int s;
    
	if ((s = socket(PF_CAN, SOCK_RAW, CAN_RAW)) < 0) {
		return -1;
	}
	strcpy(ifr.ifr_name, can_interface);
	ioctl(s, SIOCGIFINDEX, &ifr);

	memset(&addr, 0, sizeof(addr));
	addr.can_family = AF_CAN;
	addr.can_ifindex = ifr.ifr_ifindex;

	if(bind(s, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
		return -1;
	}
	fprintf(stderr, "%s interface opened successfully!\n", can_interface);
	return s;
}


JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canClose (JNIEnv *env, jobject obj, jint fd) {
    
    close((int)fd);
    fprintf(stderr, "Can interface closed.\n");

    return true;
}


JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canSendFrame
  (JNIEnv *env, jobject obj, jint fd, jlong id, jstring pay, jint pay_len) {
      
	struct can_frame frame;
	memset(&frame, 0, sizeof(struct can_frame));
	const char *payload = (*env)->GetStringUTFChars(env, pay, 0); 

	frame.can_id = (unsigned long)id + (0x1 << 31); //flag para id extendido
	frame.can_dlc = (int)pay_len;

	for (int i = 0; i < frame.can_dlc *2 ; i+=2)
		frame.data[i/2] = (asc2nibble(payload[i]) << 4) + asc2nibble(payload[i+1]);

	if (write(fd, &frame, 16) < 0)
    	return 0;

	return 1;
}


JNIEXPORT jboolean JNICALL Java_com_back_isobus_CanBUS_1socket__1_1canReceiveFrame (JNIEnv *env, jobject obj, jint fd, jobject can_frame) {

	jclass canframeClass = (*env)->GetObjectClass(env, can_frame);

	jfieldID fid_id = (*env)->GetFieldID(env, canframeClass, "id", "J");
	if (NULL == fid_id) return false;

	jfieldID fid_payload = (*env)->GetFieldID(env, canframeClass, "payload", "Ljava/lang/String;");
	if (NULL == fid_payload) return false;

	jstring val_payload = (*env)->GetObjectField(env, canframeClass, fid_payload);
	jint val_id = (*env)->GetIntField(env, canframeClass, fid_id);

    int nbytes;
    unsigned long long content = 0;
    char frame_pay[18];
    struct can_frame cf;

    if ((nbytes = read(fd, &cf, sizeof(cf))) < 0) {
		fprintf(stderr, "Error reading");
		return false; /* quit */
	}
    val_id = cf.can_id&0x1FFFFFFF;

    for (int i = 0; i < cf.can_dlc; i++) {
    	content = content << 8;
    	content |= cf.data[cf.can_dlc-i-1];
    }

    sprintf(frame_pay, "%02x%02x%02x%02x%02x%02x%02x%02x\n", cf.data[0],  cf.data[1],  cf.data[2],  cf.data[3],  cf.data[4],  cf.data[5],  cf.data[6],  cf.data[7]);
    fprintf(stderr, "%02x%02x%02x%02x%02x%02x%02x%02x\n", cf.data[0],  cf.data[1],  cf.data[2],  cf.data[3],  cf.data[4],  cf.data[5],  cf.data[6],  cf.data[7]);
    val_payload = (*env)->NewStringUTF(env, frame_pay);

    (*env)->SetIntField(env, can_frame, fid_id, val_id);
    (*env)->SetObjectField(env, can_frame, fid_payload, val_payload);

    return true;
}
