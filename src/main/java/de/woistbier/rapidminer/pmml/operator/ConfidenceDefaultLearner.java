package de.woistbier.rapidminer.pmml.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.AttributeParameterPrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mackaiver on 02/12/16.
 */
public class ConfidenceDefaultLearner extends AbstractLearner{
    private final String LABEL_NAME = "label_name";
    private final String PREDICTION_NAME = "prediction_name";
    private final String CONFIDENCE_NAMES = "confidence_names";
    private final String PARAMETER_NAME = "parameter_name";
    private final String VALUE_NAME = "value_name";

    /**
     * Creates a new abstract
     *
     * @param description
     */
    public ConfidenceDefaultLearner(OperatorDescription description) {
        super(description);
        InputPort exampleIn = getExampleSetInputPort();
        exampleIn.addPrecondition(new AttributeParameterPrecondition(
                exampleIn,
                this,
                PREDICTION_NAME,
                Attributes.TYPE_PREDICTION
        ));
        exampleIn.addPrecondition(new AttributeParameterPrecondition(
                exampleIn,
                this,
                LABEL_NAME,
                Attributes.TYPE_LABEL
        ));


//        exampleIn.addPrecondition(new AttributeParameterPrecondition(exampleIn, this, CONFIDENCE_NAME, Ontology.REAL));
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {

        int nominalValues = exampleSet.getAttributes().getLabel().getMapping().size();

//        System.out.println("Nominal Values:");
//        for(String n : exampleSet.getAttributes().getLabel().getMapping().getValues()) {
//            System.out.println(n);
//        }

        if(nominalValues != getParameterList(CONFIDENCE_NAMES).size()){
            throw new OperatorException("Not all nominal values are mapped to attributes in the example set");
        }

        HashMap<String, Attribute> map = new HashMap<>();
        for(String[] nameToValue : getParameterList(CONFIDENCE_NAMES)){


            String name = nameToValue[0];
            String nominalValue =  nameToValue[1];
            Attribute attribute = exampleSet.getAttributes().get(name);


//            System.out.println("Mapping nominal value " + nominalValue + " to name: " + name );
            System.out.println("Mapping nominal value " + nominalValue + " to attribute with name: " + attribute.getName() );
            map.put(nominalValue , attribute);
        }

        String parameterAttributeName = getParameterAsString(PREDICTION_NAME);
        Attribute parameterPrediction = exampleSet.getAttributes().get(parameterAttributeName);
        return new ConfidenceDefaultModel(exampleSet, map, parameterPrediction);

    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeAttribute(LABEL_NAME, "The attribute to get the label value from.",
                getExampleSetInputPort(), false, false);
        types.add(type);

        type = new ParameterTypeAttribute(PREDICTION_NAME, "The attribute to get the predicted value from.",
                getExampleSetInputPort(), false, false);
        types.add(type);

        types.add(new ParameterTypeList(CONFIDENCE_NAMES, "This parameter defines the value <-> confidence  relation",

                        new ParameterTypeAttribute( PARAMETER_NAME,
                                                "The name of the attribute whose role should be changed.",
                                                getExampleSetInputPort(),
                                                false,
                                                false),

                        new ParameterTypeString(VALUE_NAME,
                                                "The value of that attribute.")
        ,false ));

        return types;
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
//            case ONE_CLASS_LABEL:
//            case NUMERICAL_LABEL:
//            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }
}
