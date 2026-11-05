package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Ventana con el detalle de un workout (diseño "PANEL - Detalle Workout" de
 * Figma): nombre, nivel, duracion, boton del video, tabla de ejercicios y los
 * botones VOLVER y CONTINUAR. Solo es la parte visual: el controlador le pasa
 * los textos ya preparados y escucha los botones.
 */
public class VentanaDetalleWorkout extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/detalle_workout_bg.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	private static final String RUTA_TIEMPO = "images/iconos/tiempo.png";
	private static final String RUTA_PLAY_NEGRO = "images/iconos/play_negro.png";
	private static final String RUTA_PLAY_BLANCO = "images/iconos/play_blanco.png";
	private static final String RUTA_HECHO = "images/iconos/estado_hecho.png";
	private static final String RUTA_PENDIENTE = "images/iconos/estado_pendiente.png";

	// Texto de la columna Estado que pone el icono verde (si no, el gris)
	public static final String ESTADO_HECHO = "hecho";
	public static final String ESTADO_PENDIENTE = "pendiente";

	// Columnas de la tabla. Desde "Series" el texto va centrado
	private static final String[] COLUMNAS = { "N°", "Ejercicio", "Series", "Descanso", "Duración", "Estado" };
	private static final int[] ANCHO_COLUMNAS = { 108, 278, 88, 118, 110, 95 };
	private static final int PRIMERA_COLUMNA_CENTRADA = 2;
	private static final int COLUMNA_ESTADO = 5;

	// Posicion y tamano de la tabla (cabecera + 4 filas de 52 px)
	private static final int TABLA_X = 54;
	private static final int TABLA_Y = 245;
	private static final int TABLA_ANCHO = 797;
	private static final int TABLA_ALTO = 260;
	private static final int ALTO_FILA = 52;
	private static final int MARGEN_CELDA = 17;

	// Color de cada nivel: la posicion es el numero de nivel (0 a 4)
	private static final Color[] COLORES_NIVEL = { Estilos.COLOR_NIVEL_NOVATO, Estilos.COLOR_NIVEL_PRINCIPIANTE,
			Estilos.COLOR_NIVEL_INTERMEDIO, Estilos.COLOR_NIVEL_AVANZADO, Estilos.COLOR_NIVEL_EXPERTO };

	// Colores
	private static final Color COLOR_FILA_GRIS = new Color(242, 242, 242);
	private static final Color COLOR_LINEA_FILA = new Color(177, 177, 177);
	private static final Color COLOR_DESACTIVADO = new Color(170, 170, 170);

	// Fuentes
	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 44);
	private static final Font FUENTE_TITULO_LARGO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 32);
	private static final int LARGO_TITULO_MAXIMO = 16;
	private static final Font FUENTE_TABLA = Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 17f);

	private PanelFondo panelFondo;
	private JLabel lblTitulo;
	private JLabel lblIconoNivel;
	private JLabel lblNivel;
	private JLabel lblNombreNivel;
	private JLabel lblDuracion;
	private DefaultTableModel modeloTabla;
	private JTable tabla;
	private JLabel lblSiguiente;
	private EtiquetaUsuario etiquetaUsuario;
	private JButton btnVerVideo;
	private JButton btnVolver;
	private JButton btnContinuar;

	// Iconos de la columna Estado (se cargan una vez)
	private ImageIcon iconoHecho = UtilidadesVista.cargarImagen(RUTA_HECHO);
	private ImageIcon iconoPendiente = UtilidadesVista.cargarImagen(RUTA_PENDIENTE);

	public VentanaDetalleWorkout() {
		setTitle("GymApp - Detalle Workout");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearDatosWorkout();
		crearTabla();
		crearParteDeAbajo();

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
	 * Logo + "GymApp" arriba a la izquierda (igual que la home y workouts) y
	 * el nombre y el nivel del usuario arriba a la derecha.
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
		etiquetaUsuario.setBounds(560, 16, 290, 24);
		panelFondo.add(etiquetaUsuario);
	}

	// ====================================================================
	// PANEL: DATOS DEL WORKOUT (nombre, nivel, duracion y boton Ver video)
	// ====================================================================
	/**
	 * Nombre del workout, nivel (icono + texto de su color), duracion, la linea
	 * gris de debajo y el boton "Ver video" a la derecha.
	 */
	private void crearDatosWorkout() {
		lblTitulo = new JLabel();
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setBounds(44, 98, 590, 60);
		panelFondo.add(lblTitulo);

		lblIconoNivel = new JLabel();
		lblIconoNivel.setBounds(48, 162, 16, 16);
		panelFondo.add(lblIconoNivel);

		// "Nivel " normal y "2 - Intermedio" en negrita (dos etiquetas)
		lblNivel = new JLabel("Nivel");
		lblNivel.setFont(Estilos.FUENTE_MONO);
		lblNivel.setBounds(71, 158, 54, 24);
		panelFondo.add(lblNivel);

		lblNombreNivel = new JLabel();
		lblNombreNivel.setFont(Estilos.FUENTE_MONO_NEGRITA);
		lblNombreNivel.setBounds(125, 158, 400, 24);
		panelFondo.add(lblNombreNivel);

		JLabel lblIconoTiempo = new JLabel(UtilidadesVista.cargarImagen(RUTA_TIEMPO));
		lblIconoTiempo.setBounds(48, 187, 16, 16);
		panelFondo.add(lblIconoTiempo);

		lblDuracion = new JLabel();
		lblDuracion.setFont(Estilos.FUENTE_MONO);
		lblDuracion.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblDuracion.setBounds(71, 183, 500, 24);
		panelFondo.add(lblDuracion);

		// Linea gris de separacion (un panel de 1 px de alto)
		JPanel separador = new JPanel();
		separador.setBackground(Estilos.COLOR_SEPARADOR);
		separador.setBounds(46, 223, 805, 1);
		panelFondo.add(separador);

		btnVerVideo = new BotonRedondo("Ver video", false, 24);
		btnVerVideo.setFont(Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 18f));
		btnVerVideo.setIcon(UtilidadesVista.cargarImagen(RUTA_PLAY_NEGRO));
		btnVerVideo.setIconTextGap(18);
		btnVerVideo.setBounds(661, 109, 189, 97);
		panelFondo.add(btnVerVideo);
	}

	// ====================================================================
	// PANEL: TABLA DE EJERCICIOS
	// ====================================================================
	/**
	 * Tabla de ejercicios: cabecera negra y filas que alternan blanco y gris.
	 * No se pueden seleccionar filas, solo es informativa.
	 */
	private void crearTabla() {
		// Modelo que no deja editar las celdas
		modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int fila, int columna) {
				return false;
			}
		};

		tabla = new JTable(modeloTabla);
		tabla.setRowHeight(ALTO_FILA);
		tabla.setShowGrid(false);
		tabla.setIntercellSpacing(new Dimension(0, 0));
		tabla.setRowSelectionAllowed(false);
		tabla.setFocusable(false);
		tabla.setFillsViewportHeight(true);
		tabla.setBackground(Estilos.COLOR_FONDO);
		tabla.setDefaultRenderer(Object.class, new RenderCelda());

		JTableHeader cabecera = tabla.getTableHeader();
		cabecera.setReorderingAllowed(false);
		cabecera.setResizingAllowed(false);
		cabecera.setPreferredSize(new Dimension(TABLA_ANCHO, ALTO_FILA));
		cabecera.setDefaultRenderer(new RenderCabecera());

		for (int i = 0; i < ANCHO_COLUMNAS.length; i++) {
			tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHO_COLUMNAS[i]);
		}

		JScrollPane scrollTabla = new JScrollPane(tabla);
		scrollTabla.setBorder(BorderFactory.createEmptyBorder());
		scrollTabla.getViewport().setBackground(Estilos.COLOR_FONDO);
		scrollTabla.setBounds(TABLA_X, TABLA_Y, TABLA_ANCHO, TABLA_ALTO);
		panelFondo.add(scrollTabla);
	}

	// ====================================================================
	// PANEL: PARTE DE ABAJO (siguiente ejercicio, VOLVER y CONTINUAR)
	// ====================================================================
	/**
	 * Abajo: "● Siguiente: ..." a la izquierda, VOLVER (blanco) y CONTINUAR
	 * (negro con el icono de play) a la derecha.
	 */
	private void crearParteDeAbajo() {
		JLabel lblPunto = new JLabel("●");
		lblPunto.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblPunto.setForeground(Estilos.COLOR_CAJA);
		lblPunto.setBounds(55, 520, 16, 22);
		panelFondo.add(lblPunto);

		lblSiguiente = new JLabel();
		lblSiguiente.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblSiguiente.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblSiguiente.setBounds(77, 520, 420, 22);
		panelFondo.add(lblSiguiente);

		btnVolver = new BotonRedondo("Volver", false, Estilos.ARCO_BOTON);
		btnVolver.setFont(Estilos.FUENTE_BOTON);
		btnVolver.setBounds(516, 525, 121, 46);
		panelFondo.add(btnVolver);

		// El texto a la izquierda y el icono a la derecha
		btnContinuar = new BotonRedondo("CONTINUAR", true, Estilos.ARCO_BOTON);
		btnContinuar.setFont(Estilos.FUENTE_BOTON);
		btnContinuar.setIcon(UtilidadesVista.cargarImagen(RUTA_PLAY_BLANCO));
		btnContinuar.setHorizontalTextPosition(SwingConstants.LEFT);
		btnContinuar.setIconTextGap(24);
		btnContinuar.setBounds(661, 525, 189, 46);
		panelFondo.add(btnContinuar);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	/**
	 * Nombre del workout. Si es largo se pone mas pequeño para que no choque
	 * con el dibujo negro del fondo.
	 */
	public void setTitulo(String nombre) {
		lblTitulo.setText(nombre);
		if (nombre.length() > LARGO_TITULO_MAXIMO) {
			lblTitulo.setFont(FUENTE_TITULO_LARGO);
		} else {
			lblTitulo.setFont(FUENTE_TITULO);
		}
	}

	/**
	 * Ejemplo: setNivel(2, "Intermedio") -> "Nivel 2 - Intermedio" con el icono
	 * y el color de ese nivel. Un nivel sin numero (negativo) sale solo con su
	 * nombre y en negro.
	 */
	public void setNivel(int nivel, String nombreNivel) {
		lblIconoNivel.setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_" + nombreNivel.toLowerCase() + ".png"));
		if (nivel >= 0 && nivel < COLORES_NIVEL.length) {
			lblNombreNivel.setText(nivel + " - " + nombreNivel);
			lblNivel.setForeground(COLORES_NIVEL[nivel]);
			lblNombreNivel.setForeground(COLORES_NIVEL[nivel]);
		} else {
			lblNombreNivel.setText(nombreNivel);
			lblNivel.setForeground(Estilos.COLOR_CAJA);
			lblNombreNivel.setForeground(Estilos.COLOR_CAJA);
		}
	}

	/**
	 * Nombre y nivel del usuario (arriba a la derecha).
	 * Ejemplo: setUsuario("Alexis", 2, "Intermedio")
	 */
	public void setUsuario(String nombre, int nivel, String nombreNivel) {
		etiquetaUsuario.setUsuario(nombre, nivel, nombreNivel);
	}

	/**
	 * Ejemplo: setDuracion("12:00", 4) -> "Duración estimada: 12:00 - 4 Ejercicios"
	 */
	public void setDuracion(String duracion, int numEjercicios) {
		lblDuracion.setText("Duración estimada: " + duracion + " - " + numEjercicios + " Ejercicios");
	}

	/**
	 * Rellena la tabla. Cada fila: { n°, nombre, series, descanso, duracion,
	 * estado } y el estado es ESTADO_HECHO o ESTADO_PENDIENTE. Si hay mas de 4
	 * ejercicios las filas se hacen mas bajas para que quepan todas.
	 */
	public void cargarEjercicios(String[][] filas) {
		modeloTabla.setRowCount(0);
		for (int i = 0; i < filas.length; i++) {
			modeloTabla.addRow(filas[i]);
		}

		int espacioFilas = TABLA_ALTO - ALTO_FILA;
		if (filas.length * ALTO_FILA > espacioFilas) {
			tabla.setRowHeight(espacioFilas / filas.length);
		} else {
			tabla.setRowHeight(ALTO_FILA);
		}
	}

	/**
	 * Ejemplo: setSiguiente("2 - Crunch bicicleta"). Con null es que ya estan
	 * todos hechos: se avisa y se desactiva CONTINUAR.
	 */
	public void setSiguiente(String siguiente) {
		if (siguiente == null) {
			lblSiguiente.setText("Todos los ejercicios completados");
			btnContinuar.setEnabled(false);
		} else {
			lblSiguiente.setText("Siguiente: " + siguiente);
			btnContinuar.setEnabled(true);
		}
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JButton getBtnVerVideo() {
		return btnVerVideo;
	}

	public JButton getBtnVolver() {
		return btnVolver;
	}

	public JButton getBtnContinuar() {
		return btnContinuar;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CELDAS DE LA TABLA (filas e iconos de estado)
	// ====================================================================
	/**
	 * Celdas: filas blanco/gris con lineas grises, texto centrado desde la
	 * columna Series y en Estado el icono verde (hecho) o gris (pendiente).
	 */
	private class RenderCelda extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
				boolean conFoco, int fila, int columna) {
			super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
			setFont(FUENTE_TABLA);
			setForeground(Estilos.COLOR_CAJA);
			setIcon(null);

			if (columna >= PRIMERA_COLUMNA_CENTRADA) {
				setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				setHorizontalAlignment(SwingConstants.LEFT);
			}

			// Columna Estado: solo el icono, sin texto
			if (columna == COLUMNA_ESTADO) {
				if (ESTADO_HECHO.equals(valor)) {
					setIcon(iconoHecho);
				} else {
					setIcon(iconoPendiente);
				}
				setText("");
			}

			// Filas pares blancas, impares grises con una linea arriba y abajo
			Border linea;
			if (fila % 2 == 0) {
				setBackground(Estilos.COLOR_FONDO);
				linea = BorderFactory.createEmptyBorder(1, 0, 1, 0);
			} else {
				setBackground(COLOR_FILA_GRIS);
				linea = BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_LINEA_FILA);
			}
			Border margen = BorderFactory.createEmptyBorder(0, MARGEN_CELDA, 0, 4);
			setBorder(BorderFactory.createCompoundBorder(linea, margen));
			return this;
		}
	}

	// ====================================================================
	// DIBUJO: CABECERA NEGRA DE LA TABLA
	// ====================================================================
	/**
	 * Cabecera de la tabla: fondo negro y texto blanco.
	 */
	private class RenderCabecera extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
				boolean conFoco, int fila, int columna) {
			super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
			setFont(FUENTE_TABLA);
			setForeground(Estilos.COLOR_TEXTO_CAJA);
			setBackground(Estilos.COLOR_CAJA);
			if (columna >= PRIMERA_COLUMNA_CENTRADA) {
				setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				setHorizontalAlignment(SwingConstants.LEFT);
			}
			setBorder(BorderFactory.createEmptyBorder(0, MARGEN_CELDA, 0, 4));
			return this;
		}
	}

	// ====================================================================
	// DIBUJO: BOTONES REDONDEADOS (Ver video, VOLVER, CONTINUAR)
	// ====================================================================
	/**
	 * Boton con las esquinas redondeadas: negro con texto blanco (oscuro = true) o
	 * blanco con borde negro. Con el raton encima se pone gris. Desactivado: gris
	 * claro. "redondeo" es lo redondas que son las esquinas.
	 */
	private class BotonRedondo extends JButton {

		private static final long serialVersionUID = 1L;

		private boolean oscuro;
		private int redondeo;

		public BotonRedondo(String texto, boolean oscuro, int redondeo) {
			super(texto);
			this.oscuro = oscuro;
			this.redondeo = redondeo;
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			if (oscuro) {
				setForeground(Estilos.COLOR_TEXTO_CAJA);
			} else {
				setForeground(Estilos.COLOR_CAJA);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			boolean encima = isEnabled() && (getModel().isRollover() || getModel().isPressed());

			if (oscuro) {
				if (!isEnabled()) {
					g2.setColor(COLOR_DESACTIVADO);
				} else if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), redondeo, redondeo);
			} else {
				if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_FONDO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), redondeo, redondeo);
				g2.setColor(Estilos.COLOR_CAJA);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, redondeo, redondeo);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
