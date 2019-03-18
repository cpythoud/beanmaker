package org.beanmaker.util;

import org.dbbeans.util.Strings;

import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.CData;
import org.jcodegen.html.DivTag;
import org.jcodegen.html.FormElement;
import org.jcodegen.html.FormTag;
import org.jcodegen.html.HtmlCodeFragment;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.LabelTag;
import org.jcodegen.html.OptionTag;
import org.jcodegen.html.PTag;
import org.jcodegen.html.SelectTag;
import org.jcodegen.html.SpanTag;
import org.jcodegen.html.Tag;
import org.jcodegen.html.TextareaTag;
import org.jcodegen.html.util.CssClasses;

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
    private boolean readonly = false;
    private String readonlyExtension = "-readonly";

    private String horizontalSizeShift = "sm";
    private int horizontalLabelWidth = 4;
    private int horizontalFieldWidth = 8;


    private InputTag.InputType inputTypeForDateFields = null;
    private String cssClassForDateFields = null;
    private InputTag.InputType inputTypeForTimeFields = null;
    private String cssClassForTimeFields = null;
    private InputTag.InputType inputTypeForNumberFields = null;
    private String cssClassForNumberFields = null;

    private String cssClassForFileFields = "file";
    private String uploadButtonLabel = "Upload";
    private String uploadButtonCssClasses = "btn btn-default btn-sm";
    private String uploadNoFileLabel = "(no file)";
    private String uploadFilenameDisplayCssClasses = "file-display";
    private String uploadRemoveFileLabel = "";
    private String uploadRemoveFileCssClasses = "remove-file glyphicons glyphicons-remove text-danger";
    private String uploadRemoveFileTitle = "Remove file";

    private String extraFormCssClasses = null;

    private String readonlyPostfix = "-ro";


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

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonlyExtension(String readonlyExtension) {
        this.readonlyExtension = readonlyExtension;
    }

    public String getReadonlyExtension() {
        return readonlyExtension;
    }

    private static final List<String> BOOTSTRAP_SIZES = Arrays.asList("xs", "sm", "md", "lg");

    public void setHorizontalFormParameters(
            final String horizontalSizeShift,
            final int horizontalLabelWidth,
            final int horizontalFieldWidth)
    {
        if (!BOOTSTRAP_SIZES.contains(horizontalSizeShift))
            throw new IllegalArgumentException("Unknown Boostrap size: " + horizontalSizeShift);
        if (horizontalLabelWidth < 0 || horizontalLabelWidth > 12)
            throw new IllegalArgumentException("Illegal column index for label width: " + horizontalLabelWidth);
        if (horizontalFieldWidth < 0 || horizontalFieldWidth > 12)
            throw new IllegalArgumentException("Illegal column index for field width:" + horizontalFieldWidth);
        if (horizontalLabelWidth + horizontalFieldWidth > 12)
            throw new IllegalArgumentException(
                    "Column count for label + field is incorrect (> 12): " + horizontalLabelWidth
                            + " + " + horizontalFieldWidth
                            + " = " + (horizontalLabelWidth + horizontalFieldWidth));

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

    public void setInputTypeForNumberFields(final InputTag.InputType inputTypeForNumberFields) {
        this.inputTypeForNumberFields = inputTypeForNumberFields;
    }

    public InputTag.InputType getInputTypeForNumberFields() {
        return inputTypeForNumberFields;
    }

    public void setCssClassForNumberFields(final String cssClassForNumberFields) {
        this.cssClassForNumberFields = cssClassForNumberFields;
    }

    public String getCssClassForNumberFields() {
        return cssClassForNumberFields;
    }

    public void setCssClassForFileFields(final String cssClassForFileFields) {
        this.cssClassForFileFields = cssClassForFileFields;
    }

    public void setUploadButtonLabel(final String uploadButtonLabel) {
        this.uploadButtonLabel = uploadButtonLabel;
    }

    public void setUploadButtonCssClasses(final String uploadButtonCssClasses) {
        this.uploadButtonCssClasses = uploadButtonCssClasses;
    }

    public void setUseRequiredInHtml(final boolean useRequiredInHtml) {
        this.useRequiredInHtml = useRequiredInHtml;
    }

    public void setUploadNoFileLabel(final String uploadNoFileLabel) {
        this.uploadNoFileLabel = uploadNoFileLabel;
    }

    public void setUploadFilenameDisplayCssClasses(final String uploadFilenameDisplayCssClasses) {
        this.uploadFilenameDisplayCssClasses = uploadFilenameDisplayCssClasses;
    }

    public void setUploadRemoveFileLabel(final String uploadRemoveFileLabel) {
        this.uploadRemoveFileLabel = uploadRemoveFileLabel;
    }

    public void setUploadRemoveFileCssClasses(final String uploadRemoveFileCssClasses) {
        this.uploadRemoveFileCssClasses = uploadRemoveFileCssClasses;
    }

    public void setUploadRemoveFileTitle(final String uploadRemoveFileTitle) {
        this.uploadRemoveFileTitle = uploadRemoveFileTitle;
    }

    public String getCssClassForFileFields() {
        return cssClassForFileFields;
    }

    public String getUploadButtonLabel() {
        return uploadButtonLabel;
    }

    public String getUploadButtonCssClasses() {
        return uploadButtonCssClasses;
    }

    public String getUploadNoFileLabel() {
        return uploadNoFileLabel;
    }

    public String getUploadFilenameDisplayCssClasses() {
        return uploadFilenameDisplayCssClasses;
    }

    public String getUploadRemoveFileLabel() {
        return uploadRemoveFileLabel;
    }

    public String getUploadRemoveFileCssClasses() {
        return uploadRemoveFileCssClasses;
    }

    public String getUploadRemoveFileTitle() {
        return uploadRemoveFileTitle;
    }

    public void setExtraFormCssClasses(final String extraFormCssClasses) {
        this.extraFormCssClasses = extraFormCssClasses;
    }

    public String getExtraFormCssClasses() {
        return extraFormCssClasses;
    }

    public void setReadonlyPostfix(final String readonlyPostfix) {
        this.readonlyPostfix = readonlyPostfix;
    }

    public String getReadonlyPostfix() {
        return readonlyPostfix;
    }

    protected String getFormCssClasses(final String beanName) {
        final StringBuilder formCssClasses = new StringBuilder();

        formCssClasses.append(beanName).append("-form");
        if (readonly)
            formCssClasses.append(readonlyExtension);

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
                        .name(beanName + (readonly ? readonlyExtension : ""))
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
        return getTextField(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setValue(value)
                        .setFieldLabel(fieldLabel)
                        .setInputType(type)
                        .setRequired(required)
                        .setPlaceholder(placeholder)
                        .setDisabled(disabled)
                        .setHelpText(helpText));
    }

    public DivTag getTextField(final HFHParameters params) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());
        final LabelTag label =
                getLabel(params.getFieldLabel(), fieldId, params.isRequired(), params.getLabelExtraCssClasses());

        final InputTag input =
                getInputTag(
                        params.getInputType(),
                        fieldId,
                        params.getField(),
                        params.getValue(),
                        params.isReadonly(),
                        params.getTagExtraCssClasses());

        if (params.isRequired() && useRequiredInHtml)
            input.required();
        if (params.getPlaceholder() != null)
            input.placeholder(params.getPlaceholder());
        if (params.isDisabled())
            input.disabled();
        if (params.isReadonly())
            input.readonly();
        if (!params.isAutocomplete())
            input.attribute("autocomplete", "off");

        return getFormGroup(label, input, params.getHelpText(), params.getGroupExtraCssClasses());
    }

    protected String getFieldId(final String field, final long idBean, final boolean readonly) {
        return getFieldId(field, idBean, null, readonly);
    }

    protected String getFieldId(final String field, final long idBean, final String idNamePostfix, final boolean readonly) {
        final String readonlyIndication = readonly ? readonlyPostfix : "";

        if (Strings.isEmpty(idNamePostfix))
            return field + "_" + idBean + readonlyIndication;

        return field + "_" + idNamePostfix + "_" + idBean + readonlyIndication;
    }

    // For possible backward compatibility with older projects
    // THIS FUNCTION WILL BE DEPRECATED IN NEXT RELEASE AND REMOVED IN THE FUTURE -- DO NOT USE
    protected InputTag getInputTag(
            final InputTag.InputType type,
            final String id,
            final String name,
            final String value)
    {
        return getInputTag(type, id, name, value, false);
    }

    protected InputTag getInputTag(
            final InputTag.InputType type,
            final String id,
            final String name,
            final String value,
            final boolean readonly)
    {
        return getInputTag(type, id, name, value, readonly, null);
    }

    protected InputTag getInputTag(
            final InputTag.InputType type,
            final String id,
            final String name,
            final String value,
            final boolean readonly,
            final String extraCssClasses)
    {
        if (type == InputTag.InputType.DATE)
            return new InputTag(inputTypeForDateFields == null ? type : inputTypeForDateFields)
                    .cssClass(CssClasses.start("form-control").add(cssClassForDateFields, !readonly).add(extraCssClasses).get())
                    .id(id).name(name).value(value);

        if (type == InputTag.InputType.TIME)
            return new InputTag(readonly || inputTypeForTimeFields == null ? type : inputTypeForTimeFields)
                    .cssClass(CssClasses.start("form-control").add(cssClassForTimeFields).add(extraCssClasses).get())
                    .id(id).name(name).value(value);

        if (type == InputTag.InputType.NUMBER)
            return new InputTag(inputTypeForNumberFields == null ? type : inputTypeForNumberFields)
                    .cssClass(CssClasses.start("form-control").add(cssClassForNumberFields).add(extraCssClasses).get())
                    .id(id).name(name).value(value);

        if (type == InputTag.InputType.FILE) {
            final InputTag fileInput = new InputTag(InputTag.InputType.FILE);
            if (!Strings.isEmpty(cssClassForFileFields))
                fileInput.cssClass(CssClasses.start(cssClassForFileFields).add(extraCssClasses).get());
            if (!Strings.isEmpty(value))
                fileInput.placeholder(value);
            return fileInput.id(id).name(name);
        }

        return new InputTag(type)
                .cssClass(CssClasses.start("form-control").add(extraCssClasses).get())
                .id(id)
                .name(name)
                .value(value);
    }

    public DivTag getFormGroup() {
        return new DivTag().cssClass("form-group");
    }

    public DivTag getFormGroup(final String extraCssClasses) {
        return new DivTag().cssClass(CssClasses.start("form-group").add(extraCssClasses).get());
    }

    public DivTag getFormGroup(final LabelTag label, final Tag field) {
        return getFormGroup(label, field, null);
    }

    public DivTag getFormGroup(final LabelTag label, final Tag field, final String helpText) {
        return getFormGroup(label, field, helpText, null);
    }

    public DivTag getFormGroup(final LabelTag label, final Tag field, final String helpText, final String extraCssClasses) {
        final DivTag formGroup =
                new DivTag().cssClass(CssClasses.start("form-group").add(extraCssClasses).get())
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

    protected LabelTag getLabel(final String fieldLabel, final boolean required) {
        return getLabel(fieldLabel, null, required);
    }

    protected LabelTag getLabel(final String fieldLabel, final String fieldId, final boolean required) {
        return getLabel(fieldLabel, fieldId, required, null);
    }

    protected LabelTag getLabel(
            final String fieldLabel,
            final String fieldId,
            final boolean required,
            final String extraCssClasses)
    {
        final LabelTag label;
        if (fieldId == null)
            label = new LabelTag(getLabelText(fieldLabel, required));
        else
            label = new LabelTag(getLabelText(fieldLabel, required), fieldId);

        final StringBuilder cssClasses = new StringBuilder();
        if (inlineWithoutLabels)
            cssClasses.append("sr-only");
        if (horizontal)
            cssClasses.append(getHorizontalLabelClasses());
        if (!Strings.isEmpty(extraCssClasses)) {
            if (cssClasses.length() > 0)
                cssClasses.append(" ");
            cssClasses.append(extraCssClasses);
        }
        if (cssClasses.length() > 0)
            label.cssClass(cssClasses.toString());

        return label;
    }

    protected String getLabelText(final String fieldLabel, final boolean required) {
        if (required && !readonly)
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

    public ButtonTag getSubmitButtonTag(final String beanName, final long idBean, final String buttonLabel) {
        //return getSubmitButtonTag(beanName, idBean, buttonLabel, null);

        return getSubmitButtonTag(
                new HFHParameters()
                        .setBeanName(beanName)
                        .setIdBean(idBean)
                        .setButtonLabel(buttonLabel));
    }

    public ButtonTag getSubmitButtonTag(final HFHParameters params) {
        return getSubmitButtonTag(params, null);
    }

    public ButtonTag getSubmitButtonTag(
            final String beanName,
            final long idBean,
            final String buttonLabel,
            final String extraCssClasses)
    {
        return getSubmitButtonTag(
                new HFHParameters()
                        .setBeanName(beanName)
                        .setIdBean(idBean)
                        .setButtonLabel(buttonLabel), extraCssClasses);
    }

    public ButtonTag getSubmitButtonTag(final HFHParameters params, final String extraCssClasses) {
        return getButtonTag(
                new HFHParameters(params)
                        .setButtonType(ButtonTag.ButtonType.SUBMIT)
                        .setFunctionName("submit")
                        .setCssClasses("btn btn-default" + (extraCssClasses == null ? "" : " " + extraCssClasses)));
    }

    public ButtonTag getButtonTag(
            final ButtonTag.ButtonType type,
            final String beanName,
            final long idBean,
            final String functionName,
            final String buttonLabel,
            final String cssClasses)
    {
        return getButtonTag(
                new HFHParameters()
                        .setButtonType(type)
                        .setBeanName(beanName)
                        .setIdBean(idBean)
                        .setFunctionName(functionName)
                        .setButtonLabel(buttonLabel)
                        .setCssClasses(cssClasses));
    }

    public ButtonTag getButtonTag(final HFHParameters params) {
        return new ButtonTag(params.getButtonType())
                .child(new CData(params.getButtonLabel()))
                .id(getHtmlId(params.getBeanName() + "_" + params.getFunctionName(), params.getIdBean()))
                .cssClass(params.getCssClasses());
    }

    public Tag getSubmitButton(final String beanName, final long id, final String buttonLabel) {
        return getSubmitButton(beanName, id, buttonLabel, false);
    }

    public Tag getSubmitButton(
            final String beanName,
            final long idBean,
            final String buttonLabel,
            final boolean disabled)
    {
        return getSubmitButton(
                new HFHParameters()
                        .setBeanName(beanName)
                        .setIdBean(idBean)
                        .setButtonLabel(buttonLabel)
                        .setDisabled(disabled));
    }

    public Tag getSubmitButton(final HFHParameters params) {
        final ButtonTag submit = getSubmitButtonTag(params);

        if (params.isDisabled())
            submit.disabled();

        if (horizontal)
            return getFormGroup().child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(submit));

        return submit;
    }

    public String getHorizontalFieldClassesWithOffset() {
        return "col-" + horizontalSizeShift
                + "-offset-" + horizontalLabelWidth
                + " col-" + horizontalSizeShift
                + "-" + horizontalFieldWidth;
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
        return getSelectField(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setSelected(selected)
                        .setFieldLabel(fieldLabel)
                        .setSelectPairs(pairs)
                        .setRequired(required)
                        .setDisabled(disabled)
                        .setHelpText(helpText));
    }

    public DivTag getSelectField(final HFHParameters params) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());
        final LabelTag label = getLabel(params.getFieldLabel(), fieldId, params.isRequired(), params.getLabelExtraCssClasses());

        final FormElement formElement;
        if (params.isReadonly())
            formElement = getReadOnlyFormElement(params, fieldId);
        else
            formElement = getReadWriteFormElement(params, fieldId);

        if (params.isRequired() && useRequiredInHtml)
            formElement.required();
        if (params.isDisabled())
            formElement.disabled();

        return getFormGroup(label, formElement, params.getHelpText(), params.getGroupExtraCssClasses());
    }

    private FormElement getReadOnlyFormElement(final HFHParameters params, final String fieldId) {
        String value = "";
        if (!params.getSelected().equals("0"))
            for (IdNamePair pair: params.getSelectPairs())
                if (pair.getId().equals(params.getSelected()))
                    value = pair.getName();

        return getInputTag(InputTag.InputType.TEXT, fieldId, params.getField(), value, true, params.getTagExtraCssClasses())
                .readonly();
    }

    private FormElement getReadWriteFormElement(final HFHParameters params, final String fieldId) {
        final SelectTag select = getSelectTag(params.getField(), fieldId, params.getTagExtraCssClasses());

        for (IdNamePair pair: params.getSelectPairs()) {
            final OptionTag optionTag = new OptionTag(pair.getName(), pair.getId());
            if (pair.getId().equals(params.getSelected()))
                optionTag.selected();
            select.child(optionTag);
        }

        return select;
    }

    protected SelectTag getSelectTag(final String name, final String id) {
        return getSelectTag(name, id, null);
    }

    protected SelectTag getSelectTag(final String name, final String id, final String extraCssClasses) {
        return new SelectTag(name)
                .cssClass(CssClasses.start("form-control").add(extraCssClasses).get())
                .id(id);
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
        return getTextAreaField(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setValue(value)
                        .setFieldLabel(fieldLabel)
                        .setRequired(required)
                        .setDisabled(disabled)
                        .setHelpText(helpText));
    }

    public DivTag getTextAreaField(final HFHParameters params) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());
        final LabelTag label = getLabel(params.getFieldLabel(), fieldId, params.isRequired(), params.getLabelExtraCssClasses());

        final TextareaTag textarea = getTextAreaTag(fieldId, params.getField(), params.getValue(), params.getTagExtraCssClasses());
        if (params.isRequired() && useRequiredInHtml)
            textarea.required();
        if (params.getPlaceholder() != null)
            textarea.placeholder(params.getPlaceholder());
        if (params.isDisabled())
            textarea.disabled();
        if (params.isReadonly())
            textarea.readonly();

        return getFormGroup(label, textarea, params.getHelpText(), params.getGroupExtraCssClasses());
    }

    protected TextareaTag getTextAreaTag(final String id, final String name, final String value) {
        return getTextAreaTag(id, name, value, null);
    }

    protected TextareaTag getTextAreaTag(final String id, final String name, final String value, final String extraCssClasses) {
        return new TextareaTag(value)
                .cssClass(CssClasses.start("form-control").add(extraCssClasses).get())
                .id(id)
                .name(name);
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
        return getCheckboxField(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setChecked(checked)
                        .setFieldLabel(fieldLabel)
                        .setDisabled(disabled)
                        .setCheckboxValue(value)
                        .setIdNameSuffix(idNameSuffix));
    }

    public DivTag getCheckboxField(final HFHParameters params) {
        final DivTag innerPart = getCheckbox(params);

        if (horizontal)
            return getFormGroup(params.getGroupExtraCssClasses())
                    .child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(innerPart));

        if (params.hasGroupExtraCssClasses())
            innerPart.changeCssClasses(CssClasses.start("checkbox").add(params.getGroupExtraCssClasses()).get());

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
        return getCheckbox(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setChecked(checked)
                        .setFieldLabel(fieldLabel)
                        .setDisabled(disabled)
                        .setCheckboxValue(value)
                        .setIdNameSuffix(idNameSuffix));
    }

    protected DivTag getCheckbox(final HFHParameters params) {
        LabelTag labelTag = new LabelTag()
                .child(getCheckboxTag(params))
                .child(new CData(" " + params.getFieldLabel()));
        if (params.hasLabelExtraCssClasses())
            labelTag.cssClass(params.getLabelExtraCssClasses());

        return new DivTag()
                .cssClass("checkbox")
                .child(labelTag);
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
        return getCheckboxTag(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setChecked(checked)
                        .setDisabled(disabled)
                        .setCheckboxValue(value)
                        .setIdNameSuffix(idNameSuffix));
    }

    protected InputTag getCheckboxTag(final HFHParameters params) {
        final InputTag checkbox =
                new InputTag(InputTag.InputType.CHECKBOX)
                        .name(params.getField())
                        .id(getFieldId(params.getField(), params.getIdBean(), params.getIdNameSuffix(), params.isReadonly()));

        if (params.hasTagExtraCssClasses())
            checkbox.cssClass(params.getTagExtraCssClasses());
        if (params.isChecked())
            checkbox.checked();
        if (params.isDisabled())
            checkbox.disabled();
        if (params.isReadonly())
            checkbox.readonly()
                    .attribute("onclick", "return false;")
                    .attribute("onkeydown", "return false;");
        if (!params.isAutocomplete())
            checkbox.attribute("autocomplete", "off");
        if (params.getCheckboxValue() != null)
            checkbox.value(params.getCheckboxValue());

        return checkbox;
    }

    protected String getHtmlId(final String beanName, final long id) {
        return beanName + (readonly ? readonlyExtension : "") + "_" + id;
    }

    public DivTag getFileField(
            final String field,
            final long idBean,
            final String currentFile,
            final String fieldLabel,
            final boolean required)
    {
        return getFileField(field, idBean, currentFile, fieldLabel, required, false);
    }

    public DivTag getFileField(
            final String field,
            final long idBean,
            final String currentFile,
            final String fieldLabel,
            final boolean required,
            final boolean disabled)
    {
        return getFileField(
                new HFHParameters()
                        .setField(field)
                        .setIdBean(idBean)
                        .setCurrentFile(currentFile)
                        .setFieldLabel(fieldLabel)
                        .setRequired(required)
                        .setDisabled(disabled));
    }

    public DivTag getFileField(final HFHParameters params) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());
        final LabelTag label = getLabel(params.getFieldLabel(), null, params.isRequired(), params.getLabelExtraCssClasses());

        final LabelTag uploadButton = new LabelTag(uploadButtonLabel, fieldId).cssClass(uploadButtonCssClasses);

        final InputTag input =
                getInputTag(
                        InputTag.InputType.FILE,
                        fieldId,
                        params.getField(),
                        params.getCurrentFile(),
                        params.isReadonly(),
                        params.getTagExtraCssClasses());

        final SpanTag filenameDisplay =
                new SpanTag(params.hasCurrentFile() ? params.getCurrentFile() : uploadNoFileLabel)
                        .cssClass(uploadFilenameDisplayCssClasses)
                        .id("display_" + fieldId);

        final SpanTag removeFileButton =
                new SpanTag(uploadRemoveFileLabel)
                        .cssClass(uploadRemoveFileCssClasses + (params.hasCurrentFile() ? "" : " hidden"))
                        .id("remove_" + fieldId)
                        .data("fileinput", fieldId)
                        .title(uploadRemoveFileTitle);

        if (params.isRequired() && useRequiredInHtml && !params.hasCurrentFile())
            input.required();
        if (params.isDisabled())
            input.disabled();
        if (params.isReadonly())
            input.readonly();

        return getFileFormGroup(label, uploadButton, input, filenameDisplay, removeFileButton, params.getGroupExtraCssClasses());
    }

    private DivTag getFileFormGroup(
            final LabelTag label,
            final LabelTag uploadButton,
            final InputTag input,
            final SpanTag filenameDisplay,
            final SpanTag removeFileButton,
            final String extraCssClasses)
    {
        final DivTag formGroup =
                new DivTag()
                        .cssClass(CssClasses.start("form-group").add(extraCssClasses).get())
                        .child(label);

        if (horizontal) {
            final DivTag formElements =
                    new DivTag().cssClass(getHorizontalFieldClass())
                            .child(uploadButton)
                            .child(input)
                            .child(filenameDisplay)
                            .child(removeFileButton);
            formGroup.child(formElements);
        } else {
            formGroup.child(uploadButton)
                    .child(input)
                    .child(filenameDisplay)
                    .child(removeFileButton);
        }

        return formGroup;
    }

    public Tag getHiddenInfo(final String field, final long idBean, final String value) {
        return new InputTag(InputTag.InputType.HIDDEN)
                .name(field)
                .id(getFieldId(field, idBean, false))
                .value(value);
    }

    public DivTag getBooleanRadiosField(final HFHParameters params) {
        final LabelTag label = getLabel(params.getFieldLabel(), null, params.isRequired(), params.getLabelExtraCssClasses());

        final DivTag wrapper = new DivTag();
        wrapper.child(getBooleanRadioButton(params, true));
        wrapper.child(getBooleanRadioButton(params, false));

        return getFormGroup(label, wrapper, null, params.getGroupExtraCssClasses());
    }

    // TODO: split into 2 or more functions for clarity and separation of concerns
    private LabelTag getBooleanRadioButton(final HFHParameters params, final boolean positiveValue) {
        final String fieldLabel;
        final String fieldValue;
        if (positiveValue) {
            fieldLabel = params.getYesLabel();
            fieldValue = params.getYesValue();
        } else {
            fieldLabel = params.getNoLabel();
            fieldValue = params.getNoValue();
        }

        final HtmlCodeFragment buttonInside = new HtmlCodeFragment();
        final InputTag radioButton =
                new InputTag(InputTag.InputType.RADIO)
                        .name(params.getField())
                        .value(fieldValue);
        if (params.hasTagExtraCssClasses())
            radioButton.cssClass(params.getTagExtraCssClasses());
        if (params.isChecked() && positiveValue)
            radioButton.checked();
        if (!params.isChecked() && !positiveValue)
            radioButton.checked();
        if (params.isDisabled())
            radioButton.disabled();
        if (params.isReadonly())
            radioButton.readonly()
                    .attribute("onclick", "return false;")
                    .attribute("onkeydown", "return false;");
        if (!params.isAutocomplete())
            radioButton.attribute("autocomplete", "off");
        buttonInside.addTag(radioButton);
        buttonInside.addTag(new CData("&nbsp;" + fieldLabel));

        return new LabelTag().cssClass("radio-inline").addCodeFragment(buttonInside);
    }

    // While waiting for code in client projects to be regenerated to solve this bug.
    // This function will soon be removed. DO NOT USE.
    @Deprecated
    public DivTag getLabelFormField(
            final String value,
            final DbBeanLanguage dbBeanLanguage,
            final HFHParameters params)
    {
        return getLabelFormField(value, dbBeanLanguage, params.isRequired(), params);
    }

    public DivTag getLabelFormField(
            final String value,
            final DbBeanLanguage dbBeanLanguage,
            final boolean required,
            final HFHParameters params)
    {
        final HFHParameters actualParameters = new HFHParameters(params);
        final String iso = dbBeanLanguage.getCapIso();

        actualParameters.setField(params.getField() + iso);
        actualParameters.setValue(value);
        actualParameters.setFieldLabel(params.getFieldLabel() + " " + iso);
        actualParameters.setRequired(required);

        if (params.isAsTextArea())
            return getTextAreaField(actualParameters);

        actualParameters.setInputType(InputTag.InputType.TEXT);
        return getTextField(actualParameters);
    }

    public DivTag getTextLabelField(final HFHParameters params) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());

        return getFormGroup(
                getLabel(params.getFieldLabel(), fieldId, params.isRequired()),
                getTextLabelTag(fieldId, params.getValue()));
    }

    public DivTag getTextLabelField(final HFHParameters params, final Tag adHocRepresentation) {
        final String fieldId = getFieldId(params.getField(), params.getIdBean(), params.isReadonly());

        return getFormGroup(
                getLabel(params.getFieldLabel(), fieldId, params.isRequired()),
                adHocRepresentation.id(fieldId));
    }

    protected Tag getTextLabelTag(final String id, final String value) {
        return new SpanTag(value).cssClass("form-control").id(id);
    }
}

