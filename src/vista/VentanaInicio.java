package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Ventana de inicio (pantalla de bienvenida).
 * Muestra la imagen de fondo y un texto para pulsar una tecla o hacer clic.
 * El controlador anade el KeyListener y el MouseListener a esta ventana
 * para pasar a la siguiente.
 */
public class VentanaInicio extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/panel_inicio.jpg";

	// Cada cuantos milisegundos parpadea el texto
	private static final int TIEMPO_PARPADEO = 700;

	private PanelFondo panelFondo;
	private JLabel lblFecha;
	private JLabel lblPulsar;
	private Timer timerParpadeo;

	public VentanaInicio() {
		setTitle("GymApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		// Icono propio en vez del de Java
		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}

		// Cursor propio (triangulo negro redondeado)
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearFecha();
		crearTextoPulsar();

		// El tamano de la ventana es el de la imagen de fondo
		pack();
		setLocationRelativeTo(null);
	}

	// ====================================================================
	// PANEL: FECHA (arriba, centrada)
	// ====================================================================
	private void crearFecha() {
		lblFecha = new JLabel(UtilidadesVista.obtenerFechaHoy());
		lblFecha.setFont(Estilos.FUENTE_MONO);
		lblFecha.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblFecha.setHorizontalAlignment(SwingConstants.CENTER);

		int ancho = panelFondo.getPreferredSize().width;
		lblFecha.setBounds(0, 40, ancho, 24);
		panelFondo.add(lblFecha);
	}

	// ====================================================================
	// PANEL: TEXTO 'PULSA CUALQUIER TECLA' (abajo, parpadea)
	// ====================================================================
	private void crearTextoPulsar() {
		lblPulsar = new JLabel("Pulsa cualquier tecla o haz clic para continuar");
		lblPulsar.setFont(Estilos.FUENTE_MONO);
		lblPulsar.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblPulsar.setHorizontalAlignment(SwingConstants.CENTER);

		int ancho = panelFondo.getPreferredSize().width;
		int alto = panelFondo.getPreferredSize().height;
		lblPulsar.setBounds(0, alto - 110, ancho, 24);
		panelFondo.add(lblPulsar);

		// Efecto de parpadeo del texto (solo visual)
		timerParpadeo = new Timer(TIEMPO_PARPADEO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lblPulsar.setVisible(!lblPulsar.isVisible());
			}
		});
		timerParpadeo.start();
	}

	/**
	 * Para el parpadeo y cierra la ventana.
	 * La usa el controlador al pasar a la siguiente ventana.
	 */
	public void cerrar() {
		timerParpadeo.stop();
		dispose();
	}
}
