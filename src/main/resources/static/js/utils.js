/**
 * Created by Administrator on 2017/8/10.
 */

var scrollTop1 = function () {
    $("html, body").animate({scrollTop: 0}, "slow");//回到顶部
};

var scrollBottom = function () {
    $('html, body, .content').animate({scrollTop: $(document).height()}, "slow");
};
