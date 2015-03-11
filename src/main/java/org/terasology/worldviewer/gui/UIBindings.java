/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.terasology.rendering.nui.properties.Checkbox;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.nui.properties.OneOf.Enum;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.worldviewer.config.FacetConfig;
import org.terasology.worldviewer.lambda.Lambda;
import org.terasology.worldviewer.layers.FacetLayer;

/**
 * Provides a set of static methods that map pairs of
 * {@link Supplier} and {@link Consumer} to Swing UI elements
 * and attach listeners.
 * @author Martin Steiger
 */
public final class UIBindings {

    private UIBindings() {
        // no instances
    }

    public static JCheckBox processCheckboxAnnotation(FacetLayer layer, Field field) {
        FacetConfig config = layer.getConfig();
        Checkbox checkbox = field.getAnnotation(Checkbox.class);

        if (checkbox != null) {
            JLabel label = new JLabel(checkbox.label().isEmpty() ? field.getName() : checkbox.label());
            label.setToolTipText(checkbox.description());

            Supplier<Boolean> getter = Lambda.toRuntime(() -> field.getBoolean(config));
            Consumer<Boolean> setter = Lambda.toRuntime(v -> { field.setBoolean(config, v.booleanValue()); layer.notifyObservers(); });
            JCheckBox component = createCheckbox(getter, setter);
            component.setToolTipText(checkbox.description());

            return component;
        }

        return null;
    }

    public static JCheckBox createCheckbox(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        JCheckBox checkBox = new JCheckBox("visible");
        checkBox.setSelected(getter.get());
        checkBox.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                checkBox.setSelected(getter.get());
            }
        });
        checkBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setter.accept(checkBox.isSelected());
            }
        });

        return checkBox;
    }

    public static JSpinner processRangeAnnotation(FacetLayer layer, Field field) {
        FacetConfig config = layer.getConfig();
        Range range = field.getAnnotation(Range.class);

        if (range != null) {
            double min = range.min();
            double max = range.max();
            double stepSize = range.increment();
            Supplier<Double> getter = Lambda.toRuntime(() -> field.getDouble(config));
            Consumer<Double> setter = Lambda.toRuntime(v -> { field.setFloat(config, v.floatValue()); layer.notifyObservers(); });
            JSpinner spinner = createSpinner(min, stepSize, max, getter, setter);
            spinner.setToolTipText(range.description());

            return spinner;
        }

        return null;
    }

    public static JSpinner createSpinner(double min, double stepSize, double max, Supplier<Double> getter, Consumer<Double> setter) {
        double initValue = getter.get().doubleValue();

        final SpinnerNumberModel model = new SpinnerNumberModel(initValue, min, max, stepSize);
        final JSpinner spinner = new JSpinner(model);
        spinner.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                spinner.setValue(getter.get());
            }
        });
        spinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Double value = (Double) model.getValue();
                setter.accept(value);
            }
        });

        return spinner;
    }

    /**
     * Maps an @Enum field to a combobox
     * @param layer the facet layer that contains the config
     * @param field the (potentially annotated field)
     * @return a combobox for the annotated field or <code>null</code> if not applicable
     */
    public static JComboBox<?> processEnumAnnotation(FacetLayer layer, Field field) {
        FacetConfig config = layer.getConfig();
        Enum en = field.getAnnotation(Enum.class);
        Class<?> clazz = field.getType(); // the enum class

        if (en != null && clazz.isEnum()) {
            JLabel label = new JLabel(en.label().isEmpty() ? field.getName() : en.label());
            label.setToolTipText(en.description());

            Supplier<Object> getter = Lambda.toRuntime(() -> field.get(config));
            Consumer<Object> setter = Lambda.toRuntime(v -> { field.set(config, v); layer.notifyObservers(); });
            JComboBox<?> combo = createCombo(clazz.getEnumConstants(), getter, setter);
            combo.setToolTipText(en.description());
            return combo;
        }

        return null;
    }

    public static <T> JComboBox<T> createCombo(T[] elements, Supplier<T> getter, Consumer<T> setter) {
        T initValue = getter.get();

        JComboBox<T> combo = new JComboBox<T>(elements);
        combo.setSelectedItem(initValue);
//        combo.setRenderer(wgTextRenderer);
        combo.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                combo.setSelectedItem(getter.get());
            }
        });
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = combo.getSelectedIndex();
                setter.accept(combo.getItemAt(idx));
            }
        });

        return combo;
    }
}
