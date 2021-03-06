package model.rules;

import model.Box;
import model.formulas.Formula;
import model.formulas.Implication;
import start.Constants;

public class ImplicationElim extends Rule {

    private Integer rowRef1;
    private Integer rowRef2;
    private Implication implication;

    @Override
    public boolean hasCompleteInfo() {
        return rowRef1 != null && rowRef2 != null;
    }

    @Override
    public void updateReference(int index, String newValue) {
        if (index < 1 || index > 2) throw new IllegalArgumentException();

        if (index == 1) {
            try {
                rowRef1 = ReferenceParser.parseIntegerReference(newValue);
            } catch (NumberFormatException e) {
                rowRef1 = null;
                throw new NumberFormatException();
            }
        } else {//index == 2
            try {
                rowRef2 = ReferenceParser.parseIntegerReference(newValue);
            } catch (NumberFormatException e) {
                rowRef2 = null;
                throw new NumberFormatException();
            }
        }
    }

    private boolean verifySecondArgImpl(Formula referencedRow1, Formula referencedRow2) {
        if (referencedRow2 instanceof Implication == false) return false;
        Implication implRef = (Implication) referencedRow2;
        if (implRef.lhs.equals(referencedRow1) == false)
            return false;
        implication = implRef;
        return true;
    }

    @Override
    public boolean verifyReferences(Box data, int rowIndex) {
        implication = null;
        if (data.isInScopeOf(rowRef1, rowIndex) == false) return false;
        if (data.isInScopeOf(rowRef2, rowIndex) == false) return false;

        Formula referencedRow1 = data.getRow(rowRef1).getFormula();
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        return verifySecondArgImpl(referencedRow1, referencedRow2) || verifySecondArgImpl(referencedRow2, referencedRow1);
    }

    @Override
    public boolean verifyRow(Box data, int rowIndex) {
        return implication.rhs.equals(data.getRow(rowIndex).getFormula());
    }

    @Override
    public Formula generateRow(Box data) {
        return implication.rhs;
    }

    @Override
    public String toString() {
        return String.format("→e, %s, %s", rowRef1 == null ? "" : new Integer(rowRef1+1), rowRef2 == null ? "" : new Integer(rowRef2+1));
    }
    
	@Override
	public String[] getReferenceStrings() {
		String ref1 = rowRef1 == null ? "" : (rowRef1+1)+"";
		String ref2 = rowRef2 == null ? "" : (rowRef2+1)+"";
		return new String[]{ref1, ref2};
	}

	@Override
	public String getDisplayName() {
        return Constants.implicationElim;
	}

}
