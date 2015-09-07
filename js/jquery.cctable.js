;(function ($) {

    function zebra($table) {
        var index = 0;
        $table.find('tbody tr').each(function () {
            if (!$(this).hasClass('tb-filtered')) {
                ++index;
                if (index % 2 == 0)
                    $(this).addClass('alternate');
                else
                    $(this).removeClass('alternate');
            }
        });
    }

    function setCookie($table, col, value) {
        var name = "cctable|" + $table.attr('id') + '|' + col;
        document.cookie = name + "=" + encodeURIComponent(value);
    }

    function readCookies($table) {
        if (document.cookie === "")
            return;

        var startOfName = "cctable|" + $table.attr('id') + "|";
        var cookies = document.cookie.split(";");
        for (var i = 0; i < cookies.length; i++) {
            var nameValuePair = cookies[i].split("=");
            var name = $.trim(nameValuePair[0]);
            if (name.indexOf(startOfName) == 0) {
                var value = decodeURIComponent(nameValuePair[1]);
                var info = name.split("|");
                var field = info[2];

                $table.find('.tb-filter[name="' + field + '"]').val(value);
                filter($table);
            }
        }
    }

    // ------------------------------------------------------------------------

    function updateFilteringCounters($table) {
        var idTable = $table.attr('id');
        var total = $('#' + idTable + "_total").text();
        var filteredOut = $table.find('tr.tb-filtered').length;
        var shown = total - filteredOut;

        $('#' + idTable + "_shown").text(shown);
        $('#' + idTable + "_filtered_out").text(filteredOut);
    }

    function removeFiltering($table) {
        var count = 0;
        $table.find('tr').each(function () {
            $(this).removeClass('tb-filtered');
            ++count;
        });
        updateFilteringCounters($table);
    }

    function clearFilters($table) {
        $table.find('.tb-filter').each(function () {
            $(this).val('');
            setCookie($table, this.name, '');
        });
        removeFiltering($table);
        zebra($table);
    }

    function filter($table) {
        var didFilter = false;
        $table.find('.tb-filter').each(function () {
            var filterName = this.name;
            var filterVal = $.trim($(this).val()).toLowerCase();
            if (filterVal != '') {
                $table.find('td.' + filterName).each(function () {
                    var content;
                    if ($(this).data('filter-value'))
                        content = $(this).data('filter-value').toLowerCase();
                    else
                        content = $(this).text().toLowerCase();
                    if (content.indexOf(filterVal) > -1) {
                        if (!didFilter)
                            $(this).closest('tr').removeClass('tb-filtered');
                    } else {
                        $(this).closest('tr').addClass('tb-filtered');
                    }
                });
                didFilter = true;
            }
            setCookie($table, filterName, filterVal);
        });
        if (!didFilter)
            removeFiltering($table);
        updateFilteringCounters($table);
        zebra($table);
    }

    // ------------------------------------------------------------------------

    var directionHashes = { };

    function sort($table, sortColumn) {
        var directionHash;
        if (directionHashes[$table])
            directionHash = directionHashes[$table];
        else
            directionHash = { };

        if (!directionHash[sortColumn])
            directionHash[sortColumn] = 'asc';

        var sortVals = [];
        var tds = { };

        var index = 0;
        $table.find('td.' + sortColumn).each(function () {
            var $td = $(this);
            var val;
            if ($td.data('sort-value'))
                val = $td.data('sort-value');
            else
                val = $td.text();
            val += '~' + index;
            sortVals.push(val);
            tds[val] = $td.closest('tr');

            ++index;
        });

        sortVals.sort();
        if (directionHash[sortColumn] == 'desc')
            sortVals.reverse();

        var $content = $table.find('tbody');
        $content.empty();
        var length = sortVals.length;
        for(var i = 0; i < length; ++i) {
            $content.append(tds[sortVals[i]]);
        }

        zebra($table);

        // set cookie

        if (directionHash[sortColumn] == 'asc')
            directionHash[sortColumn] = 'desc';
        else
            directionHash[sortColumn] = 'asc';
        directionHashes[$table] = directionHash;
    }

    // ------------------------------------------------------------------------

    $.fn.cctable = function() {
        return this.each(function () {
            var $table = $(this);
            $table.find('input.tb-filter').keyup(function() {
                filter($table);
            });

            $table.find('select.tb-filter').change(function() {
                filter($table);
            });

            $table.find('a.tb-nofilter').click(function (event) {
                event.preventDefault();
                clearFilters($table);
            });

            $table.find('th.tb-sort').click(function () {
                sort($table, $(this).data('sort-class'));
            });

            readCookies($table);

            zebra($table);
        });
    };
})(jQuery);
