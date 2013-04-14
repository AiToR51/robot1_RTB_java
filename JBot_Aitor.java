//
//
// JBot_C v0.3.0 in JAVA
// Robot for RealTimeBattle v1.0.2
// (c) 1999, 2000 by Ingo Beckmann
// ingo.beckmann@fernuni-hagen.de
// http://www.nnTec.de
//
//
import java.util.*;
import java.io.*;

// What a Coward !
class JBot_Aitor extends JBot {

	// Hello ! I am:
	final private static String myDefaultName = "AiToR51";
	// borussiamoenchengladbachlikecolour:
	final private static String HOMECOLOUR = "45c126", AWAYCOLOUR = "b3e5d3";
	// sleep in ms, everytime in do-loop:
	private int SLEEP;
	// How much time have I ?
	private double myTimeShare;

	private String[] warning;

	private boolean continueThis;
	private StringTokenizer st;
	private BufferedReader bf;
	private String msg2robotString, s;
	private int msg2robot, numberPars, lastAction, lastObject;

	private String[] parString; // Parameters

	// Info from RTB:
	double radarDistance, radarAngle, robotDistance, wallDistance, wallAngle,
			collisionAngle, time, speed, energyLevel, cannonAngle,
			otherRobotEnergy;


	int radarObjectType, collisionObjectType, optionType, warningType,
			robotsLeft;

	boolean otherRobotEnemy, rotationReached;

	// Variables:
	double rotate, flee;

	int velocidad = 0;
	BotThread auxiliar;
	// Constructor:
	int multiplicador = 1;
	
	boolean waitingCookie = false;
	long waitingCookieTime = 0;
	
	JBot_Aitor(String name, int SLEEP) {
		super(name);
		this.SLEEP = SLEEP;

		warning = new String[WARNING_TYPE_COUNT];
		parString = new String[MAX_PARAMETER_COUNT];

		bf = new BufferedReader(new InputStreamReader(System.in));
		auxiliar = new BotThread(this,SLEEP);
		// some Initialization:
		lastAction = UNKNOWN_MESSAGE_TO_ROBOT;
		lastObject = NOOBJECT;
		time = 0.0;
		rotate = 3.0;
		rotationReached = true;
		flee = 0.0;

	} // end Constructor

	// Constructor:
	JBot_Aitor(int SLEEP) {
		// call constructor above with default name:
		this(myDefaultName, SLEEP);
	} // end Constructor

	@Override
	public void darVueltas() {
		frenar(0);
		girar(300,0,90,true);
		acelerar(50);
		echoDebug("dando vueltas sin paraaaar" , 4);
		
	}
	
	@Override
	public void acelerar(int porcentaje) {
		if (porcentaje < 0) {
			porcentaje = 0;
		} else if (porcentaje > 100) {
			porcentaje = 100;
		}
		if (this.energyLevel < 5.0) {
			if (porcentaje > 20) {
				porcentaje = 20;
			}
		}
		double acel = (gameOption[ROBOT_MAX_ACCELERATION]*porcentaje/100);
		this.velocidad = porcentaje;
		sendCommand("Accelerate "+acel);
	}
	
	
	@Override
	public void frenar(int porcentaje) {
		if (porcentaje < 0) {
			porcentaje = 0;
		} else if (porcentaje > 100) {
			porcentaje = 100;
		}
		if (this.energyLevel < 5.0) {
			if (porcentaje < 30) {
				porcentaje = 30;
			}
		}
		sendCommand("Brake "+porcentaje/100);
	}
	
	private void girarCookie(int grados, double anguloRelativo,int porcentajeVelocidad, boolean random, boolean fromCookie) {
		if ((this.waitingCookieTime + 2000) < System.currentTimeMillis()) {
			this.waitingCookie = false;
		}
		if(!fromCookie && this.waitingCookie) 
			return;
		double g = 0.0;
		if (grados !=0 ){
		 g = ((TWO_PI*grados/360 ) + anguloRelativo) % TWO_PI;
		}
		else {
		 g = anguloRelativo;
		}
		if (random) {
			g *= (Math.random()*2-1 > 0)?1:-1;

		}
		rotationReached = false;
		double velocidad = (gameOption[ROBOT_MAX_ROTATE] * porcentajeVelocidad )/100;
		echoDebug("grados "+grados+" rel: "+anguloRelativo +" -> "+g , 4);
		sendCommand("RotateAmount " + ROT_ROBOT
				+ " "+ velocidad+" " + g*multiplicador);
	}
	
	private void girar(int grados, double anguloRelativo,int porcentajeVelocidad, boolean random) {
		this.girarCookie(grados, anguloRelativo, porcentajeVelocidad, random, false);
	}
	
	private void disparar (int porcentaje) {
		if (this.energyLevel < 5.0)
			return;
		if (porcentaje < 0) {
			porcentaje = 0;
		} else if (porcentaje > 100) {
			porcentaje = 100;
		}
		//en funcion da nosa enerxia imolo modificar un pouco. ata un 50%.
		double p = (porcentaje *(1-(1-this.energyLevel/gameOption[ROBOT_MAX_ENERGY])*0.5))/100;
		double shoot = (gameOption[SHOT_MAX_ENERGY] - gameOption[SHOT_MIN_ENERGY])*(p)+gameOption[SHOT_MIN_ENERGY];
		sendCommand("Shoot "+shoot);
	}
	
	private void barrerRadar(int grados, int velocidad) {
		double g = (TWO_PI*grados/360) % TWO_PI;
		double v = (gameOption[ROBOT_MAX_ROTATE] * velocidad )/100;
		sendCommand("Sweep " + (ROT_CANNON + ROT_RADAR)
				+ " "+v+" -"+g+" "+g);
	}
	@Override
	public void cambio(){
		this.girar(180, 0, 100, false);
		this.multiplicador*=-1;
	}
	// JBot running as a thread:
	public void run() {

		// OK, start operation...
		do {
			try {

				if (bf.ready()) {
					// There is a message !
					// Get the message waiting:
					st = new StringTokenizer(bf.readLine());

					// get the message command (first token):
					if (st.hasMoreTokens())
						msg2robotString = st.nextToken();
					// System.err.println(msg2robotString);
					msg2robot = parseMessage(msg2robotString);

					// get number of parameters and put them in array of String:
					numberPars = st.countTokens();
					for (int i = 0; i < numberPars; i++) {
						parString[i] = st.nextToken();
					}

					auxiliar.eventReceived();
					// Okay, let's see what we've received:
					switch (msg2robot) {

					case RADAR:
						radarDistance = parseDouble(parString[0]);
						radarObjectType = Integer.parseInt(parString[1]);
						radarAngle = parseDouble(parString[2]);
						
						switch (radarObjectType) {
						case ROBOT:
							robotDistance = radarDistance;
							echoDebug("distance to robot: " + robotDistance + ",angle: "+radarAngle+ " "+parString[2], 4);
							disparar(10);
//							barrerRadar(20,30);
							
							frenar(10);
							
								// echoDebug("saw a robot, now fleeing for "
								// +(int)((flee-time)*1000)+ "ms", 4);
							if (robotDistance < 3.0 ) {
								frenar(40);
								disparar(100);
								disparar(80);
								acelerar(20);
								girar((int) (40*Math.random())+10,0,50,true);
							} else {
								girar((int) (Math.random()*10),radarAngle,50,true);
								acelerar(30);

							}
							break;
						case SHOT:
							//hacemos maniobra de esquivar, giro de 180º
							girar((int) (90+Math.random()*100),0,(int) (50+Math.random()*50),true);
							frenar(0);
							acelerar(40);
//							if (rotationReached){
//								girar(90,0,100,true);
//							}
							
							break;
						case WALL:
							wallDistance = radarDistance;
							wallAngle = radarAngle;

							if (wallDistance < 4.0) {
								acelerar(4);
								frenar(100);
								girar((int) (90+Math.random()*30),0,100,false);
							}
							if (wallDistance < 9.0) {
								frenar(70);
								girar((int) (75+40*Math.random()),0,100,false);
								acelerar(30);
								
							} 
//							else {
//								frenar(10);
//								acelerar(30);
//
//								if (rotationReached){
//									girar(90,Math.abs(wallAngle),80,true);
//								}
//	
////								barrerRadar(30,30);
//							}
							break;
						case COOKIE:
							this.waitingCookieTime = System.currentTimeMillis();
							this.waitingCookie = true;
							if (radarDistance > 8.0){
								girar(0,radarAngle,100,false);
								frenar(20);
								acelerar(60);
							} else {
								frenar(80);
								acelerar(5);
							}
										
							break;
						case MINE:
							if (radarDistance < 8.0)
								disparar(3);
							
							disparar(1);
							break;
						default:
							break;
						}
						s = "Radar Message";
						echoDebug(s, 5);
						break; // end CASE RADAR

					case INFO:
						time = parseDouble(parString[0]);
						speed = parseDouble(parString[1]);
						cannonAngle = parseDouble(parString[2]);
						// game over for me ?
						s = "Info Message, time: " + time;
						echoDebug(s, 5);
						break;

					case ROBOT_INFO:
						otherRobotEnergy = parseDouble(parString[0]);
						otherRobotEnemy = (Integer.parseInt(parString[1]) == 0) ? true
								: false;
						s = "RobotInfo Message";
						echoDebug(s, 5);
						break;

					case COLLISION:
						collisionObjectType = Integer.parseInt(parString[0]);
						collisionAngle = parseDouble(parString[1]);
						echoDebug("hit something at angle=" +collisionAngle, 4);
						switch (collisionObjectType) {
						case ROBOT:
							if (Math.abs(collisionAngle) < 0.5) {
								disparar(20);
							}
								
								frenar(0);
								girar(90, 0, 100,false);
								
								acelerar(10);
								// echoDebug("A robot! Panicing for "
								// +(int)((flee-time)*1000)+ "ms", 4);
								
							break;
						case SHOT:
							girar(90, 0, 80,true);
							frenar(0);
							acelerar(60);
							// echoDebug("A shot! Panicing for "
							// +(int)((flee-time)*1000)+ "ms", 4);
							echoDebug("Ouch!", 4);
							break;
						case WALL:
								frenar(90);

									girar(140,0,100,false);
								acelerar(10);
							break;
						case COOKIE:
							echoDebug("Yumm Yumm", 3);
							this.waitingCookie = false;
							break;
						case MINE:
							echoDebug("I hit a mine!", 3);
							break;
						default:
							break;
						}
						s = "Collision Message";
						echoDebug(s, 5);
						break;

					case ENERGY:
						energyLevel = parseDouble(parString[0]);
						// Energy lower than 70% ?
//						if (energyLevel * 10 < gameOption[ROBOT_START_ENERGY] * 7) {
//							sendCommand("Sweep " + (ROT_CANNON + ROT_RADAR)
//									+ " 1 0 0");
//						}
						echoDebug(s, 5);
						break;

					case ROBOTS_LEFT:
						robotsLeft = Integer.parseInt(parString[0]);
						if (robotsLeft < 4) {
							barrerRadar(70, 100);
						}
						s = "RobotsLeft Message: " + robotsLeft;
						echoDebug(s, 5);
						break;

					case ROTATION_REACHED:
						// Integer.parseInt(parString[0]);
						rotationReached = true;
						s = "RotationReached Message";
						echoDebug(s, 5);
						break;

					case GAME_OPTION:
						optionType = Integer.parseInt(parString[0]);
						gameOption[optionType] = parseDouble(parString[1]);
						switch (optionType) {
						case DEBUG_LEVEL:
							setDebugLevel(gameOption[optionType]);
							break;
						default:
							break;
						}
						echoDebug("received GameOption " + optionType + ": "
								+ gameOption[optionType], 3);
						break;

					case GAME_STARTS:
						this.darVueltas();
						this.barrerRadar(25, 40);
						echo("Let's go !");
						break;

					case INITIALIZE:
						if (Integer.parseInt(parString[0]) == 1) {
							sendCommand("Name " + Name);
							sendCommand("Colour " + HOMECOLOUR + " "
									+ AWAYCOLOUR);
						}
						echo("Alive & Kicking !!!");
						break;

					case WARNING:
						warningType = Integer.parseInt(parString[0]);
						warning[warningType] = parString[1];
						if (warningType == PROCESS_TIME_LOW)
							echo("low on CPU-time: " + warning[warningType]);
						echoDebug("Warning Message: " + warningType + " "
								+ warning[warningType], 2);
						break;

					case DEAD:
						// be quiet from now on:
						dead = true;
						break;

					case GAME_FINISHES:
						gameFinishes = true;
						break;

					case EXIT_ROBOT:
						exitRobot = true;
						echo("Bye, bye.");
						break;

					case YOUR_NAME:
						break;

					case YOUR_COLOUR:
						break;

					case UNKNOWN_MESSAGE_TO_ROBOT:
						s = msg2robotString + " with " + numberPars
								+ " parameters";
						echo("*** Unknown Message *** : " + s);
						break;

					default:
						break;
					} // end switch (msg2robot)

					// when there is no message:
				} else {
					// No new message:
					echoDebug("is sad. No message for him.", 5);
					// sleep in my spare time:
					try {
						int extra_sleep = 2 * SLEEP + 1;
						echoDebug("gets some extra sleep: " + extra_sleep
								+ "ms", 5);
						sleep(extra_sleep);
					} catch (InterruptedException ie) {
						echoDebug("*** InterruptedException ***", 1);
					}
				} // bf not ready

				// do other stuff here
				echoDebug("could do something else", 5);

			} // end try {...}

			// Exception caused by BufferedReader ?
			catch (IOException ioX) {
				echoDebug("*** IOException ***", 1);
			}
			// Exception caused by StringTokenizer ?
			catch (NoSuchElementException nseX) {
				echoDebug("*** NoSuchElementException ***", 1);
			}
			// Exception caused by parsing routines ?
			catch (NumberFormatException nfeX) {
				echoDebug("*** NumberFormatException ***", 1);
			} catch (ArrayIndexOutOfBoundsException aiobX) {
				echoDebug("*** ArrayIndexOutOfBoundsException ***:"
						+ msg2robotString, 1);
			} catch (Exception x) {
				echoDebug("*** Exception *** " + x.toString(), 1);
			}

			continueThis = !(dead || gameFinishes || exitRobot || handoverBaton);
			// Finally find some sleep (guaranteed!):
			if (continueThis && SLEEP > 0)
				try {
					echoDebug(
							"sleeping for " + SLEEP + "ms, ...zzzz...zzz....",
							5);
					sleep(SLEEP);
				} catch (InterruptedException ie) {
					echoDebug("*** InterruptedException ***", 1);
				}

		} while (continueThis);
	} // end run

} // end JBot_Aitor
