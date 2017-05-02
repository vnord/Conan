package model.rules;

import model.Box;
import model.ProofRow;
import model.formulas.Formula;
import start.Constants;

public class Assumption extends Rule {
    @Override
    public String toString() {
        return "assumption";
    }

    @Override
    public boolean hasCompleteInfo() {
        return true;
    }

    @Override
    public void updateReference(int index, String newValue) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean verifyReferences(Box data, int rowIndex) {
        return true;
    }

    @Override
    public boolean verifyRow(Box data, int rowIndex) {
        ProofRow rowToVerify = data.getRow(rowIndex);
        // Check if first row of box and box is an actual box.
        Box parent = rowToVerify.getParent();
        if (parent.isTopLevelBox())
            return false;
        int index = parent.indexOf(rowToVerify);
        if( index == 0) return true;
        if( index == 1 && parent.getRow(0).getRule() instanceof FreshVar) return true;
        return false;
    }

    @Override
    public Formula generateRow(Box data) {
        return null;
    }
    
    @Override
	public String[] getReferenceStrings() {
		return new String[0];
	}

	@Override
	public String getDisplayName() {
		return Constants.assumption;
	}
}
