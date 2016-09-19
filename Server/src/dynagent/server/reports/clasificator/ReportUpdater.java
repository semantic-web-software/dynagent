package dynagent.server.reports.clasificator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.Namespace;

import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;

/**Clase que crea un nuevo diseño actualizado, es decir, 
 * sin clonar los nodos de elementos que no están en el modelo importado.*/
public class ReportUpdater {
	
	/**Conjunto de entrada que contiene el atributo name de los subreports que no seran clonados 
	 * al nuevo diseño. El atributo name en el nodo jasperReport del subreport debe coincidir 
	 * con el atributo key dado al objeto en el report principal. tambien debe coincidir con 
	 * la continuación del parámetro paramsub...*/
	private HashSet<String> subreportsRemoved;
	/**Conjunto de entrada que almacena las variables eliminadas en los subreports. En el report 
	 * principal estas variables se corresponden con variables con otro nombre.*/
	private HashSet<String> variablesOfSubreportsRemoved;
	
	/**Namespace usado al crear el diseño original.*/
	private Namespace nameSpace;
	/**Objeto obtenido tras analizar el diseño.*/
	private ElemsAnalized elemsAnalized;
	
	/**Constructor usado desde un subreport.*/
	public ReportUpdater() {
		this.subreportsRemoved = new HashSet<String>();
		this.variablesOfSubreportsRemoved = new HashSet<String>();
	}
	public HashSet<String> getVariablesToRemove() {
		return elemsAnalized.getVariablesToRemove();
	}

	/**Constructor usado desde el report principal.*/
	public ReportUpdater(HashSet<String> subreportsRemoved, HashSet<String> variablesOfSubreportsRemoved) {
		this.subreportsRemoved = subreportsRemoved;
		this.variablesOfSubreportsRemoved = variablesOfSubreportsRemoved;
	}
	
	/**Inicio de la actualización. Primero se analiza el diseño usando ReportAnalizer y obteniendo los 
	 * elementos analizados. Se recorren los nodos y se clonan aquellos que no se quieren eliminar. */
	public Element startUpdater(FactoryConnectionDB fcdb, Element jasperReport) throws SQLException, NamingException, IOException {
		
		nameSpace = Namespace.getNamespace("http://jasperreports.sourceforge.net/jasperreports");
		
		ReportAnalizer reportAnalizer = new ReportAnalizer(fcdb, subreportsRemoved, variablesOfSubreportsRemoved, nameSpace);
		elemsAnalized = reportAnalizer.startAnalizer(jasperReport);
		
		Element newJasperReport = null;
		boolean reportRemove = elemsAnalized.isReportRemove();
		if (!reportRemove) {
			boolean classified = elemsAnalized.isClassified();
			if (classified) {
				System.out.println("creando nuevo diseño actualizado....");
				newJasperReport = jdomParser.cloneNode(jasperReport);
				Iterator it = jasperReport.getChildren().iterator();
				while (it.hasNext()) {
					Element child = (Element)it.next();
					//clonar si no hay que borrar
					//seguir un procesamiento similar a reportAnalizer
					if (child.getName().equals(ConstantsReport.field)) {
						Element field = child;
						//if (!elemsAnalized.getFieldsToRemove().contains(field.getAttributeValue(ConstantsReport.name))) {
							Element newField = jdomParser.cloneTree(field);
							newJasperReport.addContent(newField);
						//}
					} else if (child.getName().equals(ConstantsReport.parameter)) {
						Element parameter = child;
						if (!elemsAnalized.getParametersToRemove().contains(parameter.getAttributeValue(ConstantsReport.name))) {
							Element newParameter = jdomParser.cloneTree(parameter);
							newJasperReport.addContent(newParameter);
						}
					} else if (child.getName().equals(ConstantsReport.variable)) {
						Element variable = child;
						if (!elemsAnalized.getVariablesToRemove().contains(variable.getAttributeValue(ConstantsReport.name))) {
							Element newVariable = jdomParser.cloneTree(variable);
							newJasperReport.addContent(newVariable);
						}
					} else if (child.getName().equals(ConstantsReport.group)) {
						Element group = child;
						String name = group.getAttributeValue(ConstantsReport.name);
						if (!elemsAnalized.getGroupsToRemove().contains(name)) {
							Element newGroup = jdomParser.cloneNode(group);
							newJasperReport.addContent(newGroup);
							Element groupExpression = group.getChild(ConstantsReport.groupExpression, nameSpace);
							Element newGroupExpression = jdomParser.cloneNode(groupExpression);
							newGroup.addContent(newGroupExpression);
							
							Element groupHeader = group.getChild(ConstantsReport.groupHeader, nameSpace);
							if (groupHeader!=null && !elemsAnalized.getBandsToRemove().contains(name + "_" + ConstantsReport.groupHeader)) {
								Element newGroupHeader = jdomParser.cloneNode(groupHeader);
								boolean hasElements = updateBand(newGroupHeader, groupHeader);
								if (hasElements)
									newGroup.addContent(newGroupHeader);
							}
							Element groupFooter = group.getChild(ConstantsReport.groupFooter, nameSpace);
							if (groupFooter!=null && !elemsAnalized.getBandsToRemove().contains(name + "_" + ConstantsReport.groupFooter)) {
								Element newGroupFooter = jdomParser.cloneNode(groupFooter);
								boolean hasElements = updateBand(newGroupFooter, groupFooter);
								if (hasElements)
									newGroup.addContent(newGroupFooter);
							}
						}
					} else if (child.getName().equals(ConstantsReport.background) ||
							child.getName().equals(ConstantsReport.title) ||
							child.getName().equals(ConstantsReport.pageHeader) || 
							child.getName().equals(ConstantsReport.columnHeader) || 
							child.getName().equals(ConstantsReport.detail) || 
							child.getName().equals(ConstantsReport.columnFooter) || 
							child.getName().equals(ConstantsReport.pageFooter) || 
							child.getName().equals(ConstantsReport.summary)) {
						Element bandSup = child;
						String nameBandSup = child.getName();
						if (!elemsAnalized.getBandsToRemove().contains(nameBandSup)) {
							if (nameBandSup.equals(ConstantsReport.detail) && child.getChildren(ConstantsReport.band, nameSpace).size()>1) { 
								int nBand = 1;
								Element newBandSup = jdomParser.cloneNode(bandSup);
								boolean someHasElement = false;
								Iterator it2 = bandSup.getChildren(ConstantsReport.band, nameSpace).iterator();
								while (it2.hasNext()) {
									Element band = (Element)it2.next();
									String nameBand = nameBandSup + "_" + nBand;
									if (!elemsAnalized.getBandsToRemove().contains(nameBand)) {
										Element newBand = jdomParser.cloneNode(band);
										boolean hasElements = updateSubBand(newBand, band);
										if (hasElements) {
											someHasElement = true;
											newBandSup.addContent(newBand);
										}
									}
									nBand++;
								}
								if (someHasElement)
									newJasperReport.addContent(newBandSup);
							} else {
								Element newBandSup = jdomParser.cloneNode(bandSup);
								boolean hasElements = updateBand(newBandSup, child);
								if (hasElements)
									newJasperReport.addContent(newBandSup);
							}
						}
					} else {
						Element otherNode = child;
						Element newOtherNode = jdomParser.cloneTree(otherNode);
						newJasperReport.addContent(newOtherNode);
					}
				}
			} else {
				System.out.println("Informe no clasificado");
				newJasperReport = jasperReport;
			}
		} else
			System.out.println("Informe eliminado");
		return newJasperReport;
	}
	
	/**Si un elemento tiene nuevas coordenadas almacenadas en el conjunto newCoordinatesInTable
	 * se le pondran en el nuevo diseño.*/
	private void updateCoordinates(Element newNode) {
		Element reportElement = newNode.getChild(ConstantsReport.reportElement, nameSpace);
		String key = reportElement.getAttributeValue(ConstantsReport.key);
		HashMap<String,Coordinates> newCoordinatesInTable = elemsAnalized.getNewCoordinatesInTable();
		Coordinates coord = newCoordinatesInTable.get(key);
		if (coord!=null) {
			//poner nuevas coordenadas
			reportElement.setAttribute(ConstantsReport.x, String.valueOf(coord.getLeft()));
			reportElement.setAttribute(ConstantsReport.y, String.valueOf(coord.getTop()));
			reportElement.setAttribute(ConstantsReport.width, String.valueOf(coord.getWidth()));
			reportElement.setAttribute(ConstantsReport.height, String.valueOf(coord.getHeight()));
		}
	}
	
	/**Clona el nodo banda y llama al metodo updateSubBand.*/
	private boolean updateBand(Element newSupBand, Element supBand) throws DataErrorException {
		Element band = supBand.getChild(ConstantsReport.band, nameSpace);
		Element newBand = jdomParser.cloneNode(band);
		boolean hasElements = updateSubBand(newBand, band);
		if (hasElements)
			newSupBand.addContent(newBand);
		return hasElements;
	}
	
	/**Itera por los distintos hijos de la banda clonando si no está en el conjunto correspondiente 
	 * de elementos a eliminar. A su vez se van actualizando coordenadas de aquellos elementos que 
	 * se hayan reorganizado.
	 * Un staticText se elimina si está contenido en staticTextsToRemove o si su textField o frame 
	 * asociado ha sido eliminado. Este procesado no pudo hacerse en analisis porque ahora es 
	 * cuando se tienen todos los elementos ya procesados. Si el textField o frame asociado no existe 
	 * estamos ante un error de escritura.
	 * Si el subreport es valido se clonan sus nodos hijos, en los nodos de variables de retorno se 
	 * comprueba si son validas o no.*/
	private boolean updateSubBand(Element newBand, Element band) throws DataErrorException {
		boolean hasElements = false;
		Iterator it = band.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			String nameChild = child.getName();
			
			if (nameChild.equals(ConstantsReport.line) || nameChild.equals(ConstantsReport.rectangle) || nameChild.equals(ConstantsReport.image)) {
				Element newChild = jdomParser.cloneTree(child);
				hasElements = true;
				newBand.addContent(newChild);
				if (nameChild.equals(ConstantsReport.line))
					updateCoordinates(newChild);
			} else if (nameChild.equals(ConstantsReport.textField)) {
				Element textField = child;
				String key = textField.getChild(ConstantsReport.reportElement, nameSpace).getAttributeValue(ConstantsReport.key);
				if (key!=null && !elemsAnalized.getTextFieldsToRemove().contains(key)) {
					Element newTextField = jdomParser.cloneTree(textField);
					hasElements = true;
					newBand.addContent(newTextField);
					updateCoordinates(newTextField);
				}
			} else if (nameChild.equals(ConstantsReport.staticText)) {
				Element staticText = child;
				String key = staticText.getChild(ConstantsReport.reportElement, nameSpace).getAttributeValue(ConstantsReport.key);
				//System.out.println("key " + key);
				if (key!=null) {
					String keyTable = "";
					if (key.contains(ConstantsReport.separatorTable)) {
						keyTable = key.substring(0,key.indexOf(ConstantsReport.separatorTable)+1); //lleva la @
						key = key.substring(key.indexOf(ConstantsReport.separatorTable)+1,key.length());
					}
					//comprobacion de que es correcto
					if (key.startsWith("label-")) {
						String keyContent = keyTable + key.substring(6,key.length());
						if (!elemsAnalized.getAllTextFieldsAndFrames().contains(keyContent)) continue;
							//throw new DataErrorException("No existe frame o textField asociado a la etiqueta " + keyTable + key);
					}
					
					if (!elemsAnalized.getStaticTextsToRemove().contains(key)) {
						boolean clonar = false;
						if (key.startsWith("label-")) {
							//se elimina si su textField o frame asociado se ha eliminado, 
							String keyContent = keyTable + key.substring(6,key.length());
							//System.out.println("keyContent " + keyContent);
							if (!elemsAnalized.getTextFieldsToRemove().contains(keyContent) && !elemsAnalized.getFramesToRemove().contains(keyContent))
								clonar = true;
						} else
							clonar = true;
						if (clonar) {
							Element newStaticText = jdomParser.cloneTree(staticText);
							hasElements = true;
							newBand.addContent(newStaticText);
							updateCoordinates(newStaticText);
						}
					}
				}
			} /*else if (nameChild.equals(ConstantsReport.componentElement)) {
				Element componentElement = child;
				Element reportElement = componentElement.getChild(ConstantsReport.reportElement, nameSpace);
				String key = reportElement.getAttributeValue(ConstantsReport.key);
				if (!elemsToRemove.getTablesToRemove().contains(key)) {
					Element newComponentElement = jdomParser.cloneNode(componentElement);
					newBand.addContent(newComponentElement);
					Element newReportElement = jdomParser.cloneNode(reportElement);
					newComponentElement.addContent(newReportElement);
					
					Element jrTable = componentElement.getChild(ConstantsReport.jr_table, nameSpace);
					Element newJrTable = jdomParser.cloneNode(jrTable);
					newComponentElement.addContent(newJrTable);
					
					Element datasetRun = jrTable.getChild(ConstantsReport.datasetRun, nameSpace);
					Element newDatasetRun = jdomParser.cloneTree(datasetRun);
					newJrTable.addContent(newDatasetRun);
					Iterator it2 = jrTable.getChildren().iterator();
					while (it2.hasNext()) {
						Element child2 = (Element)it2.next();
						if (child2.getName().equals(ConstantsReport.jr_column)) {
							Element jrColumn = child2;
							processJrColumn(jrColumn, newJrTable, elemsToRemove);
						} else if (child2.getName().equals(ConstantsReport.jr_columnGroup)) {
							Element jrColumnGroup = child2;
							Iterator it3 = jrColumnGroup.getChildren().iterator();
							while (it3.hasNext()) {
								Element child3 = (Element)it3.next();
								if (child3.getName().equals(ConstantsReport.jr_groupHeader) || child3.getName().equals(ConstantsReport.jr_groupFooter)) {
									Element jrGroup = child3;
									Element cell = jrGroup.getChild(ConstantsReport.jr_cell, nameSpace);
									
									Element newCell = jdomParser.cloneNode(cell);
									updateSubBand(newCell, cell, elemsToRemove);
									if (newCell.getChildren().size()>0) {
										Element newJrGroup = jdomParser.cloneNode(jrGroup);
										Element newJrColumnGroup = jdomParser.cloneNode(jrColumnGroup);
										newJrColumnGroup.addContent(newJrGroup);
										newJrTable.addContent(newJrColumnGroup);
									}
								} else if (child3.getName().equals(ConstantsReport.jr_column)) {
									Element jrColumn = child3;
									processJrColumn(jrColumn, newJrTable, elemsToRemove);
								}
							}
						}
					}
				}
			}*/ else if (nameChild.equals(ConstantsReport.frame)) {
				Element frame = child;
				String key = frame.getChild(ConstantsReport.reportElement, nameSpace).getAttributeValue(ConstantsReport.key);
				if (key!=null && !elemsAnalized.getFramesToRemove().contains(key)) {
					hasElements = true;
					Element newFrame = jdomParser.cloneNode(frame);
					boolean hasElementsR = updateSubBand(newFrame, frame);
					if (hasElementsR)
						newBand.addContent(newFrame);
				}
			} else if (nameChild.equals(ConstantsReport.subreport)) {
				Element subreport = child;
				Element reportElement = subreport.getChild(ConstantsReport.reportElement, nameSpace);
				String key = reportElement.getAttributeValue(ConstantsReport.key);
				if (key!=null && !subreportsRemoved.contains(key)) {
					hasElements = true;
					Element newSubreport = jdomParser.cloneNode(subreport);
					newBand.addContent(newSubreport);

					Element newReportElement = jdomParser.cloneNode(reportElement);
					newSubreport.addContent(newReportElement);
					
					//ver parametros y variables de retorno
					Iterator it2 = subreport.getChildren(ConstantsReport.subreportParameter, nameSpace).iterator();
					while (it2.hasNext()) {
						Element subreportParameter = (Element)it2.next();
						//Element subreportParameterExpression = subreportParameter.getChild(ConstantsReport.subreportParameterExpression, nameSpace);
						//String contentSubreportParameterExpression = ((CDATA)subreportParameterExpression.getContent().get(0)).getText();
						//String parameter = contentSubreportParameterExpression.substring(3,contentSubreportParameterExpression.length()-1);
						//no se van a eliminar parámetros de entrada de un subreport porque el subreport se trata antes que el report
						//if (!elemsAnalized.getParametersToRemove().contains(parameter)) {
							Element newSubreportParameter = jdomParser.cloneTree(subreportParameter);
							newSubreport.addContent(newSubreportParameter);
						//}
					}

					Element connectionExpression = subreport.getChild(ConstantsReport.connectionExpression, nameSpace);
					if (connectionExpression!=null) {
						Element newConnectionExpression = jdomParser.cloneNode(connectionExpression);
						newSubreport.addContent(newConnectionExpression);
					}
					it2 = subreport.getChildren(ConstantsReport.returnValue, nameSpace).iterator();
					while (it2.hasNext()) {
						Element returnValue = (Element)it2.next();
						if (!elemsAnalized.getVariablesToRemove().contains(returnValue.getAttributeValue(ConstantsReport.toVariable))) {
							Element newReturnValue = jdomParser.cloneNode(returnValue);
							newSubreport.addContent(newReturnValue);
						}
					}
					Element subreportExpression = subreport.getChild(ConstantsReport.subreportExpression, nameSpace);
					if (subreportExpression!=null) {
						Element newSubreportExpression = jdomParser.cloneNode(subreportExpression);
						newSubreport.addContent(newSubreportExpression);
					}
				}
			} else {
				Element otherNode = child;
				Element newOtherNode = jdomParser.cloneTree(otherNode);
				hasElements = true;
				newBand.addContent(newOtherNode);
			}
		}
		return hasElements;
	}
	/*private void processJrColumn(Element jrColumn, Element newJrTable, ElemsToRemove elemsToRemove) {
		Element jrDetailCell = jrColumn.getChild(ConstantsReport.jr_detailCell, nameSpace);
		Element newJrDetailCell = jdomParser.cloneNode(jrDetailCell);
		updateSubBand(newJrDetailCell, jrDetailCell, elemsToRemove);
		
		if (newJrDetailCell.getChildren().size()>0) {
			Element jrColumnHeader = jrColumn.getChild(ConstantsReport.jr_columnHeader, nameSpace);
			Element newJrColumnHeader = jdomParser.cloneNode(jrColumnHeader);
			updateSubBand(newJrColumnHeader, jrColumnHeader, elemsToRemove);
			
			if (newJrColumnHeader.getChildren().size()>0) {
				Element newJrColumn = jdomParser.cloneNode(jrColumn);
				newJrTable.addContent(newJrColumn);
				newJrColumn.addContent(newJrColumnHeader);
				newJrColumn.addContent(newJrDetailCell);
			}
		}
	}*/


}
