// beanmaker.file-upload.js -- v0.1.0 -- 2018-03-10

$(document).ready(function() {

    var $body = $('body');

    $body.on('change', 'input.file', function () {
        var id = $(this).attr('id');
        $('#display_' + id)
            .text($(this).val().replace(/C:\\fakepath\\/i, ''));
        $('#remove_' + id).removeClass('hidden');
        $('#delete_' + id).remove();
    });

    $body.on('click', '.remove-file', function () {
        var id = $(this).data('fileinput');
        $('#' + id).val('');
        $('#display_' + id).text('(no file)');
        $(this).addClass("hidden");
        $(this).closest('form')
            .append('<input type="hidden" id="delete_' + id + '" name="delete_' + id.split('_')[0] + '" value="on">');
    });

});
