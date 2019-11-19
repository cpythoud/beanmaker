package org.beanmaker.util;

import org.dbbeans.util.Strings;

import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.InputTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private boolean readonly;
    private boolean autocomplete = true;

    // selects
    private String selected;
    private List<IdNamePair> selectPairs;
    private Map<String, List<IdNamePair>> optionGroupSelectPairs;
    
    // checkboxes
    private boolean checked;
    private String idNameSuffix;
    private String checkboxValue;

    // file inputs
    private String currentFile;
    
    // buttons
    private ButtonTag.ButtonType buttonType;
    private String beanName;
    private String functionName;
    private String cssClasses;
    private String buttonLabel;

    // yes/no radio buttons
    private String yesLabel;
    private String noLabel;
    private String yesValue;
    private String noValue;
    private boolean radioButtonsUnchecked;

    // labels
    private boolean asTextArea;

    // extra parameters
    private Map<String, String> extraParams;

    // extra CSS classes
    private String groupExtraCssClasses;
    private String labelExtraCssClasses;
    private String tagExtraCssClasses;


    public HFHParameters() { }

    public HFHParameters(final HFHParameters params) {
        field = params.field;
        idBean = params.idBean;
        value = params.value;
        fieldLabel = params.fieldLabel;
        inputType = params.inputType;
        required = params.required;
        disabled = params.disabled;
        placeholder = params.placeholder;
        helpText = params.helpText;
        readonly = params.readonly;
        autocomplete = params.autocomplete;

        // selects
        selected = params.selected;
        if (params.selectPairs != null)
            selectPairs = new ArrayList<IdNamePair>(params.selectPairs);
        if (params.optionGroupSelectPairs != null) {
            optionGroupSelectPairs = new LinkedHashMap<String, List<IdNamePair>>();
            for (String optionGroupName: params.optionGroupSelectPairs.keySet())
                optionGroupSelectPairs.put(
                        optionGroupName,
                        new ArrayList<IdNamePair>(params.optionGroupSelectPairs.get(optionGroupName)));
        }

        // checkboxes
        checked = params.checked;
        idNameSuffix = params.idNameSuffix;
        checkboxValue = params.checkboxValue;

        // file inputs
        currentFile = params.currentFile;

        // buttons
        buttonType = params.buttonType;
        beanName = params.beanName;
        functionName = params.functionName;
        cssClasses = params.cssClasses;
        buttonLabel = params.buttonLabel;

        // yes/no radio buttons
        yesLabel = params.yesLabel;
        noLabel = params.noLabel;
        yesValue = params.yesValue;
        noValue = params.noValue;
        radioButtonsUnchecked = params.radioButtonsUnchecked;

        // labels
        asTextArea = params.asTextArea;

        if (params.extraParams != null) {
            initExtraParamMap();
            extraParams.putAll(params.extraParams);
        }

        groupExtraCssClasses = params.groupExtraCssClasses;
        labelExtraCssClasses = params.labelExtraCssClasses;
        tagExtraCssClasses = params.tagExtraCssClasses;
    }

    private synchronized void initExtraParamMap() {
        if (extraParams == null)
            extraParams = new HashMap<String, String>();
    }

    // TODO: change return type into HFHParameters for fluent interface / will requires all client to recompile
    public HFHParameters setExtra(final String name, final String value) {
        if (extraParams == null)
            initExtraParamMap();

        extraParams.put(name, value);

        return this;
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

    public HFHParameters setField(final String field) {
        this.field = field;

        return this;
    }

    public long getIdBean() {
        if (idBean == -1)
            throw new HFHParameterMissingException("idBean");

        return idBean;
    }

    public HFHParameters setIdBean(final long idBean) {
        this.idBean = idBean;

        return this;
    }

    public String getValue() {
        if (value == null)
            return "";

        return value;
    }

    public HFHParameters setValue(final String value) {
        this.value = value;

        return this;
    }

    public String getFieldLabel() {
        if (fieldLabel == null)
            throw new HFHParameterMissingException("fieldLabel");

        return fieldLabel;
    }

    public HFHParameters setFieldLabel(final String fieldLabel) {
        this.fieldLabel = fieldLabel;

        return this;
    }

    public InputTag.InputType getInputType() {
        if (inputType == null)
            throw new HFHParameterMissingException("inputType");

        return inputType;
    }

    public HFHParameters setInputType(final InputTag.InputType inputType) {
        this.inputType = inputType;

        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public HFHParameters setRequired(final boolean required) {
        this.required = required;

        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public HFHParameters setDisabled(final boolean disabled) {
        this.disabled = disabled;

        return this;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public HFHParameters setPlaceholder(final String placeholder) {
        this.placeholder = placeholder;

        return this;
    }

    public String getHelpText() {
        return helpText;
    }

    public HFHParameters setHelpText(final String helpText) {
        this.helpText = helpText;

        return this;
    }

    public boolean isReadonly() {
        return readonly;
    }

    // TODO: change return type into HFHParameters for fluent interface / will requires all client to recompile
    public void setReadonly(final boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isAutocomplete() {
        return autocomplete;
    }

    public HFHParameters setAutocomplete(final boolean autocomplete) {
        this.autocomplete = autocomplete;

        return this;
    }

    public String getSelected() {
        if (selected == null)
            throw new HFHParameterMissingException("selected");

        return selected;
    }

    public HFHParameters setSelected(final String selected) {
        this.selected = selected;

        return this;
    }

    public HFHParameters setSelected(final long selected) {
        this.selected = Long.toString(selected);

        return this;
    }

    public List<IdNamePair> getSelectPairs() {
        if (selectPairs == null)
            throw new HFHParameterMissingException("selectPairs");

        return selectPairs;
    }

    public HFHParameters setSelectPairs(final List<IdNamePair> selectPairs) {
        this.selectPairs = selectPairs;

        return this;
    }

    public Map<String, List<IdNamePair>> getOptionGroupSelectPairs() {
        if (optionGroupSelectPairs == null)
            throw new HFHParameterMissingException("optionGroupSelectPairs");

        return optionGroupSelectPairs;
    }

    public boolean hasOptionGroupSelectData() {
        return optionGroupSelectPairs != null;
    }

    public HFHParameters setOptionGroupSelectPairs(Map<String, List<IdNamePair>> optionGroupSelectPairs) {
        this.optionGroupSelectPairs = optionGroupSelectPairs;

        selectPairs = new ArrayList<IdNamePair>();
        for (List<IdNamePair> idNamePairs: optionGroupSelectPairs.values())
            selectPairs.addAll(idNamePairs);

        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public HFHParameters setChecked(final boolean checked) {
        this.checked = checked;

        return this;
    }

    public String getIdNameSuffix() {
        return idNameSuffix;
    }

    public HFHParameters setIdNameSuffix(final String idNameSuffix) {
        this.idNameSuffix = idNameSuffix;

        return this;
    }

    public String getCheckboxValue() {
        return checkboxValue;
    }

    public HFHParameters setCheckboxValue(final String checkboxValue) {
        this.checkboxValue = checkboxValue;

        return this;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public HFHParameters setCurrentFile(final String currentFile) {
        this.currentFile = currentFile;

        return this;
    }

    public boolean hasCurrentFile() {
        return !Strings.isEmpty(currentFile);
    }

    public ButtonTag.ButtonType getButtonType() {
        if (buttonType == null)
            throw new HFHParameterMissingException("buttonType");

        return buttonType;
    }

    public HFHParameters setButtonType(final ButtonTag.ButtonType buttonType) {
        this.buttonType = buttonType;

        return this;
    }

    public String getBeanName() {
        return beanName;
    }

    public HFHParameters setBeanName(final String beanName) {
        this.beanName = beanName;

        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public HFHParameters setFunctionName(final String functionName) {
        this.functionName = functionName;

        return this;
    }

    public String getCssClasses() {
        return cssClasses;
    }

    public HFHParameters setCssClasses(final String cssClasses) {
        this.cssClasses = cssClasses;

        return this;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public HFHParameters setButtonLabel(final String buttonLabel) {
        this.buttonLabel = buttonLabel;

        return this;
    }

    public String getYesLabel() {
        if (yesLabel == null)
            throw new HFHParameterMissingException("yesLabel");

        return yesLabel;
    }

    public HFHParameters setYesLabel(String yesLabel) {
        this.yesLabel = yesLabel;

        return this;
    }

    public String getNoLabel() {
        if (noLabel == null)
            throw new HFHParameterMissingException("noLabel");

        return noLabel;
    }

    public HFHParameters setNoLabel(String noLabel) {
        this.noLabel = noLabel;

        return this;
    }

    public String getYesValue() {
        if (yesValue == null)
            return "true";

        return yesValue;
    }

    public HFHParameters setYesValue(String yesValue) {
        this.yesValue = yesValue;

        return this;
    }

    public String getNoValue() {
        if (noValue == null)
            return "false";

        return noValue;
    }

    public HFHParameters setNoValue(String noValue) {
        this.noValue = noValue;

        return this;
    }

    public boolean areRadioButtonsUnchecked() {
        return radioButtonsUnchecked;
    }

    public HFHParameters setRadioButtonsUnchecked(boolean radioButtonsUnchecked) {
        this.radioButtonsUnchecked = radioButtonsUnchecked;

        return this;
    }

    public HFHParameters setAsTextArea(boolean forceTextArea) {
        this.asTextArea = forceTextArea;

        return this;
    }

    public boolean isAsTextArea() {
        return asTextArea;
    }

    public String getGroupExtraCssClasses() {
        return groupExtraCssClasses;
    }

    public HFHParameters setGroupExtraCssClasses(String groupExtraCssClasses) {
        this.groupExtraCssClasses = groupExtraCssClasses;

        return this;
    }

    public boolean hasGroupExtraCssClasses() {
        return !Strings.isEmpty(groupExtraCssClasses);
    }

    public String getLabelExtraCssClasses() {
        return labelExtraCssClasses;
    }

    public HFHParameters setLabelExtraCssClasses(String labelExtraCssClasses) {
        this.labelExtraCssClasses = labelExtraCssClasses;

        return this;
    }

    public boolean hasLabelExtraCssClasses() {
        return !Strings.isEmpty(labelExtraCssClasses);
    }

    public String getTagExtraCssClasses() {
        return tagExtraCssClasses;
    }

    public HFHParameters setTagExtraCssClasses(String tagExtraCssClasses) {
        this.tagExtraCssClasses = tagExtraCssClasses;

        return this;
    }

    public boolean hasTagExtraCssClasses() {
        return !Strings.isEmpty(tagExtraCssClasses);
    }
}
