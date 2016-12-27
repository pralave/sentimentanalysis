
var emotionMap = {};

var draw = function (eCounts) {
    console.log(eCounts);

    var sum = parseInt(eCounts.happy) + parseInt(eCounts.sad) + parseInt(eCounts.angry);

    new Chartist.Pie('.ct-chart', {
        series: [parseInt(eCounts.happy), parseInt(eCounts.sad), parseInt(eCounts.angry)]
    }, {
        height: 480,
        startAngle: 270,
        total: sum,
        showLabel: true
    });
};

var setEvents = function () {
    $("#controls").find("button").click(function() {
        var filter = "#" + $(this).html().toLowerCase();
        draw(emotionMap[filter]);
    })
};

window.onload = setEvents();

var file = $.getJSON("../result.json", function (data) {
    data.forEach(function (eCounts) {
        emotionMap[eCounts.filter] = {
            happy: eCounts.happyCount,
            sad: eCounts.sadCount,
            angry: eCounts.angryCount
        };
    });
});