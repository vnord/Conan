package view.Command;

import model.BoxReference;
import model.Proof;
import view.RowPane;

import java.util.ArrayList;
import java.util.List;

public class InsertBox implements Command {
    Proof proof;
    int rowNo;
    public InsertBox(Proof proof, int rowNo, List<RowPane> rList) {
        this.proof = proof;
        this.rowNo = rowNo;
    };
    @Override
    public boolean execute() {
        boolean result = proof.insertBox(rowNo-1);
        return false; // Implement undo in the future?
    }

    @Override
    public void undo() {
    }
    @Override
    public String toString() {
        return "Insert box to " + rowNo;
    }
}
