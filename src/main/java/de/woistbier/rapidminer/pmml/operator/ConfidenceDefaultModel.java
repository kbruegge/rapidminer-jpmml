package de.woistbier.rapidminer.pmml.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.OperatorProgress;
import com.rapidminer.operator.learner.PredictionModel;

import java.util.Map;

/**
 * Created by mackaiver on 06/12/16.
 */
public class ConfidenceDefaultModel extends PredictionModel {
    private final Map<String, Attribute> confidenceMapping;
    private final Attribute predictionAttribute;

    private static final int OPERATOR_PROGRESS_STEPS = 10_000;

    protected ConfidenceDefaultModel(ExampleSet trainingExampleSet, Map<String, Attribute> confidenceMapping, Attribute predictionAttribute) {
        super(trainingExampleSet, null, null);
        this.confidenceMapping = confidenceMapping;
        this.predictionAttribute = predictionAttribute;
    }

    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabelAttribute) throws OperatorException {
        Attribute label = getLabel();
        if (! label.isNominal()){
            throw new OperatorException("Label is not nominal. But it needs to be.");
        }
        System.out.println("Predicted attribute is: " + predictedLabelAttribute);
        OperatorProgress progress = null;
        if (getShowProgress() && getOperator() != null && getOperator().getProgress() != null) {
            progress = getOperator().getProgress();
            progress.setTotal(exampleSet.size());
        }
        int progressCounter = 0;
        for (Example example : exampleSet) {
            //set the prediciton
            example.setValue(predictedLabelAttribute, example.getValue(predictionAttribute));
            for (String nominalValue :  confidenceMapping.keySet()){
                Attribute confAttribute = confidenceMapping.get(nominalValue);
                double confidenceValue = example.getValue(confAttribute);
//                System.out.println("Setting confidence " + confidenceValue + " for nominal value " + nominalValue + " from value in attribute with name: " + confAttribute);
                example.setConfidence(nominalValue, confidenceValue);
            }
//            for (String v : predictedLabelAttribute.getMapping().getValues()){
//                Attribute confAttribute = confidenceMapping.get(v);
//                double confidenceValue = example.getValue(confAttribute);
//                System.out.println("Setting confidence " + confidenceValue + " for nominal value " + v + " on attribute with name: " + confAttribute);
//                example.setConfidence(v, confidenceValue);
//            }

            if (progress != null && ++progressCounter % OPERATOR_PROGRESS_STEPS == 0) {
                progress.setCompleted(progressCounter);
            }
        }
        return exampleSet;
    }
}
