package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Ventana del resumen del workout (diseño "PANEL - Resumen" de Figma): sale
 * al terminar todos los ejercicios o al pulsar SALIR. Muestra el tiempo
 * total, el previsto, los ejercicios hechos, el porcentaje, la lista de
 * ejercicios y un mensaje motivacional. Solo es la parte visual: el
 * controlador le pasa los textos ya preparados.
 */
public class VentanaResumen extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/resumen_bg.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	private static final String RUTA_HECHO = "images/iconos/estado_hecho.png";
	private static final String RUTA_PENDIENTE = "images/iconos/estado_pendiente.png";

	// Cajas de datos: 4 en fila, de 190x109 y separadas 15 px
	private static final int CAJA_X = 40;
	private static final int CAJA_Y = 218;
	private static final int CAJA_ANCHO = 190;
	private static final int CAJA_ALTO = 109;
	private static final int CAJA_SEPARACION = 205;

	// Lista de ejercicios: primera fila y distancia entre filas
	private static final int LISTA_Y = 380;
	private static final int LISTA_SEPARACION = 30;

	// Color de cada nivel: la posicion es el numero de nivel (0 a 4)
	private static final Color[] COLORES_NIVEL = { Estilos.COLOR_NIVEL_NOVATO, Estilos.COLOR_NIVEL_PRINCIPIANTE,
			Estilos.COLOR_NIVEL_INTERMEDIO, Estilos.COLOR_NIVEL_AVANZADO, Estilos.COLOR_NIVEL_EXPERTO };

	// Colores
	private static final Color COLOR_BORDE_CAJA = new Color(128, 128, 128);
	private static final Color COLOR_TITULO_CAJA_NEGRA = new Color(150, 150, 150);
	private static final Color COLOR_TIEMPO_LISTA = new Color(120, 120, 120);
	private static final Color COLOR_TEXTO_MENSAJE = new Color(225, 225, 225);

	// Fuentes
	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 42);
	private static final Font FUENTE_DATO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 46);
	private static final Font FUENTE_TITULO_MENSAJE = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 22);
	private static final Font FUENTE_LISTA = Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 16f);

	private PanelFondo panelFondo;
	private JLabel lblTitulo;
	private JLabel lblNombreWorkout;
	private JLabel lblNivel;
	private JLabel lblFecha;
	private CajaDato cajaTiempoTotal;
	private CajaDato cajaTiempoPrevisto;
	private CajaDato cajaEjercicios;
	private CajaDato cajaCompletado;
	private JLabel lblTituloMensaje;
	private JTextArea txtMensaje;
	private JButton btnVolver;

	public VentanaResumen() {
		setTitle("GymApp - Resumen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearTitulo();
		crearCajasDatos();
		crearTituloLista();
		crearMensaje();
		crearParteDeAbajo();

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: CABECERA (logo GymApp)
	// ====================================================================
	private void crearCabecera() {
		JLabel lblLogo = new JLabel(UtilidadesVista.cargarImagen(RUTA_LOGO_NEGRO));
		lblLogo.setBounds(24, 14, 32, 24);
		panelFondo.add(lblLogo);

		JLabel lblNombreApp = new JLabel("GymApp");
		lblNombreApp.setFont(Estilos.FUENTE_LOGO_TEXTO);
		lblNombreApp.setForeground(Estilos.COLOR_TEXTO);
		lblNombreApp.setBounds(62, 11, 120, 30);
		panelFondo.add(lblNombreApp);
	}

	// ====================================================================
	// PANEL: TITULO (titulo grande + workout, nivel y fecha)
	// ====================================================================
	/**
	 * La linea de debajo del titulo son tres etiquetas seguidas: el nombre del
	 * workout, el nivel (con el color del nivel) y la fecha. Van dentro de un
	 * panel con FlowLayout, que las pone una detras de otra sin tener que
	 * calcular donde empieza cada una.
	 */
	private void crearTitulo() {
		lblTitulo = new JLabel();
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setBounds(38, 96, 650, 60);
		panelFondo.add(lblTitulo);

		JPanel panelLinea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelLinea.setOpaque(false);
		panelLinea.setBounds(42, 157, 700, 22);
		panelFondo.add(panelLinea);

		lblNombreWorkout = new JLabel();
		lblNombreWorkout.setFont(Estilos.FUENTE_MONO);
		lblNombreWorkout.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		panelLinea.add(lblNombreWorkout);

		lblNivel = new JLabel();
		lblNivel.setFont(Estilos.FUENTE_MONO_NEGRITA);
		panelLinea.add(lblNivel);

		lblFecha = new JLabel();
		lblFecha.setFont(Estilos.FUENTE_MONO);
		lblFecha.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		panelLinea.add(lblFecha);

		// Linea gris de separacion
		JPanel separador = new JPanel();
		separador.setBackground(Estilos.COLOR_SEPARADOR);
		separador.setBounds(40, 196, 805, 1);
		panelFondo.add(separador);
	}

	// ====================================================================
	// PANEL: CAJAS DE DATOS (tiempo total, previsto, ejercicios, completado)
	// ====================================================================
	private void crearCajasDatos() {
		cajaTiempoTotal = new CajaDato("TIEMPO TOTAL", true);
		cajaTiempoTotal.setBounds(CAJA_X, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(cajaTiempoTotal);

		cajaTiempoPrevisto = new CajaDato("TIEMPO PREVISTO", false);
		cajaTiempoPrevisto.setBounds(CAJA_X + CAJA_SEPARACION, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(cajaTiempoPrevisto);

		cajaEjercicios = new CajaDato("EJERCICIOS", false);
		cajaEjercicios.setBounds(CAJA_X + CAJA_SEPARACION * 2, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(cajaEjercicios);

		cajaCompletado = new CajaDato("COMPLETADO", false);
		cajaCompletado.setBounds(CAJA_X + CAJA_SEPARACION * 3, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(cajaCompletado);
	}

	// ====================================================================
	// PANEL: EJERCICIOS REALIZADOS (titulo; las filas las crea cargarEjercicios)
	// ====================================================================
	private void crearTituloLista() {
		JLabel lblLista = new JLabel("EJERCICIOS REALIZADOS");
		lblLista.setFont(Estilos.FUENTE_MONO);
		lblLista.setForeground(Estilos.COLOR_CAJA);
		lblLista.setBounds(40, 349, 300, 22);
		panelFondo.add(lblLista);
	}

	// ====================================================================
	// PANEL: MENSAJE MOTIVACIONAL (caja negra de la derecha)
	// ====================================================================
	private void crearMensaje() {
		JPanel cajaMensaje = new CajaNegra();
		cajaMensaje.setLayout(null);
		cajaMensaje.setBounds(480, 350, 365, 150);
		panelFondo.add(cajaMensaje);

		lblTituloMensaje = new JLabel();
		lblTituloMensaje.setFont(FUENTE_TITULO_MENSAJE);
		lblTituloMensaje.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblTituloMensaje.setBounds(22, 20, 330, 32);
		cajaMensaje.add(lblTituloMensaje);

		// JTextArea para que el mensaje pueda ocupar varias lineas
		txtMensaje = new JTextArea();
		txtMensaje.setFont(Estilos.FUENTE_MONO_PEQUENA.deriveFont(14f));
		txtMensaje.setForeground(COLOR_TEXTO_MENSAJE);
		txtMensaje.setLineWrap(true);
		txtMensaje.setWrapStyleWord(true);
		txtMensaje.setEditable(false);
		txtMensaje.setFocusable(false);
		txtMensaje.setOpaque(false);
		txtMensaje.setBounds(22, 62, 325, 80);
		cajaMensaje.add(txtMensaje);
	}

	// ====================================================================
	// PANEL: PARTE DE ABAJO ("Volverás a la lista..." y boton VOLVER)
	// ====================================================================
	/**
	 * VOLVER igual que el de la ventana de detalle (blanco con borde negro).
	 */
	private void crearParteDeAbajo() {
		JLabel lblAviso = new JLabel("Volverás a la lista de workouts");
		lblAviso.setFont(Estilos.FUENTE_MONO);
		lblAviso.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblAviso.setBounds(42, 538, 400, 22);
		panelFondo.add(lblAviso);

		btnVolver = new BotonVolver("Volver");
		btnVolver.setBounds(724, 525, 121, 46);
		panelFondo.add(btnVolver);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: TITULO (metodos)
	// ====================================================================

	/**
	 * Ejemplo: setTitulo("¡Workout completado!")
	 */
	public void setTitulo(String titulo) {
		lblTitulo.setText(titulo);
	}

	/**
	 * Ejemplo: setDatosWorkout("Core express", 2, "Intermedio", "21/10/2026")
	 * -> "Core express - Nivel 2 - Intermedio - 21/10/2026", con el nivel
	 * del color de ese nivel. Un nivel sin numero (negativo) sale solo con
	 * su nombre y en negro.
	 */
	public void setDatosWorkout(String nombreWorkout, int nivel, String nombreNivel, String fecha) {
		lblNombreWorkout.setText(nombreWorkout + " - ");
		lblFecha.setText(" - " + fecha);

		if (nivel >= 0 && nivel < COLORES_NIVEL.length) {
			lblNivel.setText("Nivel " + nivel + " - " + nombreNivel);
			lblNivel.setForeground(COLORES_NIVEL[nivel]);
		} else {
			lblNivel.setText("Nivel " + nombreNivel);
			lblNivel.setForeground(Estilos.COLOR_CAJA);
		}
	}

	// ====================================================================
	// PANEL: CAJAS DE DATOS (metodos)
	// ====================================================================

	/**
	 * Ejemplo: setDatos("12:47", "12:00", "4 / 4", "100%")
	 */
	public void setDatos(String tiempoTotal, String tiempoPrevisto, String ejercicios, String completado) {
		cajaTiempoTotal.setValor(tiempoTotal);
		cajaTiempoPrevisto.setValor(tiempoPrevisto);
		cajaEjercicios.setValor(ejercicios);
		cajaCompletado.setValor(completado);
	}

	// ====================================================================
	// PANEL: EJERCICIOS REALIZADOS (metodos)
	// ====================================================================

	/**
	 * Una fila por ejercicio: icono (check verde si esta hecho, gris si no),
	 * nombre y tiempo a la derecha. Ejemplo:
	 * nombres = { "Plancha lateral", "Crunch bicicleta" }
	 * tiempos = { "03:05", "--:--" }
	 * hechos  = { true, false }
	 */
	public void cargarEjercicios(String[] nombres, String[] tiempos, boolean[] hechos) {
		for (int i = 0; i < nombres.length; i++) {
			int y = LISTA_Y + i * LISTA_SEPARACION;

			JLabel lblIcono = new JLabel();
			if (hechos[i]) {
				lblIcono.setIcon(UtilidadesVista.cargarImagen(RUTA_HECHO));
			} else {
				lblIcono.setIcon(UtilidadesVista.cargarImagen(RUTA_PENDIENTE));
			}
			lblIcono.setBounds(40, y, 22, 22);
			panelFondo.add(lblIcono);

			JLabel lblNombre = new JLabel(nombres[i]);
			lblNombre.setFont(FUENTE_LISTA);
			lblNombre.setForeground(Estilos.COLOR_CAJA);
			lblNombre.setBounds(74, y - 2, 300, 26);
			panelFondo.add(lblNombre);

			JLabel lblTiempo = new JLabel(tiempos[i]);
			lblTiempo.setFont(FUENTE_LISTA);
			lblTiempo.setForeground(COLOR_TIEMPO_LISTA);
			lblTiempo.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTiempo.setBounds(340, y - 2, 100, 26);
			panelFondo.add(lblTiempo);
		}
		panelFondo.repaint();
	}

	// ====================================================================
	// PANEL: MENSAJE MOTIVACIONAL (metodos)
	// ====================================================================

	/**
	 * Ejemplo: setMensaje("¡Gran trabajo, Alexis!", "Has completado...")
	 */
	public void setMensaje(String titulo, String mensaje) {
		lblTituloMensaje.setText(titulo);
		txtMensaje.setText(mensaje);
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JButton getBtnVolver() {
		return btnVolver;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CAJA DE UN DATO (titulo pequeño + valor grande)
	// ====================================================================
	/**
	 * oscura = true: negra con el valor en blanco (TIEMPO TOTAL).
	 * oscura = false: blanca con borde gris y el valor en negro.
	 */
	private class CajaDato extends JPanel {

		private static final long serialVersionUID = 1L;

		private boolean oscura;
		private JLabel lblValor;

		public CajaDato(String titulo, boolean oscura) {
			this.oscura = oscura;
			setLayout(null);
			setOpaque(false);

			JLabel lblTituloCaja = new JLabel(titulo);
			lblTituloCaja.setFont(Estilos.FUENTE_MONO_PEQUENA);
			lblTituloCaja.setBounds(16, 13, 170, 20);
			add(lblTituloCaja);

			lblValor = new JLabel();
			lblValor.setFont(FUENTE_DATO);
			lblValor.setBounds(14, 36, 172, 62);
			add(lblValor);

			if (oscura) {
				lblTituloCaja.setForeground(COLOR_TITULO_CAJA_NEGRA);
				lblValor.setForeground(Estilos.COLOR_TEXTO_CAJA);
			} else {
				lblTituloCaja.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
				lblValor.setForeground(Estilos.COLOR_TEXTO);
			}
		}

		public void setValor(String valor) {
			lblValor.setText(valor);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (oscura) {
				g2.setColor(Estilos.COLOR_CAJA);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
			} else {
				g2.setColor(Estilos.COLOR_FONDO);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(COLOR_BORDE_CAJA);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
			}
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: CAJA NEGRA REDONDEADA (mensaje motivacional)
	// ====================================================================
	private class CajaNegra extends JPanel {

		private static final long serialVersionUID = 1L;

		public CajaNegra() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Estilos.COLOR_CAJA);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: BOTON VOLVER (igual que el de la ventana de detalle)
	// ====================================================================
	/**
	 * Blanco con borde negro y esquinas redondeadas. Con el raton encima se
	 * pone gris.
	 */
	private class BotonVolver extends JButton {

		private static final long serialVersionUID = 1L;

		public BotonVolver(String texto) {
			super(texto);
			setFont(Estilos.FUENTE_BOTON);
			setForeground(Estilos.COLOR_CAJA);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (getModel().isRollover() || getModel().isPressed()) {
				g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
			} else {
				g2.setColor(Estilos.COLOR_FONDO);
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			g2.setColor(Estilos.COLOR_CAJA);
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
