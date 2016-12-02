package de.woistbier.rapidminer.pmml.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.*;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;

import com.rapidminer.tools.Ontology;
import org.dmg.pmml.*;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.*;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

/**
 * Here we try to load a pmml model and map these things to rapidminer things.
 *
 * Created by mackaiver on 01/12/16.
 */
public class PMMLEvaluator extends AbstractReader<PredictionModel> {

    public  static final String PARAMETER_URL = "url";

    public PMMLEvaluator(OperatorDescription description, Class<? extends IOObject> generatedClass) {
        super(description, generatedClass);
    }

    public PMMLEvaluator(OperatorDescription description) {
        super(description, PredictionModel.class);
    }

    public class PMMLModel extends PredictionModel{

        private  final ModelEvaluator<? extends Model> modelEvaluator;

        public PMMLModel(ExampleSet trainingExampleSet,
                  ExampleSetUtilities.SetsCompareOption sizeCompareOperator,
                  ExampleSetUtilities.TypesCompareOption typeCompareOperator, ModelEvaluator<? extends Model> modelEvaluator) {
            super(trainingExampleSet, sizeCompareOperator, typeCompareOperator);
            this.modelEvaluator = modelEvaluator;
        }



        @Override
        public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {

            for (Example example : exampleSet) {

                Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
                for (InputField activeField : modelEvaluator.getActiveFields()) {

                    Object rawValue = example.getValue(getAttributeFromInputfield(activeField));

                    FieldValue activeValue = activeField.prepare(rawValue);

                    arguments.put(activeField.getName(), activeValue);
                }
                Map<FieldName, ?> results = modelEvaluator.evaluate(arguments);
                Object targetValue = results.get(modelEvaluator.getTargetFieldName());

//        log.info("Prediction: " + targetValue);
                try {
                    ProbabilityDistribution pD = (ProbabilityDistribution) targetValue;
                    double proba = pD.getProbability("1");
                    if (proba >= 0.5) {
                        example.setValue(predictedLabel, 1.0);
                    } else {
                        example.setValue(predictedLabel, 0.0);
                    }
                    example.setConfidence("1", proba);
                } catch (ClassCastException e) {
//                    .warn("Prediction did not contain a  ProbabilityDistribution object");
                }

            }

            return exampleSet;
        }
    }

    private Attribute getAttributeFromInputfield(InputField inputField){
            OpType type = inputField.getOpType();
            String name = inputField.toString();

            Attribute attribute;
            switch (type) {
                case CATEGORICAL:
                case ORDINAL:
                    attribute = AttributeFactory.createAttribute(name, Ontology.NOMINAL);
                    break;
                default:
                    attribute = AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
                    break;
            }

            return attribute;

    }

    @Override
    public PredictionModel read() throws OperatorException {
        PMML pmml;
        java.lang.reflect.Field[] fields = PMML.class.getDeclaredFields();
        for(java.lang.reflect.Field field : fields){
            field.setAccessible(true);
        }

        try (InputStream inputStream = getParameterAsInputStream(PARAMETER_URL)){
            pmml = PMMLUtil.unmarshal(inputStream);


            ModelEvaluator<? extends Model> evaluator = ModelEvaluatorFactory.newInstance().newModelEvaluator(pmml);
            List<InputField> inputFields = evaluator.getActiveFields();
            List<Attribute> attributes = new ArrayList<>();
            for(InputField inputField : inputFields) {
                   attributes.add(getAttributeFromInputfield(inputField));
            }


            String targetName = evaluator.getTargetField().getName().getValue();
            Attribute targetAttribute;
            switch (evaluator.getTargetField().getOpType()) {
                case CATEGORICAL:
                case ORDINAL:
                    targetAttribute = AttributeFactory.createAttribute(targetName, Ontology.NOMINAL);
                    break;
                default:
                    targetAttribute = AttributeFactory.createAttribute(targetName, Ontology.NUMERICAL);
                    break;
            }

            ExampleSet dummyExampleSet = ExampleSets.from(attributes).withRole(targetAttribute, "label").build();
            return new PMMLModel(dummyExampleSet, null, null ,evaluator);

        } catch (IOException e) {
            LogService.getRoot().log(Level.SEVERE, "Cannot read file.");
            throw new OperatorException("Cannot read from File with url: " + PARAMETER_URL);
        } catch (JAXBException | SAXException e) {
            LogService.getRoot().log(Level.SEVERE, "Cannot parse pmml file.");
            throw new OperatorException("Cannot parse PMML from File with url: " + PARAMETER_URL);
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = new LinkedList<>();
        types.add(new ParameterTypeFile(PARAMETER_URL, "The url to read the data from.","pmml", false));
        return types;
    }
}
