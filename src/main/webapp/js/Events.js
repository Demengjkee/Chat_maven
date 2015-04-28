function start() {
    var username;
    var sendButton = document.getElementsByClassName("sendButton");
    $(function () {
        $('#chat').perfectScrollbar();
        Ps.initialize(document.getElementById('chat'));
    });
    function addMessageDiv(Text, uname) {
        var newDiv = document.createElement('div');
        newDiv.className = 'message1';
        Text = decodeURIComponent(Text);
        uname = decodeURIComponent(uname);
        newDiv.innerHTML = "<span class = 'nickname'>" + uname + ": " + "</span>"
        + "<img src='/resources/remove.png' class='delete'>" + "<img src='/resources/edit.png' class='edit'>"
        + "<p class='text'>" + Text + "</p>";
        return newDiv;
    }
    sendButton[0].addEventListener("click", function () {
        if(username !== undefined) {
            var message = document.getElementsByClassName("message")[0].value;
            $.ajax({
                url: "http://localhost:8080/ChatServlet",
                method: 'POST',
                data: {
                    type: "add",
                    message: message,
                    username: username
                }
            });
        }
        else{
            alert("login pls");
        }

    });
    function addMessage(resp) {
        if (username != undefined) {
            var tmp = $.parseJSON(resp);
            var mainWindow = document.getElementsByClassName("mainWindow")[0];
            var msg = tmp.message;
            var messageDiv = addMessageDiv(msg, tmp.username);
            mainWindow.appendChild(messageDiv);
            $(messageDiv).hide().fadeIn(150);
            document.getElementsByClassName("message")[0].value = "";
            if(tmp.username === username) {
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
        }
        else {
            alert("please, enter your nickname");
        }
    }

    function addUserDiv(user) {
        var newDiv = document.createElement('div');
        newDiv.className = 'onlineUser';
        newDiv.innerHTML = "<p class='text'>" + user + "</p>";
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
        });
    });
    function log(resp) {

        var user = document.getElementsByClassName("usersOnline")[0];
        var oldOnlineList = document.getElementsByClassName("onlineUser");
        if(oldOnlineList.length > 0) {
            while (oldOnlineList.length > 0) {
                oldOnlineList[0].remove();
            }
        }
        var users = JSON.parse(resp).usernames.split(",");
        for(var i = 0; i < users.length; i++) {
            var usr = users[i].replace('[','').replace(']','').replace(' ','');
            if(usr !== "null") {
                var userDiv = addUserDiv(usr);
                user.appendChild(userDiv);
            }
        }
        $(user).hide().fadeIn(150);

    }

    (function poll() {
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'GET'

        })
            .done(function (resp) {
                console.log(resp);
                if (JSON.parse(resp).type === 'add') {
                    addMessage(resp);
                }
                if (JSON.parse(resp).type === 'log') {
                    log(resp);
                }
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: OK";
            })
            .fail(function () {
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: Unavailable";
            })
            .complete(function () {
                poll();
            })
    })();
}
