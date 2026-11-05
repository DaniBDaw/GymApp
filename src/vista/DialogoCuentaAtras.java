package vista;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Cuenta atras de 5 segundos antes de cada serie, en grande y en el centro
 * de la pantalla: "PREPÁRATE", el numero (5, 4, 3, 2, 1) y la serie que va a
 * empezar. De fondo, la ventana borrosa (como el aviso de SALIR).
 *
 * Es un JDialog sin bordes del mismo tamano que la ventana y justo encima.
 * NO es modal: el controlador sigue funcionando y le cambia el numero cada
 * segundo con setNumero(). Al llegar a 0 el controlador lo cierra.
 *
 * Como es una ventana aparte, si se mueve la ventana de detras hay que
 * moverlo tambien (seguirVentana), si no se quedaria donde estaba.
 */
public class DialogoCuentaAtras extends JDialog {

	private static final long serialVersionUID = 1L;

	// Capa oscura encima de la foto (mas oscura que la del aviso de SALIR para
	// que los numeros blancos se lean bien). El cuarto numero es la transparencia
	private static final Color COLOR_OSCURECER = new Color(0, 0, 0, 150);

	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 34);
	private static final Font FUENTE_NUMERO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 200);
	private static final Font FUENTE_SERIE = Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 18f);

	private JLabel lblNumero;

	// La ventana de detras y lo que escucha cuando se mueve (para quitarlo al cerrar)
	private JFrame ventana;
	private ComponentAdapter seguirVentana;

	/**
	 * serie: el texto de abajo. Ejemplo: "Sentadilla libre · SERIE 2 DE 3 · 10 repeticiones"
	 */
	public DialogoCuentaAtras(JFrame ventana, String serie) {
		// false = no modal: no bloquea al controlador mientras se ve
		super(ventana, false);
		setUndecorated(true);
		// No se queda con el foco del teclado (al cerrarse vuelve a la ventana)
		setFocusableWindowState(false);

		// Mismo tamano y posicion que el contenido de la ventana de detras
		Container contenido = ventana.getContentPane();
		setSize(contenido.getSize());
		setLocation(contenido.getLocationOnScreen());

		PanelOscuro panel = new PanelOscuro(UtilidadesVista.crearFotoBorrosa(contenido));
		setContentPane(panel);

		crearTextos(panel, serie);
		seguirVentana(ventana);
	}

	// ====================================================================
	// PANEL: SEGUIR A LA VENTANA (si se mueve, la cuenta atras tambien)
	// ====================================================================
	/**
	 * Cada vez que se mueve la ventana de detras (componentMoved), la cuenta
	 * atras se pone otra vez justo encima de su contenido.
	 */
	private void seguirVentana(final JFrame ventana) {
		this.ventana = ventana;
		seguirVentana = new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				setLocation(ventana.getContentPane().getLocationOnScreen());
			}
		};
		ventana.addComponentListener(seguirVentana);
	}

	// ====================================================================
	// PANEL: TEXTOS ('PREPÁRATE', el numero y la serie)
	// ====================================================================
	private void crearTextos(JPanel panel, String serie) {
		int ancho = getWidth();

		JLabel lblTitulo = new JLabel("PREPÁRATE");
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 120, ancho, 50);
		panel.add(lblTitulo);

		lblNumero = new JLabel();
		lblNumero.setFont(FUENTE_NUMERO);
		lblNumero.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblNumero.setHorizontalAlignment(SwingConstants.CENTER);
		lblNumero.setBounds(0, 170, ancho, 240);
		panel.add(lblNumero);

		JLabel lblSerie = new JLabel(serie);
		lblSerie.setFont(FUENTE_SERIE);
		lblSerie.setForeground(Estilos.COLOR_TEXTO_TARJETA);
		lblSerie.setHorizontalAlignment(SwingConstants.CENTER);
		lblSerie.setBounds(0, 420, ancho, 30);
		panel.add(lblSerie);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	/**
	 * Pone el numero grande. Ejemplo: setNumero(3) -> "3"
	 */
	public void setNumero(int segundos) {
		lblNumero.setText(Integer.toString(segundos));
	}

	public void cerrar() {
		// Ya no hace falta seguir a la ventana
		ventana.removeComponentListener(seguirVentana);
		dispose();
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: FONDO (la foto borrosa + capa oscura)
	// ====================================================================
	private class PanelOscuro extends JPanel {

		private static final long serialVersionUID = 1L;

		private BufferedImage fotoBorrosa;

		public PanelOscuro(BufferedImage fotoBorrosa) {
			this.fotoBorrosa = fotoBorrosa;
			setLayout(null);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(fotoBorrosa, 0, 0, null);
			g.setColor(COLOR_OSCURECER);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}
