package de.c20z.crosshair;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Ganzzahl-Slider mit Beschriftung "Name: Wert". */
public class ValueSlider extends AbstractSliderButton {
    private final String label;
    private final int min;
    private final int max;
    private final IntConsumer setter;
    private final IntFunction<String> format;

    public ValueSlider(int x, int y, int width, String label, int min, int max, int current, IntConsumer setter) {
        this(x, y, width, label, min, max, current, setter, String::valueOf);
    }

    public ValueSlider(int x, int y, int width, String label, int min, int max, int current,
                       IntConsumer setter, IntFunction<String> format) {
        super(x, y, width, 20, Component.empty(), (current - min) / (double) (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.setter = setter;
        this.format = format;
        updateMessage();
    }

    private int current() {
        return min + (int) Math.round(this.value * (max - min));
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + format.apply(current())));
    }

    @Override
    protected void applyValue() {
        setter.accept(current());
    }
}
