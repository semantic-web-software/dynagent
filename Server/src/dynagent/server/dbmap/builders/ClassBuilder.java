package dynagent.server.dbmap.builders;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.UnrecognizedClass;
import dynagent.server.ejb.FactoryConnectionDB;

public class ClassBuilder {

	private FactoryConnectionDB fcdb;
	private Hashtable<Integer, String> classNames;

	public ClassBuilder(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
	}

	/**
	 * Construye un mapa con todas las clases instanciadas en la aplicacion referenciadas por su IDTO.
	 * 
	 * @return Mapa con todas las clases instanciadas.
	 * @throws SQLException
	 * @throws NamingException
	 * @throws UnrecognizedClass
	 *             Si se ha intentado obtener el nombre de una clase mediante su idto de la cual no se tiene mapeado el
	 *             nombre.
	 */
	public Hashtable<Integer, ClassInfo> buildClasses() throws SQLException, NamingException, UnrecognizedClass {
		Hashtable<Integer, ClassInfo> result = new Hashtable<Integer, ClassInfo>();
		classNames = DBQueries.getClassNames(fcdb);

		Set<Integer> referencedClasses = DBQueries.getReferencedClasses(fcdb);
		for (Integer classIdto : referencedClasses) {
			String className = classNames.get(classIdto);
			if (className == null) {
				throw new UnrecognizedClass("No se tiene un nombre para la clase cuyo idto es: " + classIdto);
			}
			// Buscamos todas las propiedades de la clase y las añadimos
			List<PropertyInfo> properties = DBQueries.getPropertiesForClass(fcdb, classIdto);
			ClassInfo classInfo = new ClassInfo(classNames.get(classIdto), classIdto);
			classInfo.addAllProperties(properties);
			// Consultamos si la clase es abstracta.
			if (DBQueries.isAbstractClass(fcdb, classIdto)) {
				classInfo.setIsAbstractClass(true);
			}
			result.put(classIdto, classInfo);
		}

		setSpecializedClasses(result);

		findInmediateRelations(result);

		return result;
	}

	/**
	 * Añade todas las relaciones de especializacion a las clases.
	 * 
	 * @param classMap
	 *            Mapa con todas las clases obtenidas de la base de datos y que son instanciadas por la aplicacion.
	 * @throws SQLException
	 * @throws NamingException
	 */
	private void setSpecializedClasses(Hashtable<Integer, ClassInfo> classMap) throws SQLException, NamingException {
		Hashtable<Integer, Set<Integer>> specializedClassesMap = DBQueries.getSpecializedClassesMap(fcdb, true);
		Set<Integer> keySet = specializedClassesMap.keySet();
		for (Integer parentId : keySet) {
			Set<Integer> children = specializedClassesMap.get(parentId);
			ClassInfo classInfo = classMap.get(parentId);
			if (classInfo != null) {
				classInfo.addAllChildClasses(children);

				for (Integer childId : children) {
					classInfo = classMap.get(childId);
					if (classInfo != null) {
						classInfo.addParentClass(parentId);
					}
				}
			}
		}
	}

	/**
	 * Recorriendo todas las clases del mapa, calcula los padres inmediatos de todos los elementos, asi como los hijos
	 * inmediatos de cada uno
	 * 
	 * @param classMap
	 *            Mapa de todas las clases del modelo que son instanciables, indexadas por su idto.
	 */
	private void findInmediateRelations(Hashtable<Integer, ClassInfo> classMap) {

		for (ClassInfo classInfo : classMap.values()) {
			Set<Integer> parentClasses = classInfo.getParentClasses();
			if (parentClasses.isEmpty()) {
				classInfo.setInmediateParents(new ArrayList<Integer>());
				continue;
			}

			Set<Integer> parentsOfParents = new HashSet<Integer>();
			for (Integer parentIdto : parentClasses) {
				ClassInfo parentClassInfo = classMap.get(parentIdto);
				parentsOfParents.addAll(parentClassInfo.getParentClasses());
			}

			Set<Integer> inmediateParents = new HashSet<Integer>(parentClasses);
			inmediateParents.removeAll(parentsOfParents);

			ArrayList<Integer> inmediateParentsList = new ArrayList<Integer>(inmediateParents);
			classInfo.setInmediateParents(inmediateParentsList);

			for (Integer inmediateParentIdto : inmediateParentsList) {
				ClassInfo parentClassInfo = classMap.get(inmediateParentIdto);
				parentClassInfo.addInmediateChild(classInfo.getIdto());
			}
		}
	}

}
