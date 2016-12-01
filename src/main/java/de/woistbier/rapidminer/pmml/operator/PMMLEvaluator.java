package de.woistbier.rapidminer.pmml.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.operator.*;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.LogService;

import java.util.logging.Level;

/**
 * Created by mackaiver on 01/12/16.
 */
public class PMMLEvaluator extends AbstractReader<PredictionModel> {


    public PMMLEvaluator(OperatorDescription description, Class<? extends IOObject> generatedClass) {
        super(description, generatedClass);
    }

    class PMMLModel extends PredictionModel{

        protected PMMLModel(ExampleSet trainingExampleSet,
                            ExampleSetUtilities.SetsCompareOption sizeCompareOperator,
                            ExampleSetUtilities.TypesCompareOption typeCompareOperator) {
            super(trainingExampleSet, sizeCompareOperator, typeCompareOperator);
        }

        @Override
        public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
            return null;
        }
    }

    @Override
    public PredictionModel read() throws OperatorException {
        LogService.getRoot().log(Level.INFO, "Doing something...");

        return null;
    }
}
