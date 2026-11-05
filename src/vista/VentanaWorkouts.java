package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Ventana con la lista de workouts (diseño "PANEL - WORKOUTS" de Figma).
 * Solo es la parte visual: el controlador le pasa las filas ya preparadas
 * (nombre, nivel, ejercicios, duracion) y escucha el filtro y los botones.
 */
public class VentanaWorkouts extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/workouts_background.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	private static final String RUTA_PLAY_NEGRO = "images/iconos/play_negro.png";

	// Columnas de la tabla (mismo estilo que la tabla de "Detalle Workout")
	private static final String[] COLUMNAS = { "Nombre", "Nivel", "Ejercicios", "Duración", "Vídeo" };
	private static final int[] ANCHO_COLUMNAS = { 280, 200, 130, 108, 80 };
	private static final int COLUMNA_VIDEO = 4;

	// Lo que pone el controlador en la columna Vídeo si el workout tiene enlace
	public static final String CON_VIDEO = "Sí";

	// Posicion y tamano de la tabla
	private static final int TABLA_X = 51;
	private static final int TABLA_Y = 232;
	private static final int TABLA_ANCHO = 798;
	private static final int TABLA_ALTO = 286;
	private static final int ALTO_FILA = 52;
	private static final int MARGEN_CELDA = 17;

	// Colores de la tabla
	private static final Color COLOR_FILA_GRIS = new Color(242, 242, 242);
	private static final Color COLOR_LINEA_FILA = new Color(186, 186, 186);
	private static final Color COLOR_COMBO = new Color(23, 23, 23);
	private static final Color COLOR_DESACTIVADO = new Color(170, 170, 170);

	// Fuentes
	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 56);
	private static final Font FUENTE_TABLA = Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 17f);

	private PanelFondo panelFondo;
	private JComboBox<String> comboNivel;
	private JLabel lblSinConexion;
	private EtiquetaUsuario etiquetaUsuario;
	private DefaultTableModel modeloTabla;
	private JTable tabla;
	private JScrollPane scrollTabla;
	private JLabel lblVacio;
	private JButton btnAtras;
	private JButton btnHistorico;
	private JButton btnVerWorkout;

	public VentanaWorkouts() {
		setTitle("GymApp - Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearFiltro();
		crearTabla();
		crearBotones();

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: CABECERA (logo GymApp y titulo 'Workouts')
	// ====================================================================
	/**
	 * Logo + "GymApp" arriba a la izquierda (igual que la home) y el titulo.
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

		JLabel lblTitulo = new JLabel("Workouts");
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 84, panelFondo.getPreferredSize().width, 76);
		panelFondo.add(lblTitulo);
	}

	// ====================================================================
	// PANEL: FILTRO DE NIVEL (desplegable, aviso sin conexion y usuario)
	// ====================================================================
	/**
	 * Desplegable oscuro del filtro de nivel ("TODOS" en el diseño), un texto
	 * gris que solo sale en modo sin conexion y, a la derecha, el nombre y el
	 * nivel del usuario.
	 */
	private void crearFiltro() {
		comboNivel = new JComboBox<String>();
		comboNivel.setFont(Estilos.FUENTE_MONO);
		comboNivel.setBackground(COLOR_COMBO);
		comboNivel.setForeground(Estilos.COLOR_TEXTO_CAJA);
		comboNivel.setBorder(BorderFactory.createEmptyBorder());
		comboNivel.setFocusable(false);
		comboNivel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboNivel.setUI(new ComboOscuroUI());
		comboNivel.setRenderer(new RenderOpcionCombo());
		comboNivel.setBounds(TABLA_X, 180, 210, 38);
		panelFondo.add(comboNivel);

		lblSinConexion = new JLabel("Sin conexión: datos del ordenador");
		lblSinConexion.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblSinConexion.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblSinConexion.setBounds(TABLA_X + 230, 180, 280, 38);
		lblSinConexion.setVisible(false);
		panelFondo.add(lblSinConexion);

		etiquetaUsuario = new EtiquetaUsuario();
		etiquetaUsuario.setBounds(TABLA_X + 510, 188, TABLA_ANCHO - 510, 24);
		panelFondo.add(etiquetaUsuario);
	}

	// ====================================================================
	// PANEL: TABLA DE WORKOUTS
	// ====================================================================
	/**
	 * Tabla con cabecera negra y filas de 52 px que alternan blanco y gris.
	 * La fila seleccionada se marca con un borde negro.
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
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.setFillsViewportHeight(true);
		tabla.setBackground(Estilos.COLOR_FONDO);
		tabla.setFocusable(false);
		tabla.setCursor(new Cursor(Cursor.HAND_CURSOR));
		tabla.setDefaultRenderer(Object.class, new RenderCelda());

		// Cabecera negra con texto blanco
		JTableHeader cabecera = tabla.getTableHeader();
		cabecera.setReorderingAllowed(false);
		cabecera.setResizingAllowed(false);
		cabecera.setPreferredSize(new Dimension(TABLA_ANCHO, ALTO_FILA));
		cabecera.setDefaultRenderer(new RenderCabecera());

		for (int i = 0; i < ANCHO_COLUMNAS.length; i++) {
			tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHO_COLUMNAS[i]);
		}

		scrollTabla = new JScrollPane(tabla);
		scrollTabla.setBorder(BorderFactory.createEmptyBorder());
		scrollTabla.getViewport().setBackground(Estilos.COLOR_FONDO);
		scrollTabla.getVerticalScrollBar().setUI(new ScrollFinoUI());
		scrollTabla.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollTabla.getVerticalScrollBar().setUnitIncrement(ALTO_FILA / 2);
		scrollTabla.setBounds(TABLA_X, TABLA_Y, TABLA_ANCHO, TABLA_ALTO);

		// Mensaje cuando no hay workouts (va encima de la tabla vacia)
		lblVacio = new JLabel("No hay workouts para este nivel");
		lblVacio.setFont(Estilos.FUENTE_MONO);
		lblVacio.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
		lblVacio.setBounds(TABLA_X, TABLA_Y + ALTO_FILA + 30, TABLA_ANCHO, 30);
		lblVacio.setVisible(false);

		// Primero el mensaje para que se pinte por encima de la tabla
		panelFondo.add(lblVacio);
		panelFondo.add(scrollTabla);
	}

	// ====================================================================
	// PANEL: BOTONES DE ABAJO (ATRAS / HISTORICO / VER WORKOUT)
	// ====================================================================
	/**
	 * ATRAS (negro) abajo a la izquierda. HISTORICO y VER WORKOUT (blancos
	 * con borde) abajo a la derecha. VER WORKOUT empieza desactivado hasta
	 * elegir fila.
	 */
	private void crearBotones() {
		btnAtras = new BotonPlano("←  ATRÁS", true);
		btnAtras.setBounds(25, 536, 140, 48);
		panelFondo.add(btnAtras);

		btnHistorico = new BotonPlano("HISTÓRICO", false);
		btnHistorico.setBounds(486, 536, 180, 48);
		panelFondo.add(btnHistorico);

		btnVerWorkout = new BotonPlano("▶  VER WORKOUT", false);
		btnVerWorkout.setBounds(681, 536, 185, 48);
		btnVerWorkout.setEnabled(false);
		panelFondo.add(btnVerWorkout);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	/**
	 * Pone las opciones del filtro. Ejemplo: { "TODOS", "NIVEL 0", "NIVEL 1" }
	 */
	public void setOpcionesNivel(String[] opciones) {
		comboNivel.removeAllItems();
		for (int i = 0; i < opciones.length; i++) {
			comboNivel.addItem(opciones[i]);
		}
	}

	/**
	 * Rellena la tabla. Cada fila: { nombre, nivel, ejercicios, duracion, video }.
	 * En video va CON_VIDEO si tiene enlace (sale el icono de play) o "" si no.
	 * Si no hay filas sale el mensaje "No hay workouts para este nivel".
	 */
	public void cargarWorkouts(String[][] filas) {
		modeloTabla.setRowCount(0);
		for (int i = 0; i < filas.length; i++) {
			modeloTabla.addRow(filas[i]);
		}
		tabla.clearSelection();
		btnVerWorkout.setEnabled(false);
		lblVacio.setVisible(filas.length == 0);
		// Vuelve arriba del todo al cambiar de filtro
		tabla.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
	}

	/**
	 * Nombre y nivel del usuario. Ejemplo: setUsuario("Alexis", 2, "Intermedio")
	 */
	public void setUsuario(String nombre, int nivel, String nombreNivel) {
		etiquetaUsuario.setUsuario(nombre, nivel, nombreNivel);
	}

	/**
	 * Muestra u oculta el aviso de modo sin conexion.
	 */
	public void setModoSinConexion(boolean sinConexion) {
		lblSinConexion.setVisible(sinConexion);
	}

	/**
	 * Fila seleccionada (-1 si no hay ninguna).
	 */
	public int getFilaSeleccionada() {
		return tabla.getSelectedRow();
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JComboBox<String> getComboNivel() {
		return comboNivel;
	}

	public JTable getTabla() {
		return tabla;
	}

	public JButton getBtnAtras() {
		return btnAtras;
	}

	public JButton getBtnHistorico() {
		return btnHistorico;
	}

	public JButton getBtnVerWorkout() {
		return btnVerWorkout;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CELDAS DE LA TABLA (filas, iconos de nivel, fila elegida)
	// ====================================================================
	/**
	 * Celdas de la tabla: filas blanco/gris, columna "Nivel" con su icono y
	 * su color, columna "Vídeo" con el icono de play (centrado) y la fila
	 * seleccionada con borde negro de 2 px.
	 */
	private class RenderCelda extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		// Se carga una vez (el renderer se usa para cada celda)
		private ImageIcon iconoVideo = UtilidadesVista.cargarImagen(RUTA_PLAY_NEGRO);

		@Override
		public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
				boolean conFoco, int fila, int columna) {
			super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
			setFont(FUENTE_TABLA);
			setForeground(Estilos.COLOR_CAJA);
			setIcon(null);
			setIconTextGap(8);
			setHorizontalAlignment(SwingConstants.LEFT);

			// Filas pares blancas, impares grises (como en "Detalle Workout")
			if (fila % 2 == 0) {
				setBackground(Estilos.COLOR_FONDO);
			} else {
				setBackground(COLOR_FILA_GRIS);
			}

			// Columna Nivel: icono + texto del color del nivel
			if (columna == 1 && valor != null) {
				String texto = valor.toString().toLowerCase();
				if (texto.startsWith("novato")) {
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_novato.png"));
					setForeground(Estilos.COLOR_NIVEL_NOVATO);
				} else if (texto.startsWith("principiante")) {
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_principiante.png"));
					setForeground(Estilos.COLOR_NIVEL_PRINCIPIANTE);
				} else if (texto.startsWith("intermedio")) {
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_intermedio.png"));
					setForeground(Estilos.COLOR_NIVEL_INTERMEDIO);
				} else if (texto.startsWith("avanzado")) {
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_avanzado.png"));
					setForeground(Estilos.COLOR_NIVEL_AVANZADO);
				} else if (texto.startsWith("experto")) {
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_experto.png"));
					setForeground(Estilos.COLOR_NIVEL_EXPERTO);
				} else if (texto.startsWith("custom")) {
					// WORKOUT DE PRUEBA (se puede borrar): icono negro
					setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_custom.png"));
					setForeground(Estilos.COLOR_CAJA);
				}
			}

			// Columna Vídeo: solo el icono, sin texto
			if (columna == COLUMNA_VIDEO) {
				setText("");
				setHorizontalAlignment(SwingConstants.CENTER);
				if (CON_VIDEO.equals(valor)) {
					setIcon(iconoVideo);
				}
			}

			setBorder(crearBordeCelda(seleccionada, fila, columna, tabla.getColumnCount()));
			return this;
		}

		/**
		 * Margen interior + lineas: gris arriba/abajo en las filas grises y
		 * negro de 2 px alrededor de la fila seleccionada.
		 */
		private Border crearBordeCelda(boolean seleccionada, int fila, int columna, int totalColumnas) {
			Border margen = BorderFactory.createEmptyBorder(0, MARGEN_CELDA, 0, 4);
			Border linea;
			if (seleccionada) {
				int izquierda = 0;
				int derecha = 0;
				if (columna == 0) {
					izquierda = 2;
				}
				if (columna == totalColumnas - 1) {
					derecha = 2;
				}
				linea = BorderFactory.createMatteBorder(2, izquierda, 2, derecha, Estilos.COLOR_CAJA);
			} else if (fila % 2 == 1) {
				linea = BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_LINEA_FILA);
			} else {
				linea = BorderFactory.createEmptyBorder(1, 0, 1, 0);
			}
			return BorderFactory.createCompoundBorder(linea, margen);
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
			setBorder(BorderFactory.createEmptyBorder(0, MARGEN_CELDA, 0, 4));
			return this;
		}
	}

	// ====================================================================
	// DIBUJO: OPCIONES DEL DESPLEGABLE
	// ====================================================================
	/**
	 * Opciones del desplegable: oscuras, y la que tiene el raton encima mas clara.
	 */
	private class RenderOpcionCombo extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
				boolean seleccionada, boolean conFoco) {
			super.getListCellRendererComponent(lista, valor, indice, seleccionada, conFoco);
			setFont(Estilos.FUENTE_MONO);
			setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
			if (seleccionada) {
				setBackground(new Color(70, 70, 70));
			} else {
				setBackground(COLOR_COMBO);
			}
			setForeground(Estilos.COLOR_TEXTO_CAJA);
			return this;
		}
	}

	// ====================================================================
	// DIBUJO: DESPLEGABLE OSCURO (fondo y flecha)
	// ====================================================================
	/**
	 * Aspecto del desplegable: fondo oscuro plano y una flecha blanca dibujada
	 * a mano en lugar del boton gris de Java.
	 */
	private class ComboOscuroUI extends BasicComboBoxUI {

		@Override
		protected JButton createArrowButton() {
			JButton flecha = new JButton() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(COLOR_COMBO);
					g2.fillRect(0, 0, getWidth(), getHeight());
					// Triangulo blanco hacia abajo
					int centroX = getWidth() / 2;
					int centroY = getHeight() / 2;
					int[] xs = { centroX - 7, centroX + 7, centroX };
					int[] ys = { centroY - 4, centroY - 4, centroY + 5 };
					g2.setColor(Estilos.COLOR_TEXTO_CAJA);
					g2.fillPolygon(xs, ys, 3);
					g2.dispose();
				}
			};
			flecha.setBorder(BorderFactory.createEmptyBorder());
			flecha.setFocusable(false);
			flecha.setPreferredSize(new Dimension(36, 38));
			return flecha;
		}

		@Override
		public void paintCurrentValueBackground(Graphics g, Rectangle limites, boolean conFoco) {
			g.setColor(COLOR_COMBO);
			g.fillRect(limites.x, limites.y, limites.width, limites.height);
		}
	}

	// ====================================================================
	// DIBUJO: BARRA DE SCROLL FINA
	// ====================================================================
	/**
	 * Barra de scroll fina y negra, sin las flechas de arriba y abajo.
	 */
	private class ScrollFinoUI extends BasicScrollBarUI {

		@Override
		protected void configureScrollBarColors() {
			thumbColor = Estilos.COLOR_CAJA;
			trackColor = Estilos.COLOR_SEPARADOR;
		}

		@Override
		protected JButton createDecreaseButton(int orientacion) {
			return crearBotonVacio();
		}

		@Override
		protected JButton createIncreaseButton(int orientacion) {
			return crearBotonVacio();
		}

		private JButton crearBotonVacio() {
			JButton boton = new JButton();
			boton.setPreferredSize(new Dimension(0, 0));
			return boton;
		}

		@Override
		protected void paintThumb(Graphics g, JComponent c, Rectangle limites) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(thumbColor);
			g2.fillRoundRect(limites.x + 1, limites.y, limites.width - 2, limites.height, 6, 6);
			g2.dispose();
		}

		@Override
		protected void paintTrack(Graphics g, JComponent c, Rectangle limites) {
			g.setColor(trackColor);
			g.fillRect(limites.x + 3, limites.y, 2, limites.height);
		}

		@Override
		protected Dimension getMinimumThumbSize() {
			return new Dimension(8, 40);
		}

		@Override
		public Dimension getPreferredSize(JComponent c) {
			return new Dimension(8, ((JScrollBar) c).getHeight());
		}
	}

	// ====================================================================
	// DIBUJO: BOTONES ATRAS / HISTORICO / VER WORKOUT
	// ====================================================================
	/**
	 * Boton con las esquinas redondeadas: negro con texto blanco (oscuro =
	 * true) o blanco con borde negro. Con el raton encima se pone gris.
	 * Desactivado: borde y texto gris claro.
	 */
	private class BotonPlano extends JButton {

		private static final long serialVersionUID = 1L;

		private boolean oscuro;

		public BotonPlano(String texto, boolean oscuro) {
			super(texto);
			this.oscuro = oscuro;
			setFont(Estilos.FUENTE_BOTON);
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
			boolean encima = getModel().isRollover() || getModel().isPressed();
			if (oscuro) {
				if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			} else {
				Color colorBorde = Estilos.COLOR_CAJA;
				if (!isEnabled()) {
					colorBorde = COLOR_DESACTIVADO;
				}
				if (encima && isEnabled()) {
					g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_FONDO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
				// Borde de 1.5 px (BasicStroke es el grosor de la linea)
				g2.setColor(colorBorde);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
