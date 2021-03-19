;(function ($) {

    function zebra($table, opts) {
        let index = 0;
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
        const name = "cctable|" + $table.attr('id') + '|' + col;
        document.cookie = name + "=" + encodeURIComponent(value);
    }

    function readCookies($table, opts) {
        if (document.cookie === "")
            return;

        const startOfName = "cctable|" + $table.attr('id') + "|";
        const cookies = document.cookie.split(";");
        for (let i = 0; i < cookies.length; i++) {
            const nameValuePair = cookies[i].split("=");
            const name = $.trim(nameValuePair[0]);
            if (name.indexOf(startOfName) === 0) {
                const value = decodeURIComponent(nameValuePair[1]);
                const info = name.split("|");
                const field = info[2];

                $table.find('.' + opts.formElementFilterCssClass + '[name="' + field + '"]').val(value);
                filter($table, opts);
            }
        }
    }

    // ------------------------------------------------------------------------

    function updateFilteringCounters($table, opts) {
        const idTable = $table.attr('id');
        const total = $('#' + idTable + "_total").text();
        const filteredOut = $table.find('tr.' + opts.filteredCssClass).length;
        const shown = total - filteredOut;

        $('#' + idTable + "_shown").text(shown);
        $('#' + idTable + "_filtered_out").text(filteredOut);
    }

    function removeFiltering($table, opts) {
        let count = 0;
        $table.find('tr').each(function () {
            $(this).removeClass(opts.filteredCssClass);
            ++count;
        });
        updateFilteringCounters($table, opts);
    }

    function clearFilters($table, opts) {
        $table.find('.' + opts.formElementFilterCssClass).each(function () {
            $(this).val('');
            setCookie($table, this.name, '');
        });
        removeFiltering($table, opts);
        zebra($table, opts);
    }

    function filter($table, opts) {
        let didFilter = false;
        $table.find('.' + opts.formElementFilterCssClass).each(function () {
            const filterName = this.name;
            const filterVal = $.trim($(this).val());  // ! .toLowerCase();
            if (filterVal !== '') {
                $table.find('td.' + filterName).each(function () {
                    let content;
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
            removeFiltering($table, opts);
        updateFilteringCounters($table, opts);
        zebra($table, opts);
    }

    function advancedSearch(opts) {
        const $form = $('form[name="tb-advanced-search-form"]');
        const $table = $('#' + $form.find('input[name="tb-table-id"]').val());
        const filterName = $form.find('input[name="tb-filter-name"]').val();
        const keywords = $form.find('textarea[name="tb-search-keywords"]').val().trim();
        const andModality = $form.find('input[name="tb-filter-mods"]:checked').val() === 'AND';

        const keywordList = keywords === '' ? [] : keywords.split(/\s+/);

        // ? All basic search elements removed before advanced search (combination would make no sense)
        $table.find('.' + opts.formElementFilterCssClass).each(function () {
            $(this).val('');
        });
        removeFiltering($table, opts);

        if (keywordList.length > 0) {
            if (andModality) {
                $table.find('td.' + filterName).each(function () {
                    let content;
                    if ($(this).data('filter-value'))
                        content = $(this).data('filter-value').toLowerCase();
                    else
                        content = $(this).text().toLowerCase();
                    let foundAll = true;
                    for (let i = 0; i < keywordList.length; ++i) {
                        if (content.indexOf(keywordList[i]) === -1) {
                            foundAll = false;
                            break;
                        }
                    }
                    if (!foundAll)
                        $(this).closest('tr').addClass(opts.filteredCssClass);
                });
            } else {
                $table.find('td.' + filterName).each(function () {
                    let content;
                    if ($(this).data('filter-value'))
                        content = $(this).data('filter-value').toLowerCase();
                    else
                        content = $(this).text().toLowerCase();
                    let missing = true;
                    for (let i = 0; i < keywordList.length; ++i) {
                        if (content.indexOf(keywordList[i]) > -1) {
                            missing = false;
                            break;
                        }
                    }
                    if (missing)
                        $(this).closest('tr').addClass(opts.filteredCssClass);
                });
            }
        }

        updateFilteringCounters($table, opts);
        zebra($table, opts);
        opts.hideAdvancedSearchModale();
    }

    // ------------------------------------------------------------------------

    const directionHashes = { };

    function sort($table, sortColumn, opts) {
        let directionHash;
        if (directionHashes[$table])
            directionHash = directionHashes[$table];
        else
            directionHash = { };

        if (!directionHash[sortColumn])
            directionHash[sortColumn] = 'asc';

        const sortVals = [];
        const tds = { };

        let index = 0;
        $table.find('td.' + sortColumn).each(function () {
            const $td = $(this);
            let val;
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

        const $content = $table.find('tbody');
        $content.empty();
        const length = sortVals.length;
        for(let i = 0; i < length; ++i) {
            $content.append(tds[sortVals[i]]);
        }

        zebra($table, opts);

        // set cookie

        if (directionHash[sortColumn] === 'asc')
            directionHash[sortColumn] = 'desc';
        else
            directionHash[sortColumn] = 'asc';
        directionHashes[$table] = directionHash;
    }

    // ------------------------------------------------------------------------


    function tableShowingAllData($table, opts) {
        return $table.hasClass(opts.showMoreCssClass);
    }

    function tableMaskingSomeData($table, opts) {
        return $table.hasClass(opts.showLessCssClass);
    }

    function tableDoesMasking($table, opts) {
        return tableShowingAllData($table, opts) || tableMaskingSomeData($table, opts);
    }

    function showOrHideColumns($table, opts) {
        if (tableShowingAllData($table, opts)) {
            $table.find('.' + opts.maskableCssClass).removeClass(opts.maskedCssClass);
        }

        if (tableMaskingSomeData($table, opts)) {
            $table.find('.' + opts.maskableCssClass).addClass(opts.maskedCssClass);
        }
    }

    function toggleTableMaskingStatus($table, opts) {
        const $showMoreLink = $('#' + $table.attr('id') + '-masking-link-show');
        const $showLessLink = $('#' + $table.attr('id') + '-masking-link-hide');

        if (tableShowingAllData($table, opts)) {
            $table.removeClass(opts.showMoreCssClass);
            $table.addClass(opts.showLessCssClass);
            $showMoreLink.removeClass(opts.maskedCssClass);
            $showLessLink.addClass(opts.maskedCssClass);
            return;
        }

        if (tableMaskingSomeData($table, opts)) {
            $table.removeClass(opts.showLessCssClass);
            $table.addClass(opts.showMoreCssClass);
            $showMoreLink.addClass(opts.maskedCssClass);
            $showLessLink.removeClass(opts.maskedCssClass);
            return;
        }

        throw "Masking operation called on table that doesn't support masking";
    }


    // ------------------------------------------------------------------------

    $.fn.cctable = function(options) {
        const opts = $.extend({ }, $.fn.cctable.defaults, options);
        const $body = $('body');

        return this.each(function () {
            const $table = $(this);
            $table.find('input.' + opts.formElementFilterCssClass).keyup(function() {
                filter($table, opts);
            });

            $table.find('input.' + opts.formElementFilterCssClass).dblclick(function() {
                opts.showAdvancedSearchModale($table.attr('id'), $(this).attr('name'));
            });

            $body.on('submit', 'form[name="' + opts.advancedSearchFormName + '"]', function (event) {
                event.preventDefault();
                advancedSearch(opts);
            });

            $table.find('select.' + opts.formElementFilterCssClass).change(function() {
                filter($table, opts);
            });

            $table.find('a.' + opts.removeFilteringLinkCssClass).click(function (event) {
                event.preventDefault();
                clearFilters($table, opts);
            });

            $table.find('th.' + opts.thSortableTitleCssClass).click(function () {
                sort($table, $(this).data('sort-class'), opts);
            });

            readCookies($table, opts);

            zebra($table, opts);

            if (tableDoesMasking($table, opts)) {
                showOrHideColumns($table, opts);

                $table.find('a.' + opts.maskingLinkCssClass).on('click', function (event) {
                    event.preventDefault();
                    toggleTableMaskingStatus($table, opts);
                    showOrHideColumns($table, opts);
                });
            }

            $table.find('tbody.' + opts.sortableCssClass).sortable({
                axis: 'y',
                containment: $table,
                cursor: 'grabbing',
                opacity: 0.7,
                update: function (event, ui) {
                    zebra($table, opts);

                    const $item = ui.item;

                    const $prevItem = $item.prev();
                    if ($prevItem.is('tr') && !$prevItem.hasClass(opts.filteredCssClass)) {
                        opts.moveAfterFunction($item.attr('id'), $prevItem.attr('id'));
                        return;
                    }

                    const $nextItem = $item.next();
                    if ($nextItem.is('tr') && !$nextItem.hasClass(opts.filteredCssClass)) {
                        opts.moveBeforeFunction($item.attr('id'), $nextItem.attr('id'));
                        return;
                    }

                    console.log(event);
                    console.log(ui);
                    console.log('Moved element ID = ' + ui.item.attr('id'));
                    console.log('Preceding element = ');
                    console.log(ui.item.prev());
                    console.log('Next element = ');
                    console.log(ui.item.next());
                    console.log('Preceding element ID = ' + ui.item.prev().attr('id'));
                    console.log('Next element ID = ' + ui.item.next().attr('id'));

                    throw 'Could not determine where to move items. Check console.';
                }
            });
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
        sortableCssClass: "tb-sortable",
        moveAfterFunction: function (itemId, moveAfterItemId) {
            console.log('Moved element ID = ' + itemId);
            console.log('To be moved after element with ID = ' + moveAfterItemId);
        },
        moveBeforeFunction: function (itemId, moveBeforeItemId) {
            console.log('Moved element ID = ' + itemId);
            console.log('To be moved before element with ID = ' + moveBeforeItemId);
        },
        advancedSearchFormName: 'tb-advanced-search-form',
        showAdvancedSearchModale: function (idTable, filterName) {
            console.log("SHOW ADVANCED SEARCH MODALE: tb ID = " + idTable + ", name = " + filterName);
        },
        hideAdvancedSearchModale: function () {
            console.log("HIDE ADVANCED SEARCH MODALE");
        },
        zebraCssClass: 'alternate'
    };

})(jQuery);
