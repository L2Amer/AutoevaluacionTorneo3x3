package examen.torneo3x3.excepciones;

public class AccesoDatosException extends RuntimeException {
    public AccesoDatosException(String msg) { super(msg); }
    public AccesoDatosException(String msg, Throwable causa) { super(msg, causa); }
}