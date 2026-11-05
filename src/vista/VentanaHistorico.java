package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Ventana con el historico del usuario: los workouts que ha hecho, primero
 * los mas recientes. Mismo estilo que la ventana de Workouts (fondo, tabla
 * con cabecera negra, usuario arriba a la derecha y boton VOLVER).
 * Solo es la parte visual: el controlador le pasa las filas ya preparadas
 * (ordenadas y con el tiempo previsto calculado) y escucha VOLVER y los
 * clics en la X del final de cada fila (borrar ese workout del historico).
 */
public class VentanaHistorico extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/workouts_background.jpg";
	private static final String RUTA_LOGO_NEGRO = "images/iconos/logo_negro.png";
	// X para borrar una fila: gris y roja con el raton encima
	private static final ImageIcon ICONO_BORRAR = UtilidadesVista.cargarImagen("images/iconos/borrar.png");
	private static final ImageIcon ICONO_BORRAR_HOVER = UtilidadesVista
			.cargarImagen("images/iconos/borrar_hover.png");

	// Columnas de la tabla. La ultima no tiene titulo: es la X de borrar
	private static final String[] COLUMNAS = { "Workout", "Nivel", "Fecha", "Tiempo", "Previsto", "Completado",
			"" };
	// Suman 798 (el ancho de la tabla). El nombre mas largo es "Movilidad y estiramientos"
	private static final int[] ANCHO_COLUMNAS = { 244, 150, 108, 72, 88, 106, 30 };
	private static final int COLUMNA_NIVEL = 1;
	private static final int COLUMNA_COMPLETADO = 5;
	private static final int COLUMNA_BORRAR = 6;

	// Posicion y tamano de la tabla (igual que en Workouts)
	private static final int TABLA_X = 51;
	private static final int TABLA_Y = 232;
	private static final int TABLA_ANCHO = 798;
	private static final int TABLA_ALTO = 286;
	private static final int ALTO_FILA = 52;
	private static final int MARGEN_CELDA = 12;

	// Colores de la tabla
	private static final Color COLOR_FILA_GRIS = new Color(242, 242, 242);
	private static final Color COLOR_LINEA_FILA = new Color(186, 186, 186);

	// Fuentes (la de la tabla un poco mas pequeña que en Workouts: hay 6 columnas)
	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 56);
	private static final Font FUENTE_TABLA = Estilos.FUENTE_MONO;

	private PanelFondo panelFondo;
	private JLabel lblTotal;
	private JLabel lblSinConexion;
	private EtiquetaUsuario etiquetaUsuario;
	private JLabel lblError;
	private DefaultTableModel modeloTabla;
	private JTable tabla;
	private JLabel lblVacio;
	private JButton btnVolver;

	// Fila con el raton encima de su X (-1 = ninguna): esa X se pinta roja
	private int filaRatonBorrar = -1;
	// Sin conexion no se puede borrar: no se pintan las X
	private boolean sinConexion = false;

	public VentanaHistorico() {
		setTitle("GymApp - Histórico");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearCabecera();
		crearInfo();
		crearTabla();
		crearBotones();

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: CABECERA (logo GymApp y titulo 'Histórico')
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

		JLabel lblTitulo = new JLabel("Histórico");
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 84, panelFondo.getPreferredSize().width, 76);
		panelFondo.add(lblTitulo);
	}

	// ====================================================================
	// PANEL: INFO ENCIMA DE LA TABLA (total, aviso sin conexion y usuario)
	// ====================================================================
	/**
	 * A la izquierda cuantos workouts hay ("12 workouts realizados") y un
	 * aviso gris que solo sale en modo sin conexion. A la derecha el nombre
	 * y el nivel del usuario.
	 */
	private void crearInfo() {
		lblTotal = new JLabel();
		lblTotal.setFont(Estilos.FUENTE_MONO_NEGRITA);
		lblTotal.setForeground(Estilos.COLOR_CAJA);
		lblTotal.setBounds(TABLA_X, 180, 260, 38);
		panelFondo.add(lblTotal);

		lblSinConexion = new JLabel("Sin conexión: datos del backup");
		lblSinConexion.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblSinConexion.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblSinConexion.setBounds(TABLA_X + 260, 180, 260, 38);
		lblSinConexion.setVisible(false);
		panelFondo.add(lblSinConexion);

		// Error al borrar (en el mismo sitio que el aviso: con conexion esta libre)
		lblError = new JLabel();
		lblError.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblError.setForeground(Estilos.COLOR_ERROR);
		lblError.setBounds(TABLA_X + 260, 180, 260, 38);
		panelFondo.add(lblError);

		etiquetaUsuario = new EtiquetaUsuario();
		etiquetaUsuario.setBounds(TABLA_X + 510, 188, TABLA_ANCHO - 510, 24);
		panelFondo.add(etiquetaUsuario);
	}

	// ====================================================================
	// PANEL: TABLA DEL HISTORICO
	// ====================================================================
	/**
	 * Tabla con cabecera negra y filas de 52 px que alternan blanco y gris
	 * (igual que Workouts). No se pueden elegir filas: solo pulsar la X.
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
		tabla.setFillsViewportHeight(true);
		tabla.setBackground(Estilos.COLOR_FONDO);
		tabla.setFocusable(false);
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

		// Raton encima de una X: se pone roja y sale la mano
		tabla.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				marcarX(getFilaBorrar(e));
			}
		});
		tabla.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				marcarX(-1);
			}
		});

		JScrollPane scrollTabla = new JScrollPane(tabla);
		scrollTabla.setBorder(BorderFactory.createEmptyBorder());
		scrollTabla.getViewport().setBackground(Estilos.COLOR_FONDO);
		scrollTabla.getVerticalScrollBar().setUI(new ScrollFinoUI());
		scrollTabla.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollTabla.getVerticalScrollBar().setUnitIncrement(ALTO_FILA / 2);
		scrollTabla.setBounds(TABLA_X, TABLA_Y, TABLA_ANCHO, TABLA_ALTO);

		// Mensaje cuando todavia no ha hecho ningun workout
		lblVacio = new JLabel("Todavía no has hecho ningún workout");
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
	// PANEL: BOTON VOLVER (abajo a la izquierda, como ATRAS en Workouts)
	// ====================================================================
	private void crearBotones() {
		btnVolver = new BotonOscuro("←  VOLVER");
		btnVolver.setBounds(25, 536, 150, 48);
		panelFondo.add(btnVolver);
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	/**
	 * Rellena la tabla. Cada fila: { workout, nivel, fecha, tiempo total,
	 * tiempo previsto, completado }. Ejemplo:
	 * { "Cardio suave", "Principiante", "28/01/2026", "08:16", "06:40", "100%" }
	 * Si no hay filas sale el mensaje "Todavía no has hecho ningún workout".
	 */
	public void cargarHistorico(String[][] filas) {
		lblError.setText("");
		filaRatonBorrar = -1;
		modeloTabla.setRowCount(0);
		for (int i = 0; i < filas.length; i++) {
			modeloTabla.addRow(filas[i]);
		}
		lblVacio.setVisible(filas.length == 0);

		if (filas.length == 1) {
			lblTotal.setText("1 workout realizado");
		} else {
			lblTotal.setText(filas.length + " workouts realizados");
		}
	}

	/**
	 * Nombre y nivel del usuario. Ejemplo: setUsuario("Alexis", 2, "Intermedio")
	 */
	public void setUsuario(String nombre, int nivel, String nombreNivel) {
		etiquetaUsuario.setUsuario(nombre, nivel, nombreNivel);
	}

	/**
	 * Muestra u oculta el aviso de modo sin conexion. Sin conexion tampoco
	 * salen las X de borrar.
	 */
	public void setModoSinConexion(boolean sinConexion) {
		this.sinConexion = sinConexion;
		lblSinConexion.setVisible(sinConexion);
	}

	/**
	 * Mensaje de error en rojo encima de la tabla (ej: si no se ha podido borrar).
	 */
	public void mostrarError(String texto) {
		lblError.setText(texto);
	}

	/**
	 * Fila de la X que hay debajo del raton, o -1 si el raton no esta encima
	 * de ninguna X (o si no hay conexion). El controlador la usa al hacer clic.
	 */
	public int getFilaBorrar(MouseEvent e) {
		if (sinConexion) {
			return -1;
		}
		int fila = tabla.rowAtPoint(e.getPoint());
		int columna = tabla.columnAtPoint(e.getPoint());
		if (columna == COLUMNA_BORRAR) {
			return fila;
		}
		return -1;
	}

	/**
	 * Pinta roja la X de esa fila (-1 = ninguna) y pone la mano como cursor.
	 */
	private void marcarX(int fila) {
		if (fila == filaRatonBorrar) {
			return;
		}
		filaRatonBorrar = fila;
		if (fila >= 0) {
			tabla.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else {
			// null = el cursor de la ventana
			tabla.setCursor(null);
		}
		tabla.repaint();
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

	// El controlador le pone el MouseListener para los clics en la X
	public JTable getTabla() {
		return tabla;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CELDAS DE LA TABLA (filas, iconos de nivel, 100% en verde)
	// ====================================================================
	/**
	 * Celdas: filas blanco/gris, columna "Nivel" con su icono y su color,
	 * columna "Completado" en verde cuando es 100% y la X de borrar al final.
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
			setIconTextGap(8);
			setHorizontalAlignment(SwingConstants.LEFT);

			// Filas pares blancas, impares grises
			if (fila % 2 == 0) {
				setBackground(Estilos.COLOR_FONDO);
			} else {
				setBackground(COLOR_FILA_GRIS);
			}

			// Columna Nivel: icono + texto del color del nivel
			if (columna == COLUMNA_NIVEL && valor != null) {
				String texto = valor.toString().toLowerCase();
				setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_" + texto + ".png"));
				if (texto.equals("novato")) {
					setForeground(Estilos.COLOR_NIVEL_NOVATO);
				} else if (texto.equals("principiante")) {
					setForeground(Estilos.COLOR_NIVEL_PRINCIPIANTE);
				} else if (texto.equals("intermedio")) {
					setForeground(Estilos.COLOR_NIVEL_INTERMEDIO);
				} else if (texto.equals("avanzado")) {
					setForeground(Estilos.COLOR_NIVEL_AVANZADO);
				} else if (texto.equals("experto")) {
					setForeground(Estilos.COLOR_NIVEL_EXPERTO);
				}
			}

			// Columna Completado: verde si lo termino entero
			if (columna == COLUMNA_COMPLETADO && "100%".equals(valor)) {
				setForeground(Estilos.COLOR_NIVEL_PRINCIPIANTE);
			}

			// Columna de borrar: solo la X, centrada (roja si el raton esta encima)
			if (columna == COLUMNA_BORRAR) {
				setText("");
				setHorizontalAlignment(SwingConstants.CENTER);
				if (sinConexion) {
					setIcon(null);
				} else if (fila == filaRatonBorrar) {
					setIcon(ICONO_BORRAR_HOVER);
				} else {
					setIcon(ICONO_BORRAR);
				}
			}

			setBorder(crearBordeCelda(fila, columna));
			return this;
		}

		/**
		 * Margen interior + lineas grises arriba y abajo en las filas grises.
		 * La columna de la X no lleva margen (la X va centrada).
		 */
		private Border crearBordeCelda(int fila, int columna) {
			Border margen = BorderFactory.createEmptyBorder(0, MARGEN_CELDA, 0, 4);
			if (columna == COLUMNA_BORRAR) {
				margen = BorderFactory.createEmptyBorder();
			}
			Border linea;
			if (fila % 2 == 1) {
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
	// DIBUJO: BARRA DE SCROLL FINA (igual que en Workouts)
	// ====================================================================
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
	// DIBUJO: BOTON VOLVER (negro, como ATRAS en Workouts)
	// ====================================================================
	/**
	 * Boton negro con texto blanco y esquinas redondeadas. Con el raton
	 * encima se pone gris.
	 */
	private class BotonOscuro extends JButton {

		private static final long serialVersionUID = 1L;

		public BotonOscuro(String texto) {
			super(texto);
			setFont(Estilos.FUENTE_BOTON);
			setForeground(Estilos.COLOR_TEXTO_CAJA);
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
				g2.setColor(Estilos.COLOR_BOTON_OSCURO_HOVER);
			} else {
				g2.setColor(Estilos.COLOR_BOTON_OSCURO);
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
