package me.kiriyaga.nami.feature.setting;

import com.google.gson.JsonElement;
import me.kiriyaga.nami.feature.module.Module;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private String name;
    protected T value;
    private Runnable onChanged = null;
    private boolean show = true;
    private Module parentModule;

    private BooleanSupplier showCondition = null;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        if (onChanged != null) onChanged.run();
    }

    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;
    }

    public boolean isShow() {
        if (showCondition != null) {
            return showCondition.getAsBoolean();
        }
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public void setShowCondition(BooleanSupplier condition) {
        this.showCondition = condition;
    }

    public void setParentModule(Module module) {
        this.parentModule = module;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public abstract void fromJson(JsonElement json);
    public abstract JsonElement toJson();
}