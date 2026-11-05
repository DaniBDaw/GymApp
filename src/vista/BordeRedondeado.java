package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

/**
 * Borde con las esquinas redondeadas para los campos de texto (login,
 * registro y ajustes). Tiene las mismas esquinas que los botones
 * (Estilos.ARCO_BOTON).
 * Ejemplo: campo.setBorder(new BordeRedondeado(Color.WHITE, 3));
 */
public class BordeRedondeado extends AbstractBorder {

	private static final long serialVersionUID = 1L;

	// Espacio entre el borde y el texto, a la izquierda y a la derecha
	private static final int MARGEN_TEXTO = 8;

	private Color color;
	private int grosor;

	public BordeRedondeado(Color color, int grosor) {
		this.color = color;
		this.grosor = grosor;
	}

	/**
	 * Pinta el borde: un rectangulo redondeado sin relleno del grosor
	 * indicado (BasicStroke es el grosor de la linea).
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int ancho, int alto) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(grosor));
		// La linea se pinta centrada: se mete medio grosor para que no se corte
		int mitad = grosor / 2;
		g2.drawRoundRect(x + mitad, y + mitad, ancho - grosor, alto - grosor, Estilos.ARCO_BOTON,
				Estilos.ARCO_BOTON);
		g2.dispose();
	}

	/**
	 * Espacio que ocupa el borde (arriba, izquierda, abajo, derecha). El
	 * texto del campo empieza despues de este espacio.
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(grosor, grosor + MARGEN_TEXTO, grosor, grosor + MARGEN_TEXTO);
	}
}
