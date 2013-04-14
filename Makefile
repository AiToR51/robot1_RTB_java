#Hecho a partir del Makefile del robot de Xuco86

compile:
	javac roboAitor/*.java

configure:
	#null

install:
	cp -rf roboAitor /usr/lib/realtimebattle/Robots
	cp -f roboAitor.robot /usr/lib/realtimebattle/Robots
	chmod 777 /usr/lib/realtimebattle/Robots/roboAitor.robot
	chmod 777 /usr/lib/realtimebattle/Robots/roboAitor/*.class

clean:
	rm -f roboAitor/*.class
#	rm -f /usr/lib/realtimebattle/Robots/roboAitor.robot
#	rm -rf /usr/lib/realtimebattle/Robots/roboAitor

execute:
	realtimebattle

all:
	make clean
	make compile
	#make configure
	make install
	make execute
