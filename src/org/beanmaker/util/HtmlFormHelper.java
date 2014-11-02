package org.beanmaker.util;

import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.CData;
import org.jcodegen.html.DivTag;
import org.jcodegen.html.FormTag;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.LabelTag;
import org.jcodegen.html.OptionTag;
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
    private boolean horizontal = false;

    private String horizontalSizeShift = "sm";
    private int horizontalLabelWidth = 2;
    private int horizontalFieldWidth = 10;


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
    }

    public boolean isInline() {
        return inline;
    }

    public void setHorizontal(final boolean horizontal) {
        this.horizontal = horizontal;
        if (horizontal)
            inline = false;
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
        if (horizontalLabelWidth + horizontalFieldWidth != 12)
            throw new IllegalArgumentException("Column count for label + field is incorrest (not 12): " + horizontalLabelWidth + " + " + horizontalFieldWidth + " = " + (horizontalLabelWidth + horizontalFieldWidth));

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

    public FormTag getForm(final String beanName, final long id) {
        inline = false;
        horizontal = false;
        return new FormTag().role("form").id(getHtmlId(beanName, id)).name(beanName).method(FormTag.Method.POST);
    }

    public FormTag getInlineForm(final String beanName, final long id) {
        final FormTag form = getForm(beanName, id);
        inline = true;
        return form.cssClass("form-inline");
    }

    public FormTag getHorizontalForm(final String beanName, final long id) {
        final FormTag form = getForm(beanName, id);
        horizontal = true;
        return form.cssClass("form-horizontal");
    }

    public InputTag getHiddenSubmitInput(final String beanName) {
        return new InputTag(InputTag.InputType.HIDDEN).name("submitted" + beanName).value("true");
    }

    public DivTag getTextField(final String field, final long idBean, final String value, final String fieldLabel, final InputTag.InputType type, final boolean required) {
        return getTextField(field, idBean, value, fieldLabel, type, required, null);
    }

    public DivTag getTextField(final String field, final long idBean, final String value, final String fieldLabel, final InputTag.InputType type, final boolean required, final String placeholder) {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final InputTag input = new InputTag(type).cssClass("form-control").id(fieldId).name(field).value(value);
        if (required && useRequiredInHtml)
            input.required();
        if (placeholder != null)
            input.placeholder(placeholder);

        return getFormGroup(label, input);
    }

    protected String getFieldId(final String field, final long idBean) {
        return field + "_" + idBean;
    }

    protected DivTag getFormGroup() {
        return new DivTag().cssClass("form-group");
    }

    protected DivTag getFormGroup(final LabelTag label, final Tag field) {
        final DivTag formGroup = new DivTag().cssClass("form-group");

        if (horizontal)
            formGroup.child(new DivTag().cssClass(getHorizontalFieldClass()).child(label).child(field));
        else
            formGroup.child(label).child(field);

        return formGroup;
    }

    protected LabelTag getLabel(final String fieldLabel, final String fieldId, final boolean required) {
        final LabelTag label = new LabelTag(getLabelText(fieldLabel, required), fieldId);

        if (inline)
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
        return "col-" + horizontalSizeShift + "-" + horizontalLabelWidth + " control-label";
    }

    protected String getHorizontalFieldClass() {
        return "col-" + horizontalSizeShift + "-" + horizontalFieldWidth;
    }

    public Tag getSubmitButton(final String beanName, final long id, final String buttonLabel) {
        final ButtonTag submit = new ButtonTag(ButtonTag.ButtonType.SUBMIT, buttonLabel).id(getHtmlId(beanName + "_submit", id)).cssClass("btn btn-default");

        if (horizontal)
            return getFormGroup().child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(submit));

        return submit;
    }

    protected String getHorizontalFieldClassesWithOffset() {
        return "col-" + horizontalSizeShift + "-offset-" + horizontalLabelWidth + " col-" + horizontalSizeShift + "-" + horizontalFieldWidth;
    }

    public DivTag getSelectField(final String field, final long idBean, final long selected, final String fieldLabel, final List<IdNamePair> pairs, final boolean required) {
        return getSelectField(field, idBean, Long.toString(selected), fieldLabel, pairs, required);
    }

    public DivTag getSelectField(final String field, final long idBean, final String selected, final String fieldLabel, final List<IdNamePair> pairs, final boolean required) {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final SelectTag select = new SelectTag(field).cssClass("form-control").id(fieldId);
        if (required && useRequiredInHtml)
            select.required();

        for (IdNamePair pair: pairs) {
            final OptionTag optionTag = new OptionTag(pair.getName(), pair.getId());
            if (pair.getId().equals(selected))
                optionTag.selected();
            select.child(optionTag);
        }

        return getFormGroup(label, select);
    }

    public DivTag getTextAreaField(final String field, final long idBean, final String value, final String fieldLabel, final boolean required) {
        final String fieldId = getFieldId(field, idBean);
        final LabelTag label = getLabel(fieldLabel, fieldId, required);

        final TextareaTag textarea = new TextareaTag(value).cssClass("form-control").id(fieldId).name(field);
        if (required && useRequiredInHtml)
            textarea.required();

        return getFormGroup(label, textarea);
    }

    public DivTag getCheckboxField(final String field, final long idBean, final boolean checked, final String fieldLabel) {
        final DivTag innerPart = getCheckbox(field, idBean, checked, fieldLabel);

        if (horizontal)
            return getFormGroup().child(new DivTag().cssClass(getHorizontalFieldClassesWithOffset()).child(innerPart));

        return innerPart;
    }

    protected DivTag getCheckbox(final String field, final long idBean, final boolean checked, final String fieldLabel) {
        final InputTag checkbox = new InputTag(InputTag.InputType.CHECKBOX).name(field).id(getFieldId(field, idBean));
        if (checked)
            checkbox.checked();

        return new DivTag().cssClass("checkbox").child(new LabelTag().child(checkbox).child(new CData(" " + fieldLabel)));
    }

    protected String getHtmlId(final String beanName, final long id) {
        return beanName + "_" + id;
    }

	
	/*public void radioButtons(final StringBuilder buf, final String field, final long idBean, final long checked, final String label, final List<IdNamePair> pairs, final boolean required) {
		radioButtons(buf, field, idBean, Long.toString(checked), label, pairs, required);
	}
	
	public void radioButtons(final StringBuilder buf, final String field, final long idBean, final String checked, final String label, final List<IdNamePair> pairs, final boolean required) {
        final LabelTag mainLabelTag = getLabelTag(field, idBean, label, required);

        final HtmlCodeFragment htmlCodeFragment = new HtmlCodeFragment();
        for (IdNamePair pair: pairs) {
            final LabelTag labelTag = getLabelTag(field, idBean, pair.getId());
            final InputTag radioTag = getRadioTag(field, idBean, pair.getId()).name(field).value(pair.getId());
            if (pair.getId().equals(checked))
                radioTag.checked();
            htmlCodeFragment.addTag(labelTag.child(radioTag).child(new CData("&nbsp;" + pair.getName())));
            htmlCodeFragment.addTag(new BrTag());
        }
        htmlCodeFragment.removeLastTag();

        append(buf, field, mainLabelTag, htmlCodeFragment);
	}

    private InputTag getRadioTag(final String field, final long idBean, final String idPair) {
        return new InputTag(InputTag.InputType.RADIO).id(getHtmlId(field, idBean, idPair));
    }

	public void radio(final StringBuilder buf, final String field, final long idBean, final boolean checked, final String label) {
        final InputTag radioTag = new InputTag(InputTag.InputType.RADIO).id(getHtmlId(field, idBean)).name(field);
        if (checked)
            radioTag.checked();
        final LabelTag labelTag = new LabelTag(label, getHtmlId(field, idBean));

        if (useTables) {
            final TrTag trTag = new TrTag().child(getLabelTableCell());
            final TdTag tdTag = new TdTag().child(radioTag).child(labelTag);
            buf.append(trTag.child(tdTag));
        } else {
            buf.append(radioTag);
            buf.append(labelTag);
        }
	}*/


    /*private void addExtraParameters(final InputTag inputTag, final Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty())
            return;

        for (String key: parameters.keySet())
            inputTag.attribute(key, parameters.get(key));
    }*/
}

