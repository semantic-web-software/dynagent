package dynagent.server.application.old;


public interface sessionable {
    void rollback(session ses);
    boolean exists();
    boolean isNull();//Puede no existir pero ser orden "del", y como orden no es nulo. Si el null no existe ni como orden
    boolean hasChanged();
    void delete(session ses);
}
