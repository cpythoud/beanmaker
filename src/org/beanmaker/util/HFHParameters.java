package org.beanmaker.util;

import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.InputTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HFHParameters {

    // Base: inputs & textareas
    private String field;
    private long idBean = -1;
    private String value;
    private String fieldLabel;
    private InputTag.InputType inputType;
    private boolean required;
    private boolean disabled;
    private String placeholder;
    private String helpText;

    // selects
    private long selected = -1;
    private List<IdNamePair> selectPairs;
    
    // checkboxes
    private boolean checked;
    private String idNameSuffix;

    // file inputs
    private String currentFile;
    
    // buttons
    private ButtonTag.ButtonType buttonType;
    private String beanName;
    private String functionName;
    private String cssClasses;
    private String buttonLabel;
    private String extraCssClasses;

    // extra parameters
    private Map<String, String> extraParams;

    private synchronized void initExtraParamMap() {
        if (extraParams == null)
            extraParams = new HashMap<String, String>();
    }

    public void setExtra(final String name, final String value) {
        if (extraParams == null)
            initExtraParamMap();

        extraParams.put(name, value);
    }

    public String getExtra(final String name) {
        if (extraParams == null)
            return null;

        return extraParams.get(name);
    }


    public String getField() {
        if (field == null)
            throw new HFHParameterMissingException("field");

        return field;
    }

    public HFHParameters setField(String field) {
        this.field = field;

        return this;
    }

    public long getIdBean() {
        if (idBean == -1)
            throw new HFHParameterMissingException("idBean");

        return idBean;
    }

    public HFHParameters setIdBean(long idBean) {
        this.idBean = idBean;

        return this;
    }

    public String getValue() {
        if (value == null)
            throw new HFHParameterMissingException("value");

        return value;
    }

    public HFHParameters setValue(String value) {
        this.value = value;

        return this;
    }

    public String getFieldLabel() {
        if (fieldLabel == null)
            throw new HFHParameterMissingException("fieldLabel");

        return fieldLabel;
    }

    public HFHParameters setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;

        return this;
    }

    public InputTag.InputType getInputType() {
        if (inputType == null)
            throw new HFHParameterMissingException("inputType");

        return inputType;
    }

    public HFHParameters setInputType(InputTag.InputType inputType) {
        this.inputType = inputType;

        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public HFHParameters setRequired(boolean required) {
        this.required = required;

        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public HFHParameters setDisabled(boolean disabled) {
        this.disabled = disabled;

        return this;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public HFHParameters setPlaceholder(String placeholder) {
        this.placeholder = placeholder;

        return this;
    }

    public String getHelpText() {
        return helpText;
    }

    public HFHParameters setHelpText(String helpText) {
        this.helpText = helpText;

        return this;
    }

    public long getSelected() {
        if (selected == -1)
            throw new HFHParameterMissingException("selected");

        return selected;
    }

    public HFHParameters setSelected(long selected) {
        this.selected = selected;

        return this;
    }

    public List<IdNamePair> getSelectPairs() {
        if (selectPairs == null)
            throw new HFHParameterMissingException("selectPairs");

        return selectPairs;
    }

    public HFHParameters setSelectPairs(List<IdNamePair> selectPairs) {
        this.selectPairs = selectPairs;

        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public HFHParameters setChecked(boolean checked) {
        this.checked = checked;

        return this;
    }

    public String getIdNameSuffix() {
        return idNameSuffix;
    }

    public HFHParameters setIdNameSuffix(String idNameSuffix) {
        this.idNameSuffix = idNameSuffix;

        return this;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public HFHParameters setCurrentFile(String currentFile) {
        this.currentFile = currentFile;

        return this;
    }

    public ButtonTag.ButtonType getButtonType() {
        if (buttonType == null)
            throw new HFHParameterMissingException("buttonType");

        return buttonType;
    }

    public HFHParameters setButtonType(ButtonTag.ButtonType buttonType) {
        this.buttonType = buttonType;

        return this;
    }

    public String getBeanName() {
        return beanName;
    }

    public HFHParameters setBeanName(String beanName) {
        this.beanName = beanName;

        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public HFHParameters setFunctionName(String functionName) {
        this.functionName = functionName;

        return this;
    }

    public String getCssClasses() {
        return cssClasses;
    }

    public HFHParameters setCssClasses(String cssClasses) {
        this.cssClasses = cssClasses;

        return this;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public HFHParameters setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;

        return this;
    }

    public String getExtraCssClasses() {
        return extraCssClasses;
    }

    public HFHParameters setExtraCssClasses(String extraCssClasses) {
        this.extraCssClasses = extraCssClasses;

        return this;
    }
}
