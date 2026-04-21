package domaine.utilitaires;

public interface Command {
    void execute();
    void undo();
}
