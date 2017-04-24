package model.rules;

import model.Box;
import model.formulas.Formula;
import model.formulas.Implication;
import model.formulas.Negation;

public class ModusTollens extends Rule {

    private Integer rowRef1;
    private Integer rowRef2;

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

    @Override
    public boolean verifyReferences(Box data, int rowIndex) {
        if (!(data.isInScopeOf(rowRef1, rowIndex))) return false;
        if (!(data.isInScopeOf(rowRef2, rowIndex))) return false;
        Formula referencedRow1 = data.getRow(rowRef1).getFormula();
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        if (!(referencedRow2 instanceof Implication)) return false;
        if (!(referencedRow1 instanceof Negation)) return false;
        return true;
    }

    @Override
    public boolean verifyRow(Box data, int rowIndex) {
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        Implication implRef = (Implication) referencedRow2;
        Formula result = data.getRow(rowIndex).getFormula();
        if (result instanceof Negation == false) return false;
        Negation negResult = (Negation) result;
        return implRef.lhs.equals(negResult.formula);
    }

    @Override
    public Formula generateRow(Box data) {
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        Implication implRef = (Implication) referencedRow2;
        return new Negation(implRef.lhs);
    }

    @Override
    public String toString() {
        return "MT " + rowRef1 + ", " + rowRef2;
    }

}
