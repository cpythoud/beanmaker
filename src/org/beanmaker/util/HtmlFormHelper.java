package org.beanmaker.util;

import org.dbbeans.util.Strings;

import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.CData;
import org.jcodegen.html.DivTag;
import org.jcodegen.html.FormTag;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.LabelTag;
import org.jcodegen.html.OptionTag;
import org.jcodegen.html.PTag;
import org.jcodegen.html.SelectTag;
import org.jcodegen.html.Tag;
import org.jcodegen.html.TextareaTag;

import java.util.Arrays;
import java.util.List;

public class HtmlFormHelper {

    private String notRequiredExtension = "";
    private String requiredExtension = " *";
    private boolean useRequiredInHtml = true;

    private String htmlFormAction = null;
    private boolean htmlFormMultipart = false;

    private String defaultEncoding = null;


    private boolean inline = false;
    private boolean inlineWithoutLabels = false;
    private boolean horizontal = false;

    private String horizontalSizeShift = "sm";
    private int horizontalLabelWidth = 4;
    private int horizontalFieldWidth = 8;


    private InputTag.InputType inputTypeForDateFields = null;
    private String cssClassForDateFields = null;
    private InputTag.InputType inputTypeForTimeFields = null;
    private String cssClassForTimeFields = null;

    private String cssClassForFileFields = "file";

    private String extraFormCssClasses = null;


    public String getNotRequiredExtension() {
        return notRequiredExtension;
    }

    public void setNotRequiredExtension(final String notRequiredExtension) {
        this.notRequiredExtension = notRequiredExtension;
    }

    public String getRequiredExtension() {
        return requiredExtension;
    }

    public void setRequiredExtension(final String requiredExtension) {
        this.requiredExtension = requiredExtension;
    }

    public boolean useRequiredInHtml() {
        return useRequiredInHtml;
    }

    public void useRequiredInHtml(final boolean useRequiresInHtml) {
        this.useRequiredInHtml = useRequiresInHtml;
    }

    public String getHtmlFormAction() {
        return htmlFormAction;
    }

    public void setHtmlFormAction(final String htmlFormAction) {
        this.htmlFormAction = htmlFormAction;
    }

    public boolean htmlFormMultipart() {
        return htmlFormMultipart;
    }

    public void htmlFormMultipart(final boolean htmlFormMultipart) {
        this.htmlFormMultipart = htmlFormMultipart;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }


    public void setInline(final boolean inline) {
        this.inline = inline;
        if (inline)
            horizontal = false;
        else
            inlineWithoutLabels = false;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInlineWithoutLabels(final boolean inlineWithoutLabels) {
        this.inlineWithoutLabels = inlineWithoutLabels;
        if (inlineWithoutLabels) {
            horizontal = false;
            inline = true;
        }
    }

    public boolean isInlineWithoutLabels() {
        return inlineWithoutLabels;
    }

    public void setHorizontal(final boolean horizontal) {
        this.horizontal = horizontal;
        if (horizontal) {
            inline = false;
            inlineWithoutLabels = false;
        }
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    private static final List<String> BOOTSTRAP_SIZES = Arrays.asList("xs", "sm", "md", "lg");

    public void setHorizontalFormParameters(final String horizontalSizeShift, final int horizontalLabelWidth, final int horizontalFieldWidth) {
        if (!BOOTSTRAP_SIZES.contains(horizontalSizeShift))
            throw new IllegalArgumentException("Unknown Boostrap size: " + horizontalSizeShift);
        if (horizontalLabelWidth < 0 || horizontalLabelWidth > 12)
            throw new IllegalArgumentException("Illegal column index for label width: " + horizontalLabelWidth);
        if (horizontalFieldWidth < 0 || horizontalFieldWidth > 12)
            throw new IllegalArgumentException("Illegal column index for field width:" + horizontalFieldWidth);
        if (horizontalLabelWidth + horizontalFieldWidth > 12)
            throw new IllegalArgumentException("Column count for label + field is incorrect (> 12): " + horizontalLabelWidth + " + " + horizontalFieldWidth + " = " + (horizontalLabelWidth + horizontalFieldWidth));

        this.horizontalSizeShift = horizontalSizeShift;
        this.horizontalLabelWidth = horizontalLabelWidth;
        this.horizontalFieldWidth = horizontalFieldWidth;

        setHorizontal(true);
    }

    public String getHorizontalSizeShift() {
        return horizontalSizeShift;
    }

    public int getHorizontalLabelWidth() {
        return horizontalLabelWidth;
    }

    public int getHorizontalFieldWidth() {
        return horizontalFieldWidth;
    }

    public void setInputTypeForDateFields(final InputTag.InputType inputTypeForDateFields) {
        this.inputTypeForDateFields = inputTypeForDateFields;
    }

    public InputTag.InputType getInputTypeForDateFields() {
        return inputTypeForDateFields;
    }

    public void setCssClassForDateFields(final String cssClassForDateFields) {
        this.cssClassForDateFields = cssClassForDateFields;
    }

    public String getCssClassForDateFields() {
        return cssClassForDateFields;
    }

    public void setInputTypeForTimeFields(final InputTag.InputType inputTypeForTimeFields) {
        this.inputTypeForTimeFields = inputTypeForTimeFields;
    }

    public InputTag.InputType getInputTypeForTimeFields() {
        return inputTypeForTimeFields;
    }

    public void setCssClassForTimeFields(final String cssClassForTimeFields) {
        this.cssClassForTimeFields = cssClassForTimeFields;
    }

    public String getCssClassForTimeFields() {
        return cssClassForTimeFields;
    }

    public void setCssClassForFileFields(final String cssClassForFileFields) {
        this.cssClassForFileFields = cssClassForFileFields;
    }

    public String getCssClassForFileFields() {
        return cssClassForFileFields;
    }

    public void setExtraFormCssClasses(final String extraFormCssClasses) {
        this.extraFormCssClasses = extraFormCssClasses;
    }

    public String getExtraFormCssClasses() {
        return extraFormCssClasses;
    }


    protected String getFormCssClasses(final String beanName) {
        final StringBuilder formCssClasses = new StringBuilder();

        formCssClasses.append(beanName).append("-form");

        if (inline)
            formCssClasses.append(" form-inline");
        if (horizontal)
            formCssClasses.append(" form-horizontal");

        if (extraFormCssClasses != null)
            formCssClasses.append(" ").append(extraFormCssClasses);

        return formCssClasses.toString();
    }

    protected FormTag getFormTag(final String beanName, final long id) {
        final FormTag form =
                new FormTag()
                        .role("form")
                        .id(getHtmlId(beanName, id))
                        .name(beanName)
                        .method(FormTag.Method.POST);

        if (htmlFormMultipart)
            return form.enctype(FormTag.EncodingType.MULTIPART);

        return form;
    }

    protected void resetFormTypeFlags() {
        inline = false;
        inlineWithoutLabels = false;
        horizontal = false;
    }

    public FormTag getForm(final String beanName, final long id) {
        final FormTag form = getFormTag(beanName, id);

        resetFormTypeFlags();

        return form.cssClass(getFormCssClasses(beanName));
    }

    public FormTag getInlineForm(final String beanName, final long id) {
        final FormTag form = getFormTag(beanName, id);

        resetFormTypeFlags();
        inline = true;

        return form.cssClass(getFormCssClasses(beanName));
    }

    public FormTag getInlineFormWithoutLabels(final String beanName, final long id) {
        final FormTag form = getFormTag(beanName, id);

        resetFormTypeFlags();
        inline = true;
        inlineWithoutLabels = true;

        return form.cssClass(getFormCssClasses(beanName));
    }

    public FormTag getHorizontalForm(final String beanName, final long id) {
        final FormTag form = getFormTag(beanName, id);

        resetFormTypeFlags();
        horizontal = true;

        return form.cssClass(getFormCssClasses(beanName));
    }

    public InputTag getHiddenSubmitInput(final String beanName, final long id) {
        return new InputTag(InputTag.InputType.HIDDEN).name("submitted" + beanName).value(Long.toString(id));
    }

    public DivTag getTextField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final InputTag.InputType type,
            final boolean required)
    {
        return getTextField(field, idBean, value, fieldLabel, type, required, null);
    }

    public DivTag getTextField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final InputTag.InputType type,
            final boolean required,
            final boolean disabled) {
        return getTextField(field, idBean, value, fieldLabel, type, required, null, disabled);
    }

    public DivTag getTextField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final InputTag.InputType type,
            final boolean required,
            final String placeholder)
    {
        return getTextField(field, idBean, value, fieldLabel, type, required, placeholder, false);
    }

    public DivTag getTextField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final InputTag.InputType type,
            final boolean required,
            final String placeholder,
            final boolean disabled)
    {
        return getTextField(field, idBean, value, fieldLabel, type, required, placeholder, disabled, null);
    }

    public DivTag getTextField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final InputTag.InputType type,
            final boolean required,
            final String placeholder,
            final boolean disabled,
            final String helpText)
    {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final InputTag input = getInputTag(type, fieldId, field, value);
        if (required && useRequiredInHtml)
            input.required();
        if (placeholder != null)
            input.placeholder(placeholder);
        if (disabled)
            input.disabled();

        return getFormGroup(label, input, helpText);
    }

    protected String getFieldId(final String field, final long idBean) {
        return getFieldId(field, idBean, null);
    }

    protected String getFieldId(final String field, final long idBean, final String idNamePostfix) {
        if (Strings.isEmpty(idNamePostfix))
            return field + "_" + idBean;

        return field + "_" + idNamePostfix + "_" + idBean;
    }

    protected InputTag getInputTag(final InputTag.InputType type, final String id, final String name, final String value) {
        if (type == InputTag.InputType.DATE)
            return new InputTag(inputTypeForDateFields == null ? type : inputTypeForDateFields)
                    .cssClass(cssClassForDateFields == null ? "form-control" : "form-control " + cssClassForDateFields)
                    .id(id).name(name).value(value);

        if (type == InputTag.InputType.TIME)
            return new InputTag(inputTypeForTimeFields == null ? type : inputTypeForTimeFields)
                    .cssClass(cssClassForTimeFields == null ? "form-control" : "form-control " + cssClassForTimeFields)
                    .id(id).name(name).value(value);

        if (type == InputTag.InputType.FILE) {
            final InputTag fileInput = new InputTag(InputTag.InputType.FILE);
            if (!Strings.isEmpty(cssClassForFileFields))
                fileInput.cssClass(cssClassForFileFields);
            if (!Strings.isEmpty(value))
                fileInput.placeholder(value);
            return fileInput.id(id).name(name);
        }

        return new InputTag(type).cssClass("form-control").id(id).name(name).value(value);
    }

    public DivTag getFormGroup() {
        return new DivTag().cssClass("form-group");
    }

    public DivTag getFormGroup(final LabelTag label, final Tag field) {
        return getFormGroup(label, field, null);
    }

    public DivTag getFormGroup(final LabelTag label, final Tag field, final String helpText) {
        final DivTag formGroup =
                new DivTag().cssClass("form-group")
                        .child(label);

        if (horizontal) {
            final DivTag formElements =
                    new DivTag().cssClass(getHorizontalFieldClass())
                            .child(field);
            if (helpText != null)
                formElements.child(getHelperBlock(helpText));
            formGroup.child(formElements);
        } else {
            formGroup.child(field);
            if (helpText != null)
                formGroup.child(getHelperBlock(helpText));
        }

        return formGroup;
    }

    protected Tag getHelperBlock(final String helpText) {
        return new PTag(helpText).cssClass("helpBlock");
    }

    protected LabelTag getLabel(final String fieldLabel, final String fieldId, final boolean required) {
        final LabelTag label = new LabelTag(getLabelText(fieldLabel, required), fieldId);

        if (inlineWithoutLabels)
            label.cssClass("sr-only");
        if (horizontal)
            label.cssClass(getHorizontalLabelClasses());

        return label;
    }

    protected String getLabelText(final String fieldLabel, final boolean required) {
        if (required)
            return fieldLabel + requiredExtension;

        return fieldLabel + notRequiredExtension;
    }

    protected String getHorizontalLabelClasses() {
        return getHorizontalLabelClass() + " control-label";
    }

    public String getHorizontalLabelClass() {
        return "col-" + horizontalSizeShift + "-" + horizontalLabelWidth;
    }

    public String getHorizontalFieldClass() {
        return "col-" + horizontalSizeShift + "-" + horizontalFieldWidth;
    }

    public ButtonTag getSubmitButtonTag(final String beanName, final long id, final String buttonLabel) {
        return getSubmitButtonTag(beanName, id, buttonLabel, null);
    }

    public ButtonTag getSubmitButtonTag(
            final String beanName,
            final long id,
            final String buttonLabel,
            final String extraCssClasses)
    {
        return getButtonTag(
                ButtonTag.ButtonType.SUBMIT,
                beanName,
                id,
                "submit",
                buttonLabel,
                "btn btn-default" + (extraCssClasses == null ? "" : " " + extraCssClasses));
    }

    public ButtonTag getButtonTag(
            final ButtonTag.ButtonType type,
            final String beanName,
            final long id,
            final String functionName,
            final String buttonLabel,
            final String cssClasses)
    {
        return new ButtonTag(type)
                .child(new CData(buttonLabel))
                .id(getHtmlId(beanName + "_" + functionName, id))
                .cssClass(cssClasses);
    }

    public Tag getSubmitButton(final String beanName, final long id, final String buttonLabel) {
        return getSubmitButton(beanName, id, buttonLabel, false);
    }

    public Tag getSubmitButton(final String beanName, final long id, final String buttonLabel, final boolean disabled) {
        final ButtonTag submit = getSubmitButtonTag(beanName, id, buttonLabel);

        if (disabled)
            submit.disabled();

        if (horizontal)
            return getFormGroup().child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(submit));

        return submit;
    }

    public String getHorizontalFieldClassesWithOffset() {
        return "col-" + horizontalSizeShift + "-offset-" + horizontalLabelWidth + " col-" + horizontalSizeShift + "-" + horizontalFieldWidth;
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final long selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required)
    {
        return getSelectField(field, idBean, Long.toString(selected), fieldLabel, pairs, required);
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final long selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required,
            final boolean disabled)
    {
        return getSelectField(field, idBean, Long.toString(selected), fieldLabel, pairs, required, disabled);
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final long selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required,
            final boolean disabled,
            final String helpText)
    {
        return getSelectField(field, idBean, Long.toString(selected), fieldLabel, pairs, required, disabled, helpText);
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final String selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required)
    {
        return getSelectField(field, idBean, selected, fieldLabel, pairs, required, false);
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final String selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required,
            final boolean disabled)
    {
        return getSelectField(field, idBean, selected, fieldLabel, pairs, required, disabled, null);
    }

    public DivTag getSelectField(
            final String field,
            final long idBean,
            final String selected,
            final String fieldLabel,
            final List<IdNamePair> pairs,
            final boolean required,
            final boolean disabled,
            final String helpText)
    {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final SelectTag select = getSelectTag(field, fieldId);
        if (required && useRequiredInHtml)
            select.required();
        if (disabled)
            select.disabled();

        for (IdNamePair pair: pairs) {
            final OptionTag optionTag = new OptionTag(pair.getName(), pair.getId());
            if (pair.getId().equals(selected))
                optionTag.selected();
            select.child(optionTag);
        }

        return getFormGroup(label, select, helpText);
    }

    protected SelectTag getSelectTag(final String name, final String id) {
        return new SelectTag(name).cssClass("form-control").id(id);
    }

    public DivTag getTextAreaField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final boolean required)
    {
        return getTextAreaField(field, idBean, value, fieldLabel, required, false);
    }

    public DivTag getTextAreaField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final boolean required,
            final boolean disabled)
    {
        return getTextAreaField(field, idBean, value, fieldLabel, required, disabled, null);
    }

    public DivTag getTextAreaField(
            final String field,
            final long idBean,
            final String value,
            final String fieldLabel,
            final boolean required,
            final boolean disabled,
            final String helpText)
    {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final TextareaTag textarea = getTextAreaTag(fieldId, field, value);
        if (required && useRequiredInHtml)
            textarea.required();
        if (disabled)
            textarea.disabled();

        return getFormGroup(label, textarea, helpText);
    }

    protected TextareaTag getTextAreaTag(final String id, final String name, final String value) {
        return new TextareaTag(value).cssClass("form-control").id(id).name(name);
    }

    public DivTag getCheckboxField(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel)
    {
        return getCheckboxField(field, idBean, checked, fieldLabel, false);
    }

    public DivTag getCheckboxField(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled)
    {
        return getCheckboxField(field, idBean, checked, fieldLabel, disabled, null);
    }

    public DivTag getCheckboxField(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled,
            final String value)
    {
        return getCheckboxField(field, idBean, checked, fieldLabel, disabled, value, null);
    }

    public DivTag getCheckboxField(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled,
            final String value,
            final String idNameSuffix)
    {
        final DivTag innerPart = getCheckbox(field, idBean, checked, fieldLabel, disabled, value, idNameSuffix);

        if (horizontal)
            return getFormGroup().child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(innerPart));

        return innerPart;
    }

    protected DivTag getCheckbox(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel)
    {
        return getCheckbox(field, idBean, checked, fieldLabel, false);
    }

    protected DivTag getCheckbox(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled)
    {
        return getCheckbox(field, idBean, checked, fieldLabel, disabled, null);
    }

    protected DivTag getCheckbox(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled,
            final String value)
    {
        return getCheckbox(field, idBean, checked, fieldLabel, disabled, value, null);
    }

    protected DivTag getCheckbox(
            final String field,
            final long idBean,
            final boolean checked,
            final String fieldLabel,
            final boolean disabled,
            final String value,
            final String idNameSuffix)
    {
        return new DivTag()
                .cssClass("checkbox")
                .child(new LabelTag()
                                .child(getCheckboxTag(field, idBean, checked, disabled, value, idNameSuffix))
                                .child(new CData(" " + fieldLabel))
                );
    }

    protected InputTag getCheckboxTag(
            final String field,
            final long idBean,
            final boolean checked,
            final boolean disabled)
    {
        return getCheckboxTag(field, idBean, checked, disabled, null);
    }

    protected InputTag getCheckboxTag(
            final String field,
            final long idBean,
            final boolean checked,
            final boolean disabled,
            final String value)
    {
        return getCheckboxTag(field, idBean, checked, disabled, value, null);
    }

    protected InputTag getCheckboxTag(
            final String field,
            final long idBean,
            final boolean checked,
            final boolean disabled,
            final String value,
            final String idNameSuffix)
    {
        final InputTag checkbox = new InputTag(InputTag.InputType.CHECKBOX).name(field).id(getFieldId(field, idBean, idNameSuffix));
        if (checked)
            checkbox.checked();
        if (disabled)
            checkbox.disabled();
        if (value != null)
            checkbox.value(value);

        return checkbox;
    }

    protected String getHtmlId(final String beanName, final long id) {
        return beanName + "_" + id;
    }

    public DivTag getFileField(final String field, final long idBean, final String currentFile,
                               final String fieldLabel, final boolean required) {
        return getFileField(field, idBean, currentFile, fieldLabel, required, false);
    }

    public DivTag getFileField(final String field, final long idBean, final String currentFile,
                               final String fieldLabel, final boolean required, final boolean disabled) {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final InputTag input = getInputTag(InputTag.InputType.FILE, fieldId, field, currentFile);
        if (required && useRequiredInHtml)
            input.required();
        if (disabled)
            input.disabled();

        return getFormGroup(label, input);
    }

    public Tag getHiddenInfo(final String name, final String value) {
        return new InputTag(InputTag.InputType.HIDDEN).name(name).value(value);
    }
}

