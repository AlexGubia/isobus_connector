#!/bin/bash

case $1 in
	-d)
		sudo /sbin/ip link set $2 down
		;;
	-a)
		sudo /sbin/ip link set $2 up type can bitrate $3
		;;
	-i)	sudo ip link add dev $2 type vcan
		sudo ip link set up $2
		;;
esac
