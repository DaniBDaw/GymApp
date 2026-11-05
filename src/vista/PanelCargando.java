package vista;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Circulo de carga que gira (spinner).
 * Se dibuja un aro gris claro y encima un arco negro que va girando.
 * Se puede usar en cualquier ventana: empezar() y parar().
 */
public class PanelCargando extends JPanel {

	private static final long serialVersionUID = 1L;

	// Grosor de la linea y tamano del arco que gira (en grados)
	private static final int GROSOR = 6;
	private static final int TAMANO_ARCO = 90;

	// Cada 16 ms (unos 60 fotogramas por segundo) se giran 6 grados
	private static final int TIEMPO_PASO = 16;
	private static final int GRADOS_PASO = 6;

	private int angulo;
	private Timer timerGiro;

	public PanelCargando() {
		setOpaque(false);
		angulo = 90;

		timerGiro = new Timer(TIEMPO_PASO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Restar grados = girar en el sentido de las agujas del reloj
				angulo = angulo - GRADOS_PASO;
				if (angulo < 0) {
					angulo = angulo + 360;
				}
				repaint();
			}
		});
	}

	public void empezar() {
		timerGiro.start();
	}

	public void parar() {
		timerGiro.stop();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(GROSOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		// El circulo se deja un margen para que la linea gruesa no se corte
		int margen = GROSOR;
		int diametro = Math.min(getWidth(), getHeight()) - margen * 2;
		int x = (getWidth() - diametro) / 2;
		int y = (getHeight() - diametro) / 2;

		// 1. Aro completo gris claro (el "camino")
		g2.setColor(Estilos.COLOR_CARGA_FONDO);
		g2.drawOval(x, y, diametro, diametro);

		// 2. Arco negro que gira
		g2.setColor(Estilos.COLOR_TEXTO);
		g2.drawArc(x, y, diametro, diametro, angulo, TAMANO_ARCO);

		g2.dispose();
	}
}
