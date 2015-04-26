function start() {
    var username = "test";
    var sendButton = document.getElementsByClassName("sendButton");
    $.ajax({
        url: "http://localhost:8080/ChatServlet",
        method: 'GET',
        data: {
            type: "connect"
        }
    })
        .done(function(resp) {
            var tmp = resp.split("}");


            for(var i = 0; i < tmp.length; i++) {
                console.log(JSON.parse(tmp[i]));
                addMessage(JSON.stringify(JSON.parse(tmp[i])));
            }
        });
    $(function() {
        $('#chat').perfectScrollbar();
        Ps.initialize(document.getElementById('chat'));
    });
    function addMessageDiv(Text, uname) {
        var newDiv = document.createElement('div');
        newDiv.className = 'message1';
        Text = decodeURIComponent(Text);
        uname = decodeURIComponent(uname);
        newDiv.innerHTML = "<span class = 'nickname'>" + uname + ": " + "</span>"
        + "<img src='resources/remove.png' class='delete'>" + "<img src='resources/edit.png' class='edit'>"
        + "<p class='text'>" + Text + "</p>";
        return newDiv;
    }

    //TODO
    sendButton[0].addEventListener("click", function () {
        var message = document.getElementsByClassName("message")[0].value;
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'POST',
            data: {
                type: "add",
                message: message,
                username: username
            }
        })
            .done(function (msg) {
                addMessage(msg);
            });
        //addDevice(name, message);
    });
    function addMessage(resp) {
        if (username != undefined) {
            var tmp = JSON.parse(resp);
            var mainWindow = document.getElementsByClassName("mainWindow")[0];
            var msg = tmp.message;
            /*document.getElementsByClassName("message")[0].value;*/
            var messageDiv = addMessageDiv(msg, tmp.username);
            mainWindow.appendChild(messageDiv);
            $(messageDiv).hide().fadeIn(150);
            document.getElementsByClassName("message")[0].value = "";
            messageDiv.addEventListener("mouseover", function () {
                this.childNodes[1].style.visibility = "visible";
                this.childNodes[2].style.visibility = "visible";
            });
            messageDiv.addEventListener("mouseout", function () {
                this.childNodes[1].style.visibility = "hidden";
                this.childNodes[2].style.visibility = "hidden";
            });
            messageDiv.childNodes[1].addEventListener("click", function () {
                var todel = this.parentNode;
                $(todel).fadeOut(150, function () {
                    todel.parentNode.removeChild(todel);
                });


            });
            messageDiv.childNodes[2].addEventListener("click", function () {
                var oldmsg = this.parentNode.childNodes[3];
                oldmsg = oldmsg.innerHTML;
                this.parentNode.childNodes[3].innerHTML = prompt("EditMessage", oldmsg);
                $(this.parentNode.childNodes[3]).hide().fadeIn(300);
            });
        }
        else {
            alert("please, enter your nickname");
        }
    }

    function addUserDiv() {
        var newDiv = document.createElement('div');
        newDiv.className = 'onlineUser';
        newDiv.innerHTML = "<p class='text'>" + username + "</p>";
        return newDiv;
    }

    var setNameButton = document.getElementsByClassName("confirmNameButton")[0];
    setNameButton.addEventListener("click", function () {
        username = document.getElementsByClassName("username")[0].value;
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'POST',
            data: {
                type: "log",
                username: username
            }
        })
            .done(function (resp) {
                console.log(resp);
                log(resp);
            })
    });
    //TODO: change for server variant
    function log(resp) {
        if (JSON.parse(resp) == true) {
            var user = document.getElementsByClassName("usersOnline")[0];
            var foreveralone = document.getElementsByClassName("onlineUser")[0];
            if (foreveralone != undefined) {
                user.removeChild(document.getElementsByClassName("onlineUser")[0]);
            }
            var userDiv = addUserDiv();
            user.appendChild(userDiv);
            $(userDiv).hide().fadeIn(150);
        }
    }

    (function checkServ() {
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'GET',
            data: {
                type: "checkServer"
            }
        })
            .done(function () {
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: OK";
            })
            .fail(function () {
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: Unavailable";
            })
            .complete(function() {
                setTimeout(checkServ, 5000);
            })
    })();
}
