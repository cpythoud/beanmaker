// beanmaker.js -- v0.2.0 -- 2015-04-28

$.ajaxSetup({cache : false});

var BEANMAKER = { };

BEANMAKER.getItemId = function (linkOrButton) {
    var parts = linkOrButton.attr('id').split('_');
    return parts[parts.length - 1];
};

BEANMAKER.setupModal = function(linkId, addText, editText, url, formName, extraSetupFunc) {
    $('body').on('click', '.' + linkId, function (event) {
        event.preventDefault();
        var idBean = BEANMAKER.getItemId($(this));
        if (idBean == 0)
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
    var $container = $('#' + idContainer);
    $container.empty();
    if (stylesToAdd)
        $container.addClass(stylesToAdd);
    if (stylesToRemove)
        $container.removeClass(stylesToRemove);

    var errorList = $('<ul>');
    var errorCount = errors.length;
    for (var i = 0; i < errorCount; i++)
        errorList.append('<li>' + errors[i].fieldLabel + ' : ' + errors[i].message + '</li>');
    errorList.appendTo($container);
};

BEANMAKER.showErrorMessage = function (idContainer, message, stylesToAdd, stylesToRemove) {
    var $container = $('#' + idContainer);
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
    systemErrorFunction: function(errorCode) {
        alert("Unexpected Error: " + errorCode);
    }
};

BEANMAKER.ajaxSubmit = function(event, nonDefaultParams, refreshOnSuccessFunction, $this) {
    var params = $.extend({}, BEANMAKER.ajaxSubmitDefaults, nonDefaultParams);
    event.preventDefault();
    var $form = $('form[name="' + params.formName + '"]');
    BEANMAKER.setLoadingStatus($form);
    $.ajax({
        url: params.action,
        type: 'post',
        dataType: 'json',
        data: $form.serialize(),
        success: function(data) {
            switch (data.status) {
                case 'ok':
                    if (refreshOnSuccessFunction != undefined) {
                        if ($this != undefined)
                            refreshOnSuccessFunction($this);
                        else
                            refreshOnSuccessFunction();
                    } else
                        window.location.href = params.nextPage;
                    break;
                case 'no session':
                    window.location.href = params.noSessionPage;
                    break;
                case 'errors':
                    BEANMAKER.showErrorMessages(params.errorContainerID, data.errors, params.errorStyles);
                    if (params.elementToScrollUp) {
                        if (params.elementToScrollUp == 'body')
                            window.scrollTo(0, 0);
                        else
                            $(params.elementToScrollUp).scrollTop(0);
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
    var tempForm = document.createElement('form');
    tempForm.method = 'post';
    tempForm.action = href;

    for (var name in parameters) {
        var hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.setAttribute('name', name);
        hiddenInput.setAttribute('value', parameters[name]);
        tempForm.appendChild(hiddenInput);
    }

    $('body').append(tempForm);
    tempForm.submit();
};