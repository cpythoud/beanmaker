package org.beanmaker.util;

import org.jcodegen.html.BrTag;
import org.jcodegen.html.ButtonTag;
import org.jcodegen.html.CData;
import org.jcodegen.html.FieldsetTag;
import org.jcodegen.html.FormTag;
import org.jcodegen.html.HtmlCodeFragment;
import org.jcodegen.html.ImgTag;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.LabelTag;
import org.jcodegen.html.OptionTag;
import org.jcodegen.html.PTag;
import org.jcodegen.html.SelectTag;
import org.jcodegen.html.SpanTag;
import org.jcodegen.html.TableCell;
import org.jcodegen.html.TableTag;
import org.jcodegen.html.Tag;
import org.jcodegen.html.TdTag;
import org.jcodegen.html.TextareaTag;
import org.jcodegen.html.ThTag;
import org.jcodegen.html.TrTag;

import java.util.List;
import java.util.Map;

public class HtmlFormHelper {

    private boolean useTables = false;
    private boolean useThForLabels = true;
    private String tableClass = null;
    private boolean formElementsInPara = false;

    private String notRequiredExtension = "";
    private String requiredExtension = " *";
    private boolean useRequiredInHtml = true;

    private String htmlFormAction = null;
    private boolean htmlFormMultipart = false;

    private String defaultEncoding = "ISO-8859-1";


    public boolean isUseThForLabels() {
        return useThForLabels;
    }

    public void useThForLabels(final boolean useThForLabels) {
        this.useThForLabels = useThForLabels;
    }

    public boolean useTables() {
        return useTables;
    }

    public String getTableClass() {
        return tableClass;
    }

    public void setTableClass(final String tableClass) {
        this.tableClass = tableClass;
    }

    public boolean formElementsInPara() {
        return formElementsInPara;
    }

    public void formElementsInPara(final boolean formElementsInPara) {
        this.formElementsInPara = formElementsInPara;
    }

    public void useTables(final boolean useTables) {
        this.useTables = useTables;
    }

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

	
    public void form(final StringBuilder buf, final String beanName, final long id) {
        form(buf, beanName, id, null);
    }

    public void form(final StringBuilder buf, final String beanName, final long id, final String charset) {
        buf.append(getFormStart(beanName, id, charset));
        buf.append("\n");

        if (useTables) {
            buf.append(getTableStart());
            buf.append("\n");
        }
    }

    protected String getFormStart(final String beanName, final long id, final String charset) {
        final FormTag formTag = new FormTag().id(getHtmlId(beanName, id)).name(beanName).method(FormTag.Method.POST);
        if (htmlFormAction != null)
            formTag.action(htmlFormAction);
        if (htmlFormMultipart)
            formTag.enctype(FormTag.EncodingType.MULTIPART);
        if (charset != null)
            formTag.acceptCharset(charset);
        else
            formTag.acceptCharset(defaultEncoding);

        return formTag.getOpeningTag();
    }

    protected String getTableStart() {
        final TableTag tableTag = new TableTag();
        if (tableClass != null)
            tableTag.cssClass(tableClass);

        return tableTag.getOpeningTag()  + "\n\t<tbody>";
    }
	
	public void hiddenSubmitInput(final StringBuilder buf, final String beanName) {
        hidden(buf, "submitted" + beanName, "true");
	}

    public void hidden(final StringBuilder buf, final String name, final String value) {
        buf.append(new InputTag(InputTag.InputType.HIDDEN).name(name).value(value));
    }
	
	public void select(final StringBuilder buf, final String field, final long idBean, final long selected, final String label, final List<IdNamePair> pairs, final boolean required) {
		select(buf, field, idBean, Long.toString(selected), label, pairs, required);
	}
	
	public void select(final StringBuilder buf, final String field, final long idBean, final String selected, final String label, final List<IdNamePair> pairs, final boolean required) {
        append(buf, field, getLabelTag(field, idBean, label, required), getSelectTag(field, idBean, selected, pairs));
	}

    protected SelectTag getSelectTag(final String field, final long idBean, final String selected, final List<IdNamePair> pairs) {
        final SelectTag selectTag = new SelectTag(field).id(getHtmlId(field, idBean));
        for (IdNamePair pair: pairs) {
            final OptionTag optionTag = new OptionTag(pair.getName(), pair.getId());
            if (pair.getId().equals(selected))
                optionTag.selected();
            selectTag.child(optionTag);
        }

        return selectTag;
    }

    private void append(final StringBuilder buf, final String field, final LabelTag labelTag, final Tag tag) {
        if (useTables) {
            final TrTag trTag = new TrTag();
            trTag.child(getLabelTableCell().child(labelTag));
            trTag.child(new TdTag().child(tag));
            modifyTrTag(trTag, field);
            buf.append(trTag);
        } else if (formElementsInPara) {
            final PTag pTag = new PTag();
            pTag.child(labelTag);
            pTag.child(tag);
            modifyPTag(pTag, field);
            buf.append(pTag);
        } else {
            buf.append(labelTag);
            buf.append(tag);
        }
    }

    protected void modifyTrTag(final TrTag trTag, final String field) { }

    protected void modifyPTag(final PTag pTag, final String field) { }

    private void append(final StringBuilder buf, final String field, final LabelTag labelTag, final HtmlCodeFragment htmlCodeFragment) {
        if (useTables) {
            final TrTag trTag = new TrTag();
            trTag.child(getLabelTableCell().child(labelTag));
            trTag.child(new TdTag().addCodeFragment(htmlCodeFragment));
            modifyTrTag(trTag, field);
            buf.append(trTag);
        } else if (formElementsInPara) {
            final PTag pTag = new PTag();
            pTag.child(labelTag);
            pTag.addCodeFragment(htmlCodeFragment);
            modifyPTag(pTag, field);
            buf.append(pTag);
        } else {
            buf.append(labelTag);
            buf.append(htmlCodeFragment);
        }
    }

    private TableCell getLabelTableCell() {
        if (useThForLabels)
            return new ThTag();

        return new TdTag();
    }

    protected LabelTag getLabelTag(final String field, final long idBean, final String label, final boolean required) {
        final String labelText;
        if (required)
            labelText = label + requiredExtension;
        else
            labelText = label + notRequiredExtension;

        return new LabelTag(labelText, getHtmlId(field, idBean));
    }

    protected LabelTag getLabelTag(final String field, final long idBean, final String idPair) {
        return new LabelTag().forAttr(getHtmlId(field, idBean, idPair));
    }
	
	public void radioButtons(final StringBuilder buf, final String field, final long idBean, final long checked, final String label, final List<IdNamePair> pairs, final boolean required) {
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

    public void input(final StringBuilder buf, final String field, final long idBean, final String value, final String label, final String inputType, final boolean required) {
        input(buf, field, idBean, value, label, inputType, required, null);
    }
	
	public void input(final StringBuilder buf, final String field, final long idBean, final String value, final String label, final String inputType, final boolean required, final Map<String, String> extraParameters) {
        append(buf, field, getLabelTag(field, idBean, label, required), getInputTag(field, idBean, value, inputType, required, extraParameters));
	}

    protected InputTag getInputTag(final String field, final long idBean, final String value, final String inputType, final boolean required, final Map<String, String> extraParameters) {
        final InputTag inputTag = new InputTag(InputTag.InputType.getType(inputType)).id(getHtmlId(field, idBean)).name(field).value(value);
        if (required && useRequiredInHtml)
            inputTag.required();
        addExtraParameters(inputTag, extraParameters);

        return inputTag;
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
	}

    public void checkbox(final StringBuilder buf, final String field, final long idBean, final boolean checked, final String label) {
        final String htmlId = getHtmlId(field, idBean);
        final InputTag checkboxTag = new InputTag(InputTag.InputType.CHECKBOX).id(htmlId).name(field).value("true");
        if (checked)
            checkboxTag.checked();

        if (useTables) {
            final TrTag trTag = new TrTag().child(getLabelTableCell());
            final TdTag tdTag = new TdTag();
            tdTag.child(checkboxTag);
            tdTag.child(new CData("&nbsp;"));
            tdTag.child(new LabelTag(label, htmlId));
            modifyTrTag(trTag, field);
            buf.append(trTag.child(tdTag));
        } else if (formElementsInPara) {
            final PTag pTag = new PTag();
            pTag.child(checkboxTag);
            pTag.child(new CData("&nbsp;"));
            pTag.child(new LabelTag(label, htmlId));
            modifyPTag(pTag, field);
            buf.append(pTag);
        } else {
            final FieldsetTag fieldsetTag = new FieldsetTag().cssClass("cbrbgp");
            final LabelTag labelTag = new LabelTag().forAttr(htmlId);
            labelTag.child(checkboxTag);
            labelTag.child(new CData("&nbsp;"));
            labelTag.child(new SpanTag(label));
            buf.append(fieldsetTag.child(labelTag));
        }
    }

    public void soloCheckbox(final StringBuilder buf, final String name, final long idBean, final String label) {
        final String htmlId = getHtmlId(name, idBean);
        final LabelTag labelTag = new LabelTag().forAttr(htmlId);
        labelTag.child(new InputTag(InputTag.InputType.CHECKBOX).id(htmlId).name(name).value("true"));
        labelTag.child(new CData("&nbsp;"));
        labelTag.child(new SpanTag(label));
        buf.append(labelTag);
    }

	public void textarea(final StringBuilder buf, final String field, final long idBean, final String value, final String label, final boolean required) {
        append(buf, field, getLabelTag(field, idBean, label, required), getTextareaTag(field, idBean, value, required));
	}

    protected TextareaTag getTextareaTag(final String field, final long idBean, final String value, final boolean required) {
        final TextareaTag textareaTag = new TextareaTag(value).id(getHtmlId(field, idBean)).name(field);
        if (required && useRequiredInHtml)
            textareaTag.required();

        return textareaTag;
    }
	
	public void button(final StringBuilder buf, final String type, final String beanName, final long idBean, final String label) {
        buf.append(getButtonTag(type, beanName, idBean, label));
	}

    protected ButtonTag getButtonTag(final String type, final String beanName, final long idBean, final String label) {
        final ButtonTag buttonTag = new ButtonTag(ButtonTag.ButtonType.getType(type), label);
        buttonTag.id(getHtmlId(beanName + "_" + type, idBean));
        return buttonTag;
    }
	
	public void captcha(final StringBuilder buf, final String field, final long idBean, final String label, final String imageSource) {
        final LabelTag labelTag = getLabelTag(field, idBean, label);
        final HtmlCodeFragment captcha = new HtmlCodeFragment();
        captcha.addTag(new ImgTag(imageSource).cssClass("captcha"));
        captcha.addTag(new BrTag());
        final InputTag inputTag = new InputTag(InputTag.InputType.TEXT).id(getHtmlId(field, idBean)).name(field);
        if (useRequiredInHtml)
            inputTag.required();
        captcha.addTag(inputTag);

        append(buf, field, labelTag, captcha);
	}

    public void startButtons(final StringBuilder buf) {
        if (useTables)
            buf.append("<tr><td colspan=\"2\">\n");
        else if (formElementsInPara)
            buf.append("<p>");
    }

    public void endButtons(final StringBuilder buf) {
        if (useTables)
            buf.append("</td></tr>\n");
        else if (formElementsInPara)
            buf.append("</p>");
    }

    public void endForm(final StringBuilder buf) {
        if (useTables)
            buf.append("\t</tbody>\n</table>\n");

        buf.append("</form>\n");
    }

    protected String getHtmlId(final String name, final long idBean) {
        return name + "_" + idBean;
    }

    protected String getHtmlId(final String field, final long idBean, final String idPair) {
        return getHtmlId(field, idBean) + "_" + idPair;
    }

    private void addExtraParameters(final InputTag inputTag, final Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty())
            return;

        for (String key: parameters.keySet())
            inputTag.attribute(key, parameters.get(key));
    }
}

