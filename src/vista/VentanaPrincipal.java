package vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Ventana principal (home) que sale despues de la carga del login.
 * Solo es la parte visual: los datos del usuario los pone el controlador
 * con los setters y tambien anade los ActionListener a los botones.
 */
public class VentanaPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;

	// Imagenes
	private static final String RUTA_FONDO = "images/home_background3.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	private static final String RUTA_AJUSTES = "images/iconos/ajustes.png";
	private static final String RUTA_AJUSTES_HOVER = "images/iconos/ajustes_hover.png";
	private static final String RUTA_CERRAR_SESION = "images/iconos/cerrar_sesion.png";
	private static final String RUTA_CERRAR_SESION_HOVER = "images/iconos/cerrar_sesion_hover.png";
	private static final String RUTA_TIEMPO = "images/iconos/tiempo.png";
	private static final String RUTA_ESTADISTICAS = "images/iconos/estadisticas.png";
	// Las tarjetas ya tienen las esquinas redondeadas y el borde en la imagen.
	// La version "_hover" es la que sale con el raton encima
	private static final String RUTA_WORKOUTS = "images/home_workouts.png";
	private static final String RUTA_WORKOUTS_HOVER = "images/home_workouts_hover.png";
	private static final String RUTA_HISTORIAL = "images/home_historial.png";
	private static final String RUTA_HISTORIAL_HOVER = "images/home_historial_hover.png";

	// Margen izquierdo: textos y tarjetas empiezan en la misma linea
	private static final int MARGEN = 70;

	// Filas de nivel, tiempo y estadisticas: icono de 16 px + texto
	private static final int FILA_NIVEL_Y = 161;
	private static final int FILA_TIEMPO_Y = 185;
	private static final int FILA_ESTADISTICAS_Y = 209;
	private static final int ALTO_FILA = 22;
	private static final int TEXTO_FILA_X = MARGEN + 24;

	// Transparencia del siguiente nivel (0 = invisible, 255 = color entero)
	private static final int OPACIDAD_SIGUIENTE_NIVEL = 110;

	// Tarjetas de WORKOUTS e HISTORIAL (mismo tamano que sus imagenes)
	private static final int TARJETA_Y = 280;
	private static final int TARJETA_ANCHO = 351;
	private static final int TARJETA_ALTO = 208;
	private static final int TARJETA_SEPARACION = 54;

	// Iconos de arriba a la derecha, justo antes de la forma negra del fondo
	private static final int TAMANO_BOTON_ICONO = 34;
	private static final int ICONOS_Y = 9;
	private static final int AJUSTES_X = 712;
	private static final int CERRAR_SESION_X = 758;

	// Aparicion suave al abrir: cada 20 ms la capa blanca pierde un 5% (unos 400 ms)
	private static final int TIEMPO_PASO_FUNDIDO = 20;
	private static final float PASO_FUNDIDO = 0.05f;

	private PanelFondo panelFondo;
	private JLabel lblNombre;
	private JLabel lblIconoNivel;
	private JLabel lblNivel;
	private JLabel lblSiguienteNivel;
	private JLabel lblTiempo;
	private JLabel lblEstadisticas;
	private JButton btnAjustes;
	private JButton btnCerrarSesion;
	private JButton btnWorkouts;
	private JButton btnHistorial;
	private PanelFundido panelFundido;
	private Timer timerFundido;

	public VentanaPrincipal() {
		setTitle("GymApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		// El tamano de la ventana es el de la imagen de fondo (900x600)
		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearSaludo();
		crearDatosUsuario();
		crearTarjetas();
		crearPie();

		// Capa blanca por encima de todo para la aparicion suave
		panelFundido = new PanelFundido();
		setGlassPane(panelFundido);

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: CABECERA (logo GymApp, ajustes y cerrar sesion)
	// ====================================================================
	/**
	 * Logo + "GymApp" arriba a la izquierda y los botones de ajustes y
	 * cerrar sesion arriba a la derecha.
	 */
	private void crearCabecera() {
		JLabel lblLogo = new JLabel(UtilidadesVista.cargarImagen(RUTA_LOGO_NEGRO));
		lblLogo.setBounds(24, 14, 32, 24);
		panelFondo.add(lblLogo);

		JLabel lblNombreApp = new JLabel("GymApp");
		lblNombreApp.setFont(Estilos.FUENTE_LOGO_TEXTO);
		lblNombreApp.setForeground(Estilos.COLOR_TEXTO);
		lblNombreApp.setBounds(62, 11, 120, 30);
		panelFondo.add(lblNombreApp);

		btnAjustes = crearBotonIcono(RUTA_AJUSTES, RUTA_AJUSTES_HOVER, "Ajustes");
		btnAjustes.setBounds(AJUSTES_X, ICONOS_Y, TAMANO_BOTON_ICONO, TAMANO_BOTON_ICONO);
		panelFondo.add(btnAjustes);

		// Rayita gris vertical entre los dos iconos
		JPanel separadorIconos = new JPanel();
		separadorIconos.setBackground(Estilos.COLOR_PIE);
		separadorIconos.setBounds(752, ICONOS_Y + 5, 1, 24);
		panelFondo.add(separadorIconos);

		btnCerrarSesion = crearBotonIcono(RUTA_CERRAR_SESION, RUTA_CERRAR_SESION_HOVER, "Cerrar sesión");
		btnCerrarSesion.setBounds(CERRAR_SESION_X, ICONOS_Y, TAMANO_BOTON_ICONO, TAMANO_BOTON_ICONO);
		panelFondo.add(btnCerrarSesion);
	}

	// ====================================================================
	// PANEL: SALUDO ('Bienvenido' + nombre del usuario)
	// ====================================================================
	/**
	 * "Bienvenido" en gris y el nombre grande en negro al lado.
	 */
	private void crearSaludo() {
		JLabel lblBienvenido = new JLabel("Bienvenido");
		lblBienvenido.setFont(Estilos.FUENTE_BIENVENIDO);
		lblBienvenido.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblBienvenido.setBounds(MARGEN, 122, 120, 30);
		panelFondo.add(lblBienvenido);

		lblNombre = new JLabel();
		lblNombre.setFont(Estilos.FUENTE_NOMBRE);
		lblNombre.setForeground(Estilos.COLOR_TEXTO);
		lblNombre.setBounds(187, 85, 600, 76);
		panelFondo.add(lblNombre);
	}

	// ====================================================================
	// PANEL: DATOS DEL USUARIO (nivel, tiempo total y estadisticas)
	// ====================================================================
	/**
	 * Filas de nivel, tiempo total y estadisticas, cada una con su icono,
	 * y la linea gris de debajo.
	 */
	private void crearDatosUsuario() {
		// El icono del nivel lo pone setNivel (cambia de color segun el nivel)
		lblIconoNivel = new JLabel();
		lblIconoNivel.setBounds(MARGEN, FILA_NIVEL_Y, 16, ALTO_FILA);
		panelFondo.add(lblIconoNivel);

		JLabel lblTextoNivel = crearTextoFila("Nivel - ", FILA_NIVEL_Y);
		panelFondo.add(lblTextoNivel);

		// El nivel actual va despues de "Nivel - " y despues el siguiente nivel.
		// Sus colores y posiciones los pone setNivel
		lblNivel = new JLabel();
		lblNivel.setFont(Estilos.FUENTE_MONO_NEGRITA);
		panelFondo.add(lblNivel);

		lblSiguienteNivel = new JLabel();
		lblSiguienteNivel.setFont(Estilos.FUENTE_MONO);
		panelFondo.add(lblSiguienteNivel);

		JLabel lblIconoTiempo = new JLabel(UtilidadesVista.cargarImagen(RUTA_TIEMPO));
		lblIconoTiempo.setBounds(MARGEN, FILA_TIEMPO_Y, 16, ALTO_FILA);
		panelFondo.add(lblIconoTiempo);

		lblTiempo = crearTextoFila("", FILA_TIEMPO_Y);
		panelFondo.add(lblTiempo);

		JLabel lblIconoEstadisticas = new JLabel(UtilidadesVista.cargarImagen(RUTA_ESTADISTICAS));
		lblIconoEstadisticas.setBounds(MARGEN, FILA_ESTADISTICAS_Y, 16, ALTO_FILA);
		panelFondo.add(lblIconoEstadisticas);

		lblEstadisticas = crearTextoFila("", FILA_ESTADISTICAS_Y);
		panelFondo.add(lblEstadisticas);

		// Linea gris de debajo de los datos
		JPanel separador = new JPanel();
		separador.setBackground(Estilos.COLOR_SEPARADOR);
		separador.setBounds(MARGEN, 248, 590, 2);
		panelFondo.add(separador);
	}

	/**
	 * Texto gris de una fila (va a la derecha del icono).
	 */
	private JLabel crearTextoFila(String texto, int y) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setFont(Estilos.FUENTE_MONO);
		etiqueta.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		etiqueta.setBounds(TEXTO_FILA_X, y, 500, ALTO_FILA);
		return etiqueta;
	}

	// ====================================================================
	// PANEL: TARJETAS WORKOUTS E HISTORIAL
	// ====================================================================
	private void crearTarjetas() {
		// WORKOUTS: imagen negra -> texto blanco
		btnWorkouts = crearTarjeta("WORKOUTS", "Ver tus rutinas", RUTA_WORKOUTS, RUTA_WORKOUTS_HOVER,
				Estilos.COLOR_TEXTO_CAJA, Estilos.COLOR_TEXTO_TARJETA);
		btnWorkouts.setBounds(MARGEN, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO);
		panelFondo.add(btnWorkouts);

		// HISTORIAL: imagen blanca -> texto negro
		btnHistorial = crearTarjeta("HISTORIAL", "Ver entrenamientos hechos", RUTA_HISTORIAL,
				RUTA_HISTORIAL_HOVER, Estilos.COLOR_TEXTO, Estilos.COLOR_TEXTO_SECUNDARIO);
		btnHistorial.setBounds(MARGEN + TARJETA_ANCHO + TARJETA_SEPARACION, TARJETA_Y, TARJETA_ANCHO,
				TARJETA_ALTO);
		panelFondo.add(btnHistorial);
	}

	// ====================================================================
	// PANEL: PIE DE PAGINA (abajo)
	// ====================================================================
	private void crearPie() {
		int anio = LocalDate.now().getYear();
		JLabel lblPie = new JLabel("© " + anio + " GymApp · Todos los derechos reservados");
		lblPie.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblPie.setForeground(Estilos.COLOR_PIE);
		lblPie.setHorizontalAlignment(SwingConstants.CENTER);
		Dimension tamano = panelFondo.getPreferredSize();
		lblPie.setBounds(0, tamano.height - 36, tamano.width, 22);
		panelFondo.add(lblPie);
	}

	// ====================================================================
	// AYUDA: BOTONES DE ICONO Y TARJETAS
	// ====================================================================
	/**
	 * Boton que solo es un icono. Con el raton encima sale el icono gris.
	 */
	private JButton crearBotonIcono(String rutaIcono, String rutaHover, String textoAyuda) {
		JButton boton = new JButton(UtilidadesVista.cargarImagen(rutaIcono));
		boton.setRolloverIcon(UtilidadesVista.cargarImagen(rutaHover));
		boton.setToolTipText(textoAyuda);
		quitarAspectoBoton(boton);
		return boton;
	}

	/**
	 * Tarjeta grande: un JButton con la imagen de fondo y dos JLabel dentro
	 * (titulo y subtitulo). Con el raton encima cambia a la imagen "_hover".
	 */
	private JButton crearTarjeta(String titulo, String subtitulo, String rutaImagen, String rutaHover,
			Color colorTitulo, Color colorSubtitulo) {
		JButton tarjeta = new JButton(UtilidadesVista.cargarImagen(rutaImagen));
		tarjeta.setRolloverIcon(UtilidadesVista.cargarImagen(rutaHover));
		quitarAspectoBoton(tarjeta);
		// Layout null para poner los textos encima de la imagen con setBounds
		tarjeta.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setFont(Estilos.FUENTE_TARJETA);
		lblTitulo.setForeground(colorTitulo);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 91, TARJETA_ANCHO, 40);
		tarjeta.add(lblTitulo);

		JLabel lblSubtitulo = new JLabel(subtitulo);
		lblSubtitulo.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblSubtitulo.setForeground(colorSubtitulo);
		lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblSubtitulo.setBounds(0, 129, TARJETA_ANCHO, 20);
		tarjeta.add(lblSubtitulo);

		return tarjeta;
	}

	/**
	 * Quita el fondo, el borde y el foco del boton: solo se ve su imagen.
	 */
	private void quitarAspectoBoton(JButton boton) {
		boton.setContentAreaFilled(false);
		boton.setBorderPainted(false);
		boton.setFocusPainted(false);
		boton.setFocusable(false);
		boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	public void setNombreUsuario(String nombre) {
		lblNombre.setText(nombre);
	}

	/**
	 * Pone el nivel actual con su color (y su icono) y detras el siguiente
	 * nivel con su color pero transparente, porque todavia no se ha
	 * conseguido: "Nivel - Intermedio → Avanzado".
	 * Si siguiente es null (ya esta en el ultimo nivel) sale "· Nivel máximo".
	 */
	public void setNivel(String actual, String siguiente) {
		// El nivel actual va justo despues de "Nivel - " (que ocupa 72 px)
		int x = TEXTO_FILA_X + 72;
		lblNivel.setText(actual);
		lblNivel.setForeground(obtenerColorNivel(actual));
		lblNivel.setBounds(x, FILA_NIVEL_Y, 200, ALTO_FILA);
		lblIconoNivel.setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_" + actual.toLowerCase() + ".png"));

		// stringWidth dice cuantos pixeles ocupa el texto: el siguiente nivel va justo detras
		int anchoNivel = lblNivel.getFontMetrics(lblNivel.getFont()).stringWidth(actual);
		lblSiguienteNivel.setBounds(x + anchoNivel, FILA_NIVEL_Y, 300, ALTO_FILA);

		if (siguiente == null) {
			lblSiguienteNivel.setText("  · Nivel máximo");
			lblSiguienteNivel.setForeground(Estilos.COLOR_PIE);
			lblSiguienteNivel.setToolTipText("Has llegado al nivel máximo");
		} else {
			lblSiguienteNivel.setText("  →  " + siguiente);
			// Mismo color pero transparente (el cuarto numero es la opacidad)
			Color color = obtenerColorNivel(siguiente);
			lblSiguienteNivel.setForeground(
					new Color(color.getRed(), color.getGreen(), color.getBlue(), OPACIDAD_SIGUIENTE_NIVEL));
			lblSiguienteNivel.setToolTipText("Completa un workout de nivel " + actual + " para subir a " + siguiente);
		}
	}

	/**
	 * Color de cada nivel. Si llega un nivel que no conoce, el de Intermedio.
	 */
	private Color obtenerColorNivel(String nivel) {
		if (nivel.equalsIgnoreCase("Novato")) {
			return Estilos.COLOR_NIVEL_NOVATO;
		} else if (nivel.equalsIgnoreCase("Principiante")) {
			return Estilos.COLOR_NIVEL_PRINCIPIANTE;
		} else if (nivel.equalsIgnoreCase("Avanzado")) {
			return Estilos.COLOR_NIVEL_AVANZADO;
		} else if (nivel.equalsIgnoreCase("Experto")) {
			return Estilos.COLOR_NIVEL_EXPERTO;
		}
		return Estilos.COLOR_NIVEL_INTERMEDIO;
	}

	/**
	 * Ejemplo: setTiempoTotal("0.5h") -> "Tiempo total entrenando: 0.5h"
	 */
	public void setTiempoTotal(String tiempo) {
		lblTiempo.setText("Tiempo total entrenando: " + tiempo);
	}

	/**
	 * Ejemplo: setEstadisticas(12, "ayer") -> "12 workouts completados · Último: ayer"
	 * Si ultimo es null no se pone la parte de "Último".
	 */
	public void setEstadisticas(int workoutsCompletados, String ultimo) {
		String texto;
		if (workoutsCompletados == 0) {
			texto = "Aún no has completado ningún workout";
		} else if (workoutsCompletados == 1) {
			texto = "1 workout completado";
		} else {
			texto = workoutsCompletados + " workouts completados";
		}

		if (workoutsCompletados > 0 && ultimo != null) {
			texto = texto + " · Último: " + ultimo;
		}
		lblEstadisticas.setText(texto);
	}

	/**
	 * Hace visible la ventana con la capa blanca encima y un Timer la va
	 * haciendo transparente poco a poco.
	 */
	public void mostrar() {
		panelFundido.setOpacidad(1f);
		panelFundido.setVisible(true);
		setVisible(true);

		timerFundido = new Timer(TIEMPO_PASO_FUNDIDO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				float opacidad = panelFundido.getOpacidad() - PASO_FUNDIDO;
				if (opacidad <= 0f) {
					timerFundido.stop();
					panelFundido.setVisible(false);
				} else {
					panelFundido.setOpacidad(opacidad);
				}
			}
		});
		timerFundido.start();
	}

	public void cerrar() {
		if (timerFundido != null) {
			timerFundido.stop();
		}
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JButton getBtnWorkouts() {
		return btnWorkouts;
	}

	public JButton getBtnHistorial() {
		return btnHistorial;
	}

	public JButton getBtnAjustes() {
		return btnAjustes;
	}

	public JButton getBtnCerrarSesion() {
		return btnCerrarSesion;
	}

	// ---------------------------------------------------------------
	// Capa blanca para la aparicion suave
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CAPA BLANCA DE LA APARICION SUAVE
	// ====================================================================
	/**
	 * Panel blanco que tapa toda la ventana. Se pone como "glass pane",
	 * que es una capa que Swing pinta por encima de todo lo demas.
	 * AlphaComposite pinta el blanco con la opacidad indicada
	 * (1 = blanco del todo, 0 = invisible).
	 */
	private class PanelFundido extends JPanel {

		private static final long serialVersionUID = 1L;

		private float opacidad;

		public PanelFundido() {
			setOpaque(false);
			opacidad = 1f;
		}

		public float getOpacidad() {
			return opacidad;
		}

		public void setOpacidad(float opacidad) {
			this.opacidad = opacidad;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
			g2.setColor(Estilos.COLOR_FONDO);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}
