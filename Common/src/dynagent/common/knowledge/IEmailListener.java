package dynagent.common.knowledge;

public interface IEmailListener {

	public void requestEmail(int ido, int idto, Integer idtoReport, String email, String subject, String body, int idoMiEmpresa);

}
