package model;

import model.formulas.Formula;
import model.rules.*;

import java.io.Serializable;
import java.util.ArrayList;

public class Proof implements Serializable{
    private ArrayList<ProofListener> listeners = new ArrayList<ProofListener>();
    private Parser parser = new Parser(); //this won't be serialized
    private Formula conclusion;
    private Box proofData = new Box(null, true);

    /***
     * Add a new row at the end of the proof.
     */
    public void addRow() {
        proofData.addRow();
        for (ProofListener listener : this.listeners) {
            listener.rowAdded();
        }
        System.out.println("addRow()");
        proofData.printRows(1, 1);
        System.out.println("==========================================================");
    }

    /**
     * delete the row at index rowNumber-1
     * TODO: update indexes of rule objects in rows below?
     * TODO: verify rows below since they now might not be correct anymore
     * @param rowNumber
     */
    public void deleteRow(int rowNumber){
        if(rowNumber < 1 || rowNumber > proofData.size()){
            throw new IllegalArgumentException();
        }
        proofData.deleteRow(rowNumber-1);
        for (ProofListener listener : this.listeners) {
            listener.rowDeleted(rowNumber);
        }
        System.out.println("deleteRow("+rowNumber+")");
        proofData.printRows(1,1);
        System.out.println("==========================================================");
    }

    /**
     * Inserts a new row into the same box as the referenced row
     * TODO: update indexes of rule objects in rows below?
     * TODO: verify rows below since they now might not be correct anymore
     * @param rowNumber: the number of the row used as reference
     * @param br: Indicates whether the new row should be added before or after the reference row
     */
    public void insertNewRow(int rowNumber, BoxReference br){
        if(rowNumber == proofData.size() && br == BoxReference.AFTER) {
            addRow();
            return;
        }
        if(rowNumber < 1 || rowNumber > proofData.size()+1){
            System.out.println("Proof.insertNewRow: incorrect rowNumber");
            System.out.println("rows.size(): "+proofData.size()+", rowNumber: "+rowNumber);
            return;
        }
        proofData.insertRow(rowNumber-1, br);
        for(ProofListener pl : listeners){
            pl.rowInserted(rowNumber, br);
        }
        System.out.println("insertNewRow("+rowNumber+", "+br+")");
        proofData.printRows(1,1);
        System.out.println("==========================================================");
    }

    //Will you ever update both the formula and rule fields at the same time?
    public void updateRow(String formula, String rule, int rowNumber){}

    /**
     * Alert the listeners about the row.
     * @param formula
     * @param rowNumber
     */
    public void updateFormulaRow(String strFormula, int rowNumber){
        //System.out.println("Proof.updateFormulaRow("+formula+", "+rowNumber+")");
        int rowIndex = rowNumber-1;
        ProofRow toBeUpdated = proofData.getRow(rowIndex);
        Formula parsedFormula = null;
        boolean wellFormed;
        try{
            parsedFormula = parser.parse(strFormula);
            wellFormed = true;
        }
        catch(ParseException e){
            wellFormed = false;
        }
        toBeUpdated.setFormula(parsedFormula);
        toBeUpdated.setUserInput(strFormula);
        toBeUpdated.setWellformed(wellFormed);

        for (ProofListener listener : this.listeners) {
            listener.rowUpdated(wellFormed, rowNumber);
        }
        verifyConclusion(rowIndex);
        verifyRow(rowIndex); //should use verifyProof later probably, to verify rows lower in the proof aswell
        proofData.printRows(1,1);
        System.out.println("==========================================================");
    }

    public void updateRuleRow(String ruleString, int rowNumber) throws IllegalAccessException, InstantiationException {
    	System.out.println("updateRuleRow: rule="+ruleString+", rowNr="+rowNumber);
    	ProofRow pr = proofData.getRow(rowNumber-1);
        Rule rule = (RuleMapper.getRule(ruleString));
    	if (rule != null) {
    	    pr.setRule(rule);
            int rowIndex = rowNumber - 1;
            verifyRow(rowIndex);
        }
        proofData.printRows(1,1);
    }

    //Adds a rule object to the given row
    public void addRule(int rowNr, Rule rule){
    	System.out.println("addRule: "+rowNr+", Rule: "+rule);
        proofData.getRow(rowNr-1).setRule(rule);
        verifyRow(rowNr-1); //should use verifyProof later probably
    }

    //should verify each line in the proof from line startIndex
    public boolean verifyProof(int startIndex){
        assert(startIndex < proofData.size()) : "Proof.verifyProof: index out of bounds";
        boolean returnValue = true;
        for(int i = startIndex; i < proofData.size(); i++){
            if(verifyRow(i) == false) returnValue = false;
            //TODO: inform listeners about each row
        }
        return returnValue;
    }

    //should verify that the row is correct with regards to it's rule and
    //update the row object with this info
    public boolean verifyRow(int rowIndex){
        assert(rowIndex < proofData.size()) : "Proof.verifyRow: index out of bounds";
        ProofRow row = proofData.getRow(rowIndex);
        Rule rule = row.getRule();
        boolean isVerified;
        if (rule == null || row.getFormula() == null || rule.hasCompleteInfo() == false ) {
            isVerified = false;
        } else {
            isVerified = rule.verify(proofData, rowIndex);
        }
        row.setVerified(isVerified);
        for (ProofListener listener : this.listeners) {
            listener.rowVerified(isVerified, rowIndex+1);
        }
        return isVerified;
    }

    //This shouldn't be used when a proper UI for opening boxes has been implemented
    //since at that point, you open a box in a specific row rather than at the end of a proof
    //Instead, at that point, use openBox(int rowNr)
    public void openBox() {
        proofData.openNewBox();
        //TODO: should probbly add a new row immediatly to avoid issues with empty boxes
        for (ProofListener listener : this.listeners) {
            listener.boxOpened();
        }
    }

    public void openBox(int rowNr){
        System.out.println("Proof.openBox(int) not implemented!");
        //if rowNr refers to the last line, the new box should be open
        //otherwise closed
        for (ProofListener listener : this.listeners) {
            listener.boxOpened();
        }
    }

    public void closeBox(){
        proofData.closeBox();
        for (ProofListener listener : this.listeners) {
            listener.boxClosed();
        }
    }

    /**
     * Updates the conclusion used as the 'goal' of the proof
     * @param conclusion
     */
    public void updateConclusion(String conclusion) {
        try {
            this.conclusion = parser.parse(conclusion);
        } catch (Exception ParseException) {
            this.conclusion = null;
        }
        for (int i = 0; i < proofData.size(); i++) {
            verifyConclusion(i);
        }
    }

    /**
     * Verifies if the row matches the conclusion
     * @param rowIndex: index of the row to verify
     */
    //TODO: check that the row has been verified, not just matches conclusion
    public void verifyConclusion(int rowIndex) {
        ProofRow row = proofData.getRow(rowIndex);
        /*if (false) {
            for (ProofListener listener : this.listeners) {
                listener.conclusionReached(false, rowIndex + 1);
            }
        }*/
        if (this.conclusion != null && row.getFormula() != null && this.conclusion.equals(row.getFormula())) {
            for (ProofListener listener : this.listeners) {
                listener.conclusionReached(true, rowIndex+1);
            }
        } else {
            for (ProofListener listener : this.listeners) {
                listener.conclusionReached(false, rowIndex+1);
            }
        }
    }

    /**
     * Needs to be called after a proof has been loaded/deserialized
     */
    public void load(){
        parser = new Parser();
    }

    public void registerProofListener(ProofListener listener){
        this.listeners.add(listener);
    }

    //Only for debugging, do not use this for actual implementation
    public Box getData(){
        return proofData;
    }

    public void printBoxes(){
        proofData.printBoxes();
    }

    public void printProof(boolean zeroBasedNumbering){
        int x = zeroBasedNumbering ? 0 : 1;
        proofData.printRows(1,x);
    }

    public void rulePromptUpdate(int rowNr, int promptIndex, String newValue) {
        System.out.println("rulePromptUpdate rowNr:"+rowNr+" promptIndex:"+promptIndex+" newValue:"+newValue);
        int rowIndex = rowNr-1;
        ProofRow row = proofData.getRow(rowNr-1);

        Rule rule = row.getRule();
        //System.out.println(rowIndex);
        //System.out.println(rule.toString());
        try{
            rule.updateReference(promptIndex, newValue);
        }
        //if the string is not of the correct format ie an integer or interval
        catch(NumberFormatException e){
            System.out.println("Incorrect reference format");
        }
        //if the promptIndex does not match the rule object, for example ConjunctionIntro has 2 references,
        //so promptIndex = 3 wouldn't make sense
        catch(IllegalArgumentException e){
            System.out.println("Invalid argument for "+rule.getClass().getSimpleName());
        }
        verifyRow(rowIndex);

    }

    public void printRowScopes(boolean zeroBasedNumbering){
        proofData.printRowScopes(zeroBasedNumbering);
    }


    public void printIntervalScopes( boolean zeroBasedNumbering){
        proofData.printIntervalScopes(zeroBasedNumbering);

    }
}
