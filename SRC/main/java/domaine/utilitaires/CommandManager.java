package domaine.utilitaires;

import java.util.Stack;

public class CommandManager {
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;

    public CommandManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void executeCommand(Command command) {
        command.execute();
        this.undoStack.push(command);
        this.redoStack.clear();
    }

    public void undo() {
        if (!this.undoStack.isEmpty()) {
            Command command = this.undoStack.pop();
            command.undo();
            this.redoStack.push(command);
        }
    }

    public void redo() {
        if (!this.redoStack.isEmpty()) {
            Command command = this.redoStack.pop();
            command.execute();
            this.undoStack.push(command);
        }
    }
    
    public boolean canUndo() {
        return !this.undoStack.isEmpty();
    }
    
    public boolean canRedo() {
        return !this.redoStack.isEmpty();
    }
    
    public void clear() {
        this.undoStack.clear();
        this.redoStack.clear();
    }
}
