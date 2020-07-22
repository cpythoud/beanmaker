// beanmaker.js -- v0.6.0 -- 2020-07-17

$.ajaxSetup({cache : false});

const BEANMAKER = { };

BEANMAKER.parseId = function (idString) {
    const parts = idString.split('_');
    return parts[parts.length - 1];
};

BEANMAKER.getItemId = function (linkOrButton) {
    return BEANMAKER.parseId(linkOrButton.attr('id'));
};

BEANMAKER.setupModal = function(linkId, addText, editText, url, formName, extraSetupFunc) {
    $('body').on('click', '.' + linkId, function (event) {
        event.preventDefault();
        const idBean = BEANMAKER.getItemId($(this));
        if (idBean === 0)
            $('#' + linkId + '_dialog_title').text(addText);
        else
            $('#' + linkId + '_dialog_title').text(editText);
        $('#' + linkId + '_dialog_body').load(url, {
            form: formName,
            id: idBean
        }, function() {
            if (extraSetupFunc)
                extraSetupFunc();
            $('#' + linkId + '_dialog').modal('show');
        });
    });
};

BEANMAKER.showErrorMessages = function(idContainer, errors, stylesToAdd, stylesToRemove) {
    const $container = $('#' + idContainer);
    $container.empty();
    if (stylesToAdd)
        $container.addClass(stylesToAdd);
    if (stylesToRemove)
        $container.removeClass(stylesToRemove);

    const errorList = $('<ul>');
    const errorCount = errors.length;
    for (let i = 0; i < errorCount; i++)
        errorList.append('<li>' + errors[i].fieldLabel + ' : ' + errors[i].message + '</li>');
    errorList.appendTo($container);
};

BEANMAKER.showErrorMessage = function (idContainer, message, stylesToAdd, stylesToRemove) {
    const $container = $('#' + idContainer);
    $container.empty();
    if (stylesToAdd)
        $container.addClass(stylesToAdd);
    if (stylesToRemove)
        $container.removeClass(stylesToRemove);

    $('<p>' + message + '</p>').appendTo($container);
};

BEANMAKER.setLoadingStatus = function ($form) {
    $form.find('span.loading').addClass('glyphicon glyphicon-refresh spinning');
    $form.find('button [type="submit"]').disabled = true;
};

BEANMAKER.removeLoadingStatus = function ($form) {
    $form.find('span.loading').removeClass('glyphicon glyphicon-refresh spinning');
    $form.find('button [type="submit"]').disabled = false;
};

BEANMAKER.ajaxSubmitDefaults = {
    action: "",
    formName: "form",
    nextPage: "",
    noSessionPage: "/",
    errorContainerID: "top_message",
    errorStyles: "alert alert-danger",
    elementToScrollUp: "body",
    errorProcessingFunction: undefined,
    systemErrorFunction: function(errorCode) {
        alert("Unexpected Error: " + errorCode);
    }
};

BEANMAKER.ajaxSubmit = function(event, nonDefaultParams, refreshOnSuccessFunction, $this) {
    const params = $.extend({}, BEANMAKER.ajaxSubmitDefaults, nonDefaultParams);
    event.preventDefault();
    let $form;
    if (params.formID)
        $form = $('#' + params.formID);
    else
        $form = $('form[name="' + params.formName + '"]');
    const multipart = $form.attr('enctype') === 'multipart/form-data';
    BEANMAKER.setLoadingStatus($form);
    $.ajax({
        url: params.action,
        type: 'post',
        dataType: 'json',
        data: multipart ? new FormData($form[0]) : $form.serialize(),
        contentType: multipart ? false : 'application/x-www-form-urlencoded; charset=UTF-8',
        processData: !multipart,
        success: function(data) {
            switch (data.status) {
                case 'ok':
                    if (refreshOnSuccessFunction !==undefined) {
                        if ($this !== undefined)
                            refreshOnSuccessFunction($this, data);
                        else
                            refreshOnSuccessFunction(data);
                    } else
                        window.location.href = params.nextPage;
                    break;
                case 'no session':
                    window.location.href = params.noSessionPage;
                    break;
                case 'errors':
                    if (params.errorProcessingFunction === undefined) {
                        BEANMAKER.showErrorMessages(params.errorContainerID, data.errors, params.errorStyles);
                        if (params.elementToScrollUp) {
                            if (params.elementToScrollUp === 'body')
                                window.scrollTo(0, 0);
                            else
                                $(params.elementToScrollUp).scrollTop(0);
                        }
                    } else {
                        params.errorProcessingFunction();
                    }
                    break;
                default:
                    params.systemErrorFunction(data.status);
            }
            BEANMAKER.removeLoadingStatus($form);
        }
    });
};

BEANMAKER.ajaxDelete = function(servlet, bean, id, doneFunction) {
    $.ajax({
        url: servlet,
        type: 'post',
        dataType: 'json',
        data: {
            bean: bean,
            id: id
        },
        success: doneFunction
    });
};

BEANMAKER.scrollToTop = function() {
    window.scrollTo(0, 0);
};

BEANMAKER.postToNewLocation = function(href, parameters) {
    const tempForm = document.createElement('form');
    tempForm.method = 'post';
    tempForm.action = href;

    for (let name in parameters) {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.setAttribute('name', name);
        hiddenInput.setAttribute('value', parameters[name]);
        tempForm.appendChild(hiddenInput);
    }

    $('body').append(tempForm);
    tempForm.submit();
};

BEANMAKER.ajaxItemOrderMoveOneStep = function(servlet, bean, id, direction, doneFunction) {
    $.ajax({
        url: servlet,
        type: 'post',
        dataType: 'json',
        data: {
            bean: bean,
            id: id,
            direction: direction
        },
        success: doneFunction
    });
};

BEANMAKER.ajaxItemOrderMove = function(servlet, bean, id, direction, companionId, doneFunction) {
    $.ajax({
        url: servlet,
        type: 'post',
        dataType: 'json',
        data: {
            bean: bean,
            id: id,
            direction: direction,
            companionId: companionId
        },
        success: doneFunction
    });
};

BEANMAKER.ajaxItemOrderLocalMove = function(servlet, bean, id, direction, companionId, context, doneFunction) {
    $.ajax({
        url: servlet,
        type: 'post',
        dataType: 'json',
        data: {
            bean: bean,
            id: id,
            direction: direction,
            companionId: companionId,
            context: context
        },
        success: doneFunction
    });
};

BEANMAKER.ajaxExecuteOperation = function(servlet, idBean, operation, doneFunction) {
    $.ajax({
        url: servlet,
        type: 'post',
        dataType: 'json',
        data: {
            idBean: idBean,
            operation: operation
        },
        success: doneFunction
    });
};

BEANMAKER.loadOperation = function(idContainer, servlet, idBean, operation, doneFunction) {
    $('#' + idContainer).load(servlet, {
        idBean: idBean,
        operation: operation
    }, doneFunction);
};

BEANMAKER.reloadAfterChange = function() {
    window.location.reload();
};

BEANMAKER.endsWith = function(string, suffix) {
    return string.indexOf(suffix, string.length - suffix.length) !== -1;
};

BEANMAKER.retargetLocationOrReload = function(target) {
    if (BEANMAKER.endsWith(window.location.href, target))
        window.location.reload();
    else
        window.location.href = target;
};

BEANMAKER.reloadToHashAfterChange = function(hash) {
    const target = window.location.pathname + hash;
    if (BEANMAKER.endsWith(window.location.href, target))
        window.location.reload();
    else {
        window.location.href = target;
        window.location.reload();
    }
};

BEANMAKER.reloadAfterChangeNoParameters = function() {
    BEANMAKER.retargetLocationOrReload(window.location.pathname);
};

BEANMAKER.getRequestParametersFromDataAttributes = function ($element, extraParameters) {
    const parameters = $element.data();

    if (extraParameters)
        $.extend(parameters, extraParameters);

    if ($.isEmptyObject(parameters))
        return "";

    let start = true;
    let parameterString = "";
    for (let key in parameters) {
        if (parameters.hasOwnProperty(key)) {
            if (start) {
                parameterString += '?';
                start = false;
            } else
                parameterString += '&';

            parameterString += key;
            parameterString += '=';
            parameterString += parameters[key];
        }
    }

    return parameterString;
};

BEANMAKER.setupDataRefresh = function (id, availabilityCheckURL, dataURL, checkFrequency) {
    if (checkFrequency === undefined)
        checkFrequency = 5;

    window.setTimeout(function () {
        $.ajax({
            url: availabilityCheckURL,
            type: 'post',
            dataType: 'json',
            success: function (data) {
                if (data.available)
                    $('#' + id).load(dataURL);
                else
                    BEANMAKER.setupDataRefresh(id, availabilityCheckURL, dataURL, checkFrequency);
            }
        });
    }, checkFrequency * 1000);
};

BEANMAKER.removeTableLine = function (tableName, id) {
    if (!tableName.startsWith('#'))
        tableName = '#' + tableName;
    $(tableName + "_row_" + id).remove();
    const $counter = $(tableName + "_total");
    let count = Number($counter.text()) - 1;
    $counter.text(count.toString());
    const $shower = $(tableName + "_shown");
    count = Number($shower.text()) - 1;
    $shower.text(count.toString());
};
