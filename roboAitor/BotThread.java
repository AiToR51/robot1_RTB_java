
public class BotThread extends Thread{

	private static long GIRO = 5000;
	private static long GAP = 100;
	private static long GAP_MURO = 300;
	private JBot robot;
	private long sleepTime = 0;
	private long lastEventTime=0;
	
	private long eventoMuro = 10;
	//fase 0 nada, fase 1 empezamos a girar para atras quitando freno y girando a fondo
	//fase 2 girar pa alante
	private	int muroFase=0;
	
	private long giro=0;
	
	public BotThread(JBot robot, long sleepTime) {
		this.robot = robot;
		this.sleepTime = sleepTime;
	}
	
	public void eventMuro() {
		muroFase = 0;
//		eventoMuro = 0;
		this.eventoMuro = System.currentTimeMillis();
	}
	
	public void eventReceived() {
		this.lastEventTime = System.currentTimeMillis();
	}
	public void run() {
		while(true) {
			if (System.currentTimeMillis() > (this.giro + GIRO)) {
				this.robot.cambio();
				this.giro = System.currentTimeMillis();
			}
			if (System.currentTimeMillis() > (this.eventoMuro + GAP_MURO) &&
					muroFase < 2) {
				switch (muroFase){
					case 0:
						this.robot.frenar(0);
						break;
					case 1:
						this.robot.darVueltas();
						this.robot.acelerar(100);
						break;
				}
				this.eventoMuro = System.currentTimeMillis();
				muroFase++;
			}else if (System.currentTimeMillis() > (this.lastEventTime + GAP)) {
					this.robot.darVueltas();
					this.lastEventTime = System.currentTimeMillis();
				}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean ignorarMuro() {
		if (System.currentTimeMillis() > (this.eventoMuro + 1000)) {
			return true;
		}
		
		return false;
	}
}
