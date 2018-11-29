var REGISTER = "Register";
var UNREGISTER = "Unregister";
$(function () {
    var REGISTER_BUTTON = $('#registerButton');
    var SEARCH_BUTTON = $('#searchButton');
    var FILES_BUTTON = $('#fileButton');
    var PEERS_BUTTON = $('#peerButton');
    var RESULTS_BUTTON = $('#resultButton');
    var CHECK_BUTTON = $('#checkButton');
    var RUN_QUERY_BUTTON = $('#runQueryButton');
    var PRINT_STATS_BUTTON = $('#printStats');
    var KILL_BUTTON = $('#killButton');

    REGISTER_BUTTON.click(function () {
        if (REGISTER_BUTTON.val() === REGISTER) {
            var serverIP = $('#serverIP').val();
            var serverPort = $('#serverPort').val();
            var nodeIP = $('#nodeIP').val();
            var nodePort = $('#nodePort').val();
            var userName = $('#userName').val();
            URL_FINAL = "http://" + nodeIP + ":" + nodePort;
            this.value = UNREGISTER;
            $.ajax({
                url: URL_FINAL + '/register?' +
                    'serverIP=' + serverIP + '&serverPort=' + serverPort +
                    '&nodeIP=' + nodeIP + '&nodePort=' + nodePort + '&userName=' + userName,
                success: function (data) {
                    console.log(data);
                }
            });

            $.ajax({
                url: URL_FINAL + '/files',
                success: function (data) {
                    data = JSON.parse(data);
                    console.log(data);
                    var table = $("#files");
                    table.empty();
                    $.each(data, function (idx, elem) {
                        table.append('<tr><td>' + elem + '</td></tr>')
                    });

                }
            });

            var set_delay = 50000,
                peer = function () {
                     $.ajax({
                        url: URL_FINAL + '/peers',
                        success: function (data) {
                            data = JSON.parse(data);
                            console.log(data);
                            var table = $("#peers");
                            table.empty;
                            $.each(data, function (idx, elem) {
                                table.append(
                                    "<tr>" +
                                    "<td>" + elem["nodeIP"] + "</td>" +
                                    "<td>" + elem["nodePort"] + "</td>" +
                                    "</tr>"
                                );
                            });
                        }
                    })
                        .done(function (response) {
                            set_delay = 50000000
                        })
                        .always(function () {
                            setTimeout(peer, set_delay);
                        });
                };

            peer();
            // getting files



        } else if (REGISTER_BUTTON.val() === UNREGISTER) {
            nodeIP = $('#nodeIP').val();
            nodePort = $('#nodePort').val();
            URL_FINAL = "http://" + nodeIP + ":" + nodePort;
            this.value = REGISTER;
            $.ajax({
                url: URL_FINAL + '/unregister',
                success: function (data) {
                    var files = $("#files");
                    files.empty();
                    var peers = $("#peers");
                    peers.empty();

                }
            });
        }

    });

    SEARCH_BUTTON.click(function () {
        var query = $('#search').val();
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/findFiles?query=' + query,
            success: function (data) {
            }
        });

        // var set_delay = 5000,
        //     result = function () {
        //         $.ajax({
        //             url: URL_FINAL + '/results',
        //             success: function (data) {
        //                 data = JSON.parse(data);
        //                 var table = $("#results");
        //                 table.empty();
        //                 alert(data);
        //                 $.forEach(data, function (idx, elem) {
        //                     table.append(
        //                         "<tr>" +
        //                         "<td>" + elem["fileOwnerDetails"]["nodeIP"] + "</td>" +
        //                         "<td>" + elem["fileOwnerDetails"]["nodePort"]  + "</td>" +
        //                         "<td>" + elem["files"] + "</td>" +
        //                         "<td>" + elem["noOfHops"] + "</td>" +
        //                         "</tr>"
        //                     );
        //                 });
        //
        //             }
        //         })
        //
        //             .done(function (response) {
        //                 // update the page
        //             })
        //             .always(function () {
        //                 setTimeout(result, set_delay);
        //             });
        //     };
        //
        // result();



    });

    FILES_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/files?',
            success: function (data) {
            }
        });
    });

    PEERS_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/peers?',
            success: function (data) {
            }
        });
    });

    RESULTS_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/results?',
            success: function (data) {
            }
        });
    });


    CHECK_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/check?',
            success: function (data) {
            }
        });
    });


    RUN_QUERY_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/runQueries?',
            success: function (data) {
            }
        });
    });

    PRINT_STATS_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/printStats?',
            success: function (data) {
            }
        });
    });

    KILL_BUTTON.click(function () {
        var nodeIP = $('#nodeIP').val();
        var nodePort = $('#nodePort').val();
        URL_FINAL = "http://" + nodeIP + ":" + nodePort;
        $.ajax({
            url: URL_FINAL + '/kill?',
            success: function (data) {
            }
        });
    });

});
