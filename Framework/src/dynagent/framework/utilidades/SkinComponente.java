package dynagent.framework.utilidades;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.awt.Font;

/**
 * Clase que asigna caracteristicas graficas a cualquier componente. Pudiendo incluso
 * asignarselas a su componente padre, abuelo, hijos y nietos.
 * @author Francisco Javier Martinez Navarro
 */
public class SkinComponente{
	
	public static final String COMPONENTE="Componente";
	public static final String COMPONENTE_PADRE="ComponentePadre";
	public static final String COMPONENTE_ABUELO="ComponenteAbuelo";
	public static final String COMPONENTE_HIJO="ComponenteHijo";
	public static final String COMPONENTE_NIETO="ComponenteNieto";
	
	private HashMap<String, Color> mapeadoColorFondo;
	private HashMap<String, Color> mapeadoColorTexto;
	private HashMap<String, Font> mapeadoFuente;
	public SkinComponente(){
		mapeadoColorFondo=new HashMap<String, Color>();
		mapeadoColorTexto=new HashMap<String, Color>();
		mapeadoFuente=new HashMap<String, Font>();
	}
	
	public Color getColorFondo(String nivel){
		return (Color)mapeadoColorFondo.get(nivel);
	}
	
	public Color getColorTexto(String nivel){
		return (Color)mapeadoColorTexto.get(nivel);
	}
	
	public Font getFuente(String nivel){
		return (Font)mapeadoFuente.get(nivel);
	}
	
	public void putColorFondo(String nivel,Color color){
		mapeadoColorFondo.put(nivel,color);
	}
	
	public void putColorTexto(String nivel,Color color){
		mapeadoColorTexto.put(nivel,color);
	}
	
	public void putFuente(String nivel,Font fuente){
		mapeadoFuente.put(nivel,fuente);
	}
	
	public void setSkin(Component componente,String nivelComponente){
    	Color colorFondo=getColorFondo(nivelComponente);
		Color colorTexto=getColorTexto(nivelComponente);
		Font fuente=getFuente(nivelComponente);
		
		if(colorFondo!=null)
			componente.setBackground(colorFondo);
		if(colorTexto!=null)
			componente.setForeground(colorTexto);
		if(fuente!=null)
			componente.setFont(fuente);
    }	
    
    public void setSkinAll(Component componente){
    	setSkin(componente,SkinComponente.COMPONENTE);
    	
    	Component componentePadre=componente.getParent();
		if(componentePadre!=null){
			setSkin(componentePadre,SkinComponente.COMPONENTE_PADRE);
			Component componenteAbuelo=componentePadre.getParent();
			if(componenteAbuelo!=null)
				setSkin(componenteAbuelo,SkinComponente.COMPONENTE_ABUELO);
		}
		if(Container.class.isAssignableFrom(componente.getClass())){
			Component[] componentesHijo=((Container)componente).getComponents();
			if(componentesHijo!=null){
				Component[] componentesNieto;
				int numeroHijos=componentesHijo.length;
				for(int i=0;i<numeroHijos;i++){
					setSkin(componentesHijo[i],SkinComponente.COMPONENTE_HIJO);
					if(Container.class.isInstance(componentesHijo[i])){
						componentesNieto=((Container)componentesHijo[i]).getComponents();
						if(componentesNieto!=null){
							int numeroNietos=componentesNieto.length;
							for(int j=0;j<numeroNietos;j++){
								setSkin(componentesNieto[i],SkinComponente.COMPONENTE_NIETO);
							}
						}
					}
				}
			}
		}
    }
	
}
