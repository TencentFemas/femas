package com.tencent.tsf.femas.config.model;

import com.tencent.tsf.femas.config.enums.PropertyChangeType;

/**
 * Holds the information for a config change.
 */
public class ConfigChangeEvent<T> {

    private final String propertyName;
    private T oldValue;
    private T newValue;
    private PropertyChangeType changeType;

    /**
     * Constructor.
     *
     * @param propertyName the key whose value is changed
     * @param oldValue the value before change
     * @param newValue the value after change
     * @param changeType the change type
     */
    public ConfigChangeEvent(String propertyName, T oldValue, T newValue,
            PropertyChangeType changeType) {
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public T getOldValue() {
        return oldValue;
    }

    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    public PropertyChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PropertyChangeType changeType) {
        this.changeType = changeType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigChange{");
        sb.append("propertyName='").append(propertyName).append('\'');
        sb.append(", oldValue='").append(oldValue).append('\'');
        sb.append(", newValue='").append(newValue).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append('}');
        return sb.toString();
    }
}
