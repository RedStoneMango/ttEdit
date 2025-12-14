package io.github.redstonemango.ttedit.front.propertySheetHelpers;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class SliderPropertyEditor implements PropertyEditor<Number> {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d*(\\.\\d*)?$");

    private final HBox editor;
    private final Slider slider;

    public SliderPropertyEditor(PropertySheet.Item item, double min, double max, boolean decimal) {
        TextField inputField = new TextField();
        AtomicBoolean ignoreTextChange = new AtomicBoolean(false);
        AtomicBoolean ignoreSliderChange = new AtomicBoolean(false);

        slider = new Slider(min, max, ((Number) item.getValue()).doubleValue());
        slider.valueProperty().addListener((_, _, newV) -> {
            item.setValue(newV.doubleValue());

            if (ignoreSliderChange.get()) {
                ignoreSliderChange.set(false);
                return;
            }

            ignoreTextChange.set(true);
            inputField.setText(buildVal(newV.doubleValue(), decimal));
        });
        HBox.setHgrow(slider, Priority.ALWAYS);


        inputField.setPrefWidth(60);
        inputField.setText(buildVal(slider.getValue(), decimal));
        inputField.textProperty().addListener((_, previous, current) -> {
            if (ignoreTextChange.get()) {
                ignoreTextChange.set(false);
                return;
            }

            if (!NUMBER_PATTERN.matcher(current).matches()) {
                ignoreTextChange.set(true);
                inputField.setText(previous);
                return;
            }

            // Let's prevent some parsing bugs
            if (current.equals("-") || current.equals(".")) current = "";

            if (current.isEmpty()) {
                ignoreSliderChange.set(true);
                slider.setValue(0);
                return;
            }
            double val = Double.parseDouble(current);
            ignoreSliderChange.set(true);
            slider.setValue(Math.clamp(val, min, max));
        });
        inputField.focusedProperty().addListener((_, _, isFocused) -> {
            if (!isFocused) {
                if (inputField.getText().isBlank()) {
                    ignoreTextChange.set(true);
                    inputField.setText(buildVal(0, decimal));

                    ignoreSliderChange.set(true);
                    slider.setValue(0);
                    return;
                }
                String text = inputField.getText();
                if (text.equals(".") || text.equals("-")) text = "0";

                double val = Double.parseDouble(text);
                ignoreTextChange.set(true);
                inputField.setText(buildVal(
                        Math.clamp(val, min, max),
                    decimal)
                );

            }
        });

        editor = new HBox(10, slider, inputField);
        editor.setPadding(new Insets(0, 10, 0, 10));
    }

    private static String buildVal(double val, boolean decimal) {
        String valS = String.valueOf(val);
        if (valS.contains(".")) { // Just to be safe. This is expected to be true all the time
            if (decimal) {
                valS = valS.substring(0, Math.min(valS.indexOf(".") + 5, valS.length())); // 4 decimal points only
            }
            else {
                valS = valS.substring(0, valS.indexOf("."));
            }
        }
        return valS;
    }

    @Override public Node getEditor() { return editor; }
    @Override public Number getValue() { return slider.getValue(); }
    @Override public void setValue(Number value) { slider.setValue(value.doubleValue()); }
}
