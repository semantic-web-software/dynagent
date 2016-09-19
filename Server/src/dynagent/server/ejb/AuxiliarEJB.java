package dynagent.server.ejb;

import javax.ejb.EJBException;

public class AuxiliarEJB  {
	public static void error(String error) {
		error(error, null);
	}

	public static void error(String error, Throwable e) {
		if (e != null)
			e.printStackTrace();
		System.out.println(error+" "+e.getMessage());
		throw new EJBException(error);
	}
}
