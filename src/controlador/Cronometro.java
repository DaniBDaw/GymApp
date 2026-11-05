package controlador;

/**
 * Cronometro que cuenta en su propio hilo (extends Thread), un segundo cada
 * vez. Puede contar hacia arriba (tiempo del workout y del ejercicio) o hacia
 * abajo (cuenta atras de la serie y del descanso).
 *
 * Cada vez que pasa un segundo avisa a GestionVistas, que es quien decide
 * que hacer (cambiar el tiempo en la ventana, pasar a la siguiente serie...).
 *
 * Como se usa:
 *  - new Cronometro(false) + start()  -> el hilo arranca, pero parado
 *  - empezar() / pausar()             -> lo pone en marcha o lo para
 *  - terminar()                       -> el hilo acaba (no se puede volver a usar)
 */
public class Cronometro extends Thread {

	private static final int UN_SEGUNDO = 1000;

	// true = cuenta hacia atras (resta), false = hacia arriba (suma)
	private boolean haciaAtras;

	// "volatile" porque estas variables las cambia el hilo de la ventana (al
	// pulsar los botones) y las lee este hilo: asi los dos ven el valor bueno
	private volatile int segundos = 0;
	private volatile boolean enMarcha = false;
	private volatile boolean terminado = false;

	public Cronometro(boolean haciaAtras) {
		this.haciaAtras = haciaAtras;
	}

	/**
	 * Lo que hace el hilo: esperar un segundo y, si esta en marcha, sumar o
	 * restar un segundo y avisar. Se repite hasta que se llama a terminar().
	 */
	@Override
	public void run() {
		while (!terminado) {
			try {
				Thread.sleep(UN_SEGUNDO);
			} catch (InterruptedException e) {
				// terminar() despierta al hilo con interrupt() para que acabe ya
				terminado = true;
			}

			if (enMarcha && !terminado) {
				if (haciaAtras) {
					segundos--;
					// La cuenta atras se para sola al llegar a 0
					if (segundos <= 0) {
						segundos = 0;
						enMarcha = false;
					}
				} else {
					segundos++;
				}
				GestionVistas.avisarSegundo(this);
			}
		}
	}

	/**
	 * Pone el cronometro en marcha (tambien sirve para reanudarlo).
	 */
	public void empezar() {
		enMarcha = true;
	}

	/**
	 * Para el cronometro sin perder el tiempo que lleva.
	 */
	public void pausar() {
		enMarcha = false;
	}

	/**
	 * Acaba el hilo. Se usa al salir de la ventana del workout.
	 */
	public void terminar() {
		enMarcha = false;
		terminado = true;
		interrupt();
	}

	public boolean estaEnMarcha() {
		return enMarcha;
	}

	public boolean estaTerminado() {
		return terminado;
	}

	public int getSegundos() {
		return segundos;
	}

	/**
	 * Cambia el tiempo (por ejemplo a 0, o al tiempo de la serie).
	 */
	public void setSegundos(int segundos) {
		this.segundos = segundos;
	}
}
