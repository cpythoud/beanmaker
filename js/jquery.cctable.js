;(function ($) {

    function zebra($table) {
        var index = 0;
        $table.find('tbody tr').each(function () {
            if (!$(this).hasClass(opts.filteredCssClass)) {
                ++index;
                if (index % 2 === 0)
                    $(this).addClass(opts.zebraCssClass);
                else
                    $(this).removeClass(opts.zebraCssClass);
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
            if (name.indexOf(startOfName) === 0) {
                var value = decodeURIComponent(nameValuePair[1]);
                var info = name.split("|");
                var field = info[2];

                $table.find('.' + opts.formElementFilterCssClass + '[name="' + field + '"]').val(value);
                filter($table);
            }
        }
    }

    // ------------------------------------------------------------------------

    function updateFilteringCounters($table) {
        var idTable = $table.attr('id');
        var total = $('#' + idTable + "_total").text();
        var filteredOut = $table.find('tr.' + opts.filteredCssClass).length;
        var shown = total - filteredOut;

        $('#' + idTable + "_shown").text(shown);
        $('#' + idTable + "_filtered_out").text(filteredOut);
    }

    function removeFiltering($table) {
        var count = 0;
        $table.find('tr').each(function () {
            $(this).removeClass(opts.filteredCssClass);
            ++count;
        });
        updateFilteringCounters($table);
    }

    function clearFilters($table) {
        $table.find('.' + opts.formElementFilterCssClass).each(function () {
            $(this).val('');
            setCookie($table, this.name, '');
        });
        removeFiltering($table);
        zebra($table);
    }

    function filter($table) {
        var didFilter = false;
        $table.find('.' + opts.formElementFilterCssClass).each(function () {
            var filterName = this.name;
            var filterVal = $.trim($(this).val()).toLowerCase();
            if (filterVal !== '') {
                $table.find('td.' + filterName).each(function () {
                    var content;
                    if ($(this).data('filter-value'))
                        content = $(this).data('filter-value').toLowerCase();
                    else
                        content = $(this).text().toLowerCase();
                    if (content.indexOf(filterVal) > -1) {
                        if (!didFilter)
                            $(this).closest('tr').removeClass(opts.filteredCssClass);
                    } else {
                        $(this).closest('tr').addClass(opts.filteredCssClass);
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
        if (directionHash[sortColumn] === 'desc')
            sortVals.reverse();

        var $content = $table.find('tbody');
        $content.empty();
        var length = sortVals.length;
        for(var i = 0; i < length; ++i) {
            $content.append(tds[sortVals[i]]);
        }

        zebra($table);

        // set cookie

        if (directionHash[sortColumn] === 'asc')
            directionHash[sortColumn] = 'desc';
        else
            directionHash[sortColumn] = 'asc';
        directionHashes[$table] = directionHash;
    }

    // ------------------------------------------------------------------------


    function tableShowingAllData($table) {
        return $table.hasClass(opts.showMoreCssClass);
    }

    function tableMaskingSomeData($table) {
        return $table.hasClass(opts.showLessCssClass);
    }

    function tableDoesMasking($table) {
        return tableShowingAllData($table) || tableMaskingSomeData($table);
    }

    function showOrHideColumns($table) {
        if (tableShowingAllData($table)) {
            $table.find('.' + opts.maskableCssClass).removeClass(opts.maskedCssClass);
        }

        if (tableMaskingSomeData($table)) {
            $table.find('.' + opts.maskableCssClass).addClass(opts.maskedCssClass);
        }
    }

    function toogleTableMaskingStatus($table) {
        var $showMoreLink = $('#' + $table.attr('id') + '-masking-link-show');
        var $showLessLink = $('#' + $table.attr('id') + '-masking-link-hide');

        if (tableShowingAllData($table)) {
            $table.removeClass(opts.showMoreCssClass);
            $table.addClass(opts.showLessCssClass);
            $showMoreLink.removeClass(opts.maskedCssClass);
            $showLessLink.addClass(opts.maskedCssClass);
            return;
        }

        if (tableMaskingSomeData($table)) {
            $table.removeClass(opts.showLessCssClass);
            $table.addClass(opts.showMoreCssClass);
            $showMoreLink.addClass(opts.maskedCssClass);
            $showLessLink.removeClass(opts.maskedCssClass);
            return;
        }

        throw "Masking operation called on table that doesn't support masking";
    }


    // ------------------------------------------------------------------------

    var opts;

    $.fn.cctable = function(options) {
        opts = $.extend({ }, $.fn.cctable.defaults, options);

        return this.each(function () {
            var $table = $(this);
            $table.find('input.' + opts.formElementFilterCssClass).keyup(function() {
                filter($table);
            });

            $table.find('select.' + opts.formElementFilterCssClass).change(function() {
                filter($table);
            });

            $table.find('a.' + opts.removeFilteringLinkCssClass).click(function (event) {
                event.preventDefault();
                clearFilters($table);
            });

            $table.find('th.' + opts.thSortableTitleCssClass).click(function () {
                sort($table, $(this).data('sort-class'));
            });

            readCookies($table);

            zebra($table);

            if (tableDoesMasking($table)) {
                showOrHideColumns($table);

                $('a.' + opts.maskingLinkCssClass).on('click', function (event) {
                    event.preventDefault();
                    toogleTableMaskingStatus($table);
                    showOrHideColumns($table);
                });
            }
        });
    };

    $.fn.cctable.defaults = {
        formElementFilterCssClass: 'tb-filter',
        removeFilteringLinkCssClass: 'tb-nofilter',
        filteredCssClass: 'tb-filtered',
        thSortableTitleCssClass: 'tb-sort',
        maskedCssClass: 'tb-masked',
        showMoreCssClass: 'tb-show-more',
        showLessCssClass: 'tb-show-less',
        maskableCssClass: 'tb-maskable',
        maskingLinkCssClass: 'tb-masking-link',
        zebraCssClass: 'alternate'
    };

})(jQuery);
