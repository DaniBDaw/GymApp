package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Ventana del workout en curso (diseño "PANEL - Workout en curso" de Figma):
 * el ejercicio que se esta haciendo, el cronometro del workout y el del
 * ejercicio, la cuenta atras de la serie y del descanso, la lista de series,
 * el siguiente ejercicio y los botones SALIR y el de INICIAR / PAUSA /
 * SIGUIENTE. Solo es la parte visual: los cronometros cuentan en sus propios
 * hilos (controlador/Cronometro) y el controlador le pasa aqui los textos.
 */
public class VentanaWorkoutEnCurso extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/workout_en_curso_bg.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	private static final String RUTA_RELOJ = "images/iconos/reloj.png";
	private static final String RUTA_PLAY_BLANCO = "images/iconos/play_blanco.png";
	private static final String RUTA_PAUSA_BLANCO = "images/iconos/pausa_blanco.png";
	private static final String RUTA_SERIE_COMPLETADA = "images/iconos/serie_completada.png";

	// Estado de cada tarjeta de serie (lo elige el controlador)
	public static final int SERIE_PENDIENTE = 0;
	public static final int SERIE_ACTUAL = 1;
	public static final int SERIE_HECHA = 2;

	// Tarjetas de las series: posicion de la primera y distancia entre ellas
	private static final int TARJETA_X = 531;
	private static final int TARJETA_Y = 160;
	private static final int TARJETA_ANCHO = 342;
	private static final int TARJETA_ALTO = 83;
	private static final int TARJETA_SEPARACION = 96;
	// Hueco de la foto dentro de la tarjeta (las fotos son 600x400: 3 a 2)
	private static final int FOTO_ANCHO = 93;
	private static final int FOTO_ALTO = 63;

	// Si el nombre del ejercicio es largo se pone mas pequeño
	private static final int LARGO_NOMBRE_MAXIMO = 20;

	// Colores
	private static final Color COLOR_PAUSA = new Color(218, 182, 106);
	private static final Color COLOR_SIGUIENTE = new Color(76, 160, 80);
	private static final Color COLOR_GRIS_CAJA = new Color(133, 133, 133);
	private static final Color COLOR_DESCRIPCION = new Color(104, 104, 104);
	private static final Color COLOR_FOTO = new Color(217, 217, 217);
	private static final Color COLOR_FOTO_ACTUAL = new Color(70, 70, 70);
	private static final Color COLOR_TARJETA_HECHA = new Color(222, 222, 222);
	private static final Color COLOR_FOTO_HECHA = new Color(198, 198, 198);
	private static final Color COLOR_TEXTO_HECHO = new Color(175, 175, 175);

	// Fuentes
	private static final Font FUENTE_NOMBRE = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 40);
	private static final Font FUENTE_NOMBRE_LARGO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 30);
	private static final Font FUENTE_CRONOMETRO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 80);
	private static final Font FUENTE_CRONO_WORKOUT = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 34);
	private static final Font FUENTE_CUENTA_ATRAS = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 50);
	private static final Font FUENTE_NOMBRE_SERIE = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 18);
	private static final Font FUENTE_SIN_IMAGEN = new Font(Estilos.FAMILIA_FUENTE, Font.PLAIN, 11);
	private static final Font FUENTE_BOTON_NEGRITA = Estilos.FUENTE_BOTON.deriveFont(Font.BOLD);

	private PanelFondo panelFondo;
	private JLabel lblSubtitulo;
	private JLabel lblNombreEjercicio;
	private JTextArea txtDescripcion;
	private JLabel lblTiempoEjercicio;
	private JLabel lblTiempoWorkout;
	private EtiquetaUsuario etiquetaUsuario;
	private JLabel lblSiguiente;
	private CajaCuentaAtras cajaSerie;
	private CajaCuentaAtras cajaDescanso;
	private ArrayList<TarjetaSerie> tarjetas = new ArrayList<TarjetaSerie>();
	private BotonRedondo btnSalir;
	private BotonRedondo btnAccion;

	public VentanaWorkoutEnCurso() {
		setTitle("GymApp - Workout en curso");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearCronometroWorkout();
		crearDatosEjercicio();
		crearCronometroEjercicio();
		crearCuentasAtras();
		crearTituloSeries();
		crearSiguiente();
		crearBotones();

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: CABECERA (logo GymApp y usuario)
	// ====================================================================
	/**
	 * Logo + "GymApp" arriba a la izquierda y el nombre y el nivel del
	 * usuario arriba a la derecha.
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

		etiquetaUsuario = new EtiquetaUsuario();
		etiquetaUsuario.setBounds(TARJETA_X, 16, TARJETA_ANCHO, 24);
		panelFondo.add(etiquetaUsuario);
	}

	// ====================================================================
	// PANEL: CRONOMETRO DEL WORKOUT (arriba a la derecha)
	// ====================================================================
	/**
	 * Tiempo de todo el workout: empieza al pulsar INICIAR el primer
	 * ejercicio y sigue contando hasta que se termina o se sale.
	 */
	private void crearCronometroWorkout() {
		JLabel lblTitulo = new JLabel("TIEMPO DEL WORKOUT");
		lblTitulo.setFont(Estilos.FUENTE_MONO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setBounds(TARJETA_X, 66, 200, 22);
		panelFondo.add(lblTitulo);

		lblTiempoWorkout = new JLabel("00:00");
		lblTiempoWorkout.setFont(FUENTE_CRONO_WORKOUT);
		lblTiempoWorkout.setForeground(Estilos.COLOR_CAJA);
		lblTiempoWorkout.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTiempoWorkout.setBounds(TARJETA_X + 160, 54, TARJETA_ANCHO - 160, 44);
		panelFondo.add(lblTiempoWorkout);
	}

	// ====================================================================
	// PANEL: DATOS DEL EJERCICIO (workout, nombre y descripcion)
	// ====================================================================
	/**
	 * "Core express - Ejercicio 2 de 4", el nombre grande del ejercicio y su
	 * descripcion. La descripcion es un JTextArea porque puede ocupar varias
	 * lineas (un JLabel solo tiene una).
	 */
	private void crearDatosEjercicio() {
		lblSubtitulo = new JLabel();
		lblSubtitulo.setFont(Estilos.FUENTE_MONO);
		lblSubtitulo.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblSubtitulo.setBounds(41, 88, 460, 20);
		panelFondo.add(lblSubtitulo);

		lblNombreEjercicio = new JLabel();
		lblNombreEjercicio.setFont(FUENTE_NOMBRE);
		lblNombreEjercicio.setForeground(Estilos.COLOR_CAJA);
		lblNombreEjercicio.setBounds(40, 106, 470, 54);
		panelFondo.add(lblNombreEjercicio);

		txtDescripcion = new JTextArea();
		txtDescripcion.setFont(Estilos.FUENTE_MONO);
		txtDescripcion.setForeground(COLOR_DESCRIPCION);
		txtDescripcion.setLineWrap(true);
		txtDescripcion.setWrapStyleWord(true);
		txtDescripcion.setEditable(false);
		txtDescripcion.setFocusable(false);
		txtDescripcion.setOpaque(false);
		txtDescripcion.setBounds(41, 160, 465, 66);
		panelFondo.add(txtDescripcion);
	}

	// ====================================================================
	// PANEL: CRONOMETRO DEL EJERCICIO (tiempo grande + reloj)
	// ====================================================================
	private void crearCronometroEjercicio() {
		JLabel lblTitulo = new JLabel("TIEMPO DEL EJERCICIO");
		lblTitulo.setFont(Estilos.FUENTE_MONO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setBounds(41, 235, 300, 20);
		panelFondo.add(lblTitulo);

		lblTiempoEjercicio = new JLabel("00:00");
		lblTiempoEjercicio.setFont(FUENTE_CRONOMETRO);
		lblTiempoEjercicio.setForeground(Estilos.COLOR_CAJA);
		lblTiempoEjercicio.setBounds(37, 262, 225, 90);
		panelFondo.add(lblTiempoEjercicio);

		JLabel lblReloj = new JLabel(UtilidadesVista.cargarImagen(RUTA_RELOJ));
		lblReloj.setBounds(262, 285, 52, 52);
		panelFondo.add(lblReloj);
	}

	// ====================================================================
	// PANEL: CUENTAS ATRAS (caja de la serie y caja del descanso)
	// ====================================================================
	/**
	 * Dos cajas iguales. La que esta contando se pone negra y la otra blanca
	 * con borde gris (ver setDescansoActivo).
	 */
	private void crearCuentasAtras() {
		cajaSerie = new CajaCuentaAtras("SERIE 1 - QUEDA");
		cajaSerie.setBounds(41, 366, 179, 108);
		panelFondo.add(cajaSerie);

		cajaDescanso = new CajaCuentaAtras("DESCANSO");
		cajaDescanso.setBounds(255, 366, 179, 108);
		panelFondo.add(cajaDescanso);

		setDescansoActivo(false);
	}

	// ====================================================================
	// PANEL: SERIES (titulo; las tarjetas las crea cargarSeries)
	// ====================================================================
	private void crearTituloSeries() {
		JLabel lblSeries = new JLabel("SERIES");
		lblSeries.setFont(Estilos.FUENTE_MONO);
		lblSeries.setForeground(Estilos.COLOR_CAJA);
		lblSeries.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeries.setBounds(TARJETA_X, 116, TARJETA_ANCHO, 22);
		panelFondo.add(lblSeries);
	}

	// ====================================================================
	// PANEL: SIGUIENTE EJERCICIO (debajo de las series)
	// ====================================================================
	/**
	 * "● Siguiente: 2 - Crunch bicicleta" (igual que en la ventana de detalle).
	 * El texto lo pone setSiguiente.
	 */
	private void crearSiguiente() {
		JLabel lblPunto = new JLabel("●");
		lblPunto.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblPunto.setForeground(Estilos.COLOR_CAJA);
		lblPunto.setBounds(TARJETA_X, 545, 16, 22);
		panelFondo.add(lblPunto);

		lblSiguiente = new JLabel();
		lblSiguiente.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblSiguiente.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblSiguiente.setBounds(TARJETA_X + 22, 545, TARJETA_ANCHO - 22, 22);
		panelFondo.add(lblSiguiente);
	}

	// ====================================================================
	// PANEL: BOTONES DE ABAJO (SALIR / INICIAR - PAUSA - SIGUIENTE)
	// ====================================================================
	/**
	 * SALIR blanco con borde y el boton grande, que cambia de texto, color e
	 * icono segun lo que haga en cada momento (ver mostrarBoton...).
	 */
	private void crearBotones() {
		btnSalir = new BotonRedondo("SALIR", null);
		btnSalir.setFont(Estilos.FUENTE_BOTON);
		btnSalir.setBounds(41, 528, 121, 46);
		panelFondo.add(btnSalir);

		btnAccion = new BotonRedondo("INICIAR", Estilos.COLOR_BOTON_OSCURO);
		btnAccion.setFont(FUENTE_BOTON_NEGRITA);
		btnAccion.setIconTextGap(22);
		btnAccion.setBounds(201, 528, 233, 46);
		panelFondo.add(btnAccion);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: DATOS DEL EJERCICIO (metodos)
	// ====================================================================

	/**
	 * Ejemplo: setSubtitulo("Core express - Ejercicio 2 de 4")
	 */
	public void setSubtitulo(String texto) {
		lblSubtitulo.setText(texto);
	}

	/**
	 * Nombre y nivel del usuario. Ejemplo: setUsuario("Alexis", 2, "Intermedio")
	 */
	public void setUsuario(String nombre, int nivel, String nombreNivel) {
		etiquetaUsuario.setUsuario(nombre, nivel, nombreNivel);
	}

	/**
	 * Ejemplo: setSiguiente("2 - Crunch bicicleta"). Con null es que este es
	 * el ultimo ejercicio del workout.
	 */
	public void setSiguiente(String siguiente) {
		if (siguiente == null) {
			lblSiguiente.setText("Último ejercicio del workout");
		} else {
			lblSiguiente.setText("Siguiente: " + siguiente);
		}
	}

	/**
	 * Nombre y descripcion del ejercicio. Si el nombre es largo se pone mas
	 * pequeño para que quepa.
	 */
	public void setEjercicio(String nombre, String descripcion) {
		lblNombreEjercicio.setText(nombre);
		if (nombre.length() > LARGO_NOMBRE_MAXIMO) {
			lblNombreEjercicio.setFont(FUENTE_NOMBRE_LARGO);
		} else {
			lblNombreEjercicio.setFont(FUENTE_NOMBRE);
		}
		txtDescripcion.setText(descripcion);
	}

	// ====================================================================
	// PANEL: CRONOMETROS Y CUENTAS ATRAS (metodos)
	// ====================================================================

	/**
	 * Ejemplo: setTiempoWorkout("12:47")
	 */
	public void setTiempoWorkout(String tiempo) {
		lblTiempoWorkout.setText(tiempo);
	}

	/**
	 * Ejemplo: setTiempoEjercicio("02:13")
	 */
	public void setTiempoEjercicio(String tiempo) {
		lblTiempoEjercicio.setText(tiempo);
	}

	/**
	 * Ejemplo: setTiempoSerie("SERIE 2 - QUEDA", "00:27")
	 * o setTiempoSerie("SERIE 2 - PREPARADO", "00:05")
	 */
	public void setTiempoSerie(String titulo, String tiempo) {
		cajaSerie.setTitulo(titulo);
		cajaSerie.setTiempo(tiempo);
	}

	/**
	 * Ejemplo: setTiempoDescanso("DESCANSO - QUEDA", "00:15")
	 * o setTiempoDescanso("DESCANSO - USADO", "00:20")
	 */
	public void setTiempoDescanso(String titulo, String tiempo) {
		cajaDescanso.setTitulo(titulo);
		cajaDescanso.setTiempo(tiempo);
	}

	/**
	 * true: la caja negra es la del descanso. false: la de la serie.
	 */
	public void setDescansoActivo(boolean descanso) {
		cajaSerie.setActiva(!descanso);
		cajaDescanso.setActiva(descanso);
	}

	// ====================================================================
	// PANEL: SERIES (metodos)
	// ====================================================================

	/**
	 * Crea una tarjeta por serie, una debajo de otra. Ejemplo:
	 * nombres = { "Serie 1", "Serie 2" },
	 * detalles = { "20 repeticiones - 40 s", "15 repeticiones - 30 s" },
	 * fotos = { "https://...png", "https://...png" } (la URL de cada foto)
	 */
	public void cargarSeries(String[] nombres, String[] detalles, String[] fotos) {
		// Quitar las tarjetas del ejercicio anterior
		for (int i = 0; i < tarjetas.size(); i++) {
			panelFondo.remove(tarjetas.get(i));
		}
		tarjetas.clear();

		for (int i = 0; i < nombres.length; i++) {
			TarjetaSerie tarjeta = new TarjetaSerie(nombres[i], detalles[i]);
			tarjeta.setBounds(TARJETA_X, TARJETA_Y + i * TARJETA_SEPARACION, TARJETA_ANCHO, TARJETA_ALTO);
			panelFondo.add(tarjeta);
			tarjetas.add(tarjeta);
			tarjeta.cargarFoto(fotos[i]);
		}
		panelFondo.repaint();
	}

	/**
	 * Cambia una tarjeta a SERIE_PENDIENTE, SERIE_ACTUAL o SERIE_HECHA.
	 */
	public void setEstadoSerie(int indice, int estado) {
		if (indice >= 0 && indice < tarjetas.size()) {
			tarjetas.get(indice).setEstado(estado);
		}
	}

	// ====================================================================
	// PANEL: BOTONES DE ABAJO (metodos)
	// ====================================================================

	public void mostrarBotonIniciar() {
		cambiarBoton("INICIAR", Estilos.COLOR_BOTON_OSCURO, RUTA_PLAY_BLANCO);
	}

	public void mostrarBotonPausa() {
		cambiarBoton("PAUSA", COLOR_PAUSA, RUTA_PAUSA_BLANCO);
	}

	public void mostrarBotonReanudar() {
		cambiarBoton("REANUDAR", Estilos.COLOR_BOTON_OSCURO, RUTA_PLAY_BLANCO);
	}

	public void mostrarBotonSiguiente() {
		cambiarBoton("SIGUIENTE", COLOR_SIGUIENTE, RUTA_PLAY_BLANCO);
	}

	public void mostrarBotonResumen() {
		cambiarBoton("VER RESUMEN", COLOR_SIGUIENTE, RUTA_PLAY_BLANCO);
	}

	private void cambiarBoton(String texto, Color color, String rutaIcono) {
		btnAccion.setText(texto);
		btnAccion.setColorFondo(color);
		btnAccion.setIcon(UtilidadesVista.cargarImagen(rutaIcono));
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JButton getBtnSalir() {
		return btnSalir;
	}

	public JButton getBtnAccion() {
		return btnAccion;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CAJA DE CUENTA ATRAS (serie o descanso)
	// ====================================================================
	/**
	 * Caja con un titulo pequeño y el tiempo grande. Activa: negra con texto
	 * blanco. No activa: blanca con borde y texto gris.
	 */
	private class CajaCuentaAtras extends JPanel {

		private static final long serialVersionUID = 1L;

		private JLabel lblTitulo;
		private JLabel lblTiempo;
		private boolean activa;

		public CajaCuentaAtras(String titulo) {
			setLayout(null);
			setOpaque(false);

			lblTitulo = new JLabel(titulo);
			lblTitulo.setFont(Estilos.FUENTE_MONO_PEQUENA);
			lblTitulo.setBounds(16, 12, 160, 20);
			add(lblTitulo);

			lblTiempo = new JLabel("00:00");
			lblTiempo.setFont(FUENTE_CUENTA_ATRAS);
			lblTiempo.setBounds(14, 36, 165, 64);
			add(lblTiempo);
		}

		public void setTitulo(String titulo) {
			lblTitulo.setText(titulo);
		}

		public void setTiempo(String tiempo) {
			lblTiempo.setText(tiempo);
		}

		public void setActiva(boolean activa) {
			this.activa = activa;
			if (activa) {
				lblTitulo.setForeground(Estilos.COLOR_TEXTO_CAJA);
				lblTiempo.setForeground(Estilos.COLOR_TEXTO_CAJA);
			} else {
				lblTitulo.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
				lblTiempo.setForeground(COLOR_GRIS_CAJA);
			}
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (activa) {
				g2.setColor(Estilos.COLOR_CAJA);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
			} else {
				g2.setColor(Estilos.COLOR_FONDO);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(COLOR_GRIS_CAJA);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
			}
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: TARJETA DE UNA SERIE (foto, nombre, repeticiones y tiempo)
	// ====================================================================
	/**
	 * Tarjeta de una serie. Tiene tres aspectos:
	 *  - pendiente: blanca con borde negro
	 *  - actual: negra con el texto blanco (la que se esta haciendo)
	 *  - hecha: gris, y el check verde "cae" en el centro como un sello
	 */
	private class TarjetaSerie extends JPanel {

		private static final long serialVersionUID = 1L;

		// Check en el centro de la tarjeta: (342 - 52) / 2 y (83 - 64) / 2
		private static final int CHECK_ANCHO = 52;
		private static final int CHECK_ALTO = 64;
		private static final int CHECK_X = (TARJETA_ANCHO - CHECK_ANCHO) / 2;
		private static final int CHECK_Y_FINAL = (TARJETA_ALTO - CHECK_ALTO) / 2;
		// Animacion del check: baja 8 px cada 15 ms hasta su sitio
		private static final int PASO_CAIDA = 8;
		private static final int TIEMPO_PASO_CAIDA = 15;

		private int estado = SERIE_PENDIENTE;
		private JLabel lblFoto;
		private JLabel lblNombre;
		private JLabel lblDetalle;
		private JLabel lblCheck;
		private Timer timerCaida;

		public TarjetaSerie(String nombre, String detalle) {
			setLayout(null);
			setOpaque(false);

			// El check se anade primero para que se pinte encima de todo lo demas
			ImageIcon iconoCheck = UtilidadesVista.cargarImagen(RUTA_SERIE_COMPLETADA);
			lblCheck = new JLabel(iconoCheck);
			lblCheck.setBounds(CHECK_X, CHECK_Y_FINAL, CHECK_ANCHO, CHECK_ALTO);
			lblCheck.setVisible(false);
			add(lblCheck);

			// Hueco gris para la foto de la serie (gris mientras se descarga)
			lblFoto = new JLabel();
			lblFoto.setOpaque(true);
			lblFoto.setFont(FUENTE_SIN_IMAGEN);
			lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
			lblFoto.setBounds(12, 10, FOTO_ANCHO, FOTO_ALTO);
			add(lblFoto);

			lblNombre = new JLabel(nombre);
			lblNombre.setFont(FUENTE_NOMBRE_SERIE);
			lblNombre.setBounds(114, 12, 220, 26);
			add(lblNombre);

			lblDetalle = new JLabel(detalle);
			lblDetalle.setFont(Estilos.FUENTE_MONO);
			lblDetalle.setBounds(114, 37, 225, 24);
			add(lblDetalle);

			setEstado(SERIE_PENDIENTE);
		}

		/**
		 * Descarga la foto en otro hilo, para que la ventana no se congele
		 * mientras tanto (la tarjeta se ve gris). Cuando termina, la pone con
		 * invokeLater (los cambios en la ventana se hacen en el hilo de
		 * Swing). Si no hay foto o no se puede descargar: "Sin imagen".
		 */
		public void cargarFoto(final String rutaImagen) {
			Thread hiloFoto = new Thread(new Runnable() {
				@Override
				public void run() {
					final ImageIcon foto = UtilidadesVista.cargarImagenUrl(rutaImagen, FOTO_ANCHO, FOTO_ALTO);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (foto != null) {
								lblFoto.setIcon(foto);
							} else {
								lblFoto.setText("Sin imagen");
							}
						}
					});
				}
			});
			hiloFoto.start();
		}

		public void setEstado(int nuevoEstado) {
			estado = nuevoEstado;
			if (estado == SERIE_ACTUAL) {
				lblFoto.setBackground(COLOR_FOTO_ACTUAL);
				lblFoto.setForeground(Estilos.COLOR_TEXTO_CAJA);
				lblNombre.setForeground(Estilos.COLOR_TEXTO_CAJA);
				lblDetalle.setForeground(Estilos.COLOR_TEXTO_CAJA);
				lblCheck.setVisible(false);
			} else if (estado == SERIE_HECHA) {
				lblFoto.setBackground(COLOR_FOTO_HECHA);
				lblFoto.setForeground(COLOR_TEXTO_HECHO);
				lblNombre.setForeground(COLOR_TEXTO_HECHO);
				lblDetalle.setForeground(COLOR_TEXTO_HECHO);
				dejarCaerCheck();
			} else {
				lblFoto.setBackground(COLOR_FOTO);
				lblFoto.setForeground(COLOR_DESCRIPCION);
				lblNombre.setForeground(Estilos.COLOR_CAJA);
				lblDetalle.setForeground(Estilos.COLOR_CAJA);
				lblCheck.setVisible(false);
			}
			repaint();
		}

		/**
		 * El check empieza por encima de la tarjeta (fuera de la vista) y baja
		 * hasta su sitio (solo visual).
		 */
		private void dejarCaerCheck() {
			lblCheck.setLocation(CHECK_X, -lblCheck.getHeight());
			lblCheck.setVisible(true);

			timerCaida = new Timer(TIEMPO_PASO_CAIDA, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int y = lblCheck.getY() + PASO_CAIDA;
					if (y >= CHECK_Y_FINAL) {
						y = CHECK_Y_FINAL;
						timerCaida.stop();
					}
					lblCheck.setLocation(CHECK_X, y);
				}
			});
			timerCaida.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (estado == SERIE_ACTUAL) {
				g2.setColor(Estilos.COLOR_CAJA);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			} else if (estado == SERIE_HECHA) {
				g2.setColor(COLOR_TARJETA_HECHA);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			} else {
				g2.setColor(Estilos.COLOR_FONDO);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(Estilos.COLOR_CAJA);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
			}
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: BOTONES REDONDEADOS (SALIR y el boton grande)
	// ====================================================================
	/**
	 * Boton con las esquinas redondeadas. Con colorFondo = null es blanco con
	 * borde negro; si no, es de ese color con el texto blanco. Con el raton
	 * encima el color se oscurece un poco (darker), menos el negro, que se
	 * pone gris.
	 */
	private class BotonRedondo extends JButton {

		private static final long serialVersionUID = 1L;

		private Color colorFondo;

		public BotonRedondo(String texto, Color colorFondo) {
			super(texto);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			setColorFondo(colorFondo);
		}

		public void setColorFondo(Color colorFondo) {
			this.colorFondo = colorFondo;
			if (colorFondo == null) {
				setForeground(Estilos.COLOR_CAJA);
			} else {
				setForeground(Estilos.COLOR_TEXTO_CAJA);
			}
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			boolean encima = getModel().isRollover() || getModel().isPressed();

			if (colorFondo == null) {
				if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_FONDO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
				g2.setColor(Estilos.COLOR_CAJA);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			} else {
				if (encima && colorFondo == Estilos.COLOR_BOTON_OSCURO) {
					// El negro ya no se puede oscurecer mas: se aclara (como los
					// botones negros de las otras ventanas)
					g2.setColor(Estilos.COLOR_BOTON_OSCURO_HOVER);
				} else if (encima) {
					g2.setColor(colorFondo.darker());
				} else {
					g2.setColor(colorFondo);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
