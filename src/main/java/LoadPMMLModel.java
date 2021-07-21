import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoadPMMLModel {
    public static void main(String[] args) throws JAXBException, IOException, SAXException {
        String modelFolder = LoadPMMLModel.class.getClassLoader().getResource("model").getPath();
        String modelName = "boosting_model.pmml";
        Path modelPath = Paths.get(modelFolder, modelName);

        Evaluator evaluator = new LoadingModelEvaluatorBuilder()
                .load(modelPath.toFile())
                .build();
        evaluator.verify();

        FieldName targetName = evaluator.getTargetFields().get(0).getName();
        List<InputField> inputFields = evaluator.getInputFields();

        Map<String, Double> features = new HashMap<>();
        features.put("sepal length (cm)", 6.1);
        features.put("sepal width (cm)", 2.8);
        features.put("petal length (cm)", 4.7);
        features.put("petal width (cm)", 1.2);

        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        for (InputField inputField : inputFields) {
            FieldName inputName = inputField.getName();
            Double value = features.get(inputName.toString());
            FieldValue inputValue = inputField.prepare(value);
            arguments.put(inputName, inputValue);
        }

        // Evaluating the model with known-good arguments
        Map<FieldName, ?> results = evaluator.evaluate(arguments);

        // Extracting prediction
        Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
        Integer yPred = (Integer) resultRecord.get(targetName.toString());
        System.out.printf("Prediction is %d\n", yPred);
        System.out.printf("PMML output %s\n", resultRecord);
    }
}
