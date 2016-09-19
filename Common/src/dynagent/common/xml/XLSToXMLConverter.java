package dynagent.common.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.jdom.Document;
import org.jdom.Element;

import dynagent.common.Constants;

/**
 * converts a xls stream in a xml stream
 * 
 * @author wohlgemuth
 * 
 */
public class XLSToXMLConverter implements IConverter{

	private boolean header;

	private final static String HEADER[] = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
			"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	/**
	 * Crea un conversor de Excel a XML poniendo por defecto que la primera fila
	 * son los nombres de las columnas.
	 */
	public XLSToXMLConverter() {
		this.header = true;
	}

	/**
	 * converts the inputfile to an xml file
	 * 
	 * @throws Exception
	 * 
	 * @see edu.ucdavis.genomics.metabolomics.binbase.meta.converter.AbstractConverter#convert(java.io.InputStream,
	 *      java.io.OutputStream)
	 */
	public Document convert(FileInputStream in) throws IOException {
		return convert(in,null);
	}
	
	public Document convert(FileInputStream in,String[] header) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook(in);

		Document document = new Document();
		Element root = new Element(TAG_WORKBOOK);
		document.setRootElement(root);

		for (int a = 0; a < workbook.getNumberOfSheets(); a++) {
			HSSFSheet sheet = workbook.getSheetAt(a);

			int min = sheet.getFirstRowNum();
			int max = sheet.getLastRowNum();
			if (max == 0 && sheet.getPhysicalNumberOfRows() == 0){
				max = -1;
			}
			
			Element sheetElement = new Element(TAG_SHEET);
			sheetElement.setAttribute("name", workbook.getSheetName(a));
			sheetElement.setAttribute("index", String.valueOf(a));
			boolean buildHeader=header==null || header.length==0;

			for (int i = min; i <= max; i++) {
				HSSFRow row = sheet.getRow(i);

				if (i == min && buildHeader) {

					if (this.header == true) {
						// header row
						short firstCell = row.getFirstCellNum();
						short lastCell = row.getLastCellNum();
						header = new String[lastCell];

						for (int x = firstCell; x < lastCell; x++) {
							HSSFCell cell = row.getCell(x);

							if (cell == null) {
								header[x] = "NoHeader";
							} else {
								switch (cell.getCellType()) {
								case HSSFCell.CELL_TYPE_BLANK:
									header[x] = "NoHeader";
									break;
								case HSSFCell.CELL_TYPE_STRING:
									String headerValue = cell.getStringCellValue();
									headerValue = headerValue.replaceAll("\\(|\\)", "_");
									header[x] = headerValue;
									break;

								default:
									throw new IOException("cell type is not as header supported. Type=" + cell.getCellType());
								}
							}
						}
					} else {
						// header row
						short firstCell = row.getFirstCellNum();
						short lastCell = row.getLastCellNum();
						header = new String[lastCell];

						for (short x = firstCell; x < lastCell; x++) {
							header[x] = HEADER[x];
						}
					}
				}

				if (this.header == false) {
					createElement(header, sheetElement, row);
				} else if (!buildHeader || i > min) {
					createElement(header, sheetElement, row);
				}

			}
			root.addContent(sheetElement);
		}
		return document;
	}

	/**
	 * @param header
	 * @param sheetElement
	 * @param row
	 */
	private void createElement(String[] header, Element sheetElement,
			HSSFRow row) {
		short firstCell = row.getFirstCellNum();
		short lastCell = row.getLastCellNum();

		Element element = new Element(TAG_ROW);
		element.setAttribute("id", String.valueOf(row.getRowNum()));

		for (int x = firstCell; x < lastCell; x++) {
			HSSFCell cell = row.getCell(x);
			
			if(header.length-1<x){
				System.out.println("Error, columna "+x+" no existe, longitud maxima "+(header.length-1));
				continue;
			}
			if (header[x].equals("NoHeader") == false) {
				Element content = new Element(header[x].replaceAll(" ", "_"));

				if (cell == null) {
					content.setText("");
				} else {
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_BLANK:
						content.setText("");
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						content.setText(String.valueOf(cell.getBooleanCellValue()).trim());
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						content.setText(String.valueOf(cell.getErrorCellValue()).trim());
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						switch (cell.getCachedFormulaResultType()) {
						case HSSFCell.CELL_TYPE_BOOLEAN:
							content.setText(String.valueOf(cell.getBooleanCellValue()).trim());
							break;
						case HSSFCell.CELL_TYPE_ERROR:
							content.setText(String.valueOf(cell.getErrorCellValue()).trim());
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							String data = cell.getStringCellValue();
							// tratar dato
							if (data.matches("\\d{2,4}[\\/-]{3,}.*")) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(DateUtil.getJavaDate(new Double(data)));
								double dataN = cal.getTimeInMillis() / Constants.TIMEMILLIS;
								content.setText(String.valueOf(dataN));
							} else {
								BigDecimal round = new BigDecimal(data.trim());
								//if (dataN == round) {
									content.setText(String.valueOf(data).trim());
								/*} else {
									content.setText(String.valueOf(dataN).trim());
								}*/
							}
							break;
						case HSSFCell.CELL_TYPE_STRING:
							content.setText(String.valueOf(cell.getStringCellValue()).trim());
							break;
						default:
							content.setText("-- TIPO DESCONOCIDO --");
						}
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						//excel considera celdas como numero solo por matchear, aunque le digas que sea texto, y eso provoca en esta libreria recotar numeros grandes a double
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						String data = cell.getStringCellValue();
						// tratar dato
						if (data.matches("\\d{2,4}[\\/-]{3,}.*")) {
							Calendar cal = Calendar.getInstance();
							cal.setTime(DateUtil.getJavaDate(new Double(data)));
							double dataN = cal.getTimeInMillis() / Constants.TIMEMILLIS;
							content.setText(String.valueOf(dataN));
						} else {
							BigDecimal round = new BigDecimal(data.trim());
							//if (dataN == round) {
								content.setText(String.valueOf(data).trim());
							/*} else {
								content.setText(String.valueOf(dataN).trim());
							}*/
						}
						break;
					case HSSFCell.CELL_TYPE_STRING:
						content.setText(String.valueOf(cell.getStringCellValue()).trim());
						break;
					default:
						content.setText("-- TIPO DESCONOCIDO --");
					}
				}
				element.addContent(content);
			}

		}
		sheetElement.addContent(element);
	}

	// public static void main(String[] args) throws FileNotFoundException,
	// Exception {
	// new XLSToXMLConverter().convert(new FileInputStream(args[0]),new
	// FileOutputStream(args[1]));
	// }

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}
}